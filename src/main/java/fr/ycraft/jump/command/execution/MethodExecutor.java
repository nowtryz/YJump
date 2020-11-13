package fr.ycraft.jump.command.execution;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.InjectionPoint;
import fr.ycraft.jump.command.Provider;
import fr.ycraft.jump.command.SenderType;
import fr.ycraft.jump.command.annotations.*;
import fr.ycraft.jump.command.contexts.ExecutionContext;
import lombok.Value;
import net.nowtryz.mcutils.command.CommandResult;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodExecutor implements Executor {
    private static final Pattern PATTERN_ON_SPACE = Pattern.compile(" ", Pattern.LITERAL);
    private static final Pattern GENERIC_ARG = Pattern.compile("<?(\\w+)>?");

    private final @NotNull Method method;
    private final @NotNull ImmutableList<String> argLine;
    private final @NotNull ImmutableList<GenericArg> genericArgs;
    private final @NotNull ImmutableList<Dependency<?>> dependencies;
    private final @NotNull Map<Key<?>, com.google.inject.Provider<?>> cache = new HashMap<>();
    private final @NotNull ThreadLocal<ExecutionContext> localContext = new ThreadLocal<>();
    private final @NotNull InjectionPoint injectionPoint;
    private @Nullable Object object;
    private final @NotNull Command command;
    private final boolean staticMethod;

    private ImmutableList<ArgProvider> providers;
    private Injector childInjector;
    private Injector injector;

    public interface Factory {
        MethodExecutor create(Method method);
    }

    @Inject
    public MethodExecutor(@Assisted Method method) {
        this.method = method;
        this.object = null;
        this.command = method.getAnnotation(Command.class);
        this.argLine = ImmutableList.copyOf(PATTERN_ON_SPACE.split(this.getCommand()));
        this.staticMethod = (method.getModifiers() & Modifier.STATIC) != 0;

        ImmutableList.Builder<GenericArg> argBuilder = ImmutableList.builder();
        Matcher matcher = Pattern.compile("<(\\w+)>").matcher(this.getCommand());
        while (matcher.find()) argBuilder.add(new GenericArg(
                matcher.group(1),
                this.argLine.indexOf('<' + matcher.group(1) + '>') - 1)
        );

        this.genericArgs = argBuilder.build();
        this.injectionPoint = InjectionPoint.forMethod(method, TypeLiteral.get(method.getDeclaringClass()));

        ImmutableList.Builder<Dependency<?>> builder = ImmutableList.builder();
        for (Dependency<?> dep : injectionPoint.getDependencies()) {
            if (isCacheable(dep.getKey())) {
                builder.add(dep);
            }
        }

        this.dependencies = builder.build();
    }

    private boolean isCacheable(Key<?> key) {
        Class<?> rawType = key.getTypeLiteral().getRawType();
        if (CommandSender.class.isAssignableFrom(rawType) || ExecutionContext.class.isAssignableFrom(rawType)) {
            return false;
        }

        Class<? extends Annotation> annotationType = key.getAnnotationType();
        return annotationType == null || (!annotationType.equals(Arg.class) && !annotationType.equals(Context.class));
    }

    private ExecutionContext getLocalContext() {
        ExecutionContext context = this.localContext.get();
        if (context == null) throw new IllegalStateException("There is no context in this scope");
        return context;
    }

    @Inject
    @SuppressWarnings({"rawtypes", "unchecked"})
    void init(com.google.inject.Provider<Injector> injector) {
        // We use a provider, so AssistedInject doesn't complain about the fact we use the injector
        this.injector = injector.get();

        this.providers = Arrays.stream(method.getAnnotationsByType(Provides.class))
                .map(this::toArgProvider)
                .collect(ImmutableList.toImmutableList());

        this.dependencies.stream()
                .map(Dependency::getKey)
                .forEach(key -> this.cache.put(key, this.injector.getProvider(key)));

        this.childInjector = this.injector.createChildInjector(binder -> {
            binder.bind(CommandSender.class).toProvider(() -> this.getLocalContext().getSender());
            binder.bind(String.class).annotatedWith(Context.class).toProvider(() -> this.getLocalContext().getCommandLabel());
            binder.bind(String[].class).annotatedWith(Context.class).toProvider(() -> this.getLocalContext().getArgs());
            binder.bind(fr.ycraft.jump.command.contexts.Context.class).toProvider(this::getLocalContext);
            binder.bind(ExecutionContext.class).toProvider(this::getLocalContext);

            for (ArgProvider provider : MethodExecutor.this.providers) {
                binder.bind(provider.getProvider().getProvidedClass())
                        .annotatedWith(new ArgImpl(provider.getArg()))
                        .toProvider((javax.inject.Provider) () -> provider
                                .getProvider().provide(getLocalContext().getArgs()[provider.index]));
            }

            for (GenericArg genericArg : MethodExecutor.this.genericArgs) {
                binder.bind(String.class)
                        .annotatedWith(new ArgImpl(genericArg.getArg()))
                        .toProvider(() -> this.getLocalContext().getArgs()[genericArg.getIndex()]);
            }
        });

        if (!this.staticMethod) this.object = this.injector.getInstance(this.method.getDeclaringClass());
    }

    public ArgProvider toArgProvider(Provides provides) {
        String arg = GENERIC_ARG.matcher(provides.target()).replaceFirst("$1");
        int index = this.argLine.indexOf('<' + arg + '>');

        if (index == -1) throw new IllegalArgumentException(
                String.format("Unknown %s argument from command %s", arg, this.command)
        );

        Provider<?> provider = this.injector.getInstance(provides.provider());
        return new ArgProvider(arg, index - 1, provider);
    }

    @Value
    static class ArgProvider {
        String arg;
        int index;
        Provider<?> provider;
    }

    @SuppressWarnings("unchecked")
    private <T> T getCacheInstance(Injector injector, Key<T> key) {
        com.google.inject.Provider<T> provider = (com.google.inject.Provider<T>) this.cache.get(key);
        return Optional.ofNullable(provider)
                .map(com.google.inject.Provider::get)
                .orElseGet(() -> injector.getInstance(key));
    }

    private static Throwable unwrap(ReflectiveOperationException e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            if (cause instanceof ReflectiveOperationException) {
                return unwrap((ReflectiveOperationException) cause);
            } else return cause;
        } else return e;
    }

    public String methodID() {
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

    @Override
    public @NotNull CommandResult execute(ExecutionContext context) throws Throwable {
        // TODO remove timing
        // We keep this timing until we are sure we have a reasonable execution time.
        long start = System.nanoTime();

        this.localContext.set(context);

        Object[] args = this.injectionPoint.getDependencies()
                .stream()
                .map(Dependency::getKey)
                .map(key -> this.getCacheInstance(childInjector, key))
                .toArray(Object[]::new);

        long argsCollected = System.nanoTime();

        try {
            return (CommandResult) method.invoke(this.object, args);
        } catch (ReflectiveOperationException e) {
            throw unwrap(e);
        } finally {
            long end = System.nanoTime();
            System.out.println(String.format(
                    "command execution %07dns (collection %07dns, invocation %07dns)",
                    end - start, argsCollected - start, end - argsCollected
            ));
        }
    }

    @Override
    public @NotNull SenderType getType() {
        return this.command.type();
    }

    @Override
    public String getCommand() {
        return this.command.value();
    }

    @Override
    public boolean isAsync() {
        return this.command.async();
    }

    @Override
    public String getDescription() {
        return this.command.description();
    }

    @Override
    public String getPermission() {
        return this.command.permission();
    }

    @Override
    public String getUsage() {
        return this.command.usage();
    }

    @Override
    public String toString() {
        return "MethodExecutor[method=" + this.methodID() + ']';
    }
}

package fr.ycraft.jump.command.execution;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.InjectionPoint;
import fr.ycraft.jump.command.Provider;
import fr.ycraft.jump.command.annotations.ArgImpl;
import fr.ycraft.jump.command.annotations.Command;
import fr.ycraft.jump.command.annotations.Context;
import fr.ycraft.jump.command.annotations.Provides;
import fr.ycraft.jump.command.contexts.ExecutionContext;
import lombok.Value;
import net.nowtryz.mcutils.command.CommandResult;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodExecutor implements Executor {
    private static final Pattern PATTERN_ON_SPACE = Pattern.compile(" ", Pattern.LITERAL);
    private static final Pattern GENERIC_ARG = Pattern.compile("<?(\\w+)>?");

    private final @NotNull Method method;
    private final @NotNull ImmutableList<String> argLine;
    private final @NotNull ImmutableList<GenericArg> genericArgs;
    private final @Nullable Object object;
    private final @NotNull Command command;
    private final @NotNull InjectionPoint injectionPoint;

    private ImmutableList<ArgProvider> providers;
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

        ImmutableList.Builder<GenericArg> argBuilder = ImmutableList.builder();
        Matcher matcher = Pattern.compile("<(\\w+)>").matcher(this.getCommand());
        while (matcher.find()) argBuilder.add(new GenericArg(
                matcher.group(1),
                this.argLine.indexOf('<' + matcher.group(1) + '>') - 1)
        );

        this.genericArgs = argBuilder.build();
        this.injectionPoint = InjectionPoint.forMethod(method, TypeLiteral.get(method.getDeclaringClass()));

//        ImmutableList.Builder<Dependency<?>> builder = ImmutableList.builder();
//        for (Dependency<?> dep : injectionPoint.getDependencies()) {
//            Class<?> annotationType = dep.getKey().getAnnotationType();
//            if (annotationType == null || !annotationType.equals(Assisted.class)) {
//                builder.add(dep);
//            }
//        }
//
//        ImmutableList<Dependency<?>> dependencies = builder.build();
    }

    @Inject
    void init(com.google.inject.Provider<Injector> injector) {
        // We use a provider, so AssistedInject doesn't complain about the fact we use the injector
        this.injector = injector.get();
        this.providers = Arrays.stream(method.getAnnotationsByType(Provides.class))
                .map(this::toArgProvider)
                .collect(ImmutableList.toImmutableList());
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

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public CommandResult execute(ExecutionContext context) throws Throwable {
        // TODO remove timing
        // We keep this timing until we have a reasonable execution time, 11 ms is too long
        long start = System.currentTimeMillis();
        // TODO try some king of cache to speed up execution, 11 ms is too long
        Injector childInjector = this.injector.createChildInjector(binder -> {


            binder.bind(CommandSender.class).toProvider(context::getSender);
            binder.bind(String.class).annotatedWith(Context.class).toProvider(context::getCommandLabel);
            binder.bind(String[].class).annotatedWith(Context.class).toProvider(context::getArgs);
            binder.bind(ExecutionContext.class).toInstance(context);

            for (ArgProvider provider : MethodExecutor.this.providers) {
                binder.bind(provider.getProvider().getProvidedClass())
                        .annotatedWith(new ArgImpl(provider.getArg()))
                        .toProvider((javax.inject.Provider) () -> provider
                                .getProvider().provide(context.getArgs()[provider.index]));
            }

            for (GenericArg genericArg : MethodExecutor.this.genericArgs) {
                binder.bind(String.class)
                        .annotatedWith(new ArgImpl(genericArg.getArg()))
                        .toInstance(context.getArgs()[genericArg.getIndex()]);
            }
        });

        Object[] args = this.injectionPoint.getDependencies()
                .stream()
                .map(Dependency::getKey)
                .map(childInjector::getInstance)
                .toArray(Object[]::new);

        try {
            return (CommandResult) method.invoke(this.object, args);
        } catch (ReflectiveOperationException e) {
            throw  this.unwrapAndThrow(e);
        } finally {
            System.out.println(System.currentTimeMillis() - start);
        }
    }

    private Throwable unwrapAndThrow(ReflectiveOperationException e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            if (cause instanceof ReflectiveOperationException) {
                return this.unwrapAndThrow((ReflectiveOperationException) cause);
            } else return cause;
        } else return e;
    }

    public String methodID() {
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

    @Override
    public String getCommand() {
        return this.command.value();
    }

    @Override
    public boolean isAsync() {
        return this.command.async();
    }
}

package fr.ycraft.jump.command.execution;

import fr.ycraft.jump.command.annotations.Completer;
import fr.ycraft.jump.command.annotations.Providers;
import fr.ycraft.jump.command.annotations.Provides;
import fr.ycraft.jump.command.contexts.NodeSearchContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class MethodCompleter implements fr.ycraft.jump.command.execution.Completer {
    private final @NonNull Method method;
    private final @Nullable Object object;
    private final @NonNull Provides[] providers;
    private final @NonNull Completer completer;

    public static MethodCompleter from(Method method) {
        Completer completer = method.getAnnotation(Completer.class);
        Provides[] providers = Optional.ofNullable(method.getAnnotation(Providers.class))
                .map(Providers::value)
                .orElseGet(() -> new Provides[0]);

        return new MethodCompleter(method, null, providers, completer);
    }

    @Override
    public List<String> complete(NodeSearchContext nodeSearchContext) {
        return null;
    }

    @Override
    public String getCommand() {
        return this.completer.value();
    }
}

package fr.ycraft.jump.commands.utils;

import com.google.inject.Inject;
import fr.ycraft.jump.manager.JumpManager;
import lombok.RequiredArgsConstructor;
import net.nowtryz.mcutils.command.contexts.CompletionContext;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor(onConstructor_={@Inject})
public class JumpCompleter {
    private final JumpManager manager;

    public List<String> tabComplete(CompletionContext context) {
        return this.manager.getJumps()
                .keySet()
                .stream()
                .filter(s -> s.startsWith(context.getArgument()))
                .collect(Collectors.toList());
    }
}

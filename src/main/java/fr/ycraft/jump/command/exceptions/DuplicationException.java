package fr.ycraft.jump.command.exceptions;

import fr.ycraft.jump.command.execution.Execution;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DuplicationException extends RuntimeException {
    private static final long serialVersionUID = -7887942900292971696L;
    private final Execution present;
    private final Execution duplicate;

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}

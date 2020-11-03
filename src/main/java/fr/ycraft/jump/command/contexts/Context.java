package fr.ycraft.jump.command.contexts;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.bukkit.command.CommandSender;

@Getter
@ToString
@SuperBuilder
@EqualsAndHashCode
@FieldDefaults(makeFinal=true, level= AccessLevel.PROTECTED)
public abstract class Context {
    CommandSender sender;
    String commandLabel;
    String[] args;
}

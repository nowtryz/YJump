package fr.ycraft.jump.command.annotations;

import com.google.inject.internal.Annotations;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.lang.annotation.Annotation;

@RequiredArgsConstructor
@SuppressWarnings("ClassExplicitlyAnnotation")
public class ArgImpl implements Arg, Serializable {
    private static final long serialVersionUID = 7485335873568117446L;

    @NonNull
    @Getter(onMethod_={@Override})
    @Accessors(fluent = true)
    private final String value;

    @Override
    public Class<? extends Annotation> annotationType() {
        return Arg.class;
    }

    @Override
    public int hashCode() {
        // This is specified in java.lang.Annotation.
        return (127 * "value".hashCode()) ^ value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Arg)) {
            return false;
        }

        Arg other = (Arg) o;
        return value.equals(other.value());
    }

    @Override
    public String toString() {
        return "@" + getClass().getName() + "(value=" + Annotations.memberValueString(value) + ")";
    }
}

package fr.ycraft.jump.injection;

import com.google.inject.internal.Annotations;
import com.google.inject.name.Named;
import fr.ycraft.jump.enums.Patterns;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.lang.annotation.Annotation;

@RequiredArgsConstructor
public class PatternedImpl implements Patterned, Serializable {
    private static final long serialVersionUID = 7761105993376171721L;

    @NonNull
    @Getter(onMethod_={@Override})
    @Accessors(fluent = true)
    private final Patterns value;

    @Override
    public Class<? extends Annotation> annotationType() {
        return Patterned.class;
    }

    @Override
    public int hashCode() {
        // This is specified in java.lang.Annotation.
        return (127 * "value".hashCode()) ^ value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Patterned)) {
            return false;
        }

        Patterned other = (Patterned) o;
        return value.equals(other.value());
    }

    @Override
    public String toString() {
        return "@" + Named.class.getName() + "(value=" + Annotations.memberValueString(value.name()) + ")";
    }
}

package fr.ycraft.jump.exceptions;

import fr.ycraft.jump.Text;
import lombok.Getter;

import java.util.Locale;

public class LocaleInitializationException extends ParkourException {
    private static final long serialVersionUID = -6692975269060895475L;
    private final @Getter Locale locale;

    public LocaleInitializationException(Locale locale, Throwable cause) {
        super(String.format("Unable to correctly load %s (%s)", Text.localeToFileName(locale), locale), cause);
        this.locale = locale;
    }
}

package fr.ycraft.jump.command;

public interface Provider<T> {
    Class<T> getProvidedClass();
    T provide(String argument);

    // Able to complete a generic argument ?
}

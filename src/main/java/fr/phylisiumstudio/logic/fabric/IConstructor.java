package fr.phylisiumstudio.logic.fabric;

public interface IConstructor<T> {
    String getName();
    T construct(Object... args);
}

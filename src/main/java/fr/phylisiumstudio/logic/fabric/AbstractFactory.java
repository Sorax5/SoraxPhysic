package fr.phylisiumstudio.logic.fabric;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract factory for creating objects
 * @param <T> the type of object to create
 * @param <C> the type of constructor
 */
public abstract class AbstractFactory<T,C extends IConstructor<T>> {
    private final List<C> constructors;

    /**
     * Create a new abstract factory
     */
    public AbstractFactory() {
        this.constructors = new ArrayList<>();
    }

    /**
     * Construct an object by name
     * @param name the name of the object to construct
     * @return the object
     */
    public T construct(String name, Object... args) {
        for (C constructor : constructors) {
            if (constructor.getName().equals(name)) {
                return constructor.construct(args);
            }
        }
        return null;
    }

    /**
     * Get all the registered names
     * @return the names
     */
    public List<String> getAllRegisteredNames() {
        List<String> names = new ArrayList<>();
        for (C constructor : constructors) {
            names.add(constructor.getName());
        }
        return names;
    }

    /**
     * Register a constructor
     * @param constructor the constructor
     */
    public void registerConstructor(C constructor) {
        constructors.add(constructor);
    }

    /**
     * Unregister a constructor
     * @param constructor the constructor
     */
    public void unregisterConstructor(C constructor) {
        constructors.remove(constructor);
    }

    /**
     * Get the constructor by name
     * @param name the name
     * @return the constructor
     */
    public C getConstructor(String name) {
        for (C constructor : constructors) {
            if (constructor.getName().equals(name)) {
                return constructor;
            }
        }
        return null;
    }

    /**
     * Get all the constructors
     * @return the constructors in a new list
     */
    public List<C> getConstructors() {
        return new ArrayList<>(constructors);
    }

    /**
     * Clear all the constructors
     */
    public void clear() {
        constructors.clear();
    }
}

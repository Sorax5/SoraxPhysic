package fr.phylisiumstudio.logic;

/**
 * Interface for the motion state of an object
 * @param <T> The object to update
 */
public interface MotionState<T extends IRigidBlock> {

    /**
     * Update the object
     * @param object The object to update
     */
    public void update(T object);
}

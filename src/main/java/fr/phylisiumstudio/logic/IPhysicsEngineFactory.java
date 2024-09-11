package fr.phylisiumstudio.logic;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;

import javax.vecmath.Vector3f;

/**
 * Factory for creating physics engine objects
 */
public interface IPhysicsEngineFactory<T,C,W,V> {

    /**
     * Create a new dynamics world
     * @return the new dynamics world
     */
    T createWorld();

    /**
     * Create a new rigid body
     * @param mass the mass of the rigid body
     * @param shape the shape of the rigid body
     * @param transform the transform of the rigid body
     * @return the new rigid body
     */
    C createRigidBody(float mass, W shape, V transform);
}

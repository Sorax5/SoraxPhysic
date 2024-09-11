package fr.phylisiumstudio.bullet;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.Transform;
import fr.phylisiumstudio.logic.IPhysicsEngineFactory;

import javax.vecmath.Vector3f;

public class PhysicsEngineFactory implements IPhysicsEngineFactory<DiscreteDynamicsWorld,RigidBody,CollisionShape,Transform> {
    @Override
    public DiscreteDynamicsWorld createWorld() {
        BroadphaseInterface broadphase = new DbvtBroadphase();
        CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
        CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
        ConstraintSolver solver = new SequentialImpulseConstraintSolver();
        return new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
    }

    @Override
    public RigidBody createRigidBody(float mass, CollisionShape shape, Transform transform) {
        Vector3f localInertia = new Vector3f(0, 0, 0);
        shape.calculateLocalInertia(mass, localInertia);
        RigidBodyConstructionInfo constructionInfo = new RigidBodyConstructionInfo(mass, null, shape, localInertia);
        RigidBody body = new RigidBody(constructionInfo);
        body.setWorldTransform(transform);
        return body;
    }
}

package fr.phylisiumstudio.bullet.fabric.constructors;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.SphereShape;
import fr.phylisiumstudio.bullet.fabric.ShapeConstructor;

/**
 * Constructor for the sphere shape
 */
public class SphereConstructor extends ShapeConstructor {
    @Override
    public String getName() {
        return "sphere";
    }

    @Override
    public CollisionShape construct(Object... args) {
        float radius = (float) args[0];
        return new SphereShape(radius);
    }
}

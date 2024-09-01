package fr.phylisiumstudio.bullet.fabric.constructors;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import fr.phylisiumstudio.bullet.fabric.ShapeConstructor;

import javax.vecmath.Vector3f;

/**
 * Constructor for the cube shape
 */
public class CubeConstructor extends ShapeConstructor {
    @Override
    public String getName() {
        return "cube";
    }

    @Override
    public CollisionShape construct(Object... args) {
        Vector3f size = (Vector3f) args[0];
        return new BoxShape(size);
    }
}

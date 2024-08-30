package fr.phylisiumstudio.soraxPhysic;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.*;
import com.bulletphysics.linearmath.Transform;
import fr.phylisiumstudio.soraxPhysic.models.RigidBlock;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Vector3f;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.bukkit.block.BlockFace.NORTH;
import static org.bukkit.block.BlockFace.SOUTH;

public class PhysicsManager {
    private DiscreteDynamicsWorld dynamicsWorld;
    private final World bukkitWorld;

    private final List<RigidBlock> rigidBlocks;

    private boolean timeFreeze = false;
    private float timespan = 1/20f;
    private int maxSubSteps = 30;

    private boolean running = true;

    private final ExecutorService executorService;
    private final Object lock = new Object();

    private final Logger logger = LoggerFactory.getLogger(PhysicsManager.class);

    public PhysicsManager(World bukkitWorld) {
        this.bukkitWorld = bukkitWorld;
        this.rigidBlocks = new ArrayList<>();

        this.executorService = Executors.newSingleThreadExecutor();
        this.executorService.submit(this::SetupPhysiqueEngine);
    }

    private void SetupPhysiqueEngine(){
        BroadphaseInterface broadphase = new DbvtBroadphase();
        CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
        CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
        ConstraintSolver solver = new SequentialImpulseConstraintSolver();

        this.dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        this.runPhysics();
    }

    private void runPhysics() {
        while (running) {
            try {
                synchronized (lock) {
                    if (dynamicsWorld == null){
                        logger.error("Dynamics world is null");
                        continue;
                    }

                    if (!timeFreeze) {
                        dynamicsWorld.stepSimulation(timespan, maxSubSteps);
                    }
                }
                Thread.sleep(Duration.ofMillis(50));
            } catch (InterruptedException e) {
                logger.info("Physics thread interrupted");
                running = false;
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                running = false;
                logger.error("Error in physics thread", e);
            }
        }
    }

    public RigidBody createBoxShape(Location location, Material material, float mass, float xScale, float yScale, float zScale) {
        BlockDisplay blockDisplay = bukkitWorld.spawn(location, BlockDisplay.class, display -> {
            display.setBlock(material.createBlockData());
            display.setRotation(0, 0);

            Transformation transformation = display.getTransformation();
            org.joml.Vector3f translation = new org.joml.Vector3f(-xScale/2, -yScale/2, -zScale/2);
            org.joml.Vector3f scale = new org.joml.Vector3f(xScale, yScale, zScale);
            display.setTransformation(new Transformation(translation, transformation.getLeftRotation(), scale, transformation.getRightRotation()));
        });
        Interaction hitbox = bukkitWorld.spawn(location, Interaction.class, display -> {
            display.setInteractionHeight(xScale);
            display.setInteractionWidth(zScale);
            display.setResponsive(true);
        });

        Vector3f position = new Vector3f(xScale/2, yScale/2, zScale/2);
        BoxShape boxShape = new BoxShape(position);

        Transform transform = new Transform();
        transform.setIdentity();
        transform.origin.set((float) location.getX(), (float) location.getY(), (float) location.getZ());

        Vector3f localInertia = new Vector3f(0, 0, 0);
        boxShape.calculateLocalInertia(3, localInertia);


        RigidBodyConstructionInfo constructionInfo = new RigidBodyConstructionInfo(mass, null, boxShape, localInertia);
        RigidBody body = new RigidBody(constructionInfo);
        body.setWorldTransform(transform);
        body.setRestitution(0.0f);

        RigidBlock rigidBlock = new RigidBlock(body, blockDisplay, hitbox);
        BukkitMotionState motionState = new BukkitMotionState(rigidBlock);
        body.setMotionState(motionState);

        synchronized (lock){
            dynamicsWorld.addRigidBody(body);
            rigidBlocks.add(rigidBlock);
        }

        ConvertChunk(location.getChunk());

        return body;
    }

    public void createsphereShape(Location location, Material material, float mass, float radius) {
        BlockDisplay blockDisplay = bukkitWorld.spawn(location, BlockDisplay.class, display -> {
            display.setBlock(material.createBlockData());
            display.setRotation(0, 0);
            display.setInterpolationDuration(1);
            display.setTeleportDuration(1);

            Transformation transformation = display.getTransformation();
            org.joml.Vector3f translation = new org.joml.Vector3f(-radius/2, -radius/2, -radius/2);
            org.joml.Vector3f scale = new org.joml.Vector3f(radius, radius, radius);
            display.setTransformation(new Transformation(translation, transformation.getLeftRotation(), scale, transformation.getRightRotation()));
        });
        Interaction hitbox = bukkitWorld.spawn(location, Interaction.class, display -> {
            display.setInteractionHeight(radius);
            display.setInteractionWidth(radius);
            display.setResponsive(true);
        });

        Transformation transformation = blockDisplay.getTransformation();

        SphereShape boxShape = new SphereShape(radius);

        Transform transform = new Transform();
        transform.setIdentity();
        transform.origin.set((float) location.getX(), (float) location.getY(), (float) location.getZ());

        Vector3f localInertia = new Vector3f(0, 0, 0);
        boxShape.calculateLocalInertia(3, localInertia);

        RigidBodyConstructionInfo constructionInfo = new RigidBodyConstructionInfo(mass, null, boxShape, localInertia);
        RigidBody body = new RigidBody(constructionInfo);
        body.setWorldTransform(transform);
        body.setRestitution(0.0f);

        RigidBlock rigidBlock = new RigidBlock(body, blockDisplay, hitbox);
        BukkitMotionState motionState = new BukkitMotionState(rigidBlock);
        body.setMotionState(motionState);

        synchronized (lock){
            dynamicsWorld.addRigidBody(body);
            rigidBlocks.add(rigidBlock);
        }

        ConvertChunk(location.getChunk());
    }

    public void ConvertChunk(Chunk chunk) {
        CompoundShape compoundShape = new CompoundShape();
        final int numThreads = Runtime.getRuntime().availableProcessors(); // Nombre de threads à utiliser
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Liste pour stocker les futurs
        List<Future<Void>> futures = new ArrayList<>();

        // Divisez le travail en tâches plus petites
        for (int x = 0; x < 16; x++) {
            final int currentX = x;
            Future<Void> future = executor.submit(() -> {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < 256; y++) {
                        int newX = chunk.getX() * 16 + currentX;
                        int newY = y;
                        int newZ = chunk.getZ() * 16 + z;

                        Block block = chunk.getBlock(currentX, y, z);
                        if (block.getType() == Material.AIR || block.getType().isTransparent()) continue;

                        // Vérifier si ce bloc est adjacent à un bloc d'air
                        if (isAdjacentToAir(chunk, currentX, y, z)) {
                            // Créer une BoxShape pour ce bloc
                            BoxShape blockShape = new BoxShape(new Vector3f(0.5f, 0.5f, 0.5f));

                            // Définir la transformation pour ce bloc
                            Transform transformShape = new Transform();
                            transformShape.setIdentity();
                            transformShape.origin.set(new Vector3f(newX + 0.5f, newY + 0.5f, newZ + 0.5f));

                            // Ajouter la BoxShape à la CompoundShape
                            synchronized (compoundShape) {
                                compoundShape.addChildShape(transformShape, blockShape);
                            }
                        }
                    }
                }
                return null;
            });
            futures.add(future);
        }

        // Attendez que toutes les tâches soient terminées
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Créer et ajouter la CompoundShape au monde dynamique
        if (compoundShape.getNumChildShapes() > 0) {
            RigidBody rigidBody = createStaticRigidBody(compoundShape);
            dynamicsWorld.addRigidBody(rigidBody);
        }

        executor.shutdown();
    }

    private boolean isAdjacentToAir(Chunk chunk, int x, int y, int z) {
        // Vérifier les blocs adjacents
        return (isAir(chunk, x - 1, y, z) || isAir(chunk, x + 1, y, z) ||
                isAir(chunk, x, y - 1, z) || isAir(chunk, x, y + 1, z) ||
                isAir(chunk, x, y, z - 1) || isAir(chunk, x, y, z + 1));
    }

    private boolean isAir(Chunk chunk, int x, int y, int z) {
        // Assurez-vous que les coordonnées sont dans les limites valides
        if (x < 0 || x >= 16 || y < 0 || y >= 256 || z < 0 || z >= 16) {
            return false;
        }
        Block block = chunk.getBlock(x, y, z);
        return block.getType() == Material.AIR;
    }

    public void linkRigidBody(RigidBody r1, RigidBody r2, BlockFace f1, BlockFace f2) {
        Vector3f relativePosition1 = getOffsetFromBlockFace(f1);
        Vector3f relativePosition2 = getOffsetFromBlockFace(f2);

        relativePosition2.negate();

        Transform transform1 = new Transform();
        Transform transform2 = new Transform();
        r1.getMotionState().getWorldTransform(transform1);
        r2.getMotionState().getWorldTransform(transform2);

        // Créer les transformations locales par rapport aux positions des faces
        Transform localTransform1 = new Transform();
        localTransform1.setIdentity();
        localTransform1.origin.set(relativePosition1);

        Transform localTransform2 = new Transform();
        localTransform2.setIdentity();
        localTransform2.origin.set(relativePosition2);

        // Créer la contrainte générique pour lier les deux RigidBody
        boolean useLinearReferenceFrameA = true;
        Generic6DofConstraint constraint = new Generic6DofConstraint(r1, r2, localTransform1, localTransform2, useLinearReferenceFrameA);

        // Verrouiller tous les degrés de liberté
        constraint.setLinearLowerLimit(new Vector3f(0, 0, 0));
        constraint.setLinearUpperLimit(new Vector3f(0, 0, 0));
        constraint.setAngularLowerLimit(new Vector3f(0, 0, 0));
        constraint.setAngularUpperLimit(new Vector3f(0, 0, 0));

        dynamicsWorld.addConstraint(constraint, true);
    }

    private boolean blockExistsInWorld(int x, int y, int z) {
        return dynamicsWorld.getCollisionObjectArray().stream().anyMatch(collisionObject -> {
            Transform t = new Transform();
            collisionObject.getWorldTransform(t);
            return t.origin.x == x && t.origin.y == y && t.origin.z == z;
        });
    }

    private RigidBody createStaticRigidBody(CompoundShape compoundShape) {
        float mass = 0.0f;
        Vector3f inertia = new Vector3f(0, 0, 0);
        RigidBodyConstructionInfo chunkInfo = new RigidBodyConstructionInfo(mass, null, compoundShape, inertia);
        RigidBody groundBlock = new RigidBody(chunkInfo);
        Transform ntransform = new Transform();
        ntransform.setIdentity();
        groundBlock.setWorldTransform(ntransform);
        return groundBlock;
    }

    public void clearAll() {
        synchronized (lock){
            for (RigidBlock rigidBlock : rigidBlocks) {
                dynamicsWorld.removeRigidBody(rigidBlock.getRigidBody());
                rigidBlock.getBlockDisplay().remove();
                rigidBlock.getInteraction().remove();
            }
        }
    }

    public BukkitTask runOnMainThread(Runnable runnable){
        return Bukkit.getScheduler().runTask(SoraxPhysic.getInstance(), runnable);
    }

    public BukkitTask runOnAsyncThread(Runnable runnable){
        return Bukkit.getScheduler().runTaskAsynchronously(SoraxPhysic.getInstance(), runnable);
    }

    public void setTimeFreeze(boolean timeFreeze) {
        synchronized (lock) {
            this.timeFreeze = timeFreeze;
        }
    }

    public void setTimespan(float timespan) {
        this.timespan = timespan;
    }

    public void setMaxSubSteps(int maxSubSteps) {
        this.maxSubSteps = maxSubSteps;
    }

    public boolean isTimeFreeze() {
        return timeFreeze;
    }

    public float getTimespan() {
        return timespan;
    }

    public List<RigidBlock> getRigidBlocks() {
        return rigidBlocks;
    }

    public World getBukkitWorld() {
        return bukkitWorld;
    }

    public void stop() {
        running = false;
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow(); // Arrête les tâches en cours et rejette les nouvelles tâches
        }
    }

    private Vector3f getOffsetFromBlockFace(BlockFace face) {
        return switch (face) {
            case NORTH -> new Vector3f(0, 0, -0.5f);
            case SOUTH -> new Vector3f(0, 0, 0.5f);
            case EAST -> new Vector3f(0.5f, 0, 0);
            case WEST -> new Vector3f(-0.5f, 0, 0);
            case UP -> new Vector3f(0, 0.5f, 0);
            case DOWN -> new Vector3f(0, -0.5f, 0);
            default -> new Vector3f(0, 0, 0); // Par défaut, aucune déviation
        };
    }
}

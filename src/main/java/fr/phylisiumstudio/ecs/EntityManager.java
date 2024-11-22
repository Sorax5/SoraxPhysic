package fr.phylisiumstudio.ecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityManager {
    private final Map<String, Entity> entities = new HashMap<>();

    public Entity create(String name, Class<?>... components) {
        Entity entity = new Entity();
        for (Class<?> component : components) {
            entity.addComponent(component);
        }
        entities.put(name, entity);
        return entity;
    }

    public List<Entity> getEntities() {
        return this.entities.values().stream().toList();
    }

    public Entity getEntity(String name) {
        return this.entities.get(name);
    }

    public List<Entity> getEntitiesWithComponent(Class<?> component) {
        List<Entity> entities = new ArrayList<>();
        for (Entity entity : this.entities.values()) {
            if (entity.hasComponent(component)) {
                entities.add(entity);
            }
        }
        return entities;
    }
}

package fr.phylisiumstudio.logic;

import java.util.UUID;

public class ItemLinker {
    private final UUID id;

    private UUID firstRigidBody;
    private UUID secondRigidBody;

    public ItemLinker(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public UUID getFirstRigidBody() {
        return firstRigidBody;
    }

    public void setFirstRigidBody(UUID firstRigidBody) {
        this.firstRigidBody = firstRigidBody;
    }

    public UUID getSecondRigidBody() {
        return secondRigidBody;
    }

    public void setSecondRigidBody(UUID secondRigidBody) {
        this.secondRigidBody = secondRigidBody;
    }

    public boolean hasFirstRigidBody() {
        return firstRigidBody != null;
    }

    public boolean hasSecondRigidBody() {
        return secondRigidBody != null;
    }
}

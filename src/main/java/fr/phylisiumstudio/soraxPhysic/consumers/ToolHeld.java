package fr.phylisiumstudio.soraxPhysic.consumers;

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.inventory.ItemStack;

@FunctionalInterface
public interface ToolHeld {
    void onToolHeld(ItemStack itemStack, PlayerInventorySlotChangeEvent event);
}

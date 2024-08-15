package fr.phylisiumstudio.soraxPhysic;

import fr.phylisiumstudio.logic.ItemLinker;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemLinkerManager {
    private Map<ItemLinker, ItemStack> items;

    public ItemLinkerManager() {
        this.items = new HashMap<>();
    }

    public ItemStack CreateLinker(UUID id){
        ItemLinker linker = new ItemLinker(id);
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Linker");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(id.toString());
        meta.setLore(lore);
        item.setItemMeta(meta);

        items.put(linker, item);
        return item;
    }

    public ItemLinker getLinker(UUID id){
        for(ItemLinker linker : items.keySet()){
            if(linker.getId().equals(id)){
                return linker;
            }
        }
        return null;
    }

    public ItemStack getItem(UUID id){
        for(ItemLinker linker : items.keySet()){
            if(linker.getId().equals(id)){
                return items.get(linker);
            }
        }
        return null;
    }
}

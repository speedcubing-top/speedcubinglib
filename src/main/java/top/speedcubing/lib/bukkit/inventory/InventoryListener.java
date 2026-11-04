package top.speedcubing.lib.bukkit.inventory;

import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import top.speedcubing.lib.bukkit.events.inventory.ClickInventoryEvent;
import top.speedcubing.lib.bukkit.events.inventory.CloseInventoryEvent;

public class InventoryListener {

    public static void inventoryClickEvent(InventoryClickEvent e) {
        // top inventory of the view, so clicks in the player's own inventory (raw slot >= size) are covered too
        InventoryHolder holder = e.getInventory().getHolder();
        if (!(holder instanceof InventoryBuilder)) {
            return;
        }
        InventoryBuilder b = (InventoryBuilder) holder;
        int slot = e.getRawSlot();

        Consumer<ClickInventoryEvent> clickEvent = b.allClickEvent;
        if (clickEvent != null) {
            clickEvent.accept(new ClickInventoryEvent((Player) e.getWhoClicked(), e, b));
        }

        clickEvent = b.clickEvents.get(slot);
        if (clickEvent != null) {
            clickEvent.accept(new ClickInventoryEvent((Player) e.getWhoClicked(), e, b));
        }

        // raw slot is -999 for clicks outside the window
        if (slot < 0 || slot >= b.clickable.length || !b.clickable[slot]) {
            e.setCancelled(true);
        }
    }

    public static void inventoryCloseEvent(InventoryCloseEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();
        if (!(holder instanceof InventoryBuilder)) {
            return;
        }
        InventoryBuilder b = (InventoryBuilder) holder;
        if (b.closeInventoryEvent != null) {
            b.closeInventoryEvent.accept(new CloseInventoryEvent((Player) e.getPlayer(), e, b));
        }
    }
}

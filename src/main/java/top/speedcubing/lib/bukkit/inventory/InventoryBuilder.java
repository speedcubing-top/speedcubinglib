package top.speedcubing.lib.bukkit.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import top.speedcubing.lib.bukkit.events.inventory.ClickInventoryEvent;
import top.speedcubing.lib.bukkit.events.inventory.CloseInventoryEvent;
import top.speedcubing.lib.utils.ReflectionUtils;

// The builder is the inventory's holder: InventoryListener resolves it via getHolder(), and it is
// garbage-collected together with its inventory instead of living in a static registry.
public class InventoryBuilder implements InventoryHolder {
    public final Map<Integer, Consumer<ClickInventoryEvent>> clickEvents = new HashMap<>();
    public final boolean[] clickable;
    public Consumer<ClickInventoryEvent> allClickEvent;
    public Consumer<CloseInventoryEvent> closeInventoryEvent;
    private final Inventory inventory;

    public InventoryBuilder(int size, String title) {
        inventory = Bukkit.createInventory(this, size, title);
        clickable = new boolean[size + 36];
    }

    // player is no longer needed (builders aren't registered per player anymore); kept for API compatibility
    public InventoryBuilder(Player player, int size, String title) {
        this(size, title);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setTitle(String title) {
        ReflectionUtils.setField(ReflectionUtils.getField(inventory, inventory.getClass().getSuperclass(), "inventory"), "title", title);
    }

    public InventoryBuilder setCloseEvent(Consumer<CloseInventoryEvent> e) {
        this.closeInventoryEvent = e;
        return this;
    }

    public InventoryBuilder setItem(ItemStack stack, int slot) {
        inventory.setItem(slot, stack);
        return this;
    }

    public InventoryBuilder setItem(ItemStack stack, int... slots) {
        for (int i : slots)
            setItem(stack, i);
        return this;
    }

    public InventoryBuilder setItem(ItemStack stack, int start, int end) {
        for (; start <= end; start++)
            inventory.setItem(start, stack);
        return this;
    }

    public InventoryBuilder setItem(ItemStack stack, Consumer<ClickInventoryEvent> event, int slot) {
        inventory.setItem(slot, stack);
        clickEvents.put(slot, event);
        return this;
    }

    public InventoryBuilder setItem(ItemStack stack, Consumer<ClickInventoryEvent> event, int... slots) {
        for (int i : slots) {
            setItem(stack, event, i);
        }
        return this;
    }

    public InventoryBuilder setItem(ItemStack stack, Consumer<ClickInventoryEvent> event, int start, int end) {
        for (; start <= end; start++) {
            inventory.setItem(start, stack);
            clickEvents.put(start, event);
        }
        return this;
    }

    public InventoryBuilder setClickEvent(Consumer<ClickInventoryEvent> event, int slot) {
        clickEvents.put(slot, event);
        return this;
    }

    public InventoryBuilder setClickEvent(Consumer<ClickInventoryEvent> event, int... slots) {
        for (int i : slots)
            setClickEvent(event, i);
        return this;
    }

    public InventoryBuilder setClickEvent(Consumer<ClickInventoryEvent> event, int start, int end) {
        for (; start <= end; start++)
            clickEvents.put(start, event);
        return this;
    }

    public InventoryBuilder setAllClickEvent(Consumer<ClickInventoryEvent> event) {
        this.allClickEvent = event;
        return this;
    }

    public InventoryBuilder setClickable(boolean flag, int slot) {
        clickable[slot] = flag;
        return this;
    }

    public InventoryBuilder setClickable(boolean flag, int... slots) {
        for (int i : slots)
            setClickable(flag, i);
        return this;
    }

    public InventoryBuilder setClickable(boolean flag, int start, int end) {
        for (; start <= end; start++)
            clickable[start] = flag;
        return this;
    }
}

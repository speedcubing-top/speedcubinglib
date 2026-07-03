package top.speedcubing.lib.bukkit.entity;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import top.speedcubing.lib.bukkit.events.entity.ClickEvent;

public class Hologram {

    public static final Set<Hologram> all = java.util.concurrent.ConcurrentHashMap.newKeySet();
    public Set<PlayerConnection> tracker = new HashSet<>();
    public final Set<String> world = new HashSet<>();
    private Consumer<ClickEvent> event;
    public final boolean autoSpawn;
    public final boolean everyoneCanSee;
    public final EntityArmorStand armorStand;
    public Entity followEntity;
    public Vector followOffset;

    public Hologram(String name, boolean everyoneCanSee, boolean autoSpawn) {
        this.everyoneCanSee = everyoneCanSee;
        this.autoSpawn = autoSpawn;
        armorStand = new EntityArmorStand(((CraftWorld) Bukkit.getWorlds().get(0)).getHandle());
        armorStand.setCustomNameVisible(true);
        armorStand.setGravity(true);
        armorStand.setInvisible(true);
        armorStand.n(true);
        armorStand.setCustomName(name);
        all.add(this);
    }

    public void delete() {
        all.remove(this);
    }

    public Hologram setClickEvent(Consumer<ClickEvent> e) {
        armorStand.n(false);
        this.event = e;
        return this;
    }

    public Consumer<ClickEvent> getClickEvent() {
        return event;
    }

    Set<PlayerConnection> temp = new HashSet<>();

    public Hologram addListener(Player... players) {
        for (Player p : players) {
            tracker.add(((CraftPlayer) p).getHandle().playerConnection);
        }
        return this;
    }

    public Hologram addListener(Collection<Player> players) {
        for (Player p : players) {
            tracker.add(((CraftPlayer) p).getHandle().playerConnection);
        }
        return this;
    }

    public Hologram setListener(Player... players) {
        temp = tracker;
        tracker = new HashSet<>();
        for (Player p : players) {
            tracker.add(((CraftPlayer) p).getHandle().playerConnection);
        }
        return this;
    }

    public Hologram setListener(Collection<Player> players) {
        temp = tracker;
        tracker = new HashSet<>();
        for (Player p : players) {
            tracker.add(((CraftPlayer) p).getHandle().playerConnection);
        }
        return this;
    }

    public boolean hasListener(Player player) {
        return tracker.contains(((CraftPlayer) player).getHandle().playerConnection);
    }

    public Hologram removeListener(Player... players) {
        for (Player p : players) {
            tracker.remove(((CraftPlayer) p).getHandle().playerConnection);
        }
        return this;
    }

    public Hologram undoSetListener() {
        tracker = temp;
        temp = null;
        return this;
    }

    public Hologram world(String... world) {
        this.world.clear();
        this.world.addAll(Arrays.asList(world));
        if (everyoneCanSee) {
            this.tracker.clear();
            for (String s : world) {
                Bukkit.getWorld(s).getPlayers().forEach(a -> this.tracker.add(((CraftPlayer) a).getHandle().playerConnection));
            }
        }
        return this;
    }

    public Hologram changeWorld(String... world) {
        this.world.clear();
        this.world.addAll(Arrays.asList(world));
        if (everyoneCanSee) {
            despawn();
            this.tracker.clear();
            for (String s : world) {
                Bukkit.getWorld(s).getPlayers().forEach(a -> this.tracker.add(((CraftPlayer) a).getHandle().playerConnection));
            }
            spawn();
        }
        return this;
    }

    public Hologram setLocation(double x, double y, double z, float yaw, float pitch) {
        armorStand.setLocation(x, y, z, yaw, pitch);
        return this;
    }

    public Hologram setLocation(Location location) {
        world.add(location.getWorld().getName());
        return setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public Hologram spawn() {
        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(armorStand);
        tracker.forEach(a -> a.sendPacket(packet));
        return this;
    }

    public Hologram despawn() {
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(armorStand.getId());
        tracker.forEach(a -> a.sendPacket(packet));
        return this;
    }

    public Hologram setName(String name) {
        armorStand.setCustomName(name);
        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(armorStand.getId(), armorStand.getDataWatcher(), true);
        tracker.forEach(a -> a.sendPacket(packet));
        return this;
    }

    public void follow(Entity followEntity, Vector followOffset) {
        this.followEntity = followEntity;
        this.followOffset = followOffset;
    }
}

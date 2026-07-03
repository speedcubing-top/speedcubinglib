package top.speedcubing.lib.bukkit.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityStatus;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.speedcubing.lib.api.MojangAPI;
import top.speedcubing.lib.api.mojang.ProfileSkin;
import top.speedcubing.lib.bukkit.events.entity.ClickEvent;
import top.speedcubing.lib.bukkit.packetwrapper.OutScoreboardTeam;
import top.speedcubing.lib.speedcubingLibBukkit;
import top.speedcubing.lib.utils.ReflectionUtils;

public class NPC {
    public static final Set<NPC> all = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private Set<PlayerConnection> tracker = new java.util.HashSet<>();
    public final Set<String> world = new java.util.HashSet<>();
    private Consumer<ClickEvent> event;
    public final boolean autoSpawn;
    public final boolean everyoneCanSee;
    public final EntityPlayer entityPlayer;

    private boolean nameTagHidden;
    private boolean gravity;
    private float spawnBodyYaw;
    private ItemStack itemInHand;
    private final ItemStack[] armor = new ItemStack[4];
    public int ms = 4000;

    public NPC(String name, UUID uuid, boolean enableOuterLayerSkin, boolean everyoneCanSee, boolean autoSpawn) {
        this.everyoneCanSee = everyoneCanSee;
        this.autoSpawn = autoSpawn;
        WorldServer world = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
        entityPlayer = new EntityPlayer(((CraftServer) Bukkit.getServer()).getServer(), world, new GameProfile(uuid == null ? UUID.randomUUID() : uuid, name), new PlayerInteractManager(world));
        if (enableOuterLayerSkin)
            entityPlayer.getDataWatcher().watch(10, (byte) 127);
        all.add(this);
    }

    public void delete() {
        all.remove(this);
    }

    public NPC setClickEvent(Consumer<ClickEvent> e) {
        this.event = e;
        return this;
    }

    public Consumer<ClickEvent> getClickEvent() {
        return event;
    }

    Set<PlayerConnection> temp = new java.util.HashSet<>();

    public NPC addListener(Player... players) {
        for (Player p : players) {
            tracker.add(((CraftPlayer) p).getHandle().playerConnection);
        }
        return this;
    }

    public NPC addListener(Collection<Player> players) {
        for (Player p : players) {
            tracker.add(((CraftPlayer) p).getHandle().playerConnection);
        }
        return this;
    }

    public NPC setListener(Player... players) {
        temp = tracker;
        tracker = new java.util.HashSet<>();
        for (Player p : players) {
            tracker.add(((CraftPlayer) p).getHandle().playerConnection);
        }
        return this;
    }

    public NPC setListener(Collection<Player> players) {
        temp = tracker;
        tracker = new java.util.HashSet<>();
        for (Player p : players) {
            tracker.add(((CraftPlayer) p).getHandle().playerConnection);
        }
        return this;
    }

    public boolean hasListener(Player player) {
        return tracker.contains(((CraftPlayer) player).getHandle().playerConnection);
    }

    public NPC removeListener(Player... players) {
        for (Player p : players) {
            tracker.remove(((CraftPlayer) p).getHandle().playerConnection);
        }
        return this;
    }

    public NPC undoSetListener() {
        tracker = temp;
        temp = null;
        return this;
    }


    public NPC world(String... world) {
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

    public NPC changeWorld(String... world) {
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

    public NPC spawn() {
        float yaw = entityPlayer.yaw;
        entityPlayer.yaw = spawnBodyYaw;
        PacketPlayOutPlayerInfo p1 = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);
        PacketPlayOutNamedEntitySpawn p2 = new PacketPlayOutNamedEntitySpawn(entityPlayer);
        entityPlayer.yaw = yaw;
        PacketPlayOutEntityHeadRotation p3 = new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) ((int) (entityPlayer.yaw * 256 / 360)));
        tracker.forEach(a -> {
                    a.sendPacket(p1);
                    a.sendPacket(p2);
                    a.sendPacket(p3);
                }
        );
        if (nameTagHidden)
            hideNametag();
        if (nameTagHidden)
            hideNametag();
        if (itemInHand != null)
            setItemInHand(itemInHand);
        for (int i = 0; i < 4; i++)
            if (armor[i] != null)
                setArmor(i + 1, armor[i]);
        if (ms != -1)
            hideFromTab(ms);
        return this;
    }


    public NPC animation(int animation) {
        PacketPlayOutAnimation packet = new PacketPlayOutAnimation();
        ReflectionUtils.setField(packet, "a", entityPlayer.getId());
        ReflectionUtils.setField(packet, "b", (byte) animation);
        tracker.forEach(a -> a.sendPacket(packet));
        return this;
    }


    public NPC status(int status) {
        PacketPlayOutEntityStatus packet = new PacketPlayOutEntityStatus();
        ReflectionUtils.setField(packet, "a", entityPlayer.getId());
        ReflectionUtils.setField(packet, "b", (byte) status);
        tracker.forEach(a -> a.sendPacket(packet));
        return this;
    }

    public NPC setGravity(boolean b) {
        this.gravity = b;
        return this;
    }

    public boolean isGravity() {
        return gravity;
    }


    public NPC setSneaking(boolean sneaking) {
        entityPlayer.setSneaking(sneaking);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entityPlayer.getId(), entityPlayer.getDataWatcher(), true);
        tracker.forEach(a -> a.sendPacket(metadata));
        return this;
    }

    public NPC setItemInHand(ItemStack itemInHand) {
        this.itemInHand = itemInHand == null || itemInHand.getType().equals(Material.AIR) ? null : itemInHand;
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(entityPlayer.getId(), 0, CraftItemStack.asNMSCopy(itemInHand));
        tracker.forEach(a -> a.sendPacket(packet));
        return this;
    }

    public NPC setArmor(int i, ItemStack armor) {
        this.armor[i - 1] = armor;
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(entityPlayer.getId(), i, CraftItemStack.asNMSCopy(armor));
        tracker.forEach(a -> a.sendPacket(packet));
        return this;
    }

    public NPC hideNametag() {
        this.nameTagHidden = true;
        PacketPlayOutScoreboardTeam p1 = new OutScoreboardTeam().a("~").e("never").h(1).packet;
        PacketPlayOutScoreboardTeam p2 = new OutScoreboardTeam().a("~").e("never").packet;
        PacketPlayOutScoreboardTeam p3 = new OutScoreboardTeam().a("~").g(Collections.singletonList(entityPlayer.getName())).h(3).packet;
        tracker.forEach(a -> {
            a.sendPacket(p1);
            a.sendPacket(p2);
            a.sendPacket(p3);
        });
        return this;
    }


    public NPC hideFromTab(int ms) {
        this.ms = ms;
        if (ms == -1)
            return this;
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer);
        Set<PlayerConnection> copy = new java.util.HashSet<>(tracker);
        speedcubingLibBukkit.scheduledThreadPool.schedule(() ->
                        copy.forEach(a -> a.sendPacket(packet))
                , ms, TimeUnit.MILLISECONDS);
        return this;
    }

    public NPC setSkin(Player player) {
        Property property = ((CraftPlayer) player).getProfile().getProperties().get("textures").iterator().next();
        return setSkin(property.getValue(), property.getSignature());
    }

    public NPC setSkin(UUID uuid) {
        try {
            ProfileSkin profileSkin = MojangAPI.getSkinByUUID(uuid);
            return setSkin(profileSkin.getSkin().getValue(), profileSkin.getSkin().getSignature());
        } catch (Exception e) {
            return this;
        }
    }

    public NPC setSkin(String name) {
        try {
            ProfileSkin profileSkin = MojangAPI.getSkinByName(name);
            return setSkin(profileSkin.getSkin().getValue(), profileSkin.getSkin().getSignature());
        } catch (Exception e) {
            return this;
        }
    }

    public NPC setSkin(String value, String signature) {
        PropertyMap properties = entityPlayer.getProfile().getProperties();
        properties.removeAll("textures");
        properties.put("textures", new Property("textures", value, signature));
        despawn();
        spawn();
        updateNpcLocation();
        return this;
    }

    public NPC despawn() {
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(entityPlayer.getId());
        tracker.forEach(a -> a.sendPacket(packet));
        return this;
    }

    public NPC updateNpcLocation() {
        PacketPlayOutEntityTeleport p1 = new PacketPlayOutEntityTeleport(entityPlayer.getId(), MathHelper.floor(entityPlayer.locX * 32), MathHelper.floor(entityPlayer.locY * 32), MathHelper.floor(entityPlayer.locZ * 32), (byte) ((int) (entityPlayer.yaw * 256 / 360)), (byte) ((int) (entityPlayer.pitch * 256 / 360)), true);
        PacketPlayOutEntity.PacketPlayOutEntityLook p2 = new PacketPlayOutEntity.PacketPlayOutEntityLook(entityPlayer.getId(), (byte) ((int) (entityPlayer.yaw * 256F / 360F)), (byte) ((int) (entityPlayer.pitch * 256F / 360F)), true);
        PacketPlayOutEntityHeadRotation p3 = new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) ((int) (entityPlayer.yaw * 256F / 360F)));
        tracker.forEach(a -> {
                    a.sendPacket(p1);
                    a.sendPacket(p2);
                    a.sendPacket(p3);
                }
        );
        return this;
    }

    public NPC setSpawnBodyYaw(float spawnBodyYaw) {
        this.spawnBodyYaw = spawnBodyYaw;
        return this;
    }

    public NPC setHideFromTabDelay(int ms) {
        this.ms = ms;
        return this;
    }

    public NPC setLocation(double x, double y, double z, float headRotation, float pitch) {
        entityPlayer.setLocation(x, y, z, headRotation, pitch);
        return this;
    }

    public NPC setLocation(Location location) {
        world.add(location.getWorld().getName());
        return setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }
}

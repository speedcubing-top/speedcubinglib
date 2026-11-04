package top.speedcubing.lib;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;
import top.speedcubing.lib.bukkit.entity.Hologram;
import top.speedcubing.lib.bukkit.entity.NPC;
import top.speedcubing.lib.bukkit.inventory.Glow;
import top.speedcubing.lib.bukkit.inventory.SignBuilder;
import top.speedcubing.lib.bukkit.listeners.PlayerListener;
import top.speedcubing.lib.utils.ReflectionUtils;

public class speedcubingLibBukkit extends JavaPlugin {
    public static final boolean is1_8_8 = Bukkit.getVersion().contains("(MC: 1.8.8)");
    public static final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);

    public void onEnable() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        if (is1_8_8) {
            Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
            ReflectionUtils.setStaticField(Enchantment.class, "acceptingNew", true);

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException ignored) {
            }

            Glow.glow = new Glow(100);
            // on re-enable (/reload, plugman) id 100 is still held by the previous Glow instance (possibly from an old class loader): replace it
            Enchantment old = Enchantment.getById(Glow.glow.getId());
            if (old != null) {
                ((Map<?, ?>) ReflectionUtils.getStaticField(Enchantment.class, "byId")).remove(old.getId());
                ((Map<?, ?>) ReflectionUtils.getStaticField(Enchantment.class, "byName")).remove(old.getName());
            }
            Enchantment.registerEnchantment(Glow.glow);

            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
                try {
                    for (NPC n : NPC.all) {
                        if (n.isGravity()) {
                            n.entityPlayer.g(0, 0);
                            n.updateNpcLocation();
                        }
                    }

                    for (Hologram h : Hologram.all) {
                        if (h.followEntity != null) {
                            h.setLocation(h.followEntity.getLocation().add(h.followOffset));
                            h.tracker.forEach(p -> p.sendPacket(new PacketPlayOutEntityTeleport(h.armorStand)));
                        }
                    }

                    Set<SignBuilder> toRemove = new HashSet<>();
                    for (SignBuilder b : SignBuilder.signs) {
                        if (System.currentTimeMillis() - b.getGeneratedTime() > b.getTimeout()) {
                            toRemove.add(b);
                        }
                    }
                    SignBuilder.signs.removeAll(toRemove);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 0, 1);
        }
    }
}

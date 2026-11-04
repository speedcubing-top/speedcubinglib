package top.speedcubing.lib.bukkit.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInUpdateSign;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import top.speedcubing.lib.api.events.SignUpdateEvent;
import top.speedcubing.lib.bukkit.entity.Hologram;
import top.speedcubing.lib.bukkit.entity.NPC;
import top.speedcubing.lib.bukkit.events.entity.ClickEvent;
import top.speedcubing.lib.bukkit.events.packet.PlayInEvent;
import top.speedcubing.lib.bukkit.inventory.SignBuilder;
import top.speedcubing.lib.speedcubingLibBukkit;
import top.speedcubing.lib.utils.ReflectionUtils;

public class PacketPlayInHandler extends ChannelDuplexHandler {
    private final Player player;

    public PacketPlayInHandler(Player player) {
        this.player = player;
    }

    @Override // In Packet
    public void channelRead(ChannelHandlerContext channel, Object packet) {
        if (packet == null)
            return;

        try {
            if (((PlayInEvent) new PlayInEvent(player, (Packet<?>) packet).call()).isCancelled())
                return;
            super.channelRead(channel, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // callbacks touch Bukkit API and the lib's non-thread-safe NPC/Hologram/SignBuilder sets: run them on the main thread
        if (packet instanceof PacketPlayInUseEntity) {
            PacketPlayInUseEntity p = (PacketPlayInUseEntity) packet;
            int id = (int) ReflectionUtils.getField(p, "a");
            PacketPlayInUseEntity.EnumEntityUseAction action = p.a();
            Bukkit.getScheduler().runTask(speedcubingLibBukkit.getPlugin(speedcubingLibBukkit.class), () -> {
                for (NPC npc : NPC.all) {
                    if (npc.entityPlayer.getId() == id && npc.getClickEvent() != null) {
                        npc.getClickEvent().accept(new ClickEvent(player, action));
                        break;
                    }
                }
                for (Hologram hologram : Hologram.all) {
                    if (hologram.armorStand.getId() == id && hologram.getClickEvent() != null) {
                        hologram.getClickEvent().accept(new ClickEvent(player, action));
                        break;
                    }
                }
            });
        } else if (packet instanceof PacketPlayInUpdateSign) {
            List<String> s = new ArrayList<>();
            for (IChatBaseComponent i : ((PacketPlayInUpdateSign) packet).b())
                s.add(i.getText());
            Bukkit.getScheduler().runTask(speedcubingLibBukkit.getPlugin(speedcubingLibBukkit.class), () -> {
                Set<SignBuilder> toRemove = new HashSet<>();
                for (SignBuilder b : SignBuilder.signs) {
                    if (b.getPlayer() == player) {
                        b.getEvent().accept(new SignUpdateEvent(player, s));
                        toRemove.add(b);
                    }
                }
                SignBuilder.signs.removeAll(toRemove);
            });
        }
    }
}

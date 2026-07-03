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
import org.bukkit.entity.Player;
import top.speedcubing.lib.api.events.SignUpdateEvent;
import top.speedcubing.lib.bukkit.entity.Hologram;
import top.speedcubing.lib.bukkit.entity.NPC;
import top.speedcubing.lib.bukkit.events.entity.ClickEvent;
import top.speedcubing.lib.bukkit.events.packet.PlayInEvent;
import top.speedcubing.lib.bukkit.inventory.SignBuilder;
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
            PlayInEvent event = new PlayInEvent(player, (Packet<?>) packet);
            event.call();
            if (event.isCancelled()) {
                if (packet instanceof PacketPlayInUseEntity) {
                    System.out.println("[DEBUG] PlayInEvent CANCELLED for PacketPlayInUseEntity, player=" + player.getName());
                }
                return;
            }
            super.channelRead(channel, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (packet instanceof PacketPlayInUseEntity) {
            try {
                int id = (int) ReflectionUtils.getField(packet, "a");
                System.out.println("[DEBUG] PacketPlayInUseEntity received: entityId=" + id + ", player=" + player.getName() + ", action=" + ((PacketPlayInUseEntity) packet).a() + ", NPC.all.size=" + NPC.all.size());
                boolean found = false;
                for (NPC npc : NPC.all) {
                    System.out.println("[DEBUG]   Checking NPC: npcEntityId=" + npc.entityPlayer.getId() + ", hasClickEvent=" + (npc.getClickEvent() != null));
                    if (npc.entityPlayer.getId() == id && npc.getClickEvent() != null) {
                        System.out.println("[DEBUG]   NPC MATCH FOUND! Calling click event.");
                        npc.getClickEvent().accept(new ClickEvent(player, ((PacketPlayInUseEntity) packet).a()));
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    System.out.println("[DEBUG]   No NPC match for entityId=" + id);
                }
                for (Hologram hologram : Hologram.all) {
                    if (hologram.armorStand.getId() == id && hologram.getClickEvent() != null) {
                        hologram.getClickEvent().accept(new ClickEvent(player, ((PacketPlayInUseEntity) packet).a()));
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (packet instanceof PacketPlayInUpdateSign) {
            PacketPlayInUpdateSign p = (PacketPlayInUpdateSign) packet;
            Set<SignBuilder> toRemove = new HashSet<>();
            for (SignBuilder b : SignBuilder.signs) {
                if (b.getPlayer() == player) {
                    List<String> s = new ArrayList<>();
                    for (IChatBaseComponent i : p.b())
                        s.add(i.getText());
                    b.getEvent().accept(new SignUpdateEvent(player, s));
                }
                toRemove.add(b);
            }
            SignBuilder.signs.removeAll(toRemove);
        }
    }
}

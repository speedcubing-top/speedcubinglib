package top.speedcubing.lib.bukkit.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_8_R3.Packet;
import org.bukkit.entity.Player;
import top.speedcubing.lib.bukkit.events.packet.PlayOutEvent;

public class PacketPlayOutHandler extends ChannelOutboundHandlerAdapter {

    private final Player player;

    public PacketPlayOutHandler(Player player) {
        this.player = player;
    }

    @Override // Out Packet
    public void write(ChannelHandlerContext channel, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof Packet) {
            boolean cancelled = false;
            try {
                cancelled = ((PlayOutEvent) new PlayOutEvent(player, (Packet<?>) msg).call()).isCancelled();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (cancelled) {
                // drop the packet without failing the write (an empty encoder output would disconnect the player)
                promise.trySuccess();
                return;
            }
        }
        super.write(channel, msg, promise);
    }
}

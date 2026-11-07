package top.speedcubing.lib.bukkit.inventory;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutOpenSignEditor;
import net.minecraft.server.v1_8_R3.PacketPlayOutUpdateSign;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import top.speedcubing.lib.api.events.SignUpdateEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A sign editor used as a text input. The sign only exists on the player's client: the world is never
 * touched, and the block the client was shown is put back once the input is done or timed out.
 */
public class SignBuilder {
    public static Set<SignBuilder> signs = new HashSet<>();

    public static SignBuilder openSign(Player player, String... lines) {
        return new SignBuilder(player, lines);
    }


    private long timeout = 10000L;
    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
    private final long generatedTime = System.currentTimeMillis();

    public long getGeneratedTime() {
        return generatedTime;
    }

    private Consumer<SignUpdateEvent> event = (r) -> {
    };

    public void onFinish(Consumer<SignUpdateEvent> event) {
        this.event = event;
    }

    public Consumer<SignUpdateEvent> getEvent() {
        return event;
    }

    private final Player player;
    private final Location location;

    public Player getPlayer() {
        return player;
    }

    SignBuilder(Player player, String... lines) {
        this.player = player;
        // the bottom of the player's own column: always in a chunk the client has loaded, out of sight
        Location at = player.getLocation();
        location = new Location(player.getWorld(), at.getBlockX(), 0, at.getBlockZ());
        BlockPosition position = new BlockPosition(location.getBlockX(), 0, location.getBlockZ());

        IChatBaseComponent[] text = new IChatBaseComponent[4];
        for (int i = 0; i < 4; i++)
            text[i] = new ChatComponentText(i < lines.length ? lines[i] : "");

        player.sendBlockChange(location, Material.SIGN_POST, (byte) 0);
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutUpdateSign(((CraftWorld) player.getWorld()).getHandle(), position, text));
        connection.sendPacket(new PacketPlayOutOpenSignEditor(position));
        signs.add(this);
    }

    /** shows the player the real block again */
    @SuppressWarnings("deprecation")
    public void restore() {
        if (!player.isOnline() || player.getWorld() != location.getWorld())
            return;
        Block block = location.getBlock();
        player.sendBlockChange(location, block.getType(), block.getData());
    }
}

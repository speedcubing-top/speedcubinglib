package top.speedcubing.lib.discord;

import java.awt.Color;
import java.io.IOException;

public class SimpleWebhook {
    /**
     * Posts an embed. A failure (Discord down, webhook deleted) is logged and swallowed: callers send
     * webhooks alongside real work such as bans and logins, which must not depend on Discord.
     */
    public static void sendWebhook(String webhookUrl, String title, String description, String imageUrl, Color color) {
        DiscordWebhook discordWebhook = new DiscordWebhook(webhookUrl);
        discordWebhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setTitle(title)
                .setDescription(description)
                .setImage(imageUrl)
                .setColor(color));
        try {
            discordWebhook.execute();
        } catch (IOException e) {
            // not the message: for HTTP errors it is the webhook URL, which is a secret
            System.err.println("[SimpleWebhook] '" + title + "' webhook failed: " + e.getClass().getSimpleName()
                    + (e instanceof java.io.FileNotFoundException ? " (webhook deleted?)" : ""));
        }
    }
}
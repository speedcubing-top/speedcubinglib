package top.speedcubing.lib.api;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import top.speedcubing.lib.api.mojang.Profile;
import top.speedcubing.lib.api.mojang.ProfileSkin;
import top.speedcubing.lib.api.mojang.ProfileTexture;
import top.speedcubing.lib.bukkit.events.packet.ProfileRespondEvent;
import top.speedcubing.lib.utils.UUIDUtils;
import top.speedcubing.lib.utils.http.HTTPResponse;
import top.speedcubing.lib.utils.http.HTTPUtils;

public class MojangAPI {

    private static Profile t(String name, boolean call) throws IOException {
        HTTPResponse response = HTTPUtils.get("https://api.mojang.com/users/profiles/minecraft/" + name);

        if (response.getResponseCode() != 200)
            throw new APIResponseException(response.getUrl(), response.getResponseCode());

        JsonObject object = JsonParser.parseString(response.getData()).getAsJsonObject();
        String fixedName = object.get("name").getAsString();
        String uuid = UUIDUtils.dash(object.get("id").getAsString());
        if (call)
            new ProfileRespondEvent(new ProfileSkin(fixedName, uuid, null, null)).call();
        return new Profile(fixedName, uuid);
    }

    public static List<String> blockedServers() throws IOException {
        HTTPResponse http = HTTPUtils.get("https://sessionserver.mojang.com/blockedservers");
        return Arrays.asList(http.getData().split("\n"));
    }

    public static List<Profile> getByNames(String... names) throws IOException {
        Preconditions.checkArgument(names.length <= 10);

        StringBuilder payload = new StringBuilder("[");
        for (int i = 0; i < names.length; i++)
            payload.append("\"").append(names[i]).append(i == names.length - 1 ? "\"" : "\",");
        payload.append("]");

        String url = "https://api.mojang.com/profiles/minecraft";
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(HTTPUtils.TIMEOUT_MILLIS);
        connection.setReadTimeout(HTTPUtils.TIMEOUT_MILLIS);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        try (OutputStream out = connection.getOutputStream()) {
            out.write(payload.toString().getBytes(StandardCharsets.UTF_8));
        }

        if (connection.getResponseCode() != 200)
            throw new APIResponseException(url, connection.getResponseCode());

        String text;
        try (InputStream in = connection.getInputStream()) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        List<Profile> result = new ArrayList<>();
        for (JsonElement e : JsonParser.parseString(text).getAsJsonArray()) {
            String fixedName = e.getAsJsonObject().get("name").getAsString();
            String uuid = UUIDUtils.dash(e.getAsJsonObject().get("id").getAsString());
            result.add(new Profile(fixedName, uuid));

            new ProfileRespondEvent(new ProfileSkin(fixedName, uuid, null, null)).call();
        }
        return result;
    }

    public static Profile getByName(String name) throws IOException {
        return t(name, true);
    }

    public static ProfileSkin getSkinByName(String name) throws IOException {
        Profile p = t(name, false);
        if (p == null)
            return null;
        return getSkinByUUID(p.getUUID());
    }


    public static ProfileSkin getSkinByUUID(UUID uuid) throws IOException {
        return getSkinByUUID(uuid.toString());
    }

    public static ProfileSkin getSkinByUUID(String uuid) throws IOException {
        HTTPResponse response = HTTPUtils.get("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");

        if (response.getResponseCode() != 200)
            throw new APIResponseException(response.getUrl(), response.getResponseCode());

        JsonObject object = JsonParser.parseString(response.getData()).getAsJsonObject();
        JsonObject object2 = object.get("properties").getAsJsonArray().get(0).getAsJsonObject();
        ProfileSkin profile = new ProfileSkin(object.get("name").getAsString(), UUIDUtils.dash(object.get("id").getAsString()), object2.get("value").getAsString(), object2.get("signature").getAsString());
        new ProfileRespondEvent(profile).call();
        return profile;
    }

    public static ProfileTexture getTextureByUUID(String uuid) throws IOException {
        return getSkinByUUID(uuid).getSkin().getTexture();
    }
}

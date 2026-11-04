package top.speedcubing.lib.utils.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HTTPUtils {
    public static final int TIMEOUT_MILLIS = 10000;

    public static HTTPResponse get(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(TIMEOUT_MILLIS);
        connection.setReadTimeout(TIMEOUT_MILLIS);
        connection.setRequestMethod("GET");
        String body;
        try (InputStream in = connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream()) {
            if (in == null) {
                return new HTTPResponse(url, "", connection.getResponseCode());
            }
            body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        return new HTTPResponse(url, body, connection.getResponseCode());
    }
}

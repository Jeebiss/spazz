package net.jeebiss.spazz.github;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.jeebiss.spazz.github.deserializers.EventDeserializer;
import net.jeebiss.spazz.github.events.Event;
import net.jeebiss.spazz.util.Utilities;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Requester {

    private final GitHub root;
    private static final Requester staticInstance = new Requester(null);
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Event.class, new EventDeserializer())
            .create();
    private static final JsonParser jsonParser = new JsonParser();

    private String method = "POST";

    public Requester(GitHub root) {
        this.root = root;
    }

    public static Gson getGson() {
        return gson;
    }
    public static JsonParser getJsonParser() { return jsonParser; }

    public Requester method(String method) {
        this.method = method;
        return this;
    }

    public <T> T parse(String url, Class<T> type) {
        InputStream conn = null;
        try {
            if (method.equals("GET")) {
                conn = setupConnection(url).getInputStream();
                T object = gson.fromJson(Utilities.getStringFromStream(conn), type);
                conn.close();
                return object;
            }
        } catch (Exception e) {} finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception e) {}
        }
        return null;
    }

    public JsonArray parseArray(String url) {
        InputStream conn = null;
        try {
            if (method.equals("GET")) {
                conn = setupConnection(url).getInputStream();
                JsonArray array = jsonParser.parse(Utilities.getStringFromStream(conn)).getAsJsonArray();
                conn.close();
                return array;
            }
        } catch (Exception e) {} finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception e) {}
        }
        return new JsonArray();
    }

    public Map<String, String> parseStringMap(String url) {
        InputStream conn = null;
        try {
            if (method.equals("GET")) {
                conn = setupConnection(url).getInputStream();
                Map<String, String> map = gson.fromJson(Utilities.getStringFromStream(conn),
                        new TypeToken<Map<String, String>>(){}.getType());
                conn.close();
                return map;
            }
        } catch (Exception e) {} finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception e) {}
        }
        return new HashMap<String, String>();
    }

    public HttpURLConnection githubConnection() throws Exception {
        return setupConnection(GitHub.GITHUB_URL);
    }

    private HttpURLConnection setupConnection(String url) throws Exception {
        URL loc = new URL(url);
        HttpURLConnection uc = (HttpURLConnection) loc.openConnection();
        uc.setRequestProperty("Authorization", "token " + root.authentication);
        uc.setRequestMethod(method);
        return uc;
    }

}

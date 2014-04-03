package net.jeebiss.spazz.github;

import com.google.gson.*;
import net.jeebiss.spazz.github.deserializers.EventDeserializer;
import net.jeebiss.spazz.github.events.Event;
import net.jeebiss.spazz.util.Utilities;

import java.net.HttpURLConnection;
import java.net.URL;

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
        try {
            if (method.equals("GET")) {
                return gson.fromJson(Utilities.getStringFromStream(setupConnection(url).getInputStream()), type);
            }
        } catch (Exception e) {}
        return null;
    }

    public JsonArray parseArray(String url) {
        try {
            if (method.equals("GET")) {
                return jsonParser.parse(Utilities.getStringFromStream(setupConnection(url).getInputStream())).getAsJsonArray();
            }
        } catch (Exception e) {}
        return new JsonArray();
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

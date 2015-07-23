package net.jeebiss.spazz.mojang;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.jeebiss.spazz.util.Utilities;
import org.pircbotx.Colors;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MojangStatus {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Status.class, new MojangStatusDeserializer())
            .create();
    private static final String STATUS_API = "https://status.mojang.com/check";

    private static final String WEBSITE = "minecraft.net",
                                SESSION = "session.minecraft.net",
                                ACCOUNT = "account.mojang.com",
                                AUTH = "auth.mojang.com",
                                SKINS = "skins.minecraft.net",
                                AUTH_SERVER = "authserver.mojang.com",
                                SESSION_SERVER = "sessionserver.mojang.com",
                                API = "api.mojang.com",
                                TEXTURES = "textures.minecraft.net";

    private static List<HashMap<String, Status>> getStatusResponse() {
        InputStream conn = null;
        try {
            conn = new URL(STATUS_API).openConnection().getInputStream();
            List<HashMap<String, Status>> map = gson.fromJson(Utilities.getStringFromStream(conn),
                    new TypeToken<List<HashMap<String, Status>>>(){}.getType());
            conn.close();
            return map;
        } catch (Exception e) {} finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception e) {}
        }
        return new ArrayList<HashMap<String, Status>>();
    }

    public static String getFormattedStatus() {
        Map<String, Status> response = new HashMap<String, Status>();
        for (HashMap<String, Status> map : getStatusResponse()) {
            response.putAll(map);
        }
        return    "Website: "        + response.get(WEBSITE).translation        + "; "
                + "Session: "        + response.get(SESSION).translation        + "; "
                + "Account: "        + response.get(ACCOUNT).translation        + "; "
                + "Auth: "           + response.get(AUTH).translation           + "; "
                + "Skins: "          + response.get(SKINS).translation          + "; "
                + "Auth Server: "    + response.get(AUTH_SERVER).translation    + "; "
                + "Session Server: " + response.get(SESSION_SERVER).translation + "; "
                + "API: "            + response.get(API).translation            + "; "
                + "Textures: "       + response.get(TEXTURES).translation       ;
    }

    public enum Status {
        GREEN(Colors.DARK_GREEN + "OK"), YELLOW(Colors.YELLOW + "SLOW"), RED(Colors.RED + "DOWN");

        private final String translation;

        Status(String string) {
            this.translation = string + "<C>";
        }
    }
}

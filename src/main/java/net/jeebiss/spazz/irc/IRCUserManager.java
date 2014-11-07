package net.jeebiss.spazz.irc;

import net.jeebiss.spazz.Spazz;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class IRCUserManager {

    private static final File usersFile = new File(System.getProperty("user.dir") + "/storage/users.yml");
    private static final DumperOptions yamlOptions = new DumperOptions(); static{yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);}
    private final Map<String, IRCUser> ircUsers = new HashMap<String, IRCUser>();

    private static final File spazzFile = new File(System.getProperty("user.dir") + "/storage/spazzmatic.yml");
    private LinkedHashMap spazzData = null;

    public IRCUserManager() {
        loadUserFiles();
    }

    public boolean hasUser(String nick) {
        return ircUsers.containsKey(nick.toLowerCase());
    }

    public IRCUser getUser(String nick) {
        return ircUsers.get(nick.toLowerCase());
    }

    public void initUser(String nick) {
        ircUsers.put(nick.toLowerCase(), new IRCUser(nick));
    }

    public void setLastSeen(String nick, String doing) {
        getUser(nick).setLastSeen(doing.getBytes());
        getUser(nick).setLastSeenTime(Spazz.dateFormat.format(Calendar.getInstance().getTime()));
    }

    public void sendMessage(String nick, IRCMessage message) {
        getUser(nick).addMessage(message);
    }

    public void setUserServer(String nick, String server) {
        getUser(nick).setServerAddress(server);
    }

    public void loadUserFiles() {
        ircUsers.clear();
        FileInputStream is = null;
        Yaml yaml = new Yaml();
        try {
            is = new FileInputStream(usersFile);
            LinkedHashMap users = (LinkedHashMap) yaml.load(is);
            ircUsers.putAll(users);
            is = new FileInputStream(spazzFile);
            spazzData = (LinkedHashMap) yaml.load(is);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception e) {}
        }
    }

    public void saveUserFiles() {
        Yaml yaml = new Yaml(yamlOptions);
        FileWriter writer = null;
        try {
            writer = new FileWriter(usersFile);
            writer.write(yaml.dump(ircUsers));

            writer.close();

            writer = new FileWriter(spazzFile);
            spazzData.put("password", System.getProperty("spazz.password"));
            spazzData.put("bitly", System.getProperty("spazz.bitly"));
            spazzData.put("bitly-backup", System.getProperty("spazz.bitly-backup"));
            spazzData.put("dev-mode", Spazz.devMode);
            spazzData.put("wolfram", Spazz.queryHandler.getKey());
            spazzData.put("github", System.getProperty("spazz.github"));
            spazzData.put("message-delay", Spazz.messageDelay);
            writer.write(yaml.dump(spazzData));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {}
        }
    }

    public LinkedHashMap getSpazzData() {
        return spazzData;
    }

}

package net.jeebiss.spazz.irc;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class IRCUserManager {

    private static final File userDirectory = new File(System.getProperty("user.dir") + "/users");
    private static final FilenameFilter yamlFilter = new FilenameFilter(){@Override public boolean accept(File dir, String name){name = name.toLowerCase();return name.endsWith(".yml")||name.endsWith(".yaml");}};
    private static final DumperOptions yamlOptions = new DumperOptions(); static{yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);}
    private final Map<String, IRCUser> ircUsers = new HashMap<String, IRCUser>();

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
        getUser(nick).setLastSeen(doing);
    }

    public void sendMessage(String nick, IRCMessage message) {
        getUser(nick).addMessage(message);
    }

    public void setUserServer(String nick, String server) {
        getUser(nick).setServerAddress(server);
    }

    public void loadUserFiles() {
        ircUsers.clear();
        if ((userDirectory.exists() && userDirectory.isDirectory()) || !userDirectory.mkdirs()) {
            FileInputStream is = null;
            try {
                for (File user : userDirectory.listFiles(yamlFilter)) {
                    if (user.isDirectory()) {
                        user.delete();
                        continue;
                    }
                    is = new FileInputStream(user);
                    IRCUser ircUser = new IRCUser((LinkedHashMap) new Yaml().load(is));
                    ircUsers.put(ircUser.getNick().toLowerCase(), ircUser);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (Exception e) {}
            }
        }
        else {
            System.out.println("Directory '/users' not found and could not be created. IRCUserManager will not function.");
        }
    }

    public void saveUserFiles() {
        for (String nick : ircUsers.keySet()) {
            saveUserFile(nick);
        }
    }

    private void saveUserFile(String nick) {
        IRCUser user = ircUsers.get(nick);
        Yaml yaml = new Yaml(yamlOptions);
        FileWriter writer = null;
        try {
            writer = new FileWriter(userDirectory + "/" + user.getNick().toLowerCase().replace('|', '_') + ".yml");
            writer.write(yaml.dump(user.getData()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {}
        }
    }

}

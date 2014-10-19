package net.jeebiss.spazz.irc;

import net.jeebiss.spazz.Spazz;
import net.jeebiss.spazz.util.Utilities;
import org.pircbotx.Colors;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public class IRCUser {

    private String lastSeen;
    private Date lastSeenTime;
    private String nick;
    private List<IRCMessage> messages;
    private String serverAddress;
    private LinkedHashMap rawData;

    private File userFile = null;

    IRCUser(String nick) {
        this.nick = nick;
        this.messages = new ArrayList<IRCMessage>();
        this.setLastSeen("Existing");
        this.serverAddress = "";
        this.rawData = null;
    }

    IRCUser(LinkedHashMap data) {
        try {
            this.rawData = data;
            if (data.get("name") instanceof byte[]) {
                this.nick = new String((byte[]) data.get("name"));
            }
            else if (data.get("name") instanceof String) {
                this.nick = (String) data.get("name");
            }
            if (data.get("lastseen") instanceof byte[]) {
                this.lastSeen = new String((byte[]) data.get("lastseen"));
                this.lastSeenTime = Spazz.dateFormat.parse((String) data.get("lasttime"));
            }
            // Keep string checker so we can edit a YAML file from a program and have Spazz translate it later
            else if (data.get("lastseen") instanceof String) {
                this.lastSeen = ((String) data.get("lastseen")).replace("`````", "#").replace("````", "|");
                this.lastSeenTime = Spazz.dateFormat.parse((String) data.get("lasttime"));
            }
            else {
                setLastSeen("Existing");
            }
            this.messages = new ArrayList<IRCMessage>();
            if (data.get("messages") instanceof ArrayList<?>) {
                this.messages.addAll((ArrayList<IRCMessage>) data.get("messages"));
                Collections.sort(this.messages, new Comparator<IRCMessage>() {
                    @Override
                    public int compare(IRCMessage o1, IRCMessage o2) {
                        return Long.compare(o2.getTime(), o1.getTime());
                    }
                });
            }
            if (data.get("server_address") instanceof String) {
                this.serverAddress = ((String) data.get("server_address"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("name", getNick());
        data.put("lastseen", getLastSeen().getBytes());
        Calendar seenTime = Calendar.getInstance();
        seenTime.setTime(getLastSeenTime());
        data.put("lasttime", new SimpleDateFormat((seenTime.getTimeInMillis() < 0 ? "-yyyy" : "yyyy") + "-MM-dd HH:mm:ss.SSS zzz").format(getLastSeenTime()));
        data.put("messages", getMessages());
        data.put("server_address", getServerAddress());
        if (getNick().equals("spazzmatic")) {
            data.put("password", System.getProperty("spazz.password"));
            data.put("bitly", System.getProperty("spazz.bitly"));
            data.put("bitly-backup", System.getProperty("spazz.bitly-backup"));
            data.put("dev-mode", Spazz.devMode);
            data.put("wolfram", Spazz.queryHandler.getKey());
            data.put("github", System.getProperty("spazz.github"));
            data.put("message-delay", Spazz.messageDelay);
        }
        return data;
    }

    void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    void setLastSeen(String lastSeen) {
        this.lastSeenTime = Calendar.getInstance().getTime();
        this.lastSeen = Utilities.uncapitalize(lastSeen);
    }

    void setLastSeenRaw(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    void addMessage(IRCMessage msg) {
        this.messages.add(msg);
    }

    public String getLastSeen() {
        return this.lastSeen;
    }

    public Date getLastSeenTime() {
        return this.lastSeenTime;
    }

    public String getNick() {
        return this.nick;
    }

    public String getServerAddress() {
        return this.serverAddress;
    }

    public LinkedHashMap getRawData() {
        return rawData;
    }

//    public void checkMessages() {
//        if (this.messages.isEmpty()) return;
//        Spazz.send(Colors.NORMAL + getNick() + ": " + Spazz.chatColor + "You have messages!");
//        String lastNick = null;
//        for (IRCMessage message : this.messages) {
//            String user = message.getUser();
//            if (!user.equals(lastNick)) {
//                lastNick = user;
//                Spazz.sendNotice(getNick(), lastNick + Spazz.chatColor + ":");
//            }
//            Spazz.sendNotice(getNick(), "  " + message.getMessage());
//        }
//        this.messages.clear();
//    }

    public List<IRCMessage> getMessages() {
        return this.messages;
    }

    public void clearMessages() {
        this.messages.clear();
    }

}

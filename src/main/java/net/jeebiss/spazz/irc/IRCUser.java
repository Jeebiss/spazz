package net.jeebiss.spazz.irc;

import net.jeebiss.spazz.Spazz;
import net.jeebiss.spazz.util.Utilities;

import java.util.*;

public class IRCUser {

    private byte[] lastSeen;
    private String lastSeenTime;
    private String nick;
    private List<IRCMessage> messages;
    private String serverAddress;

    public IRCUser() {}

    IRCUser(String nick) {
        this.nick = nick;
        this.messages = new ArrayList<IRCMessage>();
        this.setLastSeen("Existing".getBytes());
        this.setLastSeenTime(Spazz.dateFormat.format(Calendar.getInstance().getTime()));
        this.serverAddress = "";
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void setLastSeen(byte[] lastSeen) {
        this.lastSeen = Utilities.uncapitalize(new String(lastSeen)).getBytes();
    }

    public void setLastSeenTime(String lastSeenTime) {
        this.lastSeenTime = lastSeenTime;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setMessages(List<IRCMessage> messages) {
        this.messages = messages;
    }

    void addMessage(IRCMessage msg) {
        this.messages.add(msg);
    }

    public byte[] getLastSeen() {
        return this.lastSeen;
    }

    public String getLastSeenTime() {
        return this.lastSeenTime;
    }

    public Date getParsedLastSeenTime() {
        try {
            return Spazz.dateFormat.parse(this.lastSeenTime);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getNick() {
        return this.nick;
    }

    public String getServerAddress() {
        return this.serverAddress;
    }

    public List<IRCMessage> getMessages() {
        return this.messages;
    }

    public void clearMessages() {
        this.messages.clear();
    }

}

package net.jeebiss.spazz.irc;

public class IRCMessage {

    private String user;
    private String message;
    private long time;
    private boolean action;

    public IRCMessage() {}

    public IRCMessage(String user, String message, boolean action) {
        setUser(user);
        setMessage(message);
        setTime(System.currentTimeMillis());
        setAction(action);
    }

    public String getUser() {
        return this.user;
    }

    public String getMessage() {
        return this.message;
    }

    public long getTime() {
        return this.time;
    }

    public boolean isAction() {
        return this.action;
    }

    public IRCMessage setUser(String user) {
        this.user = user;
        return this;
    }

    public IRCMessage setMessage(String message) {
        this.message = message;
        return this;
    }

    public IRCMessage setTime(long time) {
        this.time = time;
        return this;
    }

    public IRCMessage setAction(boolean action) {
        this.action = action;
        return this;
    }

    public boolean matches(String s) {
        return message.matches(String.format(".*(%s).*", s));
    }

    public String replaceAll(String target, String replacement) {
        String ret = "";
        if (action) ret += "* " + user;
        else ret += "<" + user + ">";
        message = message.replaceAll(target, replacement);
        return ret + " " + message;
    }

}

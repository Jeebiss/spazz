package net.jeebiss.spazz.github;

import net.minidev.json.JSONObject;

public class User {

    private final GitHub root;
    
    private final String login;
    private JSONObject information;
    
    public User(GitHub root, String login, JSONObject information) {
        this.root = root;
        this.login = login;
        this.information = information;
    }
    
    public String getLogin() {
        return login;
    }
    
}

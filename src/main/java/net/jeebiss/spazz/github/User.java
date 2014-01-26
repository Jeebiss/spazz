package net.jeebiss.spazz.github;

import net.minidev.json.JSONObject;

public class User {

    private final GitHub root;

    private JSONObject information;

    public User(GitHub root, JSONObject information) {
        this.root = root;
        this.information = information;
    }

    public GitHub getGitHub() {
        return root;
    }

    public String getLogin() {
        return (String) information.get("login");
    }

}

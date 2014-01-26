package net.jeebiss.spazz.github;

import net.jeebiss.spazz.util.Utilities;
import net.minidev.json.JSONObject;

public class PullRequest extends Issue {

    private JSONObject information;

    public PullRequest(GitHub root, Repository owner, JSONObject information) {
        super(root, owner, information);
        this.information = information;
    }

    @Override
    public String getUrl() {
        return (String) information.get("html_url");
    }

    @Override
    public boolean isPullRequest() {
        return true;
    }

    @Override
    public String getShortUrl() {
        return Utilities.getShortUrl((String) information.get("html_url"));
    }

}

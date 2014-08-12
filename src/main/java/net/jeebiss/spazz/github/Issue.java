package net.jeebiss.spazz.github;

import net.jeebiss.spazz.Spazz;
import net.jeebiss.spazz.util.Utilities;

import java.util.Date;

public class Issue {

    private User user;
    private int number;
    private String title;
    private String body;
    private String state;
    private String created_at;
    private String updated_at;
    private PullRequest pull_request;
    protected String html_url;

    public User getUser() {
        return user;
    }

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public String formatTitle() { return title.replace("<", "<LT>"); }

    public String getBody() { return body; }

    public String getState() {
        return state.toLowerCase();
    }

    public Date getLastUpdated() {
        return GitHub.parseDate(updated_at);
    }

    public String getLastUpdatedSimple() {
        return GitHub.parseDateSimple(updated_at);
    }

    public String getUrl() {
        return html_url;
    }

    public boolean isPullRequest() {
        return (pull_request != null);
    }

    public PullRequest asPullRequest() {
        try {
            return Spazz.github.retrieve().parse(pull_request.getUrl(), PullRequest.class);
        } catch (Exception e) {
            return null;
        }
    }

    public String getShortUrl() {
        return Utilities.getShortUrl(html_url);
    }

    public Date getCreatedAt() {
        return GitHub.parseDate(created_at);
    }

    public String getCreatedAtSimple() {
        return GitHub.parseDateSimple(created_at);
    }

}

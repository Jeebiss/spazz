package net.jeebiss.spazz.github.comments;

import net.jeebiss.spazz.github.User;
import net.jeebiss.spazz.util.Utilities;

public class IssueComment implements Comment {

    private long id;
    private String html_url;
    private User user;
    private String created_at;
    private String updated_at;
    private String body;

    @Override
    public long getCommentId() {
        return id;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getType() {
        return "Issue";
    }

    @Override
    public String getUrl() {
        return html_url;
    }

    @Override
    public String getShortUrl() {
        return Utilities.getShortUrl(html_url);
    }

    @Override
    public String getBody() {
        return body;
    }

}

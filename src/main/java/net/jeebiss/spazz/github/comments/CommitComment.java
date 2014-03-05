package net.jeebiss.spazz.github.comments;

import net.jeebiss.spazz.github.User;
import net.jeebiss.spazz.util.Utilities;

import java.util.Map;

public class CommitComment implements Comment {

    private long id;
    private String html_url;
    private User user;
    private int position;
    private int line;
    private String path;
    private String commit_id;
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
        return "Commit";
    }

    @Override
    public String getUrl() {
        return html_url;
    }

    @Override
    public String getShortUrl() {
        return Utilities.getShortUrl(html_url);
    }

    public int getPosition() {
        return position;
    }

    public int getLine() {
        return line;
    }

    public String getPath() {
        return path;
    }

    public String getCommitId() {
        return commit_id;
    }

    public String getBody() {
        return body;
    }

}

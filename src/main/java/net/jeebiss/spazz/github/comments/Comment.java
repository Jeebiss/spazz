package net.jeebiss.spazz.github.comments;

import net.jeebiss.spazz.github.User;

public interface Comment {

    public long getCommentId();

    public User getUser();

    public String getType();

    public String getUrl();

    public String getShortUrl();

    public String getBody();

}

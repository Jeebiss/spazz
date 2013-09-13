package net.jeebiss.spazz.github;

public interface Comment {
    
    public int getCommentId();
    public User getUser();
    public String getType();
    public String getUrl();
    public String getShortUrl();
    
}

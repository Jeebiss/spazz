package net.jeebiss.spazz.github;

import net.jeebiss.spazz.Spazz;

public class CommentEvent {

    private final GitHub root;
    
    private final Repository owner;
    private final Comment comment;
    
    public CommentEvent(GitHub root, Repository owner, Comment comment) {
        this.root = root;
        this.owner = owner;
        this.comment = comment;
        Spazz.onComment(this);
    }
    
    public GitHub getGitHub() {
        return root;
    }
    
    public Repository getRepo() {
        return owner;
    }
    
    public Comment getComment() {
        return comment;
    }
    
}

package net.jeebiss.spazz.github;

import java.util.ArrayList;

import net.jeebiss.spazz.Spazz;

public class CommitEvent {

    private final GitHub root;
    
    private final Repository owner;
    private final ArrayList<Commit> commits;
    
    public CommitEvent(GitHub root, Repository owner, ArrayList<Commit> commits) {
        this.root = root;
        this.owner = owner;
        this.commits = commits;
        Spazz.onCommit(this);
    }
    
    public GitHub getGitHub() {
        return root;
    }
    
    public ArrayList<Commit> getCommits() {
        return commits;
    }
    
    public ArrayList<String> getUsers() {
        ArrayList<String> users = new ArrayList<String>();
        for (Commit commit : commits) {
            if (!users.contains(commit.getAuthor().getLogin())) {
                users.add(commit.getAuthor().getLogin());
            }
        }
        return users;
    }
    
    public Repository getRepo() {
        return owner;
    }
    
}

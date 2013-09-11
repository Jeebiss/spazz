package net.jeebiss.spazz.github;

import net.jeebiss.spazz.Spazz;

public class IssueEvent {
    
    public enum State { OPENED, CLOSED, REOPENED }

    private final GitHub root;
    
    private final Issue issue;
    private final State state;
    
    public IssueEvent(GitHub root, Issue issue, State state) {
        this.root = root;
        this.issue = issue;
        this.state = state;
        Spazz.onIssue(this);
    }
    
    public Issue getIssue() {
        return issue;
    }
    
    public State getState() {
        return state;
    }
    
}

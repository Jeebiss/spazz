package net.jeebiss.spazz.github;

import net.jeebiss.spazz.Spazz;

public class IssueEvent {

    private final GitHub root;
    
    private final Issue issue;
    private final Issue.State state;
    
    public IssueEvent(GitHub root, Issue issue, Issue.State state) {
        this.root = root;
        this.issue = issue;
        this.state = state;
        Spazz.onIssue(this);
    }
    
    public Issue getIssue() {
        return issue;
    }
    
}

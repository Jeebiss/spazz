package net.jeebiss.spazz.github;

import net.jeebiss.spazz.Spazz;

public class PullRequestEvent {

    public enum State {OPENED, CLOSED, PULLED}

    private final GitHub root;

    private final PullRequest request;
    private final State state;

    public PullRequestEvent(GitHub root, PullRequest request, State state) {
        this.root = root;
        this.request = request;
        this.state = state;
        Spazz.onPullRequest(this);
    }

    public GitHub getGitHub() {
        return root;
    }

    public PullRequest getPullRequest() {
        return request;
    }

    public String getState() {
        return state.name();
    }

}

package net.jeebiss.spazz.github.events;

import com.google.gson.JsonObject;
import net.jeebiss.spazz.Spazz;
import net.jeebiss.spazz.github.PullRequest;
import net.jeebiss.spazz.github.Requester;

public class PullRequestEvent extends Event {

    private Payload payload;

    public PullRequestEvent(JsonObject json) {
        super(json);
        payload = Requester.getGson().fromJson(json.get("payload"), Payload.class);
    }

    public Payload getPayload() { return payload; }
    public int getIssueNumber() { return payload.getPullRequest().getNumber(); }

    private String getState() {
        String action = payload.getAction();
        if (action.equals("closed") && payload.getPullRequest().merged()) action = "pulled";
        return action;
    }

    public void fire(String commentUrl) {
        boolean c = commentUrl != null;
        PullRequest pullRequest = payload.getPullRequest();
        String action = payload.getAction() + (c ? " and commented on" : "");
        Spazz.sendToAllChannels("[<O>" + getRepo().getName() + "<C>] <D>" + getActor().getLogin() + "<C> " + action
                + " a pull request: <D>" + pullRequest.formatTitle() + "<C> (<D>" + pullRequest.getNumber() + "<C>) -- "
                + (c ? commentUrl : pullRequest.getShortUrl()));
    }

    public class Payload {
        private String action;
        private int number;
        private PullRequest pull_request;

        public String getAction() { return action; }
        public int getNumber() { return number; }
        public PullRequest getPullRequest() { return pull_request; }
    }

}

package net.jeebiss.spazz.github.events;

import com.google.gson.JsonObject;
import net.jeebiss.spazz.Spazz;
import net.jeebiss.spazz.github.Issue;
import net.jeebiss.spazz.github.Requester;

public class IssuesEvent extends Event {

    private Payload payload;

    public IssuesEvent(JsonObject json) {
        super(json);
        payload = Requester.getGson().fromJson(json.get("payload"), Payload.class);
    }

    public Payload getPayload() { return payload; }
    public int getIssueNumber() { return payload.getIssue().getNumber(); }

    @Override
    public void fire(String commentUrl) {
        boolean c = commentUrl != null;
        Issue issue = payload.getIssue();
        String action = payload.getAction() + (c ? " and commented on" : "");
        Spazz.sendToAllChannels("[<O>" + getRepo().getName() + "<C>] <D>" + getActor().getLogin() + "<C> " + action
                + " an issue: <D>" + issue.formatTitle() + "<C> (<D>" + issue.getNumber() + "<C>) -- "
                + (c ? commentUrl : issue.getShortUrl()));
    }

    public class Payload {
        private String action;
        private Issue issue;

        public String getAction() { return action; }
        public Issue getIssue() { return issue; }
    }

}

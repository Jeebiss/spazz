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

    @Override
    public void fire() {
        Issue issue = payload.getIssue();
        Spazz.sendToAllChannels("[<O>" + getRepo().getName() + "<C>] <D>" + getActor().getLogin() + "<C> "
                + payload.getAction() + " issue: <D>" + issue.getTitle().replace("<", "<LT>") + "<C> (<D>"
                + issue.getNumber() + "<C>) -- " + issue.getShortUrl());
    }

    public class Payload {
        private String action;
        private Issue issue;

        public String getAction() { return action; }
        public Issue getIssue() { return issue; }
    }

}

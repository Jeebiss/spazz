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

    private String getState() {
        String action = payload.getAction();
        if (action.equals("closed") && payload.getPullRequest().merged()) action = "pulled";
        return action;
    }

    @Override
    public void fire() {
        PullRequest issue = payload.getPullRequest();
        Spazz.sendToAllChannels("[<O>" + getRepo().getName() + "<C>] Pull request " + getState() + ": [<D>"
                + issue.getNumber() + "<C>] \"<D>" + issue.getTitle() + "<C>\" by <D>" + issue.getUser().getLogin()
                + "<C> -- " + issue.getShortUrl());
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

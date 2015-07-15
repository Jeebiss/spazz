package net.jeebiss.spazz.github.events;

import com.google.gson.JsonObject;
import net.jeebiss.spazz.Spazz;
import net.jeebiss.spazz.github.Issue;
import net.jeebiss.spazz.github.Requester;
import net.jeebiss.spazz.github.comments.IssueComment;

public class IssueCommentEvent extends Event implements CommentEvent {

    private Payload payload;

    public IssueCommentEvent(JsonObject json) {
        super(json);
        payload = Requester.getGson().fromJson(json.get("payload"), Payload.class);
    }

    public Payload getPayload() { return payload; }
    public int getIssueNumber() { return payload.getIssue().getNumber(); }

    @Override
    public void fire() {
        IssueComment comment = getPayload().getComment();
        Issue issue = payload.getIssue();
        String type = (issue.isPullRequest() ? "a pull request" : "an issue") + ": <D>";
        String sending;
        Spazz.sendToAllChannels(sending = "[<O>" + getRepo().getName() + "<C>] <D>" + getActor().getLogin() + "<C> commented on "
                + type + issue.formatTitle() + "<C> (<D>" + issue.getNumber() + "<C>) -- " + comment.getShortUrl());
        boolean isRandom = getRepo().getName().toLowerCase().contains(Spazz.random.substring(1));
        if (isRandom) {
            Spazz.sendRandom(sending);
        }
    }

    public class Payload {
        private String action;
        private Issue issue;
        private IssueComment comment;

        public String getAction() { return action; }
        public Issue getIssue() { return issue; }
        public IssueComment getComment() { return comment; }
    }

}

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

    @Override
    public void fire() {
        IssueComment comment = getPayload().getComment();
        Issue issue = payload.getIssue();
        Spazz.sendToAllChannels("[<O>" + getRepo().getName() + "<C>] <D>" + comment.getUser().getLogin()
                + "<C> commented on issue: <D>" + issue.getTitle().replace("<", "<LT>") + "<C> (<D>"
                + issue.getNumber() + "<C>) by <D>" + issue.getUser().getLogin() + "<C> -- " + comment.getShortUrl());
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

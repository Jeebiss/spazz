package net.jeebiss.spazz.github.events;

import com.google.gson.JsonObject;
import net.jeebiss.spazz.Spazz;
import net.jeebiss.spazz.github.Requester;
import net.jeebiss.spazz.github.comments.CommitComment;

public class CommitCommentEvent extends Event implements CommentEvent {

    private Payload payload;

    public CommitCommentEvent(JsonObject json) {
        super(json);
        payload = Requester.getGson().fromJson(json.get("payload"), Payload.class);
    }

    public Payload getPayload() { return payload; }

    @Override
    public void fire() {
        CommitComment comment = getPayload().getComment();
        Spazz.sendToAllChannels("[<O>" + getRepo().getName() + "<C>] <D>" + getActor().getLogin() + "<C> commented on commit:" +
                " <D>" + comment.getCommitId().substring(0, 7) + "<C> -- " + comment.getShortUrl());
    }

    public class Payload {
        private CommitComment comment;

        public CommitComment getComment() { return comment; }
    }
}

package net.jeebiss.spazz.github.deserializers;

import com.google.gson.*;
import net.jeebiss.spazz.github.events.*;

import java.lang.reflect.Type;

public class EventDeserializer implements JsonDeserializer<Event> {

    private static final String PUSH_EVENT = "PushEvent";
    private static final String ISSUES_EVENT = "IssuesEvent";
    private static final String PULL_REQUEST_EVENT = "PullRequestEvent";
    private static final String ISSUE_COMMENT_EVENT = "IssueCommentEvent";
    private static final String COMMIT_COMMENT_EVENT = "CommitCommentEvent";

    @Override
    public Event deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        switch (jsonObject.get("type").getAsString()) {
            case PUSH_EVENT:
                return new PushEvent(jsonObject);
            case ISSUES_EVENT:
                return new IssuesEvent(jsonObject);
            case PULL_REQUEST_EVENT:
                return new PullRequestEvent(jsonObject);
            case ISSUE_COMMENT_EVENT:
                return new IssueCommentEvent(jsonObject);
            case COMMIT_COMMENT_EVENT:
                return new CommitCommentEvent(jsonObject);
            default:
                return new Event(jsonObject);
        }
    }

}

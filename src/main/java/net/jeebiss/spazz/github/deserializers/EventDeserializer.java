package net.jeebiss.spazz.github.deserializers;

import com.google.gson.*;
import net.jeebiss.spazz.github.events.*;

import java.lang.reflect.Type;

public class EventDeserializer implements JsonDeserializer<Event> {

    @Override
    public Event deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        switch (jsonObject.get("type").getAsString()) {
            case "IssueCommentEvent":
                return new IssueCommentEvent(jsonObject);
            case "CommitCommentEvent":
                return new CommitCommentEvent(jsonObject);
            case "IssuesEvent":
                return new IssuesEvent(jsonObject);
            case "PullRequestEvent":
                return new PullRequestEvent(jsonObject);
            default:
                return new Event(jsonObject);
        }
    }

}

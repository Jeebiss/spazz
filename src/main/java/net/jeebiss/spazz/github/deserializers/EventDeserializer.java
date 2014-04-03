package net.jeebiss.spazz.github.deserializers;

import com.google.gson.*;
import net.jeebiss.spazz.github.events.*;

import java.lang.reflect.Type;

public class EventDeserializer implements JsonDeserializer<Event> {

    @Override
    public Event deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        switch (jsonObject.get("type").getAsString().toLowerCase().replaceAll("event", "")) {
            case "issuecomment":
                return new IssueCommentEvent(jsonObject);
            case "commitcomment":
                return new CommitCommentEvent(jsonObject);
            case "issues":
                return new IssuesEvent(jsonObject);
            case "pullrequest":
                return new PullRequestEvent(jsonObject);
            default:
                return new Event(jsonObject);
        }
    }

}

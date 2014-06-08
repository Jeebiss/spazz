package net.jeebiss.spazz.github.events;

import com.google.gson.JsonObject;
import net.jeebiss.spazz.github.Commit;
import net.jeebiss.spazz.github.Requester;

import java.util.List;

public class PushEvent extends Event {

    private Payload payload;

    public PushEvent(JsonObject json) {
        super(json);
        payload = Requester.getGson().fromJson(json.get("payload"), Payload.class);
    }

    public Payload getPayload() { return payload; }

    public class Payload {
        private List<Commit> commits;
        public List<Commit> getCommits() { return commits; }
    }

}

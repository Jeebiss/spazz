package net.jeebiss.spazz.github.events;

import com.google.gson.JsonObject;
import net.jeebiss.spazz.Spazz;
import net.jeebiss.spazz.github.Repository;
import net.jeebiss.spazz.github.Requester;

public class Event {

    private String id;
    private String type;
    private SimplifiedUser actor;
    private SimplifiedRepository repo;
    private String created_at;

    public Event(JsonObject json) {
        this.id = json.get("id").getAsString();
        this.type = json.get("type").getAsString();
        this.actor = Requester.getGson().fromJson(json.get("actor"), SimplifiedUser.class);
        this.repo = Requester.getGson().fromJson(json.get("repo"), SimplifiedRepository.class);
        this.created_at = json.get("created_at").getAsString();
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public SimplifiedUser getActor() { return actor; }
    public SimplifiedRepository getRepo() { return repo; }
    public String getCreatedAt() { return created_at; }

    public void fire() {}

    public class SimplifiedUser {
        private String login;

        public String getLogin() { return login; }
    }

    public class SimplifiedRepository {
        private String name;
        private String url;

        public String getName() { return name; }
        public String getUrl() { return url; }

        public Repository getFullRepo() {
            return Spazz.repoManager.getRepository(name.substring(name.indexOf('/')+1));
        }
    }

}


package net.jeebiss.spazz.github;

import net.jeebiss.spazz.github.events.Event;
import net.jeebiss.spazz.util.Utilities;

import java.util.Date;

public class Commit {

    private String sha;
    private Author author;
    private String message;
    private boolean distinct;
    private String url;

    public String getCommitId() { return sha; }

    public Author getAuthor() { return author; }

    public String getMessage() { return message; }

    public boolean isDistinct() { return distinct; }

    public String getUrl() { return url.replace("api.github.com/repos", "github.com").replace("commits", "commit"); }

    public String getShortUrl() { return Utilities.getShortUrl(getUrl()); }

    public boolean isMerge() { return message.matches("Merge (pull request #\\d+ from|branch '.+' of).+"); }

    public class Author {
        private String email;
        private String name;

        public String getEmail() { return email; }
        public String getName() { return name; }
    }

}

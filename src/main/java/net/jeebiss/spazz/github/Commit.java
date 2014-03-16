package net.jeebiss.spazz.github;

import net.jeebiss.spazz.util.Utilities;

import java.util.Date;

public class Commit {

    private String sha;
    private Details commit;
    private User author;
    private User committer;
    private String html_url;
    private Repository repo;

    public String getCommitId() { return sha; }

    public Details getDetails() { return commit; }

    public User getAuthor() { return author; }

    public User getCommitter() {
        return committer;
    }

    public String getUrl() { return html_url; }

    public String getShortUrl() { return Utilities.getShortUrl(html_url); }

    public boolean isMerge() { return commit.getMessage().matches("Merge (pull request #\\d+ from|branch '.+' of).+"); }

    public class Details {
        private SimplifiedUser author;
        private SimplifiedUser committer;
        private String message;
        private int comment_count;

        public SimplifiedUser getAuthor() { return author; }
        public SimplifiedUser getCommitter() { return committer; }
        public String getMessage() { return message; }
        public int getCommentCount() { return comment_count; }

        public class SimplifiedUser {
            private String name;
            private String email;
            private String date;

            public String getName() { return name; }
            public String getEmail() { return email; }
            public Date getDate() { return GitHub.parseDate(date); }
        }
    }

}

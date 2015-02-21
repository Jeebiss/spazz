package net.jeebiss.spazz.github;

import net.jeebiss.spazz.Spazz;
import net.jeebiss.spazz.util.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Commit {

    private static final Pattern isMerge = Pattern.compile("^Merge (pull request #\\d+|branch '.+').+$");
    private final static Pattern emojiPattern = Pattern.compile(":([^\\s]+):");

    private String sha;
    private Author author;
    private String message;
    private boolean distinct;
    private String url;

    public String getCommitId() { return sha; }

    public Author getAuthor() { return author; }

    public String getMessage() { return message; }

    public List<String> format() {
        List<String> messages = new ArrayList<String>();
        String cm = message;
        Matcher ma = emojiPattern.matcher(cm);
        while (ma.find()) {
            if (Spazz.github.getEmojis().containsKey(ma.group(1).toLowerCase()))
                cm = cm.replaceFirst(ma.group(0),
                        Utilities.getShortUrl(Spazz.github.getEmojis().get(ma.group(1).toLowerCase())));
        }
        String[] messageSplit = cm.replace("<", "<LT>").split("\n+");
        StringBuilder message = new StringBuilder("<D>  ").append(author.getName()).append("<C>: ")
                .append(messageSplit[0]).append(" - ");
        boolean added = false;
        int m = 0;
        for (int i = 1; i < messageSplit.length; i++) {
            String msg = messageSplit[i];
            if (m < 3) {
                char last = msg.charAt(msg.length() - 1);
                if (last == '.' || last == '?' || last == '!')
                    m = 3;
                else
                    m++;
                if (i + 1 == messageSplit.length) {
                    added = true;
                    message.append(messageSplit[i]).append(" -- ").append(getShortUrl());
                    messages.add(message.toString());
                }
                else
                    message.append(messageSplit[i]).append(" ");
            }
            else {
                m = 1;
                messages.add(message.substring(0, message.length() - 1));
                if (i + 1 == messageSplit.length) {
                    added = true;
                    messages.add(messageSplit[i] + " -- " + getShortUrl());
                }
                else
                    message = new StringBuilder(messageSplit[i]).append(" ");
            }
        }
        if (!added)
            messages.add(message.substring(0, message.length()-3) + " -- " + getShortUrl());
        return messages;
    }

    public boolean isDistinct() { return distinct; }

    public String getUrl() { return url.replace("api.github.com/repos", "github.com").replace("commits", "commit"); }

    public String getShortUrl() { return Utilities.getShortUrl(getUrl()); }

    public boolean isMerge() { return isMerge.matcher(getMessage()).matches(); }

    public class Author {
        private String email;
        private String name;

        public String getEmail() { return email; }
        public String getName() { return name; }
    }

}

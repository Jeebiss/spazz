package net.jeebiss.spazz.github.events;

import net.jeebiss.spazz.Spazz;
import net.jeebiss.spazz.github.Commit;
import net.jeebiss.spazz.github.Repository;
import net.jeebiss.spazz.github.User;
import net.jeebiss.spazz.util.Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CommitEvent {

    private List<Commit> commits;
    private List<String> users;
    private Repository repo;

    public CommitEvent(Repository repo) {
        this.repo = repo;
        commits = new ArrayList<Commit>(){
            @Override
            public boolean add(Commit commit) {
                super.add(commit);
                Collections.sort(this, new Comparator<Commit>() {
                    @Override
                    public int compare(Commit o1, Commit o2) {
                        return o1.getDetails().getAuthor().getDate().compareTo(o2.getDetails().getAuthor().getDate());
                    }
                });
                return true;
            }
        };
        users = new ArrayList<String>();
    }

    public void add(Commit commit) {
        commits.add(commit);
        String committer = commit.getCommitter().getLogin();
        if (!users.contains(committer)) {
            users.add(committer);
        }
    }

    public void fire() {
        if (commits.isEmpty()) return;
        List<String> messages = new ArrayList<String>();
        messages.add("[<O>" + repo.getFullName() + "<C>] <D>" + Utilities.join(users.iterator(), ", ") + "<C> pushed "
                + commits.size() + " commit" + (commits.size() == 1 ? "" : "s") + " to master branch");
        for (Commit commit : commits) {
            String message = commit.getDetails().getMessage().replace("<", "<LT>");
            if (message.contains("\n")) {
                message = message.substring(0, message.indexOf('\n')) + "...";
            }
            messages.add("<D>  " + commit.getAuthor().getLogin()  + "<C>: " + message + " -- " + commit.getShortUrl());
        }
        for (String message : messages)
            Spazz.sendToAllChannels(message);
    }

}

package net.jeebiss.spazz.github;

import net.jeebiss.spazz.Spazz;
import net.jeebiss.spazz.github.events.PushEvent;
import net.jeebiss.spazz.util.Utilities;

import java.util.*;

public class CommitHandler {

    private Repository repo;
    private List<Commit> waiting_commits;
    private Map<String, Commit> commits;
    private List<String> users;

    public CommitHandler(Repository repo) {
        this.repo = repo;
        this.waiting_commits = new ArrayList<Commit>(){
            @Override
            public boolean addAll(Collection<? extends Commit> c) {
                for (Commit commit : c) {
                    if (commit.isDistinct()) add(commit);
                }
                return true;
            }
        };
        this.commits = new HashMap<String, Commit>();
        this.users = new ArrayList<String>();
    }

    public void push(PushEvent event) {
        if (!users.contains(event.getActor().getLogin())) users.add(event.getActor().getLogin());
        waiting_commits.addAll(event.getPayload().getCommits());
    }

    public void fire() {
        if (waiting_commits.isEmpty()) return;
        List<String> messages = new ArrayList<String>();
        messages.add("[<O>" + repo.getFullName() + "<C>] <D>" + Utilities.join(users.iterator(), ", ") + "<C> pushed "
                + waiting_commits.size() + " commit" + (waiting_commits.size() == 1 ? "" : "s") + " to master branch");
        for (Commit commit : waiting_commits) {
            if (commit.isMerge()) continue;
            String message = commit.getMessage().replace("<", "<LT>");
            if (message.contains("\n")) {
                message = message.substring(0, message.indexOf('\n')) + "...";
            }
            messages.add("<D>  " + commit.getAuthor().getName() + "<C>: " + message + " -- " + commit.getShortUrl());
            commits.put(commit.getCommitId(), commit);
        }
        for (String message : messages)
            Spazz.sendToAllChannels(message);
        waiting_commits.clear();
    }

}

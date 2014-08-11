package net.jeebiss.spazz.github;

import net.jeebiss.spazz.Spazz;
import net.jeebiss.spazz.github.events.PushEvent;
import net.jeebiss.spazz.util.Utilities;

import java.util.*;

public class CommitHandler {

    private Repository repo;
    private List<Commit> waiting_commits;
    private Map<String, Commit> commits;

    public CommitHandler(final Repository repo) {
        this.repo = repo;
        this.waiting_commits = new ArrayList<Commit>(){
            @Override
            public boolean addAll(Collection<? extends Commit> c) {
                StringBuilder commitsThing = null;
                if (repo.weHaveParent)
                    commitsThing = Spazz.repoManager.getRepository(repo.parentName).lazyCommitsThing;
                for (Commit commit : c) {
                    if (commit.isMerge() || !commit.isDistinct()) continue;
                    if (commitsThing != null) {
                        if (commitsThing.lastIndexOf(commit.getCommitId().substring(0, 7)) != -1)
                            continue;
                    }
                    else if (!repo.isFork()) {
                        repo.lazyCommitsThing.append(",").append(commit.getCommitId().substring(0, 7));
                    }
                    add(commit);
                }
                return true;
            }
        };
        this.commits = new HashMap<String, Commit>();
    }

    public void push(PushEvent event) {
        waiting_commits.addAll(event.getPayload().getCommits());
    }

    public void fire() {
        if (waiting_commits.isEmpty()) return;
        List<String> users = new ArrayList<String>();
        List<String> messages = new ArrayList<String>();
        for (Commit commit : waiting_commits) {
            String author = commit.getAuthor().getName();
            if (!users.contains(author)) users.add(author);
            String message = commit.getMessage().replace("<", "<LT>")
                    .replaceAll("\n+", " - ");
            messages.add("<D>  " + author + "<C>: " + message + " -- " + commit.getShortUrl());
            commits.put(commit.getCommitId(), commit);
        }
        Spazz.sendToAllChannels("[<O>" + repo.getFullName() + "<C>] <D>" + Utilities.formattedList(users.iterator())
                + "<C> pushed " + waiting_commits.size() + " commit" + (waiting_commits.size() == 1 ? "" : "s")
                + " to master branch");
        for (String message : messages)
            Spazz.sendToAllChannels(message);
        waiting_commits.clear();
    }

}

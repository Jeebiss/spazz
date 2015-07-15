package net.jeebiss.spazz.github;

import net.jeebiss.spazz.Spazz;
import net.jeebiss.spazz.github.events.PushEvent;
import net.jeebiss.spazz.util.Utilities;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommitHandler {

    private Repository repo;
    private CommitList commits;
    private CommitList waiting_commits;

    private PushEvent current_push = null;
    private List<PushEvent> waiting_pushes = new ArrayList<PushEvent>();

    public CommitHandler(final Repository repo) {
        this.repo = repo;
        this.commits = new CommitList(repo);
        this.waiting_commits = new CommitList(repo);
    }

    public void push(PushEvent event) {
        if (current_push == null) {
            waiting_commits.addAll(event.getPayload().getCommits());
            current_push = event;
            if (waiting_pushes.contains(event))
                waiting_pushes.remove(event);
        }
        else {
            waiting_pushes.add(event);
        }
    }

    public void fire() {
        if (current_push == null || waiting_commits == null || waiting_commits.isEmpty()) return;
        List<String> users = new ArrayList<String>();
        List<String> messages = new ArrayList<String>();
        for (Commit commit : waiting_commits) {
            String author = commit.getAuthor().getName();
            if (!users.contains(author))
                users.add(author);
            messages.addAll(commit.format());
            commits.add(commit);
        }
        String sending = "[<O>" + repo.getFullName() + "<C>] <D>" + Utilities.formattedList(users.iterator())
                + "<C> pushed " + waiting_commits.size() + " commit" + (waiting_commits.size() == 1 ? "" : "s")
                + " to '<O>" + current_push.getPayload().getBranch() + "<C>' branch";
        Spazz.sendToAllChannels(sending);
        boolean isRandom = repo.getName().toLowerCase().contains(Spazz.random.substring(1));
        Spazz.sendRandom(sending);
        for (String message : messages) {
            Spazz.sendToAllChannels(message);
            if (isRandom) {
                Spazz.sendRandom(message);
            }
        }
        waiting_commits.clear();
        current_push = null;
        if (!waiting_pushes.isEmpty()) {
            push(waiting_pushes.get(0));
            fire();
        }
    }

    public static class CommitList extends ArrayList<Commit> {
        protected ArrayList<String> ids = new ArrayList<String>();
        protected final Repository repo;

        public CommitList(Repository repository) {
            this.repo = repository;
        }

        @Override
        public boolean add(Commit e) {
            String id = e.getCommitId();
            if (!ids.contains(id)) {
                ids.add(id);
                return super.add(e);
            }
            return true;
        }

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
    }

}

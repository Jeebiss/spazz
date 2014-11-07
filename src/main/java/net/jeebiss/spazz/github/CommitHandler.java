package net.jeebiss.spazz.github;

import net.jeebiss.spazz.Spazz;
import net.jeebiss.spazz.github.events.PushEvent;
import net.jeebiss.spazz.util.Utilities;

import java.util.*;

public class CommitHandler {

    private Repository repo;
    private CommitList commits = new CommitList();
    private CommitList waiting_commits = new CommitList();

    private PushEvent current_push = null;
    private List<PushEvent> waiting_pushes = new ArrayList<PushEvent>();

    public CommitHandler(final Repository repo) {
        this.repo = repo;
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
            if (!users.contains(author)) users.add(author);
            String[] messageSplit = commit.getMessage().replace("<", "<LT>").split("\n+");
            StringBuilder message = new StringBuilder("<D>  ").append(author).append("<C>: ")
                    .append(messageSplit[0]).append(" - ");
            int m = 1; // Just a cheaty way to make sure only 3 lines are in each message
            for (int i = 1; i < messageSplit.length; i++) {
                String msg = messageSplit[i];
                if (m < 3) {
                    char last = msg.charAt(msg.length() - 1);
                    if (last == '.' || last == '?' || last == '!')
                        m = 3;
                    else
                        m++;
                    if (i + 1 == messageSplit.length) {
                        message.append(messageSplit[i]).append(" -- ").append(commit.getShortUrl());
                        messages.add(message.toString());
                    }
                    else
                        message.append(messageSplit[i]).append(" ");
                }
                else {
                    m = 1;
                    messages.add(message.substring(0, message.length() - 1));
                    if (i + 1 == messageSplit.length)
                        messages.add(messageSplit[i] + " -- " + commit.getShortUrl());
                    else
                        message = new StringBuilder(messageSplit[i]).append(" ");
                }
            }
            if (messages.isEmpty())
                messages.add(message.substring(0, message.length()-3) + " -- " + commit.getShortUrl());
            commits.add(commit);
        }
        Spazz.sendToAllChannels("[<O>" + repo.getFullName() + "<C>] <D>" + Utilities.formattedList(users.iterator())
                + "<C> pushed " + waiting_commits.size() + " commit" + (waiting_commits.size() == 1 ? "" : "s")
                + " to '" + current_push.getPayload().getBranch() + "' branch");
        for (String message : messages)
            Spazz.sendToAllChannels(message);
        waiting_commits.clear();
        current_push = null;
        if (!waiting_pushes.isEmpty()) {
            push(waiting_pushes.get(0));
        }
    }

    private class CommitList extends ArrayList<Commit> {
        private ArrayList<String> ids = new ArrayList<String>();

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

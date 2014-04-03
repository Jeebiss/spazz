package net.jeebiss.spazz.github;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import net.jeebiss.spazz.github.events.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Repository {

    private GitHub root;
    private Gson gson;

    private long updateDelay;
    private boolean hasIssues = false;
    private boolean hasComments = false;
    private boolean hasPulls = false;

    private boolean shutdown = false;
    private boolean started = false;

    private List<String> events = new ArrayList<String>();
    private HashMap<String, Commit> commits = new HashMap<String, Commit>();

    private String name;
    private String full_name;
    private User owner;
    private String url;
    private String html_url;
    private String issues_url;
    private String pulls_url;
    private String commits_url;
    private String events_url;

    public Repository _init(GitHub root, long updateDelay, boolean hasIssues, boolean hasComments, boolean hasPulls) {
        this.root = root;
        this.gson = Requester.getGson();
        fireEvents();
        this.updateDelay = updateDelay;
        this.hasIssues = hasIssues;
        this.hasComments = hasComments;
        this.hasPulls = hasPulls;
        new Thread(new RepositoryChecker()).start();
        started = true;
        return this;
    }

    public void shutdown() {
        shutdown = true;
    }

    public GitHub getGitHub() {
        return root;
    }

    public double getUpdateDelay() {
        return updateDelay / 1000;
    }

    public boolean hasIssues() {
        return hasIssues;
    }

    public boolean hasPulls() {
        return hasPulls;
    }

    public boolean hasComments() {
        return hasComments;
    }

    public HashMap<String, Commit> getCommits() {
        return commits;
    }

    public Issue getIssue(int number) {
        return root.retrieve().parse(issues_url.replaceAll("\\{.+\\}", "/" + number), Issue.class);
    }

    public void fireEvents() {
        JsonArray eventsList = root.retrieve().parseArray(events_url.replaceAll("\\{.+\\}", ""));
        IssueCommentEvent icEvent = null;
        boolean ic = false;
        for (int i = eventsList.size()-1; i > -1; i--) {
            Event event = gson.fromJson(eventsList.get(i), Event.class);
            if (!events.contains(event.getId())) {
                if (started) {
                    ic = false;
                    if (event instanceof CommentEvent && hasComments) {
                        if (event instanceof IssueCommentEvent && eventsList.size() - i > 0) {
                            icEvent = (IssueCommentEvent) event;
                            ic = true;
                        }
                        else {
                            event.fire();
                        }
                    }
                    else if ((event instanceof IssuesEvent && hasIssues)
                            || (event instanceof PullRequestEvent && hasPulls)) {
                        int number = event instanceof IssuesEvent
                                ? ((IssuesEvent) event).getIssueNumber()
                                : ((PullRequestEvent) event).getIssueNumber();
                        if (icEvent != null) {
                            if (event.getActor().getLogin().equals(icEvent.getActor().getLogin())
                                    && number == icEvent.getIssueNumber()) {
                                event.fire(icEvent.getPayload().getComment().getShortUrl());
                                icEvent = null;
                            }
                            else {
                                icEvent.fire();
                                icEvent = null;
                                event.fire();
                            }
                        }
                        else {
                            event.fire();
                        }
                    }
                    if (icEvent != null && !ic) {
                        icEvent.fire();
                        icEvent = null;
                    }
                }
                events.add(event.getId());
            }
        }
        JsonArray commitsArray = root.retrieve().parseArray(commits_url.replaceAll("\\{.+\\}", "") + "?per_page=100");
        CommitEvent event = new CommitEvent(this);
        for (int i = 0; i < commitsArray.size(); i++) {
            Commit commit = gson.fromJson(commitsArray.get(i), Commit.class);
            if (!commits.containsKey(commit.getCommitId())) {
                if (started) event.add(commit);
                commits.put(commit.getCommitId(), commit);
            }
        }
        event.fire();
    }

    public User getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return full_name;
    }

    public class RepositoryChecker implements Runnable {

        @Override
        public void run() {
            while (!shutdown) {
                try {
                    Thread.sleep(updateDelay);
                    fireEvents();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

}

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
    private CommitHandler commitHandler;

    private String name;
    private String full_name;
    private User owner;
    private String url;
    private String html_url;
    private String issues_url;
    private String pulls_url;
    private String commits_url;
    private String events_url;
    private boolean has_issues; // boolean to check if issues are disabled on the repo

    public Repository _init(GitHub root, long updateDelay, boolean hasIssues, boolean hasComments, boolean hasPulls) {
        this.root = root;
        this.gson = Requester.getGson();
        commitHandler = new CommitHandler(this);
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

    public Issue getIssue(int number) {
        String url = has_issues ? issues_url : pulls_url;
        return root.retrieve().parse(url.replaceAll("\\{.+\\}", "/" + number), Issue.class);
    }

    public Commit getCommit(String commitId) {
        return root.retrieve().parse(commits_url.replaceAll("\\{.+\\}", "/" + commitId), Commit.class);
    }

    public void fireEvents() {
        JsonArray eventsList = root.retrieve().parseArray(events_url.replaceAll("\\{.+\\}", ""));
        IssueCommentEvent icEvent = null;
        boolean ic = false;
        for (int i = eventsList.size()-1; i >= 0; i--) {
            Event event = gson.fromJson(eventsList.get(i), Event.class);
            if (!events.contains(event.getId())) {
                if (started) {
                    ic = false;
                    if (event instanceof CommentEvent && hasComments) {
                        if (event instanceof IssueCommentEvent) {
                            if (icEvent != null) icEvent.fire();
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
                    else if (event instanceof PushEvent) {
                        commitHandler.push((PushEvent) event);
                    }
                    if (icEvent != null && !ic) {
                        icEvent.fire();
                        icEvent = null;
                    }
                }
                events.add(event.getId());
            }
        }
        if (icEvent != null) icEvent.fire();
        commitHandler.fire();
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

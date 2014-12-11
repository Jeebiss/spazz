package net.jeebiss.spazz.github;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import net.jeebiss.spazz.Spazz;
import net.jeebiss.spazz.github.events.*;

import java.util.ArrayList;
import java.util.List;

public class Repository {

    private GitHub root;
    private Gson gson;

    private long updateDelay;
    private boolean hasIssues = false;
    private boolean hasComments = false;
    private boolean hasPulls = false;

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
    private Repository parent;
    private boolean fork;

    public String parentName = null;
    public boolean weHaveParent = false;
    public StringBuilder lazyCommitsThing = new StringBuilder();
    
    private List<Integer> requestsPerHour = new ArrayList<Integer>();
    private int currentRequests = 0;

    private RepositoryChecker checker;
    private Thread checkerThread;
    private boolean invalid = false;

    public Repository _init(GitHub root, long updateDelay, boolean hasIssues, boolean hasComments, boolean hasPulls) {
        this.root = root;
        this.gson = Requester.getGson();
        commitHandler = new CommitHandler(this);
        fireEvents();
        this.updateDelay = updateDelay;
        this.hasIssues = hasIssues;
        this.hasComments = hasComments;
        this.hasPulls = hasPulls;
        if (parent != null) parentName = parent.getFullName();
        checker = new RepositoryChecker();
        checkerThread = new Thread(checker);
        checkerThread.start();
        started = true;
        return this;
    }

    public void setStuff(long updateDelay, boolean hasIssues, boolean hasComments, boolean hasPulls) {
        this.updateDelay = updateDelay;
        this.hasIssues = hasIssues;
        this.hasComments = hasComments;
        this.hasPulls = hasPulls;
    }

    public void shutdown() {
        checker.stop();
        invalid = true;
        try {
            checkerThread.interrupt();
            checkerThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public GitHub getGitHub() {
        return root;
    }

    public String getUrl() { return html_url; }

    public double getUpdateDelay() {
        return updateDelay / 1000;
    }
    
    public void upStats() {
        currentRequests++;
    }

    public void addStat(int stat) {
        requestsPerHour.add(stat);
    }
    
    public void saveStats() {
        requestsPerHour.add(currentRequests);
        currentRequests = 0;
    }
    
    public int averageStats() {
        int avg = 0;
        for (int i : requestsPerHour)
            avg += i;
        return avg > 0 ? avg/requestsPerHour.size() : 0;
    }

    public boolean isFork() { return fork; }

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
        upStats();
        return root.retrieve().parse(url.replaceAll("\\{.+\\}", "/" + number), Issue.class);
    }

    public Commit getCommit(String commitId) {
        upStats();
        return root.retrieve().parse(commits_url.replaceAll("\\{.+\\}", "/" + commitId), Commit.class);
    }

    public void fireEvents() {
        if (invalid) {
            return;
        }
        if (!weHaveParent && parentName != null) {
            weHaveParent = Spazz.repoManager.hasRepository(parentName);
        }
        JsonArray eventsList = root.retrieve().parseArray(events_url.replaceAll("\\{.+\\}", ""));
        upStats();
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
            }
            events.add(event.getId());
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
        private boolean go = true;

        public void stop() {
            go = false;
        }

        @Override
        public void run() {
            while (go) {
                if (invalid)
                    return;
                try {
                    Thread.sleep(updateDelay);
                    fireEvents();
                } catch (Exception e) {}
            }
        }
    }

}

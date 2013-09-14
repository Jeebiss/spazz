package net.jeebiss.spazz.github;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import net.jeebiss.spazz.Utilities;
import net.jeebiss.spazz.github.Comment;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class Repository {
    
    private final GitHub root;

    private long updateDelay;
    private boolean hasIssues = false;
    private JSONObject information;
    
    private boolean shutdown = false;
    
    private HashMap<Integer, Issue> openIssues;
    private HashMap<Integer, Issue> closedIssues;
    private HashMap<String, Commit> commits;
    private HashMap<Integer, Comment> comments;
    
    public Repository(GitHub root, long updateDelay, boolean hasIssues, JSONObject information) {
        this.root = root;
        this.information = information;
        if (updateDelay > 2000) {
            this.updateDelay = updateDelay;
            new Thread(new RepositoryChecker()).start();
        }
        if (hasIssues) {
            this.hasIssues = true;
            openIssues = getIssues(true);
            closedIssues = getIssues(false);
        }
        commits = getCommits();
        comments = getComments();
    }
    
    public boolean reload() {
        try {
            information = root.retrieve().parse(((String) information.get("url")).replaceAll("\\{.+\\}", ""));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        reloadCommits();
        if (hasIssues)
            reloadIssues();
        reloadComments();
        return true;
    }
    
    public void shutdown() {
        shutdown = true;
    }
    
    public GitHub getGitHub() {
        return root;
    }
    
    public int getOpenIssueCount() {
        return (int) information.get("open_issues_count");
    }
    
    public HashMap<Integer, Issue> getLoadedIssues(boolean open) {
        if (open)
            return openIssues;
        else
            return closedIssues;
    }
    
    public HashMap<String, Commit> getLoadedCommits() {
        return commits;
    }
    
    @SuppressWarnings("unchecked")
    public HashMap<Integer, Issue> getIssues(boolean open) {
        HashMap<Integer, Issue> issues = new HashMap<Integer, Issue>();
        try {
            JSONArray openIssuesArray = root.retrieve().parseArray(((String) information.get("issues_url")).replaceAll("\\{.+\\}", "") + "?state=" + (open ? "open" : "closed") + "&sort=updated");
            for (int i = 0; i < openIssuesArray.size(); i++) {
                Issue issue = new Issue(root, this, Utilities.getJSONFromMap((Map<String, Object>) openIssuesArray.get(i)));
                issues.put(issue.getNumber(), issue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return issues;
    }
    
    @SuppressWarnings("unchecked")
    public HashMap<String, Commit> getCommits() {
        HashMap<String, Commit> newCommits = new HashMap<String, Commit>();
        try {
            JSONArray commitsArray = root.retrieve().parseArray(((String) information.get("commits_url")).replaceAll("\\{.+\\}", "") + "?per_page=100");
            for (int i = 0; i < commitsArray.size(); i++) {
                Commit commit = new Commit(root, this, Utilities.getJSONFromMap((Map<String, Object>) commitsArray.get(i)));
                newCommits.put(commit.getCommitId(), commit);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newCommits;
    }
    
    @SuppressWarnings("unchecked")
    public HashMap<Integer, Comment> getComments() {
        HashMap<Integer, Comment> newComments = new HashMap<Integer, Comment>();
        try {
            JSONArray eventsList = root.retrieve().parseArray(((String) information.get("events_url")).replaceAll("\\{.+\\}", ""));
            for (int i = eventsList.size()-1; i > -1; i--) {
                Map<String, Object> map = (Map<String, Object>) eventsList.get(i);
                Map<String, Object> payload = (Map<String, Object>) map.get("payload");
                if (((String) map.get("type")).equals("IssueCommentEvent")) {
                    Issue issue = new Issue(root, this, Utilities.getJSONFromMap((Map<String, Object>) payload.get("issue")));
                    IssueComment comment = new IssueComment(root, issue, Utilities.getJSONFromMap((Map<String, Object>) payload.get("comment")));
                    newComments.put(comment.getCommentId(), comment);
                }
                if (((String) map.get("type")).equals("CommitCommentEvent")) {
                    JSONObject commentMap = Utilities.getJSONFromMap((Map<String, Object>) payload.get("comment"));
                    Commit commit = commits.get((String) commentMap.get("commit_id"));
                    CommitComment comment = new CommitComment(root, commit, commentMap);
                    newComments.put(comment.getCommentId(), comment);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newComments;
    }
    
    @SuppressWarnings("unchecked")
    public User getOwner() {
        try {
            return new User(root, Utilities.getJSONFromMap((Map<String,Object>) information.get("owner")));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String getName() {
        return (String) information.get("name");
    }
    
    public String getFullName() {
        return (String) information.get("full_name");
    }
    
    private void reloadComments() {
        HashMap<Integer, Comment> newComments = getComments();
        for (Comment newComment : newComments.values()) {
            if (!comments.containsKey(newComment.getCommentId())) {
                new CommentEvent(root, this, newComment);
            }
        }
        comments = newComments;
    }
    
    private void reloadIssues() {
        HashMap<Integer, Issue> newOpenIssues = getIssues(true);
        HashMap<Integer, Issue> newClosedIssues = getIssues(false);
        for (Issue newClosedIssue : newClosedIssues.values()) {
            if (openIssues.containsKey(newClosedIssue.getNumber()) && !committedPullRequests.contains(newClosedIssue.getNumber())) {
                new IssueEvent(root, newClosedIssue, IssueEvent.State.CLOSED);
                continue;
            }
        }
        for (Issue newOpenIssue : newOpenIssues.values()) {
            if (closedIssues.containsKey(newOpenIssue.getNumber())) {
                new IssueEvent(root, newOpenIssue, IssueEvent.State.REOPENED);
                continue;
            }
            if (!openIssues.containsKey(newOpenIssue.getNumber())) {
                new IssueEvent(root, newOpenIssue, IssueEvent.State.OPENED);
                continue;
            }
        }
        openIssues = newOpenIssues;
        closedIssues = newClosedIssues;
    }
    
    private ArrayList<Integer> committedPullRequests = new ArrayList<Integer>();
    
    private void reloadCommits() {
        HashMap<String, Commit> newCommits = getCommits();
        ArrayList<Commit> eventCommits = new ArrayList<Commit>();
        for (Commit newCommit : newCommits.values()) {
            if (!commits.containsKey(newCommit.getCommitId())) {
                eventCommits.add(newCommit);
                if (newCommit.isPullRequest()) {
                    committedPullRequests.add(Integer.valueOf(Pattern.compile("Merge pull request #(\\d+) from .+")
                            .matcher(newCommit.getMessage()).group(0)));
                }
            }
        }
        if (!eventCommits.isEmpty()) {
            new CommitEvent(root, this, eventCommits);
        }
        commits = newCommits;
    }
    
    public class RepositoryChecker implements Runnable {
        
        @Override
        public void run() {
            while (!shutdown) {
                try {
                    Thread.sleep(updateDelay);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                reload();
            }
        }
        
    }
    
}

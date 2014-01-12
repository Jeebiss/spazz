package net.jeebiss.spazz.github;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jeebiss.spazz.util.Utilities;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class Repository {
    
    private final GitHub root;

    private long updateDelay;
    private boolean hasIssues = false;
    private boolean hasComments = false;
    private JSONObject information;
    
    private boolean shutdown = false;
    
    private HashMap<Integer, Issue> openIssues;
    private HashMap<Integer, Issue> closedIssues;
    private HashMap<Integer, Issue> allIssues;
    private HashMap<String, Commit> commits;
    private HashMap<Integer, Comment> comments;
    
    public Repository(GitHub root, long updateDelay, boolean hasIssues, boolean hasComments, JSONObject information) {
        this.root = root;
        this.information = information;
        if (updateDelay > 5000) {
            this.updateDelay = updateDelay;
            new Thread(new RepositoryChecker()).start();
        }
        commits = getCommits();
        if (hasIssues) {
            allIssues = new HashMap<Integer, Issue>();
            this.hasIssues = true;
            this.hasComments = true;
            openIssues = getIssues(true);
            closedIssues = getIssues(false);
            allIssues.putAll(openIssues);
            allIssues.putAll(closedIssues);
            comments = getComments();
        }
    }
    
    public boolean reload() {
        try {
            information = root.retrieve().parse(((String) information.get("url")).replaceAll("\\{.+\\}", ""));
        } catch (Exception e) {
            return false;
        }
        reloadCommits();
        if (hasIssues)
            reloadIssues();
        if (hasComments)
            reloadComments();
        return true;
    }
    
    public void shutdown() {
        shutdown = true;
    }
    
    public GitHub getGitHub() {
        return root;
    }
    
    public double getUpdateDelay() {
        return updateDelay/1000;
    }
    
    public boolean hasIssues() {
        return hasIssues;
    }
    
    public boolean hasComments() {
        return hasComments;
    }
    
    public int getOpenIssueCount() {
        return (int) information.get("open_issues_count");
    }
    
    public HashMap<Integer, Issue> getLoadedIssuesAll() {
        return allIssues;
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
    
    public Issue getIssue(int issueNumber) {
        if (allIssues != null && allIssues.containsKey(issueNumber)) {
            return allIssues.get(issueNumber);
        }
        else {
            try {
                Issue issue = new Issue(root, this, root.retrieve().parse(((String) information.get("issues_url"))
                        .replaceAll("\\{.+\\}", "") + "/" + issueNumber));
                allIssues.put(issueNumber, issue);
                return issue;
            } catch (Exception e) {
                return null;
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public HashMap<Integer, Issue> getIssues(boolean open) {
        HashMap<Integer, Issue> issues = null;
        try {
            JSONArray openIssuesArray = root.retrieve().parseArray(((String) information.get("issues_url")).replaceAll("\\{.+\\}", "") + "?state=" + (open ? "open" : "closed") + "&sort=updated");
            issues = new HashMap<Integer, Issue>();
            for (int i = 0; i < openIssuesArray.size(); i++) {
                Issue issue = new Issue(root, this, Utilities.getJSONFromMap((Map<String, Object>) openIssuesArray.get(i)));
                issues.put(issue.getNumber(), issue);
            }
        } catch (Exception e) {}
        return issues;
    }
    
    @SuppressWarnings("unchecked")
    public HashMap<String, Commit> getCommits() {
        HashMap<String, Commit> newCommits = null;
        try {
            JSONArray commitsArray = root.retrieve().parseArray(((String) information.get("commits_url")).replaceAll("\\{.+\\}", "") + "?per_page=100");
            newCommits = new HashMap<String, Commit>();
            for (int i = 0; i < commitsArray.size(); i++) {
                Commit commit = new Commit(root, this, Utilities.getJSONFromMap((Map<String, Object>) commitsArray.get(i)));
                newCommits.put(commit.getCommitId(), commit);
            }
        } catch (Exception e) {}
        return newCommits;
    }
    
    @SuppressWarnings("unchecked")
    public HashMap<Integer, Comment> getComments() {
        HashMap<Integer, Comment> newComments = null;
        try {
            JSONArray eventsList = root.retrieve().parseArray(((String) information.get("events_url")).replaceAll("\\{.+\\}", ""));
            newComments = new HashMap<Integer, Comment>();
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
        } catch (Exception e) {}
        return newComments;
    }
    
    @SuppressWarnings("unchecked")
    public User getOwner() {
        try {
            return new User(root, Utilities.getJSONFromMap((Map<String,Object>) information.get("owner")));
        } catch (Exception e) {
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
        try {
            HashMap<Integer, Comment> newComments = getComments();
            for (Comment newComment : newComments.values()) {
                if (!comments.containsKey(newComment.getCommentId())) {
                    new CommentEvent(root, this, newComment);
                }
            }
            comments = newComments;
        } catch(Exception e) {}
    }
    
    private void reloadIssues() {
        try {
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
            allIssues.putAll(openIssues);
            allIssues.putAll(closedIssues);
        } catch(Exception e) {}
    }
    
    private ArrayList<Integer> committedPullRequests = new ArrayList<Integer>();
    
    private void reloadCommits() {
        try {
            HashMap<String, Commit> newCommits = getCommits();
            ArrayList<Commit> eventCommits = new ArrayList<Commit>();
            for (Commit newCommit : newCommits.values()) {
                if (!commits.containsKey(newCommit.getCommitId())) {
                    eventCommits.add(newCommit);
                    Matcher m = Pattern.compile("^Merge pull request #(\\d+) from .+$").matcher(newCommit.getMessage());
                    if (m.matches()) {
                        committedPullRequests.add(Integer.valueOf(m.group(1)));
                    }
                }
            }
            if (!eventCommits.isEmpty()) {
                new CommitEvent(root, this, eventCommits);
            }
            commits = newCommits;
        } catch(Exception e) {}
    }
    
    public class RepositoryChecker implements Runnable {
        
        @Override
        public void run() {
            while (!shutdown) {
                try {
                    Thread.sleep(updateDelay);
                    reload();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
    }
    
}

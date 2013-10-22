package net.jeebiss.spazz.github;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RepositoryManager {
    
    private final GitHub root;
    
    private Map<String, Repository> repositories = new HashMap<String, Repository>();

    public RepositoryManager(GitHub root) {
        this.root = root;
    }
    
    public Repository getRepository(String project) {
        if (repositories.containsKey(project))
            return repositories.get(project);
        else
            return null;
    }
    
    public Set<String> getRepositories() {
        return repositories.keySet();
    }
    
    public void reloadAll() {
        for (Repository repo : repositories.values()) {
            repo.reload();
        }
    }
    
    public void shutdown() {
        for (Repository repo : repositories.values()) {
            repo.shutdown();
        }
    }
    
    public GitHub getGitHub() {
        return root;
    }
    
    public boolean addRepository(String owner, String project, double updateDelay, boolean hasIssues) {
        try {
            repositories.put(project, root.getRepository(owner, project, ((long)updateDelay)*1000, hasIssues));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean removeRepository(String project) {
        try {
            if (!repositories.containsKey(project))
                return false;
            repositories.get(project).shutdown();
            repositories.remove(project);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    
}

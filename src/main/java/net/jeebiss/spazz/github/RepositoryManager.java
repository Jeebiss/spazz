package net.jeebiss.spazz.github;

import java.util.HashMap;
import java.util.Map;

public class RepositoryManager {
    
    private final GitHub root;
    
    private Map<String, Repository> repositories = new HashMap<String, Repository>();

    public RepositoryManager(GitHub root) {
        this.root = root;
        new Thread(new RepositoryChecker()).start();
    }
    
    public Repository getRepository(String project) {
        if (repositories.containsKey(project))
            return repositories.get(project);
        else
            return null;
    }
    
    public void reloadAll() {
        for (Repository repo : repositories.values()) {
            repo.reload();
        }
    }
    
    public GitHub getGitHub() {
        return root;
    }
    
    public boolean addRepository(String owner, String project) {
        try {
            repositories.put(project, root.getRepository(owner, project));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private class RepositoryChecker implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(10000);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                reloadAll();
            }
        }
        
    }
    
}

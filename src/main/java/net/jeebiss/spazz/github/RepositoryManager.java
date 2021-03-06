package net.jeebiss.spazz.github;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class RepositoryManager {

    private final GitHub root;
    private Map<String, Repository> repositories = new HashMap<String, Repository>();

    private RateLimitChecker checker;
    private Thread checkerThread;

    public RepositoryManager(GitHub root) {
        this.root = root;
        this.checker = new RateLimitChecker();
        this.checkerThread = new Thread(checker);
        checkerThread.start();
        try {
            loadAll();
        } catch (Exception e) {
            System.out.println("Failed to load 'storage/repositories.yml'... Loading default repositories.");
            addRepository("DenizenScript/Denizen-For-Bukkit", 20, true, true, true);
            addRepository("DenizenScript/Denizen-Core", 25, true, true, true);
            addRepository("DenizenScript/Depenizen-For-Bukkit", 30, true, true, true);
            addRepository("CitizensDev/Citizens2", 30, true, true, true);
            addRepository("CitizensDev/CitizensAPI", 30, true, true, true);
            addRepository("Jeebiss/spazz", 30, true, true, true);
            addRepository("jrbudda/Sentry", 100, true, true, false);
        }
    }

    public Repository searchRepository(String input) {
        input = input.toLowerCase().trim();
        Repository contains = getRepository(input);
        if (contains != null) {
            return contains;
        }
        String[] search = input.split("\\s+");
        int highest = 0;
        Repository repo = null;
        for (Map.Entry<String, Repository> entry : repositories.entrySet()) {
            int number = 0;
            String key = entry.getKey();
            for (String s : search) {
                if (key.contains(s))
                    number++;
            }
            if (number > highest) {
                repo = entry.getValue();
                highest = number;
            }
        }
        return repo;
    }

    public Repository getRepository(String ownerProject) {
        if (repositories.containsKey(ownerProject.toLowerCase()))
            return repositories.get(ownerProject.toLowerCase());
        else
            return null;
    }

    public Set<String> getRepositories() {
        return repositories.keySet();
    }

    public void shutdown(boolean restart) {
        try {
            if (!restart) {
                saveAll();
                checker.stop();
                checkerThread.interrupt();
                checkerThread.join();
            }
            for (Repository repo : repositories.values()) {
                repo.shutdown();
            }
            repositories.clear();
        } catch (Exception e) {}
    }

    public GitHub getGitHub() {
        return root;
    }

    public boolean addRepository(String ownerProject, double updateDelay, boolean hasIssues, boolean hasComments, boolean hasPulls) {
        try {
            repositories.put(ownerProject.toLowerCase(), root.getRepository(ownerProject)
                    ._init(root, (long) updateDelay * 1000, hasIssues, hasComments, hasPulls));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failure adding repository '" + ownerProject + "': " + e.getMessage());
            return false;
        }
    }

    public boolean removeRepository(String ownerProject) {
        try {
            if (!repositories.containsKey(ownerProject))
                return false;
            repositories.get(ownerProject).shutdown();
            repositories.remove(ownerProject);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasRepository(String ownerProject) {
        return repositories.containsKey(ownerProject.toLowerCase());
    }

    public void saveAll() throws Exception {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);

        Map<String, HashMap<String, HashMap<String, Object>>> data = new HashMap<String, HashMap<String, HashMap<String, Object>>>();
        for (Repository repo : repositories.values()) {
            String[] proj = repo.getFullName().split("/");
            if (!data.containsKey(proj[0]))
                data.put(proj[0], new HashMap<String, HashMap<String, Object>>());
            data.get(proj[0]).put(proj[1], new HashMap<String, Object>());
            data.get(proj[0]).get(proj[1]).put("delay", repo.getUpdateDelay());
            data.get(proj[0]).get(proj[1]).put("has_issues", repo.hasIssues());
            data.get(proj[0]).get(proj[1]).put("has_comments", repo.hasComments());
            data.get(proj[0]).get(proj[1]).put("has_pulls", repo.hasPulls());
            data.get(proj[0]).get(proj[1]).put("requests_per_hour", repo.averageStats());
        }

        File rf = new File(System.getProperty("user.dir") + "/storage/repositories.yml");
        if (!rf.exists())
            rf.createNewFile();
        FileWriter writer = new FileWriter(System.getProperty("user.dir") + "/storage/repositories.yml");
        writer.write(yaml.dump(data));
        writer.close();
    }

    @SuppressWarnings("unchecked")
    public void loadAll() throws Exception {
        shutdown(true);
        HashMap<String, HashMap<String, HashMap<String, Object>>> map = null;
        Yaml yaml = new Yaml();
        File f = new File(System.getProperty("user.dir") + "/storage/repositories.yml");
        InputStream is = f.toURI().toURL().openStream();
        map = (HashMap<String, HashMap<String, HashMap<String, Object>>>) yaml.load(is);
        is.close();
        for (Entry<String, HashMap<String, HashMap<String, Object>>> owner : map.entrySet()) {
            for (Entry<String, HashMap<String, Object>> repo : owner.getValue().entrySet()) {
                addRepository(owner.getKey() + "/" + repo.getKey(),
                        (double) repo.getValue().get("delay"), (boolean) repo.getValue().get("has_issues"),
                        (boolean) repo.getValue().get("has_comments"), (boolean) repo.getValue().get("has_pulls"));
                if (repo.getValue().containsKey("requests_per_hour"))
                    getRepository(owner.getKey() + "/" + repo.getKey())
                            .addStat((int) repo.getValue().get("requests_per_hour"));
            }
        }
    }

    public void saveStats() {
        for (Repository repository : repositories.values()) {
            repository.saveStats();
        }
    }

    public class RateLimitChecker implements Runnable {
        private final long wait = 360000;
        private boolean go = true;

        public void stop() {
            go = false;
        }

        @Override
        public void run() {
            try {
                final long reset = root.getRateLimit().getRate().getReset() * 1000;
                Thread.sleep(reset - Calendar.getInstance().getTimeInMillis());
                saveStats();
                while(go) {
                    Thread.sleep(wait);
                    saveStats();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

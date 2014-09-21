package net.jeebiss.spazz.github;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class RepositoryManager {

    private final GitHub root;

    private Map<String, Repository> repositories = new HashMap<String, Repository>();

    public RepositoryManager(GitHub root) {
        this.root = root;

        try {
            loadAll();
        } catch (Exception e) {
            System.out.println("Failed to load 'storage/repositories.yml'... Loading default repositories.");
            addRepository("DenizenScript/Denizen-for-Bukkit", 20, true, true, true);
            addRepository("Morphan1/Depenizen", 30, true, true, true);
            addRepository("CitizensDev/Citizens2", 20, true, true, true);
            addRepository("CitizensDev/CitizensAPI", 30, true, true, true);
            addRepository("Jeebiss/spazz", 60, true, true, true);
            addRepository("jrbudda/Sentry", 100, true, true, false);
        }
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

    public void shutdown() {
        try {
            saveAll();
            for (Repository repo : repositories.values()) {
                repo.shutdown();
            }
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
        HashMap<String, HashMap<String, HashMap<String, Object>>> map = null;
        Yaml yaml = new Yaml();
        File f = new File(System.getProperty("user.dir") + "/storage/repositories.yml");
        InputStream is = f.toURI().toURL().openStream();
        map = (HashMap<String, HashMap<String, HashMap<String, Object>>>) yaml.load(is);
        for (Entry<String, HashMap<String, HashMap<String, Object>>> owner : map.entrySet()) {
            for (Entry<String, HashMap<String, Object>> repo : owner.getValue().entrySet()) {
                addRepository(owner.getKey() + "/" + repo.getKey(),
                        (double) repo.getValue().get("delay"), (boolean) repo.getValue().get("has_issues"),
                        (boolean) repo.getValue().get("has_comments"), (boolean) repo.getValue().get("has_pulls"));
            }
        }
    }

}

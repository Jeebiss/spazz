package net.jeebiss.spazz.github;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class RepositoryManager {

    private final GitHub root;

    private Map<String, Repository> repositories = new HashMap<String, Repository>();

    public RepositoryManager(GitHub root) {
        this.root = root;

        try {
            loadAll();
        } catch (Exception e) {
            e.printStackTrace();
            addRepository("aufdemrand", "Denizen", 20, true, true, true);
            addRepository("Morphan1", "Depenizen", 30, true, true, true);
            addRepository("CitizensDev", "Citizens2", 20, false, false, false);
            addRepository("CitizensDev", "CitizensAPI", 30, false, false, false);
            addRepository("Jeebiss", "spazz", 60, true, true, true);
            addRepository("jrbudda", "Sentry", 100, true, true, false);
        }
    }

    public Repository getRepository(String project) {
        if (repositories.containsKey(project.toLowerCase()))
            return repositories.get(project.toLowerCase());
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
        try {
            saveAll();
            for (Repository repo : repositories.values()) {
                repo.shutdown();
            }
        } catch (Exception e) {
        }
    }

    public GitHub getGitHub() {
        return root;
    }

    public boolean addRepository(String owner, String project, double updateDelay, boolean hasIssues, boolean hasComments, boolean hasPulls) {
        try {
            repositories.put(project.toLowerCase(), root.getRepository(owner, project, ((long) updateDelay) * 1000, hasIssues, hasComments, hasPulls));
            return true;
        } catch (Exception e) {
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
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasRepository(String author, String project) {
        return (repositories.containsKey(project.toLowerCase())
                && repositories.get(project).getOwner().getLogin().equalsIgnoreCase(author));
    }

    public boolean hasRepository(String project) {
        return repositories.containsKey(project.toLowerCase());
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
                addRepository(owner.getKey(), repo.getKey(),
                        (double) repo.getValue().get("delay"), (boolean) repo.getValue().get("has_issues"),
                        (boolean) repo.getValue().get("has_comments"), (boolean) repo.getValue().get("has_pulls"));
            }
        }
    }

}

package net.jeebiss.spazz.github.meta.objects;

import java.util.ArrayList;
import java.util.HashMap;

public class dCommand implements Meta {
    
    private HashMap<String, ArrayList<String>> commandMeta;
    
    private String name;
    private String syntax;
    private ArrayList<String> shortDesc;
    private String author;
    private String stable;
    private ArrayList<String> usage;
    private ArrayList<String> description;
    private ArrayList<String> tags;
    private String deprecated;
    private String plugin = null;

    public dCommand(HashMap<String, ArrayList<String>> commandMeta) {
        this.commandMeta = commandMeta;
        
        this.name = commandMeta.get("name").get(0);
        this.syntax = commandMeta.get("syntax").get(0);
        this.shortDesc = commandMeta.get("short");
        this.usage = commandMeta.get("usage");
        this.description = commandMeta.get("description");
        this.stable = commandMeta.get("stable").get(0);
        if (commandMeta.containsKey("author"))
            this.author = commandMeta.get("author").get(0);
        if (commandMeta.containsKey("tags"))
            this.tags = commandMeta.get("tags");
        if (commandMeta.containsKey("plugin"))
            this.plugin = commandMeta.get("plugin").get(0);
    }
    
    public String getName() {
        return name;
    }
    
    public String getNameLwr() {
        return name.toLowerCase();
    }
    
    public String getSyntax() {
        return syntax;
    }
    
    public ArrayList<String> getShort() {
        return shortDesc;
    }
    
    public String getAuthor() {
        return author;
    }

    public ArrayList<String> getUsage() {
        return usage;
    }
    
    public ArrayList<String> getDescription() {
        return description;
    }
    
    public ArrayList<String> getTags() {
        return tags;
    }
    
    public String getStable() {
        return stable;
    }
    
    public String getDepReason() {
        return deprecated;
    }
    
    public String getPlugin() {
        return plugin;
    }
    
    public boolean isDepenizen() {
        return (plugin != null);
    }
    
    public boolean isDeprecated() {
        return (!deprecated.isEmpty());
    }

    @Override
    public String getObjectType() {
        return "dCommand";
    }
    
    @Override
    public HashMap<String, ArrayList<String>> getMeta() {
        return commandMeta;
    }
    
}

package net.jeebiss.spazz.github.meta.objects;

import java.util.ArrayList;
import java.util.HashMap;

public class dTag implements Meta, Cloneable {
    
    private HashMap<String, ArrayList<String>> tagMeta;
    
    private String attribute;
    private String returns;
    private ArrayList<String> description;
    private String deprecated;
    private String plugin = null;
    
    public dTag(HashMap<String, ArrayList<String>> tagMeta) {
        this.tagMeta = tagMeta;
        
        this.attribute = tagMeta.get("attribute").get(0);
        this.returns = tagMeta.get("returns").get(0);
        this.description = tagMeta.get("description");
        if (tagMeta.containsKey("deprecated"))
            this.deprecated = tagMeta.get("deprecated").get(0);
        if (tagMeta.containsKey("plugin"))
            this.plugin = tagMeta.get("plugin").get(0);
    }
    
    public String getFullTag() {
        return attribute;
    }
    
    public String getAttribute() {
        return attribute.substring(1, attribute.length()-1);
    }
    
    public String getReturns() {
        return returns;
    }
    
    public ArrayList<String> getDescription() {
        return description;
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
        return (deprecated != null);
    }
    
    public dTag setAttribute(String attribute) {
        this.attribute = attribute;
        return this;
    }

    @Override
    public String getObjectType() {
        return "dTag";
    }

    @Override
    public HashMap<String, ArrayList<String>> getMeta() {
        return tagMeta;
    }
    
    @Override
    public dTag clone() {
        return new dTag(tagMeta);
    }

}

package net.jeebiss.spazz.github.meta.objects;

import java.util.ArrayList;
import java.util.HashMap;

public class dEvent implements Meta, Cloneable {

    private HashMap<String, ArrayList<String>> eventMeta;
    
    private ArrayList<String> events;
    private ArrayList<String> triggers;
    private ArrayList<String> context = null;
    private ArrayList<String> determine = null;
    private String plugin = null;
    
    public dEvent(HashMap<String, ArrayList<String>> eventMeta) {
        this.eventMeta = eventMeta;
        this.events = eventMeta.get("events");
        this.triggers = eventMeta.get("triggers");
        if (eventMeta.containsKey("context"))
            this.context = eventMeta.get("context");
        if (eventMeta.containsKey("determine"))
            this.determine = eventMeta.get("determine");
        if (eventMeta.containsKey("plugin"))
            this.plugin = eventMeta.get("plugin").get(0);
    }
    
    public String getEventBase() {
        return events.get(0);
    }
    
    public String getAliases() {
        String ret = "";
        for (int x = 1; x < events.size(); x++) {
            ret += events.get(x) + ", ";
        }
        return ret.substring(0, ret.length()-2);
    }
    
    public ArrayList<String> getAliasesList() {
        ArrayList<String> ret = new ArrayList<String>();
        for (int x = 1; x < events.size(); x++) {
            ret.add(events.get(x));
        }
        return ret;
    }
    
    public ArrayList<String> getTriggers() {
        return triggers;
    }
    
    public ArrayList<String> getContext() {
        return context;
    }
    
    public ArrayList<String> getDetermine() {
        return determine;
    }
    
    public String getPlugin() {
        return plugin;
    }
    
    public boolean isDepenizen() {
        return (plugin != null);
    }
    
    public boolean hasContext() {
        return (context != null && !context.isEmpty());
    }
    
    public boolean hasDetermine() {
        return (determine != null && !determine.isEmpty());
    }
    
    public boolean hasAliases() {
        return (events.size() > 1);
    }
    
    public dEvent setNewBase(int position) {
        String origBase = events.get(0);
        events.set(0, events.get(position));
        events.set(position, origBase);
        return this;
    }

    @Override
    public String getObjectType() {
        return "dEvent";
    }

    @Override
    public HashMap<String, ArrayList<String>> getMeta() {
        return eventMeta;
    }
    
    @Override
    public dEvent clone() {
        return new dEvent(eventMeta);
    }
    
}

package net.jeebiss.spazz.github.meta.objects;

import java.util.ArrayList;
import java.util.HashMap;

public class TagManager {

    private ArrayList<dTag> tags;
    private HashMap<String, ArrayList<String>> subTags;
    
    public TagManager() {
        this.tags = new ArrayList<dTag>();
        this.subTags = new HashMap<String, ArrayList<String>>();
    }
    
    public void clear() {
        tags.clear();
    }
    
    public void add(dTag tag) {
        tags.add(tag);
        String start = getBaseAttr(tag.getAttribute());
        if (!subTags.containsKey(start))
            subTags.put(start, new ArrayList<String>());
        subTags.get(start).add(fulfill(tag.getAttribute()));
    }
    
    public String fulfill(String attribute) {
        if (attribute.contains(".")) {
            return attribute.substring(attribute.indexOf('.'), attribute.length());
        }
        return "";
    }
    
    public ArrayList<dTag> search(String input) {
        ArrayList<dTag> ret = new ArrayList<dTag>();
        boolean endDot = false;
        if (input.endsWith(".")) {
            input = input.replaceAll("\\.+$", "");
            endDot = true;
        }
        for (dTag tag : tags) {
            if (tag.getAttribute().toLowerCase().replaceAll("(<|>|\\[.*\\])", "")
                    .equals(input.replaceAll("(<|>|\\[.*\\])", ""))) {
                ret.clear();
                if (endDot) {
                    for (String subTag : subTags.get(getBaseAttr(tag.getAttribute()))) {
                        ret.add(tag.clone().setAttribute(tag.getAttribute() + subTag));
                    }
                }
                else {
                    ret.add(tag);
                }
                break;
            }
            if (tag.getAttribute().toLowerCase().replaceAll("(<|>|\\[.*\\])", "")
                    .contains(input.replaceAll("(<|>|\\[.*\\])", ""))) {
                ret.add(tag);
            }
        }
        return ret;
    }
    
    public String getBaseAttr(String attribute) {
        return attribute.replace("." + fulfill(attribute), "");
    }
    
    public String getBaseFor(String returns) {
        returns = returns.toLowerCase().replaceAll("\\(.*\\)", "");
        switch (returns) {
            case "dchunk":
                return "ch@chunk";
            case "dcolor":
                return "co@color";
            case "dcuboid":
                return "cu@cuboid";
            case "dentity":
                return "e@entity";
            case "dinventory":
                return "in@inventory";
            case "ditem":
                return "i@item";
            case "dlist":
                return "li@list";
            case "dlocation":
                return "l@location";
            case "dmaterial":
                return "m@material";
            case "dnpc":
                return "n@npc";
            case "dplayer":
                return "p@player";
            case "dscript":
                return "s@script";
            case "duration":
                return "d@duration";
            case "dworld":
                return "w@world";
            case "element":
                return "el@element";
            default:
                return returns.substring(1, 2) + "@" + returns.substring(2, returns.length());
        }
    }
    
    public int size() {
        return tags.size();
    }
    
}

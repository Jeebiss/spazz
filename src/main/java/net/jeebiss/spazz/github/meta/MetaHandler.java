package net.jeebiss.spazz.github.meta;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jeebiss.spazz.Utilities;
import net.jeebiss.spazz.github.Repository;
import net.jeebiss.spazz.github.meta.objects.Meta;
import net.jeebiss.spazz.github.meta.objects.TagManager;
import net.jeebiss.spazz.github.meta.objects.dCommand;
import net.jeebiss.spazz.github.meta.objects.dEvent;
import net.jeebiss.spazz.github.meta.objects.dTag;

public class MetaHandler {

    private static Pattern metaType = Pattern.compile("// <--\\[(.+)\\]");
    private static Pattern metaTag = Pattern.compile("// @(\\S+) (.+)");
    private static Pattern metaTagAlt = Pattern.compile("// @(.+)");
    
    private HashMap<Meta.Types, ArrayList<HashMap<String, ArrayList<String>>>> meta;
    private Repository repo;
    private String folder;
    private Matcher matcher;
    
    private ArrayList<dCommand> commandList = new ArrayList<dCommand>();
    private ArrayList<dEvent> eventList = new ArrayList<dEvent>();
    private ArrayList<dCommand> requirementList = new ArrayList<dCommand>();
    private TagManager tagManager = new TagManager();
    
    public MetaHandler(Repository repo) {
        this.meta = new HashMap<Meta.Types, ArrayList<HashMap<String, ArrayList<String>>>>();
        this.repo = repo;
        this.folder = "storage/zip/" + repo.getName();
        for (Meta.Types type : Meta.Types.values()) {
            meta.put(type, new ArrayList<HashMap<String, ArrayList<String>>>());
        }
    }
    
    public void reload() {
        extractMeta();
        loadMeta();
    }
    
    private void extractMeta() {
        Utilities.unzipFileFromURL("https://github.com/" + repo.getFullName() +"/archive/master.zip", folder);
    }
    
    private void loadMeta() {
        commandList.clear();
        eventList.clear();
        requirementList.clear();
        tagManager.clear();
        ArrayList<File> files = Utilities.findFilesByExtension(folder, ".java");
        for (File file : files) {
            int debugLine = 0;
            try {
                HashMap<String, ArrayList<String>> metaTags = null;
                boolean inMeta = false;
                String inTag = "";
                Meta.Types type = null;
                try (Scanner scanner = new Scanner(new FileInputStream(file))) {
                    while (scanner.hasNextLine()) {
                        String next = scanner.nextLine().trim();
                        debugLine++;
                        if (next.equals("//") || next.equals("")) {
                            continue;
                        }
                        else if (!inMeta) {
                            matcher = metaType.matcher(next);
                            if (matcher.matches() && Meta.Types.valueOf(matcher.group(1).toUpperCase()) != null) {
                                type = Meta.Types.valueOf(matcher.group(1).toUpperCase());
                                metaTags = new HashMap<String, ArrayList<String>>();
                                inMeta = true;
                            }
                            else if (matcher.matches()) {
                                // dUnknown code...
                            }
                            continue;
                        }
                        else if (next.startsWith("// @")) {
                            matcher = metaTag.matcher(next);
                            if (matcher.matches()) {
                                inTag = matcher.group(1).toLowerCase();
                                if (!metaTags.containsKey(inTag))
                                    metaTags.put(inTag, new ArrayList<String>());
                                metaTags.get(inTag).add(matcher.group(2));
                                continue;
                            }
                            matcher = metaTagAlt.matcher(next);
                            if (matcher.matches()) {
                                inTag = matcher.group(1).toLowerCase();
                                if (!metaTags.containsKey(inTag))
                                    metaTags.put(inTag, new ArrayList<String>());
                                continue;
                            }
                        }
                        else if (next.startsWith("// -->")) {
                            addMeta(type, metaTags);
                            metaTags = null;
                            inMeta = false;
                            inTag = "";
                            type = null;
                            continue;
                        }
                        else if (!inTag.isEmpty()) {
                            metaTags.get(inTag).add(next.substring(3));
                            continue;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }
    
    private void addMeta(Meta.Types type, HashMap<String, ArrayList<String>> metaTags) {
        switch (type) {

            case COMMAND:
                for (String name : metaTags.get("name").get(0).split(", ")) {
                    metaTags.get("name").set(0, name);
                    commandList.add(new dCommand(metaTags));
                }
                break;
                
            case EVENT:
                eventList.add(new dEvent(metaTags));
                break;
                
            case REQUIREMENT:
                for (String name : metaTags.get("name").get(0).split(", ")) {
                    metaTags.get("name").set(0, name);
                    requirementList.add(new dCommand(metaTags));
                }
                break;
            
            case TAG:
                tagManager.add(new dTag(metaTags));
                break;
    
        }
    }
    
    public ArrayList<dCommand> searchCommands(String input) {
        ArrayList<dCommand> ret = new ArrayList<dCommand>();
        for (dCommand command : commandList) {
            if (command.getNameLwr().equals(input.toLowerCase())) {
                ret.clear();
                ret.add(command);
                break;
            }
            else if (command.getNameLwr().contains(input.toLowerCase())) {
                ret.add(command);
                continue;
            }
        }
        return ret;
    }
    
    public ArrayList<dEvent> searchEvents(String input) {
        input = input.toLowerCase();
        ArrayList<dEvent> ret = new ArrayList<dEvent>();
        for (dEvent event : eventList) {
            if (event.getEventBase().toLowerCase().equals(input)) {
                ret.add(event);
                break;
            }
            else if (event.getEventBase().toLowerCase().contains(input)) {
                ret.add(event);
                continue;
            }
            if (event.hasAliases()) {
                ArrayList<String> aliases = event.getAliasesList();
                for (int x = 0; x < aliases.size(); x++) {
                    if (aliases.get(x).toLowerCase().equals(input)) {
                        ret.add(event.clone().setNewBase(x+1));
                        break;
                    }
                    else if (aliases.get(x).toLowerCase().contains(input)) {
                        ret.add(event.clone().setNewBase(x+1));
                        continue;
                    }
                }
            }
        }
        return ret;
    }
    
    @Deprecated
    public ArrayList<dCommand> searchRequirements(String input) {
        ArrayList<dCommand> ret = new ArrayList<dCommand>();
        for (dCommand requirement : requirementList) {
            if (requirement.getNameLwr().equals(input.toLowerCase())) {
                ret.clear();
                ret.add(requirement);
                break;
            }
            else if (requirement.getNameLwr().contains(input.toLowerCase())) {
                ret.add(requirement);
                continue;
            }
        }
        return ret;
    }
    
    public ArrayList<dTag> searchTags(String input) {
        input = input.toLowerCase();
        return tagManager.search(input);
    }
    
    public int commandCount() {
        return commandList.size();
    }
    
    public int requirementCount() {
        return requirementList.size();
    }
    
    public int tagCount() {
        return tagManager.size();
    }
    
    public int eventCount() {
        return eventList.size();
    }

}

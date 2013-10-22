package net.jeebiss.spazz;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.lang.System;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import net.jeebiss.spazz.github.Comment;
import net.jeebiss.spazz.github.CommentEvent;
import net.jeebiss.spazz.github.Commit;
import net.jeebiss.spazz.github.CommitComment;
import net.jeebiss.spazz.github.CommitEvent;
import net.jeebiss.spazz.github.GitHub;
import net.jeebiss.spazz.github.Issue;
import net.jeebiss.spazz.github.IssueComment;
import net.jeebiss.spazz.github.IssueEvent;
import net.jeebiss.spazz.github.Repository;
import net.jeebiss.spazz.github.RepositoryManager;

import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.DisconnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.PingEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.events.QuitEvent;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

@SuppressWarnings("rawtypes")
public class Spazz extends ListenerAdapter {
	
    public static Spazz spazz = new Spazz();
	public static PircBotX bot = new PircBotX();

	public static List<dTag> dTags = new ArrayList<dTag>();
	public static List<String> dTagPrefixes = new ArrayList<String>();
	public static Map<String, ArrayList<dTag>> dTagByType = new HashMap<String, ArrayList<dTag>>();
	public static List<dCommand> dCommands = new ArrayList<dCommand>();
	public static List<dCommand> dRequirements = new ArrayList<dCommand>();
	public static List<dEvent> dEvents = new ArrayList<dEvent>();
	
	public static File usersFolder = null;
	
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS zzz");
	
	public static Map<String, dUser> dUsers = new HashMap<String, dUser>();
	
    String[] temp;
	static String chatColor = Colors.TEAL;
	static String optionalColor = Colors.DARK_GREEN;
	static String defaultColor = Colors.OLIVE;
	boolean charging=false;
	long chargeInitiateTime;
	long chargeFullTime = 30000;
	User charger;
	int botsnack = 0;
	ArrayList<User> feeders = new ArrayList<User>();
	ArrayList<User> help = new ArrayList<User>();
	String confirmingComment = null;
	int confirmingIssue = 0;
	Boolean confirmComment = false;
	String confirmIssueUser = null;
	public static boolean shuttingDown = false;
	
    private static void loadMeta() throws Exception {
		dTags.clear();
		dTagPrefixes.clear();
		dTagByType.clear();
		dCommands.clear();
		dRequirements.clear();
		dEvents.clear();
		String pages = Utilities.getStringFromUrls(Arrays.asList("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dList.java",
		        "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/tags/core/UtilTags.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/tags/core/TextTags.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/tags/core/ProcedureScriptTag.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/tags/core/SpecialCharacterTags.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/tags/core/ContextTags.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/tags/core/AnchorTags.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/Duration.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dCuboid.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dChunk.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/properties/EntityInfected.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/properties/EntityAge.java",
				"https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dWorld.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dColor.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dItem.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dPlayer.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dNPC.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dEntity.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dScript.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dMaterial.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dInventory.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/Element.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dLocation.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/scripts/commands/core/YamlCommand.java",
		        "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/scripts/commands/CommandRegistry.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/scripts/requirements/RequirementRegistry.java",
                "https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/scripts/containers/core/WorldScriptHelper.java"));
		String[] split = pages.replace("\r", "").split("\n");
        boolean indesc = false;
        boolean inusage = false;
        boolean inaltdesc = false;
        boolean instable = false;
        boolean inexample = false;
        boolean indeprecated = false;
        boolean flip = false;
        String name = null;
        String stable = null;
        String returns = null;
        String nname = "";
        String usage = null;
        String prefix = null;
        String deprecated = null;
        List<String> descs = new ArrayList<String>();
        List<String> example = new ArrayList<String>();
        List<String> altUsage = new ArrayList<String>();
        List<String> altDesc = new ArrayList<String>();
        String type = null;
        for (int i = 0; i < split.length; i++) {
            String curline = split[i].trim();
            if (curline.startsWith("// <--")) {
                type = curline.substring(6+1, curline.length()-1).toLowerCase();
                descs.clear();
                example.clear();
                altUsage.clear();
                altDesc.clear();
                indeprecated = false;
            }
            if (curline.toLowerCase().startsWith("// @deprecated")) {
                if (curline.substring(14).length() > 0) {
                    deprecated = curline.substring(14).trim();
                }
                indeprecated = true;
            }
            if (type != null && type.equals("tag")) {
                if (curline.toLowerCase().startsWith("// @attribute")) {
                    indesc = false;
                    name = curline.substring(13).trim().substring(curline.substring(13).trim().indexOf('<')+1, curline.substring(13).trim().lastIndexOf('>'));
                    if (name.contains(".")) {
                        prefix = name.substring(0, name.indexOf('.'));
                        if (!dTagPrefixes.contains(prefix)) {
                            dTagPrefixes.add(prefix);
                        }
                    }
                    else {
                    }
                    for (int f = 0; f < name.length(); f++) {
                        if (name.charAt(f) == '[')
                            flip = true;
                        if (!flip)
                            nname += String.valueOf(name.charAt(f));
                        if (name.charAt(f) == ']')
                            flip = false;
                    }
                    if (nname.contains("@")) {
                        nname = nname.substring(nname.indexOf('@') + 1);
                    }
                }
                else if (curline.toLowerCase().startsWith("// @returns")) {
                    indesc = false;
                    returns = curline.substring(11).trim();
                }
                else if (curline.toLowerCase().startsWith("// @description")) {
                    if (curline.substring(15).length() > 0) {
                        descs.add(curline.substring(15).trim());
                    }
                    indesc = true;
                }
                else if (curline.startsWith("// -->")) {
                    if (descs.size() < 1 || returns == null || name == null) {
                        if (debugMode) System.out.println("Bad tag meta at " + String.valueOf(i) + "!");
                        continue;
                    }
                    String tagdesc = "";
                    for (String desc : descs) {
                        tagdesc += desc + "\n";
                    }
                    if (debugMode) {
                        if (debugMode) System.out.println("Adding tag " + name + " AKA " + nname + "...");
                        if (debugMode) System.out.println("...with description: " + tagdesc);
                    }
                    dTag tag = new dTag(name, tagdesc, nname, returns, deprecated);
                    dTags.add(tag);
                    if (!dTagByType.containsKey((prefix.contains("@") ? prefix.split("@")[1] : prefix))) {
                        dTagByType.put((prefix.contains("@") ? prefix.split("@")[1] : prefix), new ArrayList<dTag>());
                        if (debugMode) System.out.println("Adding tag type: " + (prefix.contains("@") ? prefix.split("@")[1] : prefix));
                    }
                    dTagByType.get((prefix.contains("@") ? prefix.split("@")[1] : prefix)).add(tag);
                    indesc = false;
                    type = null;
                    name = null;
                    returns = null;
                    nname = "";
                    deprecated = null;
                }
                else if (indesc && curline.startsWith("// ")) {
                    if (curline.substring(3).startsWith("@")) {
                        indesc = false;
                        continue;
                    }
                    descs.add(curline.substring(3));
                }
            }
            else if (type != null && (type.equals("command") || type.equals("requirement"))) {
                if (curline.startsWith("// @")) {
                    inusage = false;
                    indesc = false;
                    inaltdesc = false;
                    instable = false;
                    inexample = false;
                }
                if (curline.toLowerCase().startsWith("// @name")) {
                    name = curline.substring(8).trim();
                }
                else if (curline.toLowerCase().startsWith("// @usage")) {
                    if (usage != null) {
                        inusage = true;
                    }
                    if (curline.substring(9).length() > 0) {
                        if (usage == null) {
                            usage = curline.substring(9).trim();
                        }
                        else {
                            altUsage.add(curline.substring(9).trim());
                        }
                    }
                }
                else if (curline.toLowerCase().startsWith("// @description")) {
                    if (curline.substring(15).length() > 0) {
                        altDesc.add(curline.substring(15).trim());
                    }
                    inaltdesc = true;
                }
                else if (curline.toLowerCase().startsWith("// @stable")) {
                    if (curline.substring(10).length() > 0) {
                        stable = curline.substring(10).trim();
                    }
                    else {
                        instable = true;
                    }
                }
                else if (curline.toLowerCase().startsWith("// @example")) {
                    if (curline.substring(11).length() > 0) {
                        example.add(curline.substring(11).trim());
                    }
                    inexample = true;
                }
                else if (curline.toLowerCase().startsWith("// @short")) {
                    if (curline.substring(9).length() > 0) {
                        descs.add(curline.substring(9).trim());
                    }
                    indesc = true;
                }
                else if (curline.toLowerCase().startsWith("// @required")) {
                }
                else if (curline.startsWith("// -->")) {
                    if (descs.size() < 1 || usage == null || name == null) {
                        if (debugMode) System.out.println("Bad " + type + " meta at " + String.valueOf(i) + "!");
                        continue;
                    }
                    String fulldesc = "";
                    for (String desc : descs) {
                        fulldesc += desc + "\n";
                    }
                    String alt = "";
                    for (String u : altUsage) {
                        alt += u + "\n";
                    }
                    String descAlt = "";
                    for (String d : altDesc) {
                        descAlt += d + "\n";
                    }
                    String fullExample = "";
                    for (String e : example) {
                        fullExample += e + "\n";
                    }
                    if (debugMode) {
                        if (debugMode) System.out.println("Adding " + type + " " + name + ": " + usage + "...");
                        if (debugMode) System.out.println("...with description: " + fulldesc);
                        if (debugMode) System.out.println();
                    }
                    String realname = name.split(", ")[0];
                    for (String alias : name.split(", ")) {
                        if (type.startsWith("c")) {
                            dCommands.add(new dCommand(Utilities.capitalize(alias), Utilities.capitalize(realname),
                                    fulldesc, usage.replaceFirst(realname.toLowerCase(), name.toLowerCase()),
                                    alt, descAlt, stable, fullExample, deprecated));
                        }
                        else if (type.startsWith("r")) {
                            dRequirements.add(new dCommand(Utilities.capitalize(alias), Utilities.capitalize(realname),
                                    fulldesc, usage.replaceFirst(realname.toLowerCase(), name.toLowerCase()),
                                    alt, descAlt, stable, fullExample, deprecated));
                        }
                    }
                    indesc = false;
                    inusage = false;
                    inaltdesc = false;
                    instable = false;
                    inexample = false;
                    type = null;
                    name = null;
                    usage = null;
                    deprecated = null;
                }
                else if (curline.startsWith("// ")) {
                    if (indesc) {
                        descs.add(curline.substring(3));
                    }
                    if (inusage) {
                        altUsage.add(curline.substring(3));
                    }
                    if (inaltdesc) {
                        altDesc.add(curline.substring(3));
                    }
                    if (instable) {
                        stable = curline.substring(3);
                    }
                    if (inexample) {
                        example.add(curline.substring(3));
                    }
                }
                else if (curline.startsWith("//")) {
                    indesc = false;
                    inusage = false;
                    inaltdesc = false;
                    instable = false;
                    inexample = false;
                }
            }
            else if (type != null && type.equals("event")) {
                if (curline.startsWith("// @")) {
                    indesc = false;
                    inusage = false;
                    inaltdesc = false;
                    inexample = false;
                }
                if (curline.toLowerCase().startsWith("// @events")) {
                    if (curline.substring(10).length() > 0) {
                        altUsage.add(curline.substring(10).trim());
                    }
                    inusage = true;
                }
                else if (curline.toLowerCase().startsWith("// @triggers")) {
                    if (curline.substring(12).length() > 0) {
                        descs.add(curline.substring(12).trim());
                    }
                    indesc = true;
                }
                else if (curline.toLowerCase().startsWith("// @context")) {
                    if (curline.substring(11).length() > 0) {
                        altDesc.add(curline.substring(11).trim());
                    }
                    inaltdesc = true;
                }
                else if (curline.toLowerCase().startsWith("// @determine")) {
                    if (curline.substring(13).length() > 0) {
                        example.add(curline.substring(13).trim());
                    }
                    inexample = true;
                }
                else if (curline.startsWith("// -->")) {
                    if (descs.isEmpty() || altUsage.isEmpty()) {
                        if (debugMode) System.out.println("Bad " + type + " meta at " + String.valueOf(i) + "!");
                        continue;
                    }
                    String triggers = "";
                    for (String desc : descs) {
                        triggers += desc + "\n";
                    }
                    String context = "";
                    for (String desc : altDesc) {
                        context += desc + "\n";
                    }
                    String determine = "";
                    for (String e : example) {
                        determine += e + "\n";
                    }
                    String events = "";
                    for (String event : altUsage) {
                        events += event + ", ";
                    }
                    if (debugMode) {
                        if (debugMode) System.out.println("Adding event " + altUsage.get(0) + ": " + triggers + "...");
                        if (debugMode) System.out.println("...with aliases: " + events);
                        if (debugMode) System.out.println();
                    }
                    dEvents.add(new dEvent(events.substring(0,events.length()-2), triggers, context, determine, deprecated));
                    indesc = false;
                    inusage = false;
                    inaltdesc = false;
                    inexample = false;
                    type = null;
                    deprecated = null;
                }
                else if (curline.startsWith("// ")) {
                    if (indesc) {
                        descs.add(curline.substring(3));
                    }
                    if (inusage) {
                        altUsage.add(curline.substring(3));
                    }
                    if (inaltdesc) {
                        altDesc.add(curline.substring(3));
                    }
                    if (instable) {
                        stable = curline.substring(3);
                    }
                    if (inexample) {
                        example.add(curline.substring(3));
                    }
                }
                else if (curline.startsWith("//")) {
                    indesc = false;
                    inusage = false;
                    inaltdesc = false;
                    inexample = false;
                }
            }
            if (curline.startsWith("// ")) {
                if (indeprecated) {
                    if (!curline.substring(3).startsWith("@"))
                        deprecated = curline.substring(3);
                    else
                        indeprecated = false;
                }
            }
        }
        Collections.sort(dTags, new Comparator<dTag>() {
            public int compare(dTag tag1, dTag tag2) {
                return tag1.getName().compareTo(tag2.getName());
            }
        });
        Collections.sort(dEvents, new Comparator<dEvent>() {
            public int compare(dEvent e1, dEvent e2) {
                return e1.getEvent().compareTo(e2.getEvent());
            }
        });
        Collections.sort(dCommands, new Comparator<dCommand>() {
            public int compare(dCommand c1, dCommand c2) {
                return c1.getName().compareTo(c2.getName());
            }
        });
        Collections.sort(dRequirements, new Comparator<dCommand>() {
            public int compare(dCommand r1, dCommand r2) {
                return r1.getName().compareTo(r2.getName());
            }
        });
    }
    
    public static boolean debugMode = false;
    public static String chatChannel = "#denizen-dev";
    public static GitHub github = null;
    public static RepositoryManager repoManager = null;
    
	public static void main(String[] args) {
        
	    System.out.println("Starting Spazzmatic...");
        
	    try {
	        usersFolder = new File(System.getProperty("user.dir") + "/users");
	        
	        LinkedHashMap map = null;
	        Yaml yaml = new Yaml();
	        InputStream is = new File(usersFolder + "/spazzmatic.yml").toURI().toURL().openStream();
	        map = (LinkedHashMap) yaml.load(is);
	        if (map.get("password") instanceof String) {
	            System.setProperty("spazz.password", (String) map.get("password"));
	        }
	        if (map.get("bitly") instanceof String) {
	            System.setProperty("spazz.bitly", (String) map.get("bitly"));
	        }
	    }
	    catch (Exception e) {
	        if (!usersFolder.isDirectory() && !usersFolder.mkdir()) {
	            System.out.println("Could not load users folder. Password for Spazzmatic not found. Cancelling startup...");
	            return;
	        }
        }
	    
	    github = GitHub.connect("spazzmatic", System.getProperty("spazz.password"));
	    repoManager = new RepositoryManager(github);
	    
	    repoManager.addRepository("aufdemrand", "Denizen", 20, true);
	    repoManager.addRepository("Morphan1", "Depenizen", 30, true);
	    repoManager.addRepository("CitizensDev", "Citizens2", 20, false);
        repoManager.addRepository("CitizensDev", "CitizensAPI", 30, false);
	    repoManager.addRepository("Jeebiss", "spazz", 60, true);
	    repoManager.addRepository("jrbudda", "Sentry", 100, true);
        
        try {
            reloadSites(debugMode);
        } catch (Exception e) {
            System.out.println("Failed to load resources. CMD, REQ, TAG, and EVENT commands may not function correctly.");
        }
        
		bot.getListenerManager().addListener(spazz);
		/*
		 * Connect to #denizen-dev on start up
		 */
        bot.setVersion("Spazzmatic v0.3 [Morphan1]");
        bot.setAutoReconnect(true);
        bot.setName("spazzmatic");
        bot.setLogin("spazz");
        bot.setVerbose(debugMode);
        bot.setAutoNickChange(true);
        bot.setAutoReconnect(true);
        bot.identify(System.getProperty("spazz.password"));
        
        try {
            bot.connect("irc.esper.net");
        } catch (Exception e) {
            System.out.println("Failed to connect to EsperNet. Check your internet connection and try again.");
            return;
        }
        bot.setMessageDelay(0);
        bot.joinChannel("#denizen-dev");
        bot.joinChannel("#denizen-devs");
        
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                for (Channel chnl : bot.getChannels()) {
                    for (User usr : chnl.getUsers()) {
                        String nick = usr.getNick().toLowerCase();
                        if (dUsers.containsKey(nick) || bot.getName().equalsIgnoreCase(nick) || nick.length() == 0) continue;
                        dUsers.put(nick, new dUser(usr.getNick()));
                    }
                }
                for (String usr : findUserFiles()) {
                    if (dUsers.containsKey(usr)) continue;
                    dUsers.put(usr, new dUser(usr));
                }
                
            }
          }, 3000);
        
        System.out.println("Successfully loaded Spazzmatic. You may now begin using console commands.");
        System.out.println();
        System.out.println("Available commands:");
        System.out.println("/me <action>             Performs an action in the current chat channel.");
        System.out.println("/msg <user> <msg>        Sends a private message to a user.");
        System.out.println("/plain <msg>             Sends a non-colored message to the current chat channel.");
        System.out.println("/join <channel>          Joins a channel.");
        System.out.println("/leave <channel> <msg>   Leaves a channel with an optional message.");
        System.out.println("/disconnect              Saves user info and disconnects from the server.");
        System.out.println("/channel <channel>       Set the chat channel.");
        System.out.println("<msg>                    Sends a message to the current chat channel.");
        System.out.println();
    	
    	Scanner scanner = new Scanner(System.in);
    	String rawInput = "";
    	String inputCommand = "";
    	String commandArgs = "";
    	String channel = "";
    	
    	while (!shuttingDown) {
        	
    	    rawInput = scanner.nextLine();
    	    String[] inputArgs = rawInput.split(" ");
    	    
    		if (rawInput.startsWith("/")) {
    		    inputCommand = inputArgs[0].substring(1).toLowerCase();
                if (inputArgs.length > 1) {
                    channel = inputArgs[1];
                    if (inputArgs.length > 2) {
                        commandArgs = rawInput.substring(inputCommand.length() + inputArgs[1].length() + 3);
                    }
                }
    		}
    		
    		switch (inputCommand) {
    		
    		    case "me":
    		        if (bot.channelExists(chatChannel))
    		            bot.sendAction(chatChannel, chatColor + commandArgs);
    		        else
    		            System.out.println("Not connected to channel \"" + chatChannel + "\".");
    		        break;
    		        
    		    case "msg":
    		        if (dUsers.containsKey(channel.toLowerCase()))
    		            bot.sendMessage(channel, chatColor + commandArgs);
    		        else if (bot.channelExists(channel))
    		            System.out.println("You can't use /msg for channels! Instead, use \"<channel> <message>\"!");
    		        else
                        System.out.println("That user doesn't exist \"" + channel + "\".");
    		        break;
    		        
    		    case "plain":
                    if (bot.channelExists(chatChannel))
                        bot.sendMessage(chatChannel, commandArgs);
                    else
                        System.out.println("Not connected to channel \"" + chatChannel + "\".");
    		        break;
    		        
    		    case "channel":
    		        if (bot.channelExists(channel))
    		            chatChannel = channel;
                    else
                        System.out.println("Not connected to channel \"" + chatChannel + "\".");
    		        break;
    		        
    		    case "join":
    		        if (bot.channelExists(channel))
    		            System.out.println("Already in channel \"" + channel + "\".");
    		        else
    		            bot.joinChannel(channel);
    		        break;
    		        
    		    case "leave":
    		        if (bot.channelExists(channel))
    		            bot.partChannel(bot.getChannel(channel), commandArgs);
    		        else
    		            System.out.println("Not connected to channel \"" + channel + "\".");
    		        break;
    		        
    		    case "disconnect":
    		        repoManager.shutdown();
                    bot.disconnect();
    		        System.out.println();
    		        System.out.println();
    		        System.out.println("Disconnected.");
    		        shuttingDown = true;
    		        break;
    		        
    		    default:
    		        if (bot.channelExists(chatChannel))
    		            bot.sendMessage(chatChannel, chatColor + commandArgs);
    		        else
                        System.out.println("Not connected to channel \"" + chatChannel + "\".");
    		        break;
    		
    		}
            rawInput = "";
            inputCommand = "";
            commandArgs = "";
            channel = "";
    		
    	}
    	
    	scanner.close();
    }
    
    public static void sendToAllChannels(String message) {
        for (Channel chnl : bot.getChannels())
            bot.sendMessage(chnl, message);
    }
	
	@Override
	public void onJoin(JoinEvent event) {

	    User usr = event.getUser();
	    
        if (!dUsers.containsKey(usr.getNick().toLowerCase()))
            dUsers.put(usr.getNick().toLowerCase(), new dUser(usr.getNick()));
	    
		bot.sendNotice(event.getUser(), chatColor + "Welcome to " + Colors.BOLD + event.getChannel().getName() + Colors.NORMAL + chatColor + ", home of the Denizen project. If you'd like help with anything, type " + Colors.BOLD + Colors.BLUE + ".help");
		bot.sendNotice(event.getUser(), chatColor + "If you are using Depenizen and would like help with that as well, let me know by typing " + Colors.BOLD + Colors.BLUE + ".depenizen");
	
	}
	
	@Override
	public void onQuit(QuitEvent event) {
	    // ...
	}
	
	@Override
    	public void onPart(PartEvent event) {
	    // ...
	}
	
	@Override
	public void onDisconnect(DisconnectEvent event) {
        for (dUser usr : dUsers.values()) {
            try {
                usr.saveAll();
            } catch (Exception e) {
                if (debugMode) e.printStackTrace();
                else
                    System.out.println("An error has occured while using saving user " + usr.getNick() + " on disconnect... Turn on debug for more information.");
            }
        }
	}
	
	@Override
	public void onPing(PingEvent event) {
		// CTCP PING stuff... Not incredibly useful
	}
	
	@Override
	public void onNickChange(NickChangeEvent event) {
		// ...
	}
	
	@Override
	public void onAction(ActionEvent event) {
	    dUsers.get(event.getUser().getNick()).setLastSeen("performing an action in " + event.getChannel().getName() 
	            + chatColor + ": " + event.getAction());
	}
	
    @Override
	public void onPrivateMessage(PrivateMessageEvent event) {
	    bot.sendMessage(event.getUser(), chatColor + "The PM system has been temporarily disabled. Please use "
	            + defaultColor + "#denizen-dev" + chatColor + " or " + defaultColor + "#denizen-devs" + chatColor + " to talk to me.");
	}
    
    public static void onIssue(IssueEvent event) {
        
        Issue issue = event.getIssue();
        Repository repo = issue.getRepo();
        
        if (!issue.isPullRequest()) {
            sendToAllChannels(chatColor + "[" + repo.getName() + "] Issue " + event.getState().name().toLowerCase()
                    + ": [" + defaultColor + issue.getNumber() + chatColor + "] \"" + defaultColor + issue.getTitle()
                    + chatColor + "\" by " + optionalColor + issue.getUser().getLogin() + chatColor + " - " + issue.getShortUrl());
        }
        
        else {
            if (event.getState() == IssueEvent.State.OPENED) {
                sendToAllChannels(chatColor + "[" + repo.getName() + "] Pull request " + (event.getState().name().equals("CLOSED") ?
                        "denied" : event.getState().name().toLowerCase()) + ": [" + defaultColor + issue.getNumber()
                        + chatColor + "] \"" + defaultColor + issue.getTitle() + chatColor + "\" by " + optionalColor
                        + issue.getUser().getLogin() + chatColor + " - " + issue.getShortUrl());
            }
        }
        
    }
    
    public static void onCommit(CommitEvent event) {
        
        ArrayList<Commit> commits = event.getCommits();
        Repository repo = event.getRepo();
        
        String users = "";
        for (String user : event.getUsers()) {
            users += user + ", ";
        }
        
        sendToAllChannels(chatColor + "[" + optionalColor + repo.getName() + chatColor + "] " 
                + defaultColor + users.substring(0, users.length()-2) + chatColor + " pushed " + commits.size() 
                + " commit" + (commits.size() == 1 ? "" : "s") + " to master branch");
        
        for (Commit commit : commits) {
            String message = commit.getMessage();
            String shortenedUrl = commit.getShortUrl();
            if (message.contains("\n")) {
                message = message.substring(0, message.indexOf('\n'));
            }
            
            sendToAllChannels(defaultColor + "  " + commit.getAuthor() + chatColor + ": " + message + " - " + shortenedUrl);
        }
        
    }
    
    public static void onComment(CommentEvent event) {
        
        Comment comment = event.getComment();
        Repository repo = event.getRepo();
        
        if (comment instanceof IssueComment) {
            IssueComment icomment = (IssueComment) comment;
            Issue issue = icomment.getIssue();
            String url = comment.getShortUrl();
            sendToAllChannels(chatColor + "[" + optionalColor + repo.getName() + chatColor + "] " + defaultColor 
                    + comment.getUser().getLogin() + chatColor + " commented on " 
                    + issue.getState() + " issue: [" 
                    + defaultColor + issue.getNumber() + chatColor + "] " + defaultColor 
                    + issue.getTitle() + chatColor + " by " + defaultColor 
                    + issue.getUser().getLogin() + chatColor 
                    + (url != null ? " - " + url : ""));
        }
        else if (comment instanceof CommitComment) {
            CommitComment ccomment = (CommitComment) comment;
            Commit commit = ccomment.getCommit();
            String url = comment.getShortUrl();
            sendToAllChannels(chatColor + "[" + optionalColor + repo.getName() + chatColor + "] " + defaultColor 
                    + comment.getUser().getLogin() + chatColor + " commented on commit: " + defaultColor 
                    + commit.getMessage() + chatColor + " by " + defaultColor 
                    + commit.getAuthor() + chatColor + (url != null ? " - " + url : ""));
        }
        
    }
	
	@Override
    public void onMessage(MessageEvent event) {
	    
        final Channel chnl = event.getChannel();
		
		User usr = event.getUser();
		
		dUser dusr = null;
		if (!dUsers.containsKey(usr.getNick().toLowerCase()))
		    dUsers.put(usr.getNick().toLowerCase(), new dUser(usr.getNick()));
		
		dusr = dUsers.get(usr.getNick().toLowerCase());
		
		String msg = event.getMessage();
		
        dusr.setLastSeen("Saying \"" + msg + chatColor + "\" in " + chnl.getName());
		
		String msgLwr = msg.toLowerCase();
		final String senderNick = event.getUser().getNick();
		String address = "";
		
		if (charging) {
			if(System.currentTimeMillis() > (chargeInitiateTime + chargeFullTime + chargeFullTime/2)) {
				bot.sendAction(chnl, "loses control of his beams, accidentally making pew pew noises towards "+charger.getNick()+"!");
				charging=false;
				chargeInitiateTime=0;
				charger=null;
				botsnack=0;
				feeders.clear();
			}
				
		}
		
		if (msg.endsWith("@@")) {
			String args[] = msg.split(" ");
			if (debugMode) System.out.println(args.length);
			for (int x=0; x<args.length; x++) {
				if (!args[x].contains("@@"))
					continue;
				else {
					address=args[x];
					address=address.substring(0,address.length()-2);
					address=address + ": ";
				}
			}
		}
		if (msg.equalsIgnoreCase(".hello")) {
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + "Hello World"); 
			return;
		} else if (msgLwr.startsWith(".kitty")) {
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Meow.");
			return;
		} else if (msgLwr.startsWith(".color")) {
			
			if(!hasOp(usr, chnl) && !hasVoice(usr, chnl)) {
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "I'm sorry, but you do not have clearance to alter my photon colorization beam.");
				return;
			}
			
			String[] args = msg.split(" ");
			if(args.length > 3) {
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "I cannot read minds... yet. Hit me up with a bot-friendly color.");
				return;
			}
			String tempColor = parseColor(args[1]);
			if(tempColor == null) {
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "I eat " + args[1] + " for breakfast. That's not a color.");
				return;
			}
			
			if(args.length == 3) {
				if(args[2].equalsIgnoreCase("default"))
					defaultColor=tempColor;
				else
					if(args[2].equalsIgnoreCase("optional"))
						optionalColor=tempColor;
					else
						if(args[2].equalsIgnoreCase("chat"))
							chatColor=tempColor;
			}
			else 
				chatColor=tempColor;
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor  + "Photon colorization beam reconfigured "+ "[] " + optionalColor + "() " + defaultColor + "{}");
			return;
		} else if (msgLwr.startsWith(".botsnack")) {
			String args[] = msg.split(" ");
			
			if (feeders.toString().contains(usr.toString())) {
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Thanks, but I can't have you controlling too much of my diet.");
				return;
			}
			
			if (args.length == 2) {
				if(chnl.getUsers().toString().contains(args[1])) 
					bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Gluttony mode activated. Beginning " + args[1] + " consumption sequence.");
				else {
					ArrayList<User> users = new ArrayList<User>(chnl.getUsers());
					Random rand = new Random();
					User random = users.get(rand.nextInt(users.size()));
					bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Oh no! " + args[1] + " not found, nomming "+random.getNick() + " instead.");
				}
				feeders.add(usr);
				botsnack++;
				return;
				
			} else {
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor +  "OM NOM NOM! I love botsnacks!");
				feeders.add(usr);
				botsnack++;
				return;
			}
		
		} else if (msgLwr.startsWith(".reload")) {
		    String[] args = msgLwr.split(" ");
		    if (dusr.getDepenizen() || (args.length > 1 && args[1].equals("debug")))
                try {
                    reloadSites(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            else
                try {
                    reloadSites(false);
                } catch (Exception e) {
                    System.out.println("An error has occured while reloading resources... Turn on debug for more information.");
                }
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Reloaded data. I now have " + defaultColor + dCommands.size() + chatColor + " commands, " + defaultColor + dRequirements.size() + chatColor + " requirements, " + defaultColor + dEvents.size() + chatColor + " world events, and " + defaultColor + dTags.size() + chatColor + " tags loaded.");
			return;
		} else if (msgLwr.startsWith(".repo")) {
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Check out scripts made by other users! - http://bit.ly/14o43eF");
		} else if (msgLwr.startsWith(".materials") || msgLwr.startsWith(".mats")) {
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Here is the list of all valid bukkit materials - http://bit.ly/X5smJK");
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "All Denizen 'item:' arguments will accept a bukkit material name. Additionally you can add the data value to the name. (i.e. SANDSTONE:1)");
			return;
		} else if (msgLwr.startsWith(".enchantments") || msgLwr.startsWith(".enchants")) {
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Here is the list of all valid bukkit enchantments - http://bit.ly/YQ25ud");
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "They do not follow the same naming conventions as they do in game, so be carefull.");
			return;
		} else if (msgLwr.startsWith(".anchors") || msgLwr.startsWith(".anchor")) {
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "As of 0.8, locations can be referenced from scripts by using anchors linked to NPCs.");
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Check out the documentation on the anchor commands in the handbook.");
			return;
		} else if (msgLwr.startsWith(".assignments") || msgLwr.startsWith(".assignment") || msgLwr.startsWith(".assign")) {
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "As of Denizen 0.8, the assignments.yml file is " + Colors.BOLD + "not " + Colors.NORMAL + chatColor + "necessary and the /trait command does "+Colors.BOLD + " not work.");
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Instead, create the assignment script alongside interact scripts and assign it with:");
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + Colors.BOLD + "- /npc assign --set 'assignment_script_name'");			
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Check out an example of this new script's implementation at " + Colors.BLUE + "http://bit.ly/YiQ0hs");
			return;
		} else if (msgLwr.startsWith(".help")) {
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Greetings. I am an interactive Denizen guide. I am a scripting guru. I am spazz.");
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "For help with script commands, type " + Colors.BOLD + ".cmd <command_name>");
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "For help with script requirements, type " + Colors.BOLD + ".req <requirement_name>");
            bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "For info on replaceable tags, type " + Colors.BOLD + ".tag <tag_name>");
            bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Finally, for help with world events, type " + Colors.BOLD + ".event <event_name>");
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "For everything else, ask in the channel or visit one of the links from .getstarted");
			return;
		} else if (msgLwr.startsWith(".paste") || msgLwr.startsWith(".pastie") || msgLwr.startsWith(".hastebin") || msgLwr.startsWith(".pastebin")) {
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Need help with a script issue or server error?");
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Help us help you by pasting your script " + Colors.BOLD + "and " + Colors.NORMAL + chatColor + "server log to " + Colors.BLUE + "http://hastebin.com");
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "From there, save the page and paste the link back in this channel.");
			return;
		} else if (msgLwr.startsWith(".update")) {
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Due to the nature of our project, Denizen is always built against the " + Colors.RED +  "development" + chatColor +  " builds of Craftbukkit and Citizens.");
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Most errors can be fixed by updating all 3. (NOTE: We build on Bukkit and therefore do not support Spigot issues!)");
            bot.sendMessage((chnl != null ? chnl.getName() : senderNick), Colors.BOLD + "Denizen" + Colors.NORMAL + Colors.BLUE +  "- http://bit.ly/1aaGB3T");
            
            if (dusr.getDepenizen()) 
                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), Colors.BOLD + "Depenizen" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/1aaGEfY");
            
            bot.sendMessage((chnl != null ? chnl.getName() : senderNick), Colors.BOLD + "Citizens" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/1aaGEN2");
            bot.sendMessage((chnl != null ? chnl.getName() : senderNick), Colors.BOLD + "Craftbukkit" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/A5I50a");
			return;
		} else if (msgLwr.startsWith(".newconfig") || msgLwr.startsWith(".nc")) {
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor +  "If you are having issues with triggers not firing, you may be using the old config file.");
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor +  "You can easily generate a new one by deleteing your current config.yml file in the Denizen folder");
			return;
		} else if (msgLwr.startsWith(".wiki")) {
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor +  "The Denizen wiki is currently getting a makeover. This means that it doesn't currently have a lot of things.");
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor +  "Feel free to look at it anyway, though! http://bit.ly/14o3kdq");
			return;
		} else if (msgLwr.startsWith(".tags")) {
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor +  "Here's every replaceable tag in Denizen! - http://bit.ly/164DlSE");
		} else if (msgLwr.startsWith(".tag ")) {
			String arg = msgLwr.split(" ")[1].replaceAll("(\\[.+\\]|\\[|\\])", "");
			List<dTag> found = null;
			if (arg.contains(".")) {
			    found = FindTags(arg.endsWith("."), arg.split("\\."));
			}
			else {
			    found = FindTags(false, arg);
			}
			if (found.size() > 1) {
                String list = "";
                int size = found.size();
                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "I found " + defaultColor + (size == 50 ? "50+" : size)
                        + chatColor + " matches...");
                for (int x = 0; x < size; x++) {
                    if ((list + found.get(x).getName()).length() + 2 < 400) {
                        list += found.get(x).getName() + ", ";
                        if (x != 0 && x%10 == 0 && x+1 != size) {
                            if (x > 10) {
                                bot.sendNotice((address == "" ? senderNick : address.substring(0, address.length()-2)), optionalColor + list);
                            }
                            else {
                                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), optionalColor 
                                        + list.substring(0, (x == 10 ? list.length()-2 : list.length()))
                                        + (x == 10 ? "..." : ""));
                            }
                            list = "";
                        }
                    }
                    else {
                        x--;
                        if (x > 10) {
                            bot.sendNotice((address == "" ? senderNick : address.substring(0, address.length()-2)), optionalColor + list);
                        }
                        else {
                            bot.sendMessage((chnl != null ? chnl.getName() : senderNick), optionalColor 
                                    + list.substring(0, (x == 10 ? list.length()-2 : list.length()))
                                    + (x == 10 ? "..." : ""));
                        }
                        list = "";
                    }
                }
                if (size > 10) {
                    bot.sendNotice((address == "" ? senderNick : address.substring(0, address.length()-2)),
                            optionalColor + list.substring(0, list.length()-2) + ".");
                }
                else {
                    bot.sendMessage((chnl != null ? chnl.getName() : senderNick), optionalColor + list.substring(0, list.length()-2) + ".");
                }
                return;
            }
            else if (found.size() == 1) {
                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Tag found: " + optionalColor + found.get(0).getName() + chatColor 
                        + ", which is a " + optionalColor + found.get(0).getReturn());
                if (found.get(0).getDeprecated() != null) {
                    bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "  This tag is " + defaultColor + "deprecated" + chatColor + ": " + found.get(0).getDeprecated());
                }
                for (String line : found.get(0).getDesc().split("\n")) {
                    bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "  " + line);
                }
                return;
            }
            else {
                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Tag \"" + arg + "\" not found.");
                return;
            }
		}
		else if (msgLwr.startsWith(".effects") || msgLwr.startsWith(".potions")) {
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "A list of Bukkit potion effects is available here " + Colors.BOLD + "- http://bit.ly/13xyXur");
		}
		else if (msgLwr.startsWith(".debug")) {
		    debugMode = !debugMode;
		    bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Debug mode set to " + defaultColor + debugMode + chatColor + ".");
		    bot.setVerbose(debugMode);
		}
		else if (msgLwr.startsWith(".tutorials")) {
		    bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Here's a list of video tutorials on how to use Denizen (Thanks " + optionalColor + "Jeebiss" + chatColor + "!)");
		    bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "1) " + defaultColor + "Hello World" + chatColor + " - http://bit.ly/1dgwyOn");
		    bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "2) " + defaultColor + "Questing 101" + chatColor + " - http://bit.ly/13RT8JY");
		}
		else if (msgLwr.startsWith(".shorten")) {
		    String[] args = msg.split(" ");
		    if (args.length > 1) {
		        bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + args[1] + " -> " + Utilities.getShortUrl(args[1]));
		    }
		}
		
		else if (msgLwr.startsWith(".msg") || msgLwr.startsWith(".message")) {
		    String[] args = msg.split(" ");
		    if (args.length < 2 || args[2].length() < 1) {
                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Check your argument count. Command format: .msg <user> <message>");
                return;
            }
		    msg = msg.replaceFirst(args[0] + " " + args[1] + " ", "");
		    if (!dUsers.containsKey(args[1].toLowerCase()))
		        dUsers.put(args[1].toLowerCase(), new dUser(args[1]));
            dUsers.get(args[1].toLowerCase()).addMessage(new Message(senderNick, Calendar.getInstance().getTime(), msg));
		    bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Message sent to: " + defaultColor + args[1] + chatColor + ".");
		    return;
		}
		
		else if (msgLwr.startsWith(".yaml") || msgLwr.startsWith(".yml")) {
			
			String[] args = msg.split(" ");
			if (args.length < 2) {
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Check your argument count. Command format: .yml <link>");
				return;
			}
			String rawYaml = null;
			try {
                String url[] = args[1].split("/");
			    if (args[1].contains("hastebin")) {
			        rawYaml = Utilities.getStringFromUrl("http://hastebin.com/raw/" + url[3]);
			    } else if (args[1].contains("pastebin")) {
			        rawYaml = Utilities.getStringFromUrl("http://pastebin.com/raw.php?i=" + url[3]);
			    } else if (args[1].contains("pastie")) {
			        rawYaml = Utilities.getStringFromUrl("http://pastie.org/pastes/" + url[3] + "/download");		
			    } else if (args[1].contains("ult-gaming")) {
			        rawYaml = Utilities.getStringFromUrl("http://paste.ult-gaming.com/" + url[3] + "?raw");
			    } else if (args[1].contains("citizensnpcs")) {
			        rawYaml = Utilities.getStringFromUrl("http://scripts.citizensnpcs.co/raw/" + url[4]);
			    } else {
			        bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + Colors.RED + "I can't get your script from that website :(");
			    }
			}
			catch (Exception e) {
                if (debugMode) e.printStackTrace();
                else
                    System.out.println("An error has occured while getting script from website... Turn on debug for more information.");
                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Invalid website format!");
                return;
			}
			
			Yaml yaml = new Yaml();
			try {
				yaml.load(rawYaml);
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Your YAML is valid.");
			} catch (YAMLException e) {
				String fullStack =  getCustomStackTrace(e);
				String[] stackList = fullStack.split("\\n");
				int x = 0;
				while (!stackList[x].contains("org.yaml")) {
					bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + stackList[x]);
					x++;
				}
			}
		} else if (msgLwr.startsWith(".reqs") || msgLwr.startsWith(".requirements")
		        || msgLwr.startsWith(".cmds") || msgLwr.startsWith(".commands")) {
		    String type = null;
            if (msgLwr.startsWith(".r")) {
                type = "req";
            }
            else if (msgLwr.startsWith(".c")) {
                type = "cmd";
            }
            List<dCommand> found = FindType(type, "");
            String list = "";
            bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "I found " + defaultColor + found.size() + chatColor + " matches...");
            for (int x = 0; x < found.size();  x++) {
                list += found.get(x).getName() + ", ";
                if (x != 0 && x%40 == 0 && x+1 != x) {
                    bot.sendMessage((chnl != null ? chnl.getName() : senderNick), optionalColor + list);
                    list = "";
                }
            }
            list = list.substring(0, list.length()-2) + ".";
            bot.sendMessage((chnl != null ? chnl.getName() : senderNick), optionalColor + list);
		} else if (msgLwr.startsWith(".req") || msgLwr.startsWith(".requirement") 
		        || msgLwr.startsWith(".cmd") || msgLwr.startsWith(".command")) {
		    String shortened = null;
		    String full = null;
		    String fullCap = null;
		    if (msgLwr.startsWith(".r")) {
		        shortened = "req";
		        full = "requirement";
		        fullCap = "Requirement";
		    }
		    else if (msgLwr.startsWith(".c")) {
                shortened = "cmd";
                full = "command";
                fullCap = "Command";
            }
			String [] args = msg.split(" ");
			if (args.length < 2) {
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Check your argument count. Command format: ." + shortened +" [<" + full + ">] (stability) (usage) (description)");
				return;
			}
            List<String> argList = new ArrayList<String>();
            if (args.length > 2) {
                for (int i = 2; i < args.length; i++) {
                    argList.add(args[i].toLowerCase().substring(0, 1));
                }
            }
			String arg = args[1].toLowerCase();
            String extra = "";
            if (!argList.isEmpty()) {
                if (argList.contains("s")) {
                    extra += "s";
                }
                if (argList.contains("u")) {
                    extra += "u";
                }
                if (argList.contains("d")) {
                    extra += "d";
                }
            }
            
            List<dCommand> found = FindType(shortened, arg);
            if (found.size() > 1) {
                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "I found " + defaultColor + found.size() + chatColor + " matches...");
                String list = "";
                for (dCommand cmd : found) {
                    list += cmd.getName() + ", ";
                }
                list = list.substring(0, list.length()-2) + ".";
                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), optionalColor + list);
                return;
            }
            else if (found.size() == 1) {
                int messageCount = 0;
                dCommand match = found.get(0);
                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + fullCap + " found: " + optionalColor + match.getName());
                if (found.get(0).getDeprecated() != null) {
                    bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "  This " + full + " is " + defaultColor + "deprecated" + chatColor + ": " + found.get(0).getDeprecated());
                    messageCount++;
                }
                messageCount++;
                String[] desc = match.getDesc().split("\n");
                for (int i = 0; i < desc.length; i++) {
                    if (i == 0) {
                        bot.sendMessage((chnl != null ? chnl.getName() : senderNick), defaultColor + "  Summary" + chatColor + ": " + desc[i]);
                    }
                    else {
                        bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "           " + desc[i]);
                    }
                    messageCount++;
                }
                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), defaultColor + "  Syntax" + chatColor + ": - " + match.getUsage());
                messageCount++;
                if (extra.contains("s")) {
                    bot.sendMessage((chnl != null ? chnl.getName() : senderNick), defaultColor + "  Stability" + chatColor + ": " + match.getStable());
                    messageCount++;
                }
                if (extra.contains("u")) {
                    String[] usage = match.getAltUsage().split("\n");
                    for (int i = 0; i < usage.length; i++) {
                        if (i == 0) {
                            if (messageCount > 5) {
                                bot.sendNotice(usr, defaultColor + "  Usage" + chatColor + ": " + usage[i]);
                            }
                            else {
                                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), defaultColor + "  Usage" + chatColor + ": " + usage[i]);
                                messageCount++;
                            }
                        }
                        else {
                            if (messageCount > 5) {
                                bot.sendNotice(usr, chatColor + "         " + usage[i]);
                            }
                            else {
                                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "         " + usage[i]);
                                messageCount++;
                            }
                        }
                    }
                }
                if (extra.contains("d")) {
                    String[] altDesc = match.getAltDesc().split("\n");
                    for (int i = 0; i < altDesc.length; i++) {
                        if (i == 0) {
                            if (messageCount > 5) {
                                bot.sendNotice(usr, defaultColor + "  Description" + chatColor + ": " + altDesc[i]);
                            }
                            else {
                                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), defaultColor + "  Description" + chatColor + ": " + altDesc[i]);
                                messageCount++;
                            }
                        }
                        else {
                            if (messageCount > 5) {
                                bot.sendNotice(usr, chatColor + "               " + altDesc[i]);
                                messageCount++;
                            }
                            else {
                                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "               " + altDesc[i]);
                                messageCount++;
                            }
                        }
                    }
                }
                return;
            }
            else {
                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + fullCap + " \"" + arg + "\" not found.");
                return;
            }
			
		} else if (msgLwr.startsWith(".events")) {
            List<dEvent> found = FindEvents("");
            String list = "";
            int size = found.size();
            bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "I found " + defaultColor + (size == 50 ? "50+" : size)
                    + chatColor + " matches...");
            for (int x = 0; x < size; x++) {
                if ((list + found.get(x).getEvent()).length() + 2 < 400) {
                    list += found.get(x).getEvent() + ", ";
                    if (x != 0 && x%10 == 0 && x+1 != size) {
                        if (x > 10) {
                            bot.sendNotice((address == "" ? senderNick : address.substring(0, address.length()-2)), optionalColor + list);
                        }
                        else {
                            bot.sendMessage((chnl != null ? chnl.getName() : senderNick), optionalColor 
                                    + list.substring(0, (x == 10 ? list.length()-2 : list.length()))
                                    + (x == 10 ? "..." : ""));
                        }
                        list = "";
                    }
                }
                else {
                    x--;
                    if (x > 10) {
                        bot.sendNotice((address == "" ? senderNick : address.substring(0, address.length()-2)), optionalColor + list);
                    }
                    else {
                        bot.sendMessage((chnl != null ? chnl.getName() : senderNick), optionalColor 
                                + list.substring(0, (x == 10 ? list.length()-2 : list.length()))
                                + (x == 10 ? "..." : ""));
                    }
                    list = "";
                }
            }
            if (size > 10) {
                bot.sendNotice((address == "" ? senderNick : address.substring(0, address.length()-2)),
                        optionalColor + list.substring(0, list.length()-2) + ".");
            }
            else {
                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), optionalColor + list.substring(0, list.length()-2) + ".");
            }
            return;
        } else if (msgLwr.startsWith(".event")) {
		    String[] args = msg.split(" ");
		    int argSize = args.length;
		    if (args[args.length-1].endsWith("@@")) {
		        argSize--;
		    }
		    if (argSize < 2) {
		        bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Check your argument count. Command format: .event [<event>]");
		        return;
		    }
		    String arg = "";
		    for (int i = 1; i < argSize; i++) {
		        arg += args[i] + " ";
		    }
            arg = arg.substring(0, arg.length()-1);
            if (debugMode) System.out.println("Finding events for " + arg);
            List<dEvent> found = FindEvents(arg);
            if (found.size() > 1) {
                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "I found " + defaultColor + found.size() + chatColor + " matches...");
                String list = "";
                if (found.get(0).getDeprecated() != null) {
                    bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "  This event is " + defaultColor + "deprecated" + chatColor + ": " + found.get(0).getDeprecated());
                }
                for (int x = 0; x < found.size(); x++) {
                    list += found.get(x).getMatchedWith() + ", ";
                    if (x != 0 && x%10 == 0 && x+1 != found.size()) {
                        if (x > 10) {
                            bot.sendNotice((address == "" ? senderNick : address.substring(0, address.length()-2)), optionalColor + list);
                        }
                        else {
                            bot.sendMessage((chnl != null ? chnl.getName() : senderNick), optionalColor 
                                    + list.substring(0, (x == 10 ? list.length()-2 : list.length()))
                                    + (x == 10 ? "..." : ""));
                        }
                        list = "";
                    }
                }
                if (found.size() > 10) {
                    bot.sendNotice((address == "" ? senderNick : address.substring(0, address.length()-2)),
                            optionalColor + list.substring(0, list.length()-2) + ".");
                }
                else {
                    bot.sendMessage((chnl != null ? chnl.getName() : senderNick), optionalColor + list.substring(0, list.length()-2) + ".");
                }
                return;
            }
            else if (found.size() == 1) {
                dEvent match = found.get(0);
                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Event found: " + optionalColor + match.getMatchedWith());
                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), defaultColor + "  Aliases" + chatColor + ": " + match.getEvent()); 
                String[] trigger = match.getTrigger().split("\n");
                for (int i = 0; i < trigger.length; i++) {
                    if (i == 0) {
                        bot.sendMessage((chnl != null ? chnl.getName() : senderNick), defaultColor + "  Triggered" + chatColor + ": " + trigger[i]);
                    }
                    else {
                        bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "  " + trigger[i]);
                    }
                }
                if (!match.getContext().isEmpty()) {
                    String[] context = match.getContext().split("\n");
                    for (int i = 0; i < context.length; i++) {
                        if (i == 0) {
                            bot.sendMessage((chnl != null ? chnl.getName() : senderNick), defaultColor + "  Contexts" + chatColor + ": " + context[i]);
                        }
                        else {
                            bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "            " + context[i]);
                        }
                    }
                }
                else {
                    bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "  There are no " + defaultColor + "contexts" + chatColor + " for this event.");
                }
                if (!match.getDetermine().isEmpty()) {
                    String[] determine = match.getDetermine().split("\n");
                    for (int i = 0; i < determine.length; i++) {
                        if (i == 0) {
                            bot.sendMessage((chnl != null ? chnl.getName() : senderNick), defaultColor + "  Determine" + chatColor + ": " + determine[i]);
                        }
                        else {
                            bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "             " + determine[i]);
                        }
                    }
                }
                else {
                    bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "  There are no " + defaultColor + "determinations" + chatColor + " for this event.");
                }
            }
            else {
                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Event \"" + arg + "\" not found.");
                return;
            }
		}
		
		else if (msgLwr.startsWith(".quote")) {
		    String[] args = msg.split(" ");
			List<List<String>> quotes = new ArrayList<List<String>>();
	        quotes.add(0, Arrays.asList
	                        ("<davidcernat> I like to think of the Flag command as the two barons of hell bosses at the end of Doom 1's first episode.",
	                         "<davidcernat> And of the If command as the cyberdemon at the end of the second episode.",
	                         "<davidcernat> And of the Listen command as the spiderdemon at the end of the third episode."));
	        quotes.add(1, Arrays.asList
	        				("<Mr_Einsburgtengra> how do i bring my power up really quick?",
	        				 "<Fatal_Ink> masturbate"));
            int number = new Random().nextInt(quotes.size());
            if (args.length > 1 && Integer.valueOf(args[1]) != null)
                number = (Integer.valueOf(args[1])-1 < 0 ? 0 : Integer.valueOf(args[1]));
	        List<String> randomQuote = quotes.get(number);
	       
	        for (String line : randomQuote) {
	                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + line);
	        }
		} else if (msgLwr.startsWith(".party") || msgLwr.startsWith(".celebrate")) {
			if (msgLwr.contains("reason: ")) {
				String[] split = msg.split("reason:");
				String reason = split[1].replace(" me ", senderNick + " ");
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Woo! Let's party for " + reason.substring(1, reason.length()) + "!");
				return;
			}
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Woo! It's party time! Come on, celebrate with me!");
			return;
		} else if (msgLwr.startsWith(".blame")) {
			String[] args = msg.split(" ");
			if (args.length < 3) {
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Check your argument count. Command format: .blame <user> <reason>");
				return;
			}
			String blamed = args[1];
			args = msg.split(blamed);
			String reason = args[1];
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + senderNick + " blames " + blamed + " for" + reason + "!");
		} else if (msgLwr.startsWith(".yaii")) {
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Your argument is invalid.");
			return;
		} else if (msgLwr.startsWith(".thmf") || msgLwr.startsWith(".tfw")) {
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "That hurt even my feelings. And I'm a robot.");
		} else if (msgLwr.startsWith(".cb") || msgLwr.startsWith(".coolbeans")) {
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "That's cool beans.");
			return;	
		} else if (msgLwr.equals(".sound") || msgLwr.equals(".sounds")) {
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Here is the list of all valid bukkit sounds - "+ Colors.BLUE + "http://bit.ly/14NYbvi");
		} else if (msgLwr.startsWith(".hb") || msgLwr.startsWith(".handbook")) {
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Current Documentation - "+ Colors.BLUE + "http://bit.ly/XaWBLN");
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "PDF download (always current) - "+ Colors.BLUE + "http://bit.ly/159JBgM");
			return;	
		} else if (msgLwr.startsWith(".getstarted") || msgLwr.startsWith(".gs")) {
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "So, you're trying to use 0.9 for the first time?");
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "It's recommended that you read the current documentation.");
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), Colors.BOLD + "Denizen Handbook " + Colors.NORMAL + chatColor + "- http://bit.ly/XaWBLN");
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), Colors.BOLD + "Denizen Wiki " + Colors.NORMAL + chatColor + "- http://bit.ly/14o3kdq");
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), Colors.BOLD + "Beginner's Guide" + Colors.NORMAL + chatColor + "- http://bit.ly/1bHkByR");
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Please keep in mind that documentation is a work in progress. You will likely not find everything.");
			return;	
		} else if (msgLwr.startsWith(".fire")) {
			if(hasOp(usr, chnl) || hasVoice(usr, chnl)) {
				String args[] = msg.split(" ");
				if(!charging) {
					bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Erm... was I supposed to be charging? D:");
					return;
				}
				if(usr != charger) {
					bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Sorry, but my firing sequence has already been started by "+charger.getNick()+".");
					return;
				}
				if(args.length != 2) {
					bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "I can't just fire into thin air :(");
					return;
				}
				if(chnl.getUsers().toString().contains(args[1])) {
					double chance = (Math.random() *99 + 1) * ((System.currentTimeMillis()-chargeInitiateTime)/chargeFullTime);
					if(chance > 50) {
						bot.sendAction((chnl != null ? chnl.getName() : senderNick), chatColor + "makes pew pew noises towards "+ args[1] + "... successfully!");
						bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Take that " + args[1] + "!");
					} else {
						bot.sendAction((chnl != null ? chnl.getName() : senderNick), chatColor + "makes pew pew noises towards "+ args[1] + "... and misses D:");
						bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "You've bested me this time " + args[1] + "...");
					}
					charging=false;
					chargeInitiateTime=0;
					charger=null;
					feeders.clear();
					return;
				} else {
					bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + args[1] + ", really? I need a legitimate target. This is serious business.");
					return;
				}
			}
			
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Whoa there, you can't touch that button.");
			return;
		
		} else if (msgLwr.startsWith(".lzrbms") || msgLwr.startsWith(".lzrbmz")) {
			if(hasOp(usr, chnl) || hasVoice(usr, chnl)) {
				if (botsnack < 3) { 
					bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor +": Botsnack levels too low. Can't charge lazers...");
					return;
				}
				
				if(charging) {
					bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor +": I'm already a bit occupied here!");
					return;
				}
				
				chargeInitiateTime=System.currentTimeMillis();
				charging=true;
				charger=usr;
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Imma chargin' up meh lazerbeamz...");
				botsnack-=3;
				return;
			}
			
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Umm, that's not for you.");
			return;
		
		} else if (msg.equalsIgnoreCase(".bye")) {
		    if (!hasOp(usr, chnl) && !hasVoice(usr, chnl)) {
                bot.sendMessage(chnl, chatColor + "Ahaha, you can never kill me, " + senderNick + "!");
            }
            else {
                repoManager.shutdown();
                for (dUser duser : dUsers.values()) {
                    try {
                        duser.saveAll();
                    } catch (Exception e) {
                        if (debugMode) e.printStackTrace();
                        else
                            System.out.println("An error has occured while saving for user " + duser.getNick() + "... Turn on debug for more information.");
                    }
                }
                String[] quotes = {"Ain't nobody got time for that...", "I'm backin' up, backin' up...", "Hide yo kids, hide yo wife..."};
                bot.sendMessage(chnl, chatColor + quotes[new Random().nextInt(quotes.length)]);
                shuttingDown = true;
                bot.disconnect();
                return;
            }
		}
		
		else if (msgLwr.startsWith(".depenizen")) {
		    dusr.invertDepenizen();
		    bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Depenizen mode set to " + defaultColor + dusr.getDepenizen() + chatColor + " for " + senderNick);
		}
		
		else if (msgLwr.startsWith(".seen")) {
		    String[] args = msg.split(" ");
		    String user = null;
		    dUser dusr2 = null;
		    if (args.length > 1)
		        user = args[1];
		    if (!dUsers.containsKey(user.toLowerCase())) {
		        bot.sendMessage(chnl, chatColor + "I've never seen that user: " + defaultColor + user);
		    }
		    else {
		        dusr2 = dUsers.get(user.toLowerCase());
		    }
		    Calendar now = Calendar.getInstance();
		    long currentTime = now.getTimeInMillis();
		    Calendar seen = Calendar.getInstance();
		    seen.setTime(dusr2.getLastSeenTime());
		    long seenTime = seen.getTimeInMillis();
		    long seconds = (currentTime-seenTime)/1000;
		    long minutes = seconds/60;
		    seconds = seconds-(minutes*60);
		    long hours = minutes/60;
		    minutes = minutes-(hours*60);
		    long days = hours/24;
		    hours = hours-(days*24);
		    long years = days/365;
		    days = days-(years*365);
		    long centuries = years/100;
		    years = years-(centuries*100);
		    long milleniums = centuries/10;
		    centuries = centuries-(milleniums*10);
		    bot.sendMessage(chnl, chatColor + "Last I saw of " + defaultColor + dusr2.getNick() + chatColor + " was " 
		            + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss zzz)").format(dusr2.getLastSeenTime()).replace("DT)", "ST").replace(")", "")
		            + ", " + dusr2.getLastSeen()
		            + chatColor + ". That " + (seconds < -1 ? "is " : "was ")
                    + ((milleniums > 1 || milleniums < -1) ? (Math.abs(milleniums) + " millenia, ") : ((milleniums == 1 || milleniums == -1) ? "1 millenium, " : ""))
                    + ((centuries > 1 || centuries < -1) ? (Math.abs(centuries) + " centuries, ") : ((centuries == 1 || centuries == -1) ? "1 century, " : ""))
                    + ((years > 1 || years < -1) ? (Math.abs(years) + " years, ") : ((years == 1 || years == -1) ? "1 year, " : ""))
		            + ((days > 1 || days < -1) ? (Math.abs(days) + " days, ") : ((days == 1 || days == -1) ? "1 day, " : ""))
		            + ((hours > 1 || hours < -1) ? (Math.abs(hours) + " hours, ") : ((hours == 1 || hours == -1) ? "1 hour, " : ""))
		            + ((minutes > 1 || minutes < -1) ? (Math.abs(minutes) + " minutes, ") : ((minutes == 1 || minutes == -1) ? "1 minute, " : ""))
		            + ((seconds == 1 || seconds == -1) ? (seconds == -1 ? "1 second from now." : "1 second ago.") : Math.abs(seconds) + (seconds < -1 ? " seconds from now." : " seconds ago.")));
		} else if ((msg.contains("hastebin.") || msg.contains("pastebin.") || msg.contains("pastie.")) && !(help.contains(usr) || chnl.hasVoice(usr) || chnl.isOp(usr))) {
	        help.add(usr);
		    bot.sendNotice(usr, "If you want to whether a Denizen script will compile, type " + Colors.BOLD + ".yml link_to_the_script");
			return;
		} else if ((msgLwr.contains("help") || msgLwr.contains("halp") || msgLwr.contains("hlp")) && !(help.contains(usr) || chnl.hasVoice(usr) || chnl.isOp(usr))) {
		    help.add(usr);
			bot.sendNotice(usr, "If you need help with a Denizen issue, type " + Colors.BOLD + ".help");
			return;
		}

        else if (msgLwr.startsWith(".save-all")) {
            for (dUser dusr2 : dUsers.values()) {
                if (debugMode) System.out.println("Saving dUser: " + dusr2.getNick() + "...");
                try {
                    dusr2.saveAll();
                } catch (Exception e) {
                    bot.sendNotice(usr, Colors.RED + "ERROR. Failed to save user information: " + defaultColor + dusr2.getNick());
                    if (debugMode) e.printStackTrace();
                    else
                        System.out.println("An error has occured while using .save-all for user " + dusr2.getNick() + "... Turn on debug for more information.");
                    return;
                }
            }
            bot.sendNotice(usr, chatColor + "Successfully saved all user information.");
        }
		
		else if (msgLwr.startsWith(".load")) {
		    for (dUser dusr2 : dUsers.values()) {
		        if (debugMode) System.out.println("Loading dUser information: " + dusr2.getNick() + "...");
		        try {
                    dusr2.loadAll();
                } catch (Exception e) {
                    bot.sendNotice(usr, Colors.RED + "ERROR. Failed to load user information: " + defaultColor + dusr2.getNick());
                    if (debugMode) e.printStackTrace();
                    else
                        System.out.println("An error has occured while using .load for user " + dusr2.getNick() + "... Turn on debug for more information.");
                    return;
                }
		    }
		    bot.sendNotice(usr, chatColor + "Successfully loaded all user information.");
		}
		
		else if (msgLwr.startsWith(".rate")) {
		    bot.sendNotice(usr, chatColor + "Max rate limit: " + github.getMaxRateLimit());
		    bot.sendNotice(usr, chatColor + "Remaining rate limit: " + github.getRemainingRateLimit());
		    
            long currentTime = Calendar.getInstance().getTimeInMillis();
            Calendar seen = Calendar.getInstance();
            seen.setTime(github.getRateLimitReset());
            long seenTime = seen.getTimeInMillis();
            long seconds = (seenTime - currentTime)/1000;
            long minutes = seconds/60;
            seconds = seconds-(minutes*60);
            
		    bot.sendNotice(usr, chatColor + "Next reset: "
		            + (minutes > 0 ? minutes > 1 ? minutes + " minutes, " : "1 minute, " : "")
		            + (seconds > 0 ? seconds > 1 ? seconds + " seconds" : "1 second" : minutes > 0 ? "0 seconds" : "Now."));
		}
		
		else if (msgLwr.startsWith(".add")) {
		    if (!hasOp(usr, chnl) && !hasVoice(usr, chnl))
		        bot.sendMessage(chnl, chatColor + "Sorry, " + senderNick + ", that's only for the Dev Team.");
		        
		    String[] args = msgLwr.trim().split(" ", 3);
		    
		    if (args[1].startsWith("repo") && args.length > 2) {
		        if (args[2].contains("/")) {
		            String[] proj = args[2].split("/", 2);
		            repoManager.addRepository(proj[0], proj[1], 60, !msgLwr.contains("no_issues"));
		        }
		        else if (args.length > 3) {
		            repoManager.addRepository(args[2], args[3], 60, !msgLwr.contains("no_issues"));
		        }
		        else
		            bot.sendMessage(chnl, chatColor + "That command is written as: .add repo [<<author>/<project>>] (no_issues)");
		    }
	        else
	            bot.sendMessage(chnl, chatColor + "That command is written as: .add <object> <info>");
		}
		
		else if (msgLwr.startsWith(".remove")) {
            if (!hasOp(usr, chnl) && !hasVoice(usr, chnl))
                bot.sendMessage(chnl, chatColor + "Sorry, " + senderNick + ", that's only for the Dev Team.");
                
            String[] args = msgLwr.trim().split(" ");
            
            if (args[1].startsWith("repo") && args.length > 2) {
                if (args.length > 2)
                    repoManager.removeRepository(args[2]);
                else
                    bot.sendMessage(chnl, chatColor + "That command is written as: .remove repo [<project>]");
            }
            else
                bot.sendMessage(chnl, chatColor + "That command is written as: .remove <object>");
		}
		
		else if (msgLwr.startsWith(".list")) {
		    String[] args = msgLwr.trim().split(" ");
		    if (args.length < 2)
		        bot.sendMessage(chnl, chatColor + "That command is written as: .list <object>");
		    
		    if (args[1].startsWith("repo")) {
		        Set<String> repos = repoManager.getRepositories();
		        bot.sendMessage(chnl, chatColor + "I'm currently watching " + repos.size() + " repositories...");
		        bot.sendMessage(usr, chatColor + repos.toString());
		    }
		}
        
        dusr.checkMessages(chnl);
        
	}

	private static void reloadSites(boolean debug) throws Exception {
	    boolean original = debugMode;
	    if (!debugMode)
	        debugMode = debug;
    	loadMeta();
    	debugMode = original;
	}
	
	private static ArrayList<String> findUserFiles() {
	    ArrayList<String> ret = new ArrayList<String>();
	    
	    for (File file : usersFolder.listFiles(new FilenameFilter() {public boolean accept(File dir, String filename){return filename.endsWith(".yml");}})) {
	        ret.add(file.getName().substring(0, file.getName().indexOf('.')));
	    }
	    
	    return ret;
	}
	
	private boolean hasVoice(User chatter, Channel channel) {
		if(channel.hasVoice(chatter))
			return true;
		return false;
	}
	
	private boolean hasOp(User chatter, Channel channel) {
		if(channel.isOp(chatter))
			return true;
		return false;
	}
	
	private static String parseUsage(String unparsed) {
		
		// Keeping debugging code for use with future commands
		
		String formatted = unparsed;	
		String beforeColor = chatColor;
		
		formatted = formatted.replace("\\", "");
		formatted = formatted.replace("[", chatColor + "[");
		formatted = formatted.replace("(", optionalColor + "(");
		formatted = formatted.replace("{", defaultColor + "{");
		formatted = formatted.replace("]", chatColor + "]");
		formatted = formatted.replace(")", optionalColor + ")");
		formatted = formatted.replace("}", defaultColor + "}");		
		int requiredIndex = formatted.indexOf("]");
		//if (debugMode) System.out.println("Beginning " +formatted);

		while (requiredIndex != -1) {
			
			int lastRequired = formatted.indexOf("]", requiredIndex+1);
			int lastOptional = formatted.indexOf(")", requiredIndex);
			int lastDefault = formatted.indexOf("}", requiredIndex);
			int lastSpace = formatted.indexOf(" ", requiredIndex);

			if(lastSpace == -1 || lastSpace > lastDefault || lastSpace > lastOptional || lastSpace > lastRequired) {
				if(lastRequired != -1 && (lastSpace > lastRequired || lastSpace == -1)) {
					if((lastDefault == -1 || lastOptional == -1) || lastRequired < lastDefault && lastRequired < lastOptional);
						beforeColor = chatColor;
				}
				else
					if(lastOptional != -1 && (lastSpace > lastOptional || lastSpace == -1)) {
						if(lastDefault == -1 || lastOptional < lastDefault);
							beforeColor = optionalColor;
					}
				
					else {
						if(lastDefault != -1 && (lastSpace > lastDefault || lastSpace == -1))
							beforeColor = defaultColor;
				}
			}
			//if (debugMode) System.out.println("Required Loop before change " +formatted);
			//if (debugMode) System.out.println("First half " + formatted.substring(0, requiredIndex + 1));
			//if (debugMode) System.out.println("Second half " + formatted.substring(requiredIndex + 1));

			formatted = formatted.substring(0, requiredIndex + 1) + beforeColor + formatted.substring(requiredIndex + 1);
			requiredIndex = formatted.indexOf("]",requiredIndex + 1);
			//if (debugMode) System.out.println("Required Loop after change " +formatted);

		}
		//if (debugMode) System.out.println("After Req Loop " + formatted);

		int optionalIndex = formatted.indexOf(")");
		
		while (optionalIndex != -1) {

			int lastOptional = formatted.indexOf(")", optionalIndex+1);
			int lastRequired = formatted.indexOf("]", optionalIndex);
			int lastDefault = formatted.indexOf("}", optionalIndex);
			int lastSpace = formatted.indexOf(" ", optionalIndex);
			
			if(lastSpace == -1 || lastSpace > lastDefault || lastSpace > lastRequired || lastSpace > lastOptional) {
				if(lastOptional != -1 && (lastSpace > lastOptional || lastSpace == -1)) {
					if((lastDefault == -1 && lastRequired == -1) || (lastOptional < lastDefault && lastOptional < lastRequired))
						beforeColor = optionalColor;
				}
				else
					if(lastRequired != -1 && (lastSpace > lastRequired || lastSpace == -1)) {
						if(lastDefault == -1 || lastRequired < lastDefault);
							beforeColor = chatColor;
					}
					
					else {
						if(lastDefault != -1 && (lastSpace > lastDefault || lastSpace == -1))
							beforeColor = defaultColor;
					}
			}
			//if (debugMode) System.out.println("Optional Loop before change " + formatted);

			formatted = formatted.substring(0, optionalIndex + 1) + beforeColor + formatted.substring(optionalIndex + 1);
			optionalIndex = formatted.indexOf(")",optionalIndex + 1);
			//if (debugMode) System.out.println("Optional Loop after change " + formatted);

		}
		//if (debugMode) System.out.println("After OL " + formatted);

		int defaultIndex = formatted.indexOf("}");
		
		while (defaultIndex != -1) {
		
			int lastRequired = formatted.indexOf("]", defaultIndex);
			int lastOptional = formatted.indexOf(")", defaultIndex);
			int lastSpace = formatted.indexOf(" ", defaultIndex);
			
			if(lastSpace == -1 || lastSpace > lastOptional || lastSpace > lastRequired) {
				if(lastRequired != -1 && (lastSpace > lastRequired || lastSpace == -1)) {
					if(lastOptional == -1 || lastRequired < lastOptional);
						beforeColor = chatColor;
				}
				
				else {
					if(lastOptional != -1 && (lastSpace > lastOptional || lastSpace == -1))
						beforeColor = optionalColor;
				}
			}
			//if (debugMode) System.out.println("DL before change " + formatted);

			formatted = formatted.substring(0, defaultIndex + 1) + beforeColor + formatted.substring(defaultIndex + 1);
			defaultIndex = formatted.indexOf("}",defaultIndex + 1);
			//if (debugMode) System.out.println("DL after change " + formatted);

		}
		

		//if (debugMode) System.out.println("Final " + formatted);
		return formatted;
	}
	
	private String parseColor(String colorName) {
		
		if(colorName.contains("&")) {
			if(colorName.length() < 2)
				return null;
			String symbol = colorName.substring(1, colorName.length());
			if (symbol == "0")
				return Colors.BLACK;
			else if (symbol == "1")
				return Colors.DARK_BLUE;
			else if (symbol == "2")
				return Colors.DARK_GREEN;
			else if (symbol == "3")
				return Colors.TEAL;
			else if (symbol == "4")
				return Colors.RED;
			else if (symbol == "5")
				return Colors.PURPLE;
			else if (symbol == "6")
				return Colors.YELLOW;
			else if (symbol == "7")
				return Colors.LIGHT_GRAY;
			else if (symbol == "8")
				return Colors.DARK_GRAY;
			else if (symbol == "9")
				return Colors.BLUE;
			else if (symbol == "a")
				return Colors.GREEN;
			else if (symbol == "b")
				return Colors.CYAN;
			else if (symbol == "c")
				return Colors.RED;
			else if (symbol == "d")
				return Colors.MAGENTA;
			else if (symbol == "e")
				return Colors.YELLOW;
			else if (symbol == "f")
				return Colors.WHITE;
			else return null;
		}
		
		if(colorName.equalsIgnoreCase("black"))
			return Colors.BLACK;
		if(colorName.equalsIgnoreCase("blue") || colorName.equalsIgnoreCase("lightblue"))
			return Colors.BLUE;
		if(colorName.equalsIgnoreCase("brown") || colorName.equalsIgnoreCase("poop"))
			return Colors.BROWN;
		if(colorName.equalsIgnoreCase("cyan"))
			return Colors.CYAN;
		if(colorName.equalsIgnoreCase("darkblue"))
			return Colors.DARK_BLUE;
		if(colorName.equalsIgnoreCase("darkgray"))
			return Colors.DARK_GRAY;
		if(colorName.equalsIgnoreCase("green") || colorName.equalsIgnoreCase("darkgreen"))
			return Colors.DARK_GREEN;
		if(colorName.equalsIgnoreCase("lime") || colorName.equalsIgnoreCase("brightgreen") || colorName.equalsIgnoreCase("lightgreen") || colorName.equalsIgnoreCase("&a"))
			return Colors.GREEN;
		if(colorName.equalsIgnoreCase("lightgray"))
			return Colors.LIGHT_GRAY;
		if(colorName.equalsIgnoreCase("magenta"))
			return Colors.MAGENTA;
		if(colorName.equalsIgnoreCase("olive") || colorName.equalsIgnoreCase("orange"))
			return Colors.OLIVE;
		if(colorName.equalsIgnoreCase("purple"))
			return Colors.PURPLE;
		if(colorName.equalsIgnoreCase("red") || colorName.equalsIgnoreCase("warning"))
			return Colors.RED;
		if(colorName.equalsIgnoreCase("reverse") || colorName.equalsIgnoreCase("contrast"))
			return Colors.REVERSE;
		if(colorName.equalsIgnoreCase("teal"))
			return Colors.TEAL;
		if(colorName.equalsIgnoreCase("white"))
			return Colors.WHITE;
		if(colorName.equalsIgnoreCase("yellow"))
			return Colors.YELLOW;
		
		return null;
	}

	public static String getCustomStackTrace(Throwable aThrowable) {
	    //add the class name and any message passed to constructor
	    final StringBuilder result = new StringBuilder();
	    result.append(aThrowable.toString());
	    final String NEW_LINE = System.getProperty("line.separator");
	    result.append(NEW_LINE);

	    //add each element of the stack trace
	    for (StackTraceElement element : aThrowable.getStackTrace() ){
	      result.append( element );
	      result.append( NEW_LINE );
	    }
	    return result.toString();
	}
	
	public static List<dEvent> FindEvents(String input) {
	    List<dEvent> found = new ArrayList<dEvent>();
	    for (dEvent event : dEvents) {
	        String[] events = event.getEvent().split(", ");
	        for (String name : events) {
	            if (name.toLowerCase().replaceAll("(\\(|\\)|<|>)", "").equals(input.replaceAll("(\\(|\\)|<|>)", ""))) {
                    found.clear();
                    found.add(event.clone().setMatched(name));
                    if (debugMode) System.out.println("Adding event " + name);
                    return found;
                }
                else if (name.toLowerCase().replaceAll("(\\(|\\)|<|>)", "").contains(input.replaceAll("(\\(|\\)|<|>)", ""))) {
                    found.add(event.clone().setMatched(name));
                    break;
                }
	        }
	    }
        return found;
	}
	
	public static List<dCommand> FindType(String type, String input) {
	    List<dCommand> found = new ArrayList<dCommand>();
	    if (type.startsWith("r")) {
	        for (dCommand cmd : dRequirements) {
	            if (cmd.getName().toLowerCase().equals(input.toLowerCase())) {
	                found.clear();
	                found.add(cmd);
	                return found;
	            }
	            if (cmd.getName().toLowerCase().contains(input.toLowerCase())) {
	                found.add(cmd);
	            }
	        }
	    }
	    else if (type.startsWith("c")) {
            for (dCommand cmd : dCommands) {
                if (cmd.getName().toLowerCase().equals(input.toLowerCase())) {
                    found.clear();
                    found.add(cmd);
                    return found;
                }
                if (cmd.getName().toLowerCase().contains(input.toLowerCase())) {
                    found.add(cmd);
                }
            }
        }
	    return found;
	}
	
	public static List<dTag> FindTags(boolean endDot, String... input) {
        List<dTag> found = new ArrayList<dTag>();
        List<dTag> foundChains = new ArrayList<dTag>();
        List<String> foundNames = new ArrayList<String>();
	    List<String> possible = new ArrayList<String>();
	    boolean entity = false;
	    String root = input[0].toLowerCase();
	    String subtags = "";
	    if (input.length > 1) {
	        for (int i = 1; i < input.length; i++) {
	            subtags += "." + input[i].toLowerCase();
	        }
	    }
        if ("p@player".contains(root)) {
            root = "p@player";
            entity = true;
        }
        else if ("n@npc".contains(root)) {
            root = "n@npc";
            entity = true;
        }
        else if ("e@entity".contains(root)) {
            root = "e@entity";
        }
        else {
            for (String pre : dTagPrefixes) {
                if (root.contains(pre.toLowerCase())) {
                    root = pre;
                    break;
                }
            }
        }
	    if (entity) {
	        possible.add("e@entity" + subtags);
	    }
	    possible.add(root + subtags);
		for (dTag tag : dTags) {
		    for (String name : possible) {
		        tag = tag.clone();
                if (entity) {
                    tag.setName(tag.getName().replaceFirst("e@entity", root)).setDesc(tag.getDesc().replace("entity", root.split("@")[1]));
                }
		        if (!endDot && (tag.getName().equalsIgnoreCase(name) || tag.getAlt().equalsIgnoreCase(name))) {
		            found.clear();
		            found.add(tag);
	                if (debugMode) System.out.println("Returning found");
		            return found;
		        }
		        if (tag.getName().toLowerCase().contains(name + (endDot ? "." : "")) || tag.getAlt().toLowerCase().contains(name + (endDot ? "." : ""))) {
		            if (!foundNames.contains(tag.getName())) {
		                found.add(tag);
		                foundNames.add(tag.getName());
	                    if (found.size() + foundChains.size() == 50) {
	                        if (debugMode) System.out.println("Returning found");
                            found.addAll(foundChains);
	                        return found;
	                    }
		            }
		        }
                if (endDot && tag.getName().toLowerCase().contains(name) || tag.getAlt().toLowerCase().contains(name)) {
                    if (debugMode) System.out.println("Searching for chains");
                    if (dTagByType.containsKey((tag.getReturn().startsWith("d") ? tag.getReturn().substring(1).toLowerCase() : tag.getReturn().toLowerCase()))) {
                        if (debugMode) System.out.println("Type found");
                        for (dTag retTag : dTagByType.get((tag.getReturn().startsWith("d") ? tag.getReturn().substring(1).toLowerCase() : tag.getReturn().toLowerCase()))) {
                            retTag = retTag.clone();
                            if (entity) {
                                retTag.setName(tag.getName().replaceFirst("e@entity", root)).setDesc(tag.getDesc().replace("entity", root.split("@")[1]));
                            }
                            if (!foundNames.contains(retTag.getName())) {
                                foundChains.add(retTag);
                                foundNames.add(retTag.getName());
                                if (debugMode) System.out.println("Adding tag: " + (entity ? tag.getName().replaceFirst("e@entity", root) : tag.getName()) + retTag.getName().substring(retTag.getName().indexOf('.')));
                                if (found.size() + foundChains.size() == 50) {
                                    if (debugMode) System.out.println("Returning found");
                                    found.addAll(foundChains);
                                    return found;
                                }
                            }
                        }
                    }
                }
		    }
		}
        if (debugMode) System.out.println("Returning found");
        found.addAll(foundChains);
		return found;
    }
	
	public static class dUser {
		
		private String lastSeen;
		private Date lastSeenTime;
		private String nick;
		private boolean status;
		private MessageList messages;
		private boolean depenizen;
		
		public dUser(String nick) {
		    if (nick == null || nick.equals("")) return;
		    this.nick = nick;
            this.messages = new MessageList();
            this.depenizen = false;
            setLastSeen("Existing");
		    if (!new File(System.getProperty("user.dir") + "/users/" + nick.toLowerCase() + ".yml").exists()) {
		        try {
		            saveAll();
		        } catch (Exception e) {
	                if (debugMode) e.printStackTrace();
	                else
	                    System.out.println("An error has occured while saving new user " + getNick() + "... Turn on debug for more information.");
                }
		    }
		    else {
		        try {
                    loadAll();
                } catch (Exception e) {
                    if (debugMode) e.printStackTrace();
                    else
                        System.out.println("An error has occured while loading existing user " + getNick() + "... Turn on debug for more information.");
                }
		    }
            
		}

		void setLastSeen(String lastSeen) {
		    this.lastSeenTime = Calendar.getInstance().getTime();
			this.lastSeen = Utilities.uncapitalize(lastSeen);
		}
		
		void setLastSeenRaw(String lastSeen) {
			this.lastSeen = lastSeen;
		}
		
		void setStatus(boolean status) {
			this.status = status;
		}
		
		void invertDepenizen() {
		    depenizen = !depenizen;
		}
		
		boolean getDepenizen() {
		    return depenizen;
		}
		
		void addMessage(Message msg) {
			this.messages.add(msg);
		}
		
		void saveAll() throws Exception {
		    DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("name", getNick().toString());
            data.put("depenizen", getDepenizen());
            data.put("lastseen", getLastSeen().getBytes());
            Calendar seenTime = Calendar.getInstance();
            seenTime.setTime(getLastSeenTime());
            data.put("lasttime", new SimpleDateFormat((seenTime.getTimeInMillis() < 0 ? "-yyyy" : "yyyy") + "-MM-dd HH:mm:ss.SSS zzz").format(getLastSeenTime()));
            data.put("messages", getMessages().getMessages());
            if (getNick().equals("spazzmatic")) {
                data.put("password", System.getProperty("spazz.password"));
                data.put("bitly", System.getProperty("spazz.bitly"));
            }

            FileWriter writer = new FileWriter(usersFolder + "/" + getNick().toLowerCase() + ".yml");
            writer.write(yaml.dump(data));
            writer.close();
		}
		
        @SuppressWarnings("unchecked")
        void loadMessages() throws Exception {
		    LinkedHashMap map = null;
            Yaml yaml = new Yaml();
            File f = new File(usersFolder + "/" + getNick().toLowerCase() + ".yml");
            InputStream is = f.toURI().toURL().openStream();
            map = (LinkedHashMap) yaml.load(is);
            if (map.get("messages") instanceof HashMap<?, ?>) {
                for (Map.Entry<String, ArrayList<String>> msgs : ((HashMap<String, ArrayList<String>>) map.get("messages")).entrySet()) {
                    for (String msg : msgs.getValue()) {
                        if (!this.messages.getMessagesFrom(msgs.getKey()).contains(msg)) {
                            String[] split = msg.split("_", 2);
                            addMessage(new Message(msgs.getKey(), dateFormat.parse(split[0]), split[1]));
                        }
                    }
                }
            }
		}
		
		void loadLastSeen() throws Exception {
		    LinkedHashMap map = null;
            Yaml yaml = new Yaml();
            File f = new File(usersFolder + "/" + getNick().toLowerCase() + ".yml");
            f.mkdirs();
            InputStream is = f.toURI().toURL().openStream();
            map = (LinkedHashMap) yaml.load(is);
            // Keep string checker so we can edit a YAML file with text and have Spazz translate it later
            if (map.get("lastseen") instanceof String) {
                this.lastSeen = ((String) map.get("lastseen")).replace("`````", "#").replace("````", "|");
                this.lastSeenTime = dateFormat.parse((String) map.get("lasttime"));
            }
            else if (map.get("lastseen") instanceof byte[]) {
                this.lastSeen = new String((byte[]) map.get("lastseen"));
                this.lastSeenTime = dateFormat.parse((String) map.get("lasttime"));
            }
            else
                setLastSeen("Existing");
            
            if (debugMode) System.out.println("Loaded user with lasttime: " + dateFormat.format(getLastSeenTime()));
		}
		
		void loadDepenizen() throws Exception {
		    LinkedHashMap map = null;
            Yaml yaml = new Yaml();
            File f = new File(usersFolder + "/" + getNick().toLowerCase() + ".yml");
            f.mkdirs();
            InputStream is = f.toURI().toURL().openStream();
            map = (LinkedHashMap) yaml.load(is);
            if (map.get("depenizen") instanceof Boolean)
                this.depenizen = (Boolean) map.get("depenizen");
            else
                this.depenizen = false;
		}
		
		void loadAll() throws Exception {
            if (debugMode) System.out.println("Loading messages for \"" + getNick() + "\"...");
			loadMessages();
			if (debugMode) System.out.println("Messages loaded: " + this.messages.getMessages());
            if (debugMode) System.out.println("Loading lastseen for \"" + getNick() + "\"...");
			loadLastSeen();
            if (debugMode) System.out.println("Lastseen loaded: " + this.lastSeen);
            if (debugMode) System.out.println("Loading depenizen for \"" + getNick() + "\"...");
			loadDepenizen();
			if (debugMode) System.out.println("Depenizen loaded: " + this.depenizen);
		}
		
		public String getLastSeen() {
			return this.lastSeen;
		}
		
		public Date getLastSeenTime() {
		    return this.lastSeenTime;
		}
	
		public String getNick() {
			return this.nick;
		}
		
		public boolean getStatus() {
			return this.status;
		}
		
		public void checkMessages(Channel chnl) {
		    if (this.messages.isEmpty() || chnl == null) return;
		    ArrayList<String> toSend = new ArrayList<String>();
		    for (Map.Entry<String, ArrayList<String>> msgs : this.messages.getMessages().entrySet()) {
		        for (String time_msg : msgs.getValue()) {
		            toSend.add(time_msg + "_" + msgs.getKey());
		        }
		    }
		    Collections.sort(toSend, new Comparator<String>() {
		        public int compare(String o1, String o2) {
                    try {
                        return dateFormat.parse(o1.split("_")[0]).compareTo(dateFormat.parse(o2.split("_")[0]));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return 0;
                    }
                }
	        });
            bot.sendMessage(chnl, getNick() + ": " + chatColor + "You have messages waiting for you...");
            String user = null;
            for (String time_msg_user : toSend) {
                String[] args = time_msg_user.split("_", 3);
                String subUser = args[2];
                if (user == null || !user.equalsIgnoreCase(subUser)) {
                    user = subUser;
                    bot.sendNotice(getNick(), defaultColor + user + chatColor + ":");
                }
                String msg = args[1];
                bot.sendNotice(getNick(), chatColor + "  " + msg);
            }
		    this.messages.clear();
		    try {
                saveAll();
            } catch (Exception e) {
                if (debugMode) e.printStackTrace();
                else
                    System.out.println("An error has occured while using checkMessages() for user " + getNick() + "... Turn on debug for more information.");
            }
		}
		
		public MessageList getMessages() {
		    if (this.messages == null) {
		        try { loadAll(); }
		        catch (Exception e) {
		            if (debugMode) e.printStackTrace();
		            else 
		                System.out.print("An error has occured while using getMessages() for user " + getNick() + "... Turn on debug for more information.");
		        }
		    }
		    return this.messages;
		}
		
	}
	
    public static class MessageList {
	    
	    HashMap<String, ArrayList<String>> messages;
	    
	    public MessageList(ArrayList<Message> messages) {
	        this.messages = new HashMap<String, ArrayList<String>>();
	        for (Message message : messages) {
	            if (!this.messages.containsKey(message.getUser()))
	                this.messages.put(message.getUser(), new ArrayList<String>());
	            this.messages.get(message.getUser()).add(dateFormat.format(message.getTime()) + "_" + message.getMessage());
	        }
	    }
	    
	    public MessageList() {
            this.messages = new HashMap<String, ArrayList<String>>();
        }
	    
	    public boolean isEmpty() {
	        return this.messages.isEmpty();
	    }
	    
	    public void clear() {
	        this.messages.clear();
	    }

        public HashMap<String, ArrayList<String>> getMessages() {
	        return messages;
	    }
        
        public ArrayList<String> getMessagesFrom(String user) {
            if (messages.containsKey(user))
                return messages.get(user);
            else
                return new ArrayList<String>();
        }
        
        public MessageList add(Message msg) {
            if (!messages.containsKey(msg.getUser()))
                messages.put(msg.getUser(), new ArrayList<String>());
            messages.get(msg.getUser()).add(dateFormat.format(msg.getTime()) + "_" + msg.getMessage());
            return this;
        }
	    
	}
	
	public static class Message {
		
		private String user;
		private Date time;
		private String message;
		
		public Message(String user, Date date, String message) {
			this.user = user;
			this.time = date;
			this.message = message;
		}
		
		public String getUser() {
			return this.user;
		}
		
		public String getMessage() {
			return this.message;
		}
		
		public Date getTime() {
		    return this.time;
		}
		
	}
	
	public static class dTag {
		public String name;
        public String desc;
        public String alt;
        public String returnType;
        public String deprecated;
        
        public dTag(String name, String desc, String alt, String returnType, String deprecated) {
            this.name = name;
            this.desc = desc;
            this.alt = alt;
            this.returnType = returnType;
            this.deprecated = deprecated;
        }
        
        public String getName() {
        	return name;
        }
        
        public String getDesc() {
        	return desc;
        }
        
        public String getAlt() {
        	return alt;
        }
        
        public String getReturn() {
        	return returnType;
        }
        
        public String getDeprecated() {
            return deprecated;
        }
        
        public dTag clone() {
            return new dTag(name, desc, alt, returnType, deprecated);
        }
        
        public dTag setName(String name) {
            this.name = name;
            return this;
        }
        
        public dTag setDesc(String desc) {
            this.desc = desc;
            return this;
        }
	}
	
	public static class dCommand {
	    public String alias;
	    public String name;
	    public String desc;
	    public String usage;
        public String altUsage;
        public String altDesc;
        public String stable;
        public String example;
        public String deprecated;
	    
	    public dCommand(String alias, String name, String desc, String usage, String altUsage, String altDesc, String stable, String example, String deprecated) {
	        this.alias = alias;
	        this.name = name;
	        this.desc = desc;
	        this.usage = parseUsage(usage);
            this.altUsage = altUsage;
            this.altDesc = altDesc;
            this.stable = stable;
            this.example = example;
            this.deprecated = deprecated;
	    }
	    
	    public String getName() {
	        return alias;
	    }
	    
	    public String getRealName() {
	        return name;
	    }
	    
	    public String getDesc() {
	        return desc;
	    }
	    
	    public String getUsage() {
	        return usage;
	    }
        
        public String getAltUsage() {
            return altUsage;
        }
        
        public String getAltDesc() {
            return altDesc;
        }
        
        public String getStable() {
            return stable;
        }
        
        public String getExample() {
            return example;
        }
        
        public String getDeprecated() {
            return deprecated;
        }
	}
	
	public static class dEvent {
	    public String event;
	    public String triggers;
	    public String context;
	    public String determine;
	    public String matchedWith;
        public String deprecated;
	    
	    public dEvent(String event, String triggers, String context, String determine, String deprecated) {
	        this.event = event;
	        this.triggers = triggers;
	        this.context = context;
	        this.determine = determine;
            this.deprecated = deprecated;
	    }
	    
	    public String getEvent() {
	        return event;
	    }
	    
	    public String getTrigger() {
	        return triggers;
	    }
	    
	    public String getContext() {
	        return context;
	    }
	    
	    public String getDetermine() {
	        return determine;
	    }
	    
	    public String getMatchedWith() {
	        return matchedWith;
	    }
        
        public String getDeprecated() {
            return deprecated;
        }
	    
	    public dEvent clone() {
	        return new dEvent(event, triggers, context, determine, deprecated);
	    }
	    
	    public dEvent setMatched(String matchedWith) {
	        this.matchedWith = matchedWith;
	        return this;
	    }
	}
	
}
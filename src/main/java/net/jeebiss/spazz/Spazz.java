package net.jeebiss.spazz;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.WordUtils;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Listener;
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
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.rosaloves.bitlyj.Bitly;

@SuppressWarnings("rawtypes")
public class Spazz extends ListenerAdapter implements Listener {
	
	public static PircBotX bot = new PircBotX();
	
	public static GitHub github;
	public static GHRepository repo;
	static int openIssues = -1;
	static int closedIssues = -1;
	static Map<Integer, GHIssue> openIssuesList = new HashMap<Integer, GHIssue>();
	static Map<Integer, GHIssue> closedIssuesList = new HashMap<Integer, GHIssue>();

	public static List<dTag> dTags = new ArrayList<dTag>();
	public static List<String> dTagPrefixes = new ArrayList<String>();
	public static Map<String, ArrayList<dTag>> dTagByType = new HashMap<String, ArrayList<dTag>>();
	public static List<dCommand> dCommands = new ArrayList<dCommand>();
	public static List<dCommand> dRequirements = new ArrayList<dCommand>();
	public static List<dEvent> dEvents = new ArrayList<dEvent>();
	
	public static Map<String, dUser> dUsers = new HashMap<String, dUser>();
	
    static String getUrl(String url) throws IOException {
    	InputStream wp = new URL(url).openStream();
		String returns = IOUtils.toString(wp);
		wp.close();
		return returns;
    }
	
    String[] temp;
	static String chatColor = Colors.TEAL;
	static String optionalColor = Colors.DARK_GREEN;
	static String defaultColor = Colors.OLIVE;
	boolean charging=false;
	boolean logged_in=false;
	long chargeInitiateTime;
	long chargeFullTime = 30000;
	User charger;
	int botsnack = 0;
	ArrayList<User> feeders = new ArrayList<User>();
	ArrayList<User> help = new ArrayList<User>();
	boolean usingResources = false;
	String confirmingComment = null;
	int confirmingIssue = 0;
	Boolean confirmComment = false;
	String confirmIssueUser = null;
	GHIssueBuilder confirmIssue = null;

    static HtmlUnitDriver GHCR = new HtmlUnitDriver();
    static HtmlUnitDriver GHRR = new HtmlUnitDriver();
    static HtmlUnitDriver PASTEBIN = new HtmlUnitDriver();
    static WebClient HASTEBIN = new WebClient();
    static WebClient PASTIE = new WebClient();
	
    private static void loadMeta() throws IOException {
		dTags.clear();
		dTagPrefixes.clear();
		dTagByType.clear();
		dCommands.clear();
		dRequirements.clear();
		dEvents.clear();
		String pages = getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dList.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/tags/core/UtilTags.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/Duration.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dCuboid.java")
				+ getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dWorld.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dColor.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dItem.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dPlayer.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dNPC.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dEntity.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dScript.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dMaterial.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dInventory.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/Element.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dLocation.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/scripts/commands/core/YamlCommand.java")
		        + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/scripts/commands/CommandRegistry.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/scripts/requirements/RequirementRegistry.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/scripts/containers/core/WorldScriptHelper.java");
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
                        System.out.println("Adding tag " + name + " AKA " + nname + "...");
                        System.out.println("...with description: " + tagdesc);
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
                        System.out.println("Adding " + type + " " + name + ": " + usage + "...");
                        System.out.println("...with description: " + fulldesc);
                        System.out.println();
                    }
                    String realname = name.split(", ")[0];
                    for (String alias : name.split(", ")) {
                        if (type.startsWith("c")) {
                            dCommands.add(new dCommand(WordUtils.capitalize(alias), WordUtils.capitalize(realname),
                                    fulldesc, usage.replaceFirst(realname.toLowerCase(), name.toLowerCase()),
                                    alt, descAlt, stable, fullExample, deprecated));
                        }
                        else if (type.startsWith("r")) {
                            dRequirements.add(new dCommand(WordUtils.capitalize(alias), WordUtils.capitalize(realname),
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
                        System.out.println("Adding event " + altUsage.get(0) + ": " + triggers + "...");
                        System.out.println("...with aliases: " + events);
                        System.out.println();
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
    
    public static boolean debugMode = true;
    
	public static void main(String[] args) throws Exception {
        
        github = GitHub.connectUsingPassword("spazzmatic", System.getProperty("spazz.password"));
        repo = github.getRepository("aufdemrand/Denizen");
        openIssues = repo.getOpenIssueCount();
        closedIssues = repo.getIssues(GHIssueState.CLOSED).size();
        for (GHIssue issue : repo.getIssues(GHIssueState.OPEN))
            openIssuesList.put(issue.getNumber(), issue);
        for (GHIssue issue : repo.getIssues(GHIssueState.CLOSED))
            closedIssuesList.put(issue.getNumber(), issue);
        
        reloadSites(true);
        
		bot.getListenerManager().addListener(new Spazz());
		/*
		 * Connect to #denizen-dev on start up
		 */
        bot.setVersion("Spazzmatic v0.3 [Morphan1]");
        bot.setAutoReconnect(true);
        bot.setName("spazzmatic");
        bot.setLogin("spazz");
        bot.setVerbose(debugMode);
        bot.setAutoNickChange(true);
        bot.identify(System.getProperty("spazz.password"));
        
        bot.connect("irc.esper.net");
		bot.setMessageDelay(0);
        bot.joinChannel("#denizen-dev");
        bot.joinChannel("#denizen-devs");
        
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (Channel chnl : bot.getChannels()) {
                    for (User usr : chnl.getUsers()) {
                        if (!dUsers.containsKey(usr.getNick()) && !usr.getNick().equals(bot.getNick()) && usr.getNick().length() > 0)
                            dUsers.put(usr.getNick(), new dUser(usr.getNick()));
                    }
                }
            }
          }, 2000);
    	
    	Scanner scanner = new Scanner(System.in);
    	String input = "";
    	
    	while (input.equals("cancel") == false) {
        	
    		input = scanner.nextLine().toLowerCase();
    		
    		if (input.startsWith("/me ")) {
    			String message = input.substring(4);
    			bot.sendAction("#denizen-dev", chatColor + message);
    		}
    		else if (input.startsWith("/msg ")) {
    			String user = input.substring(5).split(" ")[0];
    			String message = input.substring(5).split(user + " ")[1];
    			bot.sendMessage(user, message);
    		}
    		else if (input.startsWith(".plain ")) {
    			String message = input.substring(7, input.length());
    			bot.sendMessage("#denizen-dev", message);
    		}
    		else if (input.startsWith("/join ")) {
                String chnl = input.substring(6, input.length());
    		    bot.joinChannel(chnl);
    		}
    		else if (input.startsWith("/leave ")) {
                Channel chnl = bot.getChannel(input.substring(7, input.length()));
                bot.partChannel(chnl, "UNDER CONSTRUCTION");
    		}
    		else if (input.startsWith("/disconnect")) {
    		    for (dUser usr : dUsers.values()) {
    		        usr.saveAll();
    		    }
    		    bot.disconnect();
    		}
    		else {
    			bot.sendMessage("#denizen-dev", chatColor + input);
    		}
    	}
    	
    	scanner.close();
    }
	@Override
	public void onJoin(JoinEvent event) throws Exception {

	    dUsers.put(event.getUser().getNick(), new dUser(event.getUser().getNick()));
		bot.sendNotice(event.getUser(), chatColor + "Welcome to " + Colors.BOLD + event.getChannel().getName() + Colors.NORMAL + chatColor + ", home of the Denizen project. If you'd like help with anything, type " + Colors.BOLD + Colors.BLUE + ".help");
		bot.sendNotice(event.getUser(), chatColor + "If you are using Depenizen and would like help with that as well, let me know by typing " + Colors.BOLD + Colors.BLUE + ".depenizen");
	
	}
	
	@Override
	public void onQuit(QuitEvent event) throws Exception {
		dUsers.remove(event.getUser().getNick());
	}
	
	@Override
	public void onPart(PartEvent event) throws Exception {
        dUsers.remove(event.getUser().getNick());
	}
	
	@Override
	public void onDisconnect(DisconnectEvent event) throws Exception {
		// ...
	}
	
	@Override
	public void onPing(PingEvent event) throws Exception {
		// CTCP PING stuff... Not incredibly useful
	}
	
	@Override
	public void onNickChange(NickChangeEvent event) throws Exception {
		// ...
	}
	
	@Override
	public void onAction(ActionEvent event) throws Exception {
	    dUsers.get(event.getUser().getNick()).setLastSeen("performing an action: " + event.getAction() + ".");
	}
	
	@SuppressWarnings("unchecked")
    @Override
	public void onPrivateMessage(PrivateMessageEvent event) throws Exception {
	    onMessage(new MessageEvent(bot, null, event.getUser(), event.getMessage()));
	}
	
	@Override
    public void onMessage(MessageEvent event) throws Exception {
	    
        final Channel chnl = event.getChannel();
		
		if (usingResources) return;
		
		User usr = event.getUser();
		
		dUser dusr = null;
		if (!dUsers.containsKey(usr.getNick()))
		    dusr = new dUser(usr.getNick());
		else
		    dusr = dUsers.get(usr.getNick());
		
		String msg = event.getMessage();
		
		dusr.addMessage(msg);
		
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
		} else if (msg.equalsIgnoreCase(".dBuild")) {
		    bot.sendMessage("#denizen-dev", "Citizen: build Denizen");
		} else if (msgLwr.matches("\\.createissue\\s.+")) {
			if (confirmIssueUser != null && !confirmIssueUser.equals(senderNick)) {
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), "Back of the line, " + senderNick + "!");
				return;
			}
			if (!msg.substring(13).equalsIgnoreCase("confirm")) {
				if (confirmIssue != null) {
					bot.sendMessage((chnl != null ? chnl.getName() : senderNick), "You already have an issue waiting! Please type \".createIssue confirm\" to confirm it!");
					return;
				}
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Please confirm issue creation by using \".createIssue confirm\"");
				confirmIssue = repo.createIssue("Issue from " + senderNick + " at #denizen-dev").body(msg.substring(13));
				confirmIssueUser = senderNick;
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						if (confirmIssue == null) return;
						confirmIssue = null;
						bot.sendMessage((chnl != null ? chnl.getName() : senderNick), confirmIssueUser + ": your issue creation timed out!");
						confirmIssueUser = null;
					}
			 	  }, 30000);
			}
			else {
				if (confirmIssue == null) {
					bot.sendMessage((chnl != null ? chnl.getName() : senderNick), "Umm... There's no issues waiting to be confirmed... Maybe it timed out?");
					return;
				}
				try {
					GHIssue issue = confirmIssue.create();
					bot.sendMessage((chnl != null ? chnl.getName() : senderNick), "New issue opened: \"[" + issue.getNumber() + "] " + Colors.OLIVE + issue.getTitle() + chatColor + "\" by " + Colors.TEAL + issue.getUser().getLogin() + chatColor);
					openIssues++;
					openIssuesList.put(issue.getNumber(), issue);
				} catch (IOException e) {
					bot.sendMessage((chnl != null ? chnl.getName() : senderNick), "Issue creation failed! Please report this to someone with authority!");
					return;
				}
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), "Issue #" + repo.getIssues(GHIssueState.OPEN).get(0).getNumber() + " created!");
				confirmIssue = null;
				confirmIssueUser = null;
			}
		} else if (msgLwr.matches("\\.comments \\d+($|.+)")) {
			usingResources = true;
			String beginning = msg.split("\\d+")[0];
			String end = msg.split("\\d+", 2)[1];
			Integer number = Integer.valueOf(msg.substring(beginning.length(), msg.length() - end.length()));
			try {
				int messageCount = 0;
				GHIssue issue = repo.getIssue(number);
				if (issue.getCommentsCount() > 0) {
					String user = null;
					String currentUser = null;
					for (GHIssueComment comment : issue.getComments()) {
						user = null;
						String line2 = null;
						String line3 = null;
						boolean linePrinted = false;
						boolean line2Printed = false;
						boolean checked = false;
						for (String line : comment.getBody().split("\n")) {
							for (int i = 1; i <3; i++) {
								user = comment.getUser().getLogin();
								line = line.trim();
								if (line.startsWith(">") || line.matches("On.+wrote:") || line.isEmpty())
									continue;
								if (!checked && line.length() >= 400) {
									line2 = line.substring(400, line.length());
									if (line.length() >= 800) {
										line2 = line.substring(400, 800);
										line3 = line.substring(800, line.length());
									}
									line = line.substring(0, 400);
									checked = true;
								}
								bot.sendNotice(usr, (!user.equalsIgnoreCase(currentUser) ? user + ": " : "") + (linePrinted ? line2 : line2Printed ? line3 : line));
								messageCount++;
								currentUser = user;
								if (line2Printed) break;
								else if (line2 == null) break;
								if (!linePrinted) {
									linePrinted = true;
									continue;
								} else if (line3 == null) break;
								if (!line2Printed) line2Printed = true;
							}
							line2 = null;
							line3 = null;
							linePrinted = false;
							line2Printed = false;
						}
					}
				}
				else
					bot.sendMessage((chnl != null ? chnl.getName() : senderNick), "That issue has no comments.");
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						usingResources = false;
					}
				  }, messageCount*500);
				messageCount = 0;
			}
			catch (IOException e) {
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Issue #" + number + " doesn't seem to exist...");
				return;
			}
		} else if (msgLwr.startsWith(".comment confirm")) {
			if (!(hasOp(usr, chnl) || hasVoice(usr, chnl))) {
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Hey! You don't have permission to do that!");
				return;
			}
			else if (!confirmComment) {
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Umm... I don't see that comment on the list. Perhaps it timed out?");
				return;
			}
			else repo.getIssue(confirmingIssue).comment(confirmingComment);
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Comment confirmed!");
			confirmComment = false;
			confirmingIssue = 0;
			confirmingComment = null;
		} else if (msgLwr.matches("\\.comment \\d+\\s.+")) {
			
			if (!(hasOp(usr, chnl) || hasVoice(usr, chnl))) {
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Hey! You don't have permission to do that!");
				return;
			}

			String beginning = msg.split("\\d+")[0];
			String end = msg.split("\\d+\\s", 2)[1];
			Integer number = Integer.valueOf(msg.substring(beginning.length(), msg.length() - (end.length() + 1)));
			try {
				GHIssue issue = repo.getIssue(number);
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Please confirm comment on issue #" + number + " by using \".comment confirm\" (" + Colors.OLIVE + issue.getTitle() + chatColor + ")");
				confirmingComment = end + "\n\n(Sent from IRC channel #denizen-dev by " + usr.getNick() + ")";
				confirmingIssue = number;
				confirmComment = true;
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						if (confirmComment) {
							confirmComment = false;
							confirmingIssue = 0;
							confirmingComment = null;
							bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "your comment creation timed out!");
						}
					}
		           }, 30000);
			}
			catch (IOException e) {
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Issue #" + number + " doesn't seem to exist...");
				return;
			}
		} else if (msgLwr.matches("(^|.+)issues/\\d+($|.+)")) {
			String beginning = msg.split("/\\d+")[0];
			String end = msg.split("/\\d+", 2)[1];
			String number = msg.substring(beginning.length() + 1, msg.length() - end.length());
			try {
				GHIssue issue = (repo.getIssue(Integer.valueOf(number)) != null ? repo.getIssue(Integer.valueOf(number)) : null);
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + (issue.getState().equals(GHIssueState.OPEN) ? "Open issue: \"" : "Closed issue: \"") + Colors.OLIVE + issue.getTitle() + chatColor + "\" by " + Colors.TEAL + issue.getUser().getLogin() + chatColor + ". Last updated: " + issue.getUpdatedAt());
			} catch(IOException e) {
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Issue #" + number + " doesn't seem to exist...");
			}
			if (!msg.matches("(.*)github.com/.+/issues.+"))
					bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + Bitly.as("spazzmatic", System.getProperty("spazz.bitly")).call(Bitly.shorten("https://github.com/aufdemrand/Denizen/issues/" + number)).getShortUrl());
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
		    if (args.length > 1 && args[1].equals("debug")) reloadSites(true);
		    else reloadSites(false);
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
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Most errors can be fixed by updating all 3.");
			// bot.sendMessage((chnl != null ? chnl.getName() : senderNick), Colors.BOLD + "Denizen" + Colors.NORMAL + Colors.BLUE +  "- http://bit.ly/Wvvg8N");
			// bot.sendMessage((chnl != null ? chnl.getName() : senderNick), Colors.BOLD + "Citizens" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/Xe8YWZ");
			// bot.sendMessage((chnl != null ? chnl.getName() : senderNick), Colors.BOLD + "Craftbukkit" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/A5I50a");
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
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor +  "Here's every replaceable tag in Denizen! - http://bit.ly/1aaHhGs");
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
		        bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + args[1] + " -> " + Bitly.as("spazzmatic", System.getProperty("spazz.bitly")).call(Bitly.shorten(args[1])).getShortUrl());
		    }
		}
		
		else if (msgLwr.startsWith(".yaml") || msgLwr.startsWith(".yml")) {
			
			String[] args = msg.split(" ");
			if (args.length < 2) {
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Check your argument count. Command format: .yml <link>");
				return;
			}
			String rawYaml = null;
			if (args[1].contains("hastebin")) {
				String url[] = args[1].split("/");
				TextPage txt = HASTEBIN.getPage("http://hastebin.com/raw/"+url[3]);
				rawYaml = txt.getContent();
				HASTEBIN.closeAllWindows();	
			} else if (args[1].contains("pastebin")) {
				PASTEBIN.get(args[1]);
				WebElement pastedYaml = PASTEBIN.findElement(By.id("paste_code"));
				rawYaml = pastedYaml.getAttribute("value");
			} else if (args[1].contains("pastie")) {
				String url[] = args[1].split("/");
				HtmlPage page = PASTIE.getPage("http://pastie.org/pastes/" + url[3] + "/text");
				rawYaml = page.asText();
				PASTIE.closeAllWindows();				
			} else if (args[1].contains("ult-gaming")) {
				String url[] = args[1].split("/");
				HtmlPage page = PASTIE.getPage("http://paste.ult-gaming.com/" + url[3] + "?raw");
				rawYaml = page.asText();
				PASTIE.closeAllWindows();			
			} else {
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + Colors.RED + "I cant get your script from that website :(");
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
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Check your argument count. Command format: ." + shortened +" [<" + full + ">] (stability) (usage) (description) (example)");
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
                if (argList.contains("e")) {
                    extra += "e";
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
                usingResources = true;
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
                if (extra.contains("e")) {
                    String[] example = match.getExample().split("\n");
                    for (int i = 0; i < example.length; i++) {
                        if (i == 0) {
                            if (messageCount > 5) {
                                bot.sendNotice(usr, defaultColor + "  Example" + chatColor + ": " + example[i]);
                            }
                            else {
                                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), defaultColor + "  Example" + chatColor + ": " + example[i]);
                                messageCount++;
                            }
                        }
                        else {
                            if (messageCount > 5) {
                                bot.sendNotice(usr, chatColor + "           " + example[i]);
                                messageCount++;
                            }
                            else {
                                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "           " + example[i]);
                                messageCount++;
                            }
                        }
                    }
                }
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        usingResources = false;
                    }
                  }, (messageCount*400));
                return;
            }
            else {
                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + fullCap + " \"" + arg + "\" not found.");
                return;
            }
			
		} else if (msgLwr.startsWith(".events")) {
            List<dEvent> found = FindEvents("");
            String list = "";
            bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "I found " + defaultColor + found.size() + chatColor + " matches...");
            for (int x = 0; x < found.size();  x++) {
                list += found.get(x).getEvent().split(", ")[0] + ", ";
                if (x != 0 && x%20 == 0 && x+1 != x) {
                    bot.sendMessage((chnl != null ? chnl.getName() : senderNick), optionalColor + list);
                    list = "";
                }
            }
            list = list.substring(0, list.length()-2) + ".";
            bot.sendMessage((chnl != null ? chnl.getName() : senderNick), optionalColor + list);
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
			List<List<String>> quotes = new ArrayList<List<String>>();
	        quotes.add(0, Arrays.asList
	                        ("<davidcernat> I like to think of the Flag command as the two barons of hell bosses at the end of Doom 1's first episode.",
	                         "<davidcernat> And of the If command as the cyberdemon at the end of the second episode.",
	                         "<davidcernat> And of the Listen command as the spiderdemon at the end of the third episode."));
	        quotes.add(1, Arrays.asList
	        				("<Mr_Einsburgtengra> how do i bring my power up really quick?",
	        				 "<Fatal_Ink> masturbate"));
	        List<String> randomQuote = quotes.get(new Random().nextInt(quotes.size()));
	       
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
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), Colors.BOLD + "Denizen Wiki " + Colors.NORMAL + chatColor + "- http://bit.ly/13BwnUp");
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
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Hah! You'll never kill me...");
			return;
		} else if (msgLwr.startsWith(".issues")) {
			bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Open issues: " + (openIssues == 30 ? "30+" : openIssues));
			int arg = repo.getOpenIssueCount();
			try { arg = Integer.valueOf(msgLwr.replace(".issues ", "").split(" ")[0]); } catch(Exception e){}
			if (arg < 1) return;
			if (arg > repo.getOpenIssueCount()) arg = repo.getOpenIssueCount();
			for (int i = 0; i < arg; i++) {
				GHIssue issue = repo.getIssues(GHIssueState.OPEN).get(i);
				bot.sendNotice(usr, chatColor + "[" + issue.getNumber() + "] \"" + Colors.OLIVE + issue.getTitle() + chatColor + "\", opened by " + Colors.TEAL + issue.getUser().getLogin() + chatColor + ". Last updated: " + issue.getUpdatedAt());
			}
		} else if (msgLwr.startsWith(".issue")) {
			String[] args = msg.split(" ", 2);
			if (Integer.valueOf(msg.split(" ")[1]) != null) {
				try {
					GHIssue issue = repo.getIssue(Integer.valueOf(msg.split(" ")[1]));
					if (issue.getBody().length() > 9000) {
						bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Error: Body length can't be OVER 9000!!!!!");
						return;
					}
					bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Issue found: " + "[" + issue.getNumber() + "] \"" + Colors.OLIVE + issue.getTitle() + chatColor + "\", opened by " + Colors.TEAL + issue.getUser().getLogin() + chatColor + ". Last updated: " + issue.getUpdatedAt());
					boolean lastMessagePrinted = false;
					String body = issue.getBody().replace("\r", "").replace("\n", " ");
					for (int x = 1; x>0; x++) {
						int newInt = x*400;
						int start = newInt - 400;
						if (!lastMessagePrinted) {
							if (newInt > body.length()) {
								newInt = body.length();
								lastMessagePrinted = true;
							} else if (String.valueOf(body.charAt(newInt)) != " ") {
								for (int z = newInt; z<body.length(); z++) {
									if (String.valueOf(body.charAt(z)) == " ") {
										newInt = newInt + z;
										break;
									}
								}
							}
							bot.sendNotice((address != "" ? address.substring(0, address.length()-2) : senderNick), chatColor + body.substring(start, newInt));
							start=start+400;
						}
						else break;
					}
				}
				catch (IOException e) {
					bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Issue not found: #" + args[1]);
					return;
				}
			}
		} else if (msgLwr.startsWith(".close")) {
			if(!(hasVoice(usr, chnl) | hasOp(usr, chnl))) {
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Woah there! You can't do that!");
			}
			
			int arg = Integer.valueOf(msgLwr.replace(".close ", "").split(" ")[0]);
			GHIssue issue = repo.getIssue(arg);
			if (issue.getState().equals(GHIssueState.CLOSED)) {
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Uhh... Issue #" + arg + " is already closed.");
				return;
			} else {
				bot.sendMessage((chnl != null ? chnl.getName() : senderNick), address + chatColor + "Oops! This isn't implemented yet! Check back later!");
				// String args = msgLwr.split(String.valueOf(arg))[1].matches("\\.close \\d+.+") ? msgLwr.split(String.valueOf(arg))[1] : null;
				// issue.comment((args != null ? args + "\n\n" : "") + "(Sent from IRC channel #denizen-dev by " + usr.getNick() + ")");
				// issue.close();
			}
		}
		
		else if (msgLwr.startsWith(".depenizen")) {
		    dusr.invertDepenizen();
		    bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Depenizen mode set to " + defaultColor + dusr.getDepenizen() + chatColor + " for " + senderNick);
		}
		
		else if (msgLwr.startsWith(".seen")) {
		    String[] args = msg.split(" ");
		    String user = null;
		    if (args.length > 1)
		        user = args[1];
		    if (!dUsers.containsKey(user))
		        bot.sendMessage(chnl, chatColor + "I've never seen that user: " + defaultColor + user);
		    else
		        bot.sendMessage(chnl, chatColor + "The last time I saw " + defaultColor + user + chatColor + " was " + dUsers.get(user).getLastSeen());
		}
		
		else if (msgLwr.contains("the") && msgLwr.contains("owl") && msgLwr.contains("sleeps") && msgLwr.contains("at") && msgLwr.contains("day")) {
		    if (!usr.getRealName().equals("Morphan1")) {
		        if (usr.getRealName().equals("aufdemrand"))
		            bot.sendMessage(chnl, chatColor + "Nice try auf, but we all remember that time you forgot what Denizen was.");
		        else if (usr.getRealName().equals("mcmonkey"))
		            bot.sendMessage(chnl, chatColor + "Good try monkey minion, but none of you can bypass my security systems!");
		        else if (usr.getRealName().equals("Matterom"))
		            bot.sendMessage(chnl, chatColor + "Matt... Matt Matt Matt... When will you learn I am far more superior than that?");
		        else if (usr.getRealName().equals("davidcernat"))
		            bot.sendMessage(chnl, chatColor + "Oh no! Something might happen to someone somewhere at some point in time!");
		        else
		            bot.sendMessage(chnl, chatColor + "Ahaha, you can never kill me, " + senderNick + "!");
		    }
		    else {
		        String[] quotes = {"Ain't nobody got time for that...", "I'm backin' up, backin' up...", "Hide yo kids, hide yo wife..."};
		        bot.sendMessage(chnl, chatColor + quotes[new Random().nextInt(quotes.length)]);
		        bot.disconnect();
		    }
		}
		
		else if (msgLwr.startsWith(".save-all")) {
		    dusr.saveAll();
		}
		else if ((msg.contains("hastebin.") || msg.contains("pastebin.") || msg.contains("pastie.")) && !(help.contains(usr) || chnl.hasVoice(usr) || chnl.isOp(usr))) {
	        help.add(usr);
		    bot.sendNotice(usr, "If you want to whether a Denizen script will compile, type " + Colors.BOLD + ".yml link_to_the_script");
			return;
		} else if ((msgLwr.contains("help") || msgLwr.contains("halp") || msgLwr.contains("hlp")) && !(help.contains(usr) || chnl.hasVoice(usr) || chnl.isOp(usr))) {
		    help.add(usr);
			bot.sendNotice(usr, "If you need help with a Denizen issue, type " + Colors.BOLD + ".help");
			return;
		}
		
		else if (msgLwr.startsWith(".load")) {
		    dusr.loadMessages();
		}

		repo = github.getRepository("aufdemrand/denizen");
        int newOpenIssues = repo.getOpenIssueCount();
        if (newOpenIssues < openIssues) {
            List<GHIssue> closedIssuesList = repo.getIssues(GHIssueState.CLOSED);
            Collections.sort(closedIssuesList, new Comparator<GHIssue>() {
                public int compare(GHIssue issue1, GHIssue issue2) {
                    return issue2.getClosedAt().compareTo(issue1.getClosedAt());
                }
            });
            GHIssue issue = closedIssuesList.get(0);
            bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Issue resolved: \"[" + issue.getNumber() + "] " + Colors.OLIVE + issue.getTitle() + chatColor + "\"");
            openIssues = newOpenIssues;
        } else if (newOpenIssues > openIssues) {
            GHIssue issue = repo.getIssues(GHIssueState.OPEN).get(0);
            if (closedIssuesList.containsKey(issue.getNumber())) {
                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "Issue re-opened: [" + issue.getNumber() + "] " + Colors.OLIVE + issue.getTitle() + chatColor);
                closedIssuesList.remove(issue.getNumber());
                openIssuesList.put(issue.getNumber(), issue);
            }
            else {
                bot.sendMessage((chnl != null ? chnl.getName() : senderNick), chatColor + "New issue opened: \"[" + issue.getNumber() + "] " + Colors.OLIVE + issue.getTitle() + chatColor + "\" by " + Colors.TEAL + issue.getUser().getLogin() + chatColor);
                openIssuesList.put(issue.getNumber(), issue);
            }
            openIssues = newOpenIssues;
        }
        
	}

	private static void reloadSites(boolean debug) throws IOException {
	    boolean original = debugMode;
	    if (!debugMode)
	        debugMode = debug;
    	loadMeta();
    	debugMode = original;
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
                if (pre.toLowerCase().contains(root)) {
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
		private String nick;
		private boolean status;
		private ArrayList<String> messages;
		private ArrayList<Message> pMessages;
		private boolean depenizen;
		
		public dUser(String nick) {
		    if (nick == null || nick.equals("")) return;
		    this.nick = nick;
		    if (!new File(System.getProperty("user.dir") + "/users/" + nick + ".yml").exists()) {
		        setLastSeen("Existing.");
		        this.depenizen = false;
		        this.messages = new ArrayList<String>();
		        this.pMessages = new ArrayList<Message>();
		        
		        DumperOptions options = new DumperOptions();
		        options.setDefaultFlowStyle(FlowStyle.BLOCK);
		        Yaml yaml = new Yaml(options);
		        
		        Map<String, Object> data = new HashMap<String, Object>();
		        data.put("name", nick);
		        data.put("depenizen", getDepenizen());
		        data.put("lastseen", getLastSeen());
		        data.put("messages", "");
		        File f = new File(System.getProperty("user.dir") + "/users");
		        f.mkdirs();

		        try {
		            FileWriter writer = new FileWriter(f + "/" + nick + ".yml");
		            writer.write(yaml.dump(data));
		            writer.close();
		        } catch (IOException e) {
    		            e.printStackTrace();
                }
		        dUsers.put(nick, this);
		    }
		    else {
		        loadAll();
                dUsers.put(nick, this);
		    }
            
		}

		void setLastSeen(String lastSeen) {
			this.lastSeen = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss (zzz)").format(Calendar.getInstance().getTime()).replace("DT)", "ST)") + ", " + lastSeen.replaceFirst(lastSeen.substring(0, 1), lastSeen.substring(0, 1).toLowerCase());
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
		
		void addMessage(String msg) {
			this.messages.add(0, msg);
			setLastSeen("saying \"" + msg + "\".");
		}
		
		void addPrivMessage(Message message) {
			this.pMessages.add(message);
		}
		
		void saveAll() {
		    DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("name", getNick());
            data.put("depenizen", getDepenizen());
            data.put("lastseen", getLastSeen());
            data.put("messages", getMessages());
            File f = new File(System.getProperty("user.dir") + "/users");
            f.mkdirs();

            try {
                FileWriter writer = new FileWriter(f + "/" + nick + ".yml");
                writer.write(yaml.dump(data));
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
		}
		
        @SuppressWarnings("unchecked")
        void loadMessages() {
		    LinkedHashMap map = null;
            try {
                Yaml yaml = new Yaml();
                File f = new File(System.getProperty("user.dir") + "/users/" + getNick() + ".yml");
                f.mkdirs();
                InputStream is = f.toURI().toURL().openStream();
                map = (LinkedHashMap) yaml.load(is);
                if (map.get("messages") instanceof ArrayList<?>)
                    this.messages = (ArrayList<String>) map.get("messages");
            } catch (MalformedURLException e) { e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }
		}
		
		void loadPrivMessages() throws Exception {
			if (new File(System.getProperty("user.dir") + "/users/" + getNick() + "-priv.txt").exists()) {
				File f = new File(System.getProperty("user.dir") + "/users");
		        f.mkdirs();
		        
				Scanner scanner = new Scanner(new FileReader(f + "/" + getNick() + "-priv.txt"));
				int x = 0;
				while (scanner.hasNextLine()) {
					String[] line = scanner.nextLine().split(":", 2);
					getPrivMessages().add(x, new Message(line[0], line[1]));
					x++;
				}
				scanner.close();
			}
		}
		
		void loadLastSeen() {
		    LinkedHashMap map = null;
            try {
                Yaml yaml = new Yaml();
                File f = new File(System.getProperty("user.dir") + "/users/" + getNick() + ".yml");
                f.mkdirs();
                InputStream is = f.toURI().toURL().openStream();
                map = (LinkedHashMap) yaml.load(is);
                if (map.get("lastseen") instanceof String)
                    this.lastSeen = (String) map.get("lastseen");
            } catch (MalformedURLException e) { e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }
		}
		
		void checkPrivMessages() {
	    	for (Message message : getPrivMessages())
	    		bot.sendNotice(getNick(), chatColor + message.getUser() + ": " + message.getMessage());
	    	getPrivMessages().clear();
	    	if (new File(System.getProperty("user.dir") + "/users/" + getNick() + "-priv.txt").exists()
	    	&& !new File(System.getProperty("user.dir") + "/users/" + getNick() + "-priv.txt").delete())
	    			bot.sendNotice(getNick(), chatColor + "Error removing private messages. Please report this to a Denizen dev.");
		}
		
		void loadDepenizen() {
		    LinkedHashMap map = null;
            try {
                Yaml yaml = new Yaml();
                File f = new File(System.getProperty("user.dir") + "/users/" + getNick() + ".yml");
                f.mkdirs();
                InputStream is = f.toURI().toURL().openStream();
                map = (LinkedHashMap) yaml.load(is);
                if (map.get("depenizen") instanceof Boolean)
                    this.depenizen = (Boolean) map.get("depenizen");
            } catch (MalformedURLException e) { e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }
		}
		
		void loadAll() {
			loadMessages();
			loadLastSeen();
			loadDepenizen();
		}
		
		public String getLastSeen() {
			return this.lastSeen;
		}
	
		public String getNick() {
			return this.nick;
		}
		
		public boolean getStatus() {
			return this.status;
		}
		
		public ArrayList<String> getMessages() {
		    if (this.messages == null)
		        loadAll();
		    return this.messages;
		}
		
		public ArrayList<Message> getPrivMessages() {
			return this.pMessages;
		}
		
	}
	
    public static class MessageList {
	    
	    Map<String, String> messages = new HashMap<String, String>();
	    
	    public MessageList(ArrayList<Message> messages) {
	        for (Message message : messages) {
	            this.messages.put(message.getUser(), message.getMessage());
	        }
	    }
	    
	    public ArrayList<String> getMessages() {
	        ArrayList<String> ret = new ArrayList<String>();
	        ret.addAll(messages.values());
	        return ret;
	    }
	    
	}
	
	public static class Message {
		
		private String user;
		private String message;
		
		public Message(String user, String message) {
			this.user = user;
			this.message = message;
		}
		
		public String getUser() {
			return this.user;
		}
		
		public String getMessage() {
			return this.message;
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
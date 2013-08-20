package net.jeebiss.spazz;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.IOUtils;
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
import org.pircbotx.hooks.events.DisconnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.PingEvent;
import org.pircbotx.hooks.events.QuitEvent;
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
	
    static String getUrl(String url) throws IOException {
    	InputStream wp = new URL(url).openStream();
		String returns = IOUtils.toString(wp);
		wp.close();
		return returns;
    }
	
    String[] temp;
	static String chatColor = Colors.BLUE;
	String optionalColor = Colors.DARK_GREEN;
	String defaultColor = Colors.OLIVE;
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
	
	private static void loadTags() throws IOException {
		dTags.clear();
		String page = getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dPlayer.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/Duration.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dList.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dEntity.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dNPC.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dCuboid.java")
				+ getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/Element.java")
				+ getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dWorld.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dMaterial.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dLocation.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dColor.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dItem.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dScript.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/objects/dInventory.java")
                + getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/scripts/commands/core/YamlCommand.java")
				+ getUrl("https://raw.github.com/aufdemrand/Denizen/master/src/main/java/net/aufdemrand/denizen/tags/core/UtilTags.java");
		String[] split = page.replace("\r", "").split("\n");
        boolean intagdesc = false;
        List<String> descs = new ArrayList<String>();
        for (int i = 0; i < split.length; i++) {
            String curline = split[i].trim();
            if (curline.startsWith("// <--")) {
                descs.clear();
                intagdesc = true;
            }
            else if (curline.startsWith("// -->")) {
                if (descs.size() < 2) {
                    System.out.println("Bad desc at " + String.valueOf(i) + "!");
                    continue;
                }
                String[] splitdesc = descs.get(0).split(" -> ");
                String name = splitdesc[0].substring(splitdesc[0].indexOf('<') + 1, splitdesc[0].lastIndexOf('>'));
                String returns = splitdesc[1];
                String nname = "";
                boolean flip = false;
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
                System.out.println("Adding tag " + name + " AKA " + nname);
                String tagdesc = "";
                for (String desc : descs) {
                    tagdesc += desc + "\n";
                }
                dTags.add(new dTag(name, tagdesc.substring(0, tagdesc.length() - 1), nname, returns));
                intagdesc = false;
            }
            else if (curline.startsWith("// ")) {
                if (intagdesc) {
                    descs.add(curline.substring(2));
                }
            }
        }
    }
    
	public static void main(String[] args) throws Exception {
        
        github = GitHub.connectUsingPassword("spazzmatic", System.getProperty("spazz.password"));
        repo = github.getRepository("aufdemrand/Denizen");
        openIssues = repo.getOpenIssueCount();
        closedIssues = repo.getIssues(GHIssueState.CLOSED).size();
        for (GHIssue issue : repo.getIssues(GHIssueState.OPEN))
            openIssuesList.put(issue.getNumber(), issue);
        for (GHIssue issue : repo.getIssues(GHIssueState.CLOSED))
            closedIssuesList.put(issue.getNumber(), issue);
        
        reloadSites();
        
		bot.getListenerManager().addListener(new Spazz());
		/*
		 * Connect to #denizen-dev on start up
		 */
        bot.setVersion("SpazzChat v0.1 [x128] / Windows 10 [6.29GHz]");
        bot.setAutoReconnect(true);
        bot.setName("spazzmatic");
        bot.setLogin("spazz");
        bot.setVerbose(true);
        bot.setAutoNickChange(true);
        bot.identify(System.getProperty("spazz.password"));
        
        bot.connect("irc.esper.net");
		bot.setMessageDelay(0);
        bot.joinChannel("#denizen-dev");
        bot.joinChannel("#denizen-devs");
    	
    	Scanner scanner = new Scanner(System.in);
    	String input = "";
    	
    	while (input.equals("cancel") == false) {
        	
    		input = scanner.nextLine();
    		
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
    		else {
    			bot.sendMessage("#denizen-dev", chatColor + input);
    		}
    	}
    	
    	scanner.close();
    }
	@Override
	public void onJoin(JoinEvent event) throws Exception {

		bot.sendNotice(event.getUser(), "Welcome to " + Colors.BOLD + event.getChannel().getName() + Colors.NORMAL + ", home of the Denizen project. If you'd like help with anything, type " + Colors.BOLD + Colors.BLUE + ".help");
	
	}
	
	@Override
	public void onQuit(QuitEvent event) throws Exception {
		// ...
	}
	
	@Override
	public void onPart(PartEvent event) throws Exception {
		// ...
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
		//if (!users.containsKey(event.getOldNick()))
    	//	users.put(event.getOldNick(), new dUser(bot, event.getOldNick()));
		//if (!users.containsKey(event.getNewNick()))
    	//	users.put(event.getNewNick(), new dUser(bot, event.getNewNick()));
		//dUser oldUsr = users.get(event.getOldNick());
		//dUser newUsr = users.get(event.getNewNick());
		//for (Message message : users.get(event.getOldNick()).getPrivMessages())
		//	users.get(event.getNewNick()).addPrivMessage(message);
		//newUsr.setLastSeen("Changing nick (" + event.getOldNick() + " to " + event.getNewNick() + ")");
		//users.put(event.getNewNick(), oldUsr);
		
		//if (!new File(System.getProperty("user.dir") + "\\users\\" + event.getOldNick() + ".txt").renameTo(new File(System.getProperty("user.dir") + "\\users\\" + event.getNewNick() + ".txt")) && (new File(System.getProperty("user.dir") + "\\users\\" + event.getOldNick() + "-priv.txt").exists() && !new File(System.getProperty("user.dir") + "\\users\\" + event.getOldNick() + "-priv.txt").renameTo(new File(System.getProperty("user.dir") + "\\users\\" + event.getNewNick() + "-priv.txt"))))
		//	return;
		//else if (!new File(System.getProperty("user.dir") + "\\users\\" + event.getOldNick() + "-priv.txt").exists())
		//	return;
		//else
		//	bot.sendNotice(event.getNewNick(), "Internal error while changing nick. Please report this to a Denizen dev.");

		//users.get(event.getNewNick()).checkPrivMessages();
		
		//if (new File(System.getProperty("user.dir") + "lastseen.txt").exists()) {
		//	Scanner scanner = new Scanner(new FileReader("lastseen.txt"));
		//	while (scanner.hasNextLine()) {
		//		String[] line = scanner.nextLine().split(":", 2);
		//		if (line[0] == event.getUser().getNick()) {
		//			users.get(event.getUser().getNick()).setLastSeenRaw(line[2]);
		//		}
		//	}
		//	scanner.close();
		//}
		
        //for (User user : bot.getChannel(chnl).getUsers())
    	//	if (!users.containsKey(user.getNick())) {
        //		users.put(user.getNick(), new dUser(bot, user.getNick()));
        // 		users.get(user.getNick()).loadAll();
        //		users.get(user.getNick()).saveAll();
        //		System.out.println("New user registered: " + user.getNick());
    	//	}
	}
	
	@Override
    public void onMessage(MessageEvent event) throws Exception {
	    
        final Channel chnl = event.getChannel();
		
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
			bot.sendMessage(chnl, chatColor + "Issue resolved: \"[" + issue.getNumber() + "] " + Colors.OLIVE + issue.getTitle() + chatColor + "\"");
			openIssues = newOpenIssues;
		} else if (newOpenIssues > openIssues) {
			GHIssue issue = repo.getIssues(GHIssueState.OPEN).get(0);
			if (closedIssuesList.containsKey(issue.getNumber())) {
				bot.sendMessage(chnl, chatColor + "Issue re-opened: [" + issue.getNumber() + "] " + Colors.OLIVE + issue.getTitle() + chatColor);
				closedIssuesList.remove(issue.getNumber());
				openIssuesList.put(issue.getNumber(), issue);
			}
			else {
				bot.sendMessage(chnl, chatColor + "New issue opened: \"[" + issue.getNumber() + "] " + Colors.OLIVE + issue.getTitle() + chatColor + "\" by " + Colors.TEAL + issue.getUser().getLogin() + chatColor);
				openIssuesList.put(issue.getNumber(), issue);
			}
			openIssues = newOpenIssues;
		}
		
		if (usingResources) return;
		
		User usr = event.getUser();
		String msg = event.getMessage();
		
		String msgLwr = msg.toLowerCase();
		String senderNick = event.getUser().getNick();
		String address = "";
		
		if(charging) {
			if(System.currentTimeMillis() > (chargeInitiateTime + chargeFullTime + chargeFullTime/2)) {
				bot.sendAction(chnl, "loses control of his beams, accidentally making pew pew noises towards "+charger.getNick()+"!");
				charging=false;
				chargeInitiateTime=0;
				charger=null;
				botsnack=0;
				feeders.clear();
			}
				
		}
		
		if(msg.endsWith("@@")) {
			String args[] = msg.split(" ");
			System.out.println(args.length);
			for(int x=0; x<args.length; x++) {
				if(!args[x].contains("@@"))
					continue;
				else {
					address=args[x];
					address=address.substring(0,address.length()-2);
					address=address + ": ";
				}
			}
		}
		if (msg.equalsIgnoreCase(".hello")) {
			bot.sendMessage(chnl, address + "Hello World"); 
			return;
		} else if (msg.equalsIgnoreCase(".dBuild")) {
		    bot.sendMessage("#denizen-dev", "Citizen: build Denizen");
		} else if (msgLwr.matches("\\.createissue\\s.+")) {
			if (confirmIssueUser != null && !confirmIssueUser.equals(senderNick)) {
				bot.sendMessage(chnl, "Back of the line, " + senderNick + "!");
				return;
			}
			if (!msg.substring(13).equalsIgnoreCase("confirm")) {
				if (confirmIssue != null) {
					bot.sendMessage(chnl, "You already have an issue waiting! Please type \".createIssue confirm\" to confirm it!");
					return;
				}
				bot.sendMessage(chnl, chatColor + "Please confirm issue creation by using \".createIssue confirm\"");
				confirmIssue = repo.createIssue("Issue from " + senderNick + " at #denizen-dev").body(msg.substring(13));
				confirmIssueUser = senderNick;
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						if (confirmIssue == null) return;
						confirmIssue = null;
						bot.sendMessage(chnl, confirmIssueUser + ": your issue creation timed out!");
						confirmIssueUser = null;
					}
			 	  }, 30000);
			}
			else {
				if (confirmIssue == null) {
					bot.sendMessage(chnl, "Umm... There's no issues waiting to be confirmed... Maybe it timed out?");
					return;
				}
				try {
					GHIssue issue = confirmIssue.create();
					bot.sendMessage(chnl, "New issue opened: \"[" + issue.getNumber() + "] " + Colors.OLIVE + issue.getTitle() + chatColor + "\" by " + Colors.TEAL + issue.getUser().getLogin() + chatColor);
					openIssues++;
					openIssuesList.put(issue.getNumber(), issue);
				} catch (IOException e) {
					bot.sendMessage(chnl, "Issue creation failed! Please report this to someone with authority!");
					return;
				}
				bot.sendMessage(chnl, "Issue #" + repo.getIssues(GHIssueState.OPEN).get(0).getNumber() + " created!");
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
					bot.sendMessage(chnl, "That issue has no comments.");
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						usingResources = false;
					}
				  }, messageCount*500);
				messageCount = 0;
			}
			catch (IOException e) {
				bot.sendMessage(chnl, address + chatColor + "Issue #" + number + " doesn't seem to exist...");
				return;
			}
		} else if (msgLwr.startsWith(".comment confirm")) {
			if (!(hasOp(usr, chnl) || hasVoice(usr, chnl))) {
				bot.sendMessage(chnl, chatColor + "Hey! You don't have permission to do that!");
				return;
			}
			else if (!confirmComment) {
				bot.sendMessage(chnl, chatColor + "Umm... I don't see that comment on the list. Perhaps it timed out?");
				return;
			}
			else repo.getIssue(confirmingIssue).comment(confirmingComment);
			bot.sendMessage(chnl, address + chatColor + "Comment confirmed!");
			confirmComment = false;
			confirmingIssue = 0;
			confirmingComment = null;
		} else if (msgLwr.matches("\\.comment \\d+\\s.+")) {
			
			if (!(hasOp(usr, chnl) || hasVoice(usr, chnl))) {
				bot.sendMessage(chnl, chatColor + "Hey! You don't have permission to do that!");
				return;
			}

			String beginning = msg.split("\\d+")[0];
			String end = msg.split("\\d+\\s", 2)[1];
			Integer number = Integer.valueOf(msg.substring(beginning.length(), msg.length() - (end.length() + 1)));
			try {
				GHIssue issue = repo.getIssue(number);
				bot.sendMessage(chnl, chatColor + "Please confirm comment on issue #" + number + " by using \".comment confirm\" (" + Colors.OLIVE + issue.getTitle() + chatColor + ")");
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
							bot.sendMessage(chnl, chatColor + "your comment creation timed out!");
						}
					}
		           }, 30000);
			}
			catch (IOException e) {
				bot.sendMessage(chnl, address + chatColor + "Issue #" + number + " doesn't seem to exist...");
				return;
			}
		} else if (msgLwr.matches("(^|.+)issues/\\d+($|.+)")) {
			String beginning = msg.split("/\\d+")[0];
			String end = msg.split("/\\d+", 2)[1];
			String number = msg.substring(beginning.length() + 1, msg.length() - end.length());
			try {
				GHIssue issue = (repo.getIssue(Integer.valueOf(number)) != null ? repo.getIssue(Integer.valueOf(number)) : null);
				bot.sendMessage(chnl, address + chatColor + (issue.getState().equals(GHIssueState.OPEN) ? "Open issue: \"" : "Closed issue: \"") + Colors.OLIVE + issue.getTitle() + chatColor + "\" by " + Colors.TEAL + issue.getUser().getLogin() + chatColor + ". Last updated: " + issue.getUpdatedAt());
			} catch(IOException e) {
				bot.sendMessage(chnl, address + chatColor + "Issue #" + number + " doesn't seem to exist...");
			}
			if (!msg.matches("(.*)github.com/.+/issues.+"))
					bot.sendMessage(chnl, chatColor + Bitly.as("spazzmatic", System.getProperty("spazz.bitly")).call(Bitly.shorten("https://github.com/aufdemrand/Denizen/issues/" + number)).getShortUrl());
		} else if (msgLwr.startsWith(".kitty")) {
			bot.sendMessage(chnl, address + chatColor + "Meow.");
			return;
		} else if (msgLwr.startsWith(".color")) {
			
			if(!hasOp(usr, chnl) && !hasVoice(usr, chnl)) {
				bot.sendMessage(chnl, address + chatColor + "I'm sorry, but you do not have clearance to alter my photon colorization beam.");
				return;
			}
			
			String[] args = msg.split(" ");
			if(args.length > 3) {
				bot.sendMessage(chnl, address + chatColor + "I cannot read minds... yet. Hit me up with a bot-friendly color.");
				return;
			}
			String tempColor = parseColor(args[1]);
			if(tempColor == null) {
				bot.sendMessage(chnl, address + chatColor + "I eat " + args[1] + " for breakfast. That's not a color.");
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
			bot.sendMessage(chnl, address + chatColor  + "Photon colorization beam reconfigured "+ "[] " + optionalColor + "() " + defaultColor + "{}");
			return;
		} else if (msgLwr.startsWith(".botsnack")) {
			String args[] = msg.split(" ");
			
			if (feeders.toString().contains(usr.toString())) {
				bot.sendMessage(chnl, address + chatColor + "Thanks, but I can't have you controlling too much of my diet.");
				return;
			}
			
			if (args.length == 2) {
				if(chnl.getUsers().toString().contains(args[1])) 
					bot.sendMessage(chnl, address + chatColor + "Gluttony mode activated. Beginning " + args[1] + " consumption sequence.");
				else {
					ArrayList<User> users = new ArrayList<User>(chnl.getUsers());
					Random rand = new Random();
					User random = users.get(rand.nextInt(users.size()));
					bot.sendMessage(chnl, address + chatColor + "Oh no! " + args[1] + " not found, nomming "+random.getNick() + " instead.");
				}
				feeders.add(usr);
				botsnack++;
				return;
				
			} else {
				bot.sendMessage(chnl, address + chatColor +  "OM NOM NOM! I love botsnacks!");
				feeders.add(usr);
				botsnack++;
				return;
			}
		
		} else if (msg.equalsIgnoreCase(".reload")) {
			reloadSites();
			bot.sendMessage(chnl, address + "Reloaded websites. I have " + dTags.size() + " tags loaded.");
			return;
		} else if (msgLwr.startsWith(".repo")) {
			bot.sendMessage(chnl, address + chatColor + "Check out scripts made by other users! - http://bit.ly/16nRFvJ");
		} else if (msgLwr.startsWith(".materials") || msgLwr.startsWith(".mats")) {
			bot.sendMessage(chnl, address + chatColor + "Here is the list of all valid bukkit materials - http://bit.ly/X5smJK");
			bot.sendMessage(chnl, chatColor + "All Denizen 'item:' arguments will accept a bukkit material name. Additionally you can add the data value to the name. (i.e. SANDSTONE:1)");
			return;
		} else if (msgLwr.startsWith(".enchantments") || msgLwr.startsWith(".enchants")) {
			bot.sendMessage(chnl, address + chatColor + "Here is the list of all valid bukkit enchantments - http://bit.ly/YQ25ud");
			bot.sendMessage(chnl, chatColor + "They do not follow the same naming conventions as they do in game, so be carefull.");
			return;
		} else if (msgLwr.startsWith(".anchors") || msgLwr.startsWith(".anchor")) {
			bot.sendMessage(chnl, address + chatColor + "As of 0.8, locations can be referenced from scripts by using anchors linked to NPCs.");
			bot.sendMessage(chnl, chatColor + "Check out the documentation on the anchor commands in the handbook.");
			return;
		} else if (msgLwr.startsWith(".assignments") || msgLwr.startsWith(".assignment") || msgLwr.startsWith(".assign")) {
			bot.sendMessage(chnl, address + chatColor + "As of Denizen 0.8, the assignments.yml file is " + Colors.BOLD + "not " + Colors.NORMAL + chatColor + "necessary and the /trait command does "+Colors.BOLD + " not work.");
			bot.sendMessage(chnl, chatColor + "Instead, create the assignment script alongside interact scripts and assign it with:");
			bot.sendMessage(chnl, chatColor + Colors.BOLD + "- /npc assign --set 'assignment_script_name'");			
			bot.sendMessage(chnl, chatColor + "Check out an example of this new script's implementation at " + Colors.BLUE + "http://bit.ly/YiQ0hs");
			return;
		} else if (msgLwr.startsWith(".help")) {
			bot.sendMessage(chnl, address + chatColor + "Greetings. I am an interactive Denizen guide. I am a scripting guru. I am spazz.");
			bot.sendMessage(chnl, chatColor + "If you're a newbie to Denizen, type " + Colors.BOLD + ".getstarted");
			bot.sendMessage(chnl, chatColor + "For help with script commands, type " + Colors.BOLD + ".cmd <command_name>");
			bot.sendMessage(chnl, chatColor + "For help with script requirements, type " + Colors.BOLD + ".req <requirement_name>");
			bot.sendMessage(chnl, chatColor + "To open a new issue on GitHub, if you don't have an account, type " + Colors.BOLD + ".createIssue <issue_description>");
			bot.sendMessage(chnl, chatColor + "For everything else, ask in the channel or visit one of the links from .getstarted");
			return;
		} else if (msgLwr.startsWith(".paste") || msgLwr.startsWith(".pastie") || msgLwr.startsWith(".hastebin") || msgLwr.startsWith(".pastebin")) {
			bot.sendMessage(chnl, address + chatColor + "Need help with a script issue or server error?");
			bot.sendMessage(chnl, chatColor + "Help us help you by pasting your script " + Colors.BOLD + "and " + Colors.NORMAL + chatColor + "server log to " + Colors.BLUE + "http://hastebin.com");
			bot.sendMessage(chnl, chatColor + "From there, save the page and paste the link back in this channel.");
			return;
		} else if (msgLwr.startsWith(".update")) {
			bot.sendMessage(chnl, address + chatColor + "Due to the nature of our project, Denizen is always built against the " + Colors.RED +  "development" + chatColor +  " builds of Craftbukkit and Citizens.");
			bot.sendMessage(chnl, chatColor + "Most errors can be fixed by updating all 3.");
			bot.sendMessage(chnl, Colors.BOLD + "Denizen" + Colors.NORMAL + Colors.BLUE +  "- http://bit.ly/Wvvg8N");
			bot.sendMessage(chnl, Colors.BOLD + "Citizens" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/Xe8YWZ");
			bot.sendMessage(chnl, Colors.BOLD + "Craftbukkit" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/A5I50a");
			return;
		} else if (msgLwr.startsWith(".newconfig") || msgLwr.startsWith(".nc")) {
			bot.sendMessage(chnl, address + chatColor +  "If you are having issues with triggers not firing, you may be using the old config file.");
			bot.sendMessage(chnl, chatColor +  "You can easily generate a new one by deleteing your current config.yml file in the Denizen folder");
			return;
		} else if (msgLwr.startsWith(".wiki")) {
			bot.sendMessage(chnl, address + chatColor +  "The Denizen wiki is currently getting a makeover. This means that it doesn't currently have a lot of things.");
			bot.sendMessage(chnl, chatColor +  "Feel free to look at it anyway, though! http://bit.ly/13BwnUp");
			return;
		} else if (msgLwr.startsWith(".tags")) {
			bot.sendMessage(chnl, chatColor +  "Here's the current list of replaceable tags. (Incomplete)- http://bit.ly/11AQ4kr");
		} else if (msgLwr.startsWith(".tag ")) {
			String arg = msgLwr.split(" ")[1];
			List<intstr> found = FindTags(arg, null, null, null);
            List<StringBuilder> FoundInfo = new ArrayList<StringBuilder>();
            if (found.size() > 0) {
                FoundInfo.add(new StringBuilder());
                FoundInfo.get(0).append(chatColor);
                String nname = "";
                boolean flip = false;
                for (int f = 0; f < arg.length(); f++) {
                    if (arg.charAt(f) == '[') {
                        flip = true;
                    }
                    if (!flip) {
                        nname += String.valueOf(arg.charAt(f));
                    }
                    if (arg.charAt(f) == ']') {
                        flip = false;
                    }
                }
                if (nname.contains("@")) {
                    nname = nname.substring(nname.indexOf('@') + 1);
                }
                for (int i = 0; i < found.size(); i++) {
                    for (int x = i + 1; x < found.size(); x++) {
                        if (found.get(i).getStringValue().equalsIgnoreCase(found.get(x).getStringValue())) {
                            System.out.println(i + "-" + x + ",");
                            found.remove(x);
                            x--;
                        }
                    }
                }
                System.out.println("Count...");
                for (int i = 0; i < found.size(); i++) {
                    System.out.println(i + "-" + found.get(i).getIntValue() + "-" + found.get(i).getStringValue() + ",");
                    FoundInfo.get(FoundInfo.size() - 1).append(found.get(i).getStringValue() + ", ");
                    if (FoundInfo.get(FoundInfo.size() - 1).length() > 400) {
                        FoundInfo.add(new StringBuilder());
                        FoundInfo.get(FoundInfo.size() - 1).append(chatColor);
                    }
                    if (i >= 50) {
                        break;
                    }
                    if (found.get(i).getStringValueA().equalsIgnoreCase(nname) || found.get(i).getStringValue().equalsIgnoreCase(arg) || found.get(i).getStringValueB().equalsIgnoreCase(nname)) {
                        intstr isa = found.get(i);
                        found.clear();
                        found.add(isa);
                        break;
                    }
                }
            }
            if (found.size() == 0) {
                bot.sendMessage(chnl, address + chatColor + "No matches found. Are you sure it exists?");
            }
            else if (found.size() == 1) {
                System.out.println("Found 1 " + found.get(0).getIntValue() + " is " + found.get(0).getStringValue());
                bot.sendMessage(chnl, address + chatColor + "Oh look! A matching tag: " + found.get(0).getStringValue());
                String[] founddesc = dTags.get(found.get(0).getIntValue()).getDesc().split("\n");
                for (int x = 1; x < founddesc.length; x++) {
                    bot.sendMessage(chnl, address + chatColor + "  " + founddesc[x]);
                }
            }
            else {
                if (FoundInfo.get(FoundInfo.size() - 1).length() < 5) {
                    FoundInfo.remove(FoundInfo.size() - 1);
                }
                FoundInfo.set(FoundInfo.size() - 1, new StringBuilder(FoundInfo.get(FoundInfo.size() - 1).toString().substring(0, FoundInfo.get(FoundInfo.size() - 1).length() - 2) + "."));
                bot.sendMessage(chnl, address + chatColor + "I found " + optionalColor + found.size() + (found.size() == 50?"+":"") + chatColor + " matches...");
                boolean flipp = false;
                for (StringBuilder match : FoundInfo) {
                    if (!flipp && found.size() < 10) {
                        bot.sendMessage(chnl, match.toString());
                        flipp = true;
                    }
                    else {
                        bot.sendNotice(usr, match.toString());
                    }
                }
            }
		}
		else if (msgLwr.startsWith(".effects") || msgLwr.startsWith(".potions")) {
			bot.sendMessage(chnl, chatColor + "A list of Bukkit potion effects is available here " + Colors.BOLD + "- http://bit.ly/13xyXur");
		}
		
		else if (msgLwr.startsWith(".yaml") || msgLwr.startsWith(".yml")) {
			
			String[] args = msg.split(" ");
			if (args.length < 2) {
				bot.sendMessage(chnl, address + chatColor + "Check your argument count. Command format: .yml <link>");
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
				bot.sendMessage(chnl, address + Colors.RED + "I cant get your script from that website :(");
			}
			
			Yaml yaml = new Yaml();
			try {
				yaml.load(rawYaml);
				bot.sendMessage(chnl, address + chatColor + "Your YAML is valid.");
			} catch (YAMLException e) {
				String fullStack =  getCustomStackTrace(e);
				String[] stackList = fullStack.split("\\n");
				int x = 0;
				while (!stackList[x].contains("org.yaml")) {
					bot.sendMessage(chnl, address + stackList[x]);
					x++;
				}
			}
		} else if (msgLwr.startsWith(".cmds") || msgLwr.startsWith(".commands")) {
			bot.sendMessage(chnl, address + chatColor + "Here's a list of every command in Denizen.- http://bit.ly/12v6zs9");
		} else if (msgLwr.startsWith(".cmd") || msgLwr.startsWith(".command")) {
			String [] args = msg.split(" ");
			if (args.length < 2) {
				bot.sendMessage(chnl, address + chatColor + "Check your argument count. Command format: .cmd <command>");
				return;
			}
			String command = args[1].toLowerCase();
				
			int x = 59;
			boolean done = false;
			while (!done) {
				try {
					WebElement usage = GHCR.findElement(By.xpath("//*[@id=\"LC" + x + "\"]/span[1]"));
					if (usage.getText().startsWith("// STOP")) {
						done = true;
					}
					String oldcommandname = usage.getText().replace("\"", "");
					String[] commandnames = oldcommandname.split(", ");
					System.out.println(oldcommandname);
					if (command.equalsIgnoreCase("SANTACLAUS")) {
						bot.sendMessage(chnl, address + chatColor + "Usage: - santaclaus (location:<location>) (presents:<item>|...) (reindeers:<entity>|...) (speed:<#.#>)");
						return;
					}
					else for(int i =0; i < commandnames.length; i++) {
						String commandname = commandnames[i];
						if(commandname.equalsIgnoreCase(command.toUpperCase())) {
							usage = GHCR.findElement(By.xpath("//*[@id=\"LC" + x + "\"]/span[3]"));
							String unparsed = usage.getText();
							if (!unparsed.contains(commandnames[0].toLowerCase() + " ")) {
								bot.sendMessage(chnl, address + chatColor + "Usage: - " + command.toLowerCase());
								return;
							}
							String unparsedfinal = unparsed.substring(commandnames[0].length()+1, unparsed.length());
							String formatted = parseUsage(unparsedfinal.substring(1, unparsedfinal.length() - 1));
							bot.sendMessage(chnl, address + chatColor + "Usage: - " + command.toLowerCase() + " " + formatted);
							return;
				        }
				    }
					x = x + 3;
				} catch (Exception e) { e.printStackTrace(); done = true; System.out.println("done."); }
			}
			
			bot.sendMessage(chnl, address + chatColor + "The command '" + command + "' does not exist. If you think it should, feel free to suggest it to a developer.");
			return;
			
		} else if (msgLwr.startsWith(".req") || msgLwr.startsWith(".requirement")) {
			String [] args = msg.split(" ");
			if (args.length < 2) {
				bot.sendMessage(chnl, address + chatColor + "Check your argument count. Command format: .req <requirement>");
				return;
			}
			String requirement = args[1].toLowerCase();
			
			int x = 61;
			boolean done = false;
			while (!done) {
				try {
					WebElement usage = GHRR.findElement(By.xpath("//*[@id=\"LC" + x + "\"]/span[1]"));
					String requirementname = usage.getText();
					System.out.println(requirementname);
					if (requirementname.substring(1, requirementname.length()-1).equalsIgnoreCase(requirement.toUpperCase())) {
						usage = GHRR.findElement(By.xpath("//*[@id=\"LC" + x + "\"]/span[3]"));
						String unparsed = usage.getText();
						String formatted = parseUsage(unparsed.substring(1, unparsed.length() - 1));
						bot.sendMessage(chnl, address + chatColor + "Usage: - " + formatted);
						return;
					}
					x = x + 3;
				} catch (Exception e) { done = true; System.out.println("done."); }
			}
			
			bot.sendMessage(chnl, address + chatColor + "The requirement '" + requirement + "' does not exist. If you think it should, feel free to suggest it to a developer.");
			return;
			
		} else if (msgLwr.startsWith(".quote")) {
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
	                bot.sendMessage(chnl, address + chatColor + line);
	        }
		} else if (msgLwr.startsWith(".party") || msgLwr.startsWith(".celebrate")) {
			if (msgLwr.contains("reason: ")) {
				String[] split = msg.split("reason:");
				String reason = split[1].replace(" me ", senderNick + " ");
				bot.sendMessage(chnl, address + chatColor + "Woo! Let's party for " + reason.substring(1, reason.length()) + "!");
				return;
			}
			bot.sendMessage(chnl, address + chatColor + "Woo! It's party time! Come on, celebrate with me!");
			return;
		} else if (msgLwr.startsWith(".blame")) {
			String[] args = msg.split(" ");
			if (args.length < 3) {
				bot.sendMessage(chnl, address + chatColor + "Check your argument count. Command format: .blame <user> <reason>");
				return;
			}
			String blamed = args[1];
			args = msg.split(blamed);
			String reason = args[1];
			bot.sendMessage(chnl, address + chatColor + senderNick + " blames " + blamed + " for" + reason + "!");
		} else if (msgLwr.startsWith(".yaii")) {
			bot.sendMessage(chnl, address + chatColor + "Your argument is invalid.");
			return;
		} else if (msgLwr.startsWith(".thmf") || msgLwr.startsWith(".tfw")) {
			bot.sendMessage(chnl, address + chatColor + "That hurt even my feelings. And I'm a robot.");
		} else if (msgLwr.startsWith(".cb") || msgLwr.startsWith(".coolbeans")) {
			bot.sendMessage(chnl, address + chatColor + "That's cool beans.");
			return;	
		} else if (msgLwr.equals(".sound") || msgLwr.equals(".sounds")) {
			bot.sendMessage(chnl, address + chatColor + "Here is the list of all valid bukkit sounds - "+ Colors.BLUE + "http://bit.ly/14NYbvi");
		} else if (msgLwr.startsWith(".hb") || msgLwr.startsWith(".handbook")) {
			bot.sendMessage(chnl, address + chatColor + "Current Documentation - "+ Colors.BLUE + "http://bit.ly/XaWBLN");
			bot.sendMessage(chnl, chatColor + "PDF download (always current) - "+ Colors.BLUE + "http://bit.ly/159JBgM");
			return;	
		} else if (msgLwr.startsWith(".getstarted") || msgLwr.startsWith(".gs")) {
			bot.sendMessage(chnl, address + chatColor + "So, you're trying to use 0.9 for the first time?");
			bot.sendMessage(chnl, chatColor + "It's recommended that you read the current documentation.");
			bot.sendMessage(chnl, Colors.BOLD + "Denizen Handbook " + Colors.NORMAL + chatColor + "- http://bit.ly/XaWBLN");
			bot.sendMessage(chnl, Colors.BOLD + "Denizen Wiki " + Colors.NORMAL + chatColor + "- http://bit.ly/13BwnUp");
			bot.sendMessage(chnl, Colors.BOLD + "Beginner's Guide" + Colors.NORMAL + chatColor + "- http://bit.ly/1bHkByR");
			bot.sendMessage(chnl, chatColor + "Please keep in mind that documentation is a work in progress. You will likely not find everything.");
			return;	
		} else if (msgLwr.startsWith(".fire")) {
			if(hasOp(usr, chnl) || hasVoice(usr, chnl)) {
				String args[] = msg.split(" ");
				if(!charging) {
					bot.sendMessage(chnl, address + chatColor + "Erm... was I supposed to be charging? D:");
					return;
				}
				if(usr != charger) {
					bot.sendMessage(chnl, address + chatColor + "Sorry, but my firing sequence has already been started by "+charger.getNick()+".");
					return;
				}
				if(args.length != 2) {
					bot.sendMessage(chnl, address + chatColor + "I can't just fire into thin air :(");
					return;
				}
				if(chnl.getUsers().toString().contains(args[1])) {
					double chance = (Math.random() *99 + 1) * ((System.currentTimeMillis()-chargeInitiateTime)/chargeFullTime);
					if(chance > 50) {
						bot.sendAction(chnl, chatColor + "makes pew pew noises towards "+ args[1] + "... successfully!");
						bot.sendMessage(chnl, chatColor + "Take that " + args[1] + "!");
					} else {
						bot.sendAction(chnl, chatColor + "makes pew pew noises towards "+ args[1] + "... and misses D:");
						bot.sendMessage(chnl, chatColor + "You've bested me this time " + args[1] + "...");
					}
					charging=false;
					chargeInitiateTime=0;
					charger=null;
					feeders.clear();
					return;
				} else {
					bot.sendMessage(chnl, chatColor + args[1] + ", really? I need a legitimate target. This is serious business.");
					return;
				}
			}
			
			bot.sendMessage(chnl, chatColor + "Whoa there, you can't touch that button.");
			return;
		
		} else if (msgLwr.startsWith(".lzrbms") || msgLwr.startsWith(".lzrbmz")) {
			if(hasOp(usr, chnl) || hasVoice(usr, chnl)) {
				if (botsnack < 3) { 
					bot.sendMessage(chnl, address + chatColor +": Botsnack levels too low. Can't charge lazers...");
					return;
				}
				
				if(charging) {
					bot.sendMessage(chnl, address + chatColor +": I'm already a bit occupied here!");
					return;
				}
				
				chargeInitiateTime=System.currentTimeMillis();
				charging=true;
				charger=usr;
				bot.sendMessage(chnl, address + chatColor + "Imma chargin' up meh lazerbeamz...");
				botsnack-=3;
				return;
			}
			
			bot.sendMessage(chnl, address + chatColor + "Umm, that's not for you.");
			return;
		
		} else if (msg.equalsIgnoreCase(".bye")) {
			bot.sendMessage(chnl, address + chatColor + "Hah! You'll never kill me...");
			return;
		} else if (msgLwr.startsWith(".issues")) {
			bot.sendMessage(chnl, address + chatColor + "Open issues: " + (openIssues == 30 ? "30+" : openIssues));
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
						bot.sendMessage(chnl, chatColor + "Error: Body length can't be OVER 9000!!!!!");
						return;
					}
					bot.sendMessage(chnl, chatColor + "Issue found: " + "[" + issue.getNumber() + "] \"" + Colors.OLIVE + issue.getTitle() + chatColor + "\", opened by " + Colors.TEAL + issue.getUser().getLogin() + chatColor + ". Last updated: " + issue.getUpdatedAt());
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
					bot.sendMessage(chnl, address + chatColor + "Issue not found: #" + args[1]);
					return;
				}
			}
		} else if (msgLwr.startsWith(".close")) {
			if(!(hasVoice(usr, chnl) | hasOp(usr, chnl))) {
				bot.sendMessage(chnl, address + chatColor + "Woah there! You can't do that!");
			}
			
			int arg = Integer.valueOf(msgLwr.replace(".close ", "").split(" ")[0]);
			GHIssue issue = repo.getIssue(arg);
			if (issue.getState().equals(GHIssueState.CLOSED)) {
				bot.sendMessage(chnl, address + chatColor + "Uhh... Issue #" + arg + " is already closed.");
				return;
			} else {
				bot.sendMessage(chnl, address + chatColor + "Oops! This isn't implemented yet! Check back later!");
				// String args = msgLwr.split(String.valueOf(arg))[1].matches("\\.close \\d+.+") ? msgLwr.split(String.valueOf(arg))[1] : null;
				// issue.comment((args != null ? args + "\n\n" : "") + "(Sent from IRC channel #denizen-dev by " + usr.getNick() + ")");
				// issue.close();
			}
		}
		// else if (msgLwr.startsWith(".save-all")) {
		//	for (User user : users.values())
		//		users.get(user.getNick()).saveAll();
		// } else if (msgLwr.startsWith(".seen ")) {
		//	String[] args = msg.split(" ");
		//	if (users.containsKey(args[1]))
		//		bot.sendMessage(chnl, address + chatColor + "The last time I last saw " + args[1] + " was " + users.get(args[1]).getLastSeen());
		//	else
		//		bot.sendMessage(chnl, address + chatColor + "I've never seen that user.");
		//} else if (msgLwr.startsWith(".send")
		//		|| msgLwr.startsWith(".msg")
		//		|| msgLwr.startsWith(".pm")) {
		//	String[] args = msg.split(" ");
		//	if (users.containsKey(args[1])) {
		//		bot.sendMessage(chnl, address + chatColor + "Your message will be sent to " + args[1] + " ASAP.");
		//		users.get(args[1]).addPrivMessage(new Message(usr.getNick(), msg.substring(args[0].length() + args[1].length() + 2)));
		//	}
		//	else
		//		bot.sendMessage(chnl, address + chatColor + "I've never seen that user.");
		//} 
		else if ((msg.contains("hastebin.") || msg.contains("pastebin.") || msg.contains("pastie.")) && !(help.contains(usr) || chnl.hasVoice(usr) || chnl.isOp(usr))) {
	        help.add(usr);
		    bot.sendNotice(usr, "If you want to whether a Denizen script will compile, type " + Colors.BOLD + ".yml link_to_the_script");
			return;
		} else if ((msgLwr.contains("help") || msgLwr.contains("halp") || msgLwr.contains("hlp")) && !(help.contains(usr) || chnl.hasVoice(usr) || chnl.isOp(usr))) {
		    help.add(usr);
			bot.sendNotice(usr, "If you need help with a Denizen issue, type " + Colors.BOLD + ".help");
			return;
		}
	}

	private static void reloadSites() throws IOException {
		GHCR.get("https://github.com/aufdemrand/Denizen/blob/master/src/main/java/net/aufdemrand/denizen/scripts/commands/CommandRegistry.java");
    	GHRR.get("https://github.com/aufdemrand/Denizen/blob/master/src/main/java/net/aufdemrand/denizen/scripts/requirements/RequirementRegistry.java");
    	loadTags();
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
	
	private String parseUsage(String unparsed) {
		
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
		//System.out.println("Beginning " +formatted);

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
			//System.out.println("Required Loop before change " +formatted);
			//System.out.println("First half " + formatted.substring(0, requiredIndex + 1));
			//System.out.println("Second half " + formatted.substring(requiredIndex + 1));

			formatted = formatted.substring(0, requiredIndex + 1) + beforeColor + formatted.substring(requiredIndex + 1);
			requiredIndex = formatted.indexOf("]",requiredIndex + 1);
			//System.out.println("Required Loop after change " +formatted);

		}
		//System.out.println("After Req Loop " + formatted);

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
			//System.out.println("Optional Loop before change " + formatted);

			formatted = formatted.substring(0, optionalIndex + 1) + beforeColor + formatted.substring(optionalIndex + 1);
			optionalIndex = formatted.indexOf(")",optionalIndex + 1);
			//System.out.println("Optional Loop after change " + formatted);

		}
		//System.out.println("After OL " + formatted);

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
			//System.out.println("DL before change " + formatted);

			formatted = formatted.substring(0, defaultIndex + 1) + beforeColor + formatted.substring(defaultIndex + 1);
			defaultIndex = formatted.indexOf("}",defaultIndex + 1);
			//System.out.println("DL after change " + formatted);

		}
		

		//System.out.println("Final " + formatted);
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
	
	static List<intstr> FindTags(String input, Integer layers, List<intstr> toret, String replacer) {
		if (layers == null) layers = 0;
        System.out.println("Finding " + input);
        if (toret == null) {
            toret = new ArrayList<intstr>();
        }
        String nname = "";
        boolean flip = false;
        if (layers > 50) {
            System.out.println("Overloaded!");
            return toret;
        }
        for (int f = 0; f < input.length(); f++) {
            if (input.charAt(f) == '[') {
                flip = true;
            }
            if (!flip) {
                nname += String.valueOf(input.charAt(f));
            }
            if (input.charAt(f) == ']') {
                flip = false;
            }
        }
        if (nname.contains("@")) {
            nname = nname.substring(nname.indexOf('@') + 1);
        }
        for (int i = 0; i < dTags.size(); i++) {
        	dTag tag = dTags.get(i);
            if (tag.getName().contains(input) || tag.getAlt().contains(nname)) {
                if (replacer == null) {
                    toret.add(new intstr(i, tag.getName(), tag.getAlt(), tag.getAlt()));
                }
                else {
                    toret.add(new intstr(i,
                        replacer + tag.getName().substring(tag.getName().indexOf('.')),
                        tag.getAlt(),
                        replacer + tag.getAlt().substring(tag.getAlt().indexOf('.'))));
                }
                if (toret.size() >= 50) {
                    return toret;
                }
            }
        }
        if (nname.contains(".")) {
            String ts;
            if (replacer == null) {
                ts = nname;
            }
            else {
                if (nname.indexOf('.') + 1 < nname.length()) {
                    ts = replacer + nname.substring(nname.indexOf('.') + 1);
                }
                else {
                    ts = nname;
                }
            }
            String[] split = ts.split(".");
            for (int i = 0; i < split.length; i++) {
                String cat = concat(split, i);
                if (cat.length() == 0) {
                    continue;
                }
                cat = cat.substring(0, cat.length()-1);
                List<intstr> found = FindTags(cat, layers, null, null);
                System.out.println(cat + " returns " + found.size());
                for (int x = 0; x < found.size(); x++) {
                    if (found.get(x).getStringValue().equalsIgnoreCase(cat)) {
                        String ret = dTags.get(found.get(x).getIntValue()).getReturn();
                        System.out.println("Ret: " + ret);
                        if (ret.toLowerCase().contains("dlocation")) {
                            FindTags("location" + nname.substring(cat.length()), layers, toret, cat);
                        }
                        else if (ret.toLowerCase().contains("dplayer")) {
                            FindTags("player" + nname.substring(cat.length()), layers, toret, cat);
                        }
                        else if (ret.toLowerCase().contains("dlist")) {
                            FindTags("list" + nname.substring(cat.length()), layers, toret, cat);
                        }
                        else if (ret.toLowerCase().contains("dentity")) {
                            FindTags("entity" + nname.substring(cat.length()), layers, toret, cat);
                        }
                        else if (ret.toLowerCase().contains("dnpc")) {
                            FindTags("npc" + nname.substring(cat.length()), layers, toret, cat);
                        }
                        else if (ret.toLowerCase().contains("dinventory")) {
                            FindTags("inventory" + nname.substring(cat.length()), layers, toret, cat);
                        }
                        break;
                    }
                }
            }
            if (nname.toLowerCase().startsWith("player.") && nname.length() > 7) {
                String asentity = "entity." + nname.substring(7);
                FindTags(asentity, layers, toret, "player");
            }

            if (nname.toLowerCase().startsWith("npc.") && nname.length() > 4) {
                String asentity = "entity." + nname.substring(4);
                FindTags(asentity, layers, toret, "npc");
            }
        }
        return toret;
    }
	
	public static class intstr {
        public int intvalue;
        public String stringValue;
        public String stringValuea;
        public String stringValueb;
        public intstr(int ia, String sa, String sva, String svb) {
            intvalue = ia;
            stringValue = sa;
            stringValuea = sva;
            stringValueb = svb;
        }
        
        public int getIntValue() {
        	return intvalue;
        }
        public String getStringValue() {
        	return stringValue;
        }
        public String getStringValueA() {
        	return stringValuea;
        }
        public String getStringValueB() {
        	return stringValueb;
        }
    }
	
    static String concat(List<String> strs, int start) {
        StringBuilder toret = new StringBuilder();
        for (int i = start; i < strs.size(); i++) {
            toret.append(strs.get(i));
            if (i + 1 < strs.size()) {
                toret.append(" ");
            }
        }
        return toret.toString();
    }
	
	static String concat(String[] input, int upto) {
        String toret = "";
        for (int i = 0; i < upto; i++) {
            toret += input[i] + ".";
        }
        return toret;
    }
	
	public static class dUser extends org.pircbotx.User {
		
		private String lastSeen;
		private int id;
		private boolean status;
		private ArrayList<Message> messages;
		private ArrayList<Message> pMessages;
		
		public dUser(PircBotX bot, String nick) {
			super(bot, nick);
			this.messages = new ArrayList<Message>();
			this.pMessages = new ArrayList<Message>();
		}

		void setLastSeen(String lastSeen) {
			this.lastSeen = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss (zzz)").format(Calendar.getInstance().getTime()).replace("DT)", "ST)") + ", " + lastSeen.replaceFirst(lastSeen.substring(0, 1), lastSeen.substring(0, 1).toLowerCase());
		}
		
		void setLastSeenRaw(String lastSeen) {
			this.lastSeen = lastSeen;
		}
		
		void setId(int id) {
			this.id = id;
		}
		
		void setStatus(boolean status) {
			this.status = status;
		}
		
		void addMessage(Message message) {
			this.messages.add(0, message);
		}
		
		void addPrivMessage(Message message) {
			this.pMessages.add(message);
		}
		
		void saveAll() throws Exception {
			if (getMessages().size() > 0) {
				final String NEW_LINE = System.getProperty("line.separator");
			
				File f = new File(System.getProperty("user.dir") + "\\users");
		        f.mkdirs();
		        
				FileWriter writer = new FileWriter(f + "\\" + getNick() + ".txt");
				for (Message message : getMessages())
					writer.write(message.getUser() + ": " + message.getMessage() + NEW_LINE);
				writer.close();
			}
			if (getPrivMessages().size() > 0) {
				final String NEW_LINE = System.getProperty("line.separator");
				
				File f = new File(System.getProperty("user.dir") + "\\users");
		        f.mkdirs();
		        
				FileWriter writer = new FileWriter(f + "\\" + getNick() + "-priv.txt");
				for (Message message : getPrivMessages())
					writer.write(message.getUser() + ": " + message.getMessage() + NEW_LINE);
				writer.close();
			}
		}
		
		void loadMessages() throws Exception {
			if (new File(System.getProperty("user.dir") + "\\users\\" + getNick() + ".txt").exists()) {
				File f = new File(System.getProperty("user.dir") + "\\users");
		        f.mkdirs();
		        
				Scanner scanner = new Scanner(new FileReader(f + "\\" + getNick() + ".txt"));
				int x = 0;
				while (scanner.hasNextLine()) {
					getMessages().add(x, new Message(getNick(), scanner.nextLine().substring(getNick().length() + 2)));
				}
				scanner.close();
			}
		}
		
		void loadPrivMessages() throws Exception {
			if (new File(System.getProperty("user.dir") + "\\users\\" + getNick() + "-priv.txt").exists()) {
				File f = new File(System.getProperty("user.dir") + "\\users");
		        f.mkdirs();
		        
				Scanner scanner = new Scanner(new FileReader(f + "\\" + getNick() + "-priv.txt"));
				int x = 0;
				while (scanner.hasNextLine()) {
					String[] line = scanner.nextLine().split(":", 2);
					getPrivMessages().add(x, new Message(line[0], line[1]));
					x++;
				}
				scanner.close();
			}
		}
		
		void loadLastSeen() throws Exception {
			if (new File(System.getProperty("user.dir") + "lastseen.txt").exists()) {
				Scanner scanner = new Scanner(new FileReader("lastseen.txt"));
				while (scanner.hasNextLine()) {
					String[] line = scanner.nextLine().split(":", 2);
					if (line[0] == getNick()) {
						setLastSeenRaw(line[2]);
					}
				}
				scanner.close();
			}
		}
		
		void checkPrivMessages() {
	    	for (Message message : getPrivMessages())
	    		bot.sendNotice(getNick(), chatColor + message.getUser() + ": " + message.getMessage());
	    	getPrivMessages().clear();
	    	if (new File(System.getProperty("user.dir") + "\\users\\" + getNick() + "-priv.txt").exists()
	    	&& !new File(System.getProperty("user.dir") + "\\users\\" + getNick() + "-priv.txt").delete())
	    			bot.sendNotice(getNick(), chatColor + "Error removing private messages. Please report this to a Denizen dev.");
		}
		
		void loadAll() {
			try {loadMessages();} catch(Exception e){}
			try {loadPrivMessages();} catch(Exception e){System.out.println("User has no message log file. Creating empty log..."); try{saveAll();}catch(Exception ex){System.out.println("Error creating file!");}}
		}
		
		public String getLastSeen() {
			return this.lastSeen;
		}
	
		public int getId() {
			return this.id;
		}
		
		public boolean getStatus() {
			return this.status;
		}
		
		public ArrayList<Message> getMessages() {
			return this.messages;
		}
		
		public ArrayList<Message> getPrivMessages() {
			return this.pMessages;
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
        
        public dTag(String name, String desc, String alt, String returnType) {
            this.name = name;
            this.desc = desc;
            this.alt = alt;
            this.returnType = returnType;
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
	}
}
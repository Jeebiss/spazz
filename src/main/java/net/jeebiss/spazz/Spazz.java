package net.jeebiss.spazz;

import java.util.ArrayList;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PingEvent;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

@SuppressWarnings("rawtypes")
public class Spazz extends ListenerAdapter implements Listener {
	
    String[] temp;
	String chatColor = Colors.DARK_GREEN;
	String optionalColor = Colors.DARK_BLUE;
	String defaultColor = Colors.OLIVE;
	boolean charging=false;
	long chargeInitiateTime;
	long chargeFullTime = 30000;
	User charger;
	int botsnack = 0;
	ArrayList<User> feeders = new ArrayList<User>();

    static HtmlUnitDriver GHCR = new HtmlUnitDriver();
    static HtmlUnitDriver GHRR = new HtmlUnitDriver();
    static HtmlUnitDriver PASTEBIN = new HtmlUnitDriver();
    static WebClient HASTEBIN = new WebClient();
    static WebClient PASTIE = new WebClient();
    
	public static void main(String[] args) throws Exception {
		PircBotX bot = new PircBotX();
		bot.getListenerManager().addListener(new Spazz());
		/*
		 * Connect to #denizen-dev on start up
		 */
        bot.setName("spazzmatic");
        bot.setLogin("spazz");
        bot.setVerbose(true);
        bot.setAutoNickChange(true);
        
        bot.connect("irc.esper.net");
        bot.joinChannel("#denizen-dev");
		bot.setMessageDelay(0);
        
    	GHCR.get("https://github.com/aufdemrand/Denizen/blob/master/src/main/java/net/aufdemrand/denizen/scripts/commands/CommandRegistry.java");   
    	GHRR.get("https://github.com/aufdemrand/Denizen/blob/master/src/main/java/net/aufdemrand/denizen/scripts/requirements/RequirementRegistry.java");   
    }
	
	@Override
	public void onJoin(JoinEvent event) throws Exception {
		event.getBot().sendNotice(event.getUser(), "Welcome to " + Colors.BOLD + "#denizen-dev" + Colors.NORMAL + ", home of the Denizen project. If you'd like help with anything, type " + Colors.BOLD + Colors.BLUE + ".help");
	}
	
	@Override
	public void onPing(PingEvent event) throws Exception {
		// ???
	}
	
	@Override
    public void onMessage(MessageEvent event) throws Exception {
		
		PircBotX bot = event.getBot();
		String msg = event.getMessage();
		String msgLwr = msg.toLowerCase();
		Channel chnl = event.getChannel();
		User usr = event.getUser();
		String senderNick = usr.getNick();
		
		String address = "";
		if(charging) {
			if(System.currentTimeMillis() > (chargeInitiateTime + chargeFullTime + chargeFullTime/2)) {
				bot.sendAction("#denizen-dev", "loses control of his beams, accidentally making pew pew noises towards "+charger.getNick()+"!");
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
			event.respond("Hello World"); 
			return;
		} else if (msgLwr.startsWith(".color")) {
			
			if(!hasOp(usr, chnl) && !hasVoice(usr, chnl)) {
				bot.sendMessage("#denizen-dev", senderNick+ ": " + chatColor + "I'm sorry, but you do not have clearance to alter my photon colorization beam.");
				return;
			}
			
			String[] args = msg.split(" ");
			if(args.length > 3) {
				bot.sendMessage("#denizen-dev", senderNick+ ": " + chatColor + "I cannot read minds... yet. Hit me up with a bot-friendly color.");
				return;
			}
			String tempColor = parseColor(args[1]);
			if(tempColor == null) {
				bot.sendMessage("#denizen-dev", senderNick+ ": " + chatColor + "I eat " + args[1] + " for breakfast. That's not a color.");
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
			bot.sendMessage("#denizen-dev", chatColor  + "Photon colorization beam reconfigured "+ "[] " + optionalColor + "() " + defaultColor + "{}");
			return;
		} else if (msgLwr.startsWith(".botsnack")) {
			String args[] = msg.split(" ");
			
			if (feeders.toString().contains(usr.toString())) {
				bot.sendMessage("#denizen-dev", senderNick+ ": " + chatColor + "Thanks, but I can't have you controlling too much of my diet.");
				return;
			}
			
			if (args.length == 2) {
				if(chnl.getUsers().toString().contains(args[1])) 
					bot.sendMessage("#denizen-dev", chatColor + "Gluttony mode activated. Beginning " + args[1] + " consumption sequence.");
				else {
					ArrayList<User> users = new ArrayList<User>(chnl.getUsers());
					Random rand = new Random();
					User random = users.get(rand.nextInt(users.size()));
					bot.sendMessage("#denizen-dev", chatColor + "Oh no! " + args[1] + " not found, nomming "+random.getNick() + " instead.");
				}
				feeders.add(usr);
				botsnack++;
				return;
				
			} else {
				bot.sendMessage("#denizen-dev", chatColor +  "OM NOM NOM! I love botsnacks!");
				feeders.add(usr);
				botsnack++;
				return;
			}
		
		} else if (msg.equalsIgnoreCase(".reload")) {
			reloadSites();
			bot.sendMessage("#denizen-dev", "Reloaded websites.");
			return;
		} else if (msgLwr.startsWith(".anchors") || msgLwr.startsWith(".anchor")) {
			bot.sendMessage("#denizen-dev", address + chatColor + "As of 0.8, locations can be referenced from scripts by using anchors linked to NPCs.");
			bot.sendMessage("#denizen-dev", chatColor + "Psst, this should be expanded upon by someone better-acquainted with it.");
			return;
		} else if (msgLwr.startsWith(".assignments") || msgLwr.startsWith(".assignment") || msgLwr.startsWith(".assign")) {
			bot.sendMessage("#denizen-dev", address + chatColor + "As of Denizen 0.8, the assignments.yml file is " + Colors.BOLD + "not " + Colors.NORMAL + chatColor + "necessary.");
			bot.sendMessage("#denizen-dev", chatColor + "Instead, create the assignment script alongside interact scripts and assign it with:");
			bot.sendMessage("#denizen-dev", chatColor + Colors.BOLD + "- /npc assign --set 'assignment_script_name'");			
			bot.sendMessage("#denizen-dev", chatColor + "Check out an example of this new script's implementation at " + Colors.BLUE + "http://bit.ly/YiQ0hs");
			return;
		} else if (msgLwr.startsWith(".help")) {
			bot.sendMessage("#denizen-dev", address + chatColor + "Greetings. I am an interactive Denizen guide. I am a scripting guru. I am spazz.");
			bot.sendMessage("#denizen-dev", chatColor + "If you're a newbie to Denizen, type " + Colors.BOLD + ".getstarted");
			bot.sendMessage("#denizen-dev", chatColor + "For help with script commands, type " + Colors.BOLD + ".cmd command_name");
			bot.sendMessage("#denizen-dev", chatColor + "For help with script requirements, type " + Colors.BOLD + ".req requirement_name");
			bot.sendMessage("#denizen-dev", chatColor + "For everything else, ask in the channel or visit "+Colors.BLUE + "http://goo.gl/4CSK8"+ chatColor);
			return;
		} else if (msgLwr.startsWith(".paste") || msgLwr.startsWith(".pastie") || msgLwr.startsWith(".hastebin") || msgLwr.startsWith(".pastebin")) {
			bot.sendMessage("#denizen-dev", address + chatColor + "Need help with a script issue or server error?");
			bot.sendMessage("#denizen-dev", chatColor + "Help us help you by pasting your script " + Colors.BOLD + "and " + Colors.NORMAL + chatColor + "server log to " + Colors.BLUE + "http://hastebin.com");
			bot.sendMessage("#denizen-dev", chatColor + "From there, save the page and paste the link back in this channel.");
			return;
		} else if (msgLwr.startsWith(".update")) {
			bot.sendMessage("#denizen-dev", address + chatColor + "Due to the nature of our project, Denizen is always built against the " + Colors.RED +  "development" + chatColor +  " builds of Craftbukkit and Citizens.");
			bot.sendMessage("#denizen-dev", chatColor + "Most errors can be fixed by updating all 3.");
			bot.sendMessage("#denizen-dev", Colors.BOLD + "Denizen" + Colors.NORMAL + Colors.BLUE +  "- http://bit.ly/Wvvg8N");
			bot.sendMessage("#denizen-dev", Colors.BOLD + "Citizens" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/Xe8YWZ");
			bot.sendMessage("#denizen-dev", Colors.BOLD + "Craftbukkit" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/A5I50a");
			return;
		}
		
		else if (msgLwr.startsWith(".yaml") || msgLwr.startsWith(".yml")) {
			
			String[] args = msg.split(" ");
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
			} else {
				bot.sendMessage("#denizen-dev", Colors.RED + "I cant get your script from that website :(");
			}
			
			Yaml yaml = new Yaml();
			try {
				yaml.load(rawYaml);
				bot.sendMessage("#denizen-dev", address + chatColor + "Your YAML is valid.");
			} catch (YAMLException e) {
				String fullStack =  getCustomStackTrace(e);
				String[] stackList = fullStack.split("\\n");
				int x = 0;
				while (!stackList[x].contains("org.yaml")) {
					bot.sendMessage("#denizen-dev", stackList[x]);
					x++;
				}
			}
		} else if (msgLwr.startsWith(".cmd") || msgLwr.startsWith(".command")) {
			String [] args = msg.split(" ");
			String command = args[1].toLowerCase();
				
			int x = 51;
			boolean done = false;
			while (!done) {
				try {
					WebElement usage = GHCR.findElement(By.xpath("//*[@id=\"LC" + x + "\"]/span[1]"));
					String commandname = usage.getText();
					System.out.println(commandname);
						if (commandname.substring(1, commandname.length()-1).equalsIgnoreCase(command.toUpperCase())) {
							usage = GHCR.findElement(By.xpath("//*[@id=\"LC" + x + "\"]/span[3]"));
							String unparsed = usage.getText();
							String formatted = parseUsage(unparsed.substring(1, unparsed.length() - 1));
							bot.sendMessage("#denizen-dev", address + chatColor + "Usage: - " + formatted);
							return;
						}
					x = x + 3;
				} catch (Exception e) { e.printStackTrace(); done = true; System.out.println("done."); }
			}
			
			bot.sendMessage("#denizen-dev", chatColor + "The command '" + command + "' does not exist. If you think it should, feel free to suggest it to a developer.");
			return;
			
		} else if (msgLwr.startsWith(".req") || msgLwr.startsWith(".requirement")) {
			String [] args = msg.split(" ");
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
						bot.sendMessage("#denizen-dev", address + chatColor + "Usage: - " + formatted);
						return;
					}
					x = x + 3;
				} catch (Exception e) { done = true; System.out.println("done."); }
			}
			
			bot.sendMessage("#denizen-dev", chatColor + "The requirement '" + requirement + "' does not exist. If you think it should, feel free to suggest it to a developer.");
			return;
			
		} else if (msgLwr.startsWith(".cb") || msgLwr.startsWith(".coolbeans")) {
			bot.sendMessage("#denizen-dev", address + chatColor + "That's cool beans.");
			return;	
		} else if (msgLwr.startsWith(".hb") || msgLwr.startsWith(".handbook")) {
			bot.sendMessage("#denizen-dev", address + chatColor + "Current 0.8 Documentation - "+ Colors.BLUE + "http://bit.ly/XaWBLN");
			return;	
		} else if (msgLwr.startsWith(".getstarted") || msgLwr.startsWith(".gs")) {
			bot.sendMessage("#denizen-dev", address + chatColor + "So you're trying to to use 0.8 for first time?");
			bot.sendMessage("#denizen-dev", chatColor + "To see how I can help you, type " + Colors.BOLD + ".help");
			bot.sendMessage("#denizen-dev", chatColor + "It's also recommened that you read the current documentation.");
			bot.sendMessage("#denizen-dev", chatColor + "Denizen 0.8 Handbook - " + Colors.BLUE + "http://goo.gl/4CSK8");
			bot.sendMessage("#denizen-dev", chatColor + "Please keep in mind the handbook its a work in progress. It does not contain everything.");
			return;	
		} else if (msgLwr.startsWith(".fire")) {
			if(hasOp(usr, chnl) || hasVoice(usr, chnl)) {
				String args[] = msg.split(" ");
				if(!charging) {
					bot.sendMessage("#denizen-dev", senderNick+ ": " + chatColor + "Erm... was I supposed to be charging? D:");
					return;
				}
				if(usr != charger) {
					bot.sendMessage("#denizen-dev", senderNick+ ": " + chatColor + "Sorry, but my firing sequence has already been started by "+charger.getNick()+".");
					return;
				}
				if(args.length != 2) {
					bot.sendMessage("#denizen-dev", senderNick+ ": " + chatColor + "I can't just fire into thin air :(");
					return;
				}
				if(chnl.getUsers().toString().contains(args[1])) {
					double chance = (Math.random() *99 + 1) * ((System.currentTimeMillis()-chargeInitiateTime)/chargeFullTime);
					if(chance > 50) {
						bot.sendAction("#denizen-dev", chatColor + "makes pew pew noises towards "+ args[1] + "... successfully!");
						bot.sendMessage("#denizen-dev", chatColor + "Take that " + args[1] + "!");
					} else {
						bot.sendAction("#denizen-dev", chatColor + "makes pew pew noises towards "+ args[1] + "... and misses D:");
						bot.sendMessage("#denizen-dev", chatColor + "You've bested me this time " + args[1] + "...");
					}
					charging=false;
					chargeInitiateTime=0;
					charger=null;
					feeders.clear();
					return;
				} else {
					bot.sendMessage("#denizen-dev", senderNick + ": "+ chatColor + args[1] + ", really? I need a legitimate target. This is serious business.");
					return;
				}
			}
			
			bot.sendMessage("#denizen-dev", senderNick+ ": " + chatColor + "Whoa there, you can't touch that button.");
			return;
		
		} else if (msgLwr.startsWith(".lzrbms") || msgLwr.startsWith(".lzrbmz")) {
			if(hasOp(usr, chnl) || hasVoice(usr, chnl)) {
				if (botsnack < 3) { 
					bot.sendMessage("#denizen-dev", senderNick+ chatColor +": Botsnack levels too low. Can't charge lazers...");
					return;
				}
				
				if(charging) {
					bot.sendMessage("#denizen-dev", senderNick+ chatColor +": I'm already a bit occupied here!");
					return;
				}
				
				chargeInitiateTime=System.currentTimeMillis();
				charging=true;
				charger=usr;
				bot.sendMessage("#denizen-dev", senderNick+ ": " + chatColor + "Imma chargin' up meh lazerbeamz...");
				botsnack-=3;
				return;
			}
			
			bot.sendMessage("#denizen-dev", senderNick+ ": " + chatColor + "Umm, that's not for you.");
			return;
		
		} else if (msg.equalsIgnoreCase(".bye")) {
			if(hasOp(usr, chnl) || hasVoice(usr, chnl)) {
				bot.sendMessage("#denizen-dev", chatColor + "Goodbye cruel world!");
				bot.disconnect();
				return;
			}
			
			bot.sendMessage("#denizen-dev", senderNick+ ": " + chatColor + "Hah! You'll never kill me...");
			return;
		} else if ((msg.contains("hastebin.") || msg.contains("pastebin.") || msg.contains("pastie.")) && !(chnl.hasVoice(usr) || chnl.isOp(usr))) {
			bot.sendNotice(usr, "If you want to whether a Denizen script will compile, type " + Colors.BOLD + ".yml link_to_the_script");
			return;
		} else if ((msgLwr.contains("help") || msgLwr.contains("halp") || msgLwr.contains("hlp")) && !(chnl.hasVoice(usr) || chnl.isOp(usr))) {
			bot.sendNotice(usr, "If you need help with a Denizen issue, type " + Colors.BOLD + ".help");
			return;
		}
	}
	
	private void reloadSites() {
		GHCR.get("https://github.com/aufdemrand/Denizen/blob/master/src/main/java/net/aufdemrand/denizen/scripts/commands/CommandRegistry.java");   
    	GHRR.get("https://github.com/aufdemrand/Denizen/blob/master/src/main/java/net/aufdemrand/denizen/scripts/requirements/RequirementRegistry.java");	
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
		
		if(formatted.contains("chat")) {
			return "chat " + chatColor + "[\"message to chat\"] " + optionalColor + "(npcid:#) (target(s):npc.#|player.player_name"+defaultColor+"{attached player}" + optionalColor + ")";
		}
		
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
			switch(symbol){
			case "0":
				return Colors.BLACK;
			case "1":
				return Colors.DARK_BLUE;
			case "2":
				return Colors.DARK_GREEN;
			case "3":
				return Colors.TEAL;
			case "4":
				return Colors.RED;
			case "5":
				return Colors.PURPLE;
			case "6":
				return Colors.YELLOW;
			case "7":
				return Colors.LIGHT_GRAY;
			case "8":
				return Colors.DARK_GRAY;
			case "9":
				return Colors.BLUE;
			case "a":
				return Colors.GREEN;
			case "b":
				return Colors.CYAN;
			case "c":
				return Colors.RED;
			case "d":
				return Colors.MAGENTA;
			case "e":
				return Colors.YELLOW;
			case "f":
				return Colors.WHITE;
			default:
				return null;
			}
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
	
}
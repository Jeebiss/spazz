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
	public void onPing(PingEvent event) throws Exception {
		// ???
	}
	
	@Override
    public void onMessage(MessageEvent event) throws Exception {
		
		String address = "";
		if(charging) {
			if(System.currentTimeMillis() > (chargeInitiateTime + chargeFullTime + chargeFullTime/2)) {
				event.getBot().sendAction("#denizen-dev", "loses control of his beams, accidentally making pew pew noises towards "+charger.getNick()+"!");
				charging=false;
				chargeInitiateTime=0;
				charger=null;
			}
				
		}
		
		if(event.getMessage().endsWith("@@")) {
			String args[] = event.getMessage().split(" ");
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
		if (event.getMessage().equalsIgnoreCase(".hello")) {
			event.respond("Hello World"); 
		
		} else if (event.getMessage().toLowerCase().startsWith(".color")) {
			
			if(!hasOp(event.getUser(), event.getChannel()) && !hasVoice(event.getUser(), event.getChannel())) {
				event.getBot().sendMessage("#denizen-dev", event.getUser().getNick()+ ": " + chatColor + "I'm sorry, but you do not have clearance to alter my photon colorization beam.");
				return;
			}
			
			String[] args = event.getMessage().split(" ");
			if(args.length != 2) {
				event.getBot().sendMessage("#denizen-dev", event.getUser().getNick()+ ": " + chatColor + "I cannot read minds... yet. Hit me up with a bot-friendly color.");
				return;
			}
			String tempColor = parseColor(args[1]);
			if(tempColor == null) {
				event.getBot().sendMessage("#denizen-dev", event.getUser().getNick()+ ": " + chatColor + "I eat " + args[1] + " for breakfast. That's not a color.");
				return;
			}
			chatColor=tempColor;
			event.getBot().sendMessage("#denizen-dev", chatColor + "Photon colorization beam reconfigured.");
		
		} else if (event.getMessage().toLowerCase().startsWith(".botsnack")) {
			String args[] = event.getMessage().split(" ");
			
			if (feeders.toString().contains(event.getUser().toString())) {
				event.getBot().sendMessage("#denizen-dev", event.getUser().getNick()+ ": " + chatColor + "Thanks, but I can't have you controlling too much of my diet.");
				return;
			}
			
			if (args.length == 2) {
				if(event.getChannel().getUsers().toString().contains(args[1])) 
					event.getBot().sendMessage("#denizen-dev", chatColor + "Gluttony mode activated. Beginning " + args[1] + " consumption sequence.");
				else {
					ArrayList<User> users = new ArrayList<User>(event.getChannel().getUsers());
					Random rand = new Random();
					User random = users.get(rand.nextInt(users.size()));
					event.getBot().sendMessage("#denizen-dev", chatColor + "Oh no! " + args[1] + " not found, nomming "+random.getNick() + " instead.");
				}
				feeders.add(event.getUser());
				botsnack++;
				return;
				
			} else {
				event.getBot().sendMessage("#denizen-dev", chatColor +  "OM NOM NOM! I love botsnacks!");
				feeders.add(event.getUser());
				botsnack++;
				return;
			}
		
		} else if (event.getMessage().equalsIgnoreCase(".reload")) {
			reloadSites();
			event.getBot().sendMessage("#denizen-dev", "Reloaded websites.");
		
		} else if (event.getMessage().toLowerCase().startsWith(".anchors") || event.getMessage().toLowerCase().startsWith(".anchor")) {
			event.getBot().sendMessage("#denizen-dev", address + chatColor + "As of 0.8, locations can be referenced from scripts by using anchors linked to NPCs.");
			event.getBot().sendMessage("#denizen-dev", chatColor + "Psst, this should be expanded upon by someone better-acquainted with it.");
		
		} else if (event.getMessage().toLowerCase().startsWith(".assignments") || event.getMessage().toLowerCase().startsWith(".assignment") || event.getMessage().toLowerCase().startsWith(".assign")) {
			event.getBot().sendMessage("#denizen-dev", address + chatColor + "As of Denizen 0.8, the assignments.yml file is " + Colors.BOLD + "not " + Colors.NORMAL + chatColor + "necessary.");
			event.getBot().sendMessage("#denizen-dev", chatColor + "Instead, create the assignment script alongside interact scripts and assign it with:");
			event.getBot().sendMessage("#denizen-dev", chatColor + Colors.BOLD + "- /npc assign --set 'assignment_script_name'");			
			event.getBot().sendMessage("#denizen-dev", chatColor + "Check out an example of this new script's implementation at " + Colors.BLUE + "http://bit.ly/YiQ0hs");
		
		} else if (event.getMessage().toLowerCase().startsWith(".help")) {
			event.getBot().sendMessage("#denizen-dev", address + chatColor + "Greetings. I am an interactive Denizen guide. I am a scripting guru. I am spazz.");
			event.getBot().sendMessage("#denizen-dev", chatColor + "For help with script commands, type " + Colors.BOLD + ".cmd command_name");
			event.getBot().sendMessage("#denizen-dev", chatColor + "For help with script requirements, type " + Colors.BOLD + ".req requirement_name");
			event.getBot().sendMessage("#denizen-dev", chatColor + "For everything else, ask in the channel or visit "+Colors.BLUE + "http://goo.gl/4CSK8"+ chatColor);
		
		} else if (event.getMessage().toLowerCase().startsWith(".paste") || event.getMessage().toLowerCase().startsWith(".pastie") || event.getMessage().toLowerCase().startsWith(".hastebin") || event.getMessage().toLowerCase().startsWith(".pastebin")) {
			event.getBot().sendMessage("#denizen-dev", address + chatColor + "Need help with a script issue or server error?");
			event.getBot().sendMessage("#denizen-dev", chatColor + "Help us help you by pasting your script " + Colors.BOLD + "and " + Colors.NORMAL + chatColor + "server log to " + Colors.BLUE + "http://hastebin.com");
			event.getBot().sendMessage("#denizen-dev", chatColor + "From there, save the page and paste the link back in this channel.");
		
		} else if (event.getMessage().toLowerCase().startsWith(".update")) {
			event.getBot().sendMessage("#denizen-dev", address + chatColor + "Due to the nature of our project, Denizen is always built against the " + Colors.RED +  "development" + chatColor +  " builds of Craftbukkit and Citizens.");
			event.getBot().sendMessage("#denizen-dev", chatColor + "Most errors can be fixed by updating all 3.");
			event.getBot().sendMessage("#denizen-dev", Colors.BOLD + "Denizen" + Colors.NORMAL + Colors.BLUE +  "- http://bit.ly/Wvvg8N");
			event.getBot().sendMessage("#denizen-dev", Colors.BOLD + "Citizens" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/Xe8YWZ");
			event.getBot().sendMessage("#denizen-dev", Colors.BOLD + "Craftbukkit" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/A5I50a");
		}
		
		else if (event.getMessage().toLowerCase().startsWith(".yaml") || event.getMessage().toLowerCase().startsWith(".yml")) {
			PircBotX bot = event.getBot();
			
			String[] args = event.getMessage().split(" ");
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
			
		} else if (event.getMessage().toLowerCase().startsWith(".cmd") || event.getMessage().toLowerCase().startsWith(".command")) {
			PircBotX bot = event.getBot();
			String [] args = event.getMessage().split(" ");
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
			
		} else if (event.getMessage().toLowerCase().startsWith(".req") || event.getMessage().toLowerCase().startsWith(".requirement")) {
			PircBotX bot = event.getBot();
			String [] args = event.getMessage().split(" ");
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
			
		} else if (event.getMessage().toLowerCase().startsWith(".cb") || event.getMessage().toLowerCase().startsWith(".coolbeans")) {
			event.getBot().sendMessage("#denizen-dev", address + chatColor + "That's cool beans.");
			
		} else if (event.getMessage().toLowerCase().startsWith(".hb") || event.getMessage().toLowerCase().startsWith(".handbook")) {
			event.getBot().sendMessage("#denizen-dev", address + chatColor + "Current 0.8 Documentation - "+ Colors.BLUE + "http://bit.ly/XaWBLN");
			
		} else if (event.getMessage().toLowerCase().startsWith(".getstarted") || event.getMessage().toLowerCase().startsWith(".gs")) {
			event.getBot().sendMessage("#denizen-dev", address + chatColor + "So you're trying to to use 0.8 for first time?");
			event.getBot().sendMessage("#denizen-dev", chatColor + "It's recommened that you read the current documentation.");
			event.getBot().sendMessage("#denizen-dev", chatColor + "Denizen 0.8 Handbook - http://goo.gl/4CSK8");
			event.getBot().sendMessage("#denizen-dev", chatColor + "Please keep in mind the handbook its a work in progress. It does not contain everything.");
			
		} else if (event.getMessage().toLowerCase().startsWith(".fire")) {
			if(hasOp(event.getUser(), event.getChannel()) || hasVoice(event.getUser(), event.getChannel())) {
				String args[] = event.getMessage().split(" ");
				if(!charging) {
					event.getBot().sendMessage("#denizen-dev", event.getUser().getNick()+ ": " + chatColor + "Erm... was I supposed to be charging? D:");
					return;
				}
				if(event.getUser() != charger) {
					event.getBot().sendMessage("#denizen-dev", event.getUser().getNick()+ ": " + chatColor + "Sorry, but my firing sequence has already been started by "+charger.getNick()+".");
					return;
				}
				if(args.length != 2) {
					event.getBot().sendMessage("#denizen-dev", event.getUser().getNick()+ ": " + chatColor + "I can't just fire into thin air :(");
					return;
				}
				if(event.getChannel().getUsers().toString().contains(args[1])) {
					double chance = (Math.random() *99 + 1) * ((System.currentTimeMillis()-chargeInitiateTime)/chargeFullTime);
					if(chance > 50) {
						event.getBot().sendAction("#denizen-dev", chatColor + "makes pew pew noises towards "+ args[1] + "... successfully!");
						event.getBot().sendMessage("#denizen-dev", chatColor + "Take that " + args[1] + "!");
					} else {
						event.getBot().sendAction("#denizen-dev", chatColor + "makes pew pew noises towards "+ args[1] + "... and misses D:");
						event.getBot().sendMessage("#denizen-dev", chatColor + "You've bested me this time " + args[1] + "...");
					}
					charging=false;
					chargeInitiateTime=0;
					charger=null;
					feeders.clear();
					return;
				} else {
					event.getBot().sendMessage("#denizen-dev", event.getUser().getNick() + ": "+ chatColor + args[1] + ", really? I need a legitimate target. This is serious business.");
					return;
				}
			}
			
			event.getBot().sendMessage("#denizen-dev", event.getUser().getNick()+ ": " + chatColor + "Whoa there, you can't touch that button.");
			return;
		
		} else if (event.getMessage().toLowerCase().startsWith(".lzrbms") || event.getMessage().toLowerCase().startsWith(".lzrbmz")) {
			if(hasOp(event.getUser(), event.getChannel()) || hasVoice(event.getUser(), event.getChannel())) {
				if (botsnack < 3) { 
					event.getBot().sendMessage("#denizen-dev", event.getUser().getNick()+ chatColor +": Botsnack levels too low. Can't charge lazers...");
					return;
				}
				
				if(charging) {
					event.getBot().sendMessage("#denizen-dev", event.getUser().getNick()+ chatColor +": I'm already a bit occupied here!");
					return;
				}
				
				chargeInitiateTime=System.currentTimeMillis();
				charging=true;
				charger=event.getUser();
				event.getBot().sendMessage("#denizen-dev", event.getUser().getNick()+ ": " + chatColor + "Imma chargin' up meh lazerbeamz...");
				botsnack-=5;
				return;
			}
			
			event.getBot().sendMessage("#denizen-dev", event.getUser().getNick()+ ": " + chatColor + "Umm, that's not for you.");
			return;
		
		} else if (event.getMessage().equalsIgnoreCase(".bye")) {
			if(hasOp(event.getUser(), event.getChannel()) || hasVoice(event.getUser(), event.getChannel())) {
				event.getBot().sendMessage("#denizen-dev", chatColor + "Goodbye cruel world!");
				event.getBot().disconnect();
				return;
			}
			
			event.getBot().sendMessage("#denizen-dev", event.getUser().getNick()+ ": " + chatColor + "Hah! You'll never kill me...");
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
		String formatted = unparsed;	
		String beforeColor = chatColor;
		
		int requiredIndex = formatted.indexOf("]");
		System.out.println("First required index: " + requiredIndex);
		
		formatted = formatted.replace("[", chatColor + "[");
		formatted = formatted.replace("(", optionalColor + "(");
		formatted = formatted.replace("{", defaultColor + "{");
		formatted = formatted.replace("]", chatColor + "]");
		formatted = formatted.replace(")", optionalColor + ")");
		formatted = formatted.replace("}", defaultColor + "}");
		while (requiredIndex != -1) {
			
			System.out.println("Req index: " + requiredIndex);
			
			int lastOptional = formatted.indexOf(")", requiredIndex);
			System.out.println("Opt index: " + lastOptional);

			int lastDefault = formatted.indexOf("}", requiredIndex);
			System.out.println("Def index: " + lastDefault);

			int lastSpace = formatted.indexOf(" ", requiredIndex);
			System.out.println("Space index: " + lastSpace);

			
			if(lastSpace > lastDefault || lastSpace > lastOptional) {
				if(lastOptional != -1 && lastSpace > lastOptional) {
					if(lastOptional < lastDefault);
						beforeColor = optionalColor;
				}
				
				else {
					if(lastDefault != -1 && lastSpace > lastDefault)
						beforeColor = defaultColor;
				}
			}
			
			formatted = formatted.substring(0, requiredIndex + 1) + beforeColor + formatted.substring(requiredIndex + 1);
			requiredIndex = formatted.indexOf("]",requiredIndex + 1);

		}
		
		int optionalIndex = formatted.indexOf(")");
		System.out.println("First optional index: " + optionalIndex);
		
		while (optionalIndex != -1) {
			System.out.println("Opt index: " + optionalIndex);

			int lastRequired = formatted.indexOf("]", optionalIndex);
			System.out.println("Req index: " + lastRequired);
			int lastDefault = formatted.indexOf("}", optionalIndex);
			System.out.println("Def index: " + lastDefault);
			int lastSpace = formatted.indexOf(" ", optionalIndex);
			System.out.println("Space index: " + lastSpace);
			
			if(lastSpace > lastDefault || lastSpace > lastRequired) {
				if(lastRequired != -1 && lastSpace > lastRequired) {
					if(lastRequired < lastDefault);
						beforeColor = chatColor;
				}
				
				else {
					if(lastDefault != -1 && lastSpace > lastDefault)
						beforeColor = defaultColor;
				}
			}
			
			formatted = formatted.substring(0, optionalIndex + 1) + beforeColor + formatted.substring(optionalIndex + 1);
			optionalIndex = formatted.indexOf(")",optionalIndex + 1);

		}
		
		int defaultIndex = formatted.indexOf("}");
		System.out.println("First optional index: " + defaultIndex);
		
		while (defaultIndex != -1) {
			System.out.println("Opt index: " + defaultIndex);

			int lastRequired = formatted.indexOf("]", defaultIndex);
			System.out.println("Req index: " + lastRequired);
			int lastOptional = formatted.indexOf(")", defaultIndex);
			System.out.println("Def index: " + lastOptional);
			int lastSpace = formatted.indexOf(" ", defaultIndex);
			System.out.println("Space index: " + lastSpace);
			
			if(lastSpace > lastOptional || lastSpace > lastRequired) {
				if(lastRequired != -1 && lastSpace > lastRequired) {
					if(lastRequired < lastOptional);
						beforeColor = chatColor;
				}
				
				else {
					if(lastOptional != -1 && lastSpace > lastOptional)
						beforeColor = optionalColor;
				}
			}
			
			formatted = formatted.substring(0, defaultIndex + 1) + beforeColor + formatted.substring(defaultIndex + 1);
			defaultIndex = formatted.indexOf("}",defaultIndex + 1);

		}
	
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
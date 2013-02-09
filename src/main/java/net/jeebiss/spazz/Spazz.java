package net.jeebiss.spazz;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.pircbotx.Colors;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

@SuppressWarnings("rawtypes")
public class Spazz extends ListenerAdapter implements Listener {
	
    String[] temp;

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
    public void onMessage(MessageEvent event) throws Exception {
		
		String address = "";
		
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
		} else if (event.getMessage().equalsIgnoreCase(".reload")) {
			reloadSites();
			event.getBot().sendMessage("#denizen-dev", "Reloaded websites.");
		} else if (event.getMessage().toLowerCase().startsWith(".update")) {
			event.getBot().sendMessage("#denizen-dev", address + Colors.DARK_GREEN + "Due to the nature of our project, Denizen is always built against the " + Colors.RED +  "development" + Colors.NORMAL +  " builds of Craftbukkit and Citizens.");
			event.getBot().sendMessage("#denizen-dev", Colors.DARK_GREEN + "Most errors can be fixed by updating all 3.");
			event.getBot().sendMessage("#denizen-dev", Colors.BOLD + "Denizen" + Colors.NORMAL + Colors.BLUE +  "- http://bit.ly/Wvvg8N");
			event.getBot().sendMessage("#denizen-dev", Colors.BOLD + "Citizens" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/Xe8YWZ");
			event.getBot().sendMessage("#denizen-dev", Colors.BOLD + "Craftbukkit" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/A5I50a");
		} else if (event.getMessage().toLowerCase().startsWith(".help")) {
			event.getBot().sendMessage("#denizen-dev", address + Colors.DARK_GREEN + "So you're trying to to use 0.8 for first time?");
			event.getBot().sendMessage("#denizen-dev", Colors.DARK_GREEN + "It's recommened that you read the current documentation.");
			event.getBot().sendMessage("#denizen-dev", Colors.DARK_GREEN + "Denizen 0.8 Handbook - http://goo.gl/4CSK8");
			event.getBot().sendMessage("#denizen-dev", Colors.DARK_GREEN + "Please keep in mind the handbook its a work in progress. It does not contain everything.");
		} else if (event.getMessage().toLowerCase().startsWith(".paste") || event.getMessage().toLowerCase().startsWith(".pastie") || event.getMessage().toLowerCase().startsWith(".hastebin") || event.getMessage().toLowerCase().startsWith(".pastebin")) {
			event.getBot().sendMessage("#denizen-dev", address + Colors.DARK_GREEN + "Need help with a script issue or server error?");
			event.getBot().sendMessage("#denizen-dev", Colors.DARK_GREEN + "Help us help you by pasting your script or server log to " + Colors.BLUE + "http://hastebin.com");
			event.getBot().sendMessage("#denizen-dev", Colors.DARK_GREEN + "From there, save the page and paste the link back in this channel.");
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
				bot.sendMessage("#denizen-dev", address + Colors.DARK_GREEN + "Your YAML is valid.");
			} catch (YAMLException e) {
				String fullStack =  getCustomStackTrace(e);
				String[] stackList = fullStack.split("\\n");
				int x = 0;
				while (!stackList[x].contains("org.yaml")) {
					bot.sendMessage("#denizen-dev", stackList[x]);
					x++;
				}
			}
			
		} else if (event.getMessage().toLowerCase().startsWith(".command") || event.getMessage().toLowerCase().startsWith(".cmd")) {
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
							String message = usage.getText();
							bot.sendMessage("#denizen-dev", address + Colors.DARK_GREEN + "Usage: - " + message.substring(1, message.length() - 1));
							return;
						}
					x = x + 3;
				} catch (Exception e) { done = true; System.out.println("done."); }
			}
			
			bot.sendMessage("#denizen-dev", Colors.DARK_GREEN + "The command '" + command + "' does not exist. If you think it should, feel free to suggest it to a developer.");
			return;
			
		} else if (event.getMessage().toLowerCase().startsWith(".requirement") || event.getMessage().toLowerCase().startsWith(".req")) {
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
						String message = usage.getText();
						bot.sendMessage("#denizen-dev", address + Colors.DARK_GREEN + "Usage: - " + message.substring(1, message.length() - 1));
						return;
					}
					x = x + 3;
				} catch (Exception e) { done = true; System.out.println("done."); }
			}
			
			bot.sendMessage("#denizen-dev", Colors.DARK_GREEN + "The requirement '" + requirement + "' does not exist. If you think it should, feel free to suggest it to a developer.");
			return;
			
		} else if (event.getMessage().toLowerCase().startsWith(".cb") || event.getMessage().toLowerCase().startsWith(".coolbeans")) {
			event.getBot().sendMessage("#denizen-dev", address + Colors.DARK_GREEN + "That's cool beans.");
			
		} else if (event.getMessage().toLowerCase().startsWith(".hb") || event.getMessage().toLowerCase().startsWith(".handbook")) {
			event.getBot().sendMessage("#denizen-dev", address + Colors.DARK_GREEN + "Current 0.8 Documentation - "+ Colors.BLUE + "http://bit.ly/XaWBLN");
			
		} else if (event.getMessage().toLowerCase().startsWith(".getstarted") || event.getMessage().toLowerCase().startsWith(".gs")) {
			event.getBot().sendMessage("#denizen-dev", address + Colors.DARK_GREEN + "Add info for newbies");
			
		} else if (event.getMessage().equalsIgnoreCase(".bye")) {
			event.getBot().sendMessage("#denizen-dev", Colors.DARK_GREEN + "Goodbye cruel world!");
			event.getBot().disconnect();
		}
	}
	
	private void reloadSites() {
		GHCR.get("https://github.com/aufdemrand/Denizen/blob/master/src/main/java/net/aufdemrand/denizen/scripts/commands/CommandRegistry.java");   
    	GHRR.get("https://github.com/aufdemrand/Denizen/blob/master/src/main/java/net/aufdemrand/denizen/scripts/requirements/RequirementRegistry.java");	
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
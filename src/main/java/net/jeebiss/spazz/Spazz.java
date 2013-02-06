package net.jeebiss.spazz;

import java.util.Map;

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


@SuppressWarnings("rawtypes")
public class Spazz extends ListenerAdapter implements Listener {
	
    String[] temp;

    static HtmlUnitDriver GHCR = new HtmlUnitDriver();
    static HtmlUnitDriver GHRR = new HtmlUnitDriver();
    static HtmlUnitDriver YAML = new HtmlUnitDriver();
    static HtmlUnitDriver PASTEBIN = new HtmlUnitDriver();
    static HtmlUnitDriver PASTIE = new HtmlUnitDriver();
    
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
    	YAML.setJavascriptEnabled(true);
    	YAML.get("http://yaml-online-parser.appspot.com/");
    }
	
	@Override
    public void onMessage(MessageEvent event) throws Exception {
		if (event.getMessage().equalsIgnoreCase(".hello")) {
			event.respond("Hello World");
		} else if (event.getMessage().equalsIgnoreCase(".reload")) {
			reloadSites();
			event.getBot().sendMessage("#denizen-dev", "Reloaded websites.");
		} else if (event.getMessage().equalsIgnoreCase(".update")) {
			event.getBot().sendMessage("#denizen-dev", "Due to the nature of our project, Denizen is always built against the " + Colors.RED +  "development" + Colors.NORMAL +  " builds of Craftbukkit and Citizens.");
			event.getBot().sendMessage("#denizen-dev", "Most errors can be fixed by updating all 3.");
			event.getBot().sendMessage("#denizen-dev", Colors.BOLD + "Denizen" + Colors.NORMAL +  "- http://bit.ly/Wvvg8N");
			event.getBot().sendMessage("#denizen-dev", Colors.BOLD + "Citizens" + Colors.NORMAL +  "- http://bit.ly/Xe8YWZ");
			event.getBot().sendMessage("#denizen-dev", Colors.BOLD + "Craftbukkit" + Colors.NORMAL +  "- http://bit.ly/A5I50a");
		} else if (event.getMessage().equalsIgnoreCase(".help")) {
			event.getBot().sendMessage("#denizen-dev", "So you're trying to to use 0.8 for first time?");
			event.getBot().sendMessage("#denizen-dev", "Its recommened that you read the current documentation.");
			event.getBot().sendMessage("#denizen-dev", "Denizen 0.8 Handbook - http://goo.gl/4CSK8");
			event.getBot().sendMessage("#denizen-dev", "Please keep in mind the handbook its a work in progress. It does not contain everything.");
			
		}
		
		else if (event.getMessage().startsWith(".yaml")) {
			PircBotX bot = event.getBot();
			
			String[] args = event.getMessage().split(" ");
			String rawYaml = null;
			if (args[1].contains("pastebin")) {
				PASTEBIN.get(args[1]);
				WebElement pastedYaml = PASTEBIN.findElement(By.id("paste_code"));
				rawYaml = pastedYaml.getAttribute("value");
			} else if (args[1].contains("pastie")) {
				String url[] = args[1].split("/");
				PASTIE.get("http://pastie.org/pastes/" + url[3] + "/text");
				WebElement pastedYaml = PASTIE.findElement(By.xpath("/html/body/pre"));
				rawYaml = pastedYaml.getText();
				//rawYaml = pastedYaml.getAttribute("value");
			} else if (args[1].contains("pastebin")) {
				
			} else {
				bot.sendMessage("#denizen-dev", "I cant get your script from that website :(");
			}
			
			Yaml yaml = new Yaml();
			try {
			Map<String, Object> loadedYaml = (Map<String, Object>) yaml.load(rawYaml);
			bot.sendMessage("#denizen-dev", "Your YAML is valid.");
			} catch (YAMLException e) {
				String fullStack =  getCustomStackTrace(e);
				String[] stackList = fullStack.split("\\n");
				int x = 0;
				while (!stackList[x].contains("org.yaml")) {
					bot.sendMessage("#denizen-dev", stackList[x]);
					x++;
				}
			}
			
		} else if (event.getMessage().startsWith(".command") || event.getMessage().startsWith(".cmd")) {
			PircBotX bot = event.getBot();
			String [] args = event.getMessage().split(" ");
			String command = args[1].toLowerCase();
				
			int x = 50;
			boolean done = false;
			while (!done) {
				try {
					WebElement usage = GHCR.findElement(By.xpath("//*[@id=\"LC" + x + "\"]/span[1]"));
					String commandname = usage.getText();
					System.out.println(commandname);
						if (commandname.substring(1, commandname.length()-1).equalsIgnoreCase(command.toUpperCase())) {
							usage = GHCR.findElement(By.xpath("//*[@id=\"LC" + x + "\"]/span[3]"));
							String message = usage.getText();
							bot.sendMessage("#denizen-dev", "Usage: - " + message.substring(1, message.length() - 1));
							return;
						}
					x = x + 3;
				} catch (Exception e) { done = true; System.out.println("done."); }
			}
			
			bot.sendMessage("#denizen-dev", "The command '" + command + "' does not exist. If you feel it should, feel free to suggest it to a developer.");
			return;
			
		} else if (event.getMessage().startsWith(".requirement") || event.getMessage().startsWith(".req")) {
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
						bot.sendMessage("#denizen-dev", "Usage: - " + message.substring(1, message.length() - 1));
						return;
					}
					x = x + 3;
				} catch (Exception e) { done = true; System.out.println("done."); }
			}
			
			bot.sendMessage("#denizen-dev", "The requirement '" + requirement + "' does not exist. If you feel it should, feel free to suggest it to a developer.");
			return;
			
		} else if (event.getMessage().equalsIgnoreCase(".hb") || event.getMessage().equalsIgnoreCase(".handbook")) {
			event.getBot().sendMessage("#denizen-dev", "Current 0.8 Documentation - http://bit.ly/XaWBLN");
			
		} else if (event.getMessage().equalsIgnoreCase(".getstarted") || event.getMessage().equalsIgnoreCase(".gs")) {
			event.getBot().sendMessage("#denizen-dev", "Add info for newbies");
			
		} else if (event.getMessage().equalsIgnoreCase(".bye")) {
			event.getBot().disconnect();
		}
	}
	
	private void reloadSites() {
		GHCR.get("https://github.com/aufdemrand/Denizen/blob/master/src/main/java/net/aufdemrand/denizen/scripts/commands/CommandRegistry.java");   
    	GHRR.get("https://github.com/aufdemrand/Denizen/blob/master/src/main/java/net/aufdemrand/denizen/scripts/requirements/RequirementRegistry.java");
    	YAML.get("http://yaml-online-parser.appspot.com/");		
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
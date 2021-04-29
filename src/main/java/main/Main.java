package main;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;

public class Main {
	private static Logger LOGGER = LogManager.getLogger("BotMain");

	public static JDA api;
    public static void main(String[] args) throws LoginException {
		System.setProperty("sun.stdout.encoding", "UTF-8");
		ConfigStorage.getConfig();
    	
    	if(!ConfigStorage.initialized) {
    		LOGGER.fatal("Config file either invalid or missing! Exiting... ");
    		return;
    	}
    	
    	YtubeList.setup();
    	
    	api = JDABuilder.createDefault(ConfigStorage.discordToken)
    			.setMemberCachePolicy(MemberCachePolicy.ALL)
    			.enableIntents(GatewayIntent.GUILD_MEMBERS)
    			.enableIntents(GatewayIntent.GUILD_PRESENCES)
    			.build();
    	
    	api.addEventListener(new BotListener());
    }
}
    
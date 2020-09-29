package main;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Main {
	public static JDA api;
    public static void main(String[] args) throws LoginException {
    	ConfigStorage.getConfig();
    	
    	if(!ConfigStorage.initialized) {
    		System.err.println("Config file either invalid or missing!\n Exiting... ");
    		return;
    	}
    	
    	YtubeList.setup();
    	
    	api = JDABuilder.createDefault(ConfigStorage.discordToken)
    			.setMemberCachePolicy(MemberCachePolicy.ALL)
    			.enableIntents(GatewayIntent.GUILD_MEMBERS)
    			.enableIntents(GatewayIntent.GUILD_PRESENCES)
    			.build();
    	
    	api.addEventListener(new Bot_Listener());
    }
    
    
    
}
    
package main;

import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.api.entities.Member;

public class ZorkManager {
	private static final Map<String,ZorkGame> games = new HashMap<>();
	
	public String newGame(Member user) {
		games.put(user.getUser().getId(), new ZorkGame());
		return games.get(user.getUser().getId()).lastFound;
		
	}
	
	public String input(Member user,String input) {
		ZorkGame game=games.get(user.getUser().getId());
		if(game==null) {
			return "**Starting new game for:**\t" + user.getAsMention() + "\n\n" + newGame(user);
		}		
		
		return game.command(input);
	}
	
}

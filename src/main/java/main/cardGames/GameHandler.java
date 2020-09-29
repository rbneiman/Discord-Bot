package main.cardGames;

import java.util.HashMap;

public class GameHandler {
	private static final HashMap<Long,BlackJack> blackjackGames = new HashMap<Long,BlackJack>();
	private static final HashMap<Long,Integer> payOuts = new HashMap<Long,Integer>();
	
	public static boolean blackJackGameExists(long id) {
		return blackjackGames.containsKey(id);
	}
	
	public static int recievePayOut(long id) {
		if(!payOuts.containsKey(id)) {payOuts.put(id,0);}
		int out = payOuts.get(id);
		payOuts.put(id,0);
		return out;
	}
	
	public static String blackJackHandler(String name, int bet, String action, long id) {
		int playResult = -1;
		BlackJack game;
		String out = "";
		if(!blackjackGames.containsKey(id)) {
			game = new BlackJack(bet);
			playResult = game.Play("hit");
			blackjackGames.put(id,game);
			payOuts.put(id,0);
		}
		else if(blackjackGames.get(id).isDone()) {
			game = blackjackGames.get(id);
			game.newGame(bet);
			playResult = game.Play("hit");
			payOuts.put(id,0);
		}
		else {
			game = blackjackGames.get(id);
			playResult = game.Play(action);
		}
		
		
		
		if(!action.equals("stand")) {
			out += name + " drew a " + game.getPlayerCard() + "\n";
		}
		
        if(playResult==1) { 
        	if(game.getBotScore()<17) {
        		out += "Bot drew a " + game.getBotCard() + "\n";
        	}
        	out += name + ": " + game.getPlayerScore() + " Bot: " + game.getBotScore() + "\n";
        	out += name + " has won " + game.getBet() + " dine-in dollars!" + "\n";
        	payOuts.put(id,game.getBet());
        }
        else if(playResult==2) {
        	if(game.getBotScore()<17) {
        		out += "Bot drew a " + game.getBotCard() + "\n";
        	}
        	out += name + ": " + game.getPlayerScore() + " Bot: " + game.getBotScore() + "\n";
        	out += name + " has lost " + game.getBet() + " dine-in dollars!";
        	payOuts.put(id,-game.getBet());
        }
        else if(playResult==3) { 
        	out += "Bot drew a " + game.getBotCard() + "\n";
        	out += name + ": " + game.getPlayerScore() + " Bot: " + game.getBotScore() + "\n";
        	out += "Ended in a draw!";
        }
        else if(playResult == 0){
        	if(game.getBotScore()<17) {
        		out += "Bot drew a " + game.getBotCard() + "\n";
        	}
        	out += name + ": " + game.getPlayerScore() + " Bot: " + game.getBotScore();
        }
        else {
        	out = "Invalid move!";
        }
        
        return out;
    }
}

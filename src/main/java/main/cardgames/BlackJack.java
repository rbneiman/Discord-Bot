package main.cardgames;

public class BlackJack implements CardGame{	
	private final Deck deck;
	private int bet;
	private int botHardScore;
	private int playerHardScore;
	private int botAces;
	private int playerAces;
	private String recentPlayerCard;
	private String recentBotCard;
	private boolean done;
	
	public BlackJack(int bet) {
		this.bet = bet;
		this.deck = new Deck(true);
		this.botHardScore = 0;
		this.playerHardScore = 0;
		this.botAces = 0;
		this.playerAces = 0;
		recentPlayerCard = null;
		recentBotCard = null;
		done = false;
		playerHardScore += DrawFromDeck(true);
		botHardScore += DrawFromDeck(false);
	}
	
	public void newGame(int bet) {
		deck.Shuffle();
		this.bet = bet;
		this.botHardScore = 0;
		this.playerHardScore = 0;
		this.botAces = 0;
		this.playerAces = 0;
		recentPlayerCard = null;
		recentBotCard = null;
		done = false;
		playerHardScore += DrawFromDeck(true);
		botHardScore += DrawFromDeck(false);
	}
	

	public int getBotScore() {
		int softScore = botHardScore;
		for(int i=0; i<botAces; i++) {
			if(softScore<11) {softScore+=11;}
			else {softScore+=1;}
		}
		return softScore;
	}
	
	public int getPlayerScore() {
		int softScore = playerHardScore;
		for(int i=0; i<playerAces; i++) {
			if(softScore<11) {softScore+=11;}
			else {softScore+=1;}
		}
		return softScore;
	}
	
	public String getPlayerCard() {
		return recentPlayerCard;
	}
	
	public String getBotCard() {
		return recentBotCard;
	}
	
	public int getBet() {
		return bet;
	}
	
	public boolean isDone() {
		return done;
	}
	
	private int DrawFromDeck(boolean isPlayer) {
		Card newCard = deck.Draw();		
		if(isPlayer) {this.recentPlayerCard = newCard.toString();}
		else {this.recentBotCard = newCard.toString();}
		int num = newCard.number;
		
		if(num>10) {num = 10;}
		if(num==1) {
			num = 0;
			if(isPlayer) {playerAces++;}
			else {botAces++;}
		}
		return num;
	}
	
	//returns -1 invalid move, 0 game still going, 1 player won, 2 bot won, 3 draw
	public int Play(String action) {
		boolean stand = false;
		done = true;
		if(action.equals("hit")) {
			playerHardScore += DrawFromDeck(true);
			if(getPlayerScore()>21) {return 2;}
		}
		else if(action.equals("stand")) {
			stand = true;
		}
		else {
			done = false;
			return -1;
		}
		
		if(getBotScore()<17) {
			botHardScore += DrawFromDeck(false);
		}
		
		if(getBotScore()>21) {return 1;}
		if(getBotScore()==21&&getPlayerScore()==21) {return 3;}
//		else if(getBotScore()==21) {return 2;}
		else if(getPlayerScore()==21) {return 1;}
		
		if(getBotScore()>16&&stand) {
			if(getBotScore()>getPlayerScore()) {return 2;}
			if(getBotScore()<getPlayerScore()) {return 1;}
			if(getBotScore()==getPlayerScore()) {return 3;}
		}
		done = false;
		return 0;
	}
	
}

package main.cardGames;

import java.util.Random;

public class Deck {
	private Card[] cards;
	private int topInd;
	
	//initializes deck
	//also shuffles deck if shuffle==true
	public Deck(boolean shuffle) {
		this.topInd = 51;
		this.cards = new Card[52];
		for(int i = 0; i<4; i++) {
			for(int j = 0; j<13; j++) {
				cards[i*13+j] = new Card(i,j+1);
			}
		}

		if(shuffle) {Shuffle();}
	}
	
	//O(n) shuffle algorithm
	public void Shuffle() { 
		topInd = 51;
		Random rand = new Random();
		int j;
		for(int i=51; i>0; i--) {
			j = rand.nextInt(i);
			Card temp = cards[j];
			cards[j] = cards[i];
			cards[i] = temp;
		}
	}
	
	//returns card on top of the deck
	public Card Draw() {
		if(topInd<=0) {return null;}
		return cards[topInd--];
	}
	
}

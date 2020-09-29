package main.cardGames;

public class Card {
	public final int suit; //0 Clubs, 1 Diamonds, 2 Hearts, 3 Spades
	public final int number; //1 ace, 2-10 nums, 11 Jack, 12 Queen, 13 King
	public final String cardString; 
	
	public Card(int suit, int number) {
		this.suit = suit;
		this.number = number;
		String temp = "";
		switch(number) {
		case 1: 
        	temp += "Ace"; 
            break; 
        case 2: 
        	temp += "Two"; 
            break; 
        case 3: 
        	temp += "Three"; 
            break;
        case 4: 
        	temp += "Four"; 
            break; 
        case 5: 
        	temp += "Five"; 
            break; 
        case 6: 
        	temp += "Six"; 
            break;
        case 7: 
        	temp += "Seven"; 
            break; 
        case 8: 
        	temp += "Eight"; 
            break; 
        case 9: 
        	temp += "Nine"; 
            break;
        case 10: 
        	temp += "Ten"; 
            break; 
        case 11: 
        	temp += "Jack"; 
            break; 
        case 12: 
        	temp += "Queen"; 
            break;
        case 13: 
        	temp += "King"; 
            break;
        default:
        	temp = "Invalid";
		}
		
		switch(suit) {
		case 0: 
			temp += " of Clubs"; 
            break; 
        case 1: 
        	temp += " of Diamonds"; 
            break; 
        case 2: 
        	temp += " of Hearts"; 
            break; 
        case 3: 
        	temp += " of Spades"; 
            break;
        default:
        	temp = "Invalid";
		}
		this.cardString = temp;
		
	}
	
	@Override
	public String toString() {
		return cardString;
	}
}

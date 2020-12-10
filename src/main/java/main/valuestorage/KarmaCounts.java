package main.valuestorage;

import java.io.Serializable;

public class KarmaCounts implements Serializable{

	private static final long serialVersionUID = 696969696968L;
	
	public int upvotes;
	public int downvotes;
	public int goldTimeLeft;
	public int goldTokens;
	public int downLeft;
	public int upLeft;
	public transient int timerPerTick;
	
	
	public KarmaCounts(int upvotes, int downvotes) {
		this.upvotes = upvotes;
		this.downvotes = downvotes;
		this.goldTimeLeft = 0;
		this.goldTokens = 0;
		this.timerPerTick = 1;
	}
	
	public Integer getKarma() {
		return Math.max(upvotes-downvotes,0);
	}
	
}

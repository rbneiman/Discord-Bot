package main.valuestorage;

public class MemberInfo {
	public final long memberId;
	public final long guildId;

	private int upvotes;
	private int downvotes;
	public int upvotesLeft;
	public int downvotesLeft;
	public int ticksPerIncrement;

	private boolean upvotesChanged = false;
	private boolean downvotesChanged = false;
	private boolean balanceChanged = false;

	private int balance;

	public MemberInfo(long memberId, long guildId) {
		this(memberId, guildId, 0, 0, 0);
	}

	public MemberInfo(long memberId, long guildId, int upvotes, int downvotes, int balance) {
		this.memberId = memberId;
		this.guildId = guildId;
		this.upvotes = upvotes;
		this.downvotes = downvotes;
		this.upvotesLeft = VoteStuff.calcLimitUp(this);
		this.downvotesLeft = VoteStuff.calcLimitDown(this);
		this.ticksPerIncrement = 1;
		this.balance = balance;
	}

	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		balanceChanged = true;
		this.balance = balance;
	}

	public int getUpvotes() {
		return upvotes;
	}

	public void upvote(){
		upvotesChanged = true;
		this.upvotes++;
	}

	public int getDownvotes() {
		return downvotes;
	}

	public void downvote(){
		downvotesChanged = true;
		this.downvotes++;
	}

	public int getKarma(){
		return Math.max(upvotes - downvotes, 0);
	}

	public int getKarmaUnbounded() {
		return upvotes - downvotes;
	}

	public void update(){
		if(upvotesChanged){
			DatabaseManager.updateMember(memberId, "upvotes", upvotes);
			upvotesChanged = false;
		}
		if(downvotesChanged){
			DatabaseManager.updateMember(memberId, "downvotes", downvotes);
			downvotesChanged = false;
		}
		if(balanceChanged){
			DatabaseManager.updateMember(memberId, "balance", balance);
			balanceChanged = false;
		}
	}
}

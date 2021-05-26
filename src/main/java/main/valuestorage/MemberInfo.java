package main.valuestorage;

import main.ConfigStorage;
import main.Main;
import net.dv8tion.jda.api.entities.Member;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class MemberInfo {
	private static final Logger LOGGER = LogManager.getLogger(MemberInfo.class);

	private static final float ABUSE_FRACTION = 0.35F;
	private static final int ABUSE_NUMBER = 5;
	public final long memberId;
	public final long guildId;
	public final String effectiveName;

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
		effectiveName = Main.api.getGuildById(guildId).getMemberById(memberId).getEffectiveName();
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

	public void upvote(MemberInfo voter){
		upvotesChanged = true;
		this.upvotes++;
		ValueStorage.addVoteAction(voter, this, true);
	}

	public int getDownvotes() {
		return downvotes;
	}

	public void downvote(MemberInfo voter){
		downvotesChanged = true;
		this.downvotes++;
		ValueStorage.addVoteAction(voter, this, false);
	}

	public int getKarma(){
		return Math.max(upvotes - downvotes, 0);
	}

	public int getKarmaUnbounded() {
		return upvotes - downvotes;
	}

	public boolean canVote(MemberInfo author, boolean isUpvote){
		if(isUpvote){
			if(this.upvotesLeft<=0 && this.memberId != ConfigStorage.developerID)
				return false;

			ArrayList<VoteAction> actionsWithAuthor = ValueStorage.getVoteActions(this, author, true);
			ArrayList<VoteAction> actionsOverall = ValueStorage.getVoteActions(author, true);
			int tempInt = actionsWithAuthor.size();
			float tempFloat = ((float) actionsWithAuthor.size())/((float) actionsOverall.size());
			if(actionsWithAuthor.size() >= ABUSE_NUMBER && ((float) actionsWithAuthor.size())/((float) actionsOverall.size()) > ABUSE_FRACTION){
				LOGGER.info(this.effectiveName + " hit abuse threshold upvoting " + author.effectiveName);
				return false;
			}

		}else{
			if(this.downvotesLeft<=0 && this.memberId != ConfigStorage.developerID)
				return false;

			ArrayList<VoteAction> actionsWithAuthor = ValueStorage.getVoteActions(this, author, false);
			ArrayList<VoteAction> actionsOverall = ValueStorage.getVoteActions(author, false);

			if(actionsWithAuthor.size() > ABUSE_NUMBER && ((float) actionsWithAuthor.size())/((float) actionsOverall.size()) > ABUSE_FRACTION){
				LOGGER.info(this.effectiveName + " hit abuse threshold downvoting " + author.effectiveName);
				return false;
			}
		}

		return true;
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

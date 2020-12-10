package main.valuestorage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;


public class VoteStuff {
	
	

	
	class VoteCheck extends TimerTask{
		private Logger LOGGER = LogManager.getLogger("VoteCheck");

		boolean lastEmpty = false;
		public long ticks;
		@Override
		public void run() {
			for(ConcurrentHashMap<Long, MemberInfo> memberInfoMap : ValueStorage.guildMemberMap.values()){
				for(MemberInfo m : memberInfoMap.values()) {
					int upLimit = calcLimitUp(m);
					int downLimit = calcLimitDown(m);


					if(m.downvotesLeft==0) {m.ticksPerIncrement++;}

					if(ticks%m.ticksPerIncrement==0) {

						if(m.upvotesLeft<upLimit) {
							m.upvotesLeft++;
							LOGGER.debug("U: " + m.upvotesLeft + " Time Tick: " + m.ticksPerIncrement + " ID: " + m.memberId);
						}

						if(m.downvotesLeft<downLimit) {
							m.downvotesLeft++;
							LOGGER.debug("D: " + m.downvotesLeft + "Time Tick: " + m.ticksPerIncrement + " ID: " + m.memberId);
						}

						if(m.downvotesLeft==downLimit&&m.ticksPerIncrement>1) {
							m.ticksPerIncrement--;
						}

					}
				}
			}
	    }
		
		//reaction "points" added every so often, rate reduced if bottom of quota hit
		public VoteCheck() {
			this.ticks = 0;
		}
		
	}
	
	UserVals vals;
	
	public static int calcLimitUp(KarmaCounts k) {
		return (int) (0.89*Math.pow((double) k.getKarma(), 0.383141762452) + 2.0);
	}
	public static int calcLimitUp(MemberInfo memberInfo){
		return (int) (0.89*Math.pow((double) memberInfo.getKarma(), 0.383141762452) + 2.0);
	}
	
	public static int calcLimitDown(KarmaCounts k) {
		return (int) (0.7*Math.pow((double) k.getKarma(), 0.383141762452) + 1.0);
	}

	public static int calcLimitDown(MemberInfo memberInfo) {
		return (int) (0.7*Math.pow((double) memberInfo.getKarma(), 0.383141762452) + 1.0);
	}
	
	public VoteStuff(UserVals vals){
		this.vals = vals;
		
		Timer timer = new Timer(); 
        TimerTask task = new VoteCheck();
          
       timer.schedule(task, 1800000, 1800000); //30 mins
//        timer.schedule(task, 5000, 5000); //5 secs
	}
}

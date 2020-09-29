package main.valueStorage;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;



public class Vote_Stuff {
	
	

	
	class VoteCheck extends TimerTask{
		boolean lastEmpty = false;
		public HashMap<Long,KarmaCounts> karmaCounter;
		public long ticks;
		@Override
		public void run() {
			for(KarmaCounts k : karmaCounter.values() ) {
				int upLimit = calcLimitUp(k) + 4;
				int downLimit = calcLimitDown(k) + 1;
				
				
				if(k.downLeft==0) {k.timerPerTick++;}
				
				if(ticks%k.timerPerTick==0) {
					
					if(k.upLeft<upLimit) {
						k.upLeft++;
//						System.out.println("U: " + k.upLeft);
//						System.out.println("Time Tick: " + k.timerPerTick);
					}
										
					if(k.downLeft<downLimit) {
						k.downLeft++;
//						System.out.println("D: " + k.downLeft);
//						System.out.println("Time Tick: " + k.timerPerTick);
					}
					
					if(k.downLeft==downLimit&&k.timerPerTick>1) {
						k.timerPerTick--;
					}
				}				
			}
			vals.saveToFile(SAVE_TYPE.KARMA);
	    }
		
		//reaction "points" added every so often, rate reduced if bottom of quota hit
		public VoteCheck(HashMap<Long,KarmaCounts> karmaCounter) {
			this.karmaCounter = karmaCounter;		
			this.ticks = 0;
		} 
		
	}
	
	User_Vals vals;
	
	public static int calcLimitUp(KarmaCounts k) {
		return (int) Math.sqrt((double)k.getKarma());
	}
	
	public static int calcLimitDown(KarmaCounts k) {
		return (int) Math.pow((double) k.getKarma(), 0.333333);
	}
	
	public Vote_Stuff(User_Vals vals){
		this.vals = vals;
		
		Timer timer = new Timer(); 
        TimerTask task = new VoteCheck(vals.karmaCounter); 
          
        timer.schedule(task, 900000, 900000); //15 mins
//        timer.schedule(task, 5000, 5000); //5 secs
	}
}

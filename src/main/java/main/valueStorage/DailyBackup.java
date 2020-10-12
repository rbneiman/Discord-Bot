package main.valueStorage;

import java.util.TimerTask;


public class DailyBackup extends TimerTask{
	private User_Vals vals;
	
	public void run() {
		User_Vals.backup++;
		vals.saveBackup(User_Vals.backup);
		vals.saveToFile(SAVE_TYPE.ALL);
		System.out.println("BACKUP COMPLETE");
    }
	
	public DailyBackup(User_Vals vals){
		super();
		this.vals = vals;
	}
}
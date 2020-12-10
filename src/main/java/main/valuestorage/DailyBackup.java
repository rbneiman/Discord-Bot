package main.valuestorage;

import java.util.TimerTask;


public class DailyBackup extends TimerTask{
	private UserVals vals;
	
	public void run() {
//		User_Vals.backup++;
//		vals.saveBackup(User_Vals.backup);
//		vals.saveToFile(SAVE_TYPE.ALL);
		System.out.println("BACKUP COMPLETE");
    }
	
	public DailyBackup(UserVals vals){
		super();
		this.vals = vals;
	}
}
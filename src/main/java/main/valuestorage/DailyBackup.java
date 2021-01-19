package main.valuestorage;

import main.MiscUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.TimerTask;


public class DailyBackup extends TimerTask{
	private static final Logger LOGGER = LogManager.getLogger(DailyBackup.class);

	public void run() {
		DatabaseManager.backupDatabase();
		LOGGER.info("BACKUP COMPLETE");
    }
	
	public DailyBackup(){
		super();
	}
}
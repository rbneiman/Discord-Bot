package main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;


public class ConfigStorage {
	private static Logger LOGGER = LogManager.getLogger(ConfigStorage.class);

	public static boolean initialized = false;
	
	public static String discordToken;
	public static String googleApiKey;
	
	public static String botdataPath;
	public static String databasePath;
	public static String databaseBackupPath;
	
	public static long mainGuildID;
	
	public static long classCategory;
	public static long privateVoiceCategory;
	
	public static long botspamChannel;
	
	public static long botHelperID;
	public static long developerID;
	public static long specialUserID;
	
	public static void getConfig() {

		try {			
			
			String content = new String(Files.readAllBytes(Paths.get("bot_config.json")));
			JSONObject configJson = new JSONObject(content);
 
			discordToken = (String) configJson.get("discord_token");
			googleApiKey = (String) configJson.get("google_apikey");
			
			botdataPath = (String) configJson.get("botdata_path");
			databasePath = (String) configJson.get("database_path");
			databaseBackupPath = (String) configJson.get("database_backup_path");

			mainGuildID = (Long) configJson.get("main_guild_id");
			
			classCategory = (Long) configJson.get("class_category");
			privateVoiceCategory = (Long) configJson.get("private_voice_category");
			
			botspamChannel = (Long) configJson.get("bot_spam_channel");
			
			developerID = (Long) configJson.get("developer_id");
			botHelperID = (Long) configJson.get("bot_helper_id");
			specialUserID = (Long) configJson.get("special_user_id");
			LOGGER.trace("Using dev id " + developerID);
			initialized = true;
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}
}

package main;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.*;


public class ConfigStorage {
	
	public static boolean initialized = false;
	
	public static String discordToken;
	public static String googleApiKey;
	
	public static String botdataPath;
	
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
			
			mainGuildID = (Long) configJson.get("main_guild_id");
			
			classCategory = (Long) configJson.get("class_category");
			privateVoiceCategory = (Long) configJson.get("private_voice_category");
			
			botspamChannel = (Long) configJson.get("bot_spam_channel");
			
			developerID = (Long) configJson.get("developer_id");
			botHelperID = (Long) configJson.get("bot_helper_id");
			specialUserID = (Long) configJson.get("special_user_id");
			System.out.println(developerID);
			
			initialized = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

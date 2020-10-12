package main.valueStorage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.json.*;

import main.ConfigStorage;



public class ValueStore {
	
	private static JSONObject dataJson;
	private static Path path;
	
	public boolean init() {
		try {			
			path = Paths.get(ConfigStorage.botdataPath + "UserData.json");
			String content = new String(Files.readAllBytes(path));
			JSONObject dataJson = new JSONObject(content);
 
			

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static HashMap<Long,MemberInfo> getConfig() {
		HashMap<Long,MemberInfo> out = new HashMap<Long,MemberInfo>();
		
		try {			
			JSONArray members = (JSONArray) dataJson.getJSONArray("Members");
			
		
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return out;
	}
	
	public static boolean save() {
		
		try {			
			path = Paths.get(ConfigStorage.botdataPath + "UserData.json");
			String content = new String(Files.readAllBytes(path));
			JSONObject dataJson = new JSONObject(content);
 
			Files.write(path, dataJson.toString().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}

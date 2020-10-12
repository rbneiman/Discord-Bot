package main.valueStorage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

import main.ConfigStorage;
import main.Main;
import main.valueStorage.Vote_Stuff.VoteCheck;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction;


public class User_Vals {
	
	
	
	private Guild guild;
	
	public final HashMap<Long,Integer> balanceSheet;
	public final HashMap<Long,Room_Info> roomOwners;
	public final HashMap<Long,HashSet<String>> coursesEnrolled;
	public final HashMap<String,TextChannel> courseList; //names are all uppercase
	public final HashMap<Long,KarmaCounts> karmaCounter;
	public final HashMap<String,CompanyInfo> companyList;
	public final Vote_Stuff vote_stuff;
	public static Long backup;
	public static String botDataPath;

	
	public User_Vals(){
		this.guild = Main.api.getGuildById(ConfigStorage.mainGuildID);
		if(botDataPath == null)
			
		backup = getBackup();
		balanceSheet  = getFromFile(SAVE_TYPE.BALANCE);
		roomOwners  = new HashMap<Long,Room_Info>();
		coursesEnrolled = getFromFile(SAVE_TYPE.COURSE);
		courseList = new HashMap<String, TextChannel>();
		karmaCounter = getFromFile(SAVE_TYPE.KARMA);
		companyList = getFromFile(SAVE_TYPE.COMPANIES);
		backup++;
		saveBackup(backup);
		System.out.println("Backup Num: " + backup);
		saveToFile(SAVE_TYPE.ALL);
		for(KarmaCounts k : karmaCounter.values()) {
			k.timerPerTick = 1;
		}
		vote_stuff = new Vote_Stuff(this);
		
		for(TextChannel m : this.guild.getCategoryById(ConfigStorage.classCategory).getTextChannels()) {
			if(!m.getName().contains("class")&&!m.getName().contains("spam")) {
				courseList.put(m.getName().toUpperCase(), m);
				System.out.println(m.getName());
			}
		}
		
		for(VoiceChannel v : this.guild.getCategoryById(ConfigStorage.privateVoiceCategory).getVoiceChannels()) {
			if(v.getMembers().isEmpty()) {
				this.guild.getCategoryById(ConfigStorage.privateVoiceCategory).getVoiceChannels().forEach(k -> k.delete().queue());
			}
			else {
				roomOwners.put(v.getIdLong(), new Room_Info(this,v.getIdLong(),v));
			}
		}
		
		Timer timer = new Timer(); 	
		DailyBackup backupTask = new DailyBackup(this);		
		timer.schedule(backupTask, 3600000*24, 3600000*24); //24 hours
//		timer.schedule(backupTask, 10000, 10000);
	}
	
	
	
	public String leaderBoard(Guild g) {
		String out = "TOP KARMA SCORES:\n";
		Integer scores[] = new Integer[5];
		String names[] = new String[5];
		
		for(Map.Entry<Long, KarmaCounts > m: karmaCounter.entrySet()) {
			KarmaCounts i = m.getValue();
			long k = m.getKey();
			for(int j=0; j<5; j++) {
				if(g.getMemberById(k)==null) {break;}
				if(scores[j]==null) {
					scores[j] = i.getKarma();
					names[j] = g.getMemberById(k).getEffectiveName();
					break;
				}
				else if(i.getKarma()>scores[j]) {
					int temp = scores[j];
					String tempS = names[j];
					scores[j] = i.getKarma();
					names[j] = g.getMemberById(k).getEffectiveName();
					for(int n=j; n<5; n++) {
						if(scores[n]==null) {
							scores[n] = temp;
							names[n] = tempS;
							break;
						}
						else if(temp >= scores[n]){
							int temp2 = scores[n];
							String tempS2 = names[n];
							scores[n] = temp;
							names[n] = tempS;
							temp = temp2;
							tempS = tempS2;
						}
					}
					break;
				}
			}
			
		}
		
		for(Integer i=0; i<5; i++) {
			if(names[i] != null) {
				out+= names[i] + ": " + scores[i] + "\n";
			}
		}
		
		return out;
	}
	
	public String leaderBoardDown(Guild g) {
		String out = "MOST DOWNVOTED:\n";
		Integer scores[] = new Integer[5];
		String names[] = new String[5];
		
		for(Map.Entry<Long, KarmaCounts > m: karmaCounter.entrySet()) {
			KarmaCounts i = m.getValue();
			long k = m.getKey();
			for(int j=0; j<5; j++) {
				if(g.getMemberById(k)==null) {break;}
				if(scores[j]==null) {
					scores[j] = i.downvotes;
					names[j] = g.getMemberById(k).getEffectiveName();
					break;
				}
				else if(i.downvotes>scores[j]) {
					int temp = scores[j];
					String tempS = names[j];
					scores[j] = i.downvotes;
					names[j] = g.getMemberById(k).getEffectiveName();
					for(int n=j; n<5; n++) {
						if(scores[n]==null) {
							scores[n] = temp;
							names[n] = tempS;
							break;
						}
						else if(temp >= scores[n]){
							int temp2 = scores[n];
							String tempS2 = names[n];
							scores[n] = temp;
							names[n] = tempS;
							temp = temp2;
							tempS = tempS2;
						}
					}
					break;
				}
			}
			
		}
		
		for(Integer i=0; i<5; i++) {
			if(names[i] != null) {
				out+= names[i] + ": " + scores[i] + "\n";
			}
		}
		
		return out;
	}
	
	public String leaderBoardUp(Guild g) {
		String out = "MOST UPVOTED:\n";
		Integer scores[] = new Integer[5];
		String names[] = new String[5];
		
		for(Map.Entry<Long, KarmaCounts > m: karmaCounter.entrySet()) {
			KarmaCounts i = m.getValue();
			long k = m.getKey();
			for(int j=0; j<5; j++) {
				if(g.getMemberById(k)==null) {break;}
				if(scores[j]==null) {
					scores[j] = i.upvotes;
					names[j] = g.getMemberById(k).getEffectiveName();
					break;
				}
				else if(i.upvotes>scores[j]) {
					int temp = scores[j];
					String tempS = names[j];
					scores[j] = i.upvotes;
					names[j] = g.getMemberById(k).getEffectiveName();
					for(int n=j; n<5; n++) {
						if(scores[n]==null) {
							scores[n] = temp;
							names[n] = tempS;
							break;
						}
						else if(temp >= scores[n]){
							int temp2 = scores[n];
							String tempS2 = names[n];
							scores[n] = temp;
							names[n] = tempS;
							temp = temp2;
							tempS = tempS2;
						}
					}
					break;
				}
			}
			
		}
		
		for(Integer i=0; i<5; i++) {
			if(names[i] != null) {
				out+= names[i] + ": " + scores[i] + "\n";
			}
		}
		
		return out;
	}
	
	public String leaderBoardMean(Guild g) {
		String out = "LOWEST UNCAPPED KARMA:\n";
		Integer scores[] = new Integer[5];
		String names[] = new String[5];
		
		for(Map.Entry<Long, KarmaCounts > m: karmaCounter.entrySet()) {
			KarmaCounts i = m.getValue();
			long k = m.getKey();
			for(int j=0; j<5; j++) {
				if(g.getMemberById(k)==null) {break;}
				if(scores[j]==null) {
					scores[j] = i.upvotes-i.downvotes;
					names[j] = g.getMemberById(k).getEffectiveName();
					break;
				}
				else if(i.upvotes-i.downvotes<scores[j]) {
					int temp = scores[j];
					String tempS = names[j];
					scores[j] = i.upvotes-i.downvotes;
					names[j] = g.getMemberById(k).getEffectiveName();
					for(int n=j; n<5; n++) {
						if(scores[n]==null) {
							scores[n] = temp;
							names[n] = tempS;
							break;
						}
						else if(temp <= scores[n]){
							int temp2 = scores[n];
							String tempS2 = names[n];
							scores[n] = temp;
							names[n] = tempS;
							temp = temp2;
							tempS = tempS2;
						}
					}
					break;
				}
			}
			
		}
		
		for(Integer i=0; i<5; i++) {
			if(names[i] != null) {
				out+= names[i] + ": " + scores[i] + "\n";
			}
		}
		
		return out;
	}
	
	public void createRoom(Guild g, long owner, int size, MessageChannel fromChannel, String name, String[] others) {
		roomOwners.put(owner, new Room_Info(this,g,owner,fromChannel,size,name,others));
//		saveRoomsToFile();
	}
	
	
	
	
	public void addCourses(MessageChannel c, long userID, String[] words) {
		String temp = "Added:```\n";
		if(!coursesEnrolled.containsKey(userID)) {
			coursesEnrolled.put(userID, new HashSet<String>());
		}
		boolean didAdd = false;
		for(int i=2; i<words.length && words[i]!=null; i++) {
			if(courseList.containsKey(words[i].toUpperCase())) {			
				if(coursesEnrolled.get((Long) userID).add(words[i].toUpperCase())) {
					didAdd = true;
					temp += " " + words[i].toUpperCase();
					courseList.get(words[i].toUpperCase())
					.putPermissionOverride(
							guild.getMemberById(userID))
					.setAllow(
							EnumSet.of(Permission.MESSAGE_READ)).queue();
				}
			}
		}
		if(!didAdd) {
			c.sendMessage("No courses added").queue();
		}
		else {
			temp += "```";
			c.sendMessage(temp).queue();
		}
		
		saveToFile(SAVE_TYPE.COURSE);
	}
	
	public void dropCourses(MessageChannel c, long userID, String[] words) {
		String temp = "Dropped:```\n";
		if(!coursesEnrolled.containsKey(userID)) {
			coursesEnrolled.put(userID, new HashSet<String>());
		}
		boolean didDrop = true;
		for(int i=2; i<words.length && words[i]!=null; i++) {
			if(coursesEnrolled.get((Long) userID).remove(words[i].toUpperCase())) {
				if(courseList.containsKey(words[i].toUpperCase())) {								
					courseList.get(words[i].toUpperCase()).putPermissionOverride(guild.getMemberById(userID)).setDeny(EnumSet.of(Permission.MESSAGE_READ)).queue();		
				}
				didDrop = true;			
				temp += " " + words[i].toUpperCase();
			}
		}
		
		if(!didDrop) {
			c.sendMessage("No courses dropped").queue();
		}
		else {
			temp += "```";
			c.sendMessage(temp).queue();
		}
		
		saveToFile(SAVE_TYPE.COURSE);
	}
	
	public void listCourses(MessageChannel c, long userID) {
		if(coursesEnrolled.containsKey(userID)) {
			String temp = "Your courses:```\n";
			for(String s : coursesEnrolled.get(userID)) {
				temp += " " + s;
			}
			temp += "```";
			c.sendMessage(temp).queue();
		}
		else {
			c.sendMessage("No courses have been added!").queue();
		}	
		
		saveToFile(SAVE_TYPE.COURSE);
	}
	
	public void allCourses(MessageChannel c) {
		String temp = "Available courses:```\n";
		for(String s : courseList.keySet()) {
			temp += " " + s;			
		}
		temp += "```";
		c.sendMessage(temp).queue();
		
	}
	
	@SuppressWarnings("unchecked")
	public static <k, v> HashMap<k, v> getFromFile(SAVE_TYPE t){
		try {
			File hFile;
			
			if(t==SAVE_TYPE.BALANCE) {
				hFile = new File(ConfigStorage.botdataPath + "playerBalances"+backup+".txt");
			}else if(t==SAVE_TYPE.COURSE) {
				hFile = new File(ConfigStorage.botdataPath + "courses"+backup+".txt");
			}else if(t==SAVE_TYPE.KARMA) {
				hFile = new File(ConfigStorage.botdataPath + "Karma"+backup+".txt");
			}else if(t==SAVE_TYPE.COMPANIES) {
				hFile = new File(ConfigStorage.botdataPath + "companies"+backup+".txt");
			}else {
				return null;
			}
			
			FileInputStream fis = new FileInputStream(hFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			
			
			HashMap<k, v> h = (HashMap<k, v>) ois.readObject();
			ois.close();
			return h;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static <k, v> void saveToFile(HashMap<k,v> hMap, String fileName){
	
		try {
			File hFile = new File(fileName);	
			
			FileOutputStream fos = new FileOutputStream(hFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			
			
			oos.writeObject(hMap);
			fos.close();	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void saveToFile(SAVE_TYPE t){		
		
		if(t==SAVE_TYPE.BALANCE) {
			saveToFile(balanceSheet,ConfigStorage.botdataPath + "playerBalances" + backup + ".txt");
		}else if(t==SAVE_TYPE.COURSE) {
			saveToFile(coursesEnrolled, ConfigStorage.botdataPath + "courses" + backup + ".txt");
		}else if(t==SAVE_TYPE.KARMA) {
			saveToFile(karmaCounter, ConfigStorage.botdataPath + "Karma" + backup + ".txt");
		}else if(t==SAVE_TYPE.COMPANIES) {
			saveToFile(companyList, ConfigStorage.botdataPath + "companies" + backup + ".txt");
		}else if(t==SAVE_TYPE.ALL) {
			saveToFile(balanceSheet, ConfigStorage.botdataPath + "playerBalances" + backup + ".txt");
			saveToFile(coursesEnrolled, ConfigStorage.botdataPath + "courses" + backup + ".txt");
			saveToFile(karmaCounter, ConfigStorage.botdataPath + "Karma" + backup + ".txt");
			saveToFile(companyList, ConfigStorage.botdataPath + "companies" + backup + ".txt");
		}
		
	}
	
	
	public Long getBackup() {
		try {
			File hFile = new File(ConfigStorage.botdataPath + "backup.txt");

			
			FileInputStream fis = new FileInputStream(hFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			
			
			Long h = (Long) ois.readObject();
			ois.close();
			return h;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	public void saveBackup(Long backup) {
		try {
			File hFile = new File(ConfigStorage.botdataPath + "backup.txt");
			FileOutputStream fos = new FileOutputStream(hFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(backup);
	        fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	
}

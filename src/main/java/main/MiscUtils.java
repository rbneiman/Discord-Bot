package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;

import main.valuestorage.CompanyInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MiscUtils {
	private static final Logger LOGGER = LogManager.getLogger(MiscUtils.class);

	public static String[] splitWords(String s) {
		String[] words=new String[10];
		words[0]="";
		int index=0;
		int lastSpace=-1;
		int start=0;
		boolean ignoreSpaces = false;
		
		for(int i=0;i<s.length();i++) {
			if((s.charAt(i)=='!')) {
				start=i;
				if(i!=0) {
					lastSpace=i-1;
				}
			}
		}
		for(int i=start;i<s.length()&&index+1<words.length;i++) {			
			if(s.charAt(i)==' ' && (!ignoreSpaces)) {
				if(i-lastSpace>1) {
					index++;
					words[index]="";
				}
				lastSpace=i;
			}
			else if((s.charAt(i)=='"' || s.charAt(i)=='“' || s.charAt(i)=='”') && (!ignoreSpaces)) {
				ignoreSpaces = true;
			}
			else if((s.charAt(i)=='"' || s.charAt(i)=='“' || s.charAt(i)=='”') && (ignoreSpaces)) {
				ignoreSpaces = false;
				index++;
				words[index]="";
				lastSpace=i;
			}
			else {				
				words[index]+=s.charAt(i);
			}
		}
		if(words[index].contentEquals("")) words[index] = null;
		return words;
	}
	
	public static String wolframRead(String input) {
		String out = "";
		 
		try {
			String encodeURL=URLEncoder.encode( input, "UTF-8" );
            URL url = new URL("http://api.wolframalpha.com/v1/result?appid=2T7ER2-67WVLU6L9U&i="+encodeURL);
            // read text returned by server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            
            int chara;
            while((chara = (in.read()))>0) {
            	out+= (char) chara;
            }
            in.close();
             
        }
        catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());
        }
        catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }
		
		return out;
	}
	
	public static String getIp() throws Exception {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            String ip = in.readLine();
            return ip;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}
	
	public static String padSpaces(String s, int minSize) {
		
		for(int i=s.length(); i<minSize; i++) {
			s += " ";
		}
		
		return s;
	}
	
	public static boolean CheckServerOnline() throws Exception {
		return true;
//        URL whatismyip = new URL("http://checkip.amazonaws.com");
//        BufferedReader in = null;
//        try {
//            in = new BufferedReader(new InputStreamReader(
//                    whatismyip.openStream()));
//            String ip = in.readLine();
//            return ip.compareTo("66.69.76.202")!=0;
//        } finally {
//            if (in != null) {
//                try {
//                    in.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
	}
	
	public static void jobHelper(HashMap<String, CompanyInfo>hmap, MessageChannel channel, String[] words) {
		
		if(words[1]==null) {
			channel.sendMessage("```diff\n"
        			+ "𝐕𝐚𝐥𝐢𝐝 𝐢𝐧𝐩𝐮𝐭𝐬 𝐟𝐨𝐫 𝐣𝐨𝐛 𝐬𝐞𝐚𝐫𝐜𝐡𝐞𝐫:\n"
        			+ "- NOTE: inputs containing spaces need to be entirely surrounded by double quotes\n"
        			+ "\n!job list                                 - list companies added so far\n"
        			+ "\n!job list   <company>                     - list jobs within a company that contain links\n"
        			+ "\n!job list   <company> <job>               - list links associated with a job in a company\n"
        			+ "\n!job add    <company> <logo>              - add a logo image to display when searching up a company\n"
        			+ "\n!job add    <company> <job> <url>         - add a link to the job searcher\n"
        			+ "\n!job remove <company> <job> <url>         - remove a link from the job searcher\n"
        			+ "```").queue();
		}		
		else if(words[1].toLowerCase().contentEquals("list")) {
			if(words[2] == null) {
				channel.sendMessage(CompanyInfo.getCompanies(hmap).toString()).queue();
			}
			else {
				if(words[3] == null) {
					channel.sendMessage(CompanyInfo.getJobs(hmap, words[2].toLowerCase()).toString()).queue();
				}
				else if(words[2] != null){
					channel.sendMessage(CompanyInfo.getLinks(hmap, words[2].toLowerCase(), words[3].toLowerCase()).toString()).queue();
				}
			}			
		}
		else if(words[1].toLowerCase().contentEquals("add")){
			if(words[2]==null) return;
			if(words[4] == null) {
				CompanyInfo.setLogo(hmap, words[2].toLowerCase(), words[3]);
			}
			else {
				if(words[3]==null || words[4]==null) return;
				
//				MessageChannel  c = (MessageChannel) channel.getJDA().getGuildById(ConfigStorage.mainGuildID).getGuildChannelById("REDACTED");
//				c.sendMessage(CompanyInfo.addLink(hmap, words[2].toLowerCase(), words[3].toLowerCase(), words[4])).queue();
			}	
		}
		else if(words[1].toLowerCase().contentEquals("remove")){
			CompanyInfo.removeLink(hmap, words[2].toLowerCase(), words[3].toLowerCase(), words[4]);	
		}
		
		return;
	}
	
	public static String getFaceVid(String url) {
		//TODO
		return null;
	}
	
	
	static HashMap<String, Member> memberNames;
	private static void getMembers(Guild g) {
		List<Member> members = g.getMembers();
		memberNames = new HashMap<String,Member>();
		members.forEach(m -> memberNames.put(m.getEffectiveName(),m));
		
	}
	
	
	public static Member findMember(Guild g, String s) {
		if(memberNames == null) {
			getMembers(g);
		}
		Member out;
		try {
			out = g.getMemberById(s);
		}
		catch(Exception e) {
			try {
				out = g.getMemberByTag(s);
			}
			catch(IllegalArgumentException e2){
				out = memberNames.get(s);
			}
		}
		
		return out;
	}	
	
	
	public static void karmaLog(String event) {
		ZonedDateTime now = ZonedDateTime.now();
		String out = String.format("%1$tY-%1$tm-%1$td %1$tk:%1$tM:%1$tS - ", now) + event;
		LOGGER.trace(out);
		try {
		    BufferedWriter writer = new BufferedWriter(new FileWriter(ConfigStorage.botdataPath + "karmaLog.txt", true));
		    writer.append('\n');
		    writer.append(out);
		    writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void auditLogSearch(Guild guild) {
//		AuditLogPaginationAction auditLogs = guild.retrieveAuditLogs();
//		auditLogs.type(ActionType.MESSAGE_UPDATE);
//		auditLogs.queue( (entries) ->
//        {
//
//        });
	}


	//Fortnite dance 0-9, lecture 10
	public static String asciiArchive(int id) {
		String out = "";
		switch (id){
			case(0):
				out = "⠀⠀⠀⣶⣿⣶\r\n" + 
						"⠀⠀⠀⣿⣿⣿⣀\r\n" + 
						"⠀⣀⣿⣿⣿⣿⣿⣿\r\n" + 
						"⣶⣿⠛⣭⣿⣿⣿⣿\r\n" + 
						"⠛⠛⠛⣿⣿⣿⣿⠿\r\n" + 
						"⠀⠀⠀⠀⣿⣿⣿\r\n" + 
						"⠀⠀⣀⣭⣿⣿⣿⣿⣀\r\n" + 
						"⠀⠤⣿⣿⣿⣿⣿⣿⠉\r\n" + 
						"⠀⣿⣿⣿⣿⣿⣿⠉\r\n" + 
						"⣿⣿⣿⣿⣿⣿\r\n" + 
						"⣿⣿⣶⣿⣿\r\n" + 
						"⠉⠛⣿⣿⣶⣤\r\n" + 
						"⠀⠀⠉⠿⣿⣿⣤\r\n" + 
						"⠀⠀⣀⣤⣿⣿⣿\r\n" + 
						"⠀⠒⠿⠛⠉⠿⣿\r\n" + 
						"⠀⠀⠀⠀⠀⣀⣿⣿\r\n" + 
						"⠀⠀⠀⠀⣶⠿⠿⠛";
			break;
			case(1):
				out = "⠀⠀⠀⠀⠀⠀⠀⠀⠀⣤⣤\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⣿\r\n" + 
						"⠀⠀⣶⠀⠀⣀⣤⣶⣤⣉⣿⣿⣤⣀\r\n" + 
						"⠤⣤⣿⣤⣿⠿⠿⣿⣿⣿⣿⣿⣿⣿⣿⣀\r\n" + 
						"⠀⠛⠿⠀⠀⠀⠀⠉⣿⣿⣿⣿⣿⠉⠛⠿⣿⣤\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠿⣿⣿⣿⠛⠀⠀⠀⣶⠿\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⣀⣿⣿⣿⣿⣤⠀⣿⠿\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⣶⣿⣿⣿⣿⣿⣿⣿⣿\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠿⣿⣿⣿⣿⣿⠿⠉⠉\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠉⣿⣿⣿⣿⠿\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⣿⠉\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⣛⣿⣭⣶⣀\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⣿⣿⣿\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠉⠛⣿\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⣿⣿\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣉⠀⣶⠿\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⣶⣿⠿\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠛⠿⠛";
			break;
			case(2):
				out = "⠀⠀⠀⠀⠀⠀⠀⠀⠀⣤⣶\r\n" + 
						"⠀⠀⠀⠀⠀⣀⣀⠀⣶⣿⣿⠶\r\n" + 
						"⣶⣿⠿⣿⣿⣿⣿⣿⣿⣿⣿⣤⣤\r\n" + 
						"⠀⠉⠶⣶⣀⣿⣿⣿⣿⣿⣿⣿⠿⣿⣤⣀\r\n" + 
						"⠀⠀⠀⣿⣿⠿⠉⣿⣿⣿⣿⣭⠀⠶⠿⠿\r\n" + 
						"⠀⠀⠛⠛⠿⠀⠀⣿⣿⣿⣉⠿⣿⠶\r\n" + 
						"⠀⠀⠀⠀⠀⣤⣶⣿⣿⣿⣿⣿\r\n" + 
						"⠀⠀⠀⠀⠀⣿⣿⣿⣿⣿⣿⣿⠒\r\n" + 
						"⠀⠀⠀⠀⣀⣿⣿⣿⣿⣿⣿⣿\r\n" + 
						"⠀⠀⠀⠀⠀⣿⣿⣿⠛⣭⣭⠉\r\n" + 
						"⠀⠀⠀⠀⠀⣿⣿⣭⣤⣿⠛\r\n" + 
						"⠀⠀⠀⠀⠀⠛⠿⣿⣿⣿⣭\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⣿⣿⠉⠛⠿⣶⣤\r\n" + 
						"⠀⠀⠀⠀⠀⠀⣀⣿⠀⠀⣶⣶⠿⠿⠿\r\n" + 
						"⠀⠀⠀⠀⠀⠀⣿⠛\r\n" + 
						"⠀⠀⠀⠀⠀⠀⣭⣶";
			break;
			case(3):
				out = "⠀⠀⠀⠀⠀⠀⣶⣿⣶\r\n" + 
						"⠀⠀⠀⣤⣤⣤⣿⣿⣿\r\n" + 
						"⠀⠀⣶⣿⣿⣿⣿⣿⣿⣿⣶\r\n" + 
						"⠀⠀⣿⣿⣿⣿⣿⣿⣿⣿⣿\r\n" + 
						"⠀⠀⣿⣉⣿⣿⣿⣿⣉⠉⣿⣶\r\n" + 
						"⠀⠀⣿⣿⣿⣿⣿⣿⣿⣿⠿⣿\r\n" + 
						"⠀⣤⣿⣿⣿⣿⣿⣿⣿⠿⠀⣿⣶\r\n" + 
						"⣤⣿⠿⣿⣿⣿⣿⣿⠿⠀⠀⣿⣿⣤\r\n" + 
						"⠉⠉⠀⣿⣿⣿⣿⣿⠀⠀⠒⠛⠿⠿⠿\r\n" + 
						"⠀⠀⠀⠉⣿⣿⣿⠀⠀⠀⠀⠀⠀⠉\r\n" + 
						"⠀⠀⠀⣿⣿⣿⣿⣿⣶\r\n" + 
						"⠀⠀⠀⠀⣿⠉⠿⣿⣿\r\n" + 
						"⠀⠀⠀⠀⣿⣤⠀⠛⣿⣿\r\n" + 
						"⠀⠀⠀⠀⣶⣿⠀⠀⠀⣿⣶\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⣭⣿⣿\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⣤⣿⣿⠉";
			break;
			case(4):
				out = "⠀⠀⠀⠀⠀⠀⣤⣶⣶\r\n" + 
						"⠀⠀⠀⠀⠀⠀⣿⣿⣿⣿⣀⣀\r\n" + 
						"⠀⠀⠀⠀⠀⣀⣶⣿⣿⣿⣿⣿⣿\r\n" + 
						"⣤⣶⣀⠿⠶⣿⣿⣿⠿⣿⣿⣿⣿\r\n" + 
						"⠉⠿⣿⣿⠿⠛⠉⠀⣿⣿⣿⣿⣿\r\n" + 
						"⠀⠀⠉⠀⠀⠀⠀⠀⠀⣿⣿⣿⣿⣤⣤\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⣤⣶⣿⣿⣿⣿⣿⣿\r\n" + 
						"⠀⠀⠀⠀⠀⣀⣿⣿⣿⣿⣿⠿⣿⣿⣿⣿\r\n" + 
						"⠀⠀⠀⠀⣀⣿⣿⣿⠿⠉⠀⠀⣿⣿⣿⣿\r\n" + 
						"⠀⠀⠀⠀⣿⣿⠿⠉⠀⠀⠀⠀⠿⣿⣿⠛\r\n" + 
						"⠀⠀⠀⠀⠛⣿⣿⣀⠀⠀⠀⠀⠀⣿⣿⣀\r\n" + 
						"⠀⠀⠀⠀⠀⣿⣿⣿⠀⠀⠀⠀⠀⠿⣿⣿\r\n" + 
						"⠀⠀⠀⠀⠀⠉⣿⣿⠀⠀⠀⠀⠀⠀⠉⣿\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⣿⠀⠀⠀⠀⠀⠀⣀⣿\r\n" + 
						"⠀⠀⠀⠀⠀⠀⣀⣿⣿\r\n" + 
						"⠀⠀⠀⠀⠤⣿⠿⠿⠿";
			break;
			case(5):
				out = "⠀⠀⠀⠀⠀⠀⠀⣀⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\r\n" + 
						"⠀⠀⠀⠀⠀⠀⣿⣿⣿⣤⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣤⣤⣿\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠉⣿⣿⣿⣶⣿⣿⣿⣶⣶⣤⣶⣶⠶⠛⠉⠉\r\n" + 
						"⠀⠀⠀⠀⠀⠀⣤⣿⠿⣿⣿⣿⣿⣿⠀⠀⠉⠀⠀⠀⠀⠀⠀\r\n" + 
						"⠛⣿⣤⣤⣀⣤⠿⠉⠀⠉⣿⣿⣿⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀\r\n" + 
						"⠀⠉⠉⠉⠉⠉⠀⠀⠀⠀⠉⣿⣿⣿⣀⠀⠀⠀⠀⠀⠀⠀⠀\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣶⣿⣿⣿⣿⣿⠀⠀⠀⠀⠀⠀⠀\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⣿⣿⣿⣿⣿⠀⠀⠀⠀⠀⠀⠀\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⣿⣿⣿⣿⠛⠀⠀⠀⠀⠀⠀⠀\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⣿⣿⣿⣿⠀⠀⠀⠀⠀⠀⠀⠀\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⣛⣿⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⣶⣿⣿⠛⠿⣿⣿⣿⣶⣤⠀⠀⠀⠀⠀⠀\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⣿⠛⠉⠀⠀⠀⠛⠿⣿⣿⣶⣀⠀⠀⠀⠀\r\n" + 
						"⠀⠀⠀⠀⠀⠀⣿⣀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠛⠿⣶⣤⠀⠀\r\n" + 
						"⠀⠀⠀⠀⠀⠛⠿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣿⣿⠿⠀\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠛⠉⠉⠀";
			break;
			case(6):
				out = "⠀⠀⣀\r\n" + 
						"⠀⠿⣿⣿⣀\r\n" + 
						"⠀⠉⣿⣿⣀\r\n" + 
						"⠀⠀⠛⣿⣭⣀⣀⣤\r\n" + 
						"⠀⠀⣿⣿⣿⣿⣿⠛⠿⣶⣀\r\n" + 
						"⠀⣿⣿⣿⣿⣿⣿⠀⠀⠀⣉⣶\r\n" + 
						"⠀⠀⠉⣿⣿⣿⣿⣀⠀⠀⣿⠉\r\n" + 
						"⠀⠀⠀⣿⣿⣿⣿⣿⣿⣿⣿\r\n" + 
						"⠀⣀⣿⣿⣿⣿⣿⣿⣿⣿⠿\r\n" + 
						"⠀⣿⣿⣿⠿⠉⣿⣿⣿⣿\r\n" + 
						"⠀⣿⣿⠿⠀⠀⣿⣿⣿⣿\r\n" + 
						"⣶⣿⣿⠀⠀⠀⠀⣿⣿⣿\r\n" + 
						"⠛⣿⣿⣀⠀⠀⠀⣿⣿⣿⣿⣶⣀\r\n" + 
						"⠀⣿⣿⠉⠀⠀⠀⠉⠉⠉⠛⠛⠿⣿⣶\r\n" + 
						"⠀⠀⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣿\r\n" + 
						"⠀⠀⣿⣿⠀⠀⠀⠀⠀⠀⠀⠀⠉⠉\r\n" + 
						"⣀⣶⣿⠛";
			break;
			case(7):
				out = "⠀⠀⠀⠀⠀⠀⠀⠀⣤⣿⣿⠶⠀⠀⣀⣀\r\n" + 
						"⠀⠀⠀⠀⠀⠀⣀⣀⣤⣤⣶⣿⣿⣿⣿⣿⣿\r\n" + 
						"⠀⠀⣀⣶⣤⣤⠿⠶⠿⠿⠿⣿⣿⣿⣉⣿⣿\r\n" + 
						"⠿⣉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠛⣤⣿⣿⣿⣀\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⣿⣿⣿⣿⣶⣤\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣤⣿⣿⣿⣿⠿⣛⣿\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⣿⠛⣿⣿⣿⣿\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣶⣿⣿⠿⠀⣿⣿⣿⠛\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⣿⠀⠀⣿⣿⣿\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠿⠿⣿⠀⠀⣿⣶\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⠛⠀⠀⣿⣿⣶\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⣿⣿⠤\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠿⣿\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣀\r\n" + 
						"⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣶⣿";
			break;
			case(8):
				out = "⠀⠀⠀⣀⣶⣀\r\n" + 
						"⠀⠀⠀⠒⣛⣭\r\n" + 
						"⠀⠀⠀⣀⠿⣿⣶\r\n" + 
						"⠀⣤⣿⠤⣭⣿⣿\r\n" + 
						"⣤⣿⣿⣿⠛⣿⣿⠀⣀\r\n" + 
						"⠀⣀⠤⣿⣿⣶⣤⣒⣛\r\n" + 
						"⠉⠀⣀⣿⣿⣿⣿⣭⠉\r\n" + 
						"⠀⠀⣭⣿⣿⠿⠿⣿\r\n" + 
						"⠀⣶⣿⣿⠛⠀⣿⣿\r\n" + 
						"⣤⣿⣿⠉⠤⣿⣿⠿\r\n" + 
						"⣿⣿⠛⠀⠿⣿⣿\r\n" + 
						"⣿⣿⣤⠀⣿⣿⠿\r\n" + 
						"⠀⣿⣿⣶⠀⣿⣿⣶\r\n" + 
						"⠀⠀⠛⣿⠀⠿⣿⣿\r\n" + 
						"⠀⠀⠀⣉⣿⠀⣿⣿\r\n" + 
						"⠀⠶⣶⠿⠛⠀⠉⣿\r\n" + 
						"⠀⠀⠀⠀⠀⠀⣀⣿\r\n" + 
						"⠀⠀⠀⠀⠀⣶⣿⠿";
			break;
			case(9):
				out = "⠀⠀⠀⠀⣀⣤\r\n" + 
						"⠀⠀⠀⠀⣿⠿⣶\r\n" + 
						"⠀⠀⠀⠀⣿⣿⣀\r\n" + 
						"⠀⠀⠀⣶⣶⣿⠿⠛⣶\r\n" + 
						"⠤⣀⠛⣿⣿⣿⣿⣿⣿⣭⣿⣤\r\n" + 
						"⠒⠀⠀⠀⠉⣿⣿⣿⣿⠀⠀⠉⣀\r\n" + 
						"⠀⠤⣤⣤⣀⣿⣿⣿⣿⣀⠀⠀⣿\r\n" + 
						"⠀⠀⠛⣿⣿⣿⣿⣿⣿⣿⣭⣶⠉\r\n" + 
						"⠀⠀⠀⠤⣿⣿⣿⣿⣿⣿⣿\r\n" + 
						"⠀⠀⠀⣭⣿⣿⣿⠀⣿⣿⣿\r\n" + 
						"⠀⠀⠀⣉⣿⣿⠿⠀⠿⣿⣿\r\n" + 
						"⠀⠀⠀⠀⣿⣿⠀⠀⠀⣿⣿⣤\r\n" + 
						"⠀⠀⠀⣀⣿⣿⠀⠀⠀⣿⣿⣿\r\n" + 
						"⠀⠀⠀⣿⣿⣿⠀⠀⠀⣿⣿⣿\r\n" + 
						"⠀⠀⠀⣿⣿⠛⠀⠀⠀⠉⣿⣿\r\n" + 
						"⠀⠀⠀⠉⣿⠀⠀⠀⠀⠀⠛⣿\r\n" + 
						"⠀⠀⠀⠀⣿⠀⠀⠀⠀⠀⠀⣿⣿\r\n" + 
						"⠀⠀⠀⠀⣛⠀⠀⠀⠀⠀⠀⠛⠿⠿⠿\r\n" + 
						"⠀⠀⠀⠛⠛";
			break;
			case(10):
				out = "```\nWe trust you have received the usual lecture from the local System\n" +
						"Administrator. It usually boils down to these three things:\n" +
						"\n" +
						"    #1) Respect the privacy of others.\n" +
						"    #2) Think before you type.\n" +
						"    #3) With great power comes great responsibility.```";
				break;
			default:
				out = "String ID not found!";
		}
		return out;
		
	}
}

class BotTimer extends TimerTask{
	public int counter = 0;
	public void run() { 		
		if(counter>0) {
//			System.out.print(counter+",");
			counter--;
//			System.out.println(counter);
		}				
    } 
}


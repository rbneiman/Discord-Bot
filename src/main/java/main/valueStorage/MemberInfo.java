package main.valueStorage;

import java.util.HashSet;

public class MemberInfo {
	
	public long guildId;
	public long memberId;
	
	public KarmaCounts karmaInfo;
	public int balance;
	public HashSet<String> courses;
	
	
	public MemberInfo(long guildId, long memberId, KarmaCounts karmaInfo, int balance, HashSet<String>courses) {
		this.guildId = guildId;
		this.memberId = memberId;
		this.karmaInfo = karmaInfo;
		this.balance = balance;
		this.courses = courses;
	}
	
//	public boolean save() {
//		
//	}
}

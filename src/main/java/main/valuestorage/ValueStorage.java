package main.valuestorage;


import main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ValueStorage {
    //guild_id -> map( member_id -> MemberInfo )
    public static final ConcurrentHashMap<Long,ConcurrentHashMap<Long,MemberInfo>> guildMemberMap = new ConcurrentHashMap<>();
    //user_id -> MemberInfo
    public static final ConcurrentHashMap<Long,UserInfo> userInfoMap = new ConcurrentHashMap<>();



    public static MemberInfo getMemberInfo(Member member){
        if(!guildMemberMap.containsKey(member.getGuild().getIdLong()))
            guildMemberMap.put(member.getGuild().getIdLong(), new ConcurrentHashMap<>());

        ConcurrentHashMap<Long,MemberInfo> memberInfoMap = guildMemberMap.get(member.getGuild().getIdLong());

        if(memberInfoMap.containsKey(member.getIdLong()))
            return memberInfoMap.get(member.getIdLong());

        ArrayList<MemberInfo> memberList = DatabaseManager.getMembersWithCondition("WHERE member_id = " + member.getIdLong() + " AND guild_id = " + member.getGuild().getIdLong(), null);
        MemberInfo out;
        if(memberList.size() == 0){
            out = new MemberInfo(member.getIdLong(), member.getGuild().getIdLong());
            DatabaseManager.addMember(out);
        }
        else
            out = memberList.get(0);

        memberInfoMap.put(member.getIdLong(), out);
        return out;
    }

    public static ArrayList<MemberInfo> getGuildMemberInfos(Guild guild){
        return DatabaseManager.getMembersWithCondition("WHERE guild_id = " + guild.getIdLong(), null);
    }

    public static UserInfo getUserInfo(User user){
        if(userInfoMap.containsKey(user.getIdLong()))
            return userInfoMap.get(user.getIdLong());

        ArrayList<UserInfo> userList = DatabaseManager.getUsersWithCondition("WHERE user_id = " + user.getIdLong(), null);

        UserInfo out;
        if(userList.size() == 0){
            out = new UserInfo(user.getIdLong(), 0);
            userInfoMap.put(user.getIdLong(), out);
        }
        else
            out = userList.get(0);

        return out;
    }

    public static ArrayList<CourseInfo> getGuildCourses(Guild guild){
        ArrayList<CourseInfo> infoList = DatabaseManager.getCourseInfosWithCondition("WHERE guild_id = " + guild.getIdLong(), null);
//        ArrayList<String> out = new ArrayList<>();
//        for(CourseInfo info : infoList)
//            out.add(info.name);
        return infoList;
    }

    public static boolean addCourse(TextChannel channel){
        ArrayList<CourseInfo> infoList = DatabaseManager.getCourseInfosWithCondition("WHERE channel_id = " + channel.getIdLong(), null);
        if(infoList.size() == 0){
            CourseInfo courseInfo = new CourseInfo(channel.getIdLong(), channel.getGuild().getIdLong(), channel.getName().toUpperCase());
            DatabaseManager.addCourseInfo(courseInfo);
            return true;
        }else{
            DatabaseManager.updateCourseInfo(channel.getIdLong(), "name", channel.getName().toUpperCase());
            return false;
        }
    }

    public static boolean removeCourse(TextChannel channel){
        ArrayList<CourseInfo> infoList = DatabaseManager.getCourseInfosWithCondition("WHERE channel_id = " + channel.getIdLong(), null);
        if(infoList.size() > 0){
            CourseInfo courseInfo = new CourseInfo(channel.getIdLong(), channel.getGuild().getIdLong(), channel.getName().toUpperCase());
            DatabaseManager.removeCourseInfo(courseInfo);
            DatabaseManager.removeCourseActionsWithCondition("WHERE channel_id = " + channel.getIdLong(), null);
            return true;
        }else{
            return false;
        }
    }

    public static ArrayList<String> getRegisteredCourses(Member member){
        ArrayList<CourseAction> actionList = DatabaseManager.getCourseActionsWithCondition("WHERE member_id = " + member.getIdLong() + " AND guild_id = " + member.getGuild().getIdLong(), null);
        ArrayList<String> out = new ArrayList<>();
        for(CourseAction action : actionList)
            out.add(action.name);
        return out;
    }

    public static void enrollCourse(Member member, TextChannel channel){
        PermissionOverride override = channel.getPermissionOverride(member);
        if(override == null)
            override = channel.createPermissionOverride(member).complete();
        override.getManager().setAllow(Permission.MESSAGE_READ).queue();
        ArrayList<CourseAction> actions = DatabaseManager.getCourseActionsWithCondition("WHERE member_id = " + member.getIdLong() + " AND guild_id = " + member.getGuild().getIdLong() + " AND channel_id = " + channel.getIdLong(), null);
        if(actions.size() == 0){
            CourseAction action = new CourseAction(member.getIdLong(), channel.getIdLong(), member.getGuild().getIdLong(), channel.getName().toUpperCase());
            DatabaseManager.addCourseAction(action);
        }
    }

    public static void dropCourse(Member member, TextChannel channel){
        ArrayList<CourseAction> actions = DatabaseManager.getCourseActionsWithCondition("WHERE member_id = " + member.getIdLong() + " AND guild_id = " + member.getGuild().getIdLong() + " AND channel_id = " + channel.getIdLong(), null);
        if(actions.size() != 0){
            PermissionOverride override = channel.getPermissionOverride(member);
            if(override == null)
                override = channel.createPermissionOverride(member).complete();
            override.getManager().setDeny(Permission.MESSAGE_READ).queue();
            DatabaseManager.removeCourseAction(member, channel);
        }else{
            ArrayList<CourseInfo>  infos = DatabaseManager.getCourseInfosWithCondition("WHERE guild_id = " + member.getGuild().getIdLong() + " AND channel_id = " + channel.getIdLong(), null);
            if(infos.size() != 0){
                PermissionOverride override = channel.getPermissionOverride(member);
                if(override == null)
                    return;
                override.getManager().setDeny(Permission.MESSAGE_READ).queue();
            }
        }

    }

    public static void addVoteAction(MemberInfo voter, MemberInfo author, boolean isUpvote){
        String notesString = voter.effectiveName + (isUpvote ? " upvoted " : " downvoted ") + author.effectiveName;
        DatabaseManager.addVoteAction(new VoteAction(voter.memberId, author.memberId, voter.guildId, isUpvote), notesString);
    }

    public static ArrayList<VoteAction> getVoteActions(Member voter){
        ArrayList<VoteAction> out = DatabaseManager.getVoteActionsWithCondition("WHERE voter_id = " + voter.getIdLong() + " AND guild_id = " + voter.getGuild().getIdLong(), null);
        return out;
    }

    public static ArrayList<VoteAction> getVoteActions(MemberInfo author, boolean isUpvote){
        ArrayList<VoteAction> out = DatabaseManager.getVoteActionsWithCondition("WHERE author_id = " + author.memberId + " AND guild_id = " + author.guildId + " AND is_upvote = '" + isUpvote + "'", null);
        return out;
    }

    public static ArrayList<VoteAction> getVoteActions(MemberInfo voter, MemberInfo author, boolean isUpvote){
        ArrayList<VoteAction> out = DatabaseManager.getVoteActionsWithCondition("WHERE voter_id = " + voter.memberId + " AND guild_id = " + voter.guildId + " AND author_id = " + author.memberId + " AND is_upvote = '" + isUpvote + "'", null);
        return out;
    }

    public static void updateMemberNames(List<MemberName> memberNames){
        HashMap<Long, String> existingMembers = DatabaseManager.getMemberNames();
        for(MemberName memberName : memberNames){
            if(existingMembers.containsKey(memberName.memberId)){
                DatabaseManager.updateMemberName(memberName);
            }else{
                DatabaseManager.addMemberName(memberName);
            }
        }

    }
}

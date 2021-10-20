package main.valuestorage;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseManager {
    public static DatabaseManager INSTANCE = new DatabaseManager();
    private static Logger LOGGER = LogManager.getLogger("DatabaseManager");
    private static String url;
//    public static final String urlBackup = "D:/SQL/SQLite/AlecBot/Bak/BotData";
    private static String urlBackup;
    private static Connection conn;

    public static void startDB(String url, String urlBackup){
        Connection temp = null;
        DatabaseManager.urlBackup = urlBackup;
        try {
            temp = DriverManager.getConnection("jdbc:sqlite:" + url);
        } catch (final Exception e) {
            e.printStackTrace();
            LOGGER.error("Database path does not exist: " + url);
            return;
        }
        DatabaseManager.url = url;
        conn = temp;
        LOGGER.info("Database connected");
    }

    public static ArrayList<MemberInfo> getMemberTemplate(){
        ArrayList<MemberInfo> out = new ArrayList<>();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM member_info");
            if (!rs.next() ) {
                ;
            }else{
                do {
                    MemberInfo temp = new MemberInfo(rs.getLong("member_id"),
                            rs.getLong("guild_id"),
                            rs.getInt("upvotes"),
                            rs.getInt("downvotes"),
                            rs.getInt("balance")
                    );
                    out.add(temp);
                }while (rs.next());
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return out;
    }

    public static void addMember(MemberInfo member){
        try {
            Statement st = conn.createStatement();
            st.execute("INSERT INTO member_info (member_id, guild_id, upvotes, downvotes)" +
                    String.format("Values (%d,%d,%d,%d)", member.memberId, member.guildId, member.getUpvotes(), member.getDownvotes()));
            st.close();
            PreparedStatement st2 = conn.prepareStatement( "INSERT INTO member_names (member_id, name)" +
                    String.format("Values (%d,(?))", member.memberId));
            st2.setString(1, member.effectiveName);
            st2.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addUser(UserInfo user){
        try {
            Statement st = conn.createStatement();
            st.execute("INSERT INTO user_info (user_id, gold_tokens)" +
                    String.format("Values (%d,%d)", user.userId, user.goldTokens));
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addCourseAction(CourseAction action){
        try {
            PreparedStatement st = conn.prepareStatement("INSERT INTO course_actions (member_id, channel_id, guild_id, name)" +
                    String.format("Values (%d,%d,%d,(?))", action.memberId, action.channelId, action.guildId));
            st.setString(1, action.name);
            st.execute();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addCourseInfo(CourseInfo courseInfo) {
        try {
            PreparedStatement st = conn.prepareStatement("INSERT INTO course_info (channel_id, guild_id, name)" +
                    String.format("Values (%d,%d,(?));", courseInfo.channelId, courseInfo.guildId));
            st.setString(1, courseInfo.name);
            st.execute();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addVoteAction(VoteAction action, String notesString){
        try {
            PreparedStatement st = conn.prepareStatement("INSERT INTO vote_actions (voter_id, author_id, guild_id, is_upvote, notes)" +
                    String.format("Values (%d,%d,%d,'%s',(?));", action.voterId, action.authorId, action.guildId, action.isUpvote));
            st.setString(1, notesString);
            st.execute();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeCourseAction(Member member, MessageChannel channel){
        try {
            Statement st = conn.createStatement();
            st.execute("DELETE FROM course_actions WHERE " +
                    String.format("member_id = %d AND channel_id = %d AND guild_id = %d ;", member.getIdLong(), channel.getIdLong(), member.getGuild().getIdLong()));
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeCourseInfo(CourseInfo courseInfo) {
        try {
            PreparedStatement st = conn.prepareStatement("DELETE FROM course_info WHERE " +
                    String.format("channel_id = %d AND guild_id = %d;",  courseInfo.channelId, courseInfo.guildId));
            st.execute();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<MemberInfo> getMembersWithCondition(String condition, String[] args){
        ArrayList<MemberInfo> out = new ArrayList<>();
        try {
            PreparedStatement  st = conn.prepareStatement("SELECT * FROM member_info " + condition);
            for(int i=0; args!=null && i<args.length; i++)
                st.setString(i + 1, args[i]);
            ResultSet rs = st.executeQuery();
            if (!rs.next() ) {
                return out;
            }else{
                do {
                    MemberInfo temp = new MemberInfo(rs.getLong("member_id"),
                            rs.getLong("guild_id"),
                            rs.getInt("upvotes"),
                            rs.getInt("downvotes"),
                            rs.getInt("balance")
                    );
                    out.add(temp);
                }while (rs.next());
            }
            st.close();
            return out;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    public static ArrayList<CourseAction> getCourseActionsWithCondition(String condition, String[] args){
        ArrayList<CourseAction> out = new ArrayList<>();
        try {
            PreparedStatement  st = conn.prepareStatement("SELECT * FROM course_actions " + condition);
            for(int i=0; args!=null && i<args.length; i++)
                st.setString(i + 1, args[i]);
            ResultSet rs = st.executeQuery();
            if (!rs.next() ) {
                return out;
            }else{
                do {
                    CourseAction temp = new CourseAction(rs.getLong("member_id"),
                            rs.getLong("channel_id"),
                            rs.getLong("guild_id"),
                            rs.getString("name")
                    );
                    out.add(temp);
                }while (rs.next());
            }
            st.close();
            return out;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    public static ArrayList<UserInfo> getUsersWithCondition(String condition, String[] args){
        ArrayList<UserInfo> out = new ArrayList<>();
        try {
            PreparedStatement  st = conn.prepareStatement("SELECT * FROM member_info " + condition);
            for(int i=0; args!=null && i<args.length; i++)
                st.setString(i + 1, args[i]);
            ResultSet rs = st.executeQuery();
            if (!rs.next() ) {
                return out;
            }else{
                do {
                    UserInfo temp = new UserInfo(rs.getLong("user_id"),
                            rs.getLong("gold_tokens")
                    );
                    out.add(temp);
                }while (rs.next());
            }
            st.close();
            return out;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }




    public static ArrayList<CourseInfo> getCourseInfosWithCondition(String condition, String[] args) {
        ArrayList<CourseInfo> out = new ArrayList<>();
        try {
            PreparedStatement  st = conn.prepareStatement("SELECT * FROM course_info " + condition + " ORDER BY name;");
            for(int i=0; args!=null && i<args.length; i++)
                st.setString(i + 1, args[i]);
            ResultSet rs = st.executeQuery();
            if (!rs.next() ) {
                return out;
            }else{
                do {
                    CourseInfo temp = new CourseInfo(rs.getLong("channel_id"),
                            rs.getLong("guild_id"),
                            rs.getString("name")
                    );
                    out.add(temp);
                }while (rs.next());
            }
            st.close();
            return out;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    public static ArrayList<VoteAction> getVoteActionsWithCondition(String condition, String[] args){
        ArrayList<VoteAction> out = new ArrayList<>();
        try {
            PreparedStatement  st = conn.prepareStatement("SELECT * FROM vote_actions " + condition);
            for(int i=0; args!=null && i<args.length; i++)
                st.setString(i + 1, args[i]);
            ResultSet rs = st.executeQuery();
            if (!rs.next() ) {
                return out;
            }else{
                do {
                    VoteAction temp = new VoteAction(rs.getLong("event_id"),
                            rs.getLong("voter_id"),
                            rs.getLong("author_id"),
                            rs.getLong("guild_id"),
                            rs.getTimestamp("datetime"),
                            rs.getBoolean("is_upvote")
                    );
                    out.add(temp);
                }while (rs.next());
            }
            st.close();
            return out;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    public static void removeCourseActionsWithCondition(String condition, String[] args) {
        try {
            PreparedStatement  st = conn.prepareStatement("DELETE FROM course_actions " + condition + ";");
            for(int i=0; args!=null && i<args.length; i++)
                st.setString(i + 1, args[i]);
            st.execute();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeCourseInfosWithCondition(String condition, String[] args) {
        try {
            PreparedStatement  st = conn.prepareStatement("DELETE FROM course_info " + condition + ";");
            for(int i=0; args!=null && i<args.length; i++)
                st.setString(i + 1, args[i]);
            st.execute();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateMember(Long memberId, String name, Object value){
        try {
            Statement st = conn.createStatement();
            st.execute(String.format("UPDATE member_info SET %s = '%s' WHERE member_id = %d;", name, value.toString(), memberId));
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateCourseInfo(Long channelId, String name, Object value){
        try {
            PreparedStatement st = conn.prepareStatement(String.format("UPDATE course_info SET %s = (?) WHERE channel_id = %d;", name, channelId));
            st.setString(1, value.toString());
            st.execute();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateMemberName(MemberName memberName){
        try {
            PreparedStatement st = conn.prepareStatement(
                    String.format("UPDATE member_names SET name = (?) WHERE member_id = %d;", memberName.memberId));
            st.setString(1, memberName.name);
            st.execute();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addMemberName(MemberName memberName){
        try {
            PreparedStatement st = conn.prepareStatement("INSERT INTO member_names (member_id, name)" +
                    String.format("Values (%d,(?));", memberName.memberId));
            st.setString(1, memberName.name);
            st.execute();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static HashMap<Long, String> getMemberNames(){
        HashMap<Long, String> out = new HashMap<>();
        try {
            PreparedStatement  st = conn.prepareStatement("SELECT * FROM member_names");
            ResultSet rs = st.executeQuery();
            if (!rs.next() ) {
                return out;
            }else{
                do {
                    out.put(rs.getLong("member_id"), rs.getString("name"));
                }while (rs.next());
            }
            st.close();
            return out;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }


    public static void backupDatabase(){
        //TODO better backup functionality
        if(urlBackup == null){
            LOGGER.warn("Backup path null, daily backup cancelled");
            return;
        }
        Instant now = Instant.now();
        String backupURL = urlBackup + now.truncatedTo(ChronoUnit.MINUTES).toString().replace(":","-") + ".db";
        FileInputStream in = null;
        FileOutputStream out = null;
        LOGGER.debug(backupURL);
        try {
            in = new FileInputStream(url);
            out = new FileOutputStream(backupURL);

            int c;
            while ((c = in.read()) != -1) {
                out.write(c);
            }
        } catch (IOException e) {
            LOGGER.error(e,e);
        } finally{
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.error(e,e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOGGER.error(e,e);
                }
            }
        }

    }
}

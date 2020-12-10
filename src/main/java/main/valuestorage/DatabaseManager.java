package main.valuestorage;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseManager {
    public static DatabaseManager INSTANCE = new DatabaseManager();
    private static Logger LOGGER = LogManager.getLogger("DatabaseManager");
    public static final String url = "jdbc:sqlite:C:/sqlite/db/BotData.db";
    public static Connection conn;

    private DatabaseManager(){
        Connection temp = null;
        try {
            temp = DriverManager.getConnection(url);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        conn = temp;
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addUser(UserInfo user){
        try {
            Statement st = conn.createStatement();
            st.execute("INSERT INTO user_info (user_id, gold_tokens)" +
                    String.format("Values (%d,%d)", user.userId, user.goldTokens));
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeCourseAction(Member member, MessageChannel channel){
        try {
            Statement st = conn.createStatement();
            st.execute("DELETE FROM course_actions WHERE " +
                    String.format("member_id = %d AND channel_id = %d AND guild_id = %d ;", member.getIdLong(), channel.getIdLong(), member.getGuild().getIdLong()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeCourseInfo(CourseInfo courseInfo) {
        try {
            PreparedStatement st = conn.prepareStatement("DELETE FROM course_info WHERE " +
                    String.format("channel_id = %d AND guild_id = %d AND name = (?) ;",  courseInfo.channelId, courseInfo.guildId));
            st.setString(1, courseInfo.name);
            st.execute();
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

            return out;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    public static void removeCourseActionsWithCondition(String condition, String[] args) {
        try {
            PreparedStatement  st = conn.prepareStatement("DELETE * FROM course_actions " + condition + ";");
            for(int i=0; args!=null && i<args.length; i++)
                st.setString(i + 1, args[i]);
            st.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeCourseInfosWithCondition(String condition, String[] args) {
        try {
            PreparedStatement  st = conn.prepareStatement("DELETE * FROM course_info " + condition + ";");
            for(int i=0; args!=null && i<args.length; i++)
                st.setString(i + 1, args[i]);
            st.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateMember(Long memberId, String name, Object value){
        try {
            Statement st = conn.createStatement();
            st.execute(String.format("UPDATE member_info SET %s = '%s' WHERE member_id = %d;", name, value.toString(), memberId));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateCourseInfo(Long channelId, String name, Object value){
        try {
            PreparedStatement st = conn.prepareStatement(String.format("UPDATE course_info SET %s = (?) WHERE channel_id = %d;", name, channelId));
            st.setString(1, value.toString());
            st.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}

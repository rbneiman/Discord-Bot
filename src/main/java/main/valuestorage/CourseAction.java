package main.valuestorage;

public class CourseAction {
    public final long channelId;
    public final long memberId;
    public final long guildId;
    public String name;

    public CourseAction(long memberId, long channelId, long guildId, String name) {
        this.channelId = channelId;
        this.memberId = memberId;
        this.guildId = guildId;
        this.name = name;
    }
}

package main.valuestorage;

public class CourseInfo {
    public final long channelId;
    public final long guildId;
    public String name;

    public CourseInfo(long channelId, long guildId, String name) {
        this.channelId = channelId;
        this.guildId = guildId;
        this.name = name;
    }
}

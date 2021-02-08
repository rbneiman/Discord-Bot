package main.valuestorage;

import java.sql.Timestamp;

public class VoteAction {
    long eventId;
    long voterId;
    long authorId;
    long guildId;
    Timestamp timestamp;
    boolean isUpvote;

    public VoteAction(long voterId, long authorId, long guildId, boolean isUpvote) {
        this.eventId = -1;
        this.voterId = voterId;
        this.authorId = authorId;
        this.guildId = guildId;
        this.isUpvote = isUpvote;
    }

    public VoteAction(long eventId, long voterId, long authorId, long guildId, Timestamp timestamp, boolean isUpvote) {
        this.eventId = eventId;
        this.voterId = voterId;
        this.authorId = authorId;
        this.guildId = guildId;
        this.timestamp = timestamp;
        this.isUpvote = isUpvote;
    }
}

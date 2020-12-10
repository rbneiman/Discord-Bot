package main.valuestorage;

public class UserInfo {
    public final long userId;
    public long goldTokens;

    public UserInfo(long userId, long goldTokens) {
        this.userId = userId;
        this.goldTokens = goldTokens;
    }
}

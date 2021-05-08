package main.valuestorage;

import net.dv8tion.jda.api.entities.Member;

public class MemberName {
    final long memberId;
    final String name;
    public MemberName(Member member){
        this.memberId = member.getIdLong();
        this.name = member.getEffectiveName();
    }
}

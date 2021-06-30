package main.commands;

public enum CommandType{
    BAD(-1),
    START(0),
    STOP(1),
    PLAY(2),
    SKIP(3),
    SERVER_IP(4),
    ROLL(5),
    ASK(6),
    FANCY_TEST(7),
    PARSE_TEST(8),
    ZORK(9),
    DEFAULT(10),
    COUNT(11),
    INTERRUPT(12),
    LEAVE(13),
    CLEAR(14),
    BALANCE(15),
    DEBUG_ADD(16),
    DEBUG_SET(17),
    BET(18),
    BLACKJACK(19),
    MONEY_PLS(20),
    RESERVE(21),
    COURSES(22),
    CLASS(23),
    CLASS_ADD(24),
    CLASS_DROP(25),
    CLASS_LIST(26),
    CLASS_SUDO_CREATE(27),
    CLASS_SUDO_REMOVE(28),
    KARMA(29),
    HIGHSCORES(30),
    LOWSCORES(31),
    VERYHIGHSCORES(32),
    MEANSCORES(33),
    JOB(34),
    HELP(35);


    public int value;
    CommandType(int value){
        this.value = value;
    }

    public static CommandType fromString(String commandString){
        CommandType out = BAD;
        switch (commandString.toLowerCase()){
            case "!start":
                out = START;
                break;
            case "!stop":
                out = STOP;
                break;
            case "!play":
                out = PLAY;
                break;
            case "!skip":
                out = SKIP;
                break;
            case "!serverip":
                out = SERVER_IP;
                break;
            case "!roll":
                out = ROLL;
                break;
            case "!ask":
                out = ASK;
                break;
            case "!fancytest":
                out = FANCY_TEST;
                break;
            case "!parsetest":
                out = PARSE_TEST;
                break;
            case "!zork":
                out = ZORK;
                break;
            case "!default":
                out = DEFAULT;
                break;
            case "!count":
                out = COUNT;
                break;
            case "!interrupt":
                out = INTERRUPT;
                break;
            case "!leave":
                out = LEAVE;
                break;
            case "!clear":
                out = CLEAR;
                break;
            case "!balance":
                out = BALANCE;
                break;
            case "!debugadd":
                out = DEBUG_ADD;
                break;
            case "!debugset":
                out = DEBUG_SET;
                break;
            case "!bet":
                out = BET;
                break;
            case "!blackjack":
                out = BLACKJACK;
                break;
            case "!moneypls":
                out = MONEY_PLS;
                break;
            case "!reserve":
                out = RESERVE;
                break;
            case "!courses":
                out = COURSES;
                break;
            case "!class":
                out = CLASS;
                break;
            case "!karma":
                out = KARMA;
                break;
            case "!highscores":
                out = HIGHSCORES;
                break;
            case "!lowscores":
                out = LOWSCORES;
                break;
            case "!veryhighscores":
                out = VERYHIGHSCORES;
                break;
            case "!meanscores":
                out = MEANSCORES;
                break;
            case "!job":
                out = JOB;
                break;
            case "!help":
                out = HELP;
                break;
        }
        return out;
    }
}
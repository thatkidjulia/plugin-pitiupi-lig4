package pitiupi.lig4.net;

import pitiupi.models.Messages.Message;

/** Convite, resposta e jogada de uma partida Lig4. */
public final class Lig4Message extends Message {
    public static final String INVITE = "INVITE";
    public static final String ACCEPT = "ACCEPT";
    public static final String REJECT = "REJECT";
    public static final String MOVE = "MOVE";
    public static final String REMATCH = "REMATCH";

    private String type;
    private String matchId;
    private String senderName;
    private String senderIp;
    private int column;
    private int moveNumber;

    public Lig4Message() { }

    public Lig4Message(String type, String matchId, String senderName, String senderIp, int column, int moveNumber) {
        this.type = type;
        this.matchId = matchId;
        this.senderName = senderName;
        this.senderIp = senderIp;
        this.column = column;
        this.moveNumber = moveNumber;
    }

    public String getType() { return type; }
    public String getMatchId() { return matchId; }
    public String getSenderName() { return senderName; }
    public String getSenderIp() { return senderIp; }
    public int getColumn() { return column; }
    public int getMoveNumber() { return moveNumber; }
}

package com.cuttherope.game;

public class VersusResult {
    public final String playerA;
    public final String playerB;
    public final int level;
    public final int starsA;
    public final int starsB;
    public final long timeA;
    public final long timeB;
    public final String winner;
    public final String reason;

    public VersusResult(String playerA, String playerB, int level, int starsA, int starsB, long timeA, long timeB, String winner, String reason) {
        this.playerA = playerA;
        this.playerB = playerB;
        this.level = level;
        this.starsA = starsA;
        this.starsB = starsB;
        this.timeA = timeA;
        this.timeB = timeB;
        this.winner = winner;
        this.reason = reason;
    }
}

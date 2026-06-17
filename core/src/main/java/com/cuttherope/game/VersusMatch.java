package com.cuttherope.game;

import java.io.Serializable;
import java.util.Date;

/** Partida VS binaria: solicitud, aceptación, listo y resultados nivel 1-5. */
public class VersusMatch implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum State { REQUESTED, ACCEPTED, BOTH_READY, IN_PROGRESS, FINISHED }

    public final String id;
    public final String requester;
    public final String opponent;
    public State state;
    public boolean requesterReady;
    public boolean opponentReady;
    public boolean statsUpdated;
    public int[] requesterStars = new int[5];
    public int[] opponentStars = new int[5];
    public long[] requesterTime = new long[5];
    public long[] opponentTime = new long[5];
    public Date createdAt = new Date();
    public Date updatedAt = new Date();
    public String winner = "Pendiente";
    public String reason = "";

    public VersusMatch(String id, String requester, String opponent) {
        this.id = id;
        this.requester = requester;
        this.opponent = opponent;
        this.state = State.REQUESTED;
    }

    public boolean includes(String username) {
        return requester.equalsIgnoreCase(username) || opponent.equalsIgnoreCase(username);
    }

    public String otherPlayer(String username) {
        return requester.equalsIgnoreCase(username) ? opponent : requester;
    }

    public boolean isReady(String username) {
        return requester.equalsIgnoreCase(username) ? requesterReady : opponentReady;
    }

    public boolean hasBothReady() { return requesterReady && opponentReady; }

    public boolean isExpiredRequest(long nowMs, long timeoutMs) {
        return state == State.REQUESTED && createdAt != null && (nowMs - createdAt.getTime()) > timeoutMs;
    }

    public boolean isCompleteFor(String username) {
        long[] t = requester.equalsIgnoreCase(username) ? requesterTime : opponentTime;
        for (int i = 0; i < 5; i++) if (t[i] <= 0) return false;
        return true;
    }

    public boolean isCompleteForBoth() { return isCompleteFor(requester) && isCompleteFor(opponent); }

    public void setReady(String username) {
        if (requester.equalsIgnoreCase(username)) requesterReady = true;
        if (opponent.equalsIgnoreCase(username)) opponentReady = true;
        if ((state == State.ACCEPTED || state == State.REQUESTED) && hasBothReady()) state = State.BOTH_READY;
        updatedAt = new Date();
    }

    public void recordLevel(String username, int level, int stars, long timeMs) {
        if (level < 0 || level >= 5) return;
        int safeStars = Math.max(0, Math.min(3, stars));
        long safeTime = Math.max(1L, timeMs);
        if (requester.equalsIgnoreCase(username)) {
            requesterStars[level] = safeStars;
            requesterTime[level] = safeTime;
        } else if (opponent.equalsIgnoreCase(username)) {
            opponentStars[level] = safeStars;
            opponentTime[level] = safeTime;
        }
        state = State.IN_PROGRESS;
        updatedAt = new Date();
        computeWinnerIfFinished();
    }

    public int totalStars(String username) {
        int[] a = requester.equalsIgnoreCase(username) ? requesterStars : opponentStars;
        int s = 0; for (int v : a) s += v; return s;
    }

    public long totalTime(String username) {
        long[] a = requester.equalsIgnoreCase(username) ? requesterTime : opponentTime;
        long s = 0; for (long v : a) s += v; return s;
    }

    public void computeWinnerIfFinished() {
        if (!isCompleteForBoth()) return;
        int sA = totalStars(requester), sB = totalStars(opponent);
        long tA = totalTime(requester), tB = totalTime(opponent);
        if (sA > sB) { winner = requester; reason = "Ganó por más estrellas"; }
        else if (sB > sA) { winner = opponent; reason = "Ganó por más estrellas"; }
        else if (tA < tB) { winner = requester; reason = "Empate en estrellas: ganó por menor tiempo"; }
        else if (tB < tA) { winner = opponent; reason = "Empate en estrellas: ganó por menor tiempo"; }
        else { winner = "Empate"; reason = "Mismas estrellas y mismo tiempo"; }
        state = State.FINISHED;
        updatedAt = new Date();
    }
}

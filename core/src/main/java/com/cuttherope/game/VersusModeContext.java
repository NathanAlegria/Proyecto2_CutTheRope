package com.cuttherope.game;


public final class VersusModeContext {
    private static String activeMatchId;

    private VersusModeContext() {}

    public static void start(String matchId) { activeMatchId = matchId; }
    public static boolean isActive() { return activeMatchId != null && !activeMatchId.isEmpty(); }
    public static String getMatchId() { return activeMatchId; }
    public static void clear() { activeMatchId = null; }
}

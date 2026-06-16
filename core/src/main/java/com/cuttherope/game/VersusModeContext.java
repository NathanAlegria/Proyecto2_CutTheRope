package com.cuttherope.game;

/** Contexto temporal para jugar los 5 niveles de una partida VS normal. */
public final class VersusModeContext {
    private static String activeMatchId;

    private VersusModeContext() {}

    public static void start(String matchId) { activeMatchId = matchId; }
    public static boolean isActive() { return activeMatchId != null && !activeMatchId.isEmpty(); }
    public static String getMatchId() { return activeMatchId; }
    public static void clear() { activeMatchId = null; }
}

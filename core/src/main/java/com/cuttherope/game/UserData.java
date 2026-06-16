/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cuttherope.game;

/**
 *
 * @author Nathan
 */
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * UserData - Modelo de datos del usuario.
 * Implementa Serializable para persistencia en archivos binarios.
 * Almacena toda la información requerida por el proyecto.
 */
public class UserData implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Datos de identidad ──────────────────────────────────────────────────
    private String username;          // Identificador único
    private String passwordHash;      // Contraseña (hasheada con SHA-256)
    private String fullName;          // Nombre completo
    private String avatarId;          // ID del avatar seleccionado ("avatar1"…"avatar5")

    // ── Timestamps ──────────────────────────────────────────────────────────
    private Date registrationDate;
    private Date lastLoginDate;

    // ── Estadísticas generales ───────────────────────────────────────────────
    private int    totalGamesPlayed;
    private long   totalTimePlayed;      // milisegundos
    private int    totalStarsCollected;
    private int    highestLevelReached;  // 1-5

    // ── Progreso por nivel (5 niveles) ───────────────────────────────────────
    private int[]    levelStars;         // estrellas obtenidas por nivel (0-3)
    private boolean[] levelUnlocked;     // si el nivel está desbloqueado
    private long[]   bestTimePerLevel;   // mejor tiempo en ms por nivel
    private int[]    attemptsPerLevel;   // intentos por nivel

    // ── Historial de partidas ────────────────────────────────────────────────
    private List<GameRecord> gameHistory;

    // ── Preferencias ────────────────────────────────────────────────────────
    private float   musicVolume;      // 0.0 - 1.0
    private float   sfxVolume;        // 0.0 - 1.0
    private String  language;         // "es" | "en"
    private boolean showTimer;
    private Boolean musicEnabled;     // Boolean para compatibilidad con usuarios antiguos
    private Boolean sfxEnabled;       // Boolean para compatibilidad con usuarios antiguos

    // ── Social ──────────────────────────────────────────────────────────────
    private List<String> friends;     // usernames de amigos
    private List<String> pendingFriendRequests; // solicitudes recibidas
    private List<String> sentFriendRequests;    // solicitudes enviadas
    private int    totalScore;        // puntuación global para ranking

    // ── Estadísticas VS ─────────────────────────────────────────────────
    private int versusPlayed;
    private int versusWins;
    private int versusLosses;
    private List<VersusHistoryRecord> versusHistory;

    // ────────────────────────────────────────────────────────────────────────
    //  Constructor
    // ────────────────────────────────────────────────────────────────────────
    public UserData(String username, String passwordHash, String fullName) {
        this.username         = username;
        this.passwordHash     = passwordHash;
        this.fullName         = fullName;
        this.avatarId         = "avatar1";
        this.registrationDate = new Date();
        this.lastLoginDate    = new Date();

        this.totalGamesPlayed    = 0;
        this.totalTimePlayed     = 0L;
        this.totalStarsCollected = 0;
        this.highestLevelReached = 1;

        this.levelStars      = new int[5];
        this.levelUnlocked   = new boolean[5];
        this.bestTimePerLevel = new long[5];
        this.attemptsPerLevel = new int[5];

        // Solo el nivel 1 empieza desbloqueado
        this.levelUnlocked[0] = true;
        for (int i = 1; i < 5; i++) this.levelUnlocked[i] = false;

        this.gameHistory  = new ArrayList<>();
        this.musicVolume  = 0.7f;
        this.sfxVolume    = 0.8f;
        this.language     = "es";
        this.showTimer    = true;
        this.musicEnabled = Boolean.TRUE;
        this.sfxEnabled   = Boolean.TRUE;
        this.friends      = new ArrayList<>();
        this.pendingFriendRequests = new ArrayList<>();
        this.sentFriendRequests = new ArrayList<>();
        this.totalScore   = 0;
        this.versusPlayed = 0;
        this.versusWins = 0;
        this.versusLosses = 0;
        this.versusHistory = new ArrayList<>();
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Métodos de negocio
    // ────────────────────────────────────────────────────────────────────────

    /** Registra el resultado de una partida y actualiza estadísticas. */
    public void recordGame(int level, int stars, long timeMs, boolean won) {
        totalGamesPlayed++;
        totalTimePlayed += timeMs;
        attemptsPerLevel[level]++;

        if (won) {
            // Actualizar estrellas si mejora
            if (stars > levelStars[level]) {
                int diff = stars - levelStars[level];
                levelStars[level] = stars;
                totalStarsCollected += diff;
            }
            // Mejor tiempo
            if (bestTimePerLevel[level] == 0 || timeMs < bestTimePerLevel[level]) {
                bestTimePerLevel[level] = timeMs;
            }
            // Desbloquear siguiente nivel
            if (level + 1 < 5) {
                levelUnlocked[level + 1] = true;
            }
            if (level + 1 > highestLevelReached) {
                highestLevelReached = level + 1;
            }
            // Puntuación: estrellas * 1000 - tiempo en segundos
            totalScore += stars * 1000 - (int)(timeMs / 1000);
            if (totalScore < 0) totalScore = 0;
        }

        gameHistory.add(new GameRecord(level, stars, timeMs, won, new Date()));
        // Mantener historial razonable
        if (gameHistory.size() > 100) gameHistory.remove(0);
    }

    /** Retorna tiempo total en formato legible. */
    public String getFormattedTotalTime() {
        long secs  = totalTimePlayed / 1000;
        long mins  = secs / 60;
        long hours = mins / 60;
        return String.format("%02d:%02d:%02d", hours, mins % 60, secs % 60);
    }


    /** Registra el resultado final de una partida VS de 5 niveles. */
    public void recordVersus(String opponent, String winner, int myStars, long myTimeMs, String reason) {
        recordVersus(opponent, winner, myStars, myTimeMs, reason, myStars, myTimeMs, 15);
    }

    /** Registra el VS con datos completos del ganador para mostrarlo en Estadísticas. */
    public void recordVersus(String opponent, String winner, int myStars, long myTimeMs, String reason,
                             int winnerStars, long winnerTimeMs, int totalPossibleStars) {
        ensureVersusHistory();
        versusPlayed++;
        if (username != null && username.equalsIgnoreCase(winner)) versusWins++;
        else if (!"Empate".equalsIgnoreCase(winner)) versusLosses++;
        versusHistory.add(new VersusHistoryRecord(opponent, winner, myStars, myTimeMs, reason, new Date(),
                winnerStars, winnerTimeMs, totalPossibleStars));
        if (versusHistory.size() > 50) versusHistory.remove(0);
    }

    public void ensureVersusHistory() { if (versusHistory == null) versusHistory = new ArrayList<>(); }

    // ────────────────────────────────────────────────────────────────────────
    //  Getters / Setters
    // ────────────────────────────────────────────────────────────────────────
    public String  getUsername()           { return username; }
    public String  getPasswordHash()       { return passwordHash; }
    public void    setPasswordHash(String h){ this.passwordHash = h; }
    public String  getFullName()           { return fullName; }
    public void    setFullName(String n)   { this.fullName = n; }
    public String  getAvatarId()           { return avatarId; }
    public void    setAvatarId(String id)  { this.avatarId = id; }
    public Date    getRegistrationDate()   { return registrationDate; }
    public Date    getLastLoginDate()      { return lastLoginDate; }
    public void    setLastLoginDate(Date d){ this.lastLoginDate = d; }
    public int     getTotalGamesPlayed()   { return totalGamesPlayed; }
    public long    getTotalTimePlayed()    { return totalTimePlayed; }
    public int     getTotalStarsCollected(){ return totalStarsCollected; }
    public int     getHighestLevelReached(){ return highestLevelReached; }
    public int[]   getLevelStars()         { return levelStars; }
    public boolean[] getLevelUnlocked()    { return levelUnlocked; }
    public long[]  getBestTimePerLevel()   { return bestTimePerLevel; }
    public int[]   getAttemptsPerLevel()   { return attemptsPerLevel; }
    public List<GameRecord> getGameHistory(){ return gameHistory; }
    public float   getMusicVolume()        { return musicVolume; }
    public void    setMusicVolume(float v) { this.musicVolume = v; }
    public float   getSfxVolume()          { return sfxVolume; }
    public void    setSfxVolume(float v)   { this.sfxVolume = v; }
    public String  getLanguage()           { return language; }
    public void    setLanguage(String l)   { this.language = l; }
    public boolean isShowTimer()           { return showTimer; }
    public void    setShowTimer(boolean s) { this.showTimer = s; }
    public boolean isMusicEnabled()        { return musicEnabled == null ? true : musicEnabled.booleanValue(); }
    public void    setMusicEnabled(boolean b){ this.musicEnabled = b; }
    public boolean isSfxEnabled()          { return sfxEnabled == null ? true : sfxEnabled.booleanValue(); }
    public void    setSfxEnabled(boolean b){ this.sfxEnabled = b; }
    public List<String> getFriends()       { ensureSocialLists(); return friends; }
    public List<String> getPendingFriendRequests() { ensureSocialLists(); return pendingFriendRequests; }
    public List<String> getSentFriendRequests() { ensureSocialLists(); return sentFriendRequests; }
    public void ensureSocialLists() {
        if (friends == null) friends = new ArrayList<>();
        if (pendingFriendRequests == null) pendingFriendRequests = new ArrayList<>();
        if (sentFriendRequests == null) sentFriendRequests = new ArrayList<>();
    }
    public void    addFriend(String u)     { ensureSocialLists(); if (u != null && !friends.contains(u)) friends.add(u); }
    public int     getStarsForLevel(int level) { return levelStars == null || level < 0 || level >= levelStars.length ? 0 : levelStars[level]; }
    public long    getBestTimeForLevel(int level) { return bestTimePerLevel == null || level < 0 || level >= bestTimePerLevel.length ? 0L : bestTimePerLevel[level]; }
    public int     getTotalScore()         { return totalScore; }
    public int     getVersusPlayed()       { return versusPlayed; }
    public int     getVersusWins()         { return versusWins; }
    public int     getVersusLosses()       { return versusLosses; }
    public List<VersusHistoryRecord> getVersusHistory() { ensureVersusHistory(); return versusHistory; }

    // ────────────────────────────────────────────────────────────────────────
    //  Clase interna: registro individual de partida
    // ────────────────────────────────────────────────────────────────────────
    public static class GameRecord implements Serializable {
        private static final long serialVersionUID = 1L;
        public final int  level;
        public final int  stars;
        public final long timeMs;
        public final boolean won;
        public final Date playedAt;

        public GameRecord(int level, int stars, long timeMs, boolean won, Date playedAt) {
            this.level    = level;
            this.stars    = stars;
            this.timeMs   = timeMs;
            this.won      = won;
            this.playedAt = playedAt;
        }

        @Override
        public String toString() {
            return String.format("Nivel %d | %s | %d★ | %.1fs | %tF",
                level + 1, won ? "Victoria" : "Derrota", stars, timeMs / 1000.0, playedAt);
        }
    }

    public static class VersusHistoryRecord implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String opponent;
        public final String winner;
        public final int stars;
        public final long timeMs;
        public final String reason;
        public final Date playedAt;
        public final int winnerStars;
        public final long winnerTimeMs;
        public final int totalPossibleStars;

        public VersusHistoryRecord(String opponent, String winner, int stars, long timeMs, String reason, Date playedAt) {
            this(opponent, winner, stars, timeMs, reason, playedAt, stars, timeMs, 15);
        }

        public VersusHistoryRecord(String opponent, String winner, int stars, long timeMs, String reason, Date playedAt,
                                   int winnerStars, long winnerTimeMs, int totalPossibleStars) {
            this.opponent = opponent;
            this.winner = winner;
            this.stars = stars;
            this.timeMs = timeMs;
            this.reason = reason;
            this.playedAt = playedAt;
            this.winnerStars = winnerStars;
            this.winnerTimeMs = winnerTimeMs;
            this.totalPossibleStars = totalPossibleStars;
        }
    }

}
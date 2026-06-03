/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cuttherope.game;

/**
 *
 * @author Nathan
 */

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Juego - Clase base abstracta requerida por el enunciado del proyecto.
 * Define el contrato que todo juego concreto debe implementar.
 * Patrón: Template Method.
 */
public abstract class Juego {

    protected int    currentLevel;
    protected int    lives;
    protected int    score;
    protected boolean paused;
    protected boolean gameOver;
    protected boolean levelComplete;
    protected long   startTimeMs;
    protected long   elapsedMs;

    public Juego() {
        this.currentLevel  = 0;
        this.lives         = 3;
        this.score         = 0;
        this.paused        = false;
        this.gameOver      = false;
        this.levelComplete = false;
        this.startTimeMs   = System.currentTimeMillis();
    }

    // ── Métodos abstractos (contrato) ────────────────────────────────────────

    /** Inicializa el estado del nivel actual. */
    public abstract void initLevel(int level);

    /** Actualiza la lógica del juego. delta = tiempo desde último frame en seg. */
    public abstract void update(float delta);

    /** Dibuja todos los elementos del juego. */
    public abstract void render(SpriteBatch batch);

    /** Libera recursos del nivel. */
    public abstract void disposeLevel();

    /** Verifica si se cumple la condición de victoria del nivel. */
    public abstract boolean checkWinCondition();

    /** Verifica si se perdió el nivel. */
    public abstract boolean checkLoseCondition();

    // ── Template methods ─────────────────────────────────────────────────────

    /** Pausa/reanuda el juego. */
    public void togglePause() { paused = !paused; }

    /** Reinicia el nivel actual. */
    public void restartLevel() {
        lives--;
        if (lives <= 0) {
            gameOver = true;
        } else {
            startTimeMs   = System.currentTimeMillis();
            levelComplete = false;
            initLevel(currentLevel);
        }
    }

    /** Avanza al siguiente nivel. */
    public void nextLevel() {
        currentLevel++;
        levelComplete = false;
        startTimeMs   = System.currentTimeMillis();
        initLevel(currentLevel);
    }

    /** Calcula el tiempo transcurrido en el nivel actual (ms). */
    public long getElapsedTime() {
        if (paused) return elapsedMs;
        return elapsedMs + (System.currentTimeMillis() - startTimeMs);
    }

    /** Formatea el tiempo como mm:ss. */
    public String getFormattedTime() {
        long secs = getElapsedTime() / 1000;
        return String.format("%02d:%02d", secs / 60, secs % 60);
    }

    // ── Getters / Setters ────────────────────────────────────────────────────
    public int     getCurrentLevel()       { return currentLevel; }
    public int     getLives()              { return lives; }
    public void    setLives(int l)         { this.lives = l; }
    public int     getScore()              { return score; }
    public void    addScore(int s)         { this.score += s; }
    public boolean isPaused()              { return paused; }
    public boolean isGameOver()            { return gameOver; }
    public boolean isLevelComplete()       { return levelComplete; }
    public void    setLevelComplete(boolean b){ this.levelComplete = b; }
}


package com.cuttherope.game;


import com.badlogic.gdx.graphics.g2d.SpriteBatch;


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


    public abstract void initLevel(int level);


    public abstract void update(float delta);


    public abstract void render(SpriteBatch batch);


    public abstract void disposeLevel();


    public abstract boolean checkWinCondition();


    public abstract boolean checkLoseCondition();


    public void togglePause() { paused = !paused; }


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


    public void nextLevel() {
        currentLevel++;
        levelComplete = false;
        startTimeMs   = System.currentTimeMillis();
        initLevel(currentLevel);
    }


    public long getElapsedTime() {
        if (paused) return elapsedMs;
        return elapsedMs + (System.currentTimeMillis() - startTimeMs);
    }


    public String getFormattedTime() {
        long secs = getElapsedTime() / 1000;
        return String.format("%02d:%02d", secs / 60, secs % 60);
    }


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



package com.cuttherope.game;


import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class GameLogicThread extends Thread {

    public interface GameTimerCallback {
        void onTimeUpdate(int remainingSeconds);
        void onTimeWarning();
        void onTimeOut();
    }

    private final AtomicBoolean running    = new AtomicBoolean(false);
    private final AtomicBoolean paused     = new AtomicBoolean(false);
    private final AtomicInteger timeLeft   = new AtomicInteger(0);

    private final int tickMs = 1000;
    private GameTimerCallback callback;
    private boolean warningFired = false;

    public GameLogicThread(String name) {
        super(name);
        setDaemon(true);
    }

    public void setCallback(GameTimerCallback cb) { this.callback = cb; }


    public void startTimer(int timeSeconds) {
        timeLeft.set(timeSeconds);
        warningFired = false;
        running.set(true);
        paused.set(false);
        if (!isAlive()) start();
    }

    public void pauseTimer()  { paused.set(true);  }
    public void resumeTimer() { paused.set(false); }
    public void stopTimer()   { running.set(false); }

    @Override
    public void run() {
        while (running.get()) {
            try {
                Thread.sleep(tickMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            if (!paused.get() && running.get()) {
                int current = timeLeft.get();

                if (current > 0) {
                    int newTime = current - 1;
                    timeLeft.set(newTime);

                    if (callback != null) {
                        callback.onTimeUpdate(newTime);

                        if (newTime <= 10 && !warningFired) {
                            warningFired = true;
                            callback.onTimeWarning();
                        }

                        if (newTime == 0) {
                            callback.onTimeOut();
                            running.set(false);
                        }
                    }
                }
            }
        }
    }

    public int getTimeLeft()    { return timeLeft.get(); }
    public boolean isRunning()  { return running.get(); }
    public boolean isPaused()   { return paused.get(); }
}

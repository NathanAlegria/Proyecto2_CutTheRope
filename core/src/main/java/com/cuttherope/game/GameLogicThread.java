/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cuttherope.game;

/**
 *
 * @author Nathan
 */

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GameLogicThread - Hilo secundario que maneja lógica de juego en tiempo real.
 * Cumple el requisito de uso de hilos del proyecto.
 *
 * Responsabilidades:
 *  - Contador regresivo del nivel
 *  - Notificaciones de tiempo (alerta cuando queda poco)
 *  - Registro asíncrono de estadísticas
 */
public class GameLogicThread extends Thread {

    public interface GameTimerCallback {
        void onTimeUpdate(int remainingSeconds);
        void onTimeWarning();   // cuando quedan ≤10 s
        void onTimeOut();       // cuando llega a 0
    }

    private final AtomicBoolean running    = new AtomicBoolean(false);
    private final AtomicBoolean paused     = new AtomicBoolean(false);
    private final AtomicInteger timeLeft   = new AtomicInteger(0);

    private final int tickMs = 1000;  // actualización cada 1 segundo
    private GameTimerCallback callback;
    private boolean warningFired = false;

    public GameLogicThread(String name) {
        super(name);
        setDaemon(true);  // muere cuando cierra la JVM
    }

    public void setCallback(GameTimerCallback cb) { this.callback = cb; }

    /**
     * Inicia la cuenta regresiva desde timeSeconds.
     * Si timeSeconds <= 0, el hilo corre sin límite de tiempo.
     */
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

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cuttherope.game;

/**
 *
 * @author Nathan
 */

import com.cuttherope.game.UserData;
import com.cuttherope.game.UserManager;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * StatsSaveThread - Hilo de guardado asíncrono de estadísticas.
 * Evita bloquear el hilo de render al escribir archivos binarios.
 * Segundo hilo del proyecto.
 */
public class StatsSaveThread extends Thread {

    private static StatsSaveThread instance;

    // Cola de peticiones de guardado
    private final BlockingQueue<SaveRequest> queue = new LinkedBlockingQueue<>();
    private volatile boolean running = true;

    private StatsSaveThread() {
        super("StatsSaveThread");
        setDaemon(true);
    }

    public static StatsSaveThread getInstance() {
        if (instance == null || !instance.isAlive()) {
            instance = new StatsSaveThread();
            instance.start();
        }
        return instance;
    }

    @Override
    public void run() {
        System.out.println("[StatsSaveThread] Iniciado.");
        while (running) {
            try {
                SaveRequest req = queue.take();  // bloquea hasta que hay trabajo
                process(req);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("[StatsSaveThread] Detenido.");
    }

    private void process(SaveRequest req) {
        UserManager.getInstance().saveUser(req.userData);
        System.out.println("[StatsSaveThread] Guardado: " + req.userData.getUsername()
            + " | nivel=" + (req.level + 1) + " | estrellas=" + req.stars);
    }

    /**
     * Encola el guardado de estadísticas (no bloquea el hilo de render).
     */
    public void enqueueRecord(UserData ud, int level, int stars, long timeMs, boolean won) {
        // Primero actualiza el modelo de datos en el hilo de render
        ud.recordGame(level, stars, timeMs, won);
        // Luego encola el guardado
        queue.offer(new SaveRequest(ud, level, stars));
    }

    public void shutdown() {
        running = false;
        interrupt();
    }

    // ── Clase de petición ─────────────────────────────────────────────────────
    private static class SaveRequest {
        final UserData userData;
        final int      level;
        final int      stars;

        SaveRequest(UserData ud, int level, int stars) {
            this.userData = ud;
            this.level    = level;
            this.stars    = stars;
        }
    }
}
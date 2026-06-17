

package com.cuttherope.game;


import com.cuttherope.game.UserData;
import com.cuttherope.game.UserManager;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class StatsSaveThread extends Thread {

    private static StatsSaveThread instance;


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
                SaveRequest req = queue.take();
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


    public void enqueueRecord(UserData ud, int level, int stars, long timeMs, boolean won) {

        ud.recordGame(level, stars, timeMs, won);

        queue.offer(new SaveRequest(ud, level, stars));
    }

    public void shutdown() {
        running = false;
        interrupt();
    }


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

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cuttherope.game;

/**
 *
 * @author Nathan
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import java.text.SimpleDateFormat;
import java.util.List;

public class StatsScreen implements Screen {

    private final MainGame game;
    private final UserData ud;
    private ShapeRenderer sr;
    private int scrollOffset = 0;

    private final Rectangle btnBack       = new Rectangle(18, 18, 140, 40);
    private final Rectangle btnScrollUp   = new Rectangle(740, 310, 38, 38);
    private final Rectangle btnScrollDown = new Rectangle(740, 260, 38, 38);

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yy HH:mm");

    public StatsScreen(MainGame game) {
        this.game = game;
        this.ud   = UserManager.getInstance().getCurrentUser();
    }

    @Override
    public void show() {
        sr = new ShapeRenderer();

        if (game.audioManager != null) {
            game.audioManager.playMenuMusic();
        }
    }

    @Override
    public void render(float delta) {
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);
        sr.setProjectionMatrix(game.camera.combined);

        Gdx.gl.glClearColor(0.07f, 0.05f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        drawBg();
        drawHeader();
        drawLevelStats();
        drawHistory();
        drawButtons();
        handleInput();
    }

    private void drawBg() {
        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(0.07f, 0.05f, 0.12f, 1f));
        sr.rect(0, 0, 800, 700);

        sr.setColor(new Color(0.10f, 0.08f, 0.18f, 1f));
        sr.rect(10, 10, 780, 680);

        // Decoración superior
        sr.setColor(new Color(0.5f, 0.25f, 0.9f, 0.12f));
        sr.circle(705, 615, 85);

        sr.setColor(new Color(0.2f, 0.75f, 0.2f, 0.10f));
        sr.circle(85, 105, 70);

        sr.setColor(new Color(1f, 0.85f, 0.2f, 0.08f));
        sr.circle(720, 110, 58);

        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);

        sr.setColor(new Color(0.5f, 0.3f, 0.8f, 1f));
        sr.rect(10, 10, 780, 680);

        sr.setColor(new Color(1f, 1f, 1f, 0.12f));
        sr.line(25, 588, 775, 588);
        sr.line(25, 398, 775, 398);

        sr.end();
    }

    private void drawHeader() {
        game.batch.begin();

        game.fontLarge.setColor(new Color(0.9f, 0.7f, 1f, 1f));
        game.fontLarge.draw(game.batch, MainGame.t("Estadísticas"), 245, 668);

        if (ud == null) {
            game.font.setColor(Color.WHITE);
            game.font.draw(game.batch, MainGame.t("No hay usuario activo."), 250, 400);
            game.batch.end();
            return;
        }

        game.font.setColor(Color.WHITE);
        game.font.draw(
            game.batch,
            MainGame.t("Jugador:") + " " + ud.getFullName() + " (@" + ud.getUsername() + ")",
            30,
            638
        );

        game.fontSmall.setColor(new Color(0.72f, 0.72f, 0.72f, 1f));
        game.fontSmall.draw(
            game.batch,
            MainGame.t("Registro:") + " " + SDF.format(ud.getRegistrationDate())
                + "   " + MainGame.t("Última sesión:") + " " + SDF.format(ud.getLastLoginDate()),
            30,
            620
        );

        game.font.setColor(new Color(0.8f, 0.9f, 1f, 1f));
        game.font.draw(
            game.batch,
            MainGame.t("Partidas:") + " " + ud.getTotalGamesPlayed()
                + "  " + MainGame.t("Tiempo total:") + " " + ud.getFormattedTotalTime()
                + "  " + MainGame.t("Estrellas:") + " " + ud.getTotalStarsCollected()
                + "  " + MainGame.t("Pts:") + " " + ud.getTotalScore(),
            30,
            600
        );

        game.batch.end();
    }

    private void drawLevelStats() {
        if (ud == null) {
            return;
        }

        int[] stars       = ud.getLevelStars();
        long[] best       = ud.getBestTimePerLevel();
        int[] attempts    = ud.getAttemptsPerLevel();
        boolean[] unlocked = ud.getLevelUnlocked();

        game.batch.begin();

        game.font.setColor(new Color(0.9f, 0.75f, 1f, 1f));
        game.font.draw(game.batch, MainGame.t("Progreso por nivel:"), 30, 576);

        game.batch.end();

        String[] names = {
            "Tutorial",
            "Doble",
            "Triple",
            "Laberinto",
            "Final"
        };

        for (int i = 0; i < 5; i++) {
            float rowY = 556 - i * 32;

            sr.begin(ShapeRenderer.ShapeType.Filled);

            sr.setColor(i % 2 == 0
                ? new Color(0.14f, 0.11f, 0.24f, 1f)
                : new Color(0.11f, 0.09f, 0.20f, 1f)
            );

            sr.rect(28, rowY - 22, 720, 26);

            if (unlocked[i]) {
                sr.setColor(new Color(0.2f, 0.75f, 0.2f, 0.12f));
                sr.rect(28, rowY - 22, 6, 26);
            } else {
                sr.setColor(new Color(1f, 0.2f, 0.2f, 0.12f));
                sr.rect(28, rowY - 22, 6, 26);
            }

            sr.end();

            sr.begin(ShapeRenderer.ShapeType.Line);

            sr.setColor(new Color(0.32f, 0.25f, 0.50f, 1f));
            sr.rect(28, rowY - 22, 720, 26);

            sr.end();

            game.batch.begin();

            game.fontSmall.setColor(Color.WHITE);
            game.fontSmall.draw(
                game.batch,
                MainGame.t("Nivel") + " " + (i + 1) + " - " + names[i],
                40,
                rowY
            );

            game.fontSmall.setColor(unlocked[i]
                ? new Color(0.45f, 1f, 0.45f, 1f)
                : new Color(0.9f, 0.4f, 0.4f, 1f)
            );

            game.fontSmall.draw(
                game.batch,
                unlocked[i] ? "OK" : MainGame.t("Bloqueado"),
                185,
                rowY
            );

            game.fontSmall.setColor(new Color(1f, 0.85f, 0.2f, 1f));
            game.fontSmall.draw(game.batch, starsToText(stars[i]), 295, rowY);

            game.fontSmall.setColor(new Color(0.8f, 0.9f, 1f, 1f));
            game.fontSmall.draw(
                game.batch,
                MainGame.t("Mejor:") + " " + formatTime(best[i]),
                405,
                rowY
            );

            game.fontSmall.setColor(new Color(0.8f, 0.8f, 0.9f, 1f));
            game.fontSmall.draw(
                game.batch,
                MainGame.t("Int:") + " " + attempts[i],
                610,
                rowY
            );

            game.batch.end();
        }
    }

    private void drawHistory() {
        if (ud == null) {
            return;
        }

        List<UserData.GameRecord> hist = ud.getGameHistory();

        game.batch.begin();

        game.font.setColor(new Color(0.9f, 0.75f, 1f, 1f));
        game.font.draw(game.batch, MainGame.t("Historial de partidas:"), 30, 380);

        game.batch.end();

        if (hist == null || hist.isEmpty()) {
            game.batch.begin();

            game.fontSmall.setColor(new Color(0.75f, 0.75f, 0.82f, 1f));
            game.fontSmall.draw(game.batch, MainGame.t("Aún no has jugado ninguna partida."), 40, 350);

            game.batch.end();
            return;
        }

        int visible = 7;
        int total = hist.size();

        if (scrollOffset < 0) {
            scrollOffset = 0;
        }

        if (scrollOffset > Math.max(0, total - visible)) {
            scrollOffset = Math.max(0, total - visible);
        }

        int start = Math.max(0, total - visible - scrollOffset);
        int end = Math.min(total, start + visible);

        for (int idx = start; idx < end; idx++) {
            UserData.GameRecord gr = hist.get(idx);

            int row = idx - start;
            float y = 352 - row * 38;

            sr.begin(ShapeRenderer.ShapeType.Filled);

            sr.setColor(row % 2 == 0
                ? new Color(0.14f, 0.11f, 0.24f, 1f)
                : new Color(0.11f, 0.09f, 0.20f, 1f)
            );

            sr.rect(34, y - 23, 690, 30);

            sr.setColor(gr.won
                ? new Color(0.2f, 0.75f, 0.2f, 0.12f)
                : new Color(1f, 0.2f, 0.2f, 0.12f)
            );

            sr.rect(34, y - 23, 6, 30);

            sr.end();

            sr.begin(ShapeRenderer.ShapeType.Line);

            sr.setColor(new Color(0.32f, 0.25f, 0.50f, 1f));
            sr.rect(34, y - 23, 690, 30);

            sr.end();

            game.batch.begin();

            game.fontSmall.setColor(Color.WHITE);
            game.fontSmall.draw(
                game.batch,
                MainGame.t("Nivel") + " " + (gr.level + 1),
                48,
                y
            );

            game.fontSmall.setColor(gr.won
                ? new Color(0.45f, 1f, 0.45f, 1f)
                : new Color(1f, 0.45f, 0.45f, 1f)
            );

            game.fontSmall.draw(
                game.batch,
                MainGame.t(gr.won ? "Victoria" : "Derrota"),
                128,
                y
            );

            game.fontSmall.setColor(new Color(1f, 0.85f, 0.2f, 1f));
            game.fontSmall.draw(game.batch, starsToText(gr.stars), 235, y);

            game.fontSmall.setColor(new Color(0.8f, 0.9f, 1f, 1f));
            game.fontSmall.draw(game.batch, formatTime(gr.timeMs), 335, y);

            game.fontSmall.setColor(new Color(0.72f, 0.72f, 0.78f, 1f));
            game.fontSmall.draw(game.batch, SDF.format(gr.playedAt), 455, y);

            game.batch.end();
        }

        drawScrollButtons(total, visible);
    }

    private void drawScrollButtons(int total, int visible) {
        if (total <= visible) {
            return;
        }

        drawSmallBtn(btnScrollUp, "▲");
        drawSmallBtn(btnScrollDown, "▼");
    }

    private void drawButtons() {
        drawBtn(btnBack, MainGame.t("← Menú"), new Color(0.4f, 0.3f, 0.6f, 1f));
    }

    private void drawBtn(Rectangle r, String text, Color col) {
        boolean hov = isHovered(r);

        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(0f, 0f, 0f, 0.25f));
        sr.rect(r.x + 3, r.y - 3, r.width, r.height);

        sr.setColor(hov ? col.cpy().add(0.12f, 0.12f, 0.12f, 0) : col);
        sr.rect(r.x, r.y, r.width, r.height);

        sr.setColor(new Color(1f, 1f, 1f, 0.10f));
        sr.rect(r.x, r.y + r.height - 5, r.width, 5);

        sr.end();

        game.batch.begin();

        game.fontSmall.setColor(Color.WHITE);
        game.fontSmall.draw(game.batch, text, r.x + 8, r.y + r.height - 8);

        game.batch.end();
    }

    private void drawSmallBtn(Rectangle r, String text) {
        boolean hov = isHovered(r);

        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(hov
            ? new Color(0.55f, 0.40f, 0.78f, 1f)
            : new Color(0.34f, 0.25f, 0.54f, 1f)
        );

        sr.rect(r.x, r.y, r.width, r.height);

        sr.end();

        game.batch.begin();

        game.fontSmall.setColor(Color.WHITE);
        game.fontSmall.draw(game.batch, text, r.x + 11, r.y + 26);

        game.batch.end();
    }

    private void handleInput() {
        if (!Gdx.input.justTouched()) {
            return;
        }

        com.badlogic.gdx.math.Vector3 _touch =
            new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);

        game.viewport.unproject(_touch);

        float mx = _touch.x;
        float my = _touch.y;

        if (btnBack.contains(mx, my)) {
            playClick();
            game.setScreen(new MainMenuScreen(game));
            dispose();
            return;
        }

        if (ud == null) {
            return;
        }

        List<UserData.GameRecord> hist = ud.getGameHistory();

        if (hist == null || hist.size() <= 7) {
            return;
        }

        if (btnScrollUp.contains(mx, my)) {
            playClick();
            scrollOffset = Math.min(scrollOffset + 1, Math.max(0, hist.size() - 7));
        }

        if (btnScrollDown.contains(mx, my)) {
            playClick();
            scrollOffset = Math.max(scrollOffset - 1, 0);
        }
    }

    private String starsToText(int n) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 3; i++) {
            sb.append(i < n ? "★" : "☆");
        }

        return sb.toString();
    }

    private String formatTime(long ms) {
        if (ms <= 0) {
            return "--:--";
        }

        long sec = ms / 1000;
        return String.format("%02d:%02d", sec / 60, sec % 60);
    }

    private boolean isHovered(Rectangle r) {
        com.badlogic.gdx.math.Vector3 _touch =
            new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);

        game.viewport.unproject(_touch);

        return r.contains(_touch.x, _touch.y);
    }

    private void playClick() {
        if (game.audioManager != null) {
            game.audioManager.playClick();
        }
    }

    @Override
    public void resize(int w, int h) {
        game.viewport.update(w, h, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (sr != null) {
            sr.dispose();
        }
    }
}
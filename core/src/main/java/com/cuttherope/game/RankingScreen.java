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

import java.util.List;

public class RankingScreen implements Screen {

    private final MainGame game;
    private ShapeRenderer sr;
    private List<UserData> ranking;

    private final Rectangle btnBack = new Rectangle(18, 18, 140, 40);

    private static final Color[] MEDAL = {
        new Color(1f, 0.84f, 0f, 1f),
        new Color(0.75f, 0.75f, 0.75f, 1f),
        new Color(0.8f, 0.5f, 0.2f, 1f),
    };

    public RankingScreen(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        sr = new ShapeRenderer();

        if (game.audioManager != null) {
            game.audioManager.playMenuMusic();
        }

        Thread t = new Thread(() -> ranking = UserManager.getInstance().getRanking(), "RankingLoad");
        t.setDaemon(true);
        t.start();

        try {
            t.join(2000);
        } catch (InterruptedException ignored) {}

        if (ranking == null) {
            ranking = UserManager.getInstance().getRanking();
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
        drawTitle();
        drawTable();
        drawBtn(btnBack, MainGame.t("← Menú"), new Color(0.4f, 0.3f, 0.6f, 1f));

        handleInput();
    }

    private void drawBg() {
        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(0.07f, 0.05f, 0.12f, 1f));
        sr.rect(0, 0, 800, 700);

        sr.setColor(new Color(0.10f, 0.08f, 0.18f, 1f));
        sr.rect(10, 10, 780, 680);

        // Decoraciones suaves
        sr.setColor(new Color(1f, 0.84f, 0f, 0.10f));
        sr.circle(705, 610, 90);

        sr.setColor(new Color(0.5f, 0.25f, 0.9f, 0.12f));
        sr.circle(85, 105, 70);

        sr.setColor(new Color(0.2f, 0.75f, 0.2f, 0.10f));
        sr.circle(720, 100, 58);

        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);

        sr.setColor(new Color(0.5f, 0.3f, 0.8f, 1f));
        sr.rect(10, 10, 780, 680);

        sr.setColor(new Color(1f, 1f, 1f, 0.12f));
        sr.line(25, 602, 775, 602);

        sr.end();
    }

    private void drawTitle() {
        game.batch.begin();

        game.fontLarge.setColor(new Color(1f, 0.85f, 0.2f, 1f));
        game.fontLarge.draw(game.batch, MainGame.t("Ranking Global"), 228, 665);

        game.fontSmall.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        game.fontSmall.draw(game.batch, "#", 40, 632);
        game.fontSmall.draw(game.batch, MainGame.t("Usuario"), 72, 632);
        game.fontSmall.draw(game.batch, MainGame.t("Nombre"), 230, 632);
        game.fontSmall.draw(game.batch, MainGame.t("Pts:"), 420, 632);
        game.fontSmall.draw(game.batch, MainGame.t("Estrellas:"), 510, 632);
        game.fontSmall.draw(game.batch, MainGame.t("Partidas:"), 635, 632);

        game.batch.end();
    }

    private void drawTable() {
        if (ranking == null || ranking.isEmpty()) {
            game.batch.begin();

            game.font.setColor(Color.WHITE);
            game.font.draw(game.batch, MainGame.t("No hay jugadores registrados todavía."), 230, 390);

            game.batch.end();
            return;
        }

        int max = Math.min(10, ranking.size());

        for (int i = 0; i < max; i++) {
            UserData u = ranking.get(i);

            float y = 595 - i * 48;
            boolean even = i % 2 == 0;

            sr.begin(ShapeRenderer.ShapeType.Filled);

            sr.setColor(even ? new Color(0.14f, 0.11f, 0.24f, 1f)
                             : new Color(0.11f, 0.09f, 0.20f, 1f));
            sr.rect(25, y - 30, 750, 38);

            if (i < 3) {
                sr.setColor(MEDAL[i]);
                sr.circle(44, y - 11, 14);
            }

            sr.end();

            sr.begin(ShapeRenderer.ShapeType.Line);

            sr.setColor(new Color(0.35f, 0.26f, 0.52f, 1f));
            sr.rect(25, y - 30, 750, 38);

            sr.end();

            game.batch.begin();

            if (i < 3) {
                game.fontSmall.setColor(Color.BLACK);
                game.fontSmall.draw(game.batch, String.valueOf(i + 1), 40, y - 6);
            } else {
                game.fontSmall.setColor(new Color(0.8f, 0.8f, 0.9f, 1f));
                game.fontSmall.draw(game.batch, String.valueOf(i + 1), 40, y - 6);
            }

            game.fontSmall.setColor(Color.WHITE);
            game.fontSmall.draw(game.batch, u.getUsername(), 72, y - 6);

            game.fontSmall.setColor(new Color(0.82f, 0.78f, 1f, 1f));
            game.fontSmall.draw(game.batch, trim(u.getFullName(), 20), 230, y - 6);

            game.fontSmall.setColor(new Color(0.8f, 0.9f, 1f, 1f));
            game.fontSmall.draw(game.batch, String.valueOf(u.getTotalScore()), 420, y - 6);

            game.fontSmall.setColor(new Color(1f, 0.85f, 0.2f, 1f));
            game.fontSmall.draw(game.batch, String.valueOf(u.getTotalStarsCollected()), 535, y - 6);

            game.fontSmall.setColor(new Color(0.75f, 1f, 0.75f, 1f));
            game.fontSmall.draw(game.batch, String.valueOf(u.getTotalGamesPlayed()), 660, y - 6);

            game.batch.end();
        }
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

    private void handleInput() {
        if (!Gdx.input.justTouched()) {
            return;
        }

        com.badlogic.gdx.math.Vector3 _touch =
            new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);

        game.viewport.unproject(_touch);

        if (btnBack.contains(_touch.x, _touch.y)) {
            if (game.audioManager != null) {
                game.audioManager.playClick();
            }

            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
    }

    private boolean isHovered(Rectangle r) {
        com.badlogic.gdx.math.Vector3 _touch =
            new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);

        game.viewport.unproject(_touch);

        return r.contains(_touch.x, _touch.y);
    }

    private String trim(String s, int max) {
        if (s == null) {
            return "";
        }

        if (s.length() <= max) {
            return s;
        }

        return s.substring(0, max - 3) + "...";
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
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

public class MainMenuScreen implements Screen {

    private final MainGame    game;
    private final UserManager um;
    private final LevelData[] levels;
    private ShapeRenderer     sr;

    // 5 botones de nivel en grilla 5 col, dentro del área de 800
    private final Rectangle[] btnLevels   = new Rectangle[5];
    private final Rectangle   btnStats    = new Rectangle(20,  18, 155, 42);
    private final Rectangle   btnSettings = new Rectangle(185, 18, 155, 42);
    private final Rectangle   btnLogout   = new Rectangle(350, 18, 155, 42);
    private final Rectangle   btnRanking  = new Rectangle(515, 18, 155, 42);

    public MainMenuScreen(MainGame game) {
        this.game   = game;
        this.um     = UserManager.getInstance();
        this.levels = LevelData.createAll();

        // 5 botones de nivel distribuidos uniformemente en 800px
        for (int i = 0; i < 5; i++) {
            float bx = 20 + i * 152f;
            btnLevels[i] = new Rectangle(bx, 200, 136, 130);
        }
    }

    @Override
    public void show() {
        sr = new ShapeRenderer();

        UserData ud = um.getCurrentUser();

        if (ud != null) {
            game.applyRuntimePreferences(ud);
        }

        if (game.audioManager != null) {
            game.audioManager.playMenuMusic();
        }
    }

    @Override
    public void render(float delta) {
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);
        sr.setProjectionMatrix(game.camera.combined);

        Gdx.gl.glClearColor(0.08f, 0.06f, 0.14f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        UserData ud = um.getCurrentUser();

        drawBackground();
        drawHeader(ud);
        drawLevelGrid(ud);
        drawBottomButtons();
        handleInput();
    }

    private void drawBackground() {
        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Fondo principal
        sr.setColor(new Color(0.08f, 0.06f, 0.14f, 1f));
        sr.rect(0, 0, 800, 700);

        // Zona superior
        sr.setColor(new Color(0.15f, 0.10f, 0.28f, 1f));
        sr.rect(0, 430, 800, 270);

        // Zona inferior
        sr.setColor(new Color(0.10f, 0.07f, 0.20f, 1f));
        sr.rect(0, 0, 800, 190);

        // Decoración
        sr.setColor(new Color(0.5f, 0.2f, 0.85f, 0.12f));
        sr.circle(720, 610, 95);

        sr.setColor(new Color(0.2f, 0.75f, 0.2f, 0.12f));
        sr.circle(85, 95, 70);

        sr.setColor(new Color(1f, 0.85f, 0.2f, 0.08f));
        sr.circle(720, 95, 55);

        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);

        sr.setColor(new Color(0.55f, 0.35f, 0.9f, 0.35f));
        sr.line(0, 430, 800, 430);
        sr.line(0, 190, 800, 190);

        sr.end();
    }

    private void drawHeader(UserData ud) {
        // Avatar
        sr.begin(ShapeRenderer.ShapeType.Filled);

        String av = ud != null ? ud.getAvatarId() : "avatar1";

        // sombra
        sr.setColor(new Color(0f, 0f, 0f, 0.35f));
        sr.circle(58, 557, 42);

        // cuerpo
        sr.setColor(avatarColor(av));
        sr.circle(55, 560, 42);

        // ojos
        sr.setColor(Color.WHITE);
        sr.circle(42, 572, 11);
        sr.circle(68, 572, 11);

        sr.setColor(Color.BLACK);
        sr.circle(44, 570, 5);
        sr.circle(70, 570, 5);

        // boca
        sr.setColor(Color.BLACK);
        sr.ellipse(33, 538, 44, 16);

        // brillo
        sr.setColor(new Color(1f, 1f, 1f, 0.18f));
        sr.circle(42, 580, 8);

        sr.end();

        game.batch.begin();

        game.fontLarge.setColor(new Color(0.9f, 0.75f, 1f, 1f));
        game.fontLarge.draw(game.batch, "Cut the Rope", 110, 575);

        if (ud != null) {
            String greeting = "en".equals(ud.getLanguage()) ? "Hello, " : "¡Hola, ";

            game.font.setColor(Color.WHITE);
            game.font.draw(game.batch, greeting + ud.getFullName() + "!", 110, 550);

            game.fontSmall.setColor(new Color(0.78f, 0.78f, 0.78f, 1f));
            game.fontSmall.draw(
                game.batch,
                MainGame.t("Partidas:") + " " + ud.getTotalGamesPlayed()
                    + "  " + MainGame.t("Estrellas:") + " " + ud.getTotalStarsCollected()
                    + "  " + MainGame.t("Puntos:") + " " + ud.getTotalScore(),
                110,
                530
            );
        }

        game.fontSmall.setColor(new Color(0.7f, 0.6f, 0.9f, 1f));
        game.fontSmall.draw(game.batch, MainGame.t("Selecciona un nivel:"), 20, 360);

        game.batch.end();
    }

    private void drawLevelGrid(UserData ud) {
        boolean[] unlocked = ud != null
            ? ud.getLevelUnlocked()
            : new boolean[]{true, false, false, false, false};

        int[] stars = ud != null ? ud.getLevelStars() : new int[5];
        long[] best = ud != null ? ud.getBestTimePerLevel() : new long[5];

        for (int i = 0; i < 5; i++) {
            Rectangle r = btnLevels[i];
            boolean unl = unlocked[i];
            boolean hov = isHovered(r);

            // Tarjeta del nivel
            sr.begin(ShapeRenderer.ShapeType.Filled);

            sr.setColor(new Color(0f, 0f, 0f, 0.28f));
            sr.rect(r.x + 4, r.y - 4, r.width, r.height);

            if (unl) {
                sr.setColor(hov ? new Color(0.35f, 0.25f, 0.55f, 1f)
                                : new Color(0.22f, 0.16f, 0.40f, 1f));
            } else {
                sr.setColor(new Color(0.12f, 0.10f, 0.20f, 1f));
            }

            sr.rect(r.x, r.y, r.width, r.height);

            if (unl) {
                sr.setColor(new Color(1f, 1f, 1f, 0.08f));
                sr.rect(r.x, r.y + r.height - 8, r.width, 8);
            }

            sr.end();

            // Borde
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(unl ? new Color(0.6f, 0.4f, 1f, 1f)
                            : new Color(0.28f, 0.22f, 0.38f, 1f));
            sr.rect(r.x, r.y, r.width, r.height);
            sr.end();

            game.batch.begin();

            // Número de nivel
            game.fontLarge.setColor(unl ? Color.WHITE : new Color(0.4f, 0.4f, 0.4f, 1f));
            game.fontLarge.draw(game.batch, String.valueOf(i + 1), r.x + 50, r.y + r.height - 8);

            if (unl) {
                // Título del nivel
                game.fontSmall.setColor(new Color(0.82f, 0.76f, 1f, 1f));
                game.fontSmall.draw(game.batch, MainGame.t(levels[i].title), r.x + 8, r.y + 76);

                // Estrellas ganadas
                StringBuilder sb = new StringBuilder();

                for (int s = 0; s < 3; s++) {
                    sb.append(s < stars[i] ? "★" : "☆");
                }

                game.fontSmall.setColor(new Color(1f, 0.85f, 0.2f, 1f));
                game.fontSmall.draw(game.batch, sb.toString(), r.x + 26, r.y + 43);

                // Mejor tiempo
                if (best[i] > 0) {
                    long secs = best[i] / 1000;

                    game.fontSmall.setColor(new Color(0.55f, 1f, 0.55f, 1f));
                    game.fontSmall.draw(
                        game.batch,
                        MainGame.t("Mejor:") + " " + String.format("%02d:%02d", secs / 60, secs % 60),
                        r.x + 12,
                        r.y + 22
                    );
                } else {
                    game.fontSmall.setColor(new Color(0.60f, 0.60f, 0.70f, 1f));
                    game.fontSmall.draw(game.batch, "--:--", r.x + 47, r.y + 22);
                }
            } else {
                game.fontLarge.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
                game.fontLarge.draw(game.batch, "X", r.x + 55, r.y + 72);

                game.fontSmall.setColor(new Color(0.55f, 0.55f, 0.60f, 1f));
                game.fontSmall.draw(game.batch, MainGame.t("Bloqueado"), r.x + 32, r.y + 38);
            }

            game.batch.end();
        }
    }

    private void drawBottomButtons() {
        drawBtn(btnStats,    MainGame.t("Estadísticas"),    new Color(0.2f, 0.5f, 0.7f, 1f));
        drawBtn(btnSettings, MainGame.t("Ajustes"),         new Color(0.5f, 0.4f, 0.2f, 1f));
        drawBtn(btnLogout,   MainGame.t("Cerrar sesión"),   new Color(0.6f, 0.2f, 0.2f, 1f));
        drawBtn(btnRanking,  MainGame.t("Ranking"),         new Color(0.3f, 0.6f, 0.3f, 1f));
    }

    private void drawBtn(Rectangle r, String text, Color col) {
        boolean hov = isHovered(r);

        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(0f, 0f, 0f, 0.25f));
        sr.rect(r.x + 3, r.y - 3, r.width, r.height);

        sr.setColor(hov ? col.cpy().add(0.14f, 0.14f, 0.14f, 0) : col);
        sr.rect(r.x, r.y, r.width, r.height);

        sr.setColor(new Color(1f, 1f, 1f, 0.10f));
        sr.rect(r.x, r.y + r.height - 5, r.width, 5);

        sr.end();

        game.batch.begin();

        game.fontSmall.setColor(Color.WHITE);
        game.fontSmall.draw(game.batch, text, r.x + 8, r.y + 28);

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

        UserData ud = um.getCurrentUser();

        boolean[] unlocked = ud != null
            ? ud.getLevelUnlocked()
            : new boolean[]{true, false, false, false, false};

        for (int i = 0; i < 5; i++) {
            if (btnLevels[i].contains(mx, my) && unlocked[i]) {
                game.audioManager.playClick();
                game.setScreen(new GameScreen(game, i));
                dispose();
                return;
            }
        }

        if (btnStats.contains(mx, my)) {
            game.audioManager.playClick();
            game.setScreen(new StatsScreen(game));
            dispose();
            return;
        }

        if (btnSettings.contains(mx, my)) {
            game.audioManager.playClick();
            game.setScreen(new SettingsScreen(game));
            dispose();
            return;
        }

        if (btnLogout.contains(mx, my)) {
            game.audioManager.playClick();
            um.logout();
            MainGame.loadLang("es");
            game.setScreen(new LoginScreen(game));
            dispose();
            return;
        }

        if (btnRanking.contains(mx, my)) {
            game.audioManager.playClick();
            game.setScreen(new RankingScreen(game));
            dispose();
        }
    }

    private boolean isHovered(Rectangle r) {
        com.badlogic.gdx.math.Vector3 _touch =
            new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);

        game.viewport.unproject(_touch);

        return r.contains(_touch.x, _touch.y);
    }

    private Color avatarColor(String id) {
        switch (id) {
            case "avatar2":
                return new Color(0.2f, 0.4f, 0.9f, 1f);

            case "avatar3":
                return new Color(0.9f, 0.3f, 0.3f, 1f);

            case "avatar4":
                return new Color(0.9f, 0.6f, 0.1f, 1f);

            case "avatar5":
                return new Color(0.7f, 0.2f, 0.9f, 1f);

            default:
                return new Color(0.2f, 0.75f, 0.2f, 1f);
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
package com.cuttherope.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class MainMenuScreen implements Screen {
    private final MainGame game;
    private final UserManager um;

    // ── Niveles: fila 1 → 1,2,3  |  fila 2 → 4,5 (centrados en panel morado) ──
    private final Rectangle[] levelBtns = new Rectangle[5];
    private static final String[] LEVEL_LABELS = {"1-1","1-2","1-7","1-16","1-18"};

    // ── Botones inferiores ──
    private final Rectangle btnStats    = new Rectangle(10,  15, 148, 45);
    private final Rectangle btnSettings = new Rectangle(166, 15, 148, 45);
    private final Rectangle btnRanking  = new Rectangle(322, 15, 148, 45);
    private final Rectangle btnVersus   = new Rectangle(478, 15, 148, 45);
    private final Rectangle btnLogout   = new Rectangle(634, 15, 148, 45);

    // Colores
    private static final Color BLANCO_NIVEL = new Color(0.98f, 0.98f, 0.98f, 1.00f);
    private static final Color BORDE_NIVEL  = new Color(0.36f, 0.27f, 0.49f, 1.00f);
    private static final Color BLOQUEADO    = new Color(0.78f, 0.78f, 0.82f, 1.00f);
    private static final Color CELESTE      = new Color(0.12f, 0.56f, 1.00f, 0.92f);
    private static final Color CELESTE_OSC  = new Color(0.05f, 0.25f, 0.60f, 1.00f);

    private ShapeRenderer sr;
    private Texture fondo;

    public MainMenuScreen(MainGame game) {
        this.game = game;
        this.um   = UserManager.getInstance();

        // Fila 1 — 3 círculos centrados dentro del panel morado
        levelBtns[0] = new Rectangle(264, 356, 72, 72);
        levelBtns[1] = new Rectangle(364, 356, 72, 72);
        levelBtns[2] = new Rectangle(464, 356, 72, 72);

        // Fila 2 — 2 círculos centrados y más abajo, sin tocar la fila superior
        levelBtns[3] = new Rectangle(314, 236, 72, 72);
        levelBtns[4] = new Rectangle(414, 236, 72, 72);
    }

    @Override public void show() {
        sr    = new ShapeRenderer();
        fondo = AssetPaths.texture(AssetPaths.FONDO_MENU);
        UserData ud = um.getCurrentUser();
        if (ud != null) game.applyRuntimePreferences(ud);
        if (game.audioManager != null) game.audioManager.playMenuMusic();
    }

    @Override public void render(float delta) {
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);
        sr.setProjectionMatrix(game.camera.combined);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Dibujar fondo (imagen Cut the Rope con panel morado)
        game.batch.begin();
        game.batch.draw(fondo, 0, 0, 800, 700);
        game.batch.end();

        drawTitulo();
        drawLevels();
        drawBottom();
        handle();
    }

    private void drawTitulo() {
        game.batch.begin();
        game.fontLarge.getData().setScale(1.5f);
        game.fontLarge.setColor(new Color(1f, 0.78f, 0.08f, 1f));
        game.fontLarge.draw(game.batch, MainGame.t("Mapa de niveles"), 440, 625);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, MainGame.t("Selecciona un nivel"), 480, 590);
        game.batch.end();
    }

    private void drawLevels() {
        UserData ud = um.getCurrentUser();
        for (int i = 0; i < levelBtns.length; i++) {
            Rectangle r   = levelBtns[i];
            float cx      = r.x + r.width  / 2f;
            float cy      = r.y + r.height / 2f;
            float radio   = 36f;
            boolean desbloqueado = ud == null
                || (ud.getLevelUnlocked() != null && ud.getLevelUnlocked()[i]);

            // Sombra suave para que el botón resalte sobre el panel morado
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(new Color(0f, 0f, 0f, 0.28f));
            sr.circle(cx + 3, cy - 3, radio + 2);
            sr.end();

            // Relleno blanco como en la referencia
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(desbloqueado ? BLANCO_NIVEL : BLOQUEADO);
            sr.circle(cx, cy, radio);
            sr.end();

            // Doble borde delgado
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(BORDE_NIVEL);
            sr.circle(cx, cy, radio);
            sr.circle(cx, cy, radio - 5);
            sr.end();

            // Número centrado
            game.batch.begin();
            game.fontLarge.getData().setScale(2.15f);
            game.fontLarge.setColor(desbloqueado ? Color.BLACK : Color.DARK_GRAY);
            String lbl = desbloqueado ? String.valueOf(i + 1) : "X";
            game.fontLarge.draw(game.batch, lbl, cx - 12, cy + 24);
            game.batch.end();
        }
    }

    private void drawBottom() {
        btnCeleste(btnStats,    MainGame.t("Estadísticas"));
        btnCeleste(btnSettings, MainGame.t("Ajustes"));
        btnCeleste(btnRanking,  MainGame.t("Ranking"));
        btnCeleste(btnVersus,   MainGame.t("VS"));
        btnCeleste(btnLogout,   MainGame.t("Salir"));
    }

    private void btnCeleste(Rectangle r, String text) {
        // Sombra
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0f, 0f, 0f, 0.35f));
        sr.rect(r.x + 3, r.y - 3, r.width, r.height);
        sr.end();
        // Relleno azul celeste
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(CELESTE);
        sr.rect(r.x, r.y, r.width, r.height);
        sr.end();
        // Outline negro
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(Color.BLACK);
        sr.rect(r.x,     r.y,     r.width,     r.height);
        sr.rect(r.x - 1, r.y - 1, r.width + 2, r.height + 2);
        sr.end();
        // Texto blanco centrado
        game.batch.begin();
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, text, r.x + (r.width / 2f) - (text.length() * 4.5f), r.y + 28);
        game.batch.end();
    }

    private void handle() {
        if (!Gdx.input.justTouched()) return;
        float x = Gdx.input.getX() * 800f / Gdx.graphics.getWidth();
        float y = 700f - Gdx.input.getY() * 700f / Gdx.graphics.getHeight();
        if (game.audioManager != null) game.audioManager.playClick();
        UserData ud = um.getCurrentUser();
        for (int i = 0; i < levelBtns.length; i++) {
            if (levelBtns[i].contains(x, y)
                && (ud == null || (ud.getLevelUnlocked() != null && ud.getLevelUnlocked()[i]))) {
                game.setScreen(new GameScreen(game, i));
                return;
            }
        }
        if      (btnStats.contains(x, y))    game.setScreen(new StatsScreen(game));
        else if (btnSettings.contains(x, y)) game.setScreen(new SettingsScreen(game));
        else if (btnRanking.contains(x, y))  game.setScreen(new RankingScreen(game));
        else if (btnVersus.contains(x, y))   game.setScreen(new VersusScreen(game));
        else if (btnLogout.contains(x, y))   { um.logout(); game.setScreen(new LoginScreen(game)); }
    }

    @Override public void resize(int w, int h) { game.viewport.update(w, h, true); }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}
    @Override public void dispose() {
        if (sr    != null) sr.dispose();
        if (fondo != null) fondo.dispose();
    }
}

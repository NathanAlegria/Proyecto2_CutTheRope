package com.cuttherope.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import java.util.List;

public class RankingScreen implements Screen {

    private final MainGame game;
    private ShapeRenderer sr;
    private Texture fondo;
    private List<UserData> ranking;

    private final Rectangle btnBack = new Rectangle(110, 28, 155, 42);
    private final Rectangle mainPanel = new Rectangle(68, 72, 654, 540);

    private static final Color GOLD = new Color(0.95f, 0.75f, 0.12f, 1f);
    private static final Color SILVER = new Color(0.78f, 0.78f, 0.80f, 1f);
    private static final Color BRONZE = new Color(0.82f, 0.52f, 0.18f, 1f);

    public RankingScreen(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        sr = new ShapeRenderer();
        fondo = AssetPaths.textureAnyOrNull("FondoStat", "FondoStat.png", "FondoStat.jpeg", "FondoStat.jpg", "Imagenes/FondoStat", "Imagenes/FondoStat.png", "Imagenes/FondoStat.jpeg", "Imagenes/FondoStat.jpg", "assets/FondoStat", "assets/Imagenes/FondoStat", "assets/Imagenes/FondoStat.png", "assets/Imagenes/FondoStat.jpeg", AssetPaths.FONDO_MENU, AssetPaths.FONDO);

        if (game.audioManager != null) {
            game.audioManager.playMenuMusic();
        }

        UserData ud = UserManager.getInstance().getCurrentUser();
        if (ud != null) game.applyRuntimePreferences(ud);

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

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        drawBackground();
        drawMainPanel();
        drawHeader();
        drawTable();
        drawBtn(btnBack, MainGame.t("← Menú"), new Color(0.42f, 0.28f, 0.70f, 1f));
        handleInput();
    }

    private void drawBackground() {
        game.batch.begin();
        if (fondo != null) {
            game.batch.draw(fondo, 0, 0, 800, 700);
        }
        game.batch.end();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0f, 0f, 0f, 0.18f));
        sr.rect(0, 0, 800, 700);
        sr.end();
    }

    private void drawMainPanel() {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0f, 0f, 0f, 0.18f));
        sr.rect(mainPanel.x + 4, mainPanel.y - 4, mainPanel.width, mainPanel.height);
        sr.setColor(new Color(0.08f, 0.05f, 0.22f, 0.90f));
        sr.rect(mainPanel.x, mainPanel.y, mainPanel.width, mainPanel.height);
        sr.setColor(new Color(0.06f, 0.04f, 0.18f, 0.35f));
        sr.rect(mainPanel.x + 2, mainPanel.y + 2, mainPanel.width - 4, mainPanel.height - 4);

        // Bloques rectangulares decorativos en las esquinas, sin usar círculos.
        sr.setColor(new Color(0.92f, 0.72f, 0.08f, 0.55f));
        sr.rect(mainPanel.x + mainPanel.width - 92, mainPanel.y + mainPanel.height - 86, 92, 86);
        sr.setColor(new Color(0.50f, 0.18f, 0.88f, 0.42f));
        sr.rect(0, 0, 84, 84);
        sr.setColor(new Color(0.24f, 0.72f, 0.06f, 0.48f));
        sr.rect(mainPanel.x + mainPanel.width - 82, mainPanel.y - 2, 82, 82);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(new Color(0.42f, 0.30f, 0.72f, 1f));
        sr.rect(mainPanel.x, mainPanel.y, mainPanel.width, mainPanel.height);
        sr.end();
    }

    private void drawHeader() {
        game.batch.begin();

        game.fontLarge.setColor(new Color(1f, 0.84f, 0.10f, 1f));
        game.fontLarge.draw(game.batch, MainGame.t("Ranking Global"), 285, 585);

        game.font.setColor(new Color(0.90f, 0.90f, 0.94f, 1f));
        game.font.draw(game.batch, "#", 122, 538);
        game.font.draw(game.batch, MainGame.t("Usuario"), 168, 538);
        game.font.draw(game.batch, MainGame.t("Nombre"), 310, 538);
        game.font.draw(game.batch, MainGame.t("Pts:"), 470, 538);
        game.font.draw(game.batch, MainGame.t("Estrellas:"), 558, 538);
        game.font.draw(game.batch, MainGame.t("Partidas:"), 655, 538);

        game.batch.end();
    }

    private void drawTable() {
        if (ranking == null || ranking.isEmpty()) {
            game.batch.begin();
            game.font.setColor(Color.WHITE);
            game.font.draw(game.batch, MainGame.t("No hay jugadores registrados todavía."), 220, 350);
            game.batch.end();
            return;
        }

        int max = Math.min(10, ranking.size());
        for (int i = 0; i < max; i++) {
            UserData u = ranking.get(i);
            float y = 500 - i * 62;
            drawRow(i, u, y);
        }
    }

    private void drawRow(int i, UserData u, float y) {
        float rowX = 92f;
        float rowY = y - 34f;
        float rowW = 610f;
        float rowH = 46f;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0.16f, 0.11f, 0.30f, 0.96f));
        sr.rect(rowX, rowY, rowW, rowH);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(new Color(0.34f, 0.24f, 0.62f, 1f));
        sr.rect(rowX, rowY, rowW, rowH);
        sr.end();

        Color rankColor = new Color(0.35f, 0.35f, 0.40f, 1f);
        if (i == 0) rankColor = GOLD;
        else if (i == 1) rankColor = SILVER;
        else if (i == 2) rankColor = BRONZE;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(rankColor);
        sr.rect(112, y - 28, 34, 34);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(new Color(0f, 0f, 0f, 0.6f));
        sr.rect(112, y - 28, 34, 34);
        sr.end();

        game.batch.begin();

        game.font.setColor(i < 3 ? Color.BLACK : Color.WHITE);
        game.font.draw(game.batch, String.valueOf(i + 1), 124, y - 3);

        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, u.getUsername(), 166, y - 3);

        game.font.setColor(new Color(0.92f, 0.92f, 0.97f, 1f));
        game.font.draw(game.batch, trim(u.getFullName(), 18), 310, y - 3);

        game.font.setColor(new Color(0.82f, 0.90f, 1f, 1f));
        game.font.draw(game.batch, String.valueOf(u.getTotalScore()), 462, y - 3);

        game.font.setColor(new Color(1f, 0.85f, 0.15f, 1f));
        game.font.draw(game.batch, String.valueOf(u.getTotalStarsCollected()), 573, y - 3);

        game.font.setColor(new Color(0.62f, 1f, 0.48f, 1f));
        game.font.draw(game.batch, String.valueOf(u.getTotalGamesPlayed()), 673, y - 3);

        game.batch.end();
    }

    private void drawBtn(Rectangle r, String text, Color col) {
        boolean hov = isHovered(r);

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0f, 0f, 0f, 0.35f));
        sr.rect(r.x + 4, r.y - 4, r.width, r.height);
        sr.setColor(hov ? col.cpy().add(0.10f, 0.10f, 0.10f, 0) : col);
        sr.rect(r.x, r.y, r.width, r.height);
        sr.setColor(new Color(1f, 1f, 1f, 0.12f));
        sr.rect(r.x, r.y + r.height - 5, r.width, 5);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(Color.BLACK);
        sr.rect(r.x, r.y, r.width, r.height);
        sr.end();

        game.batch.begin();
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, text, r.x + 12, r.y + 28);
        game.batch.end();
    }

    private void handleInput() {
        if (!Gdx.input.justTouched()) return;

        Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        game.viewport.unproject(touch);

        if (btnBack.contains(touch.x, touch.y)) {
            if (game.audioManager != null) game.audioManager.playClick();
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
    }

    private boolean isHovered(Rectangle r) {
        Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        game.viewport.unproject(touch);
        return r.contains(touch.x, touch.y);
    }

    private String trim(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max - 3) + "...";
    }

    @Override public void resize(int w, int h) { game.viewport.update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (sr != null) sr.dispose();
        if (fondo != null) {
            fondo.dispose();
            fondo = null;
        }
    }
}

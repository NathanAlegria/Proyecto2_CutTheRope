package com.cuttherope.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import java.text.SimpleDateFormat;
import java.util.List;

public class StatsScreen implements Screen {

    private final MainGame game;
    private final UserData ud;
    private ShapeRenderer sr;
    private Texture fondo;

    private int normalScrollOffset = 0;
    private int vsScrollOffset = 0;

    private final Rectangle btnBack = new Rectangle(22, 18, 150, 40);

    private final Rectangle panelNormal = new Rectangle(98, 315, 610, 255);
    private final Rectangle panelVS = new Rectangle(98, 125, 610, 150);

    private final Rectangle normalBar = new Rectangle(713, 318, 28, 249);
    private final Rectangle vsBar = new Rectangle(713, 128, 28, 144);

    private final Rectangle btnNormalUp = new Rectangle(713, 534, 28, 26);
    private final Rectangle btnNormalDown = new Rectangle(713, 326, 28, 26);
    private final Rectangle btnVsUp = new Rectangle(713, 239, 28, 26);
    private final Rectangle btnVsDown = new Rectangle(713, 136, 28, 26);

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yy HH:mm");

    public StatsScreen(MainGame game) {
        this.game = game;
        this.ud = UserManager.getInstance().getCurrentUser();
    }

    @Override
    public void show() {
        sr = new ShapeRenderer();
        fondo = AssetPaths.textureAnyOrNull("FondoStat", "FondoStat.png", "FondoStat.jpeg", "FondoStat.jpg", "Imagenes/FondoStat", "Imagenes/FondoStat.png", "Imagenes/FondoStat.jpeg", "Imagenes/FondoStat.jpg", "assets/FondoStat", "assets/Imagenes/FondoStat", "assets/Imagenes/FondoStat.png", "assets/Imagenes/FondoStat.jpeg", AssetPaths.FONDO_MENU, AssetPaths.FONDO);
        if (game.audioManager != null) game.audioManager.playMenuMusic();
        if (ud != null) game.applyRuntimePreferences(ud);
    }

    @Override
    public void render(float delta) {
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);
        sr.setProjectionMatrix(game.camera.combined);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        drawBackground();
        drawHeader();
        drawNormalHistoryPanel();
        drawVsHistoryPanel();
        drawButtons();
        handleInput();
    }

    private void drawBackground() {
        game.batch.begin();
        if (fondo != null) {
            game.batch.draw(fondo, 0, 0, 800, 700);
        }
        game.batch.end();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0f, 0f, 0f, 0.25f));
        sr.rect(0, 0, 800, 700);

        sr.setColor(new Color(0f, 0f, 0f, 0.38f));
        sr.rect(0, 610, 800, 90);

        sr.setColor(new Color(0.38f, 0.18f, 0.78f, 0.95f));
        sr.rect(675, 610, 115, 90);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(new Color(1f, 1f, 1f, 0.25f));
        sr.line(18, 605, 782, 605);
        sr.end();
    }

    private void drawHeader() {
        game.batch.begin();

        game.fontLarge.setColor(new Color(0.93f, 0.74f, 1f, 1f));
        game.fontLarge.draw(game.batch, MainGame.t("Estadísticas"), 322, 678);

        if (ud == null) {
            game.font.setColor(Color.WHITE);
            game.font.draw(game.batch, MainGame.t("No hay usuario activo."), 250, 400);
            game.batch.end();
            return;
        }

        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch,
                MainGame.t("Jugador:") + " " + ud.getFullName() + " (@" + ud.getUsername() + ")",
                112, 650);

        game.fontSmall.setColor(new Color(0.85f, 0.85f, 0.88f, 1f));
        game.fontSmall.draw(game.batch,
                MainGame.t("Registro:") + " " + SDF.format(ud.getRegistrationDate())
                        + "   " + MainGame.t("Última sesión:") + " " + SDF.format(ud.getLastLoginDate()),
                112, 629);

        game.font.setColor(new Color(0.82f, 0.92f, 1f, 1f));
        game.font.draw(game.batch,
                MainGame.t("Partidas:") + " " + ud.getTotalGamesPlayed()
                        + "   " + MainGame.t("Tiempo total:") + " " + ud.getFormattedTotalTime()
                        + "   " + MainGame.t("Estrellas:") + " " + ud.getTotalStarsCollected()
                        + "   " + MainGame.t("Pts:") + " " + ud.getTotalScore(),
                112, 603);

        game.batch.end();
    }

    private void drawNormalHistoryPanel() {
        drawPanel(panelNormal);
        drawScrollbar(normalBar, btnNormalUp, btnNormalDown, true);

        game.batch.begin();
        game.font.setColor(new Color(0.95f, 0.90f, 1f, 1f));
        game.font.draw(game.batch, MainGame.t("Historial normal:"), 128, 542);
        game.batch.end();

        if (ud == null) return;
        List<UserData.GameRecord> hist = ud.getGameHistory();
        if (hist == null || hist.isEmpty()) {
            game.batch.begin();
            game.fontSmall.setColor(new Color(0.82f, 0.82f, 0.88f, 1f));
            game.fontSmall.draw(game.batch, MainGame.t("Aún no has jugado ninguna partida."), 130, 508);
            game.batch.end();
            return;
        }

        int visible = 5;
        int total = hist.size();
        if (normalScrollOffset < 0) normalScrollOffset = 0;
        if (normalScrollOffset > Math.max(0, total - visible)) normalScrollOffset = Math.max(0, total - visible);

        int start = Math.max(0, total - visible - normalScrollOffset);
        int end = Math.min(total, start + visible);

        for (int idx = start; idx < end; idx++) {
            UserData.GameRecord gr = hist.get(idx);
            int row = idx - start;
            float y = 500 - row * 40;
            drawRowBox(122, y - 23, 575, 34, row % 2 == 0);

            game.batch.begin();
            game.fontSmall.setColor(Color.WHITE);
            game.fontSmall.draw(game.batch, MainGame.t("Nivel") + " " + (gr.level + 1), 132, y);

            game.fontSmall.setColor(gr.won ? new Color(0.45f, 1f, 0.45f, 1f) : new Color(1f, 0.45f, 0.45f, 1f));
            game.fontSmall.draw(game.batch, MainGame.t(gr.won ? "Victoria" : "Derrota"), 220, y);

            game.fontSmall.setColor(new Color(1f, 0.85f, 0.2f, 1f));
            game.fontSmall.draw(game.batch, starsToText(gr.stars), 310, y);

            game.fontSmall.setColor(new Color(0.82f, 0.88f, 1f, 1f));
            game.fontSmall.draw(game.batch, formatTime(gr.timeMs), 415, y);

            game.fontSmall.setColor(new Color(0.80f, 0.80f, 0.86f, 1f));
            game.fontSmall.draw(game.batch, SDF.format(gr.playedAt), 510, y);
            game.batch.end();
        }
    }

    private void drawVsHistoryPanel() {
        drawPanel(panelVS);
        drawScrollbar(vsBar, btnVsUp, btnVsDown, false);

        game.batch.begin();
        game.font.setColor(new Color(1f, 0.84f, 0.20f, 1f));
        game.font.draw(game.batch, MainGame.t("Historial de partidas VS:"), 128, 245);
        game.batch.end();

        if (ud == null) return;

        game.batch.begin();
        game.fontSmall.setColor(new Color(1f, 0.92f, 0.45f, 1f));
        game.fontSmall.draw(game.batch,
                MainGame.t("VS jugados:") + " " + ud.getVersusPlayed()
                        + "   " + MainGame.t("Ganados:") + " " + ud.getVersusWins()
                        + "   " + MainGame.t("Perdidos:") + " " + ud.getVersusLosses(),
                128, 220);
        game.batch.end();

        List<UserData.VersusHistoryRecord> vs = ud.getVersusHistory();
        if (vs == null || vs.isEmpty()) {
            game.batch.begin();
            game.fontSmall.setColor(new Color(0.82f, 0.82f, 0.88f, 1f));
            game.fontSmall.draw(game.batch, MainGame.t("Aún no tienes partidas VS terminadas."), 130, 186);
            game.batch.end();
            return;
        }

        int visible = 2;
        int total = vs.size();
        if (vsScrollOffset < 0) vsScrollOffset = 0;
        if (vsScrollOffset > Math.max(0, total - visible)) vsScrollOffset = Math.max(0, total - visible);

        int start = Math.max(0, total - visible - vsScrollOffset);
        int end = Math.min(total, start + visible);

        for (int idx = start; idx < end; idx++) {
            UserData.VersusHistoryRecord vr = vs.get(idx);
            int row = idx - start;
            float y = 186 - row * 38;
            drawRowBox(122, y - 22, 575, 32, row % 2 == 0);

            game.batch.begin();
            boolean winnerIsMe = ud.getUsername().equalsIgnoreCase(vr.winner);
            game.fontSmall.setColor(winnerIsMe ? new Color(0.45f, 1f, 0.45f, 1f) : new Color(1f, 0.65f, 0.65f, 1f));
            game.fontSmall.draw(game.batch, buildVsMessage(vr), 132, y);
            game.batch.end();
        }
    }

    private void drawPanel(Rectangle r) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0f, 0f, 0f, 0.18f));
        sr.rect(r.x + 5, r.y - 5, r.width, r.height);
        sr.setColor(new Color(0.07f, 0.05f, 0.22f, 0.95f));
        sr.rect(r.x, r.y, r.width, r.height);
        sr.setColor(new Color(0.05f, 0.03f, 0.16f, 0.45f));
        sr.rect(r.x + 2, r.y + 2, r.width - 4, r.height - 4);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(new Color(0.30f, 0.20f, 0.56f, 1f));
        sr.rect(r.x, r.y, r.width, r.height);
        sr.end();
    }

    private void drawRowBox(float x, float y, float w, float h, boolean alt) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(alt ? new Color(0.15f, 0.11f, 0.27f, 0.95f) : new Color(0.11f, 0.09f, 0.22f, 0.95f));
        sr.rect(x, y, w, h);
        sr.setColor(new Color(0.20f, 0.85f, 0.35f, 1f));
        sr.rect(x, y, 4, h);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(new Color(0.32f, 0.24f, 0.60f, 1f));
        sr.rect(x, y, w, h);
        sr.end();
    }

    private void drawScrollbar(Rectangle bar, Rectangle up, Rectangle down, boolean longHandle) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0.10f, 0.08f, 0.24f, 1f));
        sr.rect(bar.x, bar.y, bar.width, bar.height);

        float handleH = longHandle ? 70f : 48f;
        float handleY = longHandle ? 450f : 180f;
        sr.setColor(new Color(0.45f, 0.30f, 0.80f, 1f));
        sr.rect(bar.x + 5, handleY, bar.width - 10, handleH);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(new Color(0.28f, 0.20f, 0.52f, 1f));
        sr.rect(bar.x, bar.y, bar.width, bar.height);
        sr.end();

        drawArrowButton(up, true);
        drawArrowButton(down, false);
    }

    private void drawArrowButton(Rectangle r, boolean up) {
        boolean hov = isHovered(r);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(hov ? new Color(0.52f, 0.38f, 0.84f, 1f) : new Color(0.24f, 0.18f, 0.48f, 1f));
        sr.rect(r.x, r.y, r.width, r.height);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(new Color(0.55f, 0.45f, 0.95f, 1f));
        sr.rect(r.x, r.y, r.width, r.height);
        sr.end();

        float cx = r.x + r.width / 2f;
        float cy = r.y + r.height / 2f;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(Color.WHITE);
        if (up) {
            sr.triangle(cx - 6, cy - 2, cx + 6, cy - 2, cx, cy + 6);
        } else {
            sr.triangle(cx - 6, cy + 2, cx + 6, cy + 2, cx, cy - 6);
        }
        sr.end();
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
        game.fontSmall.draw(game.batch, text, r.x + 10, r.y + r.height - 8);
        game.batch.end();
    }

    private String buildVsMessage(UserData.VersusHistoryRecord vr) {
        String winner = vr.winner == null ? "Empate" : vr.winner;
        String loser;
        if ("Empate".equalsIgnoreCase(winner)) loser = MainGame.t("sin ganador");
        else loser = winner.equalsIgnoreCase(ud.getUsername()) ? vr.opponent : ud.getUsername();
        int totalPossible = vr.totalPossibleStars > 0 ? vr.totalPossibleStars : 15;
        int starsToShow = vr.winnerStars > 0 ? vr.winnerStars : vr.stars;
        long timeToShow = vr.winnerTimeMs > 0 ? vr.winnerTimeMs : vr.timeMs;
        String result = "Empate".equalsIgnoreCase(winner)
                ? MainGame.t("Empate contra") + " " + vr.opponent
                : winner + " " + MainGame.t("le ganó a") + " " + loser;
        return result + " | " + MainGame.t("Estrellas:") + " " + starsToShow + "/" + totalPossible
                + " | " + MainGame.t("Tiempo:") + " " + formatTime(timeToShow)
                + " | " + MainGame.t("Fecha:") + " " + SDF.format(vr.playedAt);
    }

    private String starsToText(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) sb.append(i < n ? "★" : "☆");
        return sb.toString();
    }

    private String formatTime(long ms) {
        if (ms <= 0) return "--:--";
        long sec = ms / 1000;
        return String.format("%02d:%02d", sec / 60, sec % 60);
    }

    private boolean isHovered(Rectangle r) {
        Vector3 t = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        game.viewport.unproject(t);
        return r.contains(t.x, t.y);
    }

    private void playClick() {
        if (game.audioManager != null) game.audioManager.playClick();
    }

    private void handleInput() {
        if (!Gdx.input.justTouched()) return;
        Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        game.viewport.unproject(touch);

        if (btnBack.contains(touch.x, touch.y)) {
            playClick();
            game.setScreen(new MainMenuScreen(game));
            dispose();
            return;
        }

        if (btnNormalUp.contains(touch.x, touch.y) && ud != null && ud.getGameHistory() != null) {
            playClick();
            normalScrollOffset = Math.min(normalScrollOffset + 1, Math.max(0, ud.getGameHistory().size() - 5));
        }
        if (btnNormalDown.contains(touch.x, touch.y)) {
            playClick();
            normalScrollOffset = Math.max(normalScrollOffset - 1, 0);
        }

        if (btnVsUp.contains(touch.x, touch.y) && ud != null && ud.getVersusHistory() != null) {
            playClick();
            vsScrollOffset = Math.min(vsScrollOffset + 1, Math.max(0, ud.getVersusHistory().size() - 2));
        }
        if (btnVsDown.contains(touch.x, touch.y)) {
            playClick();
            vsScrollOffset = Math.max(vsScrollOffset - 1, 0);
        }
    }

    @Override public void resize(int width, int height) { game.viewport.update(width, height, true); }
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

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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import com.cuttherope.game.LevelData;
import com.cuttherope.game.UserData;
import com.cuttherope.game.UserManager;
import com.cuttherope.game.GameLogicThread;
import com.cuttherope.game.StatsSaveThread;

import java.util.ArrayList;
import java.util.List;

/**
 * GameScreen - Pantalla de juego principal.
 * Extiende Juego e implementa Screen.
 * Gestiona física de cuerdas, estrellas, Om Nom, temporizador, audio e idioma.
 */
public class GameScreen extends Juego implements Screen {

    private final MainGame game;
    private final UserManager um;
    private final LevelData[] allLevels;
    private LevelData currentLevelData;
    private ShapeRenderer sr;

    // ── Entidades del nivel ───────────────────────────────────────────────────
    private Candy candy;
    private OmNom omNom;
    private List<Rope> ropes;
    private List<Star> stars;

    // ── Estado de corte ───────────────────────────────────────────────────────
    private boolean cutting = false;
    private float cutStartX, cutStartY;
    private float cutCurrentX, cutCurrentY;

    // ── Pantalla de resultado ─────────────────────────────────────────────────
    private enum GameState {
        PLAYING, WIN, LOSE, PAUSED
    }

    private GameState state = GameState.PLAYING;
    private float resultTimer = 0f;

    // ── HUD ───────────────────────────────────────────────────────────────────
    private int starsCollected = 0;
    private int displayTime = 0;
    private boolean timeWarning = false;
    private boolean timeoutLosePending = false;
    private long levelStartMs;

    // ── Hilo de temporizador ─────────────────────────────────────────────────
    private GameLogicThread timerThread;

    // ── Botones HUD ──────────────────────────────────────────────────────────
    private final Rectangle btnPause = new Rectangle(640, 660, 55, 32);
    private final Rectangle btnMenu = new Rectangle(578, 660, 55, 32);
    private final Rectangle btnRestart = new Rectangle(310, 210, 180, 45);
    private final Rectangle btnNext = new Rectangle(310, 260, 180, 45);
    private final Rectangle btnBack = new Rectangle(310, 158, 180, 45);

    public GameScreen(MainGame game, int levelIndex) {
        super();
        this.game = game;
        this.um = UserManager.getInstance();
        this.allLevels = LevelData.createAll();
        this.currentLevel = levelIndex;
    }

    // ── Juego abstracto: implementaciones ────────────────────────────────────

    @Override
    public void initLevel(int level) {
        currentLevelData = allLevels[level];
        LevelData d = currentLevelData;

        // Inicializar entidades
        candy = new Candy(d.candyX, d.candyY);
        omNom = new OmNom(d.omNomX, d.omNomY);

        // Aplicar avatar del usuario a Om Nom
        UserData udInit = um.getCurrentUser();
        if (udInit != null) {
            omNom.setAvatarColor(udInit.getAvatarId());
        }

        ropes = new ArrayList<>();
        stars = new ArrayList<>();

        // Crear cuerdas
        for (int i = 0; i < d.anchorX.length; i++) {
            ropes.add(new Rope(d.anchorX[i], d.anchorY[i], candy, d.ropeColor));
        }

        // Crear estrellas
        for (int i = 0; i < d.starX.length; i++) {
            stars.add(new Star(d.starX[i], d.starY[i]));
        }

        // Reiniciar estado
        starsCollected = 0;
        timeWarning = false;
        timeoutLosePending = false;
        state = GameState.PLAYING;
        resultTimer = 0f;
        levelStartMs = System.currentTimeMillis();

        lives = d.lives;
        this.lives = d.lives;

        startTimeMs = System.currentTimeMillis();
        elapsedMs = 0;

        // Iniciar/reiniciar hilo de temporizador
        startTimerThread(d.timeLimit);
    }

    private void startTimerThread(int timeLimitSecs) {
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.stopTimer();
        }

        timerThread = new GameLogicThread("LevelTimer-" + currentLevel);
        displayTime = timeLimitSecs;

        timerThread.setCallback(new GameLogicThread.GameTimerCallback() {
            @Override
            public void onTimeUpdate(int remaining) {
                displayTime = remaining;
            }

            @Override
            public void onTimeWarning() {
                timeWarning = true;
            }

            @Override
            public void onTimeOut() {
                if (state == GameState.PLAYING) {
                    timeoutLosePending = true;
                }
            }
        });

        if (timeLimitSecs > 0) {
            timerThread.startTimer(timeLimitSecs);
        }
    }

    // Guarda la velocidad de la punta en el frame anterior para transferirla al cortar
    private float lastTipVx = 0;
    private float lastTipVy = 0;

    @Override
    public void update(float delta) {
        if (state != GameState.PLAYING) {
            return;
        }

        if (timeoutLosePending) {
            state = GameState.LOSE;

            if (timerThread != null) {
                timerThread.stopTimer();
            }

            game.audioManager.playLose();
            recordResult(false);
            return;
        }

        // Contar cuerdas activas antes
        int activeRopesBefore = 0;
        for (Rope r : ropes) {
            if (!r.isCut()) {
                activeRopesBefore++;
            }
        }

        // Guardar la velocidad de la punta antes de actualizar
        if (activeRopesBefore > 0) {
            float tvx = 0;
            float tvy = 0;
            int count = 0;

            for (Rope r : ropes) {
                if (!r.isCut()) {
                    com.badlogic.gdx.math.Vector2 tv = r.getTipVelocity(delta);
                    tvx += tv.x;
                    tvy += tv.y;
                    count++;
                }
            }

            if (count > 0) {
                lastTipVx = tvx / count;
                lastTipVy = tvy / count;
            }
        }

        // Actualizar cuerdas
        boolean allCut = true;

        for (Rope r : ropes) {
            r.update(delta);

            if (!r.isCut()) {
                allCut = false;
            }
        }

        // Contar cuerdas activas después
        int activeRopesAfter = 0;
        for (Rope r : ropes) {
            if (!r.isCut()) {
                activeRopesAfter++;
            }
        }

        // Si se acaba de cortar la última cuerda, transferir velocidad al dulce
        if (activeRopesAfter == 0 && activeRopesBefore > 0) {
            candy.releaseWithVelocity(lastTipVx, lastTipVy);
        }

        // Con múltiples cuerdas activas se conserva el comportamiento original
        if (activeRopesAfter > 1) {
            float px = 0;
            float py = 0;

            for (Rope r : ropes) {
                if (!r.isCut()) {
                    px += candy.position.x;
                    py += candy.position.y;
                }
            }
        }

        // Si todas las cuerdas están cortadas, el caramelo cae libremente
        if (allCut) {
            candy.update(delta);
        }

        // Actualizar estrellas
        for (Star s : stars) {
            s.update(delta);

            if (!s.collected && s.checkCollision(candy)) {
                s.collected = true;
                starsCollected++;
                game.audioManager.playStar();
                addScore(500);
            }
        }

        // Actualizar Om Nom
        omNom.update(delta);

        // Verificar victoria
        if (checkWinCondition()) {
            levelComplete = true;
            state = GameState.WIN;

            omNom.eat();

            game.audioManager.playEat();
            game.audioManager.playWin();

            if (timerThread != null) {
                timerThread.stopTimer();
            }

            recordResult(true);
        }

        // Verificar derrota
        if (checkLoseCondition()) {
            state = GameState.LOSE;

            game.audioManager.playLose();

            if (timerThread != null) {
                timerThread.stopTimer();
            }

            recordResult(false);
        }

        // Contador de tiempo cuando no hay límite
        if (currentLevelData.timeLimit == 0) {
            displayTime = (int) (getElapsedTime() / 1000);
        }
    }

    @Override
    public boolean checkWinCondition() {
        return omNom.isCanyCaught(candy);
    }

    @Override
    public boolean checkLoseCondition() {
        return candy.isOutOfBounds(800, 700) || candy.fallen;
    }

    @Override
    public void render(SpriteBatch batch) {
        // No usado. Se usa render(float).
    }

    @Override
    public void disposeLevel() {
        if (ropes != null) {
            ropes.clear();
        }

        if (stars != null) {
            stars.clear();
        }

        if (timerThread != null) {
            timerThread.stopTimer();
        }
    }

    // ── Screen: render ────────────────────────────────────────────────────────

    @Override
    public void render(float delta) {
        if (!isPaused()) {
            update(delta);
        }

        // Aplicar viewport virtual
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);
        sr.setProjectionMatrix(game.camera.combined);

        LevelData d = currentLevelData;

        Gdx.gl.glClearColor(d.bgColor1.r, d.bgColor1.g, d.bgColor1.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Fondo mejorado sin cambiar la lógica
        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(d.bgColor1);
        sr.rect(0, 350f, 800, 350f);

        sr.setColor(d.bgColor2);
        sr.rect(0, 0, 800, 350f);

        // Decoraciones suaves
        sr.setColor(new Color(1f, 1f, 1f, 0.08f));
        sr.circle(90, 590, 58);
        sr.circle(720, 555, 74);

        sr.setColor(new Color(0f, 0f, 0f, 0.10f));
        sr.rect(0, 0, 800, 82);

        sr.setColor(new Color(1f, 1f, 1f, 0.08f));
        sr.rect(0, 80, 800, 3);

        sr.end();

        // Entidades
        sr.begin(ShapeRenderer.ShapeType.Filled);

        omNom.draw(sr);

        for (Star s : stars) {
            s.draw(sr);
        }

        for (Rope r : ropes) {
            r.draw(sr);
        }

        candy.draw(sr);

        // Línea de corte del dedo
        if (cutting) {
            sr.setColor(new Color(1f, 1f, 1f, 0.6f));
            sr.rectLine(cutStartX, cutStartY, cutCurrentX, cutCurrentY, 2f);
        }

        sr.end();

        drawHUD();

        if (state == GameState.WIN) {
            drawWinOverlay();
        }

        if (state == GameState.LOSE) {
            drawLoseOverlay();
        }

        if (state == GameState.PAUSED) {
            drawPauseOverlay();
        }

        handleTouchInput();
        handleButtonInput();
    }

    // ── HUD ───────────────────────────────────────────────────────────────────

    private void drawHUD() {
        // Barra HUD superior
        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(0f, 0f, 0f, 0.45f));
        sr.rect(0, 648, 800, 50);

        sr.setColor(new Color(1f, 1f, 1f, 0.08f));
        sr.rect(0, 648, 800, 2);

        sr.end();

        UserData ud = um.getCurrentUser();

        game.batch.begin();

        // Nombre del nivel
        game.font.setColor(Color.WHITE);
        game.font.draw(
            game.batch,
            MainGame.t("Nivel") + " " + (currentLevel + 1) + " – " + MainGame.t(currentLevelData.title),
            10,
            690
        );

        // Vidas
        game.font.setColor(new Color(1f, 0.4f, 0.4f, 1f));
        game.font.draw(game.batch, buildLivesStr(lives), 155, 690);

        // Estrellas
        game.font.setColor(new Color(1f, 0.85f, 0f, 1f));
        game.font.draw(
            game.batch,
            MainGame.t("Est:") + " " + starsCollected + "/" + stars.size(),
            290,
            690
        );

        // Tiempo
        boolean noLimit = currentLevelData.timeLimit == 0;
        game.font.setColor(timeWarning && !noLimit ? new Color(1f, 0.3f, 0.3f, 1f) : Color.WHITE);

        String timeLabel = MainGame.t("Tiempo:") + " " + displayTime + "s";
        game.font.draw(game.batch, timeLabel, 430, 690);

        // Puntuación
        game.font.setColor(new Color(0.8f, 0.8f, 1f, 1f));
        game.font.draw(game.batch, MainGame.t("Pts:") + " " + score, 570, 690);

        game.batch.end();

        // Botones HUD
        drawHudBtn(btnPause, isPaused() ? MainGame.t("Play") : "II");
        drawHudBtn(btnMenu, MainGame.t("Menú"));
    }

    private void drawHudBtn(Rectangle r, String label) {
        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(0f, 0f, 0f, 0.50f));
        sr.rect(r.x, r.y, r.width, r.height);

        sr.setColor(new Color(1f, 1f, 1f, 0.10f));
        sr.rect(r.x, r.y + r.height - 4, r.width, 4);

        sr.end();

        game.batch.begin();

        game.fontSmall.setColor(Color.WHITE);
        game.fontSmall.draw(game.batch, label, r.x + 6, r.y + r.height - 6);

        game.batch.end();
    }

    // ── Overlays ─────────────────────────────────────────────────────────────

    private void drawWinOverlay() {
        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(0f, 0f, 0f, 0.65f));
        sr.rect(0, 0, 800, 700);

        sr.setColor(new Color(1f, 0.85f, 0.2f, 0.12f));
        sr.circle(400, 385, 150);

        sr.end();

        game.batch.begin();

        game.fontLarge.setColor(new Color(1f, 0.85f, 0.2f, 1f));
        game.fontLarge.draw(game.batch, MainGame.t("¡NIVEL COMPLETO!"), 160, 420);

        int earned = computeStars();

        game.batch.end();

        drawWinStars(earned);

        game.batch.begin();

        game.font.setColor(Color.WHITE);

        long elapsed = System.currentTimeMillis() - levelStartMs;

        game.font.draw(
            game.batch,
            MainGame.t("Recogidas:") + " " + starsCollected + "/" + stars.size()
                + "   " + MainGame.t("Tiempo:") + " " + elapsed / 1000 + "s"
                + "   " + MainGame.t("Puntos:") + " " + score,
            80,
            330
        );

        game.batch.end();

        if (currentLevel < 4) {
            drawOverlayBtn(btnNext, MainGame.t("Siguiente nivel"), new Color(0.3f, 0.65f, 0.3f, 1f));
        }

        drawOverlayBtn(btnRestart, MainGame.t("Reintentar"), new Color(0.5f, 0.4f, 0.2f, 1f));
        drawOverlayBtn(btnBack, MainGame.t("Menú principal"), new Color(0.5f, 0.2f, 0.2f, 1f));

        if (currentLevel == 4) {
            game.batch.begin();

            game.fontLarge.setColor(new Color(0.9f, 0.6f, 1f, 1f));
            game.fontLarge.draw(game.batch, MainGame.t("¡¡JUEGO COMPLETADO!!"), 110, 460);

            game.batch.end();
        }
    }

    private void drawLoseOverlay() {
        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(0f, 0f, 0f, 0.65f));
        sr.rect(0, 0, 800, 700);

        sr.setColor(new Color(1f, 0.1f, 0.1f, 0.10f));
        sr.circle(400, 385, 150);

        sr.end();

        game.batch.begin();

        game.fontLarge.setColor(new Color(1f, 0.3f, 0.3f, 1f));

        String msg = lives <= 0 ? MainGame.t("SIN VIDAS") : MainGame.t("¡FALLASTE!");
        game.fontLarge.draw(game.batch, msg, 250, 420);

        game.font.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
        game.font.draw(game.batch, MainGame.t(currentLevelData.hint), 120, 370);

        game.batch.end();

        drawOverlayBtn(btnRestart, MainGame.t("Reintentar"), new Color(0.3f, 0.5f, 0.7f, 1f));
        drawOverlayBtn(btnBack, MainGame.t("Menú principal"), new Color(0.5f, 0.2f, 0.2f, 1f));
    }

    private void drawPauseOverlay() {
        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(0f, 0f, 0f, 0.55f));
        sr.rect(0, 0, 800, 700);

        sr.end();

        game.batch.begin();

        game.fontLarge.setColor(Color.WHITE);
        game.fontLarge.draw(game.batch, MainGame.t("PAUSA"), 310, 400);

        game.fontSmall.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
        game.fontSmall.draw(game.batch, MainGame.t("Toca ► para continuar"), 250, 360);

        game.batch.end();

        drawOverlayBtn(btnBack, MainGame.t("Menú principal"), new Color(0.5f, 0.2f, 0.2f, 1f));
    }

    private void drawOverlayBtn(Rectangle r, String text, Color col) {
        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(0f, 0f, 0f, 0.22f));
        sr.rect(r.x + 3, r.y - 3, r.width, r.height);

        sr.setColor(col);
        sr.rect(r.x, r.y, r.width, r.height);

        sr.setColor(new Color(1f, 1f, 1f, 0.10f));
        sr.rect(r.x, r.y + r.height - 5, r.width, 5);

        sr.end();

        game.batch.begin();

        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, text, r.x + 10, r.y + r.height - 12);

        game.batch.end();
    }

    // ── Input ──────────────────────────────────────────────────────────────────

    private void handleTouchInput() {
        if (state != GameState.PLAYING) {
            return;
        }

        com.badlogic.gdx.math.Vector3 _touch =
            new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);

        game.viewport.unproject(_touch);

        float mx = _touch.x;
        float my = _touch.y;

        if (Gdx.input.isTouched()) {
            if (!cutting) {
                cutting = true;
                cutStartX = mx;
                cutStartY = my;
            }

            cutCurrentX = mx;
            cutCurrentY = my;

            // Intentar cortar cuerdas con el trayecto actual
            for (Rope r : ropes) {
                if (r.trycut(cutStartX, cutStartY, cutCurrentX, cutCurrentY)) {
                    game.audioManager.playCut();
                    addScore(100);

                    cutStartX = mx;
                    cutStartY = my;
                }
            }
        } else {
            cutting = false;
        }
    }

    private void handleButtonInput() {
        if (!Gdx.input.justTouched()) {
            return;
        }

        com.badlogic.gdx.math.Vector3 _touch =
            new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);

        game.viewport.unproject(_touch);

        float mx = _touch.x;
        float my = _touch.y;

        if (btnPause.contains(mx, my)) {
            game.audioManager.playClick();

            paused = !paused;
            state = paused ? GameState.PAUSED : GameState.PLAYING;

            if (timerThread != null) {
                if (paused) {
                    timerThread.pauseTimer();
                } else {
                    timerThread.resumeTimer();
                }
            }

            return;
        }

        if (btnMenu.contains(mx, my)) {
            game.audioManager.playClick();
            goToMenu();
            return;
        }

        if (state == GameState.WIN) {
            if (btnNext.contains(mx, my) && currentLevel < 4) {
                game.audioManager.playClick();
                game.setScreen(new GameScreen(game, currentLevel + 1));
                dispose();
            }

            if (btnRestart.contains(mx, my)) {
                game.audioManager.playClick();
                restartCurrentLevel();
            }

            if (btnBack.contains(mx, my)) {
                game.audioManager.playClick();
                goToMenu();
            }
        }

        if (state == GameState.LOSE) {
            if (btnRestart.contains(mx, my)) {
                game.audioManager.playClick();
                restartCurrentLevel();
            }

            if (btnBack.contains(mx, my)) {
                game.audioManager.playClick();
                goToMenu();
            }
        }

        if (state == GameState.PAUSED) {
            if (btnBack.contains(mx, my)) {
                game.audioManager.playClick();
                goToMenu();
            }
        }
    }

    private void restartCurrentLevel() {
        game.setScreen(new GameScreen(game, currentLevel));
        dispose();
    }

    private void goToMenu() {
        game.audioManager.playMenuMusic();
        game.setScreen(new MainMenuScreen(game));
        dispose();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String buildLivesStr(int currentLives) {
        int safeLives = Math.max(0, currentLives);

        StringBuilder sb = new StringBuilder(MainGame.t("Vidas:") + " ");

        if (safeLives == 0) {
            sb.append("0");
        } else {
            for (int i = 0; i < safeLives; i++) {
                sb.append("♥ ");
            }
        }

        return sb.toString();
    }

    private void drawWinStars(int earned) {
        int safeEarned = Math.max(0, Math.min(3, earned));

        float startX = 335f;
        float y = 375f;
        float gap = 55f;

        sr.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < 3; i++) {
            if (i < safeEarned) {
                sr.setColor(new Color(1f, 0.85f, 0.05f, 1f));
            } else {
                sr.setColor(new Color(0.25f, 0.25f, 0.25f, 1f));
            }

            drawStarShape(startX + i * gap, y, 22f, 10f);
        }

        sr.end();
    }

    private void drawStarShape(float cx, float cy, float outerRadius, float innerRadius) {
        float[] xs = new float[10];
        float[] ys = new float[10];

        for (int i = 0; i < 10; i++) {
            double angle = Math.toRadians(-90 + i * 36);
            float radius = (i % 2 == 0) ? outerRadius : innerRadius;

            xs[i] = cx + (float) Math.cos(angle) * radius;
            ys[i] = cy + (float) Math.sin(angle) * radius;
        }

        for (int i = 0; i < 10; i++) {
            int next = (i + 1) % 10;
            sr.triangle(cx, cy, xs[i], ys[i], xs[next], ys[next]);
        }
    }

    /**
     * Calcula estrellas 1-3 según rendimiento.
     */
    private int computeStars() {
        if (starsCollected == stars.size()) {
            long elapsed = System.currentTimeMillis() - levelStartMs;
            int limit = currentLevelData.timeLimit;

            if (limit == 0 || elapsed < (long) limit * 500) {
                return 3;
            }

            if (elapsed < (long) limit * 800) {
                return 2;
            }

            return 1;
        }

        if (starsCollected > 0) {
            return 2;
        }

        return 1;
    }

    private void recordResult(boolean won) {
        UserData ud = um.getCurrentUser();

        if (ud == null) {
            return;
        }

        long elapsed = System.currentTimeMillis() - levelStartMs;
        int earnedStars = won ? computeStars() : 0;

        StatsSaveThread.getInstance().enqueueRecord(ud, currentLevel, earnedStars, elapsed, won);
    }

    // ── Screen lifecycle ──────────────────────────────────────────────────────

    @Override
    public void show() {
        sr = new ShapeRenderer();

        game.applyRuntimePreferences(um.getCurrentUser());
        game.audioManager.playGameMusic();

        initLevel(currentLevel);
    }

    @Override
    public void resize(int w, int h) {
        game.viewport.update(w, h, true);
    }

    @Override
    public void pause() {
        paused = true;

        if (timerThread != null) {
            timerThread.pauseTimer();
        }
    }

    @Override
    public void resume() {
        paused = false;

        if (timerThread != null) {
            timerThread.resumeTimer();
        }
    }

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        disposeLevel();

        if (sr != null) {
            sr.dispose();
        }
    }
}
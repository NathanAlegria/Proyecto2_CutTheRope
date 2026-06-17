

package com.cuttherope.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
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


public class GameScreen extends Juego implements Screen {

    private final MainGame game;
    private final UserManager um;
    private final LevelData[] allLevels;
    private LevelData currentLevelData;
    private ShapeRenderer sr;
    private Texture fondo;


    private Candy candy;
    private OmNom omNom;
    private List<Rope> ropes;
    private List<Star> stars;
    private List<Spike> spikes;


    private boolean bubbleEnabled = false;
    private float bubbleX = 0f;
    private float bubbleY = 0f;
    private float bubbleRadius = 0f;
    private float bubbleLift = 920f;


    private boolean cutting = false;
    private float cutStartX, cutStartY;
    private float cutCurrentX, cutCurrentY;


    private enum GameState {
        PLAYING, WIN, LOSE, PAUSED
    }

    private GameState state = GameState.PLAYING;
    private float resultTimer = 0f;


    private int starsCollected = 0;
    private int displayTime = 0;
    private boolean timeWarning = false;
    private boolean timeoutLosePending = false;
    private long levelStartMs;


    private GameLogicThread timerThread;


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

    private boolean isVersusMode() {
        return VersusModeContext.isActive();
    }


    @Override
    public void initLevel(int level) {
        currentLevelData = allLevels[level];
        LevelData d = currentLevelData;


        candy = new Candy(d.candyX, d.candyY);
        omNom = new OmNom(d.omNomX, d.omNomY);


        UserData udInit = um.getCurrentUser();
        if (udInit != null) {
            omNom.setAvatarColor(udInit.getAvatarId());
        }

        ropes = new ArrayList<>();
        stars = new ArrayList<>();


        for (int i = 0; i < d.anchorX.length; i++) {
            ropes.add(new Rope(d.anchorX[i], d.anchorY[i], candy, d.ropeColor));
        }


        for (int i = 0; i < d.starX.length; i++) {
            stars.add(new Star(d.starX[i], d.starY[i]));
        }


        spikes = new ArrayList<>();
        for (int i = 0; i < d.spikeX.length; i++) {
            int dir = (d.spikeDir != null && i < d.spikeDir.length) ? d.spikeDir[i] : 0;
            spikes.add(new Spike(d.spikeX[i], d.spikeY[i], dir));
        }


        bubbleEnabled = false;
        bubbleX = 390f;
        bubbleY = 95f;
        bubbleRadius = 52f;
        bubbleLift = 920f;


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

        float dt = Math.min(delta, 0.033f);

        int activeRopes = 0;
        for (Rope r : ropes) {
            if (!r.isCut()) {
                activeRopes++;
            }
        }


        candy.update(dt);


        applyBubblePhysics(dt);


        if (activeRopes > 0) {
            for (int i = 0; i < 4; i++) {
                for (Rope r : ropes) {
                    if (!r.isCut()) {
                        r.update(dt);
                    }
                }
            }
        }


        for (Star s : stars) {
            s.update(delta);

            if (!s.collected && s.checkCollision(candy)) {
                s.collected = true;
                starsCollected++;
                game.audioManager.playStar();
                addScore(500);
            }
        }


        for (Spike sp : spikes) {
            if (sp.checkCollision(candy)) {
                candy.fallen = true;
                break;
            }
        }


        omNom.update(delta);


        if (checkWinCondition()) {
            levelComplete = true;
            state = GameState.WIN;

            candy.collected = true;
            omNom.eat();

            game.audioManager.playEat();
            game.audioManager.playWin();

            if (timerThread != null) {
                timerThread.stopTimer();
            }

            recordResult(true);
        }


        if (checkLoseCondition()) {
            state = GameState.LOSE;

            game.audioManager.playLose();

            if (timerThread != null) {
                timerThread.stopTimer();
            }

            recordResult(false);
        }


        if (currentLevelData.timeLimit == 0) {
            displayTime = (int) (getElapsedTime() / 1000);
        }
    }

    @Override
    public boolean checkWinCondition() {
        return omNom.isCandyCaught(candy);
    }

    @Override
    public boolean checkLoseCondition() {
        return candy.isOutOfBounds(800, 700) || candy.fallen;
    }

    @Override
    public void render(SpriteBatch batch) {

    }

    @Override
    public void disposeLevel() {
        if (ropes != null) {
            ropes.clear();
        }

        if (stars != null) {
            stars.clear();
        }

        if (spikes != null) {
            spikes.clear();
        }

        if (timerThread != null) {
            timerThread.stopTimer();
        }
    }


    @Override
    public void render(float delta) {
        if (!isPaused()) {
            update(delta);
        }


        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);
        sr.setProjectionMatrix(game.camera.combined);

        LevelData d = currentLevelData;

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        if (fondo != null) {
            game.batch.draw(fondo, 0, 0, 800, 700);
        }
        game.batch.end();


        sr.begin(ShapeRenderer.ShapeType.Filled);

        drawBubble();

        for (Star s : stars) {
            s.draw(sr);
        }

        for (Spike sp : spikes) {
            sp.draw(sr);
        }

        for (Rope r : ropes) {
            r.draw(sr);
        }


        if (cutting) {
            sr.setColor(new Color(1f, 1f, 1f, 0.6f));
            sr.rectLine(cutStartX, cutStartY, cutCurrentX, cutCurrentY, 2f);
        }

        sr.end();

        game.batch.begin();
        omNom.draw(game.batch);
        candy.draw(game.batch);
        game.batch.end();

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


    private void drawHUD() {

        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(0f, 0f, 0f, 0.45f));
        sr.rect(0, 648, 800, 50);

        sr.setColor(new Color(1f, 1f, 1f, 0.08f));
        sr.rect(0, 648, 800, 2);

        sr.end();

        UserData ud = um.getCurrentUser();

        game.batch.begin();


        game.font.setColor(Color.WHITE);
        game.font.draw(
            game.batch,
            (isVersusMode() ? "VS - " : "") + MainGame.t("Nivel") + " " + (currentLevel + 1) + " – " + MainGame.t(currentLevelData.title),
            10,
            690
        );


        game.font.setColor(new Color(1f, 0.4f, 0.4f, 1f));
        game.font.draw(game.batch, buildLivesStr(lives), 155, 690);


        game.font.setColor(new Color(1f, 0.85f, 0f, 1f));
        game.font.draw(
            game.batch,
            MainGame.t("Est:") + " " + starsCollected + "/" + stars.size(),
            290,
            690
        );


        boolean noLimit = currentLevelData.timeLimit == 0;
        game.font.setColor(timeWarning && !noLimit ? new Color(1f, 0.3f, 0.3f, 1f) : Color.WHITE);

        String timeLabel = MainGame.t("Tiempo:") + " " + displayTime + "s";
        game.font.draw(game.batch, timeLabel, 430, 690);


        game.font.setColor(new Color(0.8f, 0.8f, 1f, 1f));
        game.font.draw(game.batch, MainGame.t("Pts:") + " " + score, 570, 690);

        game.batch.end();


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
            MainGame.t("Recogidas:") + " " + starsCollected + "/" + stars.size() + "  (opcionales)"
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
            game.fontLarge.draw(game.batch, isVersusMode() ? "VS COMPLETADO" : MainGame.t("¡¡JUEGO COMPLETADO!!"), 160, 460);
            if (isVersusMode()) {
                game.font.setColor(Color.WHITE);
                game.font.draw(game.batch, "El resultado final aparece en Estadísticas cuando ambos terminen.", 125, 305);
            }

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
        if (isVersusMode()) VersusModeContext.clear();
        game.audioManager.playMenuMusic();
        game.setScreen(new MainMenuScreen(game));
        dispose();
    }


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

    private void applyBubblePhysics(float dt) {
        if (!bubbleEnabled || candy == null || candy.collected || candy.fallen) {
            return;
        }

        float dx = candy.position.x - bubbleX;
        float dy = candy.position.y - bubbleY;
        float insideRadius = bubbleRadius + candy.radius * 0.35f;

        if (dx * dx + dy * dy <= insideRadius * insideRadius) {

            candy.velocity.y += bubbleLift * dt;


            candy.velocity.x += (bubbleX - candy.position.x) * 1.35f * dt;


            if (candy.velocity.y > 380f) {
                candy.velocity.y = 380f;
            }
        }
    }

    private void drawBubble() {
        if (!bubbleEnabled) {
            return;
        }

        sr.setColor(new Color(0.75f, 0.92f, 1f, 0.18f));
        sr.circle(bubbleX, bubbleY, bubbleRadius);
        sr.setColor(new Color(0.95f, 1f, 1f, 0.32f));
        sr.circle(bubbleX, bubbleY, bubbleRadius - 5f);
        sr.setColor(new Color(1f, 1f, 1f, 0.42f));
        sr.circle(bubbleX - bubbleRadius * 0.22f, bubbleY + bubbleRadius * 0.25f, bubbleRadius * 0.16f);
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
        if (isVersusMode()) {
            um.recordVersusLevel(VersusModeContext.getMatchId(), ud.getUsername(), currentLevel, earnedStars, elapsed);
        }
    }


    @Override
    public void show() {
        sr = new ShapeRenderer();
        fondo = AssetPaths.textureOrNull(AssetPaths.FONDO);

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
        if (fondo != null) {
            fondo.dispose();
        }
    }
}

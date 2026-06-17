

package com.cuttherope.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.cuttherope.game.AudioManager;


public class SplashScreen implements Screen {

    private final MainGame game;
    private ShapeRenderer sr;

    private float timer = 0f;
    private float duration = 2.5f;


    private float loadAngle = 0f;
    private int dotsCount = 0;
    private float dotTimer = 0f;

    public SplashScreen(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        sr = new ShapeRenderer();


        AudioManager.getInstance().preloadAsync();


        if (game.audioManager != null) {
            game.audioManager.playMenuMusic();
        }
    }

    @Override
    public void render(float delta) {
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);
        sr.setProjectionMatrix(game.camera.combined);

        timer += delta;
        loadAngle = (loadAngle + delta * 200f) % 360f;

        dotTimer += delta;
        if (dotTimer > 0.4f) {
            dotTimer = 0f;
            dotsCount = (dotsCount + 1) % 4;
        }

        Gdx.gl.glClearColor(0.07f, 0.05f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        drawBackground();
        drawLogo();
        drawLoading();

        if (timer >= duration) {
            game.setScreen(new LoginScreen(game));
            dispose();
        }
    }

    private void drawBackground() {
        sr.begin(ShapeRenderer.ShapeType.Filled);


        sr.setColor(new Color(0.07f, 0.05f, 0.12f, 1f));
        sr.rect(0, 0, 800, 700);


        sr.setColor(new Color(0.13f, 0.08f, 0.24f, 1f));
        sr.rect(0, 350, 800, 350);

        sr.setColor(new Color(0.08f, 0.06f, 0.16f, 1f));
        sr.rect(0, 0, 800, 350);


        sr.setColor(new Color(0.5f, 0.25f, 0.9f, 0.16f));
        sr.circle(710, 610, 95);

        sr.setColor(new Color(0.2f, 0.75f, 0.2f, 0.14f));
        sr.circle(95, 95, 70);

        sr.setColor(new Color(1f, 0.85f, 0.2f, 0.09f));
        sr.circle(700, 105, 55);

        sr.setColor(new Color(1f, 1f, 1f, 0.04f));
        sr.circle(400, 390, 150);

        sr.end();
    }

    private void drawLogo() {
        float cx = 400f;
        float cy = 390f;

        sr.begin(ShapeRenderer.ShapeType.Filled);


        sr.setColor(new Color(0f, 0f, 0f, 0.35f));
        sr.circle(cx + 6, cy - 8, 74f);


        sr.setColor(new Color(0.2f, 0.75f, 0.2f, 1f));
        sr.circle(cx, cy, 70f);


        sr.setColor(new Color(0.55f, 0.9f, 0.4f, 1f));
        sr.circle(cx, cy - 15, 42f);


        sr.setColor(Color.WHITE);
        sr.circle(cx - 22, cy + 22, 18f);
        sr.circle(cx + 22, cy + 22, 18f);

        sr.setColor(Color.BLACK);
        sr.circle(cx - 18, cy + 20, 7f);
        sr.circle(cx + 18, cy + 20, 7f);


        sr.setColor(Color.WHITE);
        sr.circle(cx - 15, cy + 24, 3f);
        sr.circle(cx + 21, cy + 24, 3f);


        sr.setColor(Color.BLACK);
        sr.ellipse(cx - 30, cy - 22, 60, 24);


        sr.setColor(Color.WHITE);
        sr.rect(cx - 18, cy - 15, 12, 8);
        sr.rect(cx + 6, cy - 15, 12, 8);


        sr.setColor(new Color(0.2f, 0.75f, 0.2f, 1f));
        sr.rectLine(cx, cy + 65, cx - 12, cy + 95, 7f);
        sr.circle(cx - 14, cy + 98, 10f);

        sr.end();


        drawCandy(cx + 115, cy + 25, 22f);


        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0.6f, 0.35f, 0.1f, 1f));
        sr.rectLine(cx + 115, cy + 120, cx + 115, cy + 50, 4f);
        sr.end();


        game.batch.begin();

        game.fontLarge.setColor(new Color(0.95f, 0.85f, 1f, 1f));
        game.fontLarge.draw(game.batch, "Cut the Rope", 255, 285);

        game.fontSmall.setColor(new Color(0.75f, 0.70f, 0.88f, 1f));
        game.fontSmall.draw(game.batch, "Version " + MainGame.VERSION, 360, 255);

        game.batch.end();
    }

    private void drawCandy(float cx, float cy, float radius) {
        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(0.35f, 0f, 0f, 0.35f));
        sr.circle(cx + 3, cy - 3, radius);

        sr.setColor(new Color(0.9f, 0.15f, 0.15f, 1f));
        sr.circle(cx, cy, radius);

        sr.setColor(new Color(1f, 0.55f, 0.55f, 1f));
        sr.circle(cx - 7, cy + 7, radius * 0.35f);

        sr.setColor(new Color(0.65f, 0.05f, 0.05f, 1f));
        sr.circle(cx + 6, cy - 6, radius * 0.22f);

        sr.end();
    }

    private void drawLoading() {
        float cx = 400f;
        float cy = 170f;


        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < dotsCount; i++) {
            dots.append(".");
        }

        game.batch.begin();

        game.font.setColor(new Color(0.88f, 0.82f, 1f, 1f));
        game.font.draw(game.batch, MainGame.t("Cargando") + dots, 340, 205);

        game.batch.end();


        float progress = Math.min(1f, timer / duration);
        float barX = 260f;
        float barY = 150f;
        float barW = 280f;
        float barH = 14f;

        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(0.20f, 0.16f, 0.32f, 1f));
        sr.rect(barX, barY, barW, barH);

        sr.setColor(new Color(0.55f, 0.32f, 1f, 1f));
        sr.rect(barX, barY, barW * progress, barH);

        sr.setColor(new Color(1f, 1f, 1f, 0.14f));
        sr.rect(barX, barY + 9, barW * progress, 4);

        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);

        sr.setColor(new Color(0.75f, 0.55f, 1f, 1f));
        sr.rect(barX, barY, barW, barH);

        sr.end();


        drawSpinner(cx, cy - 45, 22f);
    }

    private void drawSpinner(float cx, float cy, float radius) {
        sr.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < 8; i++) {
            float alpha = (i + 1) / 8f;
            double angle = Math.toRadians(loadAngle + i * 45);

            float x = cx + (float) Math.cos(angle) * radius;
            float y = cy + (float) Math.sin(angle) * radius;

            sr.setColor(new Color(0.8f, 0.65f, 1f, alpha));
            sr.circle(x, y, 4f);
        }

        sr.end();
    }

    @Override
    public void resize(int w, int h) {
        game.viewport.update(w, h, true);
    }

    @Override
    public void pause() {
        if (game.audioManager != null) {
            game.audioManager.pauseMusic();
        }
    }

    @Override
    public void resume() {
        if (game.audioManager != null) {
            game.audioManager.resumeMusic();
        }
    }

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (sr != null) {
            sr.dispose();
        }
    }
}

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
    private final Rectangle[] levelBtns = new Rectangle[5];
    private final Rectangle btnStats = new Rectangle(42, 28, 150, 42);
    private final Rectangle btnSettings = new Rectangle(220, 28, 150, 42);
    private final Rectangle btnRanking = new Rectangle(398, 28, 150, 42);
    private final Rectangle btnLogout = new Rectangle(576, 28, 150, 42);
    private ShapeRenderer sr;
    private Texture fondo;

    public MainMenuScreen(MainGame game) {
        this.game = game;
        this.um = UserManager.getInstance();
        for (int i = 0; i < 5; i++) levelBtns[i] = new Rectangle(92 + i * 135, 245, 92, 92);
    }

    @Override public void show() {
        sr = new ShapeRenderer();
        fondo = AssetPaths.texture(AssetPaths.FONDO);
        UserData ud = um.getCurrentUser();
        if (ud != null) game.applyRuntimePreferences(ud);
        if (game.audioManager != null) game.audioManager.playMenuMusic();
    }

    @Override public void render(float delta) {
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);
        sr.setProjectionMatrix(game.camera.combined);
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin(); game.batch.draw(fondo, 0, 0, 800, 700); game.batch.end();
        drawPanel(); drawLevels(); drawBottom(); handle();
    }

    private void drawPanel() {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0.28f,0.08f,0.55f,.78f)); sr.rect(118, 122, 564, 455);
        sr.setColor(new Color(1f,.85f,.05f,1)); sr.rect(118, 480, 564, 3);
        sr.end();
        game.batch.begin();
        game.fontLarge.getData().setScale(1.6f); game.fontLarge.setColor(new Color(1f,.78f,.08f,1));
        game.fontLarge.draw(game.batch, "Mapa de niveles", 205, 548);
        game.font.setColor(Color.WHITE);
        UserData u = um.getCurrentUser();
        String n = u == null ? "Jugador" : u.getFullName();
        game.font.draw(game.batch, "Bienvenido: " + n, 205, 513);
        game.font.draw(game.batch, "Selecciona un nivel", 318, 445);
        game.batch.end();
    }

    private void drawLevels() {
        UserData ud = um.getCurrentUser();
        for (int i=0;i<levelBtns.length;i++) {
            boolean unlocked = ud == null || (ud.getLevelUnlocked() != null && ud.getLevelUnlocked()[i]);
            Rectangle r = levelBtns[i];
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(unlocked ? new Color(.50f,.72f,.98f,1) : new Color(.25f,.25f,.30f,.9f)); sr.circle(r.x+r.width/2, r.y+r.height/2, 46);
            sr.setColor(new Color(0,0,0,.25f)); sr.circle(r.x+r.width/2+3, r.y+r.height/2-3, 46);
            sr.end();
            sr.begin(ShapeRenderer.ShapeType.Line); sr.setColor(new Color(1f,.85f,.05f,1)); sr.circle(r.x+r.width/2, r.y+r.height/2, 47); sr.end();
            game.batch.begin();
            game.fontLarge.getData().setScale(1.5f); game.fontLarge.setColor(Color.WHITE);
            game.fontLarge.draw(game.batch, unlocked ? String.valueOf(i+1) : "X", r.x+35, r.y+59);
            game.fontSmall.setColor(Color.WHITE); game.fontSmall.draw(game.batch, "Nivel " + (i+1), r.x+22, r.y-8);
            game.batch.end();
        }
    }

    private void drawBottom() { button(btnStats,"Estadísticas"); button(btnSettings,"Ajustes"); button(btnRanking,"Ranking"); button(btnLogout,"Salir"); }
    private void button(Rectangle r, String text) {
        sr.begin(ShapeRenderer.ShapeType.Filled); sr.setColor(new Color(.50f,.72f,.98f,1)); sr.rect(r.x,r.y,r.width,r.height); sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line); sr.setColor(Color.BLACK); sr.rect(r.x,r.y,r.width,r.height); sr.end();
        game.batch.begin(); game.font.setColor(Color.WHITE); game.font.draw(game.batch, text, r.x + 16, r.y + 27); game.batch.end();
    }

    private void handle() {
        if (!Gdx.input.justTouched()) return;
        float x = Gdx.input.getX() * 800f / Gdx.graphics.getWidth();
        float y = 700f - Gdx.input.getY() * 700f / Gdx.graphics.getHeight();
        if (game.audioManager != null) game.audioManager.playClick();
        UserData ud = um.getCurrentUser();
        for (int i=0;i<levelBtns.length;i++) if (levelBtns[i].contains(x,y) && (ud == null || (ud.getLevelUnlocked() != null && ud.getLevelUnlocked()[i]))) { game.setScreen(new GameScreen(game, i)); return; }
        if (btnStats.contains(x,y)) game.setScreen(new StatsScreen(game));
        else if (btnSettings.contains(x,y)) game.setScreen(new SettingsScreen(game));
        else if (btnRanking.contains(x,y)) game.setScreen(new RankingScreen(game));
        else if (btnLogout.contains(x,y)) { um.logout(); game.setScreen(new LoginScreen(game)); }
    }

    @Override public void resize(int width, int height) { game.viewport.update(width,height,true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { if(sr!=null) sr.dispose(); if(fondo!=null) fondo.dispose(); }
}

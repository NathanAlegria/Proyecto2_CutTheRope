package com.cuttherope.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class LoginScreen implements Screen {
    private final MainGame game;
    private final UserManager um;
    private ShapeRenderer sr;
    private Texture fondo;

    private enum Mode { LOGIN, REGISTER }
    private enum Focus { FULLNAME, USERNAME, PASSWORD, NONE }
    private Mode mode = Mode.LOGIN;
    private Focus focus = Focus.NONE;

    private String fullName = "";
    private String username = "";
    private String password = "";
    private boolean showPassword = false;
    private String error = "";
    private String ok = "";
    private float msgTimer = 0;

    private static final float PW = 420, PH_LOGIN = 460, PH_REG = 555, PX = 190;
    private static final float FIELD_X = 235, FIELD_W = 338, FIELD_H = 42, SHOW_W = 58;

    private final Rectangle logUser = new Rectangle(FIELD_X, 356, FIELD_W, FIELD_H);
    private final Rectangle logPass = new Rectangle(FIELD_X, 276, FIELD_W - SHOW_W - 6, FIELD_H);
    private final Rectangle logShow = new Rectangle(FIELD_X + FIELD_W - SHOW_W, 276, SHOW_W, FIELD_H);
    private final Rectangle btnLogin = new Rectangle(262, 170, 300, 48);
    private final Rectangle btnToRegister = new Rectangle(262, 106, 300, 48);

    private final Rectangle regFull = new Rectangle(FIELD_X, 430, FIELD_W, FIELD_H);
    private final Rectangle regUser = new Rectangle(FIELD_X, 355, FIELD_W, FIELD_H);
    private final Rectangle regPass = new Rectangle(FIELD_X, 280, FIELD_W - SHOW_W - 6, FIELD_H);
    private final Rectangle regShow = new Rectangle(FIELD_X + FIELD_W - SHOW_W, 280, SHOW_W, FIELD_H);
    private final Rectangle btnRegister = new Rectangle(262, 93, 300, 42);
    private final Rectangle btnToLogin = new Rectangle(262, 41, 300, 42);

    public LoginScreen(MainGame game) {
        this.game = game;
        this.um = UserManager.getInstance();
    }

    @Override public void show() {
        sr = new ShapeRenderer();
        fondo = AssetPaths.textureOrNull(AssetPaths.FONDO);
        if (game.audioManager != null) game.audioManager.playMenuMusic();
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean keyTyped(char c) {
                if (focus == Focus.NONE) return false;
                if (c == '\b') deleteLast();
                else if (c == '\r' || c == '\n') action();
                else if (c == '\t') nextFocus();
                else if (c >= 32 && c != 127) add(c);
                return true;
            }
            @Override public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.TAB) { nextFocus(); return true; }
                if (keycode == Input.Keys.ENTER) { action(); return true; }
                return false;
            }
        });
    }

    @Override public void render(float delta) {
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);
        sr.setProjectionMatrix(game.camera.combined);
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        msgTimer -= delta;
        if (msgTimer <= 0) { error = ""; ok = ""; }

        game.batch.begin();
        game.batch.draw(fondo, 0, 0, 800, 700);
        game.batch.end();

        if (mode == Mode.REGISTER) drawRegister(); else drawLogin();
        drawMessages();
        handleClick();
    }

    private void drawPanel(float y, float h, String title) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0.28f, 0.08f, 0.55f, 0.78f));
        sr.rect(PX, y, PW, h);
        sr.setColor(new Color(0.39f, 0.12f, 0.68f, 0.45f));
        sr.rect(PX + 14, y + 20, PW - 28, h - 95);
        sr.setColor(new Color(1f, 0.85f, 0.05f, 1f));
        sr.rect(PX, y + h - 90, PW, 3);
        sr.end();

        game.batch.begin();
        game.fontLarge.setColor(new Color(1f, .78f, .08f, 1));
        game.fontLarge.getData().setScale(1.35f);
        game.fontLarge.draw(game.batch, title, PX + 34, y + h - 31);
        game.font.setColor(new Color(1f, .82f, .1f, 1));
        game.font.draw(game.batch, "Cut The Rope", PX + 34, y + h - 58);
        game.fontLarge.getData().setScale(1f);
        game.batch.end();
    }

    private void drawLogin() {
        drawPanel(95, PH_LOGIN, "Iniciar sesión");
        label("Usuario:", 235, 414); input(logUser, username, focus == Focus.USERNAME, false);
        label("Contraseña:", 235, 334); input(logPass, password, focus == Focus.PASSWORD, !showPassword); button(logShow, showPassword ? "Ocultar" : "Ver");
        button(btnLogin, "Iniciar sesión"); button(btnToRegister, "Crear Cuenta");
    }

    private void drawRegister() {
        drawPanel(30, PH_REG, "Crear cuenta");
        label("Nombre completo:", 235, 486); input(regFull, fullName, focus == Focus.FULLNAME, false);
        label("Usuario:", 235, 411); input(regUser, username, focus == Focus.USERNAME, false);
        label("Contraseña:", 235, 336); input(regPass, password, focus == Focus.PASSWORD, !showPassword); button(regShow, showPassword ? "Ocultar" : "Ver");
        checklist();
        button(btnRegister, "Registrarse"); button(btnToLogin, "Iniciar sesión");
    }

    private void label(String text, float x, float y) {
        game.batch.begin();
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, text, x, y);
        game.batch.end();
    }

    private void input(Rectangle r, String txt, boolean focused, boolean hide) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0.28f, 0.08f, 0.60f, 0.95f)); sr.rect(r.x, r.y, r.width, r.height);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(focused ? new Color(1f, .85f, .05f, 1) : new Color(.68f, .35f, 1f, 1)); sr.rect(r.x, r.y, r.width, r.height);
        sr.end();
        game.batch.begin();
        game.font.setColor(Color.WHITE);
        String shown = hide ? txt.replaceAll(".", "*") : txt;
        game.font.draw(game.batch, shown, r.x + 10, r.y + 27);
        game.batch.end();
    }

    private void button(Rectangle r, String text) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(.50f, .72f, .98f, 1)); sr.rect(r.x, r.y, r.width, r.height);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(Color.BLACK); sr.rect(r.x, r.y, r.width, r.height);
        sr.end();
        game.batch.begin();
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, text, r.x + r.width / 2 - text.length() * 4.2f, r.y + r.height / 2 + 6);
        game.batch.end();
    }

    private void checklist() {
        boolean len = password.length() >= 8, up=false, low=false, num=false, esp=false;
        for(char c: password.toCharArray()) { if(Character.isUpperCase(c)) up=true; else if(Character.isLowerCase(c)) low=true; else if(Character.isDigit(c)) num=true; else esp=true; }
        check("Mínimo 8 Caracteres", len, 248, 242);
        check("Al menos 1 letra mayúscula", up, 248, 220);
        check("Al menos 1 letra minúscula", low, 248, 198);
        check("Al menos 1 número", num, 248, 176);
        check("Al menos 1 carácter especial", esp, 248, 154);
    }

    private void check(String text, boolean valid, float x, float y) {

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(Color.WHITE);
        sr.rect(x, y - 10, 12, 12);

        if (valid) {
            sr.setColor(new Color(0.35f, 1f, 0.35f, 1f));
            sr.line(x + 2, y - 4, x + 5, y - 8);
            sr.line(x + 5, y - 8, x + 11, y + 2);
        } else {
            sr.setColor(new Color(1f, 0.35f, 0.35f, 1f));
            sr.line(x + 2, y - 8, x + 10, y);
            sr.line(x + 10, y - 8, x + 2, y);
        }
        sr.end();

        game.batch.begin();
        game.fontSmall.setColor(Color.WHITE);
        game.fontSmall.draw(game.batch, text, x + 20, y + 2);
        game.batch.end();
    }

    private void drawMessages() {
        if (error.isEmpty() && ok.isEmpty()) return;
        game.batch.begin();
        game.font.setColor(error.isEmpty() ? new Color(.5f,1f,.5f,1) : new Color(1f,.45f,.45f,1));
        game.font.draw(game.batch, error.isEmpty() ? ok : error, 210, 45);
        game.batch.end();
    }

    private void handleClick() {
        if (!Gdx.input.justTouched()) return;
        com.badlogic.gdx.math.Vector3 touch = new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        game.viewport.unproject(touch);
        float x = touch.x;
        float y = touch.y;
        if (game.audioManager != null) game.audioManager.playClick();
        if (mode == Mode.LOGIN) {
            if (logUser.contains(x,y)) focus = Focus.USERNAME;
            else if (logPass.contains(x,y)) focus = Focus.PASSWORD;
            else if (logShow.contains(x,y)) showPassword = !showPassword;
            else if (btnLogin.contains(x,y)) doLogin();
            else if (btnToRegister.contains(x,y)) { mode = Mode.REGISTER; clearMsg(); focus = Focus.FULLNAME; }
            else focus = Focus.NONE;
        } else {
            if (regFull.contains(x,y)) focus = Focus.FULLNAME;
            else if (regUser.contains(x,y)) focus = Focus.USERNAME;
            else if (regPass.contains(x,y)) focus = Focus.PASSWORD;
            else if (regShow.contains(x,y)) showPassword = !showPassword;
            else if (btnRegister.contains(x,y)) doRegister();
            else if (btnToLogin.contains(x,y)) { mode = Mode.LOGIN; clearMsg(); focus = Focus.USERNAME; }
            else focus = Focus.NONE;
        }
    }

    private void add(char c) { if (focus == Focus.FULLNAME) fullName += c; else if (focus == Focus.USERNAME) username += c; else if (focus == Focus.PASSWORD) password += c; }
    private void deleteLast() { if (focus == Focus.FULLNAME && fullName.length()>0) fullName=fullName.substring(0,fullName.length()-1); else if (focus == Focus.USERNAME && username.length()>0) username=username.substring(0,username.length()-1); else if (focus == Focus.PASSWORD && password.length()>0) password=password.substring(0,password.length()-1); }
    private void nextFocus() { if (mode == Mode.REGISTER) focus = focus == Focus.FULLNAME ? Focus.USERNAME : focus == Focus.USERNAME ? Focus.PASSWORD : Focus.FULLNAME; else focus = focus == Focus.USERNAME ? Focus.PASSWORD : Focus.USERNAME; }
    private void action() { if (mode == Mode.LOGIN) doLogin(); else doRegister(); }
    private void doLogin() { String e = um.login(username, password); if (e == null) game.setScreen(new MainMenuScreen(game)); else fail(e); }
    private void doRegister() { String e = um.register(username, password, fullName); if (e == null) { ok("¡Cuenta creada! Inicia sesión."); mode = Mode.LOGIN; focus = Focus.USERNAME; } else fail(e); }
    private void fail(String m) { error = m; ok = ""; msgTimer = 3f; }
    private void ok(String m) { ok = m; error = ""; msgTimer = 3f; }
    private void clearMsg() { error=""; ok=""; msgTimer=0; }

    @Override public void resize(int width, int height) { game.viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { if (sr != null) sr.dispose(); if (fondo != null) fondo.dispose(); }
}

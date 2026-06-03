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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

/**
 * LoginScreen — ajustada para 800×700.
 * Panel centrado, campos grandes, fondo visual mejorado.
 * Registro con requisitos de contraseña marcados con cheque.
 */
public class LoginScreen implements Screen {

    private final MainGame    game;
    private final UserManager um;
    private ShapeRenderer     sr;

    private enum Mode {
        LOGIN, REGISTER
    }

    private Mode mode = Mode.LOGIN;

    private String fieldUsername  = "";
    private String fieldPassword  = "";
    private String fieldFullName  = "";
    private String fieldConfirmPw = "";

    private enum FocusField {
        USERNAME, PASSWORD, FULLNAME, CONFIRM_PW, NONE
    }

    private FocusField focus = FocusField.NONE;

    private boolean showPassword  = false;
    private boolean showConfirmPw = false;
    private String  errorMessage   = "";
    private String  successMessage = "";
    private float   msgTimer       = 0f;

    // Panel: 460 x 560 centrado en 800x700
    private static final float PW = 460f;
    private static final float PH = 560f;
    private static final float PX = (800 - PW) / 2f;
    private static final float PY = (700 - PH) / 2f;
    private static final float CX = PX + PW / 2f;

    private static final float FIELD_W   = 380f;
    private static final float FIELD_H   = 42f;
    private static final float FIELD_X   = CX - FIELD_W / 2f;
    private static final float BTN_SHOW  = 70f;

    // LOGIN
    private final Rectangle inpUser    = new Rectangle(FIELD_X, 370, FIELD_W, FIELD_H);
    private final Rectangle inpPass    = new Rectangle(FIELD_X, 300, FIELD_W - BTN_SHOW - 5, FIELD_H);
    private final Rectangle btnShowPwL = new Rectangle(FIELD_X + FIELD_W - BTN_SHOW, 300, BTN_SHOW, FIELD_H);
    private final Rectangle btnLoginDo = new Rectangle(CX - 150, 218, 300, 48);
    private final Rectangle btnToReg   = new Rectangle(CX - 150, 158, 300, 42);

    // REGISTER
    private final Rectangle inpFull    = new Rectangle(FIELD_X, 450, FIELD_W, FIELD_H);
    private final Rectangle inpUser2   = new Rectangle(FIELD_X, 390, FIELD_W, FIELD_H);
    private final Rectangle inpPass2   = new Rectangle(FIELD_X, 328, FIELD_W - BTN_SHOW - 5, FIELD_H);
    private final Rectangle btnShowPwR = new Rectangle(FIELD_X + FIELD_W - BTN_SHOW, 328, BTN_SHOW, FIELD_H);
    private final Rectangle inpCPw     = new Rectangle(FIELD_X, 266, FIELD_W - BTN_SHOW - 5, FIELD_H);
    private final Rectangle btnShowCPw = new Rectangle(FIELD_X + FIELD_W - BTN_SHOW, 266, BTN_SHOW, FIELD_H);
    private final Rectangle btnRegDo   = new Rectangle(CX - 150, 190, 300, 48);
    private final Rectangle btnToLog   = new Rectangle(CX - 150, 132, 300, 42);

    private boolean pwHasLen;
    private boolean pwHasUpper;
    private boolean pwHasLower;
    private boolean pwHasDigit;
    private boolean pwHasSpecial;

    public LoginScreen(MainGame game) {
        this.game = game;
        this.um   = UserManager.getInstance();
    }

    @Override
    public void show() {
        sr = new ShapeRenderer();

        // Música de menú / inicio
        if (game.audioManager != null) {
            game.audioManager.playMenuMusic();
        }

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if (focus == FocusField.NONE) {
                    return false;
                }

                if (character == '\b') {
                    deleteLast();
                } else if (character == '\r' || character == '\n') {
                    doAction();
                } else if (character == '\t') {
                    advanceFocus();
                } else if (character >= 32) {
                    appendChar(character);
                }

                return true;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.TAB) {
                    advanceFocus();
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);
        sr.setProjectionMatrix(game.camera.combined);

        msgTimer -= delta;
        if (msgTimer <= 0) {
            errorMessage = "";
            successMessage = "";
        }

        Gdx.gl.glClearColor(0.10f, 0.07f, 0.17f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        drawBackground();
        drawPanel();

        if (mode == Mode.LOGIN) {
            renderLogin();
        } else {
            renderRegister();
        }

        renderMessages();
        handleMouseClick();
    }

    private void drawBackground() {
        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Fondo base
        sr.setColor(new Color(0.08f, 0.05f, 0.16f, 1f));
        sr.rect(0, 0, 800, 700);

        // Degradado simulado por bandas
        sr.setColor(new Color(0.12f, 0.08f, 0.24f, 1f));
        sr.rect(0, 350, 800, 350);

        sr.setColor(new Color(0.08f, 0.06f, 0.18f, 1f));
        sr.rect(0, 0, 800, 350);

        // Círculos decorativos
        sr.setColor(new Color(0.2f, 0.75f, 0.2f, 0.16f));
        sr.circle(95, 585, 78);

        sr.setColor(new Color(0.5f, 0.2f, 0.85f, 0.14f));
        sr.circle(720, 620, 95);

        sr.setColor(new Color(1f, 0.85f, 0.2f, 0.10f));
        sr.circle(720, 120, 65);

        sr.setColor(new Color(0.2f, 0.75f, 0.2f, 0.10f));
        sr.circle(80, 80, 60);

        sr.end();

        drawMiniOmNom(90, 585, 45);
        drawMiniCandy(710, 120, 26);
    }

    private void drawMiniOmNom(float cx, float cy, float r) {
        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(0.2f, 0.75f, 0.2f, 0.75f));
        sr.circle(cx, cy, r);

        sr.setColor(Color.WHITE);
        sr.circle(cx - 14, cy + 12, 11);
        sr.circle(cx + 14, cy + 12, 11);

        sr.setColor(Color.BLACK);
        sr.circle(cx - 12, cy + 11, 5);
        sr.circle(cx + 12, cy + 11, 5);

        sr.setColor(Color.BLACK);
        sr.ellipse(cx - 22, cy - 18, 44, 16);

        sr.setColor(Color.WHITE);
        sr.rect(cx - 13, cy - 14, 26, 7);

        sr.end();
    }

    private void drawMiniCandy(float cx, float cy, float r) {
        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(0.45f, 0f, 0f, 0.35f));
        sr.circle(cx + 3, cy - 3, r);

        sr.setColor(new Color(0.9f, 0.12f, 0.12f, 0.75f));
        sr.circle(cx, cy, r);

        sr.setColor(new Color(1f, 0.6f, 0.6f, 0.65f));
        sr.circle(cx - 8, cy + 8, r * 0.35f);

        sr.end();
    }

    private void drawPanel() {
        // Sombra del panel
        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(0f, 0f, 0f, 0.42f));
        sr.rect(PX + 7, PY - 7, PW, PH);

        // Panel principal
        sr.setColor(new Color(0.14f, 0.10f, 0.26f, 0.98f));
        sr.rect(PX, PY, PW, PH);

        // Encabezado
        sr.setColor(new Color(0.20f, 0.13f, 0.36f, 1f));
        sr.rect(PX, PY + PH - 90, PW, 90);

        // Línea decorativa
        sr.setColor(new Color(0.7f, 0.45f, 1f, 0.45f));
        sr.rect(PX, PY + PH - 94, PW, 4);

        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(new Color(0.55f, 0.35f, 0.85f, 1f));
        sr.rect(PX, PY, PW, PH);
        sr.end();

        // Título
        game.batch.begin();

        game.fontLarge.setColor(new Color(0.92f, 0.78f, 1f, 1f));
        String title = mode == Mode.LOGIN ? MainGame.t("Iniciar sesión") : MainGame.t("Registro");
        game.fontLarge.draw(game.batch, title, PX + 58, PY + PH - 24);

        game.fontSmall.setColor(new Color(0.76f, 0.70f, 0.88f, 1f));
        game.fontSmall.draw(game.batch, "Cut the Rope", PX + 62, PY + PH - 58);

        game.batch.end();
    }

    private void renderLogin() {
        game.batch.begin();

        game.font.setColor(new Color(0.84f, 0.84f, 0.96f, 1f));
        game.font.draw(game.batch, MainGame.t("Usuario:"), FIELD_X, 430);
        game.font.draw(game.batch, MainGame.t("Contraseña:"), FIELD_X, 360);

        game.batch.end();

        drawField(inpUser, fieldUsername, false, focus == FocusField.USERNAME);

        drawField(
            inpPass,
            showPassword ? fieldPassword : mask(fieldPassword),
            false,
            focus == FocusField.PASSWORD
        );

        drawToggleBtn(btnShowPwL, showPassword ? MainGame.t("Ocultar") : MainGame.t("Ver"));

        drawActionBtn(
            btnLoginDo,
            MainGame.t("Iniciar sesión"),
            new Color(0.30f, 0.55f, 0.90f, 1f)
        );

        drawActionBtn(
            btnToReg,
            MainGame.t("→ Crear cuenta"),
            new Color(0.22f, 0.18f, 0.40f, 1f)
        );
    }

    private void renderRegister() {
        game.batch.begin();

        game.font.setColor(new Color(0.84f, 0.84f, 0.96f, 1f));
        game.font.draw(game.batch, MainGame.t("Nombre completo:"), FIELD_X, 510);
        game.font.draw(game.batch, MainGame.t("Usuario:"), FIELD_X, 450);
        game.font.draw(game.batch, MainGame.t("Contraseña:"), FIELD_X, 388);
        game.font.draw(game.batch, MainGame.t("Confirmar:"), FIELD_X, 326);

        game.batch.end();

        drawField(inpFull, fieldFullName, false, focus == FocusField.FULLNAME);
        drawField(inpUser2, fieldUsername, false, focus == FocusField.USERNAME);

        drawField(
            inpPass2,
            showPassword ? fieldPassword : mask(fieldPassword),
            false,
            focus == FocusField.PASSWORD
        );

        drawToggleBtn(btnShowPwR, showPassword ? MainGame.t("Ocultar") : MainGame.t("Ver"));

        drawField(
            inpCPw,
            showConfirmPw ? fieldConfirmPw : mask(fieldConfirmPw),
            false,
            focus == FocusField.CONFIRM_PW
        );

        drawToggleBtn(btnShowCPw, showConfirmPw ? MainGame.t("Ocultar") : MainGame.t("Ver"));

        drawPasswordRequirements();

        drawActionBtn(
            btnRegDo,
            MainGame.t("Registrarse"),
            new Color(0.25f, 0.60f, 0.30f, 1f)
        );

        drawActionBtn(
            btnToLog,
            MainGame.t("← Iniciar sesión"),
            new Color(0.22f, 0.18f, 0.40f, 1f)
        );
    }

    private void drawPasswordRequirements() {
        updatePwFlags();

        int ok =
            (pwHasLen ? 1 : 0) +
            (pwHasUpper ? 1 : 0) +
            (pwHasLower ? 1 : 0) +
            (pwHasDigit ? 1 : 0) +
            (pwHasSpecial ? 1 : 0);

        Color bar =
            ok <= 1 ? new Color(0.8f, 0.2f, 0.2f, 1f) :
            ok <= 3 ? new Color(0.9f, 0.7f, 0.1f, 1f) :
                      new Color(0.2f, 0.75f, 0.2f, 1f);

        float barW = (FIELD_W / 5f) * ok;

        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(0.20f, 0.17f, 0.30f, 1f));
        sr.rect(FIELD_X, 248, FIELD_W, 8);

        sr.setColor(bar);
        sr.rect(FIELD_X, 248, barW, 8);

        sr.end();

        // Requisitos con cheque
        float y1 = 238;
        float y2 = 218;
        float x1 = FIELD_X;
        float x2 = FIELD_X + 130;
        float x3 = FIELD_X + 260;

        drawRequirement(x1, y1, MainGame.t("8+ caracteres"), pwHasLen);
        drawRequirement(x2, y1, MainGame.t("Mayúscula"), pwHasUpper);
        drawRequirement(x3, y1, MainGame.t("Minúscula"), pwHasLower);
        drawRequirement(x1, y2, MainGame.t("Número"), pwHasDigit);
        drawRequirement(x2, y2, MainGame.t("Especial"), pwHasSpecial);
    }

    private void drawRequirement(float x, float y, String label, boolean ok) {
        game.batch.begin();

        if (ok) {
            game.fontSmall.setColor(new Color(0.35f, 1f, 0.45f, 1f));
            game.fontSmall.draw(game.batch, "✓ " + label, x, y);
        } else {
            game.fontSmall.setColor(new Color(0.8f, 0.55f, 0.55f, 1f));
            game.fontSmall.draw(game.batch, "□ " + label, x, y);
        }

        game.batch.end();
    }

    private void drawField(Rectangle r, String text, boolean password, boolean focused) {
        boolean hov = isHovered(r);

        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(0f, 0f, 0f, 0.32f));
        sr.rect(r.x + 3, r.y - 3, r.width, r.height);

        if (focused) {
            sr.setColor(new Color(0.34f, 0.25f, 0.55f, 1f));
        } else if (hov) {
            sr.setColor(new Color(0.28f, 0.22f, 0.44f, 1f));
        } else {
            sr.setColor(new Color(0.21f, 0.17f, 0.34f, 1f));
        }

        sr.rect(r.x, r.y, r.width, r.height);

        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);

        sr.setColor(
            focused
                ? new Color(0.7f, 0.45f, 1f, 1f)
                : new Color(0.40f, 0.30f, 0.60f, 1f)
        );

        sr.rect(r.x, r.y, r.width, r.height);

        sr.end();

        game.batch.begin();

        game.font.setColor(Color.WHITE);
        String display = text + (focused ? "|" : "");
        game.font.draw(game.batch, display, r.x + 10, r.y + r.height - 10);

        game.batch.end();
    }

    private void drawToggleBtn(Rectangle r, String label) {
        boolean hov = isHovered(r);

        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(
            hov
                ? new Color(0.42f, 0.30f, 0.66f, 1f)
                : new Color(0.30f, 0.22f, 0.50f, 1f)
        );

        sr.rect(r.x, r.y, r.width, r.height);

        sr.end();

        game.batch.begin();

        game.fontSmall.setColor(new Color(0.88f, 0.78f, 1f, 1f));
        game.fontSmall.draw(game.batch, label, r.x + 5, r.y + r.height - 10);

        game.batch.end();
    }

    private void drawActionBtn(Rectangle r, String text, Color col) {
        boolean hov = isHovered(r);

        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(0f, 0f, 0f, 0.25f));
        sr.rect(r.x + 3, r.y - 3, r.width, r.height);

        sr.setColor(hov ? col.cpy().add(0.12f, 0.12f, 0.12f, 0) : col);
        sr.rect(r.x, r.y, r.width, r.height);

        sr.end();

        game.batch.begin();

        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, text, r.x + 14, r.y + r.height - 11);

        game.batch.end();
    }

    private void renderMessages() {
        if (!errorMessage.isEmpty()) {
            game.batch.begin();

            game.fontSmall.setColor(new Color(1f, 0.35f, 0.35f, 1f));
            game.fontSmall.draw(game.batch, MainGame.t(errorMessage), FIELD_X, PY + PH + 16);

            game.batch.end();
        }

        if (!successMessage.isEmpty()) {
            game.batch.begin();

            game.fontSmall.setColor(new Color(0.35f, 1f, 0.35f, 1f));
            game.fontSmall.draw(game.batch, MainGame.t(successMessage), FIELD_X, PY + PH + 16);

            game.batch.end();
        }
    }

    private void handleMouseClick() {
        if (!Gdx.input.justTouched()) {
            return;
        }

        com.badlogic.gdx.math.Vector3 _touch =
            new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);

        game.viewport.unproject(_touch);

        float mx = _touch.x;
        float my = _touch.y;

        focus = FocusField.NONE;

        if (mode == Mode.LOGIN) {
            if (inpUser.contains(mx, my)) {
                focus = FocusField.USERNAME;
                playClick();
            }

            if (inpPass.contains(mx, my)) {
                focus = FocusField.PASSWORD;
                playClick();
            }

            if (btnShowPwL.contains(mx, my)) {
                showPassword = !showPassword;
                playClick();
            }

            if (btnLoginDo.contains(mx, my)) {
                playClick();
                doLogin();
            }

            if (btnToReg.contains(mx, my)) {
                playClick();
                switchMode();
            }
        } else {
            if (inpFull.contains(mx, my)) {
                focus = FocusField.FULLNAME;
                playClick();
            }

            if (inpUser2.contains(mx, my)) {
                focus = FocusField.USERNAME;
                playClick();
            }

            if (inpPass2.contains(mx, my)) {
                focus = FocusField.PASSWORD;
                playClick();
            }

            if (inpCPw.contains(mx, my)) {
                focus = FocusField.CONFIRM_PW;
                playClick();
            }

            if (btnShowPwR.contains(mx, my)) {
                showPassword = !showPassword;
                playClick();
            }

            if (btnShowCPw.contains(mx, my)) {
                showConfirmPw = !showConfirmPw;
                playClick();
            }

            if (btnRegDo.contains(mx, my)) {
                playClick();
                doRegisterAction();
            }

            if (btnToLog.contains(mx, my)) {
                playClick();
                switchMode();
            }
        }
    }

    private void doLogin() {
        String err = um.login(fieldUsername, fieldPassword);

        if (err != null) {
            showError(err);
        } else {
            UserData ud = um.getCurrentUser();

            if (ud != null) {
                MainGame.loadLang(ud.getLanguage());
                game.audioManager.applyUserPrefs(ud);
            }

            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
    }

    private void doRegisterAction() {
        if (!fieldPassword.equals(fieldConfirmPw)) {
            showError("Las contraseñas no coinciden.");
            return;
        }

        String err = um.register(fieldUsername, fieldPassword, fieldFullName);

        if (err != null) {
            showError(err);
        } else {
            showSuccess("¡Cuenta creada! Inicia sesión.");
            switchMode();
        }
    }

    private void doAction() {
        if (mode == Mode.LOGIN) {
            doLogin();
        } else {
            doRegisterAction();
        }
    }

    private void switchMode() {
        mode = (mode == Mode.LOGIN) ? Mode.REGISTER : Mode.LOGIN;

        errorMessage = "";
        successMessage = "";

        fieldPassword = "";
        fieldConfirmPw = "";

        showPassword = false;
        showConfirmPw = false;

        focus = FocusField.NONE;

        updatePwFlags();
    }

    private void showError(String m) {
        errorMessage = m;
        successMessage = "";
        msgTimer = 5f;
    }

    private void showSuccess(String m) {
        successMessage = m;
        errorMessage = "";
        msgTimer = 5f;
    }

    private void appendChar(char c) {
        switch (focus) {
            case USERNAME:
                if (fieldUsername.length() < 20) {
                    fieldUsername += c;
                }
                break;

            case PASSWORD:
                if (fieldPassword.length() < 30) {
                    fieldPassword += c;
                }
                break;

            case FULLNAME:
                if (fieldFullName.length() < 40) {
                    fieldFullName += c;
                }
                break;

            case CONFIRM_PW:
                if (fieldConfirmPw.length() < 30) {
                    fieldConfirmPw += c;
                }
                break;

            default:
                break;
        }

        updatePwFlags();
    }

    private void deleteLast() {
        switch (focus) {
            case USERNAME:
                if (!fieldUsername.isEmpty()) {
                    fieldUsername = fieldUsername.substring(0, fieldUsername.length() - 1);
                }
                break;

            case PASSWORD:
                if (!fieldPassword.isEmpty()) {
                    fieldPassword = fieldPassword.substring(0, fieldPassword.length() - 1);
                }
                break;

            case FULLNAME:
                if (!fieldFullName.isEmpty()) {
                    fieldFullName = fieldFullName.substring(0, fieldFullName.length() - 1);
                }
                break;

            case CONFIRM_PW:
                if (!fieldConfirmPw.isEmpty()) {
                    fieldConfirmPw = fieldConfirmPw.substring(0, fieldConfirmPw.length() - 1);
                }
                break;

            default:
                break;
        }

        updatePwFlags();
    }

    private void advanceFocus() {
        switch (focus) {
            case FULLNAME:
                focus = FocusField.USERNAME;
                break;

            case USERNAME:
                focus = FocusField.PASSWORD;
                break;

            case PASSWORD:
                focus = mode == Mode.LOGIN ? FocusField.NONE : FocusField.CONFIRM_PW;
                break;

            case CONFIRM_PW:
                focus = FocusField.NONE;
                break;

            default:
                focus = mode == Mode.REGISTER ? FocusField.FULLNAME : FocusField.USERNAME;
                break;
        }
    }

    private void updatePwFlags() {
        pwHasLen     = false;
        pwHasUpper   = false;
        pwHasLower   = false;
        pwHasDigit   = false;
        pwHasSpecial = false;

        if (fieldPassword.length() >= 8) {
            pwHasLen = true;
        }

        for (char c : fieldPassword.toCharArray()) {
            if (Character.isUpperCase(c)) {
                pwHasUpper = true;
            } else if (Character.isLowerCase(c)) {
                pwHasLower = true;
            } else if (Character.isDigit(c)) {
                pwHasDigit = true;
            } else {
                pwHasSpecial = true;
            }
        }
    }

    private String mask(String s) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            sb.append('*');
        }

        return sb.toString();
    }

    private boolean isHovered(Rectangle r) {
        com.badlogic.gdx.math.Vector3 tp =
            new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);

        game.viewport.unproject(tp);

        return r.contains(tp.x, tp.y);
    }

    private void playClick() {
        if (game.audioManager != null) {
            game.audioManager.playClick();
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
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (sr != null) {
            sr.dispose();
        }
    }
}
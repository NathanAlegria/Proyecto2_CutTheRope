

package com.cuttherope.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;


public class SettingsScreen implements Screen {

    private final MainGame    game;
    private final UserManager um;
    private ShapeRenderer     sr;
    private Texture fondoAjustes;

    private final Rectangle btnBack      = new Rectangle(18, 18, 140, 40);


    private final Rectangle btnMusicDown = new Rectangle(362, 456, 44, 36);
    private final Rectangle btnMusicUp   = new Rectangle(416, 456, 44, 36);


    private final Rectangle btnSfxDown   = new Rectangle(362, 398, 44, 36);
    private final Rectangle btnSfxUp     = new Rectangle(416, 398, 44, 36);


    private final Rectangle btnMusicOn   = new Rectangle(525, 456, 96, 36);


    private final Rectangle btnSfxOn     = new Rectangle(525, 398, 96, 36);


    private final Rectangle btnLang      = new Rectangle(420, 314, 126, 38);


    private final Rectangle btnTimer     = new Rectangle(420, 258, 96, 38);


    private final Rectangle[] btnAvatars = new Rectangle[5];

    private static final Color[] AVATAR_COLORS = {
        new Color(0.2f, 0.75f, 0.2f, 1f),
        new Color(0.9f, 0.15f, 0.15f, 1f),
        new Color(0.7f, 0.2f, 0.9f, 1f),
        new Color(0.2f, 0.4f, 0.9f, 1f),
        new Color(0.95f, 0.68f, 0.08f, 1f)
    };

    private static final Texture[] OMNOM_AVATAR_TEXTURES = new Texture[5];

    public SettingsScreen(MainGame game) {
        this.game = game;
        this.um   = UserManager.getInstance();

        for (int i = 0; i < 5; i++) {
            btnAvatars[i] = new Rectangle(24 + i * 151f, 128, 118, 94);
        }
    }

    @Override
    public void show() {
        sr = new ShapeRenderer();
        fondoAjustes = AssetPaths.textureAnyOrNull(AssetPaths.FONDO_AJUSTES, "FondoAjustes.png", "FondoA.png", "Imagenes/FondoAjustes.png", "Imagenes/FondoA.png", "assets/FondoAjustes.png", "assets/FondoA.png", "assets/Imagenes/FondoAjustes.png", "assets/Imagenes/FondoA.png", "FondoAjustes.jpg", "FondoA.jpg", "fondoA.png", "fondoa.png");
    }

    @Override
    public void render(float delta) {
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);
        sr.setProjectionMatrix(game.camera.combined);

        Gdx.gl.glClearColor(0.07f, 0.05f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        UserData ud = um.getCurrentUser();

        drawBg();
        drawPanels();
        drawTitle();
        drawAudio(ud);
        drawPrefs(ud);
        drawAvatars(ud);
        drawBtn(btnBack, MainGame.t("← Menú"), new Color(0.4f, 0.3f, 0.6f, 1f));

        handleInput(ud);
    }

    private void drawBg() {
        if (fondoAjustes != null) {
            game.batch.begin();
            game.batch.draw(fondoAjustes, 0, 0, 800, 700);
            game.batch.end();
        } else {
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(new Color(0.035f, 0.020f, 0.095f, 1f));
            sr.rect(0, 0, 800, 700);
            sr.end();
        }


        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0.02f, 0.01f, 0.06f, 0.18f));
        sr.rect(0, 0, 800, 700);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(new Color(0.62f, 0.34f, 1f, 1f));
        sr.rect(10, 10, 780, 680);
        sr.setColor(new Color(0.36f, 0.18f, 0.70f, 0.85f));
        sr.rect(18, 18, 764, 664);
        sr.end();
    }

    private void drawPanels() {

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0.06f, 0.04f, 0.16f, 0.62f));
        sr.rect(20, 390, 760, 150);
        sr.rect(20, 255, 760, 110);
        sr.rect(20, 108, 760, 128);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(new Color(0.46f, 0.28f, 0.82f, 0.95f));
        sr.rect(20, 390, 760, 150);
        sr.rect(20, 255, 760, 110);
        sr.rect(20, 108, 760, 128);
        sr.end();
    }

    private void drawTitle() {
        String title = MainGame.t("Ajustes");
        GlyphLayout layout = new GlyphLayout(game.fontLarge, title);
        game.batch.begin();
        game.fontLarge.setColor(new Color(0.9f, 0.7f, 1f, 1f));
        game.fontLarge.draw(game.batch, title, 400f - layout.width / 2f, 665);
        game.batch.end();
    }

    private void drawAudio(UserData ud) {
        float musicVol = ud != null ? ud.getMusicVolume() : 0.7f;
        float sfxVol   = ud != null ? ud.getSfxVolume()   : 0.8f;

        boolean musicEnabled = AudioManager.getInstance().isMusicEnabled();
        boolean sfxEnabled   = AudioManager.getInstance().isSfxEnabled();

        game.batch.begin();

        game.font.setColor(new Color(0.9f, 0.75f, 1f, 1f));
        game.font.draw(game.batch, MainGame.t("Audio"), 30, 515);

        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, MainGame.t("Música:"), 30, 483);
        game.font.draw(game.batch, MainGame.t("Efectos:"), 30, 425);

        game.batch.end();

        drawVolBar(150, 468, 190, musicVol);
        drawVolBar(150, 410, 190, sfxVol);

        drawSmallBtn(btnMusicDown, "-");
        drawSmallBtn(btnMusicUp, "+");
        drawSmallBtn(btnSfxDown, "-");
        drawSmallBtn(btnSfxUp, "+");

        drawToggle(
            btnMusicOn,
            musicEnabled ? MainGame.t("ON") : MainGame.t("OFF"),
            musicEnabled ? new Color(0.2f, 0.6f, 0.2f, 1f) : new Color(0.5f, 0.2f, 0.2f, 1f)
        );

        drawToggle(
            btnSfxOn,
            sfxEnabled ? MainGame.t("ON") : MainGame.t("OFF"),
            sfxEnabled ? new Color(0.2f, 0.6f, 0.2f, 1f) : new Color(0.5f, 0.2f, 0.2f, 1f)
        );

        game.batch.begin();
        game.fontSmall.setColor(Color.WHITE);
        game.fontSmall.draw(game.batch, (int)(musicVol * 100) + "%", 635, 483);
        game.fontSmall.draw(game.batch, (int)(sfxVol * 100) + "%", 635, 425);
        game.batch.end();
    }

    private void drawVolBar(float x, float y, float w, float vol) {
        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(0.2f, 0.15f, 0.35f, 1f));
        sr.rect(x, y, w, 14);

        sr.setColor(new Color(0.55f, 0.32f, 1f, 1f));
        sr.rect(x, y, w * vol, 14);

        sr.setColor(new Color(1f, 1f, 1f, 0.18f));
        sr.rect(x, y + 9, w * vol, 4);

        sr.end();
    }

    private void drawPrefs(UserData ud) {
        String lang = ud != null ? ud.getLanguage() : "es";
        boolean showTimer = ud != null && ud.isShowTimer();

        game.batch.begin();

        game.font.setColor(new Color(0.9f, 0.75f, 1f, 1f));
        game.font.draw(game.batch, MainGame.t("Preferencias"), 30, 372);

        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, MainGame.t("Idioma:"), 30, 338);
        game.font.draw(game.batch, MainGame.t("Timer en pantalla:"), 30, 282);

        game.batch.end();

        drawToggle(
            btnLang,
            lang.equals("es") ? MainGame.t("Español") : MainGame.t("English"),
            new Color(0.3f, 0.5f, 0.7f, 1f)
        );

        drawToggle(
            btnTimer,
            showTimer ? MainGame.t("ON") : MainGame.t("OFF"),
            showTimer ? new Color(0.2f, 0.6f, 0.2f, 1f) : new Color(0.5f, 0.2f, 0.2f, 1f)
        );
    }

    private void drawAvatars(UserData ud) {
        String cur = ud != null ? ud.getAvatarId() : "avatar1";

        game.batch.begin();
        game.font.setColor(new Color(0.9f, 0.75f, 1f, 1f));
        game.font.draw(game.batch, MainGame.t("Avatar"), 30, 248);
        game.batch.end();

        for (int i = 0; i < 5; i++) {
            Rectangle r = btnAvatars[i];
            boolean sel = cur.equals("avatar" + (i + 1));

            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(sel ? new Color(0.28f, 0.22f, 0.42f, 1f) : new Color(0.11f, 0.10f, 0.14f, 1f));
            sr.rect(r.x, r.y, r.width, r.height);


            sr.setColor(new Color(1f, 1f, 1f, 0.96f));
            sr.rect(r.x + 14, r.y + 20, r.width - 28, 50);
            sr.end();

            drawOmNomAvatar(r.x + r.width / 2f, r.y + 52, i, AVATAR_COLORS[i]);

            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(sel ? new Color(1f, 0.85f, 0.2f, 1f) : new Color(0.55f, 0.45f, 0.70f, 1f));
            sr.rect(r.x, r.y, r.width, r.height);
            sr.end();

            game.batch.begin();
            game.fontSmall.setColor(Color.WHITE);
            String avatarLabel = "OmNom" + (i + 1);
            GlyphLayout layout = new GlyphLayout(game.fontSmall, avatarLabel);
            game.fontSmall.draw(game.batch, avatarLabel, r.x + r.width / 2f - layout.width / 2f, r.y + 16);
            game.batch.end();
        }
    }

    private void drawOmNomAvatar(float cx, float cy, int index, Color body) {
        Texture tex = getOmNomAvatarTexture(index);

        if (tex != null) {
            game.batch.begin();
            float w = 74f;
            float h = 74f;
            game.batch.draw(tex, cx - w / 2f, cy - h / 2f - 2f, w, h);
            game.batch.end();
            return;
        }


        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(0f, 0f, 0f, 0.38f));
        sr.ellipse(cx - 35, cy - 34, 70, 20);

        sr.setColor(body);
        sr.circle(cx, cy, 31);
        sr.rect(cx - 31, cy - 25, 62, 28);
        sr.triangle(cx - 31, cy - 25, cx - 44, cy - 37, cx - 14, cy - 25);
        sr.triangle(cx + 31, cy - 25, cx + 44, cy - 37, cx + 14, cy - 25);

        sr.rectLine(cx, cy + 28, cx + 4, cy + 47, 4f);
        sr.circle(cx + 5, cy + 49, 4);

        sr.setColor(Color.BLACK);
        sr.ellipse(cx - 27, cy - 7, 54, 30);

        sr.setColor(Color.WHITE);
        sr.triangle(cx - 18, cy - 5, cx - 10, cy - 5, cx - 14, cy - 15);
        sr.triangle(cx + 10, cy - 5, cx + 18, cy - 5, cx + 14, cy - 15);

        sr.circle(cx - 12, cy + 15, 9);
        sr.circle(cx + 12, cy + 15, 9);
        sr.setColor(Color.BLACK);
        sr.circle(cx - 10, cy + 15, 4);
        sr.circle(cx + 10, cy + 15, 4);

        sr.end();
    }

    private Texture getOmNomAvatarTexture(int index) {
        if (index < 0 || index >= OMNOM_AVATAR_TEXTURES.length) return null;
        if (OMNOM_AVATAR_TEXTURES[index] == null) {
            int n = index + 1;
            OMNOM_AVATAR_TEXTURES[index] = AssetPaths.textureAnyOrNull(
                "OmNom" + n + ".png",
                "omnom" + n + ".png",
                "OmNom " + n + ".png",
                "omnom " + n + ".png",
                "OmNom" + n + ".jpg",
                "omnom" + n + ".jpg"
            );
        }
        return OMNOM_AVATAR_TEXTURES[index];
    }

    private void drawSmallBtn(Rectangle r, String text) {
        boolean hov = isHovered(r);

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0.03f, 0.02f, 0.08f, 0.78f));
        sr.rect(r.x, r.y, r.width, r.height);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(hov ? new Color(0.85f, 0.55f, 1f, 1f) : new Color(0.60f, 0.35f, 0.95f, 1f));
        sr.rect(r.x, r.y, r.width, r.height);
        sr.end();

        GlyphLayout layout = new GlyphLayout(game.font, text);
        game.batch.begin();
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, text, r.x + r.width / 2f - layout.width / 2f, r.y + r.height / 2f + layout.height / 2f - 2f);
        game.batch.end();
    }

    private void drawToggle(Rectangle r, String text, Color col) {
        boolean hov = isHovered(r);

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(hov ? col.cpy().add(0.1f, 0.1f, 0.1f, 0) : col);
        sr.rect(r.x, r.y, r.width, r.height);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(new Color(1f, 1f, 1f, 0.18f));
        sr.rect(r.x, r.y, r.width, r.height);
        sr.end();

        GlyphLayout layout = new GlyphLayout(game.fontSmall, text);
        game.batch.begin();
        game.fontSmall.setColor(Color.WHITE);
        game.fontSmall.draw(game.batch, text, r.x + r.width / 2f - layout.width / 2f, r.y + r.height / 2f + layout.height / 2f - 2f);
        game.batch.end();
    }

    private void drawBtn(Rectangle r, String text, Color col) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(isHovered(r) ? col.cpy().add(0.1f, 0.1f, 0.1f, 0) : col);
        sr.rect(r.x, r.y, r.width, r.height);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(new Color(0f, 0f, 0f, 0.55f));
        sr.rect(r.x, r.y, r.width, r.height);
        sr.end();

        GlyphLayout layout = new GlyphLayout(game.fontSmall, text);
        game.batch.begin();
        game.fontSmall.setColor(Color.WHITE);
        game.fontSmall.draw(game.batch, text, r.x + r.width / 2f - layout.width / 2f, r.y + r.height / 2f + layout.height / 2f - 2f);
        game.batch.end();
    }

    private void handleInput(UserData ud) {
        if (!Gdx.input.justTouched()) {
            return;
        }

        com.badlogic.gdx.math.Vector3 _touch =
            new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);

        game.viewport.unproject(_touch);

        float mx = _touch.x;
        float my = _touch.y;

        if (btnBack.contains(mx, my)) {
            game.audioManager.playClick();
            game.setScreen(new MainMenuScreen(game));
            dispose();
            return;
        }

        if (ud == null) {
            return;
        }

        AudioManager am = AudioManager.getInstance();

        if (btnMusicDown.contains(mx, my)) {
            game.audioManager.playClick();
            ud.setMusicVolume(Math.max(0f, ud.getMusicVolume() - 0.1f));
            am.setMusicVolume(ud.getMusicVolume());
            um.saveUser(ud);
        }

        if (btnMusicUp.contains(mx, my)) {
            game.audioManager.playClick();
            ud.setMusicVolume(Math.min(1f, ud.getMusicVolume() + 0.1f));
            am.setMusicVolume(ud.getMusicVolume());
            um.saveUser(ud);
        }

        if (btnSfxDown.contains(mx, my)) {
            game.audioManager.playClick();
            ud.setSfxVolume(Math.max(0f, ud.getSfxVolume() - 0.1f));
            am.setSfxVolume(ud.getSfxVolume());
            um.saveUser(ud);
        }

        if (btnSfxUp.contains(mx, my)) {
            game.audioManager.playClick();
            ud.setSfxVolume(Math.min(1f, ud.getSfxVolume() + 0.1f));
            am.setSfxVolume(ud.getSfxVolume());
            um.saveUser(ud);
        }

        if (btnMusicOn.contains(mx, my)) {
            ud.setMusicEnabled(!ud.isMusicEnabled());
            am.setMusicEnabled(ud.isMusicEnabled());

            if (ud.isMusicEnabled()) {
                am.playMenuMusic();
            }

            um.saveUser(ud);
        }

        if (btnSfxOn.contains(mx, my)) {
            ud.setSfxEnabled(!ud.isSfxEnabled());
            am.setSfxEnabled(ud.isSfxEnabled());

            if (ud.isSfxEnabled()) {
                am.playClick();
            }

            um.saveUser(ud);
        }

        if (btnLang.contains(mx, my)) {
            game.audioManager.playClick();

            ud.setLanguage(ud.getLanguage().equals("es") ? "en" : "es");


            MainGame.loadLang(ud.getLanguage());

            um.saveUser(ud);
        }

        if (btnTimer.contains(mx, my)) {
            game.audioManager.playClick();
            ud.setShowTimer(!ud.isShowTimer());
            um.saveUser(ud);
        }

        for (int i = 0; i < 5; i++) {
            if (btnAvatars[i].contains(mx, my)) {
                game.audioManager.playClick();
                ud.setAvatarId("avatar" + (i + 1));
                um.saveUser(ud);
                break;
            }
        }
    }

    private boolean isHovered(Rectangle r) {
        com.badlogic.gdx.math.Vector3 tp =
            new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);

        game.viewport.unproject(tp);

        return r.contains(tp.x, tp.y);
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
        if (fondoAjustes != null) {
            fondoAjustes.dispose();
            fondoAjustes = null;
        }


    }
}

package com.cuttherope.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

/** Herencia simple funcional: las pantallas sociales heredan dibujo común. */
public abstract class SocialScreenBase implements Screen {
    protected final MainGame game;
    protected final UserManager um;
    protected ShapeRenderer sr;

    public SocialScreenBase(MainGame game) {
        this.game = game;
        this.um = UserManager.getInstance();
    }

    protected void drawButton(Rectangle r, String text, Color col) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0f, 0f, 0f, 0.35f));
        sr.rect(r.x + 3, r.y - 3, r.width, r.height);
        sr.setColor(col);
        sr.rect(r.x, r.y, r.width, r.height);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(Color.BLACK);
        sr.rect(r.x, r.y, r.width, r.height);
        sr.rect(r.x - 1, r.y - 1, r.width + 2, r.height + 2);
        sr.end();

        game.batch.begin();
        game.fontSmall.setColor(Color.WHITE);
        game.fontSmall.draw(game.batch, text, r.x + 8, r.y + r.height - 10);
        game.batch.end();
    }

    @Override public void resize(int width, int height) { game.viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}

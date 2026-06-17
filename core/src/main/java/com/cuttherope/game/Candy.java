

package com.cuttherope.game;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;


public class Candy {

    public Vector2 position;
    public Vector2 velocity;
    public float   radius    = 22f;
    public boolean collected = false;
    public boolean fallen    = false;


    private static final float GRAVITY     = -720f;

    private static final float DAMPING     = 0.999f;

    private static final float MAX_SPEED   = 1050f;

    public Candy(float x, float y) {
        position = new Vector2(x, y);
        velocity = new Vector2(0, 0);
    }

    public void update(float delta) {
        if (collected || fallen) return;
        float dt = Math.min(delta, 0.033f);
        velocity.y += GRAVITY * dt;
        velocity.x *= DAMPING;
        velocity.y *= DAMPING;

        if (velocity.len() > MAX_SPEED) velocity.setLength(MAX_SPEED);
        position.x += velocity.x * dt;
        position.y += velocity.y * dt;
    }


    public void releaseWithVelocity(float vx, float vy) {

        float speed = (float) Math.sqrt(vx * vx + vy * vy);
        if (speed > 900f) {
            vx = vx / speed * 900f;
            vy = vy / speed * 900f;
        }
        velocity.set(vx, vy);
    }

    public void draw(ShapeRenderer sr) {
        if (collected) return;

        sr.setColor(new Color(0.3f, 0f, 0f, 0.35f));
        sr.circle(position.x + 3, position.y - 3, radius);

        sr.setColor(new Color(0.9f, 0.15f, 0.15f, 1f));
        sr.circle(position.x, position.y, radius);

        sr.setColor(new Color(1f, 0.45f, 0.1f, 1f));
        sr.circle(position.x, position.y, radius * 0.65f);

        sr.setColor(new Color(0.95f, 0.85f, 0.1f, 1f));
        sr.circle(position.x, position.y, radius * 0.3f);

        sr.setColor(new Color(1f, 1f, 1f, 0.5f));
        sr.circle(position.x - radius * 0.28f, position.y + radius * 0.28f, radius * 0.22f);
    }


    public void applyImpulse(float ix, float iy) {

        float speed = (float) Math.sqrt(ix * ix + iy * iy);
        if (speed > 150f) { ix = ix / speed * 150f; iy = iy / speed * 150f; }
        velocity.add(ix, iy);
    }

    public boolean isOutOfBounds(float screenW, float screenH) {
        return position.y < -radius * 3
            || position.x < -radius * 3
            || position.x > screenW + radius * 3;
    }

    private static Texture candyTexture;

    public void draw(SpriteBatch batch) {
        if (collected) return;
        if (candyTexture == null) candyTexture = AssetPaths.textureOrNull(AssetPaths.CANDY);
        if (candyTexture != null) {
            batch.draw(candyTexture, position.x - radius, position.y - radius, radius * 2f, radius * 2f);
        }
    }

    public static void disposeTexture() {
        if (candyTexture != null) { candyTexture.dispose(); candyTexture = null; }
    }

}

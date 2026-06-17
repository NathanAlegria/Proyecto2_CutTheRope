

package com.cuttherope.game;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;


public class Star {

    public float   x, y;
    public float   radius = 18f;
    public boolean collected = false;
    private float  animAngle = 0;
    private float  scaleAnim = 1f;
    private float  collectTimer = 0f;

    public Star(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void update(float delta) {
        animAngle += 60 * delta;
        if (collected) {
            collectTimer += delta;
            scaleAnim = 1f + collectTimer * 3f;
        }
    }


    public void draw(ShapeRenderer sr) {
        if (collected && collectTimer > 0.3f) return;

        float alpha = collected ? Math.max(0, 1f - collectTimer * 3f) : 1f;
        float scale = scaleAnim;

        sr.setColor(new Color(1f, 0.9f, 0f, alpha));
        drawStar(sr, x, y, radius * scale, radius * 0.4f * scale, 5, animAngle);


        sr.setColor(new Color(1f, 1f, 0.6f, alpha * 0.6f));
        sr.circle(x, y, radius * 0.3f * scale);
    }


    private void drawStar(ShapeRenderer sr, float cx, float cy,
                          float outerR, float innerR, int nPoints, float rotation) {
        float[] vx = new float[nPoints * 2];
        float[] vy = new float[nPoints * 2];
        for (int i = 0; i < nPoints * 2; i++) {
            float angle = MathUtils.degreesToRadians * (rotation + i * 180f / nPoints);
            float r = (i % 2 == 0) ? outerR : innerR;
            vx[i] = cx + MathUtils.cos(angle) * r;
            vy[i] = cy + MathUtils.sin(angle) * r;
        }
        for (int i = 0; i < nPoints * 2; i++) {
            int next = (i + 1) % (nPoints * 2);
            sr.triangle(cx, cy, vx[i], vy[i], vx[next], vy[next]);
        }
    }


    public boolean checkCollision(Candy candy) {
        if (collected) return false;
        float dx = candy.position.x - x;
        float dy = candy.position.y - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        return dist < (radius + candy.radius * 1.05f + 4f);
    }
}

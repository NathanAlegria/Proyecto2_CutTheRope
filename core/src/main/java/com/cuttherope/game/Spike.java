package com.cuttherope.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;


public class Spike {

    public float x, y;
    public float width  = 20f;
    public float height = 20f;
    public int dir;

    public Spike(float x, float y, int dir) {
        this.x   = x;
        this.y   = y;
        this.dir = dir;
    }


    public Spike(float x, float y) {
        this(x, y, 0);
    }


    public void draw(ShapeRenderer sr) {
        float hw = width  * 0.5f;
        float hh = height * 0.5f;

        float x1, y1, x2, y2, x3, y3;
        float bx1, by1, bx2, by2, bx3, by3;

        if (dir == 1) {

            x1 = x - hw;  y1 = y - hh;
            x2 = x - hw;  y2 = y + hh;
            x3 = x + hw;  y3 = y;
            bx1 = x - hw; by1 = y + hh;
            bx2 = x + hw; by2 = y;
            bx3 = x - hw; by3 = y + hh * 0.7f;
        } else if (dir == 2) {

            x1 = x + hw;  y1 = y - hh;
            x2 = x + hw;  y2 = y + hh;
            x3 = x - hw;  y3 = y;
            bx1 = x + hw; by1 = y + hh;
            bx2 = x - hw; by2 = y;
            bx3 = x + hw; by3 = y + hh * 0.7f;
        } else {

            x1 = x - hw;  y1 = y - hh;
            x2 = x + hw;  y2 = y - hh;
            x3 = x;        y3 = y + hh;
            bx1 = x - hw; by1 = y - hh;
            bx2 = x;       by2 = y + hh;
            bx3 = x - hw * 0.3f; by3 = y - hh;
        }


        sr.setColor(new Color(0.1f, 0.1f, 0.1f, 0.3f));
        sr.triangle(x1 + 2, y1 - 2, x2 + 2, y2 - 2, x3 + 2, y3 - 2);


        sr.setColor(new Color(0.62f, 0.62f, 0.65f, 1f));
        sr.triangle(x1, y1, x2, y2, x3, y3);


        sr.setColor(new Color(0.85f, 0.85f, 0.88f, 0.7f));
        sr.triangle(bx1, by1, bx2, by2, bx3, by3);
    }


    public boolean checkCollision(Candy candy) {
        float dx   = candy.position.x - x;
        float dy   = candy.position.y - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        return dist < (width * 0.34f + candy.radius * 0.52f);
    }
}

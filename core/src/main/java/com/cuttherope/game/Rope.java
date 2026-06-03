/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cuttherope.game;

/**
 *
 * @author Nathan
 */
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Rope - Simula una cuerda con física de partículas (Verlet).
 * Física corregida: sin impulso al cortar, el dulce hereda la velocidad
 * real de los nodos en ese momento.
 */
public class Rope {

    private static final int   SEGMENTS   = 12;
    private static final float GRAVITY    = -480f;   // px/s²
    private static final int   ITERATIONS = 8;       // más iteraciones = más estable

    private Vector2[] nodes;
    private Vector2[] prevNodes;
    private boolean[] nodePinned;

    private Candy   candy;
    public  boolean cut = false;
    private Color   color;

    private float anchorX, anchorY;
    private float segmentLength;

    public Rope(float anchorX, float anchorY, Candy candy, Color color) {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.candy   = candy;
        this.color   = color;

        int total  = SEGMENTS + 2;
        nodes      = new Vector2[total];
        prevNodes  = new Vector2[total];
        nodePinned = new boolean[total];

        float dx = (candy.position.x - anchorX) / (total - 1);
        float dy = (candy.position.y - anchorY) / (total - 1);

        for (int i = 0; i < total; i++) {
            nodes[i]     = new Vector2(anchorX + dx * i, anchorY + dy * i);
            prevNodes[i] = new Vector2(nodes[i]);
        }
        nodePinned[0] = true;

        float totalLen = new Vector2(candy.position).sub(anchorX, anchorY).len();
        segmentLength  = (totalLen / (total - 1)) * 1.02f;
    }

    public void update(float delta) {
        if (cut) return;

        // Clamp delta para evitar explosiones si hay lag
        float dt = Math.min(delta, 0.033f);

        int total = nodes.length;

        // Forzar último nodo en la posición actual del dulce
        nodes[total - 1].set(candy.position);

        // Verlet integration en nodos intermedios
        for (int i = 1; i < total - 1; i++) {
            if (nodePinned[i]) continue;
            Vector2 cur  = nodes[i];
            Vector2 prev = prevNodes[i];
            float vx = (cur.x - prev.x) * 0.98f;   // amortiguación leve
            float vy = (cur.y - prev.y) * 0.98f;
            prev.set(cur);
            cur.x += vx;
            cur.y += vy + GRAVITY * dt * dt;
        }

        // Restricciones de longitud
        for (int iter = 0; iter < ITERATIONS; iter++) {
            // ancla siempre fija
            nodes[0].set(anchorX, anchorY);

            for (int i = 0; i < total - 1; i++) {
                Vector2 a    = nodes[i];
                Vector2 b    = nodes[i + 1];
                float   dist = a.dst(b);
                if (dist < 0.001f) continue;
                float diff = (dist - segmentLength) / dist * 0.5f;
                float offX = (b.x - a.x) * diff;
                float offY = (b.y - a.y) * diff;
                if (!nodePinned[i]) {
                    a.x += offX;
                    a.y += offY;
                }
                // El último nodo (dulce) puede moverse en restricción solo si
                // no hay otra cuerda activa controlándolo — GameScreen lo maneja
                if (i + 1 == total - 1) {
                    b.x -= offX;
                    b.y -= offY;
                } else if (!nodePinned[i + 1]) {
                    b.x -= offX;
                    b.y -= offY;
                }
            }
            nodes[0].set(anchorX, anchorY);
        }

        // Propagar posición del último nodo al dulce
        if (!candy.collected && !candy.fallen) {
            candy.position.set(nodes[total - 1]);
            candy.velocity.set(0, 0);
        }
    }

    /**
     * Velocidad actual del extremo inferior (para dársela al dulce al cortar).
     * Se calcula como desplazamiento entre frame actual y anterior.
     */
    public Vector2 getTipVelocity(float delta) {
        float dt = Math.max(delta, 0.001f);
        int last = nodes.length - 1;
        return new Vector2(
            (nodes[last].x - prevNodes[last].x) / dt,
            (nodes[last].y - prevNodes[last].y) / dt
        );
    }

    public void draw(ShapeRenderer sr) {
        if (cut) return;
        sr.setColor(color);
        int total = nodes.length;
        for (int i = 0; i < total - 1; i++) {
            sr.rectLine(nodes[i].x, nodes[i].y,
                        nodes[i + 1].x, nodes[i + 1].y, 3.5f);
        }
        sr.setColor(new Color(0.3f, 0.3f, 0.3f, 1f));
        sr.circle(anchorX, anchorY, 7f);
    }

    public boolean trycut(float x1, float y1, float x2, float y2) {
        if (cut) return false;
        int total = nodes.length;
        for (int i = 0; i < total - 1; i++) {
            if (segmentsIntersect(x1, y1, x2, y2,
                    nodes[i].x, nodes[i].y,
                    nodes[i + 1].x, nodes[i + 1].y)) {
                cut = true;
                // NO aplicar impulso externo: la velocidad la toma GameScreen
                // desde getTipVelocity() para que sea natural
                return true;
            }
        }
        return false;
    }

    private boolean segmentsIntersect(float ax, float ay, float bx, float by,
                                      float cx, float cy, float dx, float dy) {
        float d1 = cross(cx, cy, dx, dy, ax, ay);
        float d2 = cross(cx, cy, dx, dy, bx, by);
        float d3 = cross(ax, ay, bx, by, cx, cy);
        float d4 = cross(ax, ay, bx, by, dx, dy);
        return ((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0))
            && ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0));
    }

    private float cross(float ax, float ay, float bx, float by, float px, float py) {
        return (bx - ax) * (py - ay) - (by - ay) * (px - ax);
    }

    public float getAnchorX() { return anchorX; }
    public float getAnchorY() { return anchorY; }
    public boolean isCut()    { return cut; }
}
package com.cuttherope.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Rope - cuerda física tipo péndulo.
 * La cuerda ya NO congela el dulce ni le pone velocidad 0.
 * El dulce mantiene su velocidad real y la cuerda solo limita la distancia
 * al ancla, dejando el componente tangencial para que se balancee.
 */
public class Rope {

    private static final int SEGMENTS = 14;
    private static final int CONSTRAINT_ITERATIONS = 3;

    private final Vector2[] nodes;
    private final Candy candy;
    private final Color color;
    private final float anchorX;
    private final float anchorY;
    private final float ropeLength;

    public boolean cut = false;

    public Rope(float anchorX, float anchorY, Candy candy, Color color) {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.candy = candy;
        this.color = color;
        this.ropeLength = Math.max(30f, candy.position.dst(anchorX, anchorY));
        this.nodes = new Vector2[SEGMENTS + 1];

        for (int i = 0; i < nodes.length; i++) {
            float t = i / (float)(nodes.length - 1);
            nodes[i] = new Vector2(
                anchorX + (candy.position.x - anchorX) * t,
                anchorY + (candy.position.y - anchorY) * t
            );
        }
    }

    public void update(float delta) {
        if (cut || candy.collected || candy.fallen) return;

        float dt = Math.max(0.001f, Math.min(delta, 0.033f));

        // Varias pasadas dan estabilidad cuando hay más de una cuerda.
        for (int i = 0; i < CONSTRAINT_ITERATIONS; i++) {
            constrainCandy(dt);
        }

        rebuildVisualNodes();
    }

    /**
     * Física tipo resorte-amortiguador.
     * Antes la cuerda proyectaba el dulce a una posición exacta; con dos cuerdas
     * eso lo podía dejar pegado en el aire. Ahora la cuerda aplica tensión a la
     * velocidad del dulce y permite balanceo real con momentum.
     */
    private void constrainCandy(float dt) {
        Vector2 radial = new Vector2(candy.position.x - anchorX, candy.position.y - anchorY);
        float dist = radial.len();
        if (dist < 0.0001f) return;

        radial.scl(1f / dist);
        float stretch = dist - ropeLength;

        if (stretch > 0f) {
            float radialVelocity = candy.velocity.dot(radial);

            // Tensión: devuelve el dulce hacia el ancla sin matar el componente tangencial.
            float stiffness = 34f;
            float damping = 5.5f;
            float tension = (stretch * stiffness) + (radialVelocity * damping);

            candy.velocity.x -= radial.x * tension * dt;
            candy.velocity.y -= radial.y * tension * dt;
        }

        // Límite de seguridad: evita que la cuerda se estire demasiado si hay lag,
        // pero conserva la velocidad tangencial para que el caramelo siga oscilando.
        float maxLen = ropeLength * 1.10f;
        if (dist > maxLen) {
            candy.position.set(anchorX + radial.x * maxLen, anchorY + radial.y * maxLen);
            float radialVelocity = candy.velocity.dot(radial);
            if (radialVelocity > 0f) {
                candy.velocity.x -= radial.x * radialVelocity * 0.85f;
                candy.velocity.y -= radial.y * radialVelocity * 0.85f;
            }
        }
    }

    private void rebuildVisualNodes() {
        for (int i = 0; i < nodes.length; i++) {
            float t = i / (float)(nodes.length - 1);
            float x = anchorX + (candy.position.x - anchorX) * t;
            float y = anchorY + (candy.position.y - anchorY) * t;

            // Leve curva visual para que no parezca una línea rígida.
            float sag = (float)Math.sin(t * Math.PI) * 4f;
            nodes[i].set(x, y - sag);
        }
        nodes[0].set(anchorX, anchorY);
        nodes[nodes.length - 1].set(candy.position);
    }

    public Vector2 getTipVelocity(float delta) {
        return new Vector2(candy.velocity);
    }

    public void draw(ShapeRenderer sr) {
        if (cut) return;

        sr.setColor(color);
        for (int i = 0; i < nodes.length - 1; i++) {
            sr.rectLine(nodes[i].x, nodes[i].y, nodes[i + 1].x, nodes[i + 1].y, 4f);
        }

        sr.setColor(new Color(0.12f, 0.45f, 0.95f, 1f));
        sr.circle(anchorX, anchorY, 8f);
        sr.setColor(new Color(0.85f, 0.95f, 1f, 1f));
        sr.circle(anchorX, anchorY, 4f);
    }

    public boolean trycut(float x1, float y1, float x2, float y2) {
        if (cut) return false;
        for (int i = 0; i < nodes.length - 1; i++) {
            if (segmentsIntersect(x1, y1, x2, y2,
                    nodes[i].x, nodes[i].y,
                    nodes[i + 1].x, nodes[i + 1].y)) {
                cut = true;
                // No se toca la velocidad del dulce. Sale con el momentum que ya tenía.
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
    public float getRopeLength() { return ropeLength; }
    public boolean isCut() { return cut; }
}

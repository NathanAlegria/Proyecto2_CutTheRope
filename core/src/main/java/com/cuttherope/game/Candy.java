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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Candy - El caramelo que Om Nom debe atrapar.
 * Física corregida: gravedad razonable, sin velocidad explosiva al cortar.
 */
public class Candy {

    public Vector2 position;
    public Vector2 velocity;
    public float   radius    = 22f;
    public boolean collected = false;
    public boolean fallen    = false;

    // Gravedad más fuerte para que el caramelo tenga caída natural tipo Cut the Rope
    private static final float GRAVITY     = -620f;   // px/s²
    // Menos fricción: conserva mejor el momentum al salir de la cuerda
    private static final float DAMPING     = 0.998f;
    // Velocidad máxima más alta para permitir balanceos reales
    private static final float MAX_SPEED   = 900f;
    private static final float RELEASE_BOOST = 1.28f;
    private static final float MIN_RELEASE_SPEED = 230f;

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
        // Clamp velocidad
        if (velocity.len() > MAX_SPEED) velocity.setLength(MAX_SPEED);
        position.x += velocity.x * dt;
        position.y += velocity.y * dt;
    }

    /** Establece la velocidad inicial cuando se suelta de todas las cuerdas. */
    public void releaseWithVelocity(float vx, float vy) {
        // Al cortar, el dulce debe heredar el momentum tangencial de la cuerda.
        vx *= RELEASE_BOOST;
        vy *= RELEASE_BOOST;

        float speed = (float) Math.sqrt(vx * vx + vy * vy);

        // Si el cálculo llega muy bajo, se aplica un mínimo suave para evitar
        // que el caramelo caiga muerto sin alcanzar las estrellas.
        if (speed > 0.001f && speed < MIN_RELEASE_SPEED) {
            vx = vx / speed * MIN_RELEASE_SPEED;
            vy = vy / speed * MIN_RELEASE_SPEED;
            speed = MIN_RELEASE_SPEED;
        }

        if (speed > MAX_SPEED) {
            vx = vx / speed * MAX_SPEED;
            vy = vy / speed * MAX_SPEED;
        }

        velocity.set(vx, vy);
    }

    public void draw(ShapeRenderer sr) {
        if (collected) return;
        // Sombra
        sr.setColor(new Color(0.3f, 0f, 0f, 0.35f));
        sr.circle(position.x + 3, position.y - 3, radius);
        // Cuerpo
        sr.setColor(new Color(0.9f, 0.15f, 0.15f, 1f));
        sr.circle(position.x, position.y, radius);
        // Anillo decorativo
        sr.setColor(new Color(1f, 0.45f, 0.1f, 1f));
        sr.circle(position.x, position.y, radius * 0.65f);
        // Centro
        sr.setColor(new Color(0.95f, 0.85f, 0.1f, 1f));
        sr.circle(position.x, position.y, radius * 0.3f);
        // Brillo
        sr.setColor(new Color(1f, 1f, 1f, 0.5f));
        sr.circle(position.x - radius * 0.28f, position.y + radius * 0.28f, radius * 0.22f);
    }

    /** Ya no usa impulso externo: la velocidad la asigna releaseWithVelocity */
    public void applyImpulse(float ix, float iy) {
        float speed = (float) Math.sqrt(ix * ix + iy * iy);
        if (speed > MAX_SPEED) { ix = ix / speed * MAX_SPEED; iy = iy / speed * MAX_SPEED; }
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
        if (candyTexture == null) candyTexture = AssetPaths.texture(AssetPaths.CANDY);
        batch.draw(candyTexture, position.x - radius, position.y - radius, radius * 2f, radius * 2f);
    }

    public static void disposeTexture() {
        if (candyTexture != null) { candyTexture.dispose(); candyTexture = null; }
    }

}
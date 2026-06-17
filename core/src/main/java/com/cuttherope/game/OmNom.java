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

/**
 * OmNom - El personaje que debe recibir el caramelo.
 * Se dibuja con ShapeRenderer como un monstruito verde.
 */
public class OmNom {

    public float x, y;
    public float radius = 35f;
    public float mouthOpenness = 0f;
    private float mouthTimer = 0f;
    private boolean eating = false;
    private float eatTimer = 0f;

    // Color del cuerpo — se ajusta según el avatar del usuario
    private Color bodyColor = new Color(0.2f, 0.75f, 0.2f, 1f);
    private Color bellyColor = new Color(0.55f, 0.9f, 0.4f, 1f);

    public OmNom(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /** Aplica el color del avatar al cuerpo de Om Nom. */
    public void setAvatarColor(String avatarId) {
        switch (avatarId) {
            case "avatar2": avatarIndex = 1; bodyColor = new Color(0.9f,0.15f,0.15f,1f);
                            bellyColor= new Color(1f,0.45f,0.45f,1f);   break;
            case "avatar3": avatarIndex = 2; bodyColor = new Color(0.7f,0.2f,0.9f,1f);
                            bellyColor= new Color(0.9f,0.55f,1f,1f);    break;
            case "avatar4": avatarIndex = 3; bodyColor = new Color(0.2f,0.4f,0.9f,1f);
                            bellyColor= new Color(0.5f,0.65f,1f,1f);    break;
            case "avatar5": avatarIndex = 4; bodyColor = new Color(0.95f,0.68f,0.08f,1f);
                            bellyColor= new Color(1f,0.85f,0.35f,1f);   break;
            default:        avatarIndex = 0; bodyColor = new Color(0.2f,0.75f,0.2f,1f);
                            bellyColor= new Color(0.55f,0.9f,0.4f,1f);  break;
        }
    }

    public void update(float delta) {
        // Animación de boca pulsante
        mouthTimer += delta * 3f;
        mouthOpenness = (float)(0.3f + Math.sin(mouthTimer) * 0.1f);

        if (eating) {
            eatTimer += delta;
            mouthOpenness = Math.min(1f, eatTimer * 5f);
            if (eatTimer > 0.5f) { eating = false; eatTimer = 0f; }
        }
    }

    /** Dispara animación de comer. */
    public void eat() { eating = true; eatTimer = 0f; }

    /** Dibuja Om Nom con ShapeRenderer (círculos, arcos). */
    public void draw(ShapeRenderer sr) {
        // Sombra
        sr.setColor(new Color(0, 0.3f, 0, 0.3f));
        sr.ellipse(x - radius, y - radius * 0.15f, radius * 2, radius * 0.4f);

        // Cuerpo
        sr.setColor(bodyColor);
        sr.circle(x, y, radius);

        // Panza más clara
        sr.setColor(bellyColor);
        sr.ellipse(x - radius * 0.5f, y - radius * 0.5f, radius, radius * 0.7f);

        // Ojo izquierdo (blanco)
        sr.setColor(Color.WHITE);
        sr.circle(x - radius * 0.32f, y + radius * 0.3f, radius * 0.28f);
        // Pupila izquierda
        sr.setColor(Color.BLACK);
        sr.circle(x - radius * 0.28f, y + radius * 0.28f, radius * 0.13f);

        // Ojo derecho (blanco)
        sr.setColor(Color.WHITE);
        sr.circle(x + radius * 0.32f, y + radius * 0.3f, radius * 0.28f);
        // Pupila derecha
        sr.setColor(Color.BLACK);
        sr.circle(x + radius * 0.28f, y + radius * 0.28f, radius * 0.13f);

        // Boca (arco negro)
        float mouthW  = radius * 0.9f;
        float mouthH  = radius * 0.4f * mouthOpenness + 2f;
        sr.setColor(Color.BLACK);
        sr.ellipse(x - mouthW * 0.5f, y - radius * 0.4f, mouthW, mouthH);

        // Dientes (rectángulo blanco dentro de la boca)
        if (mouthOpenness > 0.15f) {
            sr.setColor(Color.WHITE);
            float tw = mouthW * 0.3f;
            float th = mouthH * 0.5f;
            sr.rect(x - tw * 0.5f, y - radius * 0.4f, tw, th);
        }
    }

    /** Verifica si el caramelo llegó a la boca.
     *  Se usa un área un poco más grande porque el dulce puede caer rápido y
     *  antes pasaba rozando a Om Nom sin activar la victoria.
     */
    public boolean isCandyCaught(Candy candy) {
        float dx = candy.position.x - x;
        float dy = candy.position.y - (y + radius * 0.15f);
        float catchRadius = radius + candy.radius + 35f;
        return Math.sqrt(dx * dx + dy * dy) < catchRadius;
    }

    /** Alias para compatibilidad con el nombre anterior. */
    public boolean isCanyCaught(Candy candy) {
        return isCandyCaught(candy);
    }

    private static final Texture[] omNomTextures = new Texture[5];
    private int avatarIndex = 0;

    private Texture getAvatarTexture() {
        if (avatarIndex < 0 || avatarIndex >= omNomTextures.length) avatarIndex = 0;
        if (omNomTextures[avatarIndex] == null) {
            int n = avatarIndex + 1;
            omNomTextures[avatarIndex] = AssetPaths.textureAnyOrNull(
                "OmNom" + n + ".png",
                "omnom" + n + ".png",
                "OmNom " + n + ".png",
                "omnom " + n + ".png",
                "OmNom" + n + ".jpg",
                "omnom" + n + ".jpg"
            );
        }
        return omNomTextures[avatarIndex];
    }

    public void draw(SpriteBatch batch) {
        Texture tex = getAvatarTexture();
        if (tex != null) {
            float size = radius * 2.65f;
            batch.draw(tex, x - size / 2f, y - radius * 0.62f, size, size);
        }
    }

    public static void disposeTexture() {
        for (int i = 0; i < omNomTextures.length; i++) {
            if (omNomTextures[i] != null) {
                omNomTextures[i].dispose();
                omNomTextures[i] = null;
            }
        }
    }

}
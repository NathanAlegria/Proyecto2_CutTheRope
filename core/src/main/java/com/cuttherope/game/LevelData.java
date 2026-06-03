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

/**
 * LevelData - Configuración de los 5 niveles.
 * Coordenadas ajustadas para viewport 800x700.
 * Om Nom siempre abajo (y≈55), anclas arriba (y≈610).
 * El dulce empieza cerca del centro para que las cuerdas queden tensas.
 */
public class LevelData {

    public final int    levelIndex;
    public final String title;
    public final String hint;
    public final int    timeLimit;
    public final Color  ropeColor;
    public final Color  bgColor1;
    public final Color  bgColor2;

    public final float[] anchorX;
    public final float[] anchorY;
    public final float   candyX, candyY;
    public final float   omNomX, omNomY;
    public final float[] starX;
    public final float[] starY;
    public final int     lives;

    public LevelData(int levelIndex, String title, String hint,
                     int timeLimit, Color ropeColor, Color bgColor1, Color bgColor2,
                     float[] anchorX, float[] anchorY,
                     float candyX, float candyY,
                     float omNomX, float omNomY,
                     float[] starX, float[] starY,
                     int lives) {
        this.levelIndex = levelIndex;
        this.title      = title;
        this.hint       = hint;
        this.timeLimit  = timeLimit;
        this.ropeColor  = ropeColor;
        this.bgColor1   = bgColor1;
        this.bgColor2   = bgColor2;
        this.anchorX    = anchorX;
        this.anchorY    = anchorY;
        this.candyX     = candyX;
        this.candyY     = candyY;
        this.omNomX     = omNomX;
        this.omNomY     = omNomY;
        this.starX      = starX;
        this.starY      = starY;
        this.lives      = lives;
    }

    public static LevelData[] createAll() {
        return new LevelData[]{ level1(), level2(), level3(), level4(), level5() };
    }

    // ── Nivel 1 – Tutorial: 1 cuerda, Om Nom directo debajo ──────────────────
    private static LevelData level1() {
        return new LevelData(0,
            "¡Empieza aquí!", "Corta la cuerda para alimentar a Om Nom",
            0,
            new Color(0.6f, 0.35f, 0.1f, 1f),
            new Color(0.53f, 0.81f, 0.98f, 1f),
            new Color(0.27f, 0.65f, 0.90f, 1f),
            new float[]{ 400 },        // 1 ancla centrada
            new float[]{ 610 },
            400, 450,                  // dulce justo bajo el ancla
            400, 60,                   // Om Nom en el centro abajo
            new float[]{ 400 },
            new float[]{ 340 },
            3);
    }

    // ── Nivel 2 – Dos cuerdas ─────────────────────────────────────────────────
    private static LevelData level2() {
        return new LevelData(1,
            "Doble desafío", "Corta las cuerdas en el orden correcto",
            60,
            new Color(0.2f, 0.6f, 0.2f, 1f),
            new Color(0.98f, 0.93f, 0.68f, 1f),
            new Color(0.95f, 0.80f, 0.40f, 1f),
            new float[]{ 240, 560 },
            new float[]{ 610, 610 },
            400, 430,
            400, 60,
            new float[]{ 240, 560 },
            new float[]{ 330, 330 },
            3);
    }

    // ── Nivel 3 – Tres cuerdas ────────────────────────────────────────────────
    private static LevelData level3() {
        return new LevelData(2,
            "Triple amenaza", "Recoge todas las estrellas antes de cortar",
            50,
            new Color(0.7f, 0.1f, 0.7f, 1f),
            new Color(0.18f, 0.18f, 0.30f, 1f),
            new Color(0.10f, 0.10f, 0.22f, 1f),
            new float[]{ 160, 400, 640 },
            new float[]{ 610, 610, 610 },
            400, 450,
            400, 55,
            new float[]{ 160, 400, 640 },
            new float[]{ 390, 350, 380 },
            3);
    }

    // ── Nivel 4 – Cuatro cuerdas ──────────────────────────────────────────────
    private static LevelData level4() {
        return new LevelData(3,
            "El laberinto", "Las estrellas están en lugares difíciles",
            40,
            new Color(0.8f, 0.5f, 0.0f, 1f),
            new Color(0.08f, 0.22f, 0.08f, 1f),
            new Color(0.05f, 0.15f, 0.05f, 1f),
            new float[]{ 110, 310, 490, 690 },
            new float[]{ 610, 600, 600, 610 },
            400, 455,
            400, 55,
            new float[]{ 150, 400, 650 },
            new float[]{ 430, 290, 430 },
            3);
    }

    // ── Nivel 5 – Jefe final ──────────────────────────────────────────────────
    private static LevelData level5() {
        return new LevelData(4,
            "¡Reto final!", "¡Demuestra que eres el maestro del corte!",
            30,
            new Color(0.9f, 0.1f, 0.1f, 1f),
            new Color(0.10f, 0.05f, 0.05f, 1f),
            new Color(0.20f, 0.02f, 0.02f, 1f),
            new float[]{ 80, 230, 400, 570, 720 },
            new float[]{ 610, 600, 605, 600, 610 },
            400, 460,
            400, 55,
            new float[]{ 140, 400, 660 },
            new float[]{ 400, 270, 400 },
            2);
    }
}
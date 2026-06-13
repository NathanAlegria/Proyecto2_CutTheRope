package com.cuttherope.game;

import com.badlogic.gdx.graphics.Color;

public class LevelData {
    public final int levelIndex;
    public final String title;
    public final String hint;
    public final int timeLimit;
    public final Color ropeColor, bgColor1, bgColor2;
    public final float[] anchorX, anchorY;
    public final float candyX, candyY, omNomX, omNomY;
    public final float[] starX, starY;
    public final int lives;

    public LevelData(int levelIndex, String title, String hint, int timeLimit, Color ropeColor, Color bgColor1, Color bgColor2, float[] anchorX, float[] anchorY, float candyX, float candyY, float omNomX, float omNomY, float[] starX, float[] starY, int lives) {
        this.levelIndex = levelIndex; this.title = title; this.hint = hint; this.timeLimit = timeLimit; this.ropeColor = ropeColor; this.bgColor1 = bgColor1; this.bgColor2 = bgColor2; this.anchorX = anchorX; this.anchorY = anchorY; this.candyX = candyX; this.candyY = candyY; this.omNomX = omNomX; this.omNomY = omNomY; this.starX = starX; this.starY = starY; this.lives = lives;
    }

    public static LevelData[] createAll() { return new LevelData[]{ level1(), level2(), level3(), level4(), level5() }; }

    private static final Color ROPE = new Color(0.37f,0.22f,0.10f,1);
    private static final Color BG1  = new Color(0.72f,0.47f,0.22f,1);
    private static final Color BG2  = new Color(0.92f,0.67f,0.34f,1);

    // ── NIVEL 1 ──────────────────────────────────────────────────────────────
    // Una sola cuerda vertical desde ancla arriba-centro.
    // Caramelo cuelga y al cortarla cae tocando 3 estrellas alineadas verticalmente.
    // Om Nom espera en el centro abajo.
    private static LevelData level1() {
        return new LevelData(0, "Nivel 1-1",
            "Desliza con el mouse para romper la soga. Toca las estrellas con el caramelo y logra que llegue a Om Nom.",
            0, ROPE, BG1, BG2,
            new float[]{400},
            new float[]{640},
            400, 560,
            400, 75,
            new float[]{400, 400, 400},
            new float[]{430, 300, 170},
            3);
    }

    // ── NIVEL 2 ──────────────────────────────────────────────────────────────
    // 2 anclas: una derecha-arriba (1) y una izquierda-arriba (2).
    // Cortar la derecha primero -> dulce balancea izquierda recogiendo estrella izq.
    // Cortar la segunda -> cae recogiendo estrellas restantes y llega a Om Nom (derecha abajo).
    private static LevelData level2() {
        return new LevelData(1, "Nivel 1-2",
            "Corta primero la cuerda derecha (1), luego la izquierda (2). Usa la fisica para recoger las estrellas y llegar a Om Nom.",
            0, ROPE, BG1, BG2,
            new float[]{580, 290},
            new float[]{590, 560},
            480, 470,
            570, 80,
            new float[]{240, 200, 300},
            new float[]{370, 270, 160},
            3);
    }

    // ── NIVEL 3 ──────────────────────────────────────────────────────────────
    // 6 cuerdas radiales (patron de arana) desde el candy al centro.
    // Cortar primero X azul (izq-arriba y der-arriba = paso 1),
    // luego X roja (der-media = paso 2).
    // Estrellas: der-media (azul), izq-abajo (verde), der-abajo (rojo).
    // Om Nom: abajo derecha.
    private static LevelData level3() {
        return new LevelData(2, "Nivel 1-7",
            "Corta primero las cuerdas con X azul (1), luego las rojas (2). Recoge las estrellas y lleva el dulce a Om Nom.",
            0, ROPE, BG1, BG2,
            new float[]{240, 400, 560, 220, 240, 395},
            new float[]{620, 640, 615, 400, 300, 280},
            400, 490,
            530, 80,
            new float[]{530, 255, 490},
            new float[]{390, 200, 185},
            3);
    }

    // ── NIVEL 4 ──────────────────────────────────────────────────────────────
    // 4 cuerdas. Hay puas en zona central-vertical (peligro).
    // Cortar cuerdas en orden: verde-arriba (1), morada-izq (2), soltar (3).
    // El dulce debe esquivar las puas y recoger estrellas laterales.
    // Om Nom: abajo centro-der.
    private static LevelData level4() {
        return new LevelData(3, "Nivel 1-16",
            "Aleja el dulce de las puas! Si toca una pua se rompe el dulce y pierdes el nivel.",
            0, ROPE, BG1, BG2,
            new float[]{260, 430, 310, 430},
            new float[]{625, 625, 450, 355},
            265, 510,
            490, 75,
            new float[]{510, 300, 480},
            new float[]{430, 310, 235},
            3);
    }

    // ── NIVEL 5 ──────────────────────────────────────────────────────────────
    // 4 cuerdas. Hay un bumper (elemento de rebote).
    // Orden: cortar cuerda izq-arriba (1), desactivar bumper der-arriba (2),
    // candy rebota hacia estrella (3), soltar ultima cuerda (4) -> Om Nom abajo.
    private static LevelData level5() {
        return new LevelData(4, "Nivel 1-18",
            "Corta en orden: cuerda izquierda (1), desactiva el bumper (2 y 3) y suelta la ultima cuerda (4) para llegar a Om Nom.",
            0, ROPE, BG1, BG2,
            new float[]{260, 530, 470, 220},
            new float[]{640, 615, 430, 300},
            390, 510,
            530, 75,
            new float[]{490, 440, 330},
            new float[]{435, 295, 145},
            3);
    }
}

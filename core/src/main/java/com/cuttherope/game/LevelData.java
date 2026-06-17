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
    public final float[] spikeX, spikeY;
    public final int[]   spikeDir;
    public final int lives;

    public LevelData(int levelIndex, String title, String hint, int timeLimit, Color ropeColor, Color bgColor1, Color bgColor2, float[] anchorX, float[] anchorY, float candyX, float candyY, float omNomX, float omNomY, float[] starX, float[] starY, int lives) {
        this(levelIndex, title, hint, timeLimit, ropeColor, bgColor1, bgColor2, anchorX, anchorY, candyX, candyY, omNomX, omNomY, starX, starY, new float[0], new float[0], new int[0], lives);
    }

    public LevelData(int levelIndex, String title, String hint, int timeLimit, Color ropeColor, Color bgColor1, Color bgColor2, float[] anchorX, float[] anchorY, float candyX, float candyY, float omNomX, float omNomY, float[] starX, float[] starY, float[] spikeX, float[] spikeY, int lives) {
        this(levelIndex, title, hint, timeLimit, ropeColor, bgColor1, bgColor2, anchorX, anchorY, candyX, candyY, omNomX, omNomY, starX, starY, spikeX, spikeY, new int[spikeX.length], lives);
    }

    public LevelData(int levelIndex, String title, String hint, int timeLimit, Color ropeColor, Color bgColor1, Color bgColor2, float[] anchorX, float[] anchorY, float candyX, float candyY, float omNomX, float omNomY, float[] starX, float[] starY, float[] spikeX, float[] spikeY, int[] spikeDir, int lives) {
        this.levelIndex = levelIndex; this.title = title; this.hint = hint; this.timeLimit = timeLimit; this.ropeColor = ropeColor; this.bgColor1 = bgColor1; this.bgColor2 = bgColor2; this.anchorX = anchorX; this.anchorY = anchorY; this.candyX = candyX; this.candyY = candyY; this.omNomX = omNomX; this.omNomY = omNomY; this.starX = starX; this.starY = starY; this.spikeX = spikeX; this.spikeY = spikeY; this.spikeDir = spikeDir; this.lives = lives;
    }

    public static LevelData[] createAll() { return new LevelData[]{ level1(), level2(), level3(), level4(), level5() }; }

    private static final Color ROPE = new Color(0.37f,0.22f,0.10f,1);
    private static final Color BG1  = new Color(0.72f,0.47f,0.22f,1);
    private static final Color BG2  = new Color(0.92f,0.67f,0.34f,1);


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


    private static LevelData level2() {
        return new LevelData(1, "Segundo Nivel",
            "Usa el balanceo del mockup: corta primero la soga derecha, toma las estrellas de la izquierda y luego deja caer el dulce hacia Om Nom.",
            0, ROPE, BG1, BG2,
            new float[]{360, 585, 365},
            new float[]{610, 610, 365},
            515, 455,
            550, 85,
            new float[]{380, 330, 455},
            new float[]{375, 280, 165},
            3);
    }

    private static LevelData level3() {
        return new LevelData(2, "Nivel 3",
            "Usa el balanceo del dulce. Corta las cuerdas correctas para conseguir las estrellas.",
            0, ROPE, BG1, BG2,
            new float[]{400, 220, 580, 400},
            new float[]{650, 520, 520, 300},
            400, 470,
            600, 100,
            new float[]{520, 250, 420},
            new float[]{350, 250, 160},
            3);
    }

    private static LevelData level4() {
        return new LevelData(3, "Cuarto Nivel",
            "Nivel 4 ajustado: púas más juntas, hitbox más perdonable y estrellas alineadas al recorrido para poder recoger las 3 en un intento.",
            0, ROPE, BG1, BG2,

            new float[]{185, 360, 390, 390},
            new float[]{620, 620, 500, 355},
            200, 500,
            400, 75,


            new float[]{250, 360, 392},
            new float[]{425, 300, 205},

            new float[]{325, 355, 425, 455, 305, 340, 440, 475},
            new float[]{350, 350, 350, 350, 205, 205, 205, 205},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0},
            3);
    }
    private static LevelData level5() {
        return new LevelData(4, "Quinto Nivel",
            "Nivel 5 basado en la nueva referencia: Om Nom abajo, sin burbuja, estrellas en línea recta y una ruta posible para recogerlas en una sola partida.",
            0, ROPE, BG1, BG2,

            new float[]{110, 95, 110, 690, 705, 690},
            new float[]{560, 355, 145, 560, 355, 145},
            400, 390,
            400, 75,

            new float[]{400, 400, 400},
            new float[]{295, 205, 150},

            new float[]{260, 260, 260, 260, 260, 260, 260, 540, 540, 540, 540, 540, 540, 540},
            new float[]{555, 505, 455, 405, 355, 305, 255, 555, 505, 455, 405, 355, 305, 255},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            3);
    }


}

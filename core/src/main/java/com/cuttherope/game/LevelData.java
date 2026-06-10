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
    private static final Color BG1 = new Color(0.72f,0.47f,0.22f,1);
    private static final Color BG2 = new Color(0.92f,0.67f,0.34f,1);

    private static LevelData level1() {
        return new LevelData(0, "Primer Nivel", "Corta la cuerda y recoge las 3 estrellas", 0, ROPE, BG1, BG2,
                new float[]{400}, new float[]{640}, 400, 500, 395, 70,
                new float[]{400, 400, 400}, new float[]{380, 275, 180}, 3);
    }

    private static LevelData level2() {
        return new LevelData(1, "Segundo Nivel", "Usa el balanceo antes de cortar", 0, ROPE, BG1, BG2,
                new float[]{520, 280, 392}, new float[]{610, 580, 350}, 430, 480, 575, 80,
                new float[]{270, 230, 310}, new float[]{380, 285, 190}, 3);
    }

    private static LevelData level3() {
        return new LevelData(2, "Tercer Nivel", "Corta las cuerdas en orden para formar la trayectoria", 0, ROPE, BG1, BG2,
                new float[]{270, 400, 530, 270, 395, 530}, new float[]{610, 630, 610, 355, 290, 395}, 400, 485, 540, 80,
                new float[]{515, 260, 485}, new float[]{375, 195, 185}, 3);
    }

    private static LevelData level4() {
        return new LevelData(3, "Cuarto Nivel", "Evita las púas y recoge las estrellas", 0, ROPE, BG1, BG2,
                new float[]{270, 420, 360, 330}, new float[]{620, 620, 335, 455}, 265, 500, 500, 75,
                new float[]{500, 330, 480}, new float[]{420, 300, 245}, 3);
    }

    private static LevelData level5() {
        return new LevelData(4, "Quinto Nivel", "Reto final: balanceo, púas y corte preciso", 0, ROPE, BG1, BG2,
                new float[]{285, 525, 465, 250}, new float[]{630, 610, 425, 300}, 400, 505, 540, 75,
                new float[]{475, 435, 355}, new float[]{425, 285, 145}, 3);
    }
}

package com.cuttherope.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;

public final class AssetPaths {
    private AssetPaths() {}

    public static final String FONDO = "Imagenes/Fondo.png";
    public static final String CANDY = "Imagenes/Candy.png";
    public static final String OMNOM = "Imagenes/OmNom.png";

    public static FileHandle file(String path) {
        FileHandle h = Gdx.files.internal(path);
        if (h.exists()) return h;
        h = Gdx.files.internal("assets/" + path);
        if (h.exists()) return h;
        h = Gdx.files.local(path);
        if (h.exists()) return h;
        h = Gdx.files.local("assets/" + path);
        if (h.exists()) return h;
        h = Gdx.files.local("src/main/java/" + path);
        if (h.exists()) return h;
        h = Gdx.files.local("src/main/resources/" + path);
        return h;
    }

    public static Texture texture(String path) {
        Texture t = new Texture(file(path));
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return t;
    }
} 

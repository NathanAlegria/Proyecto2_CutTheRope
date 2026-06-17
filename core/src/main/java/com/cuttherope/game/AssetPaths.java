package com.cuttherope.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;


public final class AssetPaths {
    public static final String FONDO = "Fondo.png";
    public static final String FONDO_MENU = "FondoMenu.png";
    public static final String FONDO_VS = "FondoVS.png";
    public static final String FONDO_AJUSTES = "FondoAjustes.png";
    public static final String CANDY = "Candy.png";

    public static final String OMNOM1 = "OmNom1.png";
    public static final String OMNOM2 = "OmNom2.png";
    public static final String OMNOM3 = "OmNom3.png";
    public static final String OMNOM4 = "OmNom4.png";
    public static final String OMNOM5 = "OmNom5.png";

    private AssetPaths() {}

    public static Texture texture(String fileName) {
        Texture t = textureOrNull(fileName);
        if (t == null) {
            throw new RuntimeException("No se encontró la imagen: " + fileName
                + " | Colócala en assets/Imagenes/ o src/main/java/Imagenes/");
        }
        return t;
    }

    public static Texture textureOrNull(String fileName) {
        return textureAnyOrNull(fileName);
    }

    public static Texture textureAnyOrNull(String... fileNames) {
        try {
            FileHandle fh = findAny(fileNames);
            if (fh == null || !fh.exists()) {
                System.err.println("[AssetPaths] No se encontró imagen: " + join(fileNames));
                return null;
            }
            return new Texture(fh);
        } catch (Exception e) {
            System.err.println("[AssetPaths] Error cargando imagen " + join(fileNames) + ": " + e.getMessage());
            return null;
        }
    }

    public static FileHandle findAny(String... fileNames) {
        for (String fileName : fileNames) {
            FileHandle fh = find(fileName);
            if (fh != null && fh.exists()) return fh;
        }
        return null;
    }

    public static FileHandle find(String fileName) {
        String[] names = variants(fileName);
        String[] roots = {
            "",
            "Imagenes/",
            "imagenes/",
            "assets/",
            "assets/Imagenes/",
            "assets/imagenes/",
            "../assets/",
            "../assets/Imagenes/",
            "../assets/imagenes/",
            "../../assets/",
            "../../assets/Imagenes/",
            "../../assets/imagenes/",
            "core/assets/",
            "core/assets/Imagenes/",
            "core/assets/imagenes/",
            "../core/assets/",
            "../core/assets/Imagenes/",
            "../core/assets/imagenes/",
            "lwjgl3/assets/",
            "lwjgl3/assets/Imagenes/",
            "lwjgl3/assets/imagenes/",
            "../lwjgl3/assets/",
            "../lwjgl3/assets/Imagenes/",
            "../lwjgl3/assets/imagenes/",
            "src/main/java/Imagenes/",
            "src/main/java/imagenes/"
        };

        for (String root : roots) {
            for (String name : names) {
                String p = root + name;
                try {
                    FileHandle internal = Gdx.files.internal(p);
                    if (internal.exists()) return internal;
                } catch (Exception ignored) {}
                try {
                    FileHandle local = Gdx.files.local(p);
                    if (local.exists()) return local;
                } catch (Exception ignored) {}
            }
        }
        return null;
    }


    public static String omNomFileForAvatar(String avatarId) {
        int n = 1;
        if (avatarId != null && avatarId.startsWith("avatar")) {
            try { n = Integer.parseInt(avatarId.substring(6)); } catch (Exception ignored) {}
        }
        if (n < 1 || n > 5) n = 1;
        return "OmNom" + n + ".png";
    }

    private static String[] variants(String fileName) {
        String base = fileName;
        String ext = "";
        int dot = fileName.lastIndexOf('.');
        if (dot > 0) {
            base = fileName.substring(0, dot);
            ext = fileName.substring(dot);
        }

        String lowerBase = base.toLowerCase();
        String upperFirst = base.length() > 0
            ? Character.toUpperCase(base.charAt(0)) + base.substring(1)
            : base;


        if (ext.length() == 0) {
            return new String[] {
                base,
                base + ".png",
                base + ".jpg",
                base + ".jpeg",
                base + ".PNG",
                base + ".JPG",
                base + ".JPEG",
                lowerBase,
                lowerBase + ".png",
                lowerBase + ".jpg",
                lowerBase + ".jpeg",
                upperFirst,
                upperFirst + ".png",
                upperFirst + ".jpg",
                upperFirst + ".jpeg"
            };
        }

        return new String[] {
            fileName,
            base + ext.toLowerCase(),
            base + ext.toUpperCase(),
            lowerBase + ext.toLowerCase(),
            upperFirst + ext.toLowerCase(),
            base.replace(" ", "") + ext.toLowerCase(),
            lowerBase.replace(" ", "") + ext.toLowerCase()
        };
    }

    private static String join(String... items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(items[i]);
        }
        return sb.toString();
    }
}

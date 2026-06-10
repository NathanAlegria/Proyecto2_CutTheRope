package com.cuttherope.game;

import java.io.*;

public class UsuarioDAO {
    private static final String BASE_DIR = "usuario";

    public UsuarioDAO() {
        new File(BASE_DIR).mkdirs();
    }

    public void guardarUsuario(Usuario usuario) {
        File carpeta = carpetaUsuario(usuario.getNombreUsuario());
        carpeta.mkdirs();
        File archivo = new File(carpeta, "usuario.dat");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(archivo))) {
            out.writeObject(usuario);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el usuario", e);
        }
    }

    public Usuario leerUsuario(String nombreUsuario) {
        File archivo = new File(carpetaUsuario(nombreUsuario), "usuario.dat");
        if (!archivo.exists()) return null;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(archivo))) {
            return (Usuario) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("No se pudo leer el usuario", e);
        }
    }

    public boolean existeUsuario(String nombreUsuario) {
        return new File(carpetaUsuario(nombreUsuario), "usuario.dat").exists();
    }

    public File carpetaUsuario(String nombreUsuario) {
        String limpio = nombreUsuario == null ? "" : nombreUsuario.trim().toLowerCase();
        return new File(BASE_DIR, limpio);
    }
}

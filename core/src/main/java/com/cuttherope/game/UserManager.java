/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cuttherope.game;

/**
 *
 * @author Nathan
 */
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * UserManager - Singleton que gestiona registro, login y persistencia
 * de usuarios en archivos binarios (serialización Java).
 *
 * Estructura de carpetas:
 *   data/users/<username>/profile.bin   ← UserData serializado
 *   data/users/<username>/prefs.bin     ← preferencias extra (futuro uso)
 */
public class UserManager {

    private static UserManager instance;

    private static final String BASE_DIR = "data/users/";

    private UserData currentUser;               // usuario en sesión
    private Map<String, Integer> rankingCache;  // username → score

    // ── Singleton ────────────────────────────────────────────────────────────
    private UserManager() {
        rankingCache = new LinkedHashMap<>();
        new File(BASE_DIR).mkdirs();
    }

    public static UserManager getInstance() {
        if (instance == null) instance = new UserManager();
        return instance;
    }

    // ── Registro ─────────────────────────────────────────────────────────────

    /**
     * Registra un nuevo usuario.
     * @return null si OK, mensaje de error si falla.
     */
    public String register(String username, String password, String fullName) {
        // Validaciones
        if (username == null || username.trim().isEmpty())
            return "El nombre de usuario no puede estar vacío.";
        if (username.length() < 3)
            return "El nombre de usuario debe tener al menos 3 caracteres.";
        if (userExists(username))
            return "El nombre de usuario ya está registrado.";

        String pwErr = validatePassword(password);
        if (pwErr != null) return pwErr;

        if (fullName == null || fullName.trim().isEmpty())
            return "El nombre completo no puede estar vacío.";

        // Crear y guardar
        UserData ud = new UserData(username.trim().toLowerCase(),
                                   hashPassword(password),
                                   fullName.trim());
        saveUser(ud);
        return null; // éxito
    }

    /**
     * Valida requisitos de contraseña.
     * Retorna null si es válida, mensaje de error si no.
     */
    public String validatePassword(String password) {
        if (password == null || password.length() < 8)
            return "La contraseña debe tener al menos 8 caracteres.";
        boolean hasUpper   = false, hasLower = false,
                hasDigit   = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c))     hasDigit = true;
            else                               hasSpecial = true;
        }
        if (!hasUpper)   return "La contraseña debe contener al menos una mayúscula.";
        if (!hasLower)   return "La contraseña debe contener al menos una minúscula.";
        if (!hasDigit)   return "La contraseña debe contener al menos un número.";
        if (!hasSpecial) return "La contraseña debe contener al menos un carácter especial.";
        return null;
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    /**
     * Inicia sesión.
     * @return null si OK, mensaje de error si falla.
     */
    public String login(String username, String password) {
        if (username == null || password == null)
            return "Credenciales inválidas.";

        UserData ud = loadUser(username.trim().toLowerCase());
        if (ud == null)
            return "Usuario no encontrado.";

        if (!ud.getPasswordHash().equals(hashPassword(password)))
            return "Contraseña incorrecta.";

        ud.setLastLoginDate(new Date());
        saveUser(ud);
        currentUser = ud;
        return null; // éxito
    }

    public void logout() {
        if (currentUser != null) {
            saveUser(currentUser);
            currentUser = null;
        }
    }

    // ── Persistencia ──────────────────────────────────────────────────────────

    public void saveUser(UserData ud) {
        File dir = new File(BASE_DIR + ud.getUsername());
        dir.mkdirs();
        File file = new File(dir, "profile.bin");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(ud);
        } catch (IOException e) {
            System.err.println("Error al guardar usuario: " + e.getMessage());
        }
    }

    public UserData loadUser(String username) {
        File file = new File(BASE_DIR + username + "/profile.bin");
        if (!file.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (UserData) ois.readObject();
        } catch (Exception e) {
            System.err.println("Error al cargar usuario: " + e.getMessage());
            return null;
        }
    }

    public boolean userExists(String username) {
        return new File(BASE_DIR + username.trim().toLowerCase() + "/profile.bin").exists();
    }

    /** Guarda el progreso del usuario actual tras una partida. */
    public void saveCurrentUser() {
        if (currentUser != null) saveUser(currentUser);
    }

    // ── Ranking ───────────────────────────────────────────────────────────────

    /** Carga todos los usuarios y devuelve lista ordenada por score descendente. */
    public List<UserData> getRanking() {
        List<UserData> list = new ArrayList<>();
        File base = new File(BASE_DIR);
        if (!base.exists()) return list;

        for (File userDir : base.listFiles(File::isDirectory)) {
            UserData ud = loadUser(userDir.getName());
            if (ud != null) list.add(ud);
        }

        list.sort((a, b) -> Integer.compare(b.getTotalScore(), a.getTotalScore()));
        return list;
    }

    // ── Hash ─────────────────────────────────────────────────────────────────

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            // fallback inseguro, solo para desarrollo
            return Integer.toHexString(password.hashCode());
        }
    }

    // ── Getter ────────────────────────────────────────────────────────────────
    public UserData getCurrentUser() { return currentUser; }
    public boolean  isLoggedIn()     { return currentUser != null; }
}

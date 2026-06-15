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
 *   usuario/<nombreUsuario>/usuario.dat   ← UserData serializado en binario
 *
 * Ejemplo:
 *   usuario/jeremias/usuario.dat
 */
public class UserManager {

    private static UserManager instance;

    private static final String BASE_DIR = "usuario/";

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
        File file = new File(dir, "usuario.dat");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(ud);
        } catch (IOException e) {
            System.err.println("Error al guardar usuario: " + e.getMessage());
        }
    }

    public UserData loadUser(String username) {
        File file = new File(BASE_DIR + username + "/usuario.dat");
        if (!file.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (UserData) ois.readObject();
        } catch (Exception e) {
            System.err.println("Error al cargar usuario: " + e.getMessage());
            return null;
        }
    }

    public boolean userExists(String username) {
        return new File(BASE_DIR + username.trim().toLowerCase() + "/usuario.dat").exists();
    }

    /** Guarda el progreso del usuario actual tras una partida. */
    public void saveCurrentUser() {
        if (currentUser != null) saveUser(currentUser);
    }

    // ── Ranking ───────────────────────────────────────────────────────────────

    /** Carga todos los usuarios y devuelve lista ordenada por score descendente. */
    public List<UserData> getRanking() {
        List<UserData> list = getAllUsers();
        list.sort((a, b) -> Integer.compare(b.getTotalScore(), a.getTotalScore()));
        return list;
    }


    // ── Social / amistades ───────────────────────────────────────────────────

    public List<UserData> getAllUsers() {
        List<UserData> users = new ArrayList<>();
        File base = new File(BASE_DIR);
        File[] dirs = base.listFiles(File::isDirectory);
        loadUsersRecursive(dirs, 0, users); // Recursividad #1: carga de usuarios
        return users;
    }

    private void loadUsersRecursive(File[] dirs, int index, List<UserData> out) {
        if (dirs == null || index >= dirs.length) return;
        UserData ud = loadUser(dirs[index].getName());
        if (ud != null) out.add(ud);
        loadUsersRecursive(dirs, index + 1, out);
    }

    public FriendshipStatus getFriendshipStatus(String current, String target) {
        current = clean(current);
        target = clean(target);
        if (current.equals(target)) return FriendshipStatus.SELF;

        UserData me = loadUser(current);
        if (me == null) return FriendshipStatus.NONE;
        me.ensureSocialLists();

        if (containsRecursive(me.getFriends(), target, 0)) return FriendshipStatus.FRIEND; // Recursividad #2: búsqueda en lista
        if (containsRecursive(me.getSentFriendRequests(), target, 0)) return FriendshipStatus.SENT;
        if (containsRecursive(me.getPendingFriendRequests(), target, 0)) return FriendshipStatus.RECEIVED;
        return FriendshipStatus.NONE;
    }

    private boolean containsRecursive(List<String> list, String value, int index) {
        if (list == null || index >= list.size()) return false;
        if (value.equalsIgnoreCase(list.get(index))) return true;
        return containsRecursive(list, value, index + 1);
    }

    public String sendFriendRequest(String from, String to) {
        from = clean(from);
        to = clean(to);
        if (from.equals(to)) return "No puedes enviarte solicitud a ti mismo.";

        UserData sender = loadUser(from);
        UserData receiver = loadUser(to);
        if (sender == null || receiver == null) return "Jugador no encontrado.";
        sender.ensureSocialLists();
        receiver.ensureSocialLists();

        FriendshipStatus status = getFriendshipStatus(from, to);
        if (status == FriendshipStatus.FRIEND) return "Ya son amigos.";
        if (status == FriendshipStatus.SENT) return "Solicitud pendiente.";
        if (status == FriendshipStatus.RECEIVED) return acceptFriendRequest(from, to);

        sender.getSentFriendRequests().add(to);
        receiver.getPendingFriendRequests().add(from);
        saveUser(sender);
        saveUser(receiver);
        refreshCurrentUser(sender, receiver);
        return "Solicitud enviada a " + to + ".";
    }

    public String acceptFriendRequest(String current, String requester) {
        current = clean(current);
        requester = clean(requester);

        UserData me = loadUser(current);
        UserData other = loadUser(requester);
        if (me == null || other == null) return "Jugador no encontrado.";
        me.ensureSocialLists();
        other.ensureSocialLists();

        if (!me.getPendingFriendRequests().remove(requester)) {
            return "No hay solicitud pendiente de " + requester + ".";
        }

        other.getSentFriendRequests().remove(current);
        me.addFriend(requester);
        other.addFriend(current);
        saveUser(me);
        saveUser(other);
        refreshCurrentUser(me, other);
        return "Ahora eres amigo de " + requester + ".";
    }

    public List<UserData> getFriendsFor(String username) {
        username = clean(username);
        UserData me = loadUser(username);
        List<UserData> friends = new ArrayList<>();
        if (me == null) return friends;
        me.ensureSocialLists();
        addFriendsRecursive(me.getFriends(), 0, friends);
        return friends;
    }

    private void addFriendsRecursive(List<String> names, int index, List<UserData> out) {
        if (names == null || index >= names.size()) return;
        UserData friend = loadUser(names.get(index));
        if (friend != null) out.add(friend);
        addFriendsRecursive(names, index + 1, out);
    }

    public VersusResult compareVersus(String playerA, String playerB, int level) {
        UserData a = loadUser(clean(playerA));
        UserData b = loadUser(clean(playerB));
        if (a == null || b == null) {
            return new VersusResult(playerA, playerB, level, 0, 0, 0, 0, "Sin ganador", "Jugador no encontrado");
        }

        int starsA = a.getStarsForLevel(level);
        int starsB = b.getStarsForLevel(level);
        long timeA = a.getBestTimeForLevel(level);
        long timeB = b.getBestTimeForLevel(level);

        if (starsA > starsB) return new VersusResult(a.getUsername(), b.getUsername(), level, starsA, starsB, timeA, timeB, a.getUsername(), "Ganó por más estrellas");
        if (starsB > starsA) return new VersusResult(a.getUsername(), b.getUsername(), level, starsA, starsB, timeA, timeB, b.getUsername(), "Ganó por más estrellas");

        if (timeA > 0 && (timeB == 0 || timeA < timeB)) return new VersusResult(a.getUsername(), b.getUsername(), level, starsA, starsB, timeA, timeB, a.getUsername(), "Empate en estrellas: ganó por mejor tiempo");
        if (timeB > 0 && (timeA == 0 || timeB < timeA)) return new VersusResult(a.getUsername(), b.getUsername(), level, starsA, starsB, timeA, timeB, b.getUsername(), "Empate en estrellas: ganó por mejor tiempo");

        return new VersusResult(a.getUsername(), b.getUsername(), level, starsA, starsB, timeA, timeB, "Empate", "Mismas estrellas y mismo tiempo");
    }

    private void refreshCurrentUser(UserData a, UserData b) {
        if (currentUser == null) return;
        if (currentUser.getUsername().equals(a.getUsername())) currentUser = a;
        if (currentUser.getUsername().equals(b.getUsername())) currentUser = b;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim().toLowerCase();
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

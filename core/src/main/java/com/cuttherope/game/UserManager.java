

package com.cuttherope.game;


import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


public class UserManager {

    private static UserManager instance;

    private static final String BASE_DIR = "usuario/";
    private static final String VS_DIR = BASE_DIR + "vs/";
    private String sessionToken;
    private static final long ONLINE_LOCK_TIMEOUT_MS = 45L * 1000L;
    private static final long VS_REQUEST_TIMEOUT_MS = 60L * 1000L;

    private UserData currentUser;
    private Map<String, Integer> rankingCache;


    private UserManager() {
        rankingCache = new LinkedHashMap<>();
        new File(BASE_DIR).mkdirs();
        new File(VS_DIR).mkdirs();


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { logout(); } catch (Exception ignored) {}
        }));
    }

    public static UserManager getInstance() {
        if (instance == null) instance = new UserManager();
        return instance;
    }


    public String register(String username, String password, String fullName) {

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


        UserData ud = new UserData(username.trim().toLowerCase(),
                                   hashPassword(password),
                                   fullName.trim());
        saveUser(ud);
        return null;
    }


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


    public String login(String username, String password) {
        if (username == null || password == null)
            return "Credenciales inválidas.";

        UserData ud = loadUser(username.trim().toLowerCase());
        if (ud == null)
            return "Usuario no encontrado.";

        if (!ud.getPasswordHash().equals(hashPassword(password)))
            return "Contraseña incorrecta.";

        if (isUserOnline(ud.getUsername()))
            return "Jugador en línea. Este usuario ya tiene una sesión abierta.";

        sessionToken = createOnlineLock(ud.getUsername());
        ud.setLastLoginDate(new Date());
        saveUser(ud);
        currentUser = ud;
        return null;
    }

    public void logout() {
        if (currentUser != null) {
            saveUser(currentUser);
            removeOnlineLock(currentUser.getUsername());
            currentUser = null;
            sessionToken = null;
        }
    }


    private File onlineFile(String username) {
        return new File(BASE_DIR + clean(username), "online.lock");
    }

    public boolean isUserOnline(String username) {
        File f = onlineFile(username);
        if (!f.exists()) return false;


        long age = System.currentTimeMillis() - f.lastModified();
        if (age > ONLINE_LOCK_TIMEOUT_MS) {
            f.delete();
            return false;
        }
        return true;
    }


    public void heartbeatOnlineSession() {
        if (currentUser == null || sessionToken == null) return;
        File f = onlineFile(currentUser.getUsername());
        if (!f.exists()) return;
        f.setLastModified(System.currentTimeMillis());
    }

    private String createOnlineLock(String username) {
        String token = UUID.randomUUID().toString();
        File f = onlineFile(username);
        f.getParentFile().mkdirs();
        try (FileWriter fw = new FileWriter(f, false)) {
            fw.write(token + "\n" + System.currentTimeMillis());
        } catch (IOException e) {
            System.err.println("No se pudo crear online.lock: " + e.getMessage());
        }
        return token;
    }

    private void removeOnlineLock(String username) {
        File f = onlineFile(username);
        if (!f.exists()) return;


        if (sessionToken != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String tokenFile = br.readLine();
                if (tokenFile != null && !tokenFile.equals(sessionToken)) return;
            } catch (IOException ignored) {}
        }
        f.delete();
    }


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


    public void saveCurrentUser() {
        if (currentUser != null) saveUser(currentUser);
    }


    public List<UserData> getRanking() {
        List<UserData> list = getAllUsers();
        list.sort((a, b) -> Integer.compare(b.getTotalScore(), a.getTotalScore()));
        return list;
    }


    public List<UserData> getAllUsers() {
        List<UserData> users = new ArrayList<>();
        File base = new File(BASE_DIR);
        File[] dirs = base.listFiles(File::isDirectory);
        loadUsersRecursive(dirs, 0, users);
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

        if (containsRecursive(me.getFriends(), target, 0)) return FriendshipStatus.FRIEND;
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


    private File versusFile(String id) { return new File(VS_DIR, id + ".dat"); }

    public String createVersusRequest(String opponent) {
        if (currentUser == null) return "No hay jugador activo.";
        String me = clean(currentUser.getUsername());
        String other = clean(opponent);
        if (me.equals(other)) return "No puedes jugar VS contra ti mismo.";
        if (getFriendshipStatus(me, other) != FriendshipStatus.FRIEND) return "Solo puedes retar a tus amigos.";
        String id = me + "_vs_" + other + "_" + System.currentTimeMillis();
        VersusMatch m = new VersusMatch(id, me, other);
        saveVersusMatch(m);
        return "Solicitud de partida VS enviada a " + other + ".";
    }

    public String acceptVersusRequest(String matchId) {
        if (currentUser == null) return "No hay jugador activo.";
        VersusMatch m = loadVersusMatch(matchId);
        if (m == null) return "La solicitud VS no existe.";
        if (!m.opponent.equalsIgnoreCase(currentUser.getUsername())) return "Solo el amigo retado puede aceptar.";
        m.state = VersusMatch.State.ACCEPTED;
        m.requesterReady = true;
        m.opponentReady = false;
        m.updatedAt = new Date();
        saveVersusMatch(m);
        return "Partida VS aceptada. Presiona Listo para habilitar Jugar al retador.";
    }

    public String setVersusReady(String matchId) {
        if (currentUser == null) return "No hay jugador activo.";
        VersusMatch m = loadVersusMatch(matchId);
        if (m == null) return "La partida VS no existe.";
        if (!m.includes(currentUser.getUsername())) return "No perteneces a esta partida VS.";
        if (m.state == VersusMatch.State.REQUESTED) return "Primero el amigo debe aceptar la solicitud VS.";
        if (m.opponent.equalsIgnoreCase(currentUser.getUsername())) {
            m.requesterReady = true;
            m.opponentReady = true;
            m.state = VersusMatch.State.BOTH_READY;
            m.updatedAt = new Date();
        } else {
            m.setReady(currentUser.getUsername());
        }
        saveVersusMatch(m);
        return m.hasBothReady() ? "Listo. El retador ya puede presionar Jugar." : "Listo marcado. Esperando al otro jugador.";
    }

    public void recordVersusLevel(String matchId, String username, int level, int stars, long timeMs) {
        VersusMatch m = loadVersusMatch(matchId);
        if (m == null) return;
        m.recordLevel(clean(username), level, stars, timeMs);
        saveVersusMatch(m);
        if (m.state == VersusMatch.State.FINISHED && !m.statsUpdated) updateVersusStats(m);
    }

    private void updateVersusStats(VersusMatch m) {
        UserData a = loadUser(m.requester);
        UserData b = loadUser(m.opponent);
        int winnerStars = "Empate".equalsIgnoreCase(m.winner) ? Math.max(m.totalStars(m.requester), m.totalStars(m.opponent)) : m.totalStars(m.winner);
        long winnerTime = "Empate".equalsIgnoreCase(m.winner) ? Math.min(m.totalTime(m.requester), m.totalTime(m.opponent)) : m.totalTime(m.winner);
        if (a != null) { a.recordVersus(m.opponent, m.winner, m.totalStars(m.requester), m.totalTime(m.requester), m.reason, winnerStars, winnerTime, 15); saveUser(a); }
        if (b != null) { b.recordVersus(m.requester, m.winner, m.totalStars(m.opponent), m.totalTime(m.opponent), m.reason, winnerStars, winnerTime, 15); saveUser(b); }
        m.statsUpdated = true;
        saveVersusMatch(m);
        if (currentUser != null) { if (currentUser.getUsername().equalsIgnoreCase(m.requester)) currentUser = a; if (currentUser.getUsername().equalsIgnoreCase(m.opponent)) currentUser = b; }
    }

    public String startVersusMatch(String matchId) {
        if (currentUser == null) return "No hay jugador activo.";
        VersusMatch m = loadVersusMatch(matchId);
        if (m == null) return "La partida VS no existe.";
        if (!m.requester.equalsIgnoreCase(currentUser.getUsername())) return "Solo el jugador que envió el reto puede iniciar.";
        if (!m.hasBothReady()) return "La persona retada todavía no está lista.";
        m.state = VersusMatch.State.IN_PROGRESS;
        m.updatedAt = new Date();
        saveVersusMatch(m);
        return "Partida VS iniciada.";
    }

    private boolean deleteExpiredVersusIfNeeded(VersusMatch m) {
        if (m == null) return true;
        if (m.isExpiredRequest(System.currentTimeMillis(), VS_REQUEST_TIMEOUT_MS)) {
            File f = versusFile(m.id);
            if (f.exists()) f.delete();
            return true;
        }
        return false;
    }

    public void saveVersusMatch(VersusMatch m) {
        new File(VS_DIR).mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(versusFile(m.id)))) {
            oos.writeObject(m);
        } catch (IOException e) {
            System.err.println("Error guardando VS: " + e.getMessage());
        }
    }

    public VersusMatch loadVersusMatch(String id) {
        if (id == null) return null;
        File f = versusFile(id);
        if (!f.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (VersusMatch) ois.readObject();
        } catch (Exception e) {
            System.err.println("Error cargando VS: " + e.getMessage());
            return null;
        }
    }

    public List<VersusMatch> getVersusMatchesFor(String username) {
        List<VersusMatch> out = new ArrayList<>();
        File[] files = new File(VS_DIR).listFiles((dir, name) -> name.endsWith(".dat"));
        if (files == null) return out;
        for (File f : files) {
            String id = f.getName().substring(0, f.getName().length() - 4);
            VersusMatch m = loadVersusMatch(id);
            if (deleteExpiredVersusIfNeeded(m)) continue;
            if (m != null && m.includes(clean(username))) out.add(m);
        }
        out.sort((a,b) -> b.updatedAt.compareTo(a.updatedAt));
        return out;
    }


    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {

            return Integer.toHexString(password.hashCode());
        }
    }


    public UserData getCurrentUser() { return currentUser; }
    public boolean  isLoggedIn()     { return currentUser != null; }
}

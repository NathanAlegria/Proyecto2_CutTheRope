package com.cuttherope.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cuttherope.game.UserManager;
import com.cuttherope.game.AudioManager;
import com.cuttherope.game.SplashScreen;

/**
 * MainGame - Clase principal del juego Cut the Rope.
 * Extiende Game (ApplicationListener) de libGDX.
 * Gestiona recursos globales compartidos entre pantallas.
 */
public class MainGame extends Game {

    // Recursos globales
    public SpriteBatch        batch;
    public BitmapFont         font;
    public BitmapFont         fontLarge;
    public BitmapFont         fontSmall;
    public OrthographicCamera camera;
    public FitViewport        viewport;

    // Managers singleton
    public UserManager userManager;
    public AudioManager audioManager;

    // Dimensiones de la ventana virtual
    public static final int VIRTUAL_WIDTH  = 800;
    public static final int VIRTUAL_HEIGHT = 700;

    // Versión del juego
    public static final String VERSION = "1.0";

    // ── Sistema de traducción simple ─────────────────────────────────────────
    private static java.util.Map<String,String> translations = new java.util.HashMap<>();

    /** Retorna el texto traducido al idioma activo del usuario. */
    public static String t(String key) {
        String v = translations.get(key);
        return v != null ? v : key;
    }

    /** Carga las traducciones según el idioma. */
    public static void loadLang(String lang) {
        translations.clear();
        if ("en".equals(lang)) {
            translations.put("Estadísticas",    "Statistics");
            translations.put("Ajustes",         "Settings");
            translations.put("Cerrar sesión",   "Log out");
            translations.put("Ranking",         "Ranking");
            translations.put("Selecciona un nivel:", "Select a level:");
            translations.put("Partidas:",       "Games:");
            translations.put("Estrellas:",      "Stars:");
            translations.put("Puntos:",         "Points:");
            translations.put("Audio",           "Audio");
            translations.put("Música:",         "Music:");
            translations.put("Efectos:",        "Effects:");
            translations.put("Preferencias",    "Preferences");
            translations.put("Idioma:",         "Language:");
            translations.put("Timer en pantalla:", "Show timer:");
            translations.put("Avatar",          "Avatar");
            translations.put("← Menú",          "← Menu");
            translations.put("Menú",            "Menu");
            translations.put("Iniciar sesión",  "Log in");
            translations.put("Registro",        "Register");
            translations.put("→ Crear cuenta",  "→ Create account");
            translations.put("← Iniciar sesión","← Log in");
            translations.put("Registrarse",     "Register");
            translations.put("Usuario:",        "Username:");
            translations.put("Contraseña:",     "Password:");
            translations.put("Confirmar:",      "Confirm:");
            translations.put("Nombre completo:","Full name:");
            translations.put("Ocultar",         "Hide");
            translations.put("Ver",             "Show");
            translations.put("Reintentar",      "Retry");
            translations.put("Menú principal",  "Main menu");
            translations.put("Siguiente nivel", "Next level");
            translations.put("Vidas:",          "Lives:");
            translations.put("Nivel",           "Level");
            translations.put("Est:",            "Stars:");
            translations.put("Tiempo:",         "Time:");
            translations.put("Pts:",            "Pts:");
            translations.put("Pausa",           "Pause");
            translations.put("PAUSA",           "PAUSE");
            translations.put("Play",            "Play");
            translations.put("Continuar",       "Continue");
            translations.put("Toca ► para continuar", "Tap ► to continue");
            translations.put("¡NIVEL COMPLETO!", "LEVEL COMPLETE!");
            translations.put("¡¡JUEGO COMPLETADO!!", "GAME COMPLETED!!");
            translations.put("¡FALLASTE!",      "YOU FAILED!");
            translations.put("SIN VIDAS",       "NO LIVES LEFT");
            translations.put("Recogidas:",      "Collected:");
            translations.put("Ranking Global",  "Global Ranking");
            translations.put("Historial de partidas:", "Game history:");
            translations.put("Progreso por nivel:", "Level progress:");
            translations.put("Bloqueado",       "Locked");
            translations.put("Jugador:",        "Player:");
            translations.put("No hay usuario activo.", "No active user.");
            translations.put("Registro:",       "Registered:");
            translations.put("Última sesión:",  "Last login:");
            translations.put("Tiempo total:",   "Total time:");
            translations.put("Mejor:",          "Best:");
            translations.put("Int:",            "Att:");
            translations.put("Nivel ",          "Level ");
            translations.put("Victoria",        "Win");
            translations.put("Derrota",         "Loss");
            translations.put("Aún no has jugado ninguna partida.", "You have not played any game yet.");
            translations.put("No hay jugadores registrados todavía.", "There are no registered players yet.");
            translations.put("Usuario",         "Username");
            translations.put("Nombre",          "Name");
            translations.put("Partidas",        "Games");
            translations.put("Español",         "Spanish");
            translations.put("English",         "English");
            translations.put("ON",              "ON");
            translations.put("OFF",             "OFF");
            translations.put("Volumen",         "Volume");
            translations.put("Activado",        "Enabled");
            translations.put("Desactivado",     "Disabled");
            translations.put("Cargando",        "Loading");

            // Login / registro / validaciones
            translations.put("Las contraseñas no coinciden.", "Passwords do not match.");
            translations.put("¡Cuenta creada! Inicia sesión.", "Account created! Log in.");
            translations.put("El nombre de usuario no puede estar vacío.", "Username cannot be empty.");
            translations.put("El nombre de usuario debe tener al menos 3 caracteres.", "Username must have at least 3 characters.");
            translations.put("El nombre de usuario ya está registrado.", "Username is already registered.");
            translations.put("La contraseña debe tener al menos 8 caracteres.", "Password must have at least 8 characters.");
            translations.put("La contraseña debe contener al menos una mayúscula.", "Password must contain at least one uppercase letter.");
            translations.put("La contraseña debe contener al menos una minúscula.", "Password must contain at least one lowercase letter.");
            translations.put("La contraseña debe contener al menos un número.", "Password must contain at least one number.");
            translations.put("La contraseña debe contener al menos un carácter especial.", "Password must contain at least one special character.");
            translations.put("El nombre completo no puede estar vacío.", "Full name cannot be empty.");
            translations.put("Credenciales inválidas.", "Invalid credentials.");
            translations.put("Usuario no encontrado.", "User not found.");
            translations.put("Contraseña incorrecta.", "Incorrect password.");
            translations.put("8+ caracteres", "8+ characters");
            translations.put("Mayúscula", "Uppercase");
            translations.put("Minúscula", "Lowercase");
            translations.put("Número", "Number");
            translations.put("Especial", "Special");

            // Niveles
            translations.put("¡Empieza aquí!", "Start here!");
            translations.put("Corta la cuerda para alimentar a Om Nom", "Cut the rope to feed Om Nom");
            translations.put("Doble desafío", "Double challenge");
            translations.put("Corta las cuerdas en el orden correcto", "Cut the ropes in the correct order");
            translations.put("Triple amenaza", "Triple threat");
            translations.put("Recoge todas las estrellas antes de cortar", "Collect all stars before cutting");
            translations.put("El laberinto", "The maze");
            translations.put("Las estrellas están en lugares difíciles", "The stars are in difficult places");
            translations.put("¡Reto final!", "Final challenge!");
            translations.put("¡Demuestra que eres el maestro del corte!", "Show that you are the cutting master!");
        }
        // si es "es" el mapa queda vacío → t() devuelve la clave que ya está en español
    }

    /** Aplica idioma y audio del usuario activo en tiempo real. */
    public void applyRuntimePreferences(UserData ud) {
        if (ud == null) return;
        loadLang(ud.getLanguage());
        audioManager.applyUserPrefs(ud);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Cámara y viewport virtual
        camera   = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply(true);
        batch.setProjectionMatrix(camera.combined);

        // Fuentes básicas usando BitmapFont por defecto
        font      = new BitmapFont();
        fontLarge = new BitmapFont();
        fontLarge.getData().setScale(2.5f);
        fontSmall = new BitmapFont();
        fontSmall.getData().setScale(0.85f);

        // Inicializar managers
        userManager  = UserManager.getInstance();
        audioManager = AudioManager.getInstance();

        // Idioma inicial por defecto
        loadLang("es");

        // Ir a la pantalla inicial
        setScreen(new SplashScreen(this));
    }

    @Override
    public void render() {
        super.render();
        if (userManager != null) {
            userManager.heartbeatOnlineSession();
        }
    }

    @Override
    public void dispose() {
        if (userManager != null) {
            userManager.logout();
        }
        super.dispose();
        batch.dispose();
        font.dispose();
        fontLarge.dispose();
        fontSmall.dispose();
        audioManager.dispose();
    }
}
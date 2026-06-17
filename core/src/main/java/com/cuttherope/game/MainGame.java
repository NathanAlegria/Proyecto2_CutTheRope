package com.cuttherope.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cuttherope.game.UserManager;
import com.cuttherope.game.AudioManager;
import com.cuttherope.game.SplashScreen;


public class MainGame extends Game {


    public SpriteBatch        batch;
    public BitmapFont         font;
    public BitmapFont         fontLarge;
    public BitmapFont         fontSmall;
    public OrthographicCamera camera;
    public FitViewport        viewport;


    public UserManager userManager;
    public AudioManager audioManager;


    public static final int VIRTUAL_WIDTH  = 800;
    public static final int VIRTUAL_HEIGHT = 700;
    public static final int WINDOW_WIDTH   = 1000;
    public static final int WINDOW_HEIGHT  = 1200;


    public static final String VERSION = "1.0";


    private static java.util.Map<String,String> translations = new java.util.HashMap<>();


    public static String t(String key) {
        String v = translations.get(key);
        return v != null ? v : key;
    }


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
            translations.put("Mapa de niveles", "Level map");
            translations.put("Selecciona un nivel", "Select a level");
            translations.put("VS", "VS");

            translations.put("Historial normal:", "Normal game history:");
            translations.put("Historial de partidas VS:", "VS game history:");
            translations.put("VS jugados:", "VS played:");
            translations.put("Ganados:", "Won:");
            translations.put("Perdidos:", "Lost:");
            translations.put("Aún no tienes partidas VS terminadas.", "You do not have finished VS matches yet.");
            translations.put("Empate", "Draw");
            translations.put("sin ganador", "no winner");
            translations.put("Empate contra", "Draw against");
            translations.put("le ganó a", "defeated");
            translations.put("Fecha:", "Date:");
            translations.put("Sistema VS y Amistades", "VS and Friends System");
            translations.put("Envía solicitudes, acepta amigos y reta en una partida normal del nivel 1 al 5.", "Send requests, accept friends and challenge them in a normal match from level 1 to 5.");
            translations.put("Solicitudes", "Requests");
            translations.put("VS Amigos", "VS Friends");
            translations.put("Jugadores existentes", "Existing players");
            translations.put("USUARIO                         NOMBRE                                      ESTADO", "USERNAME                        NAME                                      STATUS");
            translations.put("No hay jugadores para mostrar.", "There are no players to show.");
            translations.put("Amigos aceptados arriba. Solicitudes y partidas listas abajo.", "Accepted friends above. Requests and ready matches below.");
            translations.put("Retar VS", "Challenge VS");
            translations.put("Partidas VS", "VS Matches");
            translations.put("Aceptar / Listo / Jugar cuando ambos estén listos", "Accept / Ready / Play when both are ready");
            translations.put("AMIGO", "FRIEND");
            translations.put("Tú:", "You:");
            translations.put("Listo", "Ready");
            translations.put("No listo", "Not ready");
            translations.put("Aceptar", "Accept");
            translations.put("Enviada", "Sent");
            translations.put("Listo OK", "Ready OK");
            translations.put("Jugar", "Play");
            translations.put("Espera", "Wait");
            translations.put("Primero acepta o envía una solicitud de amistad.", "First accept or send a friend request.");
            translations.put("Esperando que el amigo acepte la partida.", "Waiting for your friend to accept the match.");
            translations.put("Aún falta que ambos presionen Listo.", "Both players still need to press Ready.");
            translations.put("El retado acepta y marca Listo. El retador presiona Jugar para iniciar ambos.", "The challenged player accepts and marks Ready. The challenger presses Play to start both players.");
            translations.put("Retador OK", "Challenger OK");
            translations.put("Solicitud recibida", "Request received");
            translations.put("Solicitud enviada", "Request sent");
            translations.put("Esperando Listo del retado", "Waiting for challenged player Ready");
            translations.put("Aceptada: presiona Listo", "Accepted: press Ready");
            translations.put("Listo: presiona Jugar", "Ready: press Play");
            translations.put("Esperando inicio del retador", "Waiting for challenger start");
            translations.put("Iniciando partida VS", "Starting VS match");
            translations.put("Espera que el jugador retado marque Listo.", "Wait for the challenged player to mark Ready.");
            translations.put("Solo el retador puede presionar Jugar cuando el retado esté listo.", "Only the challenger can press Play when the challenged player is ready.");
            translations.put("Salir", "Exit");
            translations.put("Segundo Nivel", "Second Level");
            translations.put("Cuarto Nivel", "Fourth Level");
            translations.put("Quinto Nivel", "Fifth Level");
            translations.put("Usa el balanceo del mockup: corta primero la soga derecha, toma las estrellas de la izquierda y luego deja caer el dulce hacia Om Nom.", "Use the mockup swing: cut the right rope first, collect the stars on the left and then let the candy fall toward Om Nom.");
            translations.put("Nivel 4 revisado: mantiene el diseño de referencia, pero ahora el camino del dulce y de las estrellas es más claro y posible.", "Level 4 revised: it keeps the reference design, but now the candy and star path is clearer and possible.");
            translations.put("Nivel 5 basado en la nueva referencia: Om Nom abajo, sin burbuja, estrellas en línea recta y una ruta posible para recogerlas en una sola partida.", "Level 5 based on the new reference: Om Nom below, no bubble, stars in a straight line, and a possible route to collect them in one run.");


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

    }


    public void applyRuntimePreferences(UserData ud) {
        if (ud == null) return;
        loadLang(ud.getLanguage());
        audioManager.applyUserPrefs(ud);
    }

    @Override
    public void create() {

        Gdx.graphics.setWindowedMode(WINDOW_WIDTH, WINDOW_HEIGHT);

        batch = new SpriteBatch();


        camera   = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply(true);
        batch.setProjectionMatrix(camera.combined);


        font      = new BitmapFont();
        fontLarge = new BitmapFont();
        fontLarge.getData().setScale(2.5f);
        fontSmall = new BitmapFont();
        fontSmall.getData().setScale(0.85f);


        userManager  = UserManager.getInstance();
        audioManager = AudioManager.getInstance();


        loadLang("es");


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

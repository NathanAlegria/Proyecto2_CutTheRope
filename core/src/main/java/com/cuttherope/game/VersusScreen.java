package com.cuttherope.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class VersusScreen extends SocialScreenBase {
    private final Rectangle btnBack = new Rectangle(18, 18, 120, 42);
    private final Rectangle tabSolicitudes = new Rectangle(260, 590, 145, 46);
    private final Rectangle tabVsAmigos = new Rectangle(420, 590, 145, 46);
    private final List<Rectangle> rowButtons = new ArrayList<>();
    private final List<Rectangle> secondButtons = new ArrayList<>();

    private List<UserData> allUsers = new ArrayList<>();
    private List<UserData> friends = new ArrayList<>();
    private List<VersusMatch> matches = new ArrayList<>();
    private int tab = 0;
    private String message = "";
    private Texture fondoVs;
    private float reloadTimer = 0f;
    private boolean autoStartingVs = false;
    private String pendingVsStartId = null;
    private float pendingVsStartTimer = 0f;

    public VersusScreen(MainGame game) { super(game); }

    @Override public void show() {
        sr = new ShapeRenderer();
        if (game.audioManager != null) game.audioManager.playMenuMusic();
        fondoVs = AssetPaths.textureAnyOrNull(AssetPaths.FONDO_VS, "FondoVS", "FondoVS.png", "FondoVS.jpg", "Imagenes/FondoVS", "Imagenes/FondoVS.png", "assets/Imagenes/FondoVS", "assets/Imagenes/FondoVS.png", "fondovs.png", AssetPaths.FONDO_MENU, AssetPaths.FONDO);
        UserData ud = um.getCurrentUser();
        if (ud != null) game.applyRuntimePreferences(ud);
        reloadLists();
    }

    private void reloadLists() {
        UserData current = um.getCurrentUser();
        allUsers = um.getAllUsers();
        friends = current == null ? new ArrayList<>() : um.getFriendsFor(current.getUsername());
        matches = current == null ? new ArrayList<>() : um.getVersusMatchesFor(current.getUsername());
    }

    @Override public void render(float delta) {
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);
        sr.setProjectionMatrix(game.camera.combined);
        Gdx.gl.glClearColor(0.10f, 0.045f, 0.015f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        reloadTimer += delta;
        if (reloadTimer >= 0.5f) {
            reloadTimer = 0f;
            reloadLists();
            autoStartIfMatchStarted();
        }

        if (pendingVsStartId != null) {
            pendingVsStartTimer -= delta;
            if (pendingVsStartTimer <= 0f) {
                String id = pendingVsStartId;
                pendingVsStartId = null;
                startVersusGameSafely(id);
                return;
            }
        }

        drawVsBackground();
        drawHeader();
        drawTab(tabSolicitudes, MainGame.t("Solicitudes"), tab == 0);
        drawTab(tabVsAmigos, MainGame.t("VS Amigos"), tab == 1);

        if (tab == 0) drawSolicitudesPanel();
        else drawVsAmigosPanel();

        drawButton(btnBack, MainGame.t("← Menú"), new Color(0.22f, 0.12f, 0.10f, 1f), 0);
        handleInput();
    }

    private void drawVsBackground() {
        if (fondoVs != null) {
            game.batch.begin();
            game.batch.draw(fondoVs, 0, 0, 800, 700);
            game.batch.end();
        } else {
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(new Color(0.09f, 0.045f, 0.015f, 1f));
            sr.rect(0, 0, 800, 700);
            sr.end();
        }

        // Capa oscura suave para que el texto y los paneles siempre se lean bien.
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0f, 0f, 0f, 0.22f));
        sr.rect(0, 0, 800, 700);
        sr.end();
    }

    private void drawHeader() {
        game.batch.begin();
        game.fontLarge.setColor(new Color(1f, 0.84f, 0.05f, 1f));
        game.fontLarge.draw(game.batch, MainGame.t("Sistema VS y Amistades"), 210, 675);
        game.fontSmall.setColor(new Color(1f, 0.92f, 0.40f, 1f));
        game.fontSmall.draw(game.batch, MainGame.t("Envía solicitudes, acepta amigos y reta en una partida normal del nivel 1 al 5."), 120, 645);
        if (message != null && !message.isEmpty()) {
            game.fontSmall.setColor(new Color(1f, 0.95f, 0.55f, 1f));
            game.fontSmall.draw(game.batch, message, 35, 72);
        }
        game.batch.end();
    }

    private void drawTab(Rectangle r, String text, boolean active) {
        drawButton(r, text, active ? new Color(0.18f, 0.10f, 0.08f, 1f) : new Color(0.13f, 0.075f, 0.06f, 1f), active ? 1 : 0);
    }

    private void drawPanel(String title, String subtitle) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0f, 0f, 0f, 0.35f)); sr.rect(16, 120, 768, 440);
        sr.setColor(new Color(0.09f, 0.045f, 0.015f, 0.92f)); sr.rect(20, 124, 760, 432);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(new Color(0.90f, 0.58f, 0.08f, 1f)); sr.rect(20, 124, 760, 432);
        sr.end();

        game.batch.begin();
        game.font.setColor(new Color(1f, 0.86f, 0.05f, 1f)); game.font.draw(game.batch, title, 40, 530);
        game.fontSmall.setColor(new Color(0.84f, 0.68f, 0.32f, 1f)); game.fontSmall.draw(game.batch, subtitle, 40, 508);
        game.batch.end();
    }

    private void drawSolicitudesPanel() {
        rowButtons.clear(); secondButtons.clear();
        drawPanel(MainGame.t("Jugadores existentes"), MainGame.t("USUARIO                         NOMBRE                                      ESTADO"));
        UserData current = um.getCurrentUser();
        if (current == null) return;
        int row = 0;
        for (UserData user : allUsers) {
            if (user.getUsername().equals(current.getUsername()) || row >= 7) continue;
            float y = 468 - row * 54;
            FriendshipStatus status = um.getFriendshipStatus(current.getUsername(), user.getUsername());
            Rectangle actionBtn = new Rectangle(590, y - 30, 150, 36);
            rowButtons.add(actionBtn); secondButtons.add(new Rectangle(0,0,0,0));
            drawPlayerRow(y, row, user.getUsername(), trim(user.getFullName(), 28), status.name(), status == FriendshipStatus.FRIEND);
            drawButton(actionBtn, status.getButtonText(), (status.canSendRequest() || status.canAcceptRequest()) ? new Color(0.22f, 0.12f, 0.08f, 1f) : new Color(0.13f, 0.09f, 0.07f, 1f), status == FriendshipStatus.FRIEND ? 2 : 0);
            row++;
        }
        if (row == 0) drawEmpty(MainGame.t("No hay jugadores para mostrar."));
    }

    private void drawVsAmigosPanel() {
        rowButtons.clear(); secondButtons.clear();
        drawPanel(MainGame.t("VS Amigos"), MainGame.t("Amigos aceptados arriba. Solicitudes y partidas listas abajo."));
        UserData current = um.getCurrentUser();
        if (current == null) return;
        int row = 0;
        for (UserData user : friends) {
            if (row >= 3) break;
            float y = 468 - row * 54;
            Rectangle reto = new Rectangle(590, y - 30, 150, 36);
            rowButtons.add(reto); secondButtons.add(new Rectangle(0,0,0,0));
            drawPlayerRow(y, row, user.getUsername(), trim(user.getFullName(), 28), MainGame.t("AMIGO"), true);
            drawButton(reto, MainGame.t("Retar VS"), new Color(0.22f, 0.12f, 0.08f, 1f), 2);
            row++;
        }

        game.batch.begin();
        game.font.setColor(new Color(1f, 0.86f, 0.05f, 1f)); game.font.draw(game.batch, MainGame.t("Partidas VS"), 40, 315);
        game.fontSmall.setColor(new Color(0.84f, 0.68f, 0.32f, 1f)); game.fontSmall.draw(game.batch, MainGame.t("El retado acepta y marca Listo. El retador presiona Jugar para iniciar ambos."), 40, 296);
        game.batch.end();

        int matchRow = 0;
        for (VersusMatch m : matches) {
            if (m.state == VersusMatch.State.FINISHED || matchRow >= 4) continue;
            float y = 260 - matchRow * 50;
            Rectangle a = new Rectangle(500, y - 28, 112, 34);
            Rectangle b = new Rectangle(625, y - 28, 95, 34);
            rowButtons.add(a); secondButtons.add(b);
            String other = m.otherPlayer(current.getUsername());
            String status = buildMatchStatus(m, current.getUsername());
            drawMatchRow(y, matchRow, "VS " + other, status);
            String txtA;
            if (m.state == VersusMatch.State.REQUESTED && m.opponent.equalsIgnoreCase(current.getUsername())) txtA = MainGame.t("Aceptar");
            else if (m.state == VersusMatch.State.REQUESTED) txtA = MainGame.t("Enviada");
            else if (m.requester.equalsIgnoreCase(current.getUsername())) txtA = MainGame.t("Retador OK");
            else txtA = m.isReady(current.getUsername()) ? MainGame.t("Listo OK") : MainGame.t("Listo");
            drawButton(a, txtA, new Color(0.22f, 0.12f, 0.08f, 1f), 0);
            boolean canStart = m.hasBothReady() && m.requester.equalsIgnoreCase(current.getUsername());
            drawButton(b, canStart ? MainGame.t("Jugar") : MainGame.t("Espera"), canStart ? new Color(0.12f, 0.35f, 0.12f, 1f) : new Color(0.13f, 0.09f, 0.07f, 1f), canStart ? 2 : 0);
            matchRow++;
        }
        if (friends.isEmpty() && matches.isEmpty()) drawEmpty(MainGame.t("Primero acepta o envía una solicitud de amistad."));
    }

    private String buildMatchStatus(VersusMatch m, String username) {
        if (m.state == VersusMatch.State.REQUESTED) {
            return m.opponent.equalsIgnoreCase(username) ? MainGame.t("Solicitud recibida") : MainGame.t("Solicitud enviada");
        }
        if (m.state == VersusMatch.State.ACCEPTED) {
            return m.requester.equalsIgnoreCase(username) ? MainGame.t("Esperando Listo del retado") : MainGame.t("Aceptada: presiona Listo");
        }
        if (m.state == VersusMatch.State.BOTH_READY) {
            return m.requester.equalsIgnoreCase(username) ? MainGame.t("Listo: presiona Jugar") : MainGame.t("Esperando inicio del retador");
        }
        if (m.state == VersusMatch.State.IN_PROGRESS) return MainGame.t("Iniciando partida VS");
        return m.state.name();
    }

    private void autoStartIfMatchStarted() {
        if (autoStartingVs || pendingVsStartId != null) return;
        UserData current = um.getCurrentUser();
        if (current == null) return;
        for (VersusMatch m : matches) {
            if (m != null && m.state == VersusMatch.State.IN_PROGRESS && m.includes(current.getUsername())) {
                // La cuenta que aceptó entra automáticamente. Se deja una pausa mínima
                // para no forzar dos cambios de pantalla OpenGL exactamente en el mismo instante
                // cuando se prueban dos instancias en una misma computadora.
                scheduleVersusStart(m.id, 0.65f);
                return;
            }
        }
    }

    private void scheduleVersusStart(String matchId, float delaySeconds) {
        if (matchId == null || matchId.trim().isEmpty()) return;
        if (autoStartingVs || pendingVsStartId != null) return;
        autoStartingVs = true;
        pendingVsStartId = matchId;
        pendingVsStartTimer = Math.max(0.05f, delaySeconds);
        message = MainGame.t("Iniciando partida VS");
    }

    private void startVersusGameSafely(final String matchId) {
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() {
                try {
                    VersusModeContext.start(matchId);
                    game.setScreen(new GameScreen(game, 0));
                } catch (Throwable t) {
                    autoStartingVs = false;
                    pendingVsStartId = null;
                    message = "No se pudo iniciar VS: " + t.getMessage();
                    t.printStackTrace();
                }
            }
        });
    }

    private void drawPlayerRow(float y, int row, String username, String name, String status, boolean friend) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(row % 2 == 0 ? new Color(0.20f, 0.12f, 0.07f, 0.96f) : new Color(0.14f, 0.08f, 0.045f, 0.96f));
        sr.rect(40, y - 38, 710, 46);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line); sr.setColor(new Color(0.45f, 0.25f, 0.08f, 1f)); sr.rect(40, y - 38, 710, 46); sr.end();
        game.batch.begin();
        game.fontSmall.setColor(Color.WHITE); game.fontSmall.draw(game.batch, username, 50, y - 10);
        game.fontSmall.setColor(new Color(0.82f, 0.78f, 0.70f, 1f)); game.fontSmall.draw(game.batch, name, 190, y - 10);
        game.fontSmall.setColor(friend ? new Color(0.35f, 1f, 0.45f, 1f) : new Color(1f, 0.85f, 0.10f, 1f)); game.fontSmall.draw(game.batch, status, 450, y - 10);
        game.batch.end();
    }

    private void drawMatchRow(float y, int row, String title, String status) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(row % 2 == 0 ? new Color(0.20f, 0.12f, 0.07f, 0.96f) : new Color(0.14f, 0.08f, 0.045f, 0.96f));
        sr.rect(40, y - 36, 710, 42);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line); sr.setColor(new Color(0.45f, 0.25f, 0.08f, 1f)); sr.rect(40, y - 36, 710, 42); sr.end();
        game.batch.begin();
        game.fontSmall.setColor(Color.WHITE); game.fontSmall.draw(game.batch, title, 50, y - 10);
        game.fontSmall.setColor(new Color(1f, 0.85f, 0.10f, 1f)); game.fontSmall.draw(game.batch, status, 190, y - 10);
        game.batch.end();
    }

    private void drawEmpty(String txt) {
        game.batch.begin(); game.font.setColor(Color.WHITE); game.font.draw(game.batch, txt, 65, 400); game.batch.end();
    }

    private void drawButton(Rectangle r, String text, Color base, int accentMode) {
        boolean hov = isHovered(r);
        Color border = accentMode == 2 ? new Color(0.10f, 0.82f, 0.32f, 1f) : new Color(0.18f, 0.40f, 0.95f, 1f);
        if (accentMode == 1) border = new Color(0.85f, 0.55f, 0.10f, 1f);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0f, 0f, 0f, 0.55f)); sr.rect(r.x + 3, r.y - 3, r.width, r.height);
        sr.setColor(hov ? base.cpy().add(0.08f, 0.08f, 0.08f, 0f) : base); sr.rect(r.x, r.y, r.width, r.height);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(new Color(0.72f, 0.52f, 0.30f, 1f)); sr.rect(r.x, r.y, r.width, r.height);
        sr.setColor(border); sr.line(r.x + 4, r.y + 2, r.x + r.width - 4, r.y + 2);
        sr.end();
        game.batch.begin(); game.fontSmall.setColor(Color.WHITE); game.fontSmall.draw(game.batch, text, r.x + 12, r.y + 24); game.batch.end();
    }

    private void handleInput() {
        if (!Gdx.input.justTouched()) return;
        Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0); game.viewport.unproject(touch);
        if (game.audioManager != null) game.audioManager.playClick();
        if (btnBack.contains(touch.x, touch.y)) { game.setScreen(new MainMenuScreen(game)); dispose(); return; }
        if (tabSolicitudes.contains(touch.x, touch.y)) { tab = 0; message = ""; reloadLists(); return; }
        if (tabVsAmigos.contains(touch.x, touch.y)) { tab = 1; message = ""; reloadLists(); return; }
        if (tab == 0) handleSolicitudesInput(touch.x, touch.y); else handleVsAmigosInput(touch.x, touch.y);
    }

    private void handleSolicitudesInput(float x, float y) {
        UserData current = um.getCurrentUser(); if (current == null) return;
        int visible = 0;
        for (UserData user : allUsers) {
            if (user.getUsername().equals(current.getUsername())) continue;
            if (visible >= rowButtons.size()) break;
            if (rowButtons.get(visible).contains(x, y)) {
                FriendshipStatus status = um.getFriendshipStatus(current.getUsername(), user.getUsername());
                SocialAction action = null;
                if (status.canSendRequest()) action = new FriendRequestAction(um, current.getUsername(), user.getUsername());
                else if (status.canAcceptRequest()) action = new AcceptFriendAction(um, current.getUsername(), user.getUsername());
                message = action != null ? action.execute() : status.getButtonText(); reloadLists(); return;
            }
            visible++;
        }
    }

    private void handleVsAmigosInput(float x, float y) {
        UserData current = um.getCurrentUser(); if (current == null) return;
        int index = 0;
        for (UserData user : friends) {
            if (index >= rowButtons.size()) break;
            if (rowButtons.get(index).contains(x, y)) {
                message = um.createVersusRequest(user.getUsername()); reloadLists(); return;
            }
            index++;
            if (index >= 3) break;
        }
        for (VersusMatch m : matches) {
            if (m.state == VersusMatch.State.FINISHED) continue;
            if (index >= rowButtons.size()) break;
            if (rowButtons.get(index).contains(x, y)) {
                if (m.state == VersusMatch.State.REQUESTED && m.opponent.equalsIgnoreCase(current.getUsername())) message = um.acceptVersusRequest(m.id);
                else if (m.state == VersusMatch.State.REQUESTED) message = MainGame.t("Esperando que el amigo acepte la partida.");
                else if (m.requester.equalsIgnoreCase(current.getUsername())) message = MainGame.t("Espera que el jugador retado marque Listo.");
                else message = um.setVersusReady(m.id);
                reloadLists(); return;
            }
            if (secondButtons.get(index).contains(x, y)) {
                VersusMatch fresh = um.loadVersusMatch(m.id);
                if (fresh != null && fresh.hasBothReady() && fresh.requester.equalsIgnoreCase(current.getUsername())) {
                    message = um.startVersusMatch(fresh.id);
                    scheduleVersusStart(fresh.id, 0.20f);
                } else message = MainGame.t("Solo el retador puede presionar Jugar cuando el retado esté listo.");
                reloadLists(); return;
            }
            index++;
        }
    }

    private boolean isHovered(Rectangle r) { Vector3 t = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0); game.viewport.unproject(t); return r.contains(t.x, t.y); }
    private String trim(String s, int max) { return s == null ? "" : (s.length() <= max ? s : s.substring(0, max - 3) + "..."); }
    @Override public void dispose() {
        if (sr != null) sr.dispose();
        if (fondoVs != null) { fondoVs.dispose(); fondoVs = null; }
    }
}

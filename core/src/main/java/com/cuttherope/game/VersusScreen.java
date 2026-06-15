package com.cuttherope.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class VersusScreen extends SocialScreenBase {
    private final Rectangle btnBack = new Rectangle(20, 18, 130, 42);
    private final Rectangle tabPlayers = new Rectangle(180, 610, 190, 42);
    private final Rectangle tabVersus = new Rectangle(390, 610, 190, 42);
    private final Rectangle[] levelBtns = new Rectangle[5];
    private final List<Rectangle> rowButtons = new ArrayList<>();

    private List<UserData> allUsers = new ArrayList<>();
    private List<UserData> friends = new ArrayList<>();
    private boolean showVs = false;
    private String message = "";
    private String selectedFriend = null;
    private VersusResult lastResult = null;

    public VersusScreen(MainGame game) {
        super(game);
        for (int i = 0; i < levelBtns.length; i++) {
            levelBtns[i] = new Rectangle(205 + i * 78, 140, 62, 45);
        }
    }

    @Override
    public void show() {
        sr = new ShapeRenderer();
        if (game.audioManager != null) game.audioManager.playMenuMusic();
        reloadLists();
    }

    private void reloadLists() {
        allUsers = um.getAllUsers();
        UserData current = um.getCurrentUser();
        friends = current == null ? new ArrayList<>() : um.getFriendsFor(current.getUsername());
    }

    @Override
    public void render(float delta) {
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);
        sr.setProjectionMatrix(game.camera.combined);

        Gdx.gl.glClearColor(0.06f, 0.05f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        drawBackground();
        drawHeader();
        drawButton(btnBack, "← Menú", new Color(0.45f, 0.32f, 0.70f, 1f));
        drawButton(tabPlayers, "Solicitudes", showVs ? new Color(0.18f, 0.38f, 0.82f, 1f) : new Color(0.12f, 0.56f, 1f, 1f));
        drawButton(tabVersus, "VS Amigos", showVs ? new Color(0.12f, 0.56f, 1f, 1f) : new Color(0.18f, 0.38f, 0.82f, 1f));

        if (showVs) drawVersusPanel(); else drawRequestPanel();
        handleInput();
    }

    private void drawBackground() {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0.08f, 0.06f, 0.14f, 1f));
        sr.rect(0, 0, 800, 700);
        sr.setColor(new Color(0.17f, 0.12f, 0.27f, 1f));
        sr.rect(35, 85, 730, 500);
        sr.setColor(new Color(1f, 0.84f, 0f, 0.12f));
        sr.circle(705, 625, 76);
        sr.setColor(new Color(0.12f, 0.56f, 1f, 0.12f));
        sr.circle(90, 105, 68);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(new Color(1f, 0.84f, 0f, 0.85f));
        sr.rect(35, 85, 730, 500);
        sr.end();
    }

    private void drawHeader() {
        game.batch.begin();
        game.fontLarge.setColor(new Color(1f, 0.84f, 0.12f, 1f));
        game.fontLarge.draw(game.batch, "Sistema VS y Amistades", 210, 675);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Envía solicitudes, acepta amigos y compara niveles por estrellas y tiempo.", 120, 595);
        game.fontSmall.setColor(new Color(1f, 0.95f, 0.4f, 1f));
        game.fontSmall.draw(game.batch, message == null ? "" : message, 175, 50);
        game.batch.end();
    }

    private void drawRequestPanel() {
        rowButtons.clear();
        UserData current = um.getCurrentUser();

        game.batch.begin();
        game.font.setColor(new Color(1f, 0.84f, 0.12f, 1f));
        game.font.draw(game.batch, "Jugadores existentes", 70, 555);
        game.fontSmall.setColor(Color.LIGHT_GRAY);
        game.fontSmall.draw(game.batch, "Nombre", 75, 525);
        game.fontSmall.draw(game.batch, "Estado", 345, 525);
        game.batch.end();

        if (current == null) return;
        int row = 0;
        for (UserData user : allUsers) {
            if (user.getUsername().equals(current.getUsername())) continue;
            if (row >= 8) break;
            float y = 490 - row * 48;
            drawRowBackground(y, row);
            FriendshipStatus status = um.getFriendshipStatus(current.getUsername(), user.getUsername());
            Rectangle actionBtn = new Rectangle(510, y - 26, 190, 34);
            rowButtons.add(actionBtn);

            game.batch.begin();
            game.fontSmall.setColor(Color.WHITE);
            game.fontSmall.draw(game.batch, user.getUsername(), 75, y - 5);
            game.fontSmall.setColor(new Color(0.82f, 0.78f, 1f, 1f));
            game.fontSmall.draw(game.batch, trim(user.getFullName(), 24), 190, y - 5);
            game.fontSmall.setColor(status == FriendshipStatus.FRIEND ? new Color(0.55f, 1f, 0.55f, 1f) : new Color(1f, 0.92f, 0.45f, 1f));
            game.fontSmall.draw(game.batch, status.name(), 345, y - 5);
            game.batch.end();

            Color c = (status.canSendRequest() || status.canAcceptRequest())
                ? new Color(0.12f, 0.56f, 1f, 1f)
                : new Color(0.20f, 0.22f, 0.28f, 1f);
            drawButton(actionBtn, status.getButtonText(), c);
            row++;
        }
    }

    private void drawVersusPanel() {
        rowButtons.clear();
        UserData current = um.getCurrentUser();

        game.batch.begin();
        game.font.setColor(new Color(1f, 0.84f, 0.12f, 1f));
        game.font.draw(game.batch, "Selecciona un amigo", 70, 555);
        game.fontSmall.setColor(Color.LIGHT_GRAY);
        game.fontSmall.draw(game.batch, "Luego elige nivel 1-5. Gana quien tenga más estrellas; si empatan, gana el menor tiempo.", 70, 525);
        game.batch.end();

        int row = 0;
        for (UserData friend : friends) {
            if (row >= 6) break;
            float y = 485 - row * 48;
            drawRowBackground(y, row);
            Rectangle selectBtn = new Rectangle(520, y - 26, 140, 34);
            rowButtons.add(selectBtn);

            boolean selected = friend.getUsername().equals(selectedFriend);
            game.batch.begin();
            game.fontSmall.setColor(selected ? new Color(1f, 0.84f, 0.12f, 1f) : Color.WHITE);
            game.fontSmall.draw(game.batch, friend.getUsername(), 75, y - 5);
            game.fontSmall.setColor(new Color(0.82f, 0.78f, 1f, 1f));
            game.fontSmall.draw(game.batch, trim(friend.getFullName(), 28), 220, y - 5);
            game.batch.end();
            drawButton(selectBtn, selected ? "Seleccionado" : "Elegir", new Color(0.12f, 0.56f, 1f, 1f));
            row++;
        }

        if (friends.isEmpty()) {
            game.batch.begin();
            game.font.setColor(Color.WHITE);
            game.font.draw(game.batch, "Aún no tienes amigos aceptados. Primero envía o acepta solicitudes.", 95, 420);
            game.batch.end();
        }

        for (int i = 0; i < levelBtns.length; i++) {
            drawButton(levelBtns[i], "Nivel " + (i + 1), new Color(0.20f, 0.60f, 0.25f, 1f));
        }

        if (lastResult != null) {
            game.batch.begin();
            game.font.setColor(new Color(1f, 0.84f, 0.12f, 1f));
            game.font.draw(game.batch, "Resultado nivel " + (lastResult.level + 1) + ": " + lastResult.winner, 185, 110);
            game.fontSmall.setColor(Color.WHITE);
            game.fontSmall.draw(game.batch, lastResult.playerA + ": " + lastResult.starsA + "★ / " + formatTime(lastResult.timeA)
                + "    " + lastResult.playerB + ": " + lastResult.starsB + "★ / " + formatTime(lastResult.timeB), 170, 82);
            game.fontSmall.draw(game.batch, lastResult.reason, 280, 62);
            game.batch.end();
        }
    }

    private void drawRowBackground(float y, int row) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(row % 2 == 0 ? new Color(0.12f, 0.09f, 0.20f, 1f) : new Color(0.10f, 0.08f, 0.17f, 1f));
        sr.rect(65, y - 32, 660, 40);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(new Color(0.38f, 0.28f, 0.55f, 1f));
        sr.rect(65, y - 32, 660, 40);
        sr.end();
    }

    private void handleInput() {
        if (!Gdx.input.justTouched()) return;
        Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        game.viewport.unproject(touch);
        if (game.audioManager != null) game.audioManager.playClick();

        if (btnBack.contains(touch.x, touch.y)) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
            return;
        }
        if (tabPlayers.contains(touch.x, touch.y)) { showVs = false; message = ""; reloadLists(); return; }
        if (tabVersus.contains(touch.x, touch.y)) { showVs = true; message = ""; reloadLists(); return; }

        if (showVs) handleVsInput(touch.x, touch.y); else handleRequestInput(touch.x, touch.y);
    }

    private void handleRequestInput(float x, float y) {
        UserData current = um.getCurrentUser();
        if (current == null) return;
        int visibleIndex = 0;
        for (UserData user : allUsers) {
            if (user.getUsername().equals(current.getUsername())) continue;
            if (visibleIndex >= rowButtons.size()) break;
            if (rowButtons.get(visibleIndex).contains(x, y)) {
                FriendshipStatus status = um.getFriendshipStatus(current.getUsername(), user.getUsername());
                SocialAction action = null;
                if (status.canSendRequest()) action = new FriendRequestAction(um, current.getUsername(), user.getUsername());
                else if (status.canAcceptRequest()) action = new AcceptFriendAction(um, current.getUsername(), user.getUsername());
                if (action != null) message = action.execute(); else message = status.getButtonText();
                reloadLists();
                return;
            }
            visibleIndex++;
        }
    }

    private void handleVsInput(float x, float y) {
        for (int i = 0; i < friends.size() && i < rowButtons.size(); i++) {
            if (rowButtons.get(i).contains(x, y)) {
                selectedFriend = friends.get(i).getUsername();
                lastResult = null;
                message = "Amigo seleccionado: " + selectedFriend;
                return;
            }
        }
        for (int i = 0; i < levelBtns.length; i++) {
            if (levelBtns[i].contains(x, y)) {
                UserData current = um.getCurrentUser();
                if (selectedFriend == null) { message = "Selecciona un amigo primero."; return; }
                lastResult = um.compareVersus(current.getUsername(), selectedFriend, i);
                message = "VS calculado.";
                return;
            }
        }
    }

    private String trim(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    private String formatTime(long ms) {
        if (ms <= 0) return "sin tiempo";
        return String.format("%.2fs", ms / 1000f);
    }

    @Override
    public void dispose() {
        if (sr != null) sr.dispose();
    }
}

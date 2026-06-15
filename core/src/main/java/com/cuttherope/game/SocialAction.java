package com.cuttherope.game;

/** Clase abstracta para demostrar polimorfismo en acciones sociales. */
public abstract class SocialAction {
    protected final UserManager userManager;
    protected final String currentUsername;
    protected final String targetUsername;

    public SocialAction(UserManager userManager, String currentUsername, String targetUsername) {
        this.userManager = userManager;
        this.currentUsername = currentUsername;
        this.targetUsername = targetUsername;
    }

    public abstract String execute();
}

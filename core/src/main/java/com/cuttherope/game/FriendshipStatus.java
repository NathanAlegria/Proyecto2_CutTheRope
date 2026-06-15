package com.cuttherope.game;

/** Enum complejo: cada estado tiene texto, color lógico y permisos. */
public enum FriendshipStatus {
    NONE("Mandar solicitud", true, false, false),
    SENT("Pendiente", false, false, false),
    RECEIVED("Aceptar", false, true, false),
    FRIEND("Amigos", false, false, true),
    SELF("Tu usuario", false, false, false);

    private final String buttonText;
    private final boolean canSendRequest;
    private final boolean canAcceptRequest;
    private final boolean canPlayVs;

    FriendshipStatus(String buttonText, boolean canSendRequest, boolean canAcceptRequest, boolean canPlayVs) {
        this.buttonText = buttonText;
        this.canSendRequest = canSendRequest;
        this.canAcceptRequest = canAcceptRequest;
        this.canPlayVs = canPlayVs;
    }

    public String getButtonText() { return buttonText; }
    public boolean canSendRequest() { return canSendRequest; }
    public boolean canAcceptRequest() { return canAcceptRequest; }
    public boolean canPlayVs() { return canPlayVs; }
}

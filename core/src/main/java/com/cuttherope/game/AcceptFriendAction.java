package com.cuttherope.game;

public class AcceptFriendAction extends SocialAction {
    public AcceptFriendAction(UserManager userManager, String currentUsername, String targetUsername) {
        super(userManager, currentUsername, targetUsername);
    }

    @Override
    public String execute() {
        return userManager.acceptFriendRequest(currentUsername, targetUsername);
    }
}

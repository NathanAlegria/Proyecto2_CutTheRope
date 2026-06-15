package com.cuttherope.game;

public class FriendRequestAction extends SocialAction {
    public FriendRequestAction(UserManager userManager, String currentUsername, String targetUsername) {
        super(userManager, currentUsername, targetUsername);
    }

    @Override
    public String execute() {
        return userManager.sendFriendRequest(currentUsername, targetUsername);
    }
}

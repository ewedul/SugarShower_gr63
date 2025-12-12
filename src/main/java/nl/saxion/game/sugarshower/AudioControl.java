package nl.saxion.game.sugarshower;

import nl.saxion.gameapp.GameApp;

public class AudioControl {
    public static boolean muteMode;


    public static void toggleMuteMode() {
        muteMode = !muteMode;
    }

    public static void playMusic(String audioName, boolean flag, float volume) {
        GameApp.playMusic(audioName, flag, AudioControl.muteMode? 0f:volume);
    }

    public static void playSound(String audioName, float volume) {
        GameApp.playSound(audioName, AudioControl.muteMode ? 0f : volume);
    }

}

package nl.saxion.game.sugarshower;

import nl.saxion.gameapp.GameApp;

public class AudioControl {
    public static boolean muteMode;


    public static void toggleMuteMode() {
        muteMode = !muteMode;
    }

    public static float getVolume(float volume) {
        return muteMode ? 0f : volume;
    }

}

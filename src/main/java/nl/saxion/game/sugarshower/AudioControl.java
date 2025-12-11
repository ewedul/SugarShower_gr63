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


//   Testing feature
//    public static void playMusic(String audioName, boolean flag, float volume) {
//        GameApp.playMusic(audioName, flag, AudioControl.getVolume(volume));
//    }
//    public void playSound(String audioName, float number){
//        if(!AudioControl.muteMode) {
//            GameApp.playSound(audioName, number);
//        }else{
//            GameApp.playSound(audioName,0f);
//        }
//    }

}

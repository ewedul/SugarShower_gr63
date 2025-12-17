package nl.saxion.game.sugarshower;

import com.badlogic.gdx.Input;
import nl.saxion.gameapp.GameApp;

public class Button {
    float x, y, w, h;

    public Button(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    //Detect when a button is clicked (work only with buttons drawn with drawTexture)
    public boolean isButtonClicked(float mouseX, float mouseY) {
        return GameApp.isButtonJustPressed(Input.Buttons.LEFT)
                &&GameApp.pointInRect(mouseX, mouseY, x, y, w, h);
    }

}

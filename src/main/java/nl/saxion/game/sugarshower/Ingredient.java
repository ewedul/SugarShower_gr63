package nl.saxion.game.sugarshower;

public class Ingredient {

    float x;
    float y;
    float speed;
    String type;
    boolean active;
    String filename;

    public Ingredient(String type, float speed) {
        this.type = type;
        this.speed = speed;
        this.active = true;
    }
}


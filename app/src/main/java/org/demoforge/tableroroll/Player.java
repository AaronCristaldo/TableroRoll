package org.demoforge.tableroroll;

public class Player {
    private String nombre;
    private int vida;
    private int x;
    private int y;
    private int spriteResId;
    private String imagenBase64; // Esta debería ser la imagen en Base64


    public Player() {
        // Constructor vacío requerido por Firebase
    }

    public Player(String nombre, int vida, int x, int y, int spriteResId) {
        this.nombre = nombre;
        this.vida = vida;
        this.x = x;
        this.y = y;
        this.spriteResId = spriteResId;
    }

    // Métodos get y set para cada campo
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getVida() { return vida; }
    public void setVida(int vida) { this.vida = vida; }
    public void setPosition(int x, int y) { this.x = x; this.y = y; }


    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getSpriteResId() { return spriteResId; }
    public void setSpriteResId(int spriteResId) { this.spriteResId = spriteResId; }

    public void setImagenBase64(String imagenBase64) {
        this.imagenBase64 = imagenBase64;
    }

    public String getImagenBase64() {
        return imagenBase64;
    }
}


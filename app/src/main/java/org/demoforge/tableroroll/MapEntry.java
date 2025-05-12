package org.demoforge.tableroroll;

import androidx.annotation.NonNull;

// Clase pública para usar con Firebase y adaptadores
public class MapEntry {
    public String key;
    public int tipo;
    public String nombre;

    // Constructor vacío requerido por Firebase
    public MapEntry() {}

    public MapEntry(String key, int tipo, String nombre) {
        this.key = key;
        this.tipo = tipo;
        this.nombre = nombre;
    }

    @NonNull
    @Override
    public String toString() {
        return nombre;
    }
}

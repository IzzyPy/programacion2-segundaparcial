package com.example.programacion2.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Esta clase representa la entidad (la tabla) en la base de datos de Room.
 * Cada instancia de esta clase es una fila en la tabla "log_app_table".
 */
@Entity(tableName = "log_app_table")
public class LogApp {

    /**
     * El ID único para cada registro de log.
     * Room se encargará de generarlo automáticamente.
     */
    @PrimaryKey(autoGenerate = true)
    private int id;

    /**
     * La fecha y hora en que se registró el evento, usualmente en milisegundos (timestamp).
     */
    private long fechaHora;

    /**
     * Una descripción del evento o error que ocurrió.
     */
    private String descripcionError;

    /**
     * El nombre de la clase o componente donde se originó el log.
     */
    private String claseOrigen;

    /**
     * Un campo booleano para saber si este log ya ha sido sincronizado con el servidor.
     * `false` (0 en la base de datos) = Pendiente de sincronizar.
     * `true` (1 en la base de datos) = Ya sincronizado.
     */
    private boolean synced;

    // --- Getters y Setters ---
    // Room los necesita para poder crear los objetos y acceder a sus campos.
    // También son útiles para que tú puedas manipular los datos del objeto.

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(long fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getDescripcionError() {
        return descripcionError;
    }

    public void setDescripcionError(String descripcionError) {
        this.descripcionError = descripcionError;
    }

    public String getClaseOrigen() {
        return claseOrigen;
    }

    public void setClaseOrigen(String claseOrigen) {
        this.claseOrigen = claseOrigen;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }
}

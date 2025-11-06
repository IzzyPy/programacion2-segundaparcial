package com.example.programacion2.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Esta es la clase principal de la base de datos para Room.
 * Define las entidades (tablas) que contiene y proporciona acceso a los DAOs.
 */
@Database(entities = {LogApp.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // Le dice a la base de datos qué DAO puede usar.
    public abstract LogDao logDao();

    // --- SOLUCIÓN: ESTE ES EL BLOQUE DE CÓDIGO QUE FALTABA ---
    // Se usa el patrón Singleton para asegurar que solo haya una instancia de la base de datos.

    private static volatile AppDatabase INSTANCE;

    /**
     * Este es el método estático que tu AppRepository no encontraba.
     * Crea la base de datos si no existe, o devuelve la instancia existente.
     *
     * @param context El contexto de la aplicación.
     * @return La instancia única de AppDatabase.
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "log_database")
                            // .addCallback(...) si necesitaras poblar la base de datos al crearla
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

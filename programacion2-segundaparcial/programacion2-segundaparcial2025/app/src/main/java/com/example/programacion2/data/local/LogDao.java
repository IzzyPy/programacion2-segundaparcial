package com.example.programacion2.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

/**
 * DAO (Data Access Object) para la entidad LogApp.
 * Define los métodos para interactuar con la tabla de logs en la base de datos.
 */
@Dao
public interface LogDao {

    /**
     * Inserta un nuevo registro de log en la base de datos.
     * @param log El objeto LogApp a insertar.
     */
    @Insert
    void insert(LogApp log);

    /**
     * Obtiene todos los registros de log de la tabla, ordenados por fecha descendente.
     * @return Una lista de todos los objetos LogApp.
     */
    @Query("SELECT * FROM log_app_table ORDER BY fechaHora DESC")
    List<LogApp> getAll();

    /**
     * Obtiene todos los logs que aún no han sido sincronizados con el servidor.
     * @return Una lista de objetos LogApp no sincronizados.
     */
    @Query("SELECT * FROM log_app_table WHERE synced = 0")
    List<LogApp> getUnsyncedLogs();

    /**
     * Marca una lista de logs como sincronizados.
     * @param logIds Una lista de los IDs de los logs que se van a actualizar.
     */
    @Query("UPDATE log_app_table SET synced = 1 WHERE id IN (:logIds)")
    void markLogsAsSynced(List<Integer> logIds);

    /**
     * Borra todos los registros de la tabla de logs.
     */
    @Query("DELETE FROM log_app_table")
    void deleteAll();
}

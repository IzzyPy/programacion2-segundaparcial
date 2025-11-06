package com.example.programacion2.data.repository;

import android.content.Context;
import com.example.programacion2.data.local.AppDatabase;
import com.example.programacion2.data.local.LogApp;
import com.example.programacion2.data.local.LogDao; // --> Importante añadir esta importación

import java.util.ArrayList; // --> Importación para ArrayList
import java.util.List;
import java.util.concurrent.ExecutorService; // --> Importación para el Executor
import java.util.concurrent.Executors;

public class AppRepository {
    private static volatile AppRepository instance;
    private final LogDao logDao; // --> Es mejor guardar una referencia al DAO directamente
    private final ExecutorService databaseExecutor; // --> Mejora: Reutilizar el Executor

    private AppRepository(Context c) {
        AppDatabase db = AppDatabase.getDatabase(c.getApplicationContext()); // Obtiene la instancia de la base de datos
        logDao = db.logDao(); // Obtiene el DAO desde la base de datos
        databaseExecutor = Executors.newSingleThreadExecutor(); // Inicializa el Executor una sola vez
    }

    public static synchronized AppRepository getInstance(Context c) {
        if (instance == null) {
            instance = new AppRepository(c.getApplicationContext());
        }
        return instance;
    }

    public void insertLog(LogApp log) {
        databaseExecutor.execute(() -> logDao.insert(log));
    }

    public List<LogApp> getAllLogs() {
        // Esta llamada debería idealmente no estar en el hilo principal,
        // pero desde un ViewModel con LiveData o un Worker está bien.
        return logDao.getAll();
    }

    public void deleteAllLogs() {
        databaseExecutor.execute(() -> logDao.deleteAll());
    }

    // ==================================================================
    //   SOLUCIÓN: AQUÍ SE AÑADEN LOS MÉTODOS QUE FALTABAN
    // ==================================================================

    /**
     * Obtiene una lista de logs no sincronizados desde la base de datos local.
     * Llama al método correspondiente en el DAO.
     */
    public List<LogApp> getUnsyncedLogs() {
        // Como SyncWorker se ejecuta en un hilo de fondo, podemos llamar directamente.
        return logDao.getUnsyncedLogs();
    }

    /**
     * Marca una lista de logs como sincronizados en la base de datos.
     * Llama al método correspondiente en el DAO.
     */
    public void markLogsAsSynced(List<LogApp> logs) {
        if (logs == null || logs.isEmpty()) {
            return;
        }
        // Extraemos solo los IDs de la lista de logs.
        List<Integer> logIds = new ArrayList<>();
        for (LogApp log : logs) {
            logIds.add(log.getId()); // Asumiendo que LogApp tiene un método getId()
        }

        // Ejecutamos la actualización en un hilo de fondo.
        databaseExecutor.execute(() -> {
            logDao.markLogsAsSynced(logIds);
        });
    }
}

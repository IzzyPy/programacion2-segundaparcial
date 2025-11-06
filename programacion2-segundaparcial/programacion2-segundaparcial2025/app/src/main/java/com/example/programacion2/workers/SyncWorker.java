package com.example.programacion2.workers;import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

// --> SOLUCIÓN: Añade todas las importaciones que faltan aquí
import com.example.programacion2.data.local.LogApp;
import com.example.programacion2.data.remote.ApiService;
import com.example.programacion2.data.remote.RetrofitClient;
import com.example.programacion2.data.repository.AppRepository;

import java.util.List;
import retrofit2.Call;
import retrofit2.Response;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppRepository repo = AppRepository.getInstance(getApplicationContext());
        List<LogApp> logs = repo.getUnsyncedLogs(); // Asumiendo que tienes un método para obtener logs no sincronizados

        if (logs == null || logs.isEmpty()) {
            return Result.success(); // No hay nada que sincronizar
        }

        // Esta es la línea que antes daba error. Ahora funcionará gracias a la importación.
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        Call<Void> call = api.postLogs(logs);

        try {
            // execute() hace la llamada de forma síncrona, ideal para un Worker en segundo plano
            Response<Void> response = call.execute();

            if (response.isSuccessful()) {
                // Si la subida fue exitosa, marca los logs como sincronizados en tu base de datos local
                repo.markLogsAsSynced(logs); // Necesitarás implementar este método en tu AppRepository
                return Result.success();
            } else {
                // Si el servidor dio un error, reintenta más tarde
                return Result.retry();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Si hubo un fallo de red o cualquier otra excepción, reintenta más tarde
            return Result.retry();
        }
    }
}

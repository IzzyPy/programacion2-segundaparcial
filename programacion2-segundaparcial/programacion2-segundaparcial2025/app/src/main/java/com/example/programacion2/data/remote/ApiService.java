package com.example.programacion2.data.remote;

// Importaciones para todos los métodos
import com.example.programacion2.data.local.LogApp;
import com.example.programacion2.data.model.UploadPayload; // Asumiendo que creaste UploadPayload.java
import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    /**
     * MÉTODO PARA ClienteActivity: Envía datos JSON y una lista de archivos.
     */
    @Multipart
    @POST("clientes/registrar") // <-- ¡Usa aquí tu URL real para registrar clientes!
    Call<Void> uploadJsonAndFiles(
            @Part("datos_cliente") RequestBody jsonData, // El servidor buscará la parte JSON con este nombre
            @Part List<MultipartBody.Part> files    // El servidor recibirá una lista de archivos
    );

    /**
     * MÉTODO PARA ArchivosActivity: Envía un cuerpo JSON con un ZIP en Base64.
     */
    @POST("archivos/subir_zip_json") // <-- ¡Usa aquí tu URL real para subir el ZIP!
    Call<Void> uploadJsonPayload(@Body UploadPayload payload);

    /**
     * MÉTODO PARA SyncWorker: Envía una lista de logs.
     */
    @POST("logs/sincronizar") // <-- ¡Usa aquí tu URL real para sincronizar logs!
    Call<Void> postLogs(@Body List<LogApp> logs);

}

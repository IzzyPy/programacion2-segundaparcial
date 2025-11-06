package com.example.programacion2.ui.cliente;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.programacion2.R;
import com.example.programacion2.data.local.LogApp;
// --> SOLUCIÓN: Se importa la interfaz ApiService que ya creaste.
import com.example.programacion2.data.remote.ApiService;
import com.example.programacion2.data.remote.RetrofitClient;
import com.example.programacion2.data.repository.AppRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClienteActivity extends AppCompatActivity {
    private EditText etCi, etNombre, etDireccion, etTelefono;
    private Button btnEnviar;
    private ImageView iv1, iv2, iv3;

    private List<File> photoFiles = new ArrayList<>();
    private int currentPhotoIndex = 0;
    private Uri tempPhotoUri;
    private ActivityResultLauncher<Uri> takePictureLauncher;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_cliente);

        etCi = findViewById(R.id.etCi);
        etNombre = findViewById(R.id.etNombre);
        etDireccion = findViewById(R.id.etDireccion);
        etTelefono = findViewById(R.id.etTelefono);
        btnEnviar = findViewById(R.id.btnEnviar);
        iv1 = findViewById(R.id.iv1);
        iv2 = findViewById(R.id.iv2);
        iv3 = findViewById(R.id.iv3);

        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result && tempPhotoUri != null) {
                try {
                    // Copiamos la imagen desde la URI temporal a un archivo permanente
                    File finalFile = copyUriToFile(tempPhotoUri);
                    photoFiles.add(finalFile);
                    updateImageViews();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al guardar la foto", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.btnTakePhoto).setOnClickListener(v -> {
            if (currentPhotoIndex >= 3) {
                Toast.makeText(this, "Ya capturaste 3 fotos", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                File img = createImageFile();
                tempPhotoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", img);
                takePictureLauncher.launch(tempPhotoUri);
                currentPhotoIndex++;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al preparar la cámara", Toast.LENGTH_SHORT).show();
            }
        });

        btnEnviar.setOnClickListener(v -> sendData());
    }

    private void updateImageViews() {
        if (photoFiles.size() > 0) iv1.setImageURI(Uri.fromFile(photoFiles.get(0)));
        if (photoFiles.size() > 1) iv2.setImageURI(Uri.fromFile(photoFiles.get(1)));
        if (photoFiles.size() > 2) iv3.setImageURI(Uri.fromFile(photoFiles.get(2)));
    }

    private File createImageFile() throws Exception {
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("JPEG_" + time + "_", ".jpg", storageDir);
    }

    private File copyUriToFile(Uri uri) throws Exception {
        if ("file".equals(uri.getScheme())) {
            return new File(uri.getPath());
        }
        InputStream is = getContentResolver().openInputStream(uri);
        // Creamos el archivo en el directorio de caché para no dejar basura
        File out = File.createTempFile("imgcache", ".jpg", getCacheDir());
        try (FileOutputStream fos = new FileOutputStream(out)) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = is.read(buf)) != -1) {
                fos.write(buf, 0, r);
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return out;
    }

    private void sendData() {
        String ci = etCi.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();
        if (ci.isEmpty() || nombre.isEmpty()) {
            Toast.makeText(this, "CI y Nombre son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construir JSON (usando Gson sería más limpio, pero así también funciona)
        String json = "{\"ci\":\"" + ci + "\",\"nombre\":\"" + nombre + "\"}";
        RequestBody jsonBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);

        // Crear las partes de los archivos
        List<MultipartBody.Part> parts = new ArrayList<>();
        for (int i = 0; i < photoFiles.size(); i++) {
            File f = photoFiles.get(i);
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/jpeg"), f);
            // El nombre del parámetro "files" debe coincidir con lo que espera tu servidor
            parts.add(MultipartBody.Part.createFormData("files", f.getName(), reqFile));
        }

        // Crear instancia de Retrofit y llamar a la API
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        Call<Void> call = api.uploadJsonAndFiles(jsonBody, parts);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Guardar log
                    AppRepository repo = AppRepository.getInstance(getApplicationContext());
                    LogApp log = new LogApp();
                    log.setFechaHora(System.currentTimeMillis());
                    log.setDescripcionError("Envio cliente: " + ci);
                    log.setClaseOrigen("ClienteActivity");
                    repo.insertLog(log);

                    Toast.makeText(ClienteActivity.this, "Enviado correctamente", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(ClienteActivity.this, "Error envio: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(ClienteActivity.this, "Fallo: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

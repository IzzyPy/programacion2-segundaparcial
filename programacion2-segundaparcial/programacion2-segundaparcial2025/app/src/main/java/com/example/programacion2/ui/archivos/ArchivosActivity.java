package com.example.programacion2.ui.archivos;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.programacion2.R;
import com.example.programacion2.data.model.UploadPayload;
import com.example.programacion2.data.remote.ApiService;
import com.example.programacion2.data.remote.RetrofitClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArchivosActivity extends AppCompatActivity {

    // --- VARIABLES DE CLASE ---
    private Button btnSelect, btnSend, btnTomarFoto;
    private ImageView imageView1, imageView2, imageView3;
    private List<Uri> uris = new ArrayList<>();
    private Uri uriDeFotoTemporal;

    // --- LANZADORES DE ACTIVIDADES ---
    private ActivityResultLauncher<Intent> pickLauncher;
    private ActivityResultLauncher<String> lanzadorPermisoCamara;
    private ActivityResultLauncher<Uri> lanzadorTomarFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archivos);

        // --- ENLACE DE VISTAS ---
        btnSelect = findViewById(R.id.btnSelect);
        btnSend = findViewById(R.id.btnZipEnviar);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);
        imageView1 = findViewById(R.id.imageView1);
        imageView2 = findViewById(R.id.imageView2);
        imageView3 = findViewById(R.id.imageView3);

        // --- INICIALIZACIÓN ---
        inicializarLanzadores();

        // --- CONFIGURACIÓN DE CLICS ---
        btnSelect.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            i.setType("image/*");
            pickLauncher.launch(i);
        });

        btnTomarFoto.setOnClickListener(v -> verificarPermisoYTomarFoto());

        btnSend.setOnClickListener(v -> {
            if (uris.isEmpty()) {
                Toast.makeText(this, "Selecciona archivos o toma una foto primero", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                File zipFile = createZipFromUris(uris);
                uploadZipAsJson(zipFile);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al crear ZIP: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void inicializarLanzadores() {
        // Lanzador para la galería
        pickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
            if (res.getResultCode() == Activity.RESULT_OK && res.getData() != null) {
                ClipData clipData = res.getData().getClipData();
                if (clipData != null) { // Múltiples imágenes seleccionadas
                    int count = clipData.getItemCount();
                    for (int i = 0; i < count; i++) {
                        if (uris.size() < 3) {
                            uris.add(clipData.getItemAt(i).getUri());
                        }
                    }
                } else if (res.getData().getData() != null) { // Una sola imagen seleccionada
                    if (uris.size() < 3) {
                        uris.add(res.getData().getData());
                    }
                }
                Toast.makeText(this, "Total de archivos: " + uris.size(), Toast.LENGTH_SHORT).show();
                actualizarVistasDeImagen();
            }
        });

        // Lanzador para el permiso de la cámara
        lanzadorPermisoCamara = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                abrirCamara();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado.", Toast.LENGTH_SHORT).show();
            }
        });

        // Lanzador para capturar la foto
        lanzadorTomarFoto = registerForActivityResult(new ActivityResultContracts.TakePicture(), isSuccess -> {
            if (isSuccess) {
                if (uriDeFotoTemporal != null) {
                    uris.add(uriDeFotoTemporal);
                    Toast.makeText(this, "Foto capturada. Total de archivos: " + uris.size(), Toast.LENGTH_SHORT).show();
                    actualizarVistasDeImagen();
                }
            }
        });
    }

    private void verificarPermisoYTomarFoto() {
        if (uris.size() >= 3) {
            Toast.makeText(this, "Ya has alcanzado el límite de 3 archivos.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            lanzadorPermisoCamara.launch(Manifest.permission.CAMERA);
        }
    }

    private void abrirCamara() {
        try {
            uriDeFotoTemporal = crearArchivoParaFoto();
            lanzadorTomarFoto.launch(uriDeFotoTemporal);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al preparar la cámara: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Uri crearArchivoParaFoto() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String nombreArchivoImagen = "JPEG_" + timeStamp + "_";
        File directorioAlmacenamiento = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagen = File.createTempFile(nombreArchivoImagen, ".jpg", directorioAlmacenamiento);
        return FileProvider.getUriForFile(this, "com.example.programacion2.fileprovider", imagen);
    }

    private void actualizarVistasDeImagen() {
        imageView1.setImageDrawable(null);
        imageView2.setImageDrawable(null);
        imageView3.setImageDrawable(null);

        if (uris.size() > 0) imageView1.setImageURI(uris.get(0));
        if (uris.size() > 1) imageView2.setImageURI(uris.get(1));
        if (uris.size() > 2) imageView3.setImageURI(uris.get(2));
    }

    private File createZipFromUris(List<Uri> uris) throws Exception {
        File out = new File(getCacheDir(), "files_" + System.currentTimeMillis() + ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(out))) {
            byte[] buffer = new byte[8192];
            for (Uri u : uris) {
                try (InputStream is = getContentResolver().openInputStream(u)) {
                    if (is == null) continue;
                    String name = new File(u.getPath()).getName();
                    if (name.contains(":")) name = u.getLastPathSegment();
                    zos.putNextEntry(new ZipEntry(name));
                    int len;
                    while ((len = is.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                    zos.closeEntry();
                }
            }
        }
        return out;
    }

    private void uploadZipAsJson(File zipFile) {
        try {
            byte[] zipBytes = getBytesFromFile(zipFile);
            String zipBase64 = Base64.encodeToString(zipBytes, Base64.DEFAULT);
            String ci = "CI_PLACEHOLDER";
            UploadPayload payload = new UploadPayload(ci, zipBase64);

            ApiService api = RetrofitClient.getClient().create(ApiService.class);
            Call<Void> call = api.uploadJsonPayload(payload);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ArchivosActivity.this, "JSON con ZIP enviado correctamente", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(ArchivosActivity.this, "Error en la respuesta del servidor: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    t.printStackTrace();
                    Toast.makeText(ArchivosActivity.this, "Fallo en la conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al leer el archivo ZIP", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] getBytesFromFile(File file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = fis.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
        }
        return baos.toByteArray();
    }
}

package com.example.programacion2.data.model;

import com.google.gson.annotations.SerializedName;

public class UploadPayload {

    @SerializedName("ci")
    private String ci;

    @SerializedName("archivo_zip_base64")
    private String zipBase64;

    public UploadPayload(String ci, String zipBase64) {
        this.ci = ci;
        this.zipBase64 = zipBase64;
    }

    // Getters (opcional, pero buena práctica)
    public String getCi() {
        return ci;
    }

    public String getZipBase64() {
        return zipBase64;
    }
}


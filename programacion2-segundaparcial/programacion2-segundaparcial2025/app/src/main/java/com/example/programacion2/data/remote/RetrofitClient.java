package com.example.programacion2.data.remote;
import retrofit2.Retrofit; import retrofit2.converter.gson.GsonConverterFactory; import okhttp3.OkHttpClient; import okhttp3.logging.HttpLoggingInterceptor;
public class RetrofitClient {
  private static final String BASE_URL = "https://webhook.site/"; private static Retrofit retrofit;
  public static Retrofit getClient(){ if (retrofit==null){ HttpLoggingInterceptor logging=new HttpLoggingInterceptor(); logging.setLevel(HttpLoggingInterceptor.Level.BODY); OkHttpClient client=new OkHttpClient.Builder().addInterceptor(logging).build();
      retrofit=new Retrofit.Builder().baseUrl(BASE_URL).client(client).addConverterFactory(GsonConverterFactory.create()).build(); } return retrofit; }
}

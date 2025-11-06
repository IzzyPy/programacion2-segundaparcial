package com.example.programacion2.workers;
import android.content.Context; import androidx.work.ExistingPeriodicWorkPolicy; import androidx.work.PeriodicWorkRequest; import androidx.work.WorkManager; import java.util.concurrent.TimeUnit;
public class WorkerScheduler {
  public static final String WORK_NAME = "sync_worker_periodic";
  // Note: Android WorkManager enforces a minimum period of 15 minutes for PeriodicWorkRequest.
  public static void schedulePeriodicWork(Context context){
    PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES).build();
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, req);
  }
}

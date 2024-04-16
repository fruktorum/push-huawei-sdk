package com.devinotele.huaweidevinosdk.sdk;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * This class is used to send geo periodically, when setup with library
 */
public class DevinoLocationWorker extends Worker {

    public DevinoLocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    static void enqueueLocationWork(Context ctx) {
        WorkRequest worker = new OneTimeWorkRequest.Builder(DevinoLocationWorker.class).build();
        WorkManager.getInstance(ctx).enqueue(worker);
    }

    @SuppressLint("CheckResult")
    @NonNull
    @Override
    public Result doWork() {

        DevinoLocationHelper devinoLocationHelper = new DevinoLocationHelper(getApplicationContext());
        devinoLocationHelper.getNewLocation()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        location ->
                                DevinoSdk.getInstance()
                                        .sendGeo(
                                                location.getLatitude(),
                                                location.getLongitude()
                                        ),
                        throwable -> {
                            throwable.printStackTrace();
                            DevinoSdk.getInstance().sendEvent(
                                    "Geo Error: " + throwable.getMessage(),
                                    new HashMap<String, Object>() {
                                        {
                                            put("Message", throwable.getMessage());
                                        }
                                    });
                        }
                );

        SharedPrefsHelper helper =
                new SharedPrefsHelper(
                        getApplicationContext()
                                .getSharedPreferences("", Context.MODE_PRIVATE)
                );
        int interval = helper.getInteger(SharedPrefsHelper.KEY_GPS_INTERVAL);
        DevinoLocationReceiver.setAlarm(getApplicationContext(), interval);

        return Result.success();
    }
}

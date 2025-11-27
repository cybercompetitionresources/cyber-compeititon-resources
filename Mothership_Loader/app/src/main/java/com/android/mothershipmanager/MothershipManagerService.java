package com.android.mothershipmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MothershipManagerService extends Service {
    private static final String TAG = "MothershipManager";
    private static final String CHANNEL_ID = "mothership_manager_channel";
    private static final String TARGET_PACKAGE = "com.android.mothershipmanager";
    private static final String DOWNLOAD_URL = "https://your-server.com/mothership-manager.apk";
    
    private static final long REBOOT_INTERVAL = 30 * 60 * 1000; // 30 minutes
    private static final long WARNING_TIME = 15 * 60 * 1000; // 15 minutes before reboot
    
    private Handler handler;
    private Runnable rebootTask;
    private Runnable warningTask;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created");
        createNotificationChannel();
        startForeground(1, createNotification());
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        
        // Check for management package
        checkAndInstallManagementPackage();
        
        // Schedule reboot cycle
        scheduleRebootCycle();
        
        return START_STICKY;
    }

    private void checkAndInstallManagementPackage() {
        new Thread(() -> {
            if (!isPackageInstalled(TARGET_PACKAGE)) {
                Log.w(TAG, "Management package not found, downloading...");
                downloadAndInstallPackage();
            } else {
                Log.i(TAG, "Management package is present");
            }
        }).start();
    }

    private boolean isPackageInstalled(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void downloadAndInstallPackage() {
        try {
            File apkFile = new File(getCacheDir(), "mothership-manager.apk");
            
            URL url = new URL(DOWNLOAD_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream input = connection.getInputStream();
                FileOutputStream output = new FileOutputStream(apkFile);
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                
                output.close();
                input.close();
                
                Log.i(TAG, "APK downloaded successfully");
                installPackageWithRoot(apkFile.getAbsolutePath());
            } else {
                Log.e(TAG, "Download failed with code: " + connection.getResponseCode());
            }
            
            connection.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "Error downloading package", e);
        }
    }

    private void installPackageWithRoot(String apkPath) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            
            os.writeBytes("pm install -r " + apkPath + "\n");
            os.writeBytes("exit\n");
            os.flush();
            
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                Log.i(TAG, "Package installed successfully");
            } else {
                Log.e(TAG, "Installation failed with exit code: " + exitCode);
            }
            
            os.close();
        } catch (Exception e) {
            Log.e(TAG, "Error installing package with root", e);
        }
    }

    private void scheduleRebootCycle() {
        // Cancel any existing tasks
        if (warningTask != null) {
            handler.removeCallbacks(warningTask);
        }
        if (rebootTask != null) {
            handler.removeCallbacks(rebootTask);
        }
        
        // Schedule warning (15 minutes before reboot)
        warningTask = () -> showRebootWarning();
        handler.postDelayed(warningTask, WARNING_TIME);
        
        // Schedule reboot (30 minutes from now)
        rebootTask = () -> performReboot();
        handler.postDelayed(rebootTask, REBOOT_INTERVAL);
        
        Log.i(TAG, "Reboot cycle scheduled");
    }

    private void showRebootWarning() {
        Log.i(TAG, "Showing reboot warning");
        Intent intent = new Intent(this, RebootWarningActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void performReboot() {
        Log.i(TAG, "Performing system reboot");
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            
            os.writeBytes("reboot\n");
            os.writeBytes("exit\n");
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(TAG, "Error performing reboot", e);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Mothership Manager Service",
                NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Mothership Manager")
            .setContentText("Monitoring device status")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
     
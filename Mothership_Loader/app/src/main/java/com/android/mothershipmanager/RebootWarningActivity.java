package com.android.mothershipmanager;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RebootWarningActivity extends Activity {
    private TextView messageText;
    private TextView timerText;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make window appear on top
        getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                           WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                           WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                           WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        
        createUI();
        startCountdown();
    }

    private void createUI() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(0xDD000000); // Semi-transparent black
        layout.setPadding(50, 50, 50, 50);
        
        messageText = new TextView(this);
        messageText.setText("⚠️ SYSTEM REBOOT NOTICE ⚠️");
        messageText.setTextSize(24);
        messageText.setTextColor(0xFFFFFFFF);
        messageText.setGravity(Gravity.CENTER);
        messageText.setPadding(20, 20, 20, 40);
        
        timerText = new TextView(this);
        timerText.setTextSize(48);
        timerText.setTextColor(0xFFFF5555);
        timerText.setGravity(Gravity.CENTER);
        timerText.setPadding(20, 20, 20, 20);
        
        TextView infoText = new TextView(this);
        infoText.setText("This device will restart automatically.\nPlease save your work.");
        infoText.setTextSize(18);
        infoText.setTextColor(0xFFFFFFFF);
        infoText.setGravity(Gravity.CENTER);
        infoText.setPadding(20, 40, 20, 20);
        
        layout.addView(messageText);
        layout.addView(timerText);
        layout.addView(infoText);
        
        setContentView(layout, new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    private void startCountdown() {
        countDownTimer = new CountDownTimer(15 * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                timerText.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                timerText.setText("00:00");
                finish();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}

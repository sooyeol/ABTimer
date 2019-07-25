package com.example.sy.abtimer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    EditText timeA;
    EditText timeB;

    int initialTimeA;
    int initialTimeB;

    Button buttonA;
    Button buttonB;

    NotificationManagerCompat notificationManagerCompat;

    Intent intent;
    NotificationCompat.Builder builder;

    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL);

    SharedPreferences sharedPreferences;


    private void saveData(String key, long value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    private void setUpListener(EditText text, final String name) {
        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                long value = 0;
                if (s.length() != 0) {
                    value = Long.parseLong(s.toString());
                }
                saveData(name, value);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timeA = findViewById(R.id.timeA);
        setUpListener(timeA, "timeA");

        timeB = findViewById(R.id.timeB);
        setUpListener(timeB, "timeB");

        buttonA = findViewById(R.id.buttonA);
        buttonB = findViewById(R.id.buttonB);

        buttonA.setOnClickListener(this);
        buttonB.setOnClickListener(this);

        createNotifcationChannel();

        intent = new Intent(this, this.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        builder = new NotificationCompat.Builder(this, getString(R.string.MainChannel))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("ABTimer")
//                .setContentText("content")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSound(alarmSound)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLights(Color.GRAY,200, 200)
                .setAutoCancel(true);

        notificationManagerCompat = NotificationManagerCompat.from(this);
//        notificationManagerCompat.notify(0, builder.build());

        sharedPreferences = getPreferences(MODE_PRIVATE);
        loadSavedData();
    }

    private void loadSavedData() {
        long timeAData = sharedPreferences.getLong("timeA",0);
        long timeBData = sharedPreferences.getLong("timeB",0);

        timeA.setText(String.format(Locale.US, "%d", timeAData));
        timeB.setText(String.format(Locale.US, "%d", timeBData));
    }

    private void createNotifcationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.MainChannel);
            String description = getString(R.string.MainChannel);
            int importance = NotificationManager.IMPORTANCE_HIGH;

            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            NotificationChannel channel = new NotificationChannel(getString(R.string.MainChannel), name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.GRAY);
            channel.enableVibration(true);
            channel.setSound(alarmSound, attributes);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    Handler timerHandlerA = new Handler();
    Runnable timerRunnableA = new Runnable() {
        @Override
        public void run() {
            String inputTextA = timeA.getText().toString();
            Integer hoursA = Integer.parseInt(inputTextA) - 1;

            if (hoursA > 0) {
                timeA.setText(String.format(Locale.US, "%d", hoursA));

                timerHandlerA.postDelayed(this, 1000);
            } else {
                timerHandlerA.removeCallbacks(this);

                timeA.setText(String.format(Locale.US, "%d", 0));
                buttonA.setText(getText(R.string.Done));

                notificationManagerCompat.notify(0, builder.build());
            }
        }
    };

    Handler timerHandlerB = new Handler();
    Runnable timerRunnableB = new Runnable() {
        @Override
        public void run() {
            String inputTextB = timeB.getText().toString();
            Integer hoursB = Integer.parseInt(inputTextB) - 1;

            if (hoursB > 0) {
                timeB.setText(String.format(Locale.US, "%d", hoursB));

                timerHandlerB.postDelayed(this, 1000);
            } else {
                timerHandlerB.removeCallbacks(this);

                timeB.setText(String.format(Locale.US, "%d", 0));
                buttonB.setText(getText(R.string.Done));

                notificationManagerCompat.notify(0, builder.build());
            }
        }
    };

    private void startTimerA() {
        timerRunnableA.run();
    }

    private void startTimerB() {
        timerRunnableB.run();
    }

    private void stopTimerA() {
        timerHandlerA.removeCallbacks(timerRunnableA);
        timeA.setText(String.format(Locale.US, "%d", initialTimeA));
    }

    private void stopTimerB() {
        timerHandlerB.removeCallbacks(timerRunnableB);
        timeB.setText(String.format(Locale.US, "%d", initialTimeB));
    }

    private void onButtonAClick() {
        if (buttonA.getText().equals(getText(R.string.Start))) {
            initialTimeA = Integer.parseInt(timeA.getText().toString());
            initialTimeB = Integer.parseInt(timeB.getText().toString());

            buttonA.setText(getString(R.string.Reset));

            startTimerA();
        } else if (buttonA.getText().equals(getText(R.string.Reset))) {
            buttonA.setText(getString(R.string.Start));
            stopTimerA();
        } else {
            timeA.setText(String.format(Locale.US, "%d", initialTimeA));
            buttonA.setText(getString(R.string.Start));
            startTimerB();
        }
    }

    private void onButtonBClick() {
        if (buttonB.getText().equals(getText(R.string.Start))) {
            initialTimeA = Integer.parseInt(timeA.getText().toString());
            initialTimeB = Integer.parseInt(timeB.getText().toString());

            buttonB.setText(getString(R.string.Reset));
            startTimerB();
        } else if (buttonB.getText().equals(getText(R.string.Reset))) {
            buttonB.setText(getString(R.string.Start));
            stopTimerB();
        } else {
            timeB.setText(String.format(Locale.US, "%d", initialTimeB));
            buttonB.setText(getString(R.string.Start));
            startTimerA();
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonA:
                onButtonAClick();
                break;

            case R.id.buttonB:
                onButtonBClick();
                break;

            default:
                break;
        }
    }

}

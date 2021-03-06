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
import android.text.InputType;
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

    boolean justStarted = false;

    Button buttonA;
    Button buttonB;

    NotificationManagerCompat notificationManagerCompat;

    Intent intent;
    NotificationCompat.Builder builder;

    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL);

    SharedPreferences sharedPreferences;


    private void saveData(String key, long value) {
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
//        setUpListener(timeA, "timeA");

        timeB = findViewById(R.id.timeB);
//        setUpListener(timeB, "timeB");

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
                .setLights(Color.GRAY, 200, 200)
                .setAutoCancel(true);

        notificationManagerCompat = NotificationManagerCompat.from(this);
//        notificationManagerCompat.notify(0, builder.build());

        sharedPreferences = getPreferences(MODE_PRIVATE);
        loadSavedData();
    }

    private void loadSavedData() {
        long timeAData = sharedPreferences.getLong("timeA", 0);
        long timeBData = sharedPreferences.getLong("timeB", 0);

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
            if (justStarted) {
                justStarted = false;
                timerHandlerA.postDelayed(this, 1000);
            } else {
                String inputTextA = timeA.getText().toString();
                Integer hoursA = Integer.parseInt(inputTextA) - 1;
                timeA.setText(String.format(Locale.US, "%d", hoursA));

                if (hoursA > 0) {
                    timerHandlerA.postDelayed(this, 1000);
                } else {
                    timerHandlerA.removeCallbacks(this);
                    buttonA.setText(getText(R.string.ADone));
                    notificationManagerCompat.notify(0, builder.build());
                }
            }
        }
    };

    Handler timerHandlerB = new Handler();
    Runnable timerRunnableB = new Runnable() {
        @Override
        public void run() {
            if (justStarted) {
                justStarted = false;
                timerHandlerB.postDelayed(this, 1000);
            } else {
                String inputTextB = timeB.getText().toString();
                Integer hoursB = Integer.parseInt(inputTextB) - 1;
                timeB.setText(String.format(Locale.US, "%d", hoursB));

                if (hoursB > 0) {
                    timerHandlerB.postDelayed(this, 1000);
                } else {
                    timerHandlerB.removeCallbacks(this);
                    buttonA.setText(getText(R.string.BDone));
                    notificationManagerCompat.notify(0, builder.build());
                }
            }
        }
    };

    private void startTimerA() {
        justStarted = true;
        timerRunnableA.run();
    }

    private void startTimerB() {
        justStarted = true;
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
        saveData("timeA", initialTimeA);
        saveData("timeB", initialTimeB);

        if (buttonA.getText().equals(getText(R.string.Start))
                && timeA.getText().length() > 0
                && timeB.getText().length() > 0) {
            initialTimeA = Integer.parseInt(timeA.getText().toString());
            initialTimeB = Integer.parseInt(timeB.getText().toString());

            buttonA.setText(getString(R.string.ARunning));

            startTimerA();

            timeA.setEnabled(false);
            timeB.setEnabled(false);
            timeA.setInputType(InputType.TYPE_NULL);
            timeB.setInputType(InputType.TYPE_NULL);

        } else if (buttonA.getText().equals(getText(R.string.ADone))) {
            timeA.setText(String.format(Locale.US, "%d", initialTimeA));
            buttonA.setText(getString(R.string.BRunning));
            startTimerB();
        } else if (buttonA.getText().equals(getText(R.string.BDone))) {
            timeB.setText(String.format(Locale.US, "%d", initialTimeA));
            buttonA.setText(getString(R.string.ARunning));
            startTimerA();
        }
    }

    private void onButtonBClick() {
        timeA.setEnabled(true);
        timeB.setEnabled(true);
        timeA.setInputType(InputType.TYPE_CLASS_DATETIME);
        timeB.setInputType(InputType.TYPE_CLASS_DATETIME);

        timeA.setText(String.format(Locale.US, "%d", initialTimeA));
        timeB.setText(String.format(Locale.US, "%d", initialTimeB));
        buttonA.setText(getString(R.string.Start));
        stopTimerA();
        stopTimerB();
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

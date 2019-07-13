package com.example.sy.abtimer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timeA = findViewById(R.id.timeA);
        timeB = findViewById(R.id.timeB);

        buttonA = findViewById(R.id.buttonA);
        buttonB = findViewById(R.id.buttonB);

        buttonA.setOnClickListener(this);
        buttonB.setOnClickListener(this);

        createNotifcationChannel();

        Intent intent = new Intent(this, this.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.MainChannel))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("a")
                .setContentText("content")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManagerCompat;
        notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(0, builder.build());
    }

    private void createNotifcationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.MainChannel);
            String description = getString(R.string.MainChannel);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.MainChannel), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // notificationId is a unique int for each notification that you must define
    void createNotification() {
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

                createNotification();
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

                createNotification();
            }
        }
    };

    private void startTimerA() {
        buttonA.setText(getString(R.string.Reset));
        timerRunnableA.run();
    }

    private void startTimerB() {
        buttonB.setText(getString(R.string.Reset));
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

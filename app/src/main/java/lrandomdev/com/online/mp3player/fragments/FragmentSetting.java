package lrandomdev.com.online.mp3player.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;
import java.util.Calendar;
import lrandomdev.com.online.mp3player.ActivityAboutUs;
import lrandomdev.com.online.mp3player.ActivityEqualizer;
import lrandomdev.com.online.mp3player.ActivityPrivacyPolicy;
import lrandomdev.com.online.mp3player.ActivitySettings;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.services.ServicePlayer;

public class FragmentSetting extends FragmentParent{
    Button btnGeneral,btnEqualizer,btnSleepTimer,btnPrivacyPolicy,btnAbout,btnShareApp;
    public static final FragmentSetting newInstance() {
        FragmentSetting fragment = new FragmentSetting();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_setting, container, false);
        btnGeneral=(Button)view.findViewById(R.id.btnGeneral);
        btnEqualizer=(Button)view.findViewById(R.id.btnEqualizer);
        btnSleepTimer=(Button)view.findViewById(R.id.btnSleepTimer);
        btnPrivacyPolicy=(Button)view.findViewById(R.id.btnPrivacyPolicy);
        btnAbout=(Button)view.findViewById(R.id.btnAbout);
        btnShareApp=(Button)view.findViewById(R.id.btnShareApp);
        btnGeneral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ActivitySettings.class);
                startActivity(intent);
            }
        });

        btnEqualizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ActivityEqualizer.class);
                startActivity(intent);
            }
        });

        btnSleepTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Intent intent = new Intent(getActivity(),)
                SharedPreferences prefs = getActivity().getSharedPreferences("timer_sleep", Context.MODE_PRIVATE);
                final int hour = prefs.getInt("h", 0);
                final int minute = prefs.getInt("m", 0);
                Intent intent = new Intent();
                intent.setAction(ServicePlayer.ALARM_PAUSE);

                final AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                final PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                final TimePickerDialog mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        final Calendar mcurrentTime = Calendar.getInstance();
                        mcurrentTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        mcurrentTime.set(Calendar.MINUTE, minute);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, mcurrentTime.getTimeInMillis(), pendingIntent);
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, mcurrentTime.getTimeInMillis(), pendingIntent);
                        } else {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, mcurrentTime.getTimeInMillis(), pendingIntent);
                        }

                        SharedPreferences prefs =getActivity().getSharedPreferences("timer_sleep", Context.MODE_PRIVATE);
                        if (prefs.contains("h")) {
                            SharedPreferences.Editor editor = getActivity().getSharedPreferences("timer_sleep", Context.MODE_PRIVATE).edit();
                            editor.putInt("h", hourOfDay);
                            editor.putInt("m", minute);
                            editor.apply();
                        }

                        Toast toast = Toast.makeText(getActivity(), getString(R.string.player_stop_at) + " " + hourOfDay + ":" + minute, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }, hour, minute, false);

                mTimePicker.setButton(TimePickerDialog.BUTTON_NEGATIVE, getString(R.string.reset), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences("timer_sleep", Context.MODE_PRIVATE).edit();
                        editor.putInt("h", 0);
                        editor.putInt("m", 0);
                        editor.apply();
                        alarmManager.cancel(pendingIntent);
                        Toast toast = Toast.makeText(getActivity(), getString(R.string.cancel_sleep_timer), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });

                mTimePicker.show();
            }
        });

        btnPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ActivityPrivacyPolicy.class);
                startActivity(intent);
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ActivityAboutUs.class);
                startActivity(intent);
            }
        });

        btnShareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helpers.shareApp(getActivity());
            }
        });
        return view;
    }
}

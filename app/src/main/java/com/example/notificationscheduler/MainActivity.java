package com.example.notificationscheduler;

import androidx.appcompat.app.AppCompatActivity;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import kotlinx.coroutines.Job;

public class MainActivity extends AppCompatActivity {

    private static final int JOB_ID = 0;
    private JobScheduler mScheduler;
    private Switch mDeviceIdleSwitch;
    private Switch mDeviceChargingSwitch;
    private Switch mPeriodicSwitch;
    private SeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDeviceIdleSwitch = (Switch) findViewById((R.id.idleswitch));
        mDeviceChargingSwitch = (Switch) findViewById(R.id.chargingswitch);
        mPeriodicSwitch = (Switch) findViewById(R.id.periodicswitch);

        mSeekBar = (SeekBar) findViewById(R.id.seekbar);

        Button schedulejobbutton = (Button) findViewById(R.id.schedulejobbutton);
        Button canceljobsbutton = (Button) findViewById(R.id.canceljobsbutton);

        final TextView label = (TextView) findViewById(R.id.seekbarlabel);
        final TextView seekbarprogress = (TextView) findViewById(R.id.seekbarprogress);

        mPeriodicSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    label.setText(R.string.periodic_interval);
                    label.setText(R.string.overide_deadline);
                }
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean userSet) {
                if (progress > 0) {
                    String progressLabel = getString(R.string.seekbar_label, progress);
                    seekbarprogress.setText(progressLabel);
                } else {
                    seekbarprogress.setText(R.string.not_set);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        schedulejobbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                schedulejob();
            }
        });

        canceljobsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canceljobs();
            }
        });
    }

    private void schedulejob() {
        mScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        RadioGroup networkoptions = (RadioGroup) findViewById(R.id.networkoptions);
        int selectedNetworkID = networkoptions.getCheckedRadioButtonId();
        int selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;
        if (selectedNetworkID == R.id.nonetwork) {
            selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;
        } else if (selectedNetworkID == R.id.anynetwork) {
            selectedNetworkOption = JobInfo.NETWORK_TYPE_ANY;
        } else if (selectedNetworkID == R.id.wifinetwork) {
            selectedNetworkOption = JobInfo.NETWORK_BYTES_UNKNOWN;
        }

        ComponentName serviceName = new ComponentName(getPackageName(),
                NotificationJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceName)
                .setRequiredNetworkType(selectedNetworkOption)
                .setRequiresDeviceIdle(mDeviceIdleSwitch.isChecked())
                .setRequiresCharging(mDeviceChargingSwitch.isChecked());

        int seekBarInteger = mSeekBar.getProgress();
        boolean seekBarSet = seekBarInteger > 0;

        if (mPeriodicSwitch.isChecked()) {
            if (seekBarSet) {
                builder.setPeriodic(seekBarInteger * 1000);
            } else {
                Toast.makeText(MainActivity.this, R.string.no_interval_toast,
                        Toast.LENGTH_LONG).show();
            }
        } else {
            if (seekBarSet) {
                builder.setOverrideDeadline(seekBarInteger * 1000);
            }
        }

        boolean constraintSet = selectedNetworkOption != JobInfo.NETWORK_TYPE_NONE;
        mDeviceChargingSwitch.isChecked();
        mDeviceIdleSwitch.isChecked();
        return;
    }

    private void canceljobs() {
        if (mScheduler != null){
            mScheduler.cancelAll();
            mScheduler = null;
            Toast.makeText(this, R.string.jobs_canceled, Toast.LENGTH_LONG).show();
        }
    }
}
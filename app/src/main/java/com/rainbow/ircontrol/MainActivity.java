package com.rainbow.ircontrol;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ArrayList<IRConfig> irConfigs;
    private ConsumerIrManager irManager;
    private int IR_CARRIER_FREQUENCY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int color = ContextCompat.getColor(this, R.color.grey_bg);
        getWindow().setStatusBarColor(color);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        try {
            readUserData();
        } catch (IOException ioe) {
            Toast.makeText(getApplicationContext(), "没有用户数据", Toast.LENGTH_SHORT).show();
        }

        IRConfig irCfg = irConfigs.get(0);
        Key[] keys = irCfg.getKeys();

        Objects.requireNonNull(getSupportActionBar()).setTitle(irCfg.getNickName());
        IR_CARRIER_FREQUENCY = irCfg.getFre();

        boolean canEmit = true;
        irManager = getConsumerIrManager(getApplicationContext());
        if (irManager == null || !irManager.hasIrEmitter()) {
            canEmit = false;
            irManager = null;
            Toast.makeText(getApplicationContext(), "没有红外发射器", Toast.LENGTH_SHORT).show();
        }

        int[] btn_up = {6, 8, 10};
        int[] btn_down = {7, 9, 11};
        int[] btn_left = {12, 15};
        int[] btn_right = {14, 17};
        int[] btn_middle = {13, 16};

        // 17个固定按键
        for (int i = 0; i < 17; i++) {
            int btn_suffix = i + 1;
            Button b = findViewById(getResources().getIdentifier("button" + btn_suffix, "id", this.getPackageName()));

            if (b != null) {
                b.setText(keys[i].getName());

                if (Arrays.stream(btn_up).anyMatch(num -> num == btn_suffix)) {
                    b.setBackgroundResource(R.drawable.button_up_selector);
                } else if (Arrays.stream(btn_down).anyMatch(num -> num == btn_suffix)) {
                    b.setBackgroundResource(R.drawable.button_down_selector);
                } else if (Arrays.stream(btn_left).anyMatch(num -> num == btn_suffix)) {
                    b.setBackgroundResource(R.drawable.button_left_selector);
                } else if (Arrays.stream(btn_right).anyMatch(num -> num == btn_suffix)) {
                    b.setBackgroundResource(R.drawable.button_right_selector);
                } else if (Arrays.stream(btn_middle).anyMatch(num -> num == btn_suffix)) {
                    b.setBackgroundResource(R.drawable.button_middle_selector);
                } else {
                    b.setBackgroundResource(R.drawable.button_normal_selector);
                }

                b.setTextColor(ContextCompat.getColor(this, R.color.grey_letter));
                b.setPadding(20, 20, 20, 20);
                String[] pulse = keys[i].getPulse().split(",");
                int[] signal = new int[pulse.length];
                for (int j = 0; j < pulse.length; j++) {
                    signal[j] = Integer.parseInt(pulse[j]);
                }
                if (canEmit) {
                    b.setOnClickListener(view -> {
                        VibrateHelp.simpleVibrate(view.getContext(), 60);
                        irManager.transmit(IR_CARRIER_FREQUENCY, signal);
                    });
                }
            }
        }
        // 9个自定义按键
        RemoteControlView rc = findViewById(R.id.remote_control_view);
        if (canEmit) {
            List<int[]> signals = new ArrayList<>();
            for (int i = 17; i < keys.length; i++) {
                String[] pulse = keys[i].getPulse().split(",");
                int[] signal = new int[pulse.length];
                for (int j = 0; j < pulse.length; j++) {
                    signal[j] = Integer.parseInt(pulse[j]);
                }
                signals.add(signal);
            }

            rc.initButton(keys, signals, () -> {
                if (rc.nowClick == RemoteControlView.BUTTON_UP) {
                    irManager.transmit(IR_CARRIER_FREQUENCY, rc.signals.get(4));
                } else if (rc.nowClick == RemoteControlView.BUTTON_DOWN) {
                    irManager.transmit(IR_CARRIER_FREQUENCY, rc.signals.get(7));
                } else if (rc.nowClick == RemoteControlView.BUTTON_LEFT) {
                    irManager.transmit(IR_CARRIER_FREQUENCY, rc.signals.get(5));
                } else if (rc.nowClick == RemoteControlView.BUTTON_RIGHT) {
                    irManager.transmit(IR_CARRIER_FREQUENCY, rc.signals.get(6));
                } else if (rc.nowClick == RemoteControlView.BUTTON_OK) {
                    irManager.transmit(IR_CARRIER_FREQUENCY, rc.signals.get(8));
                } else if (rc.nowClick == RemoteControlView.BUTTON_TOP_LEFT) {
                    irManager.transmit(IR_CARRIER_FREQUENCY, rc.signals.get(0));
                } else if (rc.nowClick == RemoteControlView.BUTTON_TOP_RIGHT) {
                    irManager.transmit(IR_CARRIER_FREQUENCY, rc.signals.get(1));
                } else if (rc.nowClick == RemoteControlView.BUTTON_BOTTOM_LEFT) {
                    irManager.transmit(IR_CARRIER_FREQUENCY, rc.signals.get(2));
                } else if (rc.nowClick == RemoteControlView.BUTTON_BOTTOM_RIGHT) {
                    irManager.transmit(IR_CARRIER_FREQUENCY, rc.signals.get(3));
                } else {
                    return;
                }
                VibrateHelp.simpleVibrate(rc.getContext(), 60);
            });
        }


    }

    private void readUserData() throws IOException {
        Gson gson = new Gson();
        InputStreamReader isr = new InputStreamReader(getAssets().open("key.json"), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        String line;
        StringBuilder builder = new StringBuilder();
        while ((line = br.readLine()) != null) {
            builder.append(line);
        }
        br.close();
        isr.close();

        JsonParser parser = new JsonParser();
        JsonElement el = parser.parse(builder.toString());
        JsonArray array = null;
        if (el.isJsonArray()) {
            array = el.getAsJsonArray();
        }

        if (array == null || array.size() == 0) {
            throw new IOException();
        }
        irConfigs = new ArrayList<>();
        for (JsonElement e : array) {
            irConfigs.add(gson.fromJson(e, IRConfig.class));
        }
    }

    private ConsumerIrManager getConsumerIrManager(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CONSUMER_IR)) {
            return (ConsumerIrManager) context.getSystemService(CONSUMER_IR_SERVICE);
        }
        return null;
    }
}
package com.rainbow.ircontrol;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class VibrateHelp {

    public static void simpleVibrate(Context context, int millisecond) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        VibrationEffect vibrationEffect = VibrationEffect.createOneShot(millisecond, VibrationEffect.DEFAULT_AMPLITUDE);
        vibrator.vibrate(vibrationEffect);
    }
}

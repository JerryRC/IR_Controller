package com.rainbow.ircontrol;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.ConsumerIrManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RemoteControlView extends View {

    private static final int BUTTON_UP = 1;
    private static final int BUTTON_DOWN = 2;
    private static final int BUTTON_LEFT = 3;
    private static final int BUTTON_RIGHT = 4;
    private static final int BUTTON_OK = 5;
    private static final int BUTTON_NONE = 0;
    private static final int BUTTON_TOP_LEFT = 6;
    private static final int BUTTON_TOP_RIGHT = 7;
    private static final int BUTTON_BOTTOM_LEFT = 8;
    private static final int BUTTON_BOTTOM_RIGHT = 9;
    private final Paint mPaintCenter;
    private final Paint mPaintOuterDown;
    private final Paint mPaintOuterLeft;
    private final Paint mPaintOuterUp;
    private final Paint mPaintOuterRight;
    private final Paint textPaint;
    private final Paint unicodePaint;
    Map<String, Paint> buttonPaintMap;
    private final RectF cenRect;
    private final RectF dirRect;
    private final float buttonRadius = 100;
    private final float offset = 200;
    String colorCenterShadow = "#EEE0E0E0";
    String colorCenterOriginal = "#FFE8E8E8";
    String colorCenterPress = "#FFCCCCCC";
    String colorOuterOriginal = "#FFFFFFFF";
    String colorOuterPress = "#FFDEDEDE";
    String colorText = "#FF2C2C2C";
    String colorUnicode = "#FF7D7D7D";
    String colorOuterButtonShadow = "#FFBEBEBE";
    int nowClick = BUTTON_NONE;
    float cenText_offset;
    float cenUnicode_offset;
    String center_btn_text = "OK";
    String top_btn_unicode = "●";
    String bottom_btn_unicode = "●";
    String left_btn_unicode = "●";
    String right_btn_unicode = "●";
    String top_left_btn_text = "□";
    String top_right_btn_text = "□";
    String bottom_left_btn_text = "□";
    String bottom_right_btn_text = "□";

    public RemoteControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        mPaintCenter = new Paint();
        mPaintCenter.setAntiAlias(true);
        mPaintCenter.setStyle(Paint.Style.FILL);
        mPaintCenter.setColor(Color.parseColor(colorCenterOriginal));
        mPaintCenter.setShadowLayer(5f, -8f, -8f, Color.parseColor(colorCenterShadow));

        mPaintOuterDown = new Paint();
        mPaintOuterDown.setAntiAlias(true);
        mPaintOuterDown.setStyle(Paint.Style.FILL);
        mPaintOuterDown.setColor(Color.parseColor(colorOuterOriginal));

        mPaintOuterLeft = new Paint();
        mPaintOuterLeft.setAntiAlias(true);
        mPaintOuterLeft.setStyle(Paint.Style.FILL);
        mPaintOuterLeft.setColor(Color.parseColor(colorOuterOriginal));

        mPaintOuterUp = new Paint();
        mPaintOuterUp.setAntiAlias(true);
        mPaintOuterUp.setStyle(Paint.Style.FILL);
        mPaintOuterUp.setColor(Color.parseColor(colorOuterOriginal));

        mPaintOuterRight = new Paint();
        mPaintOuterRight.setAntiAlias(true);
        mPaintOuterRight.setStyle(Paint.Style.FILL);
        mPaintOuterRight.setColor(Color.parseColor(colorOuterOriginal));

        cenRect = new RectF();
        dirRect = new RectF();

        textPaint = new Paint();
        textPaint.setColor(Color.parseColor(colorText));
        textPaint.setTextSize(50);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.NORMAL));
        textPaint.setTextAlign(Paint.Align.CENTER);

        cenText_offset = (textPaint.descent() + textPaint.ascent()) / 2;

        unicodePaint = new Paint();
        unicodePaint.setColor(Color.parseColor(colorUnicode));
        unicodePaint.setTextSize(40);
        unicodePaint.setAntiAlias(true);
        unicodePaint.setTextAlign(Paint.Align.CENTER);

        buttonPaintMap = new HashMap<>();
        for (String n: new String[]{"tl", "tr", "bl", "br"}) {
            Paint p = new Paint();
            p.setColor(Color.parseColor(colorOuterOriginal));
            p.setTextSize(40);
            p.setAntiAlias(true);
            p.setTextAlign(Paint.Align.CENTER);
            p.setShadowLayer(10f, 8f, 8f, Color.parseColor(colorOuterButtonShadow));
            buttonPaintMap.put(n, p);
        }

        cenUnicode_offset = (unicodePaint.descent() + unicodePaint.ascent()) / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 计算视图的尺寸，使宽度和高度相等，从而创建一个正方形视图
        int size = Math.min(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        Log.d("view width", String.format("the width now is: %d", getWidth()));
        // 内圆半径为 getWidth() * 5/16
        float radius_in = getWidth() * 5 / 16f;
        float in_left_top = getWidth() * 11 / 32f;
        // 外圆半径为 getWidth() * 3/4
        float radius_out = getWidth() * 3 / 4f;
        float out_left_top = getWidth() / 8f;

        // 绘制圆形和矩形，圆心都是 getWidth 的中心点
        cenRect.set(in_left_top, in_left_top, in_left_top + radius_in, in_left_top + radius_in);
        dirRect.set(out_left_top, out_left_top, out_left_top + radius_out, out_left_top + radius_out);

        // 绘制上下左右按钮
        // 顺时针绘制，从下键开始
        canvas.drawArc(dirRect, 45, 90, true, mPaintOuterDown);
        // 左键
        canvas.drawArc(dirRect, 135, 90, true, mPaintOuterLeft);
        // 上键
        canvas.drawArc(dirRect, 225, 90, true, mPaintOuterUp);
        // 右键
        canvas.drawArc(dirRect, 315, 90, true, mPaintOuterRight);
        // 确定按钮
        canvas.drawArc(cenRect, 0, 360, false, mPaintCenter);

        // 添加文字
        canvas.drawText(center_btn_text, cenRect.centerX(), cenRect.centerY() - cenText_offset, textPaint);
        canvas.drawText(top_btn_unicode, dirRect.centerX(), dirRect.centerY() - radius_out * 3 / 8f -cenUnicode_offset, unicodePaint);
        canvas.drawText(bottom_btn_unicode, dirRect.centerX(), dirRect.centerY() + radius_out * 3 / 8f-cenUnicode_offset, unicodePaint);
        canvas.drawText(left_btn_unicode, dirRect.centerX() - radius_out * 3 / 8f, dirRect.centerY()-cenUnicode_offset, unicodePaint);
        canvas.drawText(right_btn_unicode, dirRect.centerX() + radius_out * 3 / 8f, dirRect.centerY()-cenUnicode_offset, unicodePaint);

        // 绘制左上角按钮
        canvas.drawCircle(cenRect.left-offset, cenRect.top-offset, buttonRadius, Objects.requireNonNull(buttonPaintMap.get("tl")));
        // 绘制右上角按钮
        canvas.drawCircle(cenRect.right + offset, cenRect.top - offset, buttonRadius, Objects.requireNonNull(buttonPaintMap.get("tr")));
        // 绘制左下角按钮
        canvas.drawCircle(cenRect.left - offset, cenRect.bottom + offset, buttonRadius, Objects.requireNonNull(buttonPaintMap.get("bl")));
        // 绘制右下角按钮
        canvas.drawCircle(cenRect.right + offset, cenRect.bottom + offset, buttonRadius, Objects.requireNonNull(buttonPaintMap.get("br")));
    }

    // 接下来要为上面的五个图形添加点击事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        nowClick = BUTTON_NONE;
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            mPaintCenter.setColor(Color.parseColor(colorCenterOriginal));
            mPaintOuterDown.setColor(Color.parseColor(colorOuterOriginal));
            mPaintOuterLeft.setColor(Color.parseColor(colorOuterOriginal));
            mPaintOuterUp.setColor(Color.parseColor(colorOuterOriginal));
            mPaintOuterRight.setColor(Color.parseColor(colorOuterOriginal));
            for (Paint p: buttonPaintMap.values()) {
                p.setColor(Color.parseColor(colorOuterOriginal));
            }

            invalidate();
            return true;
        }
        // 接下来都是 ACTION_DOWN 和 ACTION_UP 的情况
        float x = event.getX();
        float y = event.getY();
        float centerX = getWidth() / 2f;
        float centerY = getWidth() / 2f;
        float radius_out = getWidth() * 3 / 8f;
        float radius_in = getWidth() * 5 / 32f;
        float distance = (float) Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));

        // 点击在否在大圆内
        if (distance > radius_out) {

            // 检查点击是否在左上角按钮内
            if (isTouchInsideButton(x, y, cenRect.left - offset, cenRect.top - offset)) {
                handleButtonClick(BUTTON_TOP_LEFT, event.getAction());
                return true;
            }
            // 检查点击是否在右上角按钮内
            else if (isTouchInsideButton(x, y, cenRect.right + offset, cenRect.top - offset)) {
                handleButtonClick(BUTTON_TOP_RIGHT, event.getAction());
                return true;
            }
            // 检查点击是否在左下角按钮内
            else if (isTouchInsideButton(x, y, cenRect.left - offset, cenRect.bottom + offset)) {
                handleButtonClick(BUTTON_BOTTOM_LEFT, event.getAction());
                return true;
            }
            // 检查点击是否在右下角按钮内
            else if (isTouchInsideButton(x, y, cenRect.right + offset, cenRect.bottom + offset)) {
                handleButtonClick(BUTTON_BOTTOM_RIGHT, event.getAction());
                return true;
            }

            return true;
        }

        // 现在要判断点击的是哪个按钮
        // 确定按钮
        if (distance < radius_in) {
            // 如果是按下，改变按钮的颜色
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mPaintCenter.setColor(Color.parseColor(colorCenterPress));
                invalidate();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                mPaintCenter.setColor(Color.parseColor(colorCenterOriginal));
                invalidate();
                nowClick = BUTTON_OK;
                performClick();
            }
            return true;
        }

        // 上下左右按钮
        float angle = (float) Math.toDegrees(Math.atan2(y - centerY, x - centerX));
        if (angle < 0) {
            angle += 360;
        }
        if (angle >= 45 && angle < 135) {
            // 如果是按下，改变按钮的颜色
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mPaintOuterDown.setColor(Color.parseColor(colorOuterPress));
                invalidate();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                mPaintOuterDown.setColor(Color.parseColor(colorOuterOriginal));
                invalidate();
                nowClick = BUTTON_DOWN;
                performClick();
            }
        } else if (angle >= 135 && angle < 225) {
            // 如果是按下，改变按钮的颜色
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mPaintOuterLeft.setColor(Color.parseColor(colorOuterPress));
                invalidate();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                mPaintOuterLeft.setColor(Color.parseColor(colorOuterOriginal));
                invalidate();
                nowClick = BUTTON_LEFT;
                performClick();
            }
        } else if (angle >= 225 && angle < 315) {
            // 如果是按下，改变按钮的颜色
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mPaintOuterUp.setColor(Color.parseColor(colorOuterPress));
                invalidate();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                mPaintOuterUp.setColor(Color.parseColor(colorOuterOriginal));
                invalidate();
                nowClick = BUTTON_UP;
                performClick();
            }
        } else {
            // 如果是按下，改变按钮的颜色
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mPaintOuterRight.setColor(Color.parseColor(colorOuterPress));
                invalidate();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                mPaintOuterRight.setColor(Color.parseColor(colorOuterOriginal));
                invalidate();
                nowClick = BUTTON_RIGHT;
                performClick();
            }
        }

        return true;
    }

    private boolean isTouchInsideButton(float x, float y, float buttonX, float buttonY) {
        // 检查触摸点是否在指定按钮的范围内
        float distance = (float) Math.sqrt(Math.pow(x - buttonX, 2) + Math.pow(y - buttonY, 2));
        return distance <= buttonRadius;
    }

    @Override
    public boolean performClick() {
        super.performClick();

        if (nowClick == BUTTON_UP) {
            Log.d("click", "click up");
        } else if (nowClick == BUTTON_DOWN) {
            Log.d("click", "click down");
        } else if (nowClick == BUTTON_LEFT) {
            Log.d("click", "click left");
        } else if (nowClick == BUTTON_RIGHT) {
            Log.d("click", "click right");
        } else if (nowClick == BUTTON_OK) {
            Log.d("click", "click ok");
        } else if (nowClick == BUTTON_TOP_LEFT) {
            Log.d("click", "click top left");
        } else if (nowClick == BUTTON_TOP_RIGHT) {
            Log.d("click", "click top right");
        } else if (nowClick == BUTTON_BOTTOM_LEFT) {
            Log.d("click", "click bottom left");
        } else if (nowClick == BUTTON_BOTTOM_RIGHT) {
            Log.d("click", "click bottom right");
        }

        return true;
    }

    private void handleButtonClick(int buttonId, int action) {
        // 处理按钮点击事件
        if (action == MotionEvent.ACTION_DOWN) {
            // 按下时的操作
            // 根据按钮的ID执行相应的操作
            switch (buttonId) {
                case BUTTON_TOP_LEFT:
                    // 左上角按钮按下时的操作
                    Objects.requireNonNull(buttonPaintMap.get("tl")).setColor(Color.parseColor(colorOuterPress));
                    invalidate();
                    break;
                case BUTTON_TOP_RIGHT:
                    // 右上角按钮按下时的操作
                    Objects.requireNonNull(buttonPaintMap.get("tr")).setColor(Color.parseColor(colorOuterPress));
                    invalidate();
                    break;
                case BUTTON_BOTTOM_LEFT:
                    // 左下角按钮按下时的操作
                    Objects.requireNonNull(buttonPaintMap.get("bl")).setColor(Color.parseColor(colorOuterPress));
                    invalidate();
                    break;
                case BUTTON_BOTTOM_RIGHT:
                    // 右下角按钮按下时的操作
                    Objects.requireNonNull(buttonPaintMap.get("br")).setColor(Color.parseColor(colorOuterPress));
                    invalidate();
                    break;
            }
        } else if (action == MotionEvent.ACTION_UP) {
            switch (buttonId) {
                case BUTTON_TOP_LEFT:
                    // 左上角按钮松开时的操作
                    Objects.requireNonNull(buttonPaintMap.get("tl")).setColor(Color.parseColor(colorOuterOriginal));
                    invalidate();
                    nowClick = BUTTON_TOP_LEFT;
                    performClick();
                    break;
                case BUTTON_TOP_RIGHT:
                    // 右上角按钮松开时的操作
                    Objects.requireNonNull(buttonPaintMap.get("tr")).setColor(Color.parseColor(colorOuterOriginal));
                    invalidate();
                    nowClick = BUTTON_TOP_RIGHT;
                    performClick();
                    break;
                case BUTTON_BOTTOM_LEFT:
                    // 左下角按钮松开时的操作
                    Objects.requireNonNull(buttonPaintMap.get("bl")).setColor(Color.parseColor(colorOuterOriginal));
                    invalidate();
                    nowClick = BUTTON_BOTTOM_LEFT;
                    performClick();
                    break;
                case BUTTON_BOTTOM_RIGHT:
                    // 右下角按钮松开时的操作
                    Objects.requireNonNull(buttonPaintMap.get("br")).setColor(Color.parseColor(colorOuterOriginal));
                    invalidate();
                    nowClick = BUTTON_BOTTOM_RIGHT;
                    performClick();
                    break;
            }
        }
    }

    public void initButton(Key[] keys, ConsumerIrManager irManager, int IR_CARRIER_FREQUENCY) {
        if (irManager == null) {
            return;
        }
        top_left_btn_text = keys[17].getName();
        top_right_btn_text = keys[18].getName();
        bottom_left_btn_text = keys[19].getName();
        bottom_right_btn_text = keys[20].getName();
        top_btn_unicode = keys[21].getName();
        left_btn_unicode = keys[22].getName();
        right_btn_unicode = keys[23].getName();
        bottom_btn_unicode = keys[24].getName();
        center_btn_text = keys[25].getName();
//        TODO: 要写一个回调函数把这个地方的 OnClick 事件实现
//        for (int i = 17; i < keys.length; i++) {
//            Key key = keys[i];
//            VibrateHelp.simpleVibrate(this.getContext(), 60);
//            irManager.transmit(IR_CARRIER_FREQUENCY, signal);
//        }
    }
}

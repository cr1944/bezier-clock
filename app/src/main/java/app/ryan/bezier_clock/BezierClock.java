package app.ryan.bezier_clock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by ryancheng on 14-9-12.
 */
public class BezierClock extends View {
    private Paint paint;
    private Path path;
    private BezierDigit[] digits;
    private Calendar cal;
    private boolean mAttached;

    static final int DEFAULT_DIGIT_COLOR = Color.WHITE;
    static final int DEFAULT_STROKE_WIDTH = 4;
    static final int[] BG_COLOR = {
            0xff003D79,
            0xff003D79,
            0xff467500,
            0xff467500,
            0xffD94600,
            0xffD94600,
    };

    public BezierClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BezierClock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        digits = new BezierDigit[6];
        for (int i = 0; i < 6; i++) {
            digits[i] = new BezierDigit(0, this, BG_COLOR[i]);
        }
        float density = context.getResources().getDisplayMetrics().density;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BezierClock);
        int digitColor = a.getColor(R.styleable.BezierClock_digitColor, DEFAULT_DIGIT_COLOR);
        float strokeWidth = a.getDimension(R.styleable.BezierClock_strokeWidth, DEFAULT_STROKE_WIDTH * density);
        a.recycle();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(digitColor);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);
        path = new Path();
        createTime(null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        for (int i = 0; i < 6; i++) {
            digits[i].setSize(w / 6, h);
            digits[i].setTranslation(i * w / 6, 0);
        }
    }

    public void refresh(BezierDigit digit) {
        invalidate((int)digit.transX, (int)digit.transY, (int)(digit.transX + digit.w), (int)(digit.transY + digit.h));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < 6; i++) {
            digits[i].draw(canvas, path, paint);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!mAttached) {
            mAttached = true;
            registerReceiver();
            createTime(null);
            mTicker.run();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mAttached) {
            unregisterReceiver();
            getHandler().removeCallbacks(mTicker);
            mAttached = false;
        }
    }

    private final Runnable mTicker = new Runnable() {
        public void run() {
            onTimeChanged();

            long now = SystemClock.uptimeMillis();
            long next = now + (1000 - now % 1000);

            getHandler().postAtTime(mTicker, next);
        }
    };

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                final String timeZone = intent.getStringExtra("time-zone");
                createTime(timeZone);
            }
            onTimeChanged();
        }
    };

    private void registerReceiver() {
        final IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

        getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());
    }

    private void unregisterReceiver() {
        getContext().unregisterReceiver(mIntentReceiver);
    }

    private void createTime(String timeZone) {
        if (timeZone != null) {
            cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        } else {
            cal = Calendar.getInstance();
        }
    }

    private void onTimeChanged() {
        cal.setTimeInMillis(System.currentTimeMillis());
        int second = cal.get(Calendar.SECOND);
        int second1 = second % 10;
        int second2 = second / 10;
        digits[5].setDigit(second1);
        digits[4].setDigit(second2);

        int minute = cal.get(Calendar.MINUTE);
        int minute1 = minute % 10;
        int minute2 = minute / 10;
        digits[3].setDigit(minute1);
        digits[2].setDigit(minute2);

        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int hour1 = hour % 10;
        int hour2 = hour / 10;
        digits[1].setDigit(hour1);
        digits[0].setDigit(hour2);

    }

}

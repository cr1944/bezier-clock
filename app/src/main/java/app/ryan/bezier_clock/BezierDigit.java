package app.ryan.bezier_clock;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by ryancheng on 14-9-12.
 */
public class BezierDigit {
    static final float BASEX = 400;
    static final float BASEY = 500;
     //first points
    static float[] VERTEXX = {254, 138, 104,  96, 374, 340, 301, 108, 243, 322};
    static float[] VERTEXY = { 47, 180, 111, 132, 244,  52,  26,  52, 242, 105};
    //controls
    static float[][][] CONTROLS = {
        {
            {159,  84, 123, 158, 131, 258},
            {139, 358, 167, 445, 256, 446},
            {345, 447, 369, 349, 369, 275},
            {369, 201, 365,  81, 231,  75}
        },
        {
            {226,  99, 230,  58, 243,  43},
            {256,  28, 252, 100, 253, 167},
            {254, 234, 254, 194, 255, 303},
            {256, 412, 254, 361, 255, 424}
        },
        {
            {152,  55, 208,  26, 271,  50},
            {334,  74, 360, 159, 336, 241},
            {312, 323, 136, 454, 120, 405},
            {104, 356, 327, 393, 373, 414}
        },
        {
            {113,  14, 267,  17, 311, 107},
            {355, 197, 190, 285, 182, 250},
            {174, 215, 396, 273, 338, 388},
            {280, 503, 110, 445,  93, 391}
        },
        {
            {249, 230, 192, 234, 131, 239},
            { 70, 244, 142, 138, 192,  84},
            {242,  30, 283, -30, 260, 108},
            {237, 246, 246, 435, 247, 438}
        },
        {
            {226,  42, 153,  44, 144,  61},
            {135,  78, 145, 203, 152, 223},
            {159, 243, 351, 165, 361, 302},
            {371, 439, 262, 452, 147, 409}
        },
        {
            {191, 104, 160, 224, 149, 296},
            {138, 368, 163, 451, 242, 458},
            {321, 465, 367, 402, 348, 321},
            {329, 240, 220, 243, 168, 285}
        },
        {
            {168,  34, 245,  42, 312,  38},
            {379,  34, 305, 145, 294, 166},
            {283, 187, 243, 267, 231, 295},
            {219, 323, 200, 388, 198, 452}
        },
        {
            {336, 184, 353,  52, 240,  43},
            {127,  34, 143, 215, 225, 247},
            {307, 279, 403, 427, 248, 432},
            { 93, 437, 124, 304, 217, 255}
        },
        {
            {323,   6, 171,  33, 151,  85},
            {131, 137, 161, 184, 219, 190},
            {277, 196, 346, 149, 322, 122},
            {298,  95, 297, 365, 297, 448}
        },
    };
    static final int DEFAULT_BG_COLOR = Color.BLACK;

    private static final long ANIM_DURATION = 500;
    int digit, newDigit;
    private float vertexX, vertexY;
    private float[][] controls = new float[4][6];
    private float factor;
    private ObjectAnimator anim;
    private boolean animating;
    private BezierClock bezierClock;
    float w, h;
    float transX, transY;
    private int backgroundColor = DEFAULT_BG_COLOR;

    private AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(android.animation.Animator animation) {
            factor = 0;
            digit = newDigit;
            animating = false;
        }
        @Override
        public void onAnimationCancel(android.animation.Animator animation) {
            factor = 0;
            digit = newDigit;
            animating = false;
        }
    };

    BezierDigit(int digit, BezierClock bezierClock, int bgColor) {
        if (digit < 0 || digit > 9) {
            throw new IllegalArgumentException("invalid digit: " + digit);
        }
        this.digit = digit;
        this.bezierClock = bezierClock;
        this.backgroundColor = bgColor;
        vertexX = VERTEXX[digit];
        vertexY = VERTEXY[digit];
        //System.arraycopy(CONTROLS[digit], 0, controls, 0, CONTROLS[digit].length);
        for (int i = 0; i < 4; i++) {
            System.arraycopy(CONTROLS[digit][i], 0, controls[i], 0, CONTROLS[digit][i].length);
        }
    }

    public void setDigit(int digit) {
        if (digit < 0 || digit > 9) {
            throw new IllegalArgumentException("invalid digit: " + digit);
        }
        if (digit == this.digit) {
            return;
        }
        if (digit == this.newDigit) {
            return;
        }
        if (anim != null && anim.isRunning()) {
            anim.cancel();
        }
        newDigit = digit;
        anim = ObjectAnimator.ofFloat(this, "factor", 0f, 1f);
        anim.setDuration(ANIM_DURATION);
        anim.addListener(listener);
        animating = true;
        anim.start();
    }

    public void setFactor(float f) {
        factor = f;
        vertexX = factor * (VERTEXX[newDigit] - VERTEXX[digit]) + VERTEXX[digit];
        vertexY = factor * (VERTEXY[newDigit] - VERTEXY[digit]) + VERTEXY[digit];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 6; j++) {
                controls[i][j] = factor * (CONTROLS[newDigit][i][j] - CONTROLS[digit][i][j]) + CONTROLS[digit][i][j];
            }
        }
        bezierClock.refresh(this);
    }

    public void setSize(int w, int h) {
        this.w = w;
        this.h = h;
    }

    public void setTranslation(int transX, int transY) {
        this.transX = transX;
        this.transY = transY;
    }

    public void draw(Canvas canvas, Path path, Paint paint) {
        path.reset();
        path.moveTo(vertexX, vertexY);
        path.cubicTo(controls[0][0], controls[0][1],
                controls[0][2], controls[0][3],
                controls[0][4], controls[0][5]);
        path.cubicTo(controls[1][0], controls[1][1],
                controls[1][2], controls[1][3],
                controls[1][4], controls[1][5]);
        path.cubicTo(controls[2][0], controls[2][1],
                controls[2][2], controls[2][3],
                controls[2][4], controls[2][5]);
        path.cubicTo(controls[3][0], controls[3][1],
                controls[3][2], controls[3][3],
                controls[3][4], controls[3][5]);
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.translate(transX, transY);
        canvas.scale(w / BASEX, h / BASEY, 0, 0);
        int color = paint.getColor();
        paint.setColor(backgroundColor);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawRect(0, 0, BASEX, BASEY, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        canvas.drawPath(path, paint);
        canvas.restore();
    }
}

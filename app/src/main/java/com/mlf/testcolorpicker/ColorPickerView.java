package com.mlf.testcolorpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;

public class ColorPickerView extends AppCompatTextView
{
    public static final String LOG_TAG = "AppLog";

    private static final int MIN_W = 100;

    // Círculo de selección
    private final Paint paintSelStroke;
    private final Paint paintSelFill;
    private final Paint paintBar;
    private final Paint paintStroke;
    private final Paint paintFill;

    private final float[] hsv = new float[3];

    private float[][] satMap;
    private float[][] hueMap;

    private Bitmap bmpBar, bmpPie;
    private final float[] selHSV = new float[3];
    private int pieSelX, pieSelY, barSelX, barSelY, selR;

    private Rect rcPie, rcBar;
    private Rect rcPie0, rcBar0;
    private int sampleX, sampleY, sampleR;

    public ColorPickerView(Context context)
    {
        this(context, null);
    }

    public ColorPickerView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        paintSelStroke = new Paint();
        paintSelStroke.setStyle(Paint.Style.STROKE);
        paintSelStroke.setColor(Color.BLACK);

        paintSelFill = new Paint();
        paintSelFill.setStyle(Paint.Style.FILL);
        paintSelFill.setColor(Color.WHITE);

        paintBar = new Paint();
        paintBar.setStyle(Paint.Style.STROKE);
        paintBar.setColor(Color.WHITE);

        paintStroke = new Paint();
        paintStroke.setStyle(Paint.Style.STROKE);
        paintStroke.setStrokeWidth(1);
        paintFill = new Paint();
        paintFill.setStyle(Paint.Style.FILL);

        setOnTouchListener(new OnTouchListener()
        {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                int x = (int) event.getX();
                int y = (int) event.getY();
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        selectColor(x, y);
                        selectValue(x, y);
                        invalidate();
                        break;
                }
                return true;
            }
        });

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if(width < MIN_W)
        {
            width = MIN_W;
        }
        int height = calcAreas(width);
        buildPie();
        selectColor(rcPie.centerX(), rcPie.centerY());
        selectValue(rcPie.centerX(), rcBar.centerY());
        setMeasuredDimension(width, height);
    }

    private int calcAreas(int width)
    {
        Log.e(LOG_TAG, "calcAreas. width: " + width);

        int padding = Math.round(width*0.025f);
        int pieR = Math.round(width/2f - padding);
        int pieD = 2*pieR;
        int barH = Math.round(pieR/4f);

        selR = Math.round(pieR/10f);
        sampleR = Math.round(pieR/7f);

        rcPie = new Rect(padding, padding, padding + pieD, padding + pieD);
        rcPie0 = new Rect(0, 0, pieD, pieD);
        rcBar = new Rect(rcPie.left, rcPie.bottom + selR, rcPie.right, rcPie.bottom + selR + barH);
        rcBar0 = new Rect(0, 0, pieD, barH);

        sampleX = rcPie.right - sampleR;
        sampleY = rcPie.bottom - sampleR;

        pieSelX = (int) rcPie.exactCenterX();
        pieSelY = (int) rcPie.exactCenterY();
        barSelX = pieSelX;
        barSelY = (int) rcBar.exactCenterY();

        return (rcBar.bottom + padding);
    }

    private void selectValue(int x, int y)
    {
        if(!rcBar.contains(x, y))
        {
            return;
        }
        float relX = x - rcBar.left;
        selHSV[2] = relX/rcBar.width();
        barSelX = x;
    }

    private void selectColor(int x, int y)
    {
        if(!rcPie.contains(x, y))
        {
            return;
        }
        int relX = x - rcPie.left;
        int relY = y - rcPie.top;
        if(hueMap[relX][relY] < 0 || satMap[relX][relY] < 0)
        {
            return;
        }
        pieSelX = x;
        pieSelY = y;
        selHSV[0] = hueMap[relX][relY];
        selHSV[1] = satMap[relX][relY];
        buildBar();
    }

    private void buildBar()
    {
        bmpBar = Bitmap.createBitmap(rcBar.width(), rcBar.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmpBar);
        float[] hsv = { selHSV[0], selHSV[1], 0};
        int w = rcBar.width();
        int h = rcBar.height();
        for(float x = 0; x < w; ++x)
        {
            hsv[2] = x/w;
            paintBar.setColor(Color.HSVToColor(hsv));
            canvas.drawLine(x, 0, x, h, paintBar);
        }
        paintBar.setColor(Color.WHITE);
        canvas.drawRect(0, 0, w, h, paintBar);
    }

    private void buildPie()
    {
        int pieR = Math.round(rcPie.width()/2f);
        int pieD = 2*pieR;
        satMap = new float[pieD][pieD];
        hueMap = new float[pieD][pieD];
        bmpPie = Bitmap.createBitmap(pieD, pieD, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bmpPie);
        double dist, angle;
        float dx, dy;

        hsv[2] = 1;
        for(int x = 0; x < pieD; ++x)
        {
            for(int y = 0; y < pieD; ++y)
            {
                dx = (x - pieR);
                dy = (y - pieR);
                dist = Math.sqrt(dx*dx + dy*dy);
                if(dist <= pieR)
                {
                    angle = Math.atan2(dy, dx)*180/Math.PI;
                    if(angle < 0)
                    {
                        angle += 360;
                    }
                    hsv[0] = (float) angle;
                    hsv[1] = (float) (dist/pieR);
                    hueMap[x][y] = hsv[0];
                    satMap[x][y] = hsv[1];
                    paintStroke.setColor(Color.HSVToColor(hsv));
                    canvas.drawPoint(x, y, paintStroke);
                }
                else
                {
                    hueMap[x][y] = -1;
                    satMap[x][y] = -1;
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        canvas.drawBitmap(bmpPie, rcPie0, rcPie, paintStroke);
        canvas.drawCircle(pieSelX, pieSelY, selR, paintSelFill);
        canvas.drawCircle(pieSelX, pieSelY, selR, paintSelStroke);

        canvas.drawBitmap(bmpBar, rcBar0, rcBar, paintStroke);
        canvas.drawCircle(barSelX, barSelY, selR, paintSelFill);
        canvas.drawCircle(barSelX, barSelY, selR, paintSelStroke);

        paintFill.setColor(getSelectedColor());
        paintStroke.setColor(Color.WHITE);
        canvas.drawCircle(sampleX, sampleY, sampleR, paintFill);
        canvas.drawCircle(sampleX, sampleY, sampleR, paintStroke);
    }

    public int getSelectedColor()
    {
        return Color.HSVToColor(selHSV);
    }
}

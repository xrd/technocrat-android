package com.inqry.technocratsignal;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;

/**
 * Created by cdawson on 3/2/16.
 */
public class CustomView extends View {

    private static final String TECHNOCRAT_SIGNAL_LOG = "TCSIGNAL";
    private Paint paint;
    private int x,y;
    int sideLength;
    Bitmap bmp;
    private boolean isOn;
    String z = "z";
    private int deltaY = 10;
    private int deltaX = 10;
    private int[] zOffsetX;
    private int[] zOffsetY;
    private int zAlpha = 50;
    private int zY;
    private int bunchColumnX;

    public CustomView( Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        x = 50;
        y = 50;
        zOffsetX = new int[3];
        zOffsetY = new int[3];
        zY = getDefaultZY();

        sideLength = 200;
        // create the Paint and set its color
        paint = new Paint();
        paint.setColor(Color.GRAY);
    }

    Paint zPaint;

    private void setupZPaint() {
        float fontSize = new EditText(this.getContext()).getTextSize();
        fontSize+=fontSize*0.2f;
        zPaint = new Paint();
        zPaint.setColor(Color.WHITE);
        zPaint.setAlpha(zAlpha);
        zPaint.setTextSize((int) fontSize);
        Rect textBounds = new Rect();
        zPaint.getTextBounds(z, 0, z.length(), textBounds);

        bunchColumnX = getWidth() / 3;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if( null == zPaint ) {
            setupZPaint();
        }
//        int colorPrimary = getColor(R.attr.colorPrimary);
        int colorPrimary = getColor(R.attr.colorPrimaryDark);
        canvas.drawColor(colorPrimary);
        if( isOn ) {
            drawSpotlight(canvas);
            if (null != bmp) {
                canvas.drawBitmap(bmp, x, y, paint);
            }
        }
        else {
            if( zAlpha > 255 ) {
                zAlpha = 0;
                // reset the offsets
                resetZOffsets();
                zY = getDefaultZY();
            }
            else {
                zAlpha += 10;
                zY -= 10;
            }

            drawZzzs( canvas );
        }
    }

    private int getDefaultZY() {
        return getHeight() - 200;
    }

    private void resetZOffsets() {
        zOffsetX = new int[]{ getRandomNumber(), getRandomNumber(), getRandomNumber() };
        zOffsetY = new int[]{ getRandomNumber(), getRandomNumber(), getRandomNumber() };
    }

    private int getRandomNumber() {
        return (int)((Math.random() * 15 ) * ( Math.random() > 0.5 ? -1 : 1 ) );
    }

    // Draw a set of three z's, three times.
    private void drawZzzs( Canvas canvas ) {
        int canvasHeight = canvas.getHeight();
        Log.d(TECHNOCRAT_SIGNAL_LOG, "Height: " + canvasHeight);
        int alpha = 255 - zAlpha;
        if( alpha < 0 ) {
            alpha = 0;
        }
        zPaint.setAlpha( alpha );
        for( int i = 0; i < 3; i++ ) {
            drawZBunch(canvas, i, canvasHeight );
        }
    }

    // Draw a set of three z's...
    private void drawZBunch(Canvas canvas, int bunchIndex, int canvasHeight ) {
        
        // draw each one slightly offset
        for( int j = 0; j < 3; j++ ) {
            int theX = ( bunchColumnX / 2 ) - 30 +
                    (bunchColumnX * bunchIndex) +
                    ( ( zAlpha / 20 ) * zOffsetX[bunchIndex] ) +
                    ( 30 * j );
            // go up for the second one, make them staggered a little bit.
            int theY =
                    (canvasHeight - 200) -
                            zAlpha +
                            ( j%2==0 ? 20: 0 );

            Log.d(TECHNOCRAT_SIGNAL_LOG, "X/Y: " + theX + ":" + theY);
            canvas.drawText(z, theX, theY, zPaint);
        }
    }

    private int getColor(int colorAttr) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = this.getContext().obtainStyledAttributes(typedValue.data, new int[]{colorAttr});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    private void drawSpotlight( Canvas canvas ) {
        int center = getWidth() / 2;
        int bottom = getHeight();
        Paint spotlightPaint = new Paint();
        if( null != canvas && null != bmp ) {
            int scaledWidth = bmp.getScaledWidth(canvas);
            int leftX = x, rightX = x + scaledWidth;
            int middleY = y + (bmp.getScaledHeight(canvas) / 2);

            //examples accessing colors
            int colorAccent = getColor(R.attr.colorControlHighlight);

            spotlightPaint.setColor(colorAccent);
            Path path = new Path();
            path.moveTo(leftX, middleY);
            path.lineTo(center, bottom);
            path.lineTo(rightX, middleY);
            path.close();
            canvas.drawPath(path, spotlightPaint);
        }
    }

    public void setTechnocratSignalState( boolean isOn, String message ) {
        this.isOn = isOn;
    }

    public void setBmp(Bitmap bmp) {
        this.bmp = bmp;
    }

    boolean goingUp = false;
    boolean goingLeft = false;
    int ceiling = 200;
    int floor = 100;

    public void update() {
        modifyX();
        modifyY();
        invalidate();
    }

    private void modifyY() {
        if( goingUp ) {
            if( y < ceiling ) {
                y += deltaY;
            }
            else {
                goingUp = false;
            }
        }
        else {
            if( y > floor ) {
                y -= deltaY;
            }
            else {
                goingUp = true;
            }
        }
    }

    private void modifyX() {
        if( goingLeft ) {
            if( x < getWidth() ) {
                x += deltaX;
            }
            else {
                goingLeft = false;
            }
        }
        else {
            if( x > 0 ) {
                x -= deltaX;
            }
            else {
                goingLeft = true;
            }
        }
    }

    public void setDeltaX(int x) {
        this.deltaX = x;
    }

    public void setDeltaY(int y) {
        this.deltaY = y;
    }
}
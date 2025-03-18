package org.demoforge.tableroroll;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;

public class ZoomableViewGroup extends ViewGroup {
    private float scaleFactor = 1.0f;
    private ScaleGestureDetector scaleDetector;
    private float posX = 0, posY = 0;
    private float lastTouchX, lastTouchY;

    public ZoomableViewGroup(Context context) {
        super(context);
        init(context);
    }

    public ZoomableViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(posX, posY);
        canvas.scale(scaleFactor, scaleFactor);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);

        if (!scaleDetector.isInProgress()) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    lastTouchX = event.getX();
                    lastTouchY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getX() - lastTouchX;
                    float dy = event.getY() - lastTouchY;
                    posX += dx;
                    posY += dy;
                    lastTouchX = event.getX();
                    lastTouchY = event.getY();
                    invalidate();
                    break;
            }
        }
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 5.0f)); // Zoom mínimo y máximo
            invalidate();
            return true;
        }
    }
}

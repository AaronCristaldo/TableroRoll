package org.demoforge.tableroroll;

import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;

public class PanZoomView extends FrameLayout {

    private float scale = 1f;
    private static final float MIN_ZOOM = 0.6f;  // Mínimo zoom
    private static final float MAX_ZOOM = 5f;   // Máximo zoom
    private ScaleGestureDetector scaleDetector;
    private float lastTouchX, lastTouchY;
    private boolean isScrolling = false;
    private PointF viewportOffset = new PointF(0, 0);
    private Handler handler = new Handler();
    private float velocityX = 0, velocityY = 0;

    public PanZoomView(Context context) {
        super(context);
        init(context);
    }

    public PanZoomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                // Ajuste del factor de escala al hacer zoom
                float newScale = scale * detector.getScaleFactor();

                // Limitar el zoom para que no se haga demasiado grande o pequeño
                if (newScale < MIN_ZOOM) {
                    newScale = MIN_ZOOM;
                } else if (newScale > MAX_ZOOM) {
                    newScale = MAX_ZOOM;
                }

                setScaleX(newScale);
                setScaleY(newScale);
                scale = newScale;

                adjustPosition();  // Reajustar la posición después de un zoom
                return true;
            }
        });

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scaleDetector.onTouchEvent(event);

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        lastTouchX = event.getX();
                        lastTouchY = event.getY();
                        isScrolling = false;
                        velocityX = 0;
                        velocityY = 0;
                        handler.removeCallbacks(inertiaRunnable); // Detener la inercia
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getX() - lastTouchX;
                        float dy = event.getY() - lastTouchY;

                        if (!isScrolling) {
                            isScrolling = true;
                        }

                        // Controlar la velocidad y evitar oscilaciones
                        velocityX = dx * 0.5f;  // Reducir la velocidad
                        velocityY = dy * 0.5f;  // Reducir la velocidad

                        // Desplazar en ambas direcciones, horizontal y vertical
                        viewportOffset.x += velocityX;
                        viewportOffset.y += velocityY;

                        adjustPosition();  // Reajustar la posición al mover la vista

                        setTranslationX(viewportOffset.x);
                        setTranslationY(viewportOffset.y);

                        lastTouchX = event.getX();
                        lastTouchY = event.getY();
                        break;

                    case MotionEvent.ACTION_UP:
                        isScrolling = false;
                        startInertia(); // Iniciar la inercia al soltar
                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:
                        // Detectar el doble toque
                        if (event.getPointerCount() == 2) {
                            performDoubleTap();
                        }
                        break;
                }

                return true;
            }
        });
    }

    private void adjustPosition() {
        // Ajustar la posición de la cuadrícula con la escala aplicada.
        float contentWidth = getWidth() * scale;
        float contentHeight = getHeight() * scale;

        // Calcular el desplazamiento para permitir el movimiento libre, sin restricciones
        float offsetX = (getWidth() - contentWidth) / 2;
        float offsetY = (getHeight() - contentHeight) / 2;

        // Aquí no limitamos el desplazamiento, puedes mover la cuadrícula libremente
        // (sin limitación en los desplazamientos)

        // Ajustar la posición en función del desplazamiento
        setTranslationX(viewportOffset.x + offsetX);
        setTranslationY(viewportOffset.y + offsetY);
    }

    // Función para suavizar el movimiento cuando se suelta el dedo
    private void startInertia() {
        handler.postDelayed(inertiaRunnable, 16);
    }

    private Runnable inertiaRunnable = new Runnable() {
        @Override
        public void run() {
            if (Math.abs(velocityX) > 0.1f || Math.abs(velocityY) > 0.1f) {
                viewportOffset.x += velocityX;
                viewportOffset.y += velocityY;

                velocityX *= 0.6f; // Reducir la velocidad gradualmente
                velocityY *= 0.6f;

                adjustPosition();  // Reajustar la posición

                setTranslationX(viewportOffset.x);
                setTranslationY(viewportOffset.y);

                handler.postDelayed(this, 16);
            }
        }
    };

    private void performDoubleTap() {
        // Si ya está en el máximo zoom, reducir el zoom
        if (scale == MAX_ZOOM) {
            scale = MIN_ZOOM;
        } else {
            // Aumentar el zoom en 0.5x en cada doble toque
            scale += 0.5f;
            if (scale > MAX_ZOOM) {
                scale = MAX_ZOOM;
            }
        }

        // Establecer el nuevo zoom
        setScaleX(scale);
        setScaleY(scale);
        adjustPosition();
    }
}

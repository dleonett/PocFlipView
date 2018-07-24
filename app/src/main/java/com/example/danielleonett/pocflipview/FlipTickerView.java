/* (c) Disney. All rights reserved. */
package com.example.danielleonett.pocflipview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

public class FlipTickerView extends FrameLayout {

    public static final String TAG = FlipTickerView.class.getSimpleName();

    private static final int MAX_SHADOW_ALPHA = 180; // out of 255
    private static final int MAX_SHADE_ALPHA = 130; // out of 255
    private static final int MAX_SHINE_ALPHA = 100; // out of 255
    private static final int DEGREES_START = 180;
    private static final int DEGREES_END = 0;
    private static final long DEFAULT_FLIP_DURATION_MILLIS = 300;

    // Clipping rects to get to top and bottom view pieces
    private RectF topRect = new RectF();
    private RectF bottomRect = new RectF();

    // Used for transforming the canvas of the flipping piece
    private Camera camera = new Camera();
    private Matrix matrix = new Matrix();

    // Paint drawn above views when flipping
    private Paint shadowPaint = new Paint();
    private Paint shadePaint = new Paint();
    private Paint shinePaint = new Paint();

    private float shadowCornerRadius = 0;

    private View previousView;
    private View currentView;

    private int degreesFlipped = DEGREES_START;

    private ValueAnimator animator;

    private FlipListener flipListener;

    public FlipTickerView(Context context) {
        this(context, null);
    }

    public FlipTickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlipTickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.FlipTickerView);

        shadowCornerRadius = a.getDimension(R.styleable.FlipTickerView_shadowCornerRadius, 0f);

        a.recycle();

        init();
    }

    private void init() {
        initPaints();
        initFlipAnimator();
        initEmptyView();
    }

    private void initPaints() {
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadePaint.setColor(Color.BLACK);
        shadePaint.setStyle(Paint.Style.FILL);
        shinePaint.setColor(Color.WHITE);
        shinePaint.setStyle(Paint.Style.FILL);
    }

    private void initFlipAnimator() {
        animator = ValueAnimator.ofInt(DEGREES_START, DEGREES_END);
        animator.setDuration(DEFAULT_FLIP_DURATION_MILLIS);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                degreesFlipped = Math.abs((Integer) animation.getAnimatedValue());
                invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (flipListener != null) {
                    flipListener.onFlipStarted();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (flipListener != null) {
                    flipListener.onFlipEnded();
                }
            }
        });
    }

    private void initEmptyView() {
        previousView = inflate(getContext(), R.layout.flip_view_empty_view, null);
        previousView.setTag(String.valueOf(-1));
        addView(previousView);
    }

    private void addNextView(View view) {
        addView(view);
        currentView = view;
    }

    private void resetViews() {
        if (currentView != null) {
            removeView(previousView);
            previousView = currentView;
            currentView = null;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void measureChildren(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);

        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width,
                MeasureSpec.EXACTLY);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
                MeasureSpec.EXACTLY);
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            measureChild(child, childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec,
                                int parentHeightMeasureSpec) {
        child.measure(parentWidthMeasureSpec, parentHeightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutChildren();

        topRect.top = 0;
        topRect.left = 0;
        topRect.right = getWidth();
        topRect.bottom = getHeight() / 2;

        bottomRect.top = getHeight() / 2;
        bottomRect.left = 0;
        bottomRect.right = getWidth();
        bottomRect.bottom = getHeight();
    }

    private void layoutChildren() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            layoutChild(child);
        }
    }

    private void layoutChild(View child) {
        child.layout(0, 0, getWidth(), getHeight());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        drawPreviousHalf(canvas);
        drawNextHalf(canvas);
        drawFlippingHalf(canvas);
    }

    private void drawPreviousHalf(Canvas canvas) {
        canvas.save();
        canvas.clipRect(topRect);

        final View view = currentView == null ? previousView : currentView;

        if (view != null) {
            setDrawWithLayer(view, true);
            drawChild(canvas, view, 0);
        }

        drawPreviousShadow(canvas);
        canvas.restore();
    }

    private void drawPreviousShadow(Canvas canvas) {
        if (degreesFlipped > 90) {
            final int alpha = (int) (((degreesFlipped - 90) / 90f) * MAX_SHADOW_ALPHA);
            shadowPaint.setAlpha(alpha);
            canvas.drawRoundRect(topRect, shadowCornerRadius, shadowCornerRadius, shadowPaint);
        }
    }

    private void drawNextHalf(Canvas canvas) {
        canvas.save();
        canvas.clipRect(bottomRect);

        final View view = previousView;

        if (view != null) {
            setDrawWithLayer(view, true);
            drawChild(canvas, view, 0);
        }

        drawNextShadow(canvas);
        canvas.restore();
    }

    private void drawNextShadow(Canvas canvas) {
        if (degreesFlipped < 90) {
            final int alpha = (int) ((Math.abs(degreesFlipped - 90) / 90f) * MAX_SHADOW_ALPHA);
            shadowPaint.setAlpha(alpha);
            canvas.drawRoundRect(bottomRect, shadowCornerRadius, shadowCornerRadius, shadowPaint);
        }
    }

    private void drawFlippingHalf(Canvas canvas) {
        canvas.save();
        camera.save();

        View view;
        if (degreesFlipped > 90) {
            view = previousView;
            canvas.clipRect(topRect);
            camera.rotateX(degreesFlipped - 180);
        } else {
            view = currentView;
            canvas.clipRect(bottomRect);
            camera.rotateX(degreesFlipped);
        }

        camera.getMatrix(matrix);

        positionMatrix();
        canvas.concat(matrix);

        setDrawWithLayer(view, true);
        drawChild(canvas, view, 0);

        drawFlippingShadeShine(canvas);

        camera.restore();
        canvas.restore();
    }

    private void drawFlippingShadeShine(Canvas canvas) {
        if (degreesFlipped < 90) {
            final int alpha = (int) ((degreesFlipped / 90f) * MAX_SHINE_ALPHA);
            shinePaint.setAlpha(alpha);
            canvas.drawRoundRect(bottomRect, shadowCornerRadius, shadowCornerRadius, shinePaint);
        } else {
            final int alpha = (int) ((Math.abs(degreesFlipped - 180) / 90f) * MAX_SHADE_ALPHA);
            shadePaint.setAlpha(alpha);
            canvas.drawRoundRect(topRect, shadowCornerRadius, shadowCornerRadius, shadePaint);
        }
    }

    private void positionMatrix() {
        matrix.preScale(0.25f, 0.25f);
        matrix.postScale(4.0f, 4.0f);
        matrix.preTranslate(-getWidth() / 2, -getHeight() / 2);
        matrix.postTranslate(getWidth() / 2, getHeight() / 2);
    }

    private void setDrawWithLayer(View v, boolean drawWithLayer) {
        if (v.getLayerType() != LAYER_TYPE_HARDWARE && drawWithLayer) {
            v.setLayerType(LAYER_TYPE_HARDWARE, null);
        } else if (v.getLayerType() != LAYER_TYPE_NONE && !drawWithLayer) {
            v.setLayerType(LAYER_TYPE_NONE, null);
        }
    }

    private boolean isViewCurrentlyDisplayed(View view) {
        return currentView != null && currentView.getTag() != null
                && currentView.getTag().equals(view.getTag());
    }

    // region API

    public void smoothFlipToView(View view) {
        smoothFlipToView(view, false);
    }

    public void smoothFlipToView(View view, boolean skipTagValidation) {
        if (isViewCurrentlyDisplayed(view) && !skipTagValidation) {
            return;
        }

        resetViews();
        addNextView(view);

        animator.start();
    }

    public void flipToView(View view) {
        if (isViewCurrentlyDisplayed(view)) {
            return;
        }

        resetViews();
        addNextView(view);

        degreesFlipped = 0;
        invalidate();
    }

    public void setFlipDuration(long durationMillis) {
        animator.setDuration(durationMillis);
    }

    public void setFlipListener(FlipListener listener) {
        flipListener = listener;
    }

    public void setFlipInterpolator(Interpolator interpolator) {
        animator.setInterpolator(interpolator);
    }

    // endregion

    public interface FlipListener {
        void onFlipStarted();

        void onFlipEnded();
    }

}

package com.example.danielleonett.pocflipview;

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
import android.widget.FrameLayout;

public class FlipView extends FrameLayout {

    public static final String TAG = FlipView.class.getSimpleName();

    private static final int MAX_SHADOW_ALPHA = 180; // out of 255
    private static final int MAX_SHADE_ALPHA = 130; // out of 255
    private static final int MAX_SHINE_ALPHA = 100; // out of 255
    private static final int DEGREES_START = 180;
    private static final int DEGREES_END = 0;
    private static final long DEFAULT_FLIP_DURATION_MILLIS = 300;

    // Clipping rects to get to top and bottom view pieces
    private RectF mTopRect = new RectF();
    private RectF mBottomRect = new RectF();

    // Used for transforming the canvas of the flipping piece
    private Camera mCamera = new Camera();
    private Matrix mMatrix = new Matrix();

    // Paint drawn above views when flipping
    private Paint mShadowPaint = new Paint();
    private Paint mShadePaint = new Paint();
    private Paint mShinePaint = new Paint();

    private float shadowCornerRadius = 0;

    private View currentView;
    private View nextView;

    private int degreesFlipped = DEGREES_START;

    private long flipDurationMillis = DEFAULT_FLIP_DURATION_MILLIS;

    private ValueAnimator animator;

    public FlipView(Context context) {
        this(context, null);
    }

    public FlipView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.FlipView);

        shadowCornerRadius = a.getDimension(R.styleable.FlipView_shadowCornerRadius, 0f);

        a.recycle();

        init();
    }

    private void init() {
        initPaints();
        initFlipAnimator();
        initEmptyView();
    }

    private void initPaints() {
        mShadowPaint.setColor(Color.BLACK);
        mShadowPaint.setStyle(Paint.Style.FILL);
        mShadePaint.setColor(Color.BLACK);
        mShadePaint.setStyle(Paint.Style.FILL);
        mShinePaint.setColor(Color.WHITE);
        mShinePaint.setStyle(Paint.Style.FILL);
    }

    private void initFlipAnimator() {
        animator = ValueAnimator.ofInt(DEGREES_START, DEGREES_END);
        animator.setDuration(flipDurationMillis);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                degreesFlipped = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    private void initEmptyView() {
        currentView = inflate(getContext(), R.layout.flip_view_empty_view, null);
        addView(currentView);
    }

    private void resetViews() {
        if (nextView != null) {
            removeView(currentView);
            currentView = nextView;
            nextView = null;
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

        mTopRect.top = 0;
        mTopRect.left = 0;
        mTopRect.right = getWidth();
        mTopRect.bottom = getHeight() / 2;

        mBottomRect.top = getHeight() / 2;
        mBottomRect.left = 0;
        mBottomRect.right = getWidth();
        mBottomRect.bottom = getHeight();
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
        canvas.clipRect(mTopRect);

        final View view = nextView == null ? currentView : nextView;

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
            mShadowPaint.setAlpha(alpha);
            canvas.drawRoundRect(mTopRect, shadowCornerRadius, shadowCornerRadius, mShadowPaint);
        }
    }

    private void drawNextHalf(Canvas canvas) {
        canvas.save();
        canvas.clipRect(mBottomRect);

        final View view = currentView;

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
            mShadowPaint.setAlpha(alpha);
            canvas.drawRoundRect(mBottomRect, shadowCornerRadius, shadowCornerRadius, mShadowPaint);
        }
    }

    private void drawFlippingHalf(Canvas canvas) {
        canvas.save();
        mCamera.save();

        View view;
        if (degreesFlipped > 90) {
            view = currentView;
            canvas.clipRect(mTopRect);
            mCamera.rotateX(degreesFlipped - 180);
        } else {
            view = nextView;
            canvas.clipRect(mBottomRect);
            mCamera.rotateX(degreesFlipped);
        }

        mCamera.getMatrix(mMatrix);

        positionMatrix();
        canvas.concat(mMatrix);

        setDrawWithLayer(view, true);
        drawChild(canvas, view, 0);

        drawFlippingShadeShine(canvas);

        mCamera.restore();
        canvas.restore();
    }

    private void drawFlippingShadeShine(Canvas canvas) {
        if (degreesFlipped < 90) {
            final int alpha = (int) ((degreesFlipped / 90f) * MAX_SHINE_ALPHA);
            mShinePaint.setAlpha(alpha);
            canvas.drawRoundRect(mBottomRect, shadowCornerRadius, shadowCornerRadius, mShinePaint);
        } else {
            final int alpha = (int) ((Math.abs(degreesFlipped - 180) / 90f) * MAX_SHADE_ALPHA);
            mShadePaint.setAlpha(alpha);
            canvas.drawRoundRect(mTopRect, shadowCornerRadius, shadowCornerRadius, mShadePaint);
        }
    }

    private void positionMatrix() {
        mMatrix.preScale(0.25f, 0.25f);
        mMatrix.postScale(4.0f, 4.0f);
        mMatrix.preTranslate(-getWidth() / 2, -getHeight() / 2);
        mMatrix.postTranslate(getWidth() / 2, getHeight() / 2);
    }

    private void setDrawWithLayer(View v, boolean drawWithLayer) {
        if (v.getLayerType() != LAYER_TYPE_HARDWARE && drawWithLayer) {
            v.setLayerType(LAYER_TYPE_HARDWARE, null);
        } else if (v.getLayerType() != LAYER_TYPE_NONE && !drawWithLayer) {
            v.setLayerType(LAYER_TYPE_NONE, null);
        }
    }

    // region API

    public void flipToView(View view) {
        resetViews();
        addNextView(view);

        animator.start();
    }

    public void flipToViewNoAnimation(View view) {
        resetViews();
        addNextView(view);

        degreesFlipped = 0;
        invalidate();
    }

    public void setFlipDuration(long durationMillis) {
        flipDurationMillis = durationMillis;
        animator.setDuration(flipDurationMillis);
    }

    // endregion

    private void addNextView(View view) {
        addView(view);
        nextView = view;
    }

}

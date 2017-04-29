package com.example.alexmelnichuk.indicator.indicator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.example.alexmelnichuk.indicator.R;

/**
 * Created by alexmelnichuk on 29.04.17.
 */

public class SnakeIndicatorView extends View {
    private static final int
            DEFAULT_SELECTED_INDICATOR_COLOR = 0xFFEEEEEE, //200 grey
            DEFAULT_INDICATOR_COLOR = 0xFFBDBDBD, //400 grey
            DEFAULT_INDICATOR_RADIUS_DP = 4,
            DEFAULT_SPACING_DP = 4,
            DEFAULT_ANIMATION_DURATION_MILLIS = 200,
            POS_UNSET = Integer.MIN_VALUE;

    @NonNull
    private Paint selectedIndicatorPaint, indicatorPaint, linePaint;
    @Nullable
    private IndicatorState indicatorState;

    private int itemSpacing;
    private int radius;
    private int diameter;
    private int diameterWithSpacing;
    private int headDuration, tailDuration;

    private float selectedPosition = 0.0f;
    private int currPosition;
    private int nextPosition = POS_UNSET;

    private boolean animating;
    private AnimatorSet set;
    public SnakeIndicatorView(Context context) {
        super(context);
        init(context, null);
    }

    public SnakeIndicatorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SnakeIndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SnakeIndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int cy = canvas.getHeight() / 2;
        int w = getWidth();
        int n = getNumItems();
        int numSpacingItems = Math.max(0, n - 1);
        int indicatorWidth = n * diameter + numSpacingItems * itemSpacing;
        int left = (w - indicatorWidth) / 2 + getPaddingLeft() - getPaddingRight() + radius;

        for (int i = 0; i < n; ++i) {
            int cx = left + i * diameterWithSpacing;
            canvas.drawCircle(cx, cy, radius, indicatorPaint);
        }

        float startX = left + currPosition * diameterWithSpacing;
        float endX = left + selectedPosition * diameterWithSpacing;
        if (startX == endX) {
            canvas.drawCircle(startX, cy, radius, selectedIndicatorPaint);
        } else {
            canvas.drawLine(startX, cy, endX, cy, linePaint);
        }
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = (float) selectedPosition;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setIndicatorState(@Nullable IndicatorState indicatorState) {
        this.indicatorState = indicatorState;
    }

    public boolean isAnimating() {
        return animating;
    }

    public void animate(int position) {
        if (animating)
            nextPosition = position;
        else if (position != currPosition)
            doAnim(position);
    }

    private void doAnim(final int initialPos) {
        final int position = Math.max(0, Math.min(initialPos, getNumItems() - 1));
        animating = true;
        final int prevPosition = currPosition;

        ValueAnimator headAnimator = ValueAnimator.ofFloat(currPosition, position);
        headAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                selectedPosition = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        headAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        headAnimator.setDuration(headDuration);
        ValueAnimator tailAnimator = ValueAnimator.ofFloat(prevPosition, position);
        tailAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                selectedPosition = (float) animation.getAnimatedValue();
                invalidate();
            }

        });
        tailAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                selectedPosition = prevPosition;
                currPosition = position;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onEnd(position);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
               onEnd(position);
            }
        });
        tailAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        tailAnimator.setDuration(tailDuration);
        set = new AnimatorSet();
        set.playSequentially(headAnimator, tailAnimator);
        set.start();
    }

    private void onEnd(int position) {
        currPosition = position;
        animating = false;
        if (currPosition == nextPosition)
            nextPosition = POS_UNSET;
        if (nextPosition != POS_UNSET)
            doAnim(nextPosition);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        initAttributes(context, attrs);
    }

    private void initAttributes(Context context, @Nullable AttributeSet attrs) {
        int indicatorColor;
        int selectedIndicatorColor;

        if (attrs == null)  {
            indicatorColor = DEFAULT_INDICATOR_COLOR;
            selectedIndicatorColor = DEFAULT_SELECTED_INDICATOR_COLOR;
            radius = dpToPx(context, DEFAULT_INDICATOR_RADIUS_DP);
            itemSpacing = dpToPx(context, DEFAULT_SPACING_DP);
            headDuration = DEFAULT_ANIMATION_DURATION_MILLIS;
        } else {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.SnakeIndicatorView,
                    0, 0);
            indicatorColor = a.getColor(R.styleable.SnakeIndicatorView_sn_unselected_color, DEFAULT_INDICATOR_COLOR);
            selectedIndicatorColor = a.getColor(R.styleable.SnakeIndicatorView_sn_selected_color, DEFAULT_SELECTED_INDICATOR_COLOR);
            radius = (int) a.getDimension(R.styleable.SnakeIndicatorView_sn_radius, dpToPx(context, DEFAULT_INDICATOR_RADIUS_DP));
            itemSpacing = (int) a.getDimension(R.styleable.SnakeIndicatorView_sn_spacing, dpToPx(context, DEFAULT_SPACING_DP));
            headDuration = a.getInteger(R.styleable.SnakeIndicatorView_sn_head_animation_duration, DEFAULT_ANIMATION_DURATION_MILLIS);
            tailDuration =  a.getInteger(R.styleable.SnakeIndicatorView_sn_tail_animation_duration, DEFAULT_ANIMATION_DURATION_MILLIS);
            a.recycle();
        }
        diameter = 2 * radius;
        diameterWithSpacing = diameter + itemSpacing;

        selectedIndicatorPaint = new Paint();
        selectedIndicatorPaint.setAntiAlias(true);
        selectedIndicatorPaint.setStyle(Paint.Style.FILL);
        selectedIndicatorPaint.setColor(selectedIndicatorColor);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setColor(selectedIndicatorColor);
        linePaint.setStrokeWidth(diameter);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        indicatorPaint = new Paint();
        indicatorPaint.setAntiAlias(true);
        selectedIndicatorPaint.setStyle(Paint.Style.FILL);
        indicatorPaint.setColor(indicatorColor);
    }

    private int getNumItems() {
        return indicatorState == null ? 0 : Math.max(0, indicatorState.numItems());
    }

    public static int dpToPx(Context c, int dp) {
        Resources r = c.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

}

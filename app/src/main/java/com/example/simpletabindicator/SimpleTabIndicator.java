package com.example.simpletabindicator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 简单的ViewPager指示器，标签数量建议在 2~4 个<br/>
 * 支持动态设置标签，支持点击切换标签，支持滚动动画<br/>
 * 不支持超屏幕滚动<br/>
 *
 * @author dwj  2017/8/23 09:08
 */
public class SimpleTabIndicator extends View {

    public static final String TAG = SimpleTabIndicator.class.getSimpleName();

    private String[] mTitles; // 标题

    private Paint mTitlePaint;
    private Rect titleRect = new Rect();
    private int mTitleSize; // 标题大小
    private int mTitleColor; // 标题颜色

    private Paint mTabPaint;
    private int mTabHeight; // 标签高度
    private int mTabColor; // 标签颜色
    private int mTabTopPadding; // 标签距离标题间距
    private float mTabWidthPercent; // 标签宽度百分比

    private int mCurrentTab = -1; // 当前标签索引

    private int mScrollStartX;
    private ValueAnimator mScrollAnimation;

    /**
     * 标签切换回调
     */
    public boolean mFollowViewPagerScroll; // 是否跟随ViewPager滚动

    private ViewPager mViewPager;

    public SimpleTabIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SimpleTabIndicator);
        final float density = context.getResources().getDisplayMetrics().density;

        mTitleSize = (int) ta.getDimension(R.styleable.SimpleTabIndicator_sti_titleSize, density * 18f);
        mTitleColor = ta.getColor(R.styleable.SimpleTabIndicator_sti_titleColor, Color.RED);
        mTabHeight = (int) ta.getDimension(R.styleable.SimpleTabIndicator_sti_tabHeight, density * 3f);
        mTabColor = ta.getColor(R.styleable.SimpleTabIndicator_sti_tabColor, Color.RED);
        mTabTopPadding = (int) ta.getDimension(R.styleable.SimpleTabIndicator_sti_tabTopPadding, density * 12f);

        mTabWidthPercent = ta.getFloat(R.styleable.SimpleTabIndicator_sti_tabWidthPercent, 1f);
        mTabWidthPercent = mTabWidthPercent > 1.0f ? 1.0f : mTabWidthPercent;
        mTabWidthPercent = mTabWidthPercent < 0.0f ? 0.5f : mTabWidthPercent;

        mFollowViewPagerScroll = ta.getBoolean(R.styleable.SimpleTabIndicator_sti_followViewPagerScroll, false);

        mTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTitlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTitlePaint.setTextSize(mTitleSize);
        mTitlePaint.setTextAlign(Paint.Align.CENTER);
        mTitlePaint.setColor(mTitleColor);

        mTabPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTabPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTabPaint.setStrokeWidth(mTabHeight);
        mTabPaint.setColor(mTabColor);
        mTabPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    /**
     * @param viewPager
     */
    public void setViewPager(final ViewPager viewPager, final String... titles) {
        if (viewPager != null) {
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (mFollowViewPagerScroll) {
                        followViewPagerScroll(position, positionOffset, positionOffsetPixels);
                    }
                }

                @Override
                public void onPageSelected(int position) {
                    if (!mFollowViewPagerScroll) {
                        setCurrentTab(position, true);
                    } else {
                        mCurrentTab = position;
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state == ViewPager.SCROLL_STATE_IDLE) {
                        mCurrentTab = viewPager.getCurrentItem();
                    }
                }
            });
            mViewPager = viewPager;
        } else {
            mFollowViewPagerScroll = false;
        }

        if (titles == null || titles.length < 2) {
            throw new IllegalArgumentException("titles must not be null or its length must not be less than 2.");
        } else {
            mTitles = titles;
        }

        setCurrentTab(0, false);
    }

    /**
     * 设置指定标签页
     *
     * @param tab    标签页
     * @param scroll 是否需要滚动动画
     */
    public void setCurrentTab(final int tab, final boolean scroll) {
        setCurrentTab(tab, scroll, false);
    }

    /**
     * 设置指定标签页
     *
     * @param tab    标签页
     * @param scroll 是否需要滚动动画
     */
    private void setCurrentTab(final int tab, final boolean scroll, final boolean callback) {
        if (mTitles != null && tab >= 0 && tab < mTitles.length && tab != mCurrentTab) {

            final int count = mTitles.length;
            final int averageWidth = getWidth() / count;
            final int tabWidth = (int) (averageWidth * mTabWidthPercent);
            final int startX = (averageWidth - tabWidth) / 2 + mCurrentTab * averageWidth;
            final int endX = startX + averageWidth * (tab - mCurrentTab);

            if (scroll) {
                smoothScrollTo(tab, startX, endX, callback);
            } else {
                mScrollStartX = (averageWidth - tabWidth) / 2 + tab * averageWidth;
                mCurrentTab = tab;
                postInvalidate();

                if (mViewPager != null) {
                    mViewPager.setCurrentItem(tab, false);
                }
                if (callback) {
                    if (onTabChangedListener != null) {
                        onTabChangedListener.onTabChanged(tab);
                    }
                }
            }
        }
    }

    private void followViewPagerScroll(int tab, float offsetPercent, int offsetPixels) {
        if (offsetPercent == 0f || offsetPixels == 0) {
            return;
        }

        final int tabCount = mTitles.length;
        final int averageWidth = getWidth() / tabCount;
        final int tabWidth = (int) (averageWidth * mTabWidthPercent);

        if (tab == mCurrentTab) { // 往左滑动 或者 到达左边边界
            mScrollStartX = (int) ((averageWidth - tabWidth) / 2 + mCurrentTab * averageWidth + offsetPercent * averageWidth);
        } else if (tab < mCurrentTab) { // 往右滑动  或者 到达右边边界
            mScrollStartX = (int) ((averageWidth - tabWidth) / 2 + mCurrentTab * averageWidth - (1 - offsetPercent) * averageWidth);
        }

        invalidate();
    }

    /**
     * 滑动tab
     *
     * @param tab
     */

    private void smoothScrollTo(final int tab, int startX, int endX, final boolean callback) {
        mScrollAnimation = ValueAnimator.ofInt(startX, endX);
        mScrollAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                mScrollStartX = value.intValue();
                invalidate();
            }
        });
        mScrollAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mViewPager != null) {
                    mViewPager.setCurrentItem(tab, false);
                }
                if (callback) {
                    if (onTabChangedListener != null) {
                        onTabChangedListener.onTabChanged(tab);
                    }
                }
                mCurrentTab = tab;
            }
        });
        mScrollAnimation.setDuration(100);
        mScrollAnimation.start();
    }

    /**
     * 滑动是否正在执行
     *
     * @return
     */
    private boolean isTabScrolling() {
        return mScrollAnimation != null && mScrollAnimation.isRunning();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (heightMode == MeasureSpec.AT_MOST) {
            mTitlePaint.getTextBounds(TAG, 0, TAG.length(), titleRect);
            int heightSize = getPaddingTop() + titleRect.height() + mTabTopPadding + mTabHeight + getPaddingBottom();
            setMeasuredDimension(widthSize, heightSize);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isTabScrolling()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    final int clickedX = (int) event.getX();
                    final int clickedTab = calculateClickedTab(clickedX);
                    setCurrentTab(clickedTab, true, true);
                    break;
            }
        }
        return true;
    }

    private int calculateClickedTab(int x) {
        final int count = mTitles.length;
        final int averageWidth = getWidth() / count;
        int clickedTab = x / averageWidth;
        return clickedTab;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mTitles != null && mTitles.length > 1) {

            final int count = mTitles.length;
            final int averageWidth = getWidth() / count;
            final int paddingTop = getPaddingTop();

            for (int i = 0; i < count; i++) {
                String title = mTitles[i];
                mTitlePaint.getTextBounds(title, 0, title.length(), titleRect);
                int baseline = paddingTop + getTextBaseline(titleRect.height(), mTitlePaint);
                canvas.drawText(title, averageWidth / 2 + i * averageWidth, baseline, mTitlePaint);
            }

            final int tabWidth = (int) (averageWidth * mTabWidthPercent);
            int startX = mScrollStartX;
            int startY = paddingTop + titleRect.height() + mTabTopPadding;
            int endX = startX + tabWidth;
            int endY = startY;
            canvas.drawLine(startX, startY, endX, endY, mTabPaint);

        }
    }

    private int getTextBaseline(int rectHeight, Paint paint) {
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        return rectHeight / 2 - (fontMetrics.top + fontMetrics.bottom) / 2;
    }

    public interface OnTabChangedListener {
        void onTabChanged(int currentTab);
    }

    private OnTabChangedListener onTabChangedListener;

    public void setOnTabChangedListener(OnTabChangedListener onTabChangedListener) {
        this.onTabChangedListener = onTabChangedListener;
    }
}

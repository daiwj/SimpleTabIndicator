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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * 简单的ViewPager指示器，标签数量建议在 2~5 个<br/>
 * 支持动态设置标签，支持点击切换标签，支持滚动动画<br/>
 * 不支持超屏幕滚动<br/>
 *
 * @author dwj  2017/8/23 09:08
 */
public class SimpleTabIndicator extends View {

    public static final String TAG = SimpleTabIndicator.class.getSimpleName();

    private String[] mTitles; // 标题

    private Paint mTitlePaint;
    private final Rect titleRect = new Rect();
    private int mTitleSize; // 标题大小
    private int mCheckedTitleColor, mUncheckedTitleColor; // 标题颜色
    private int mTitlePadding; // 标题padding 仅stretchMode为stretch_space时有效

    private Paint mLinePaint;
    private int mLineHeight; // 标签高度
    private int mLineColor; // 标签颜色
    private int mLineTopPadding; // 标签距离标题间距
    private float mLineWidthPercent; // 标签宽度百分比

    private ViewPager mViewPager; // 绑定的ViewPager
    public boolean mEnableFollowPageScroll; // 是否与ViewPager联动
    public boolean mEnableLineAnimation; // 标签是否执行滚动动画

    private int mCurrentTabIndex = 0; // 当前标签索引位置

    private int mScrollStartX, mScrollEndX; // 指示器滚动起点, 终点坐标

    private ValueAnimator mLineScrollAnimation; // 手动滚动动画

    public static final int STRETCH_WIDTH = 0;
    public static final int STRETCH_SPACE = 1;
    private int mStretchMode = STRETCH_WIDTH;

    public static final int WRAP_CONTENT = 0;
    public static final int MATCH_PARENT = 1;
    private int mLineWidthMode = STRETCH_WIDTH;

    private SparseArray<Tab> mTabs = new SparseArray<>();

    public SimpleTabIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SimpleTabIndicator);
        final float density = context.getResources().getDisplayMetrics().density;

        mTitleSize = (int) ta.getDimension(R.styleable.SimpleTabIndicator_sti_titleSize, density * 18f);
        mCheckedTitleColor = ta.getColor(R.styleable.SimpleTabIndicator_sti_checkedTitleColor, Color.RED);
        mUncheckedTitleColor = ta.getColor(R.styleable.SimpleTabIndicator_sti_unCheckedTitleColor, Color.RED);
        mLineHeight = (int) ta.getDimension(R.styleable.SimpleTabIndicator_sti_lineHeight, density * 3f);
        mLineColor = ta.getColor(R.styleable.SimpleTabIndicator_sti_lineColor, Color.RED);
        mLineTopPadding = (int) ta.getDimension(R.styleable.SimpleTabIndicator_sti_lineTopPadding, density * 12f);
        mTitlePadding = (int) ta.getDimension(R.styleable.SimpleTabIndicator_sti_titlePadding, 0f);
        mLineWidthPercent = ta.getFloat(R.styleable.SimpleTabIndicator_sti_lineWidthPercent, 1f);
        mLineWidthPercent = mLineWidthPercent > 1.0f ? 1.0f : mLineWidthPercent;
        mLineWidthPercent = mLineWidthPercent < 0.0f ? 0.5f : mLineWidthPercent;

        mEnableFollowPageScroll = ta.getBoolean(R.styleable.SimpleTabIndicator_sti_enableFollowPageScroll, true);
        mEnableLineAnimation = ta.getBoolean(R.styleable.SimpleTabIndicator_sti_enableLineAnimation, true);

        mStretchMode = ta.getInt(R.styleable.SimpleTabIndicator_sti_stretchMode, STRETCH_WIDTH);
        mLineWidthMode = ta.getInt(R.styleable.SimpleTabIndicator_sti_lineWidthMode, WRAP_CONTENT);

        ta.recycle();

        mTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTitlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTitlePaint.setTextSize(mTitleSize);
        mTitlePaint.setTextAlign(Paint.Align.CENTER);
        mTitlePaint.setColor(mCheckedTitleColor);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mLinePaint.setStrokeWidth(mLineHeight);
        mLinePaint.setColor(mLineColor);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setTitles(String... titles) {
        if (titles == null) {
            throw new IllegalArgumentException("titles must not be null");
        } else {
            mTitles = titles;
        }
    }

    /**
     * 绑定ViewPager
     *
     * @param viewPager 当viewpager为空时，指示器不会和ViewPager联动，ViewPager翻页时，指示器不会自动切换。</br>
     * @param titles    标题
     */
    public void setViewPager(final ViewPager viewPager, final String... titles) {

        setTitles(titles);

        if (viewPager != null) {
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (mEnableFollowPageScroll) {
                        followPageScroll(position, positionOffset, positionOffsetPixels);
                    }
                }

                @Override
                public void onPageSelected(int position) {
                    if (!mEnableFollowPageScroll) {
                        setCurrentTab(position, mEnableLineAnimation);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
            viewPager.addOnAdapterChangeListener(new ViewPager.OnAdapterChangeListener() {
                @Override
                public void onAdapterChanged(@NonNull ViewPager viewPager, @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
                    if (newAdapter != null && newAdapter.getCount() != titles.length) {
                        throw new IllegalArgumentException("ViewPager's page count must be same as titles'");
                    }
                }
            });

            final PagerAdapter adapter = viewPager.getAdapter();
            if (adapter != null && adapter.getCount() != titles.length) {
                throw new IllegalArgumentException("ViewPager's page count must be same as titles'");
            }

            mViewPager = viewPager;
        }

        setTab(0, false);
    }

    /**
     * 设置指定标签页
     *
     * @param tabIndex 标签页
     */
    public void setCurrentTab(final int tabIndex) {
        setCurrentTab(tabIndex, mEnableLineAnimation);
    }

    /**
     * 设置指定标签页
     *
     * @param tabIndex     标签页
     * @param tabAnimation 是否需要滚动动画
     */
    public void setCurrentTab(final int tabIndex, final boolean tabAnimation) {
        if (tabIndex >= 0 && tabIndex < mTabs.size() && tabIndex != mCurrentTabIndex) {
            setTab(tabIndex, tabAnimation);
        }
    }

    /**
     * 设置指定标签页
     *
     * @param tabIndex     标签页
     * @param tabAnimation 是否需要滚动动画
     */
    private void setTab(final int tabIndex, final boolean tabAnimation) {
        post(new Runnable() {
            @Override
            public void run() {
                if (tabAnimation) {
                    smoothScrollTo(tabIndex);
                } else {
                    mCurrentTabIndex = tabIndex;

                    final Tab nextTab = mTabs.get(tabIndex);
                    mScrollStartX = nextTab.lineStart;
                    mScrollEndX = nextTab.lineEnd;
                    invalidate();

                    if (mViewPager != null) {
                        mViewPager.setCurrentItem(tabIndex, false);
                    }
                }
            }
        });
    }

    /**
     * 跟随ViewPager滚动
     *
     * @param currentTabIndex 滚动的tab
     * @param offsetPercent   ViewPager当前页滚动距离百分比
     * @param offsetPixels    ViewPager当前页滚动距离
     */
    private void followPageScroll(int currentTabIndex, float offsetPercent, int offsetPixels) {
        if (offsetPercent == 0f || offsetPixels == 0) {
            return;
        }

        final Tab currentTab = mTabs.get(currentTabIndex);
        final Tab nextTab = mTabs.get(currentTabIndex + 1);

        final int distance = nextTab.lineStart - currentTab.lineStart;
        final int widthOffset = nextTab.lineWidth - currentTab.lineWidth;
        mScrollStartX = (int) (currentTab.lineStart + offsetPercent * distance);
        mScrollEndX = (int) (mScrollStartX + currentTab.lineWidth + widthOffset * offsetPercent);

        invalidate();
    }

    /**
     * 滑动tab
     *
     * @param to
     */
    private void smoothScrollTo(final int to) {

        final Tab currentTab = mTabs.get(mCurrentTabIndex);
        final Tab nextTab = mTabs.get(to);

        mCurrentTabIndex = to;

        final int distance = nextTab.lineStart - currentTab.lineStart;
        final int widthOffset = nextTab.lineWidth - currentTab.lineWidth;
        mLineScrollAnimation = ValueAnimator.ofFloat(0f, 1f);
        mLineScrollAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float offsetPercent = ((Float) animation.getAnimatedValue()).floatValue();
                mScrollStartX = (int) (currentTab.lineStart + offsetPercent * distance);
                mScrollEndX = (int) (mScrollStartX + currentTab.lineWidth + widthOffset * offsetPercent);
                invalidate();
            }
        });

        mLineScrollAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mViewPager != null) {
                    mViewPager.setCurrentItem(to, false);
                }
            }
        });
        mLineScrollAnimation.setDuration(300);
        mLineScrollAnimation.start();
    }

    /**
     * 滑动是否正在执行
     *
     * @return
     */
    private boolean isTabScrolling() {
        return mLineScrollAnimation != null && mLineScrollAnimation.isRunning();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (heightMode == MeasureSpec.AT_MOST) { // 布局文件里面设置的高度是wrap_content
            mTitlePaint.getTextBounds(TAG, 0, TAG.length(), titleRect);
            int heightSize = getPaddingTop()  // 顶部padding
                    + titleRect.height()  // 标题文本内容高度
                    + mLineTopPadding // 标签距离标题间距
                    + mLineHeight // 标签高度
                    + getPaddingBottom(); // 底部padding
            setMeasuredDimension(widthSize, heightSize);
        } else { // 非wrap_content的高度模式，不解释了
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw || h != oldh) {
            buildTabs();
        }
    }

    private void buildTabs() {
        mTabs.clear();
        final int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        final int count = mTitles.length;
        if (mStretchMode == STRETCH_WIDTH) {
            final int averageWidth = width / count;
            for (int i = 0; i < count; i++) {
                final String title = mTitles[i];
                mTitlePaint.getTextBounds(title, 0, title.length(), titleRect);
                Tab tab = new Tab();
                tab.title = title;
                tab.start = getPaddingLeft() + averageWidth * i;
                tab.end = tab.start + averageWidth;
                tab.width = averageWidth;
                tab.centerX = tab.start + tab.width / 2;
                if (mLineWidthMode == WRAP_CONTENT) {
                    tab.lineStart = tab.centerX - titleRect.width() / 2 - mTitlePadding;
                    tab.lineEnd = tab.centerX + titleRect.width() / 2 + mTitlePadding;
                    tab.lineWidth = tab.lineEnd - tab.lineStart;
                } else {
                    final int lineWidth = (int) (tab.width * mLineWidthPercent);
                    tab.lineStart = tab.centerX - lineWidth / 2;
                    tab.lineEnd = tab.centerX + lineWidth / 2;
                    tab.lineWidth = tab.lineEnd - tab.lineStart;
                }
                mTabs.put(i, tab);
            }
        } else if (mStretchMode == STRETCH_SPACE) {
            int totalTitleWidth = 0;
            for (String title : mTitles) {
                mTitlePaint.getTextBounds(title, 0, title.length(), titleRect);
                totalTitleWidth += titleRect.width();
            }
            final int spaceWidth = width - totalTitleWidth;
            final int averageSpaceWidth = (int) (spaceWidth / (count * 2) + 0.5f);
            int start = getPaddingLeft();
            for (int i = 0; i < count; i++) {
                final String title = mTitles[i];
                mTitlePaint.getTextBounds(title, 0, title.length(), titleRect);
                Tab tab = new Tab();
                tab.title = title;
                tab.start = start;
                tab.end = tab.start + averageSpaceWidth + titleRect.width() + averageSpaceWidth;
                tab.width = tab.end - tab.start;
                tab.centerX = tab.start + tab.width / 2;
                if (mLineWidthMode == WRAP_CONTENT) {
                    tab.lineStart = tab.centerX - titleRect.width() / 2 - mTitlePadding;
                    tab.lineEnd = tab.centerX + titleRect.width() / 2 + mTitlePadding;
                    tab.lineWidth = tab.lineEnd - tab.lineStart;
                } else {
                    final int lineWidth = (int) (tab.width * mLineWidthPercent);
                    tab.lineStart = tab.centerX - lineWidth / 2;
                    tab.lineEnd = tab.centerX + lineWidth / 2;
                    tab.lineWidth = tab.lineEnd - tab.lineStart;
                }
                start += tab.width;
                mTabs.put(i, tab);
            }
        }

        for (int i = 0; i < count; i++) {
            Tab tab = mTabs.get(i);
            Log.w(TAG, "start: " + tab.start + ", end: " + tab.end + ", width: " + tab.width + ", centerX: " + tab.centerX);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isTabScrolling()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    final int clickedX = (int) event.getX();
                    final int clickedTab = calculateClickedTab(clickedX);
                    if (clickedTab != -1) {
                        setCurrentTab(clickedTab, mEnableLineAnimation);
                        for (OnTabChangedListener onTabChangedListener : mOnTabChangedListeners) {
                            onTabChangedListener.onTabChanged(clickedTab);
                        }
                    }
                    break;
            }
        }
        return true;
    }

    private int calculateClickedTab(int x) {
        for (int i = 0, count = mTabs.size(); i < count; i++) {
            Tab tab = mTabs.get(i);
            if (tab.start <= x && x <= tab.end) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int count = mTabs.size(); // 标题数量
        final int paddingTop = getPaddingTop(); // 上边距

        for (int i = 0; i < count; i++) {
            Tab tab = mTabs.get(i);
            mTitlePaint.setColor(mCurrentTabIndex == i ? mCheckedTitleColor : mUncheckedTitleColor);
            final int baseline = paddingTop + getTextBaseline(titleRect.height(), mTitlePaint);
            canvas.drawText(tab.title, tab.centerX, baseline, mTitlePaint);
        }

        // 标签起始 x = 动态设置的标签起始坐标
        int startX = mScrollStartX;

        /** 标签起始 y = 上边距
         *           + 标题内容高度
         *           + 标签距离标题间距
         *           + 标签一半高度（画笔宽度一半）（为什么要加上标签一半高度？）
         */
        int startY = paddingTop
                + titleRect.height()
                + mLineTopPadding
                + mLineHeight / 2;

        // 标签结束 x = 标签起始坐标 + 标签宽度
        int endX = mScrollEndX;

        // 标签结束 y = 标签起始坐标 + 标签宽度
        int endY = startY;

        canvas.drawLine(startX, startY, endX, endY, mLinePaint);
    }

    private int getTextBaseline(int rectHeight, Paint paint) {
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        return rectHeight / 2 - (fontMetrics.top + fontMetrics.bottom) / 2;
    }

    class Tab {
        String title;
        int start;
        int end;
        int width;
        int centerX;
        int lineStart;
        int lineEnd;
        int lineWidth;
    }

    public interface OnTabChangedListener {
        /**
         * @param currentTabIndex 当前选中了哪个标签
         */
        void onTabChanged(int currentTabIndex);

    }

    private final ArrayList<OnTabChangedListener> mOnTabChangedListeners = new ArrayList<>();

    /**
     * 设置标签变化回调
     *
     * @param onTabChangedListener
     */
    public void addOnTabChangedListener(OnTabChangedListener onTabChangedListener) {
        mOnTabChangedListeners.add(onTabChangedListener);
    }
}

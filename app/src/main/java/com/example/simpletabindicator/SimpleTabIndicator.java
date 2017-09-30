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
import android.view.MotionEvent;
import android.view.View;

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

    private Paint mTabPaint;
    private int mTabHeight; // 标签高度
    private int mTabColor; // 标签颜色
    private int mTabTopPadding; // 标签距离标题间距
    private float mTabWidthPercent; // 标签宽度百分比

    private ViewPager mViewPager; // 绑定的ViewPager
    /**
     * 标签切换回调
     */
    public boolean mFollowPageScrolled; // 是否与ViewPager联动

    private int mCurrentTab = -1; // 当前标签索引位置

    private int mScrollStartX; // 指示器滚动起点坐标
    private ValueAnimator mScrollAnimation; // 手动滚动动画

    public SimpleTabIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SimpleTabIndicator);
        final float density = context.getResources().getDisplayMetrics().density;

        mTitleSize = (int) ta.getDimension(R.styleable.SimpleTabIndicator_sti_titleSize, density * 18f);
        mCheckedTitleColor = ta.getColor(R.styleable.SimpleTabIndicator_sti_checkedTitleColor, Color.RED);
        mUncheckedTitleColor = ta.getColor(R.styleable.SimpleTabIndicator_sti_unCheckedTitleColor, Color.RED);
        mTabHeight = (int) ta.getDimension(R.styleable.SimpleTabIndicator_sti_tabHeight, density * 3f);
        mTabColor = ta.getColor(R.styleable.SimpleTabIndicator_sti_tabColor, Color.RED);
        mTabTopPadding = (int) ta.getDimension(R.styleable.SimpleTabIndicator_sti_tabTopPadding, density * 12f);

        mTabWidthPercent = ta.getFloat(R.styleable.SimpleTabIndicator_sti_tabWidthPercent, 1f);
        mTabWidthPercent = mTabWidthPercent > 1.0f ? 1.0f : mTabWidthPercent;
        mTabWidthPercent = mTabWidthPercent < 0.0f ? 0.5f : mTabWidthPercent;

        mFollowPageScrolled = ta.getBoolean(R.styleable.SimpleTabIndicator_sti_followPageScrolled, false);

        mTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTitlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTitlePaint.setTextSize(mTitleSize);
        mTitlePaint.setTextAlign(Paint.Align.CENTER);
        mTitlePaint.setColor(mCheckedTitleColor);

        mTabPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTabPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTabPaint.setStrokeWidth(mTabHeight);
        mTabPaint.setColor(mTabColor);
        mTabPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    /**
     * 绑定ViewPager
     *
     * @param viewPager 当viewpager为空时，指示器不会和ViewPager联动，ViewPager翻页时，指示器不会自动切换。</br>
     */
    public void setViewPager(final ViewPager viewPager, final String... titles) {

        if (titles == null || titles.length < 2) {
            throw new IllegalArgumentException("titles must not be null or its length must not be less than 2.");
        } else {
            mTitles = titles;
        }

        if (viewPager != null) {
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (mFollowPageScrolled) {
                        followPageScroll(position, positionOffset, positionOffsetPixels);
                    }
                }

                @Override
                public void onPageSelected(int position) {
                    if (!mFollowPageScrolled) {
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
        } else {
            mFollowPageScrolled = false;
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
        post(new Runnable() {
            @Override
            public void run() {
                setCurrentTab(tab, scroll, false);
            }
        });
    }

    /**
     * 设置指定标签页
     *
     * @param tab      标签页
     * @param scroll   是否需要滚动动画
     * @param callback 标签切换时，是否回调切换状态
     */
    private void setCurrentTab(final int tab, final boolean scroll, final boolean callback) {
        if (mTitles != null && tab >= 0 && tab < mTitles.length && tab != mCurrentTab) {

            final int currentTab = mCurrentTab;

            final int count = mTitles.length;
            final int averageWidth = getWidth() / count;
            final int tabWidth = (int) (averageWidth * mTabWidthPercent);

            mCurrentTab = tab;

            if (scroll) {
                final int startX = (averageWidth - tabWidth) / 2 + currentTab * averageWidth;
                final int endX = startX + averageWidth * (tab - currentTab);
                smoothScrollTo(tab, startX, endX, callback);
            } else {
                mScrollStartX = (averageWidth - tabWidth) / 2 + tab * averageWidth;
                invalidate();

                if (mViewPager != null) {
                    mViewPager.setCurrentItem(tab, false);
                }

                if (callback && onTabChangedListener != null) {
                    onTabChangedListener.onTabChanged(tab);
                }
            }
        }
    }

    /**
     * 跟随ViewPager滚动
     *
     * @param tab           滚动的tab
     * @param offsetPercent ViewPager当前页滚动距离百分比
     * @param offsetPixels  ViewPager当前页滚动距离
     */
    private void followPageScroll(int tab, float offsetPercent, int offsetPixels) {
        if (offsetPercent == 0f || offsetPixels == 0) {
            return;
        }

        final int tabCount = mTitles.length;
        final int averageWidth = getWidth() / tabCount;
        final int tabWidth = (int) (averageWidth * mTabWidthPercent);

        // 这个代码等于下面的条件判断语句
        mScrollStartX = (int) ((averageWidth - tabWidth) / 2 + (tab + offsetPercent) * averageWidth);

//        if (tab == mCurrentTab) { // 往左滑动 或者 到达左边边界
//            Log.d(TAG, "向左滑动 tab: " + tab + ", currentTab: " + mCurrentTab + ", offsetPercent: " + offsetPercent);
//            mScrollStartX = (int) ((averageWidth - tabWidth) / 2 + mCurrentTab * averageWidth + offsetPercent * averageWidth);
//        } else if (tab < mCurrentTab) { // 往右滑动  或者 到达右边边界
//            Log.d(TAG, "向右滑动 tab: " + tab + ", currentTab: " + mCurrentTab + ", offsetPercent: " + offsetPercent);
//            mScrollStartX = (int) ((averageWidth - tabWidth) / 2 + mCurrentTab * averageWidth - (1 - offsetPercent) * averageWidth);
//        }

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

                if (callback && onTabChangedListener != null) {
                    onTabChangedListener.onTabChanged(tab);
                }
            }
        });
        mScrollAnimation.setDuration(200);
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

        if (heightMode == MeasureSpec.AT_MOST) { // 布局文件里面设置的高度是wrap_content
            mTitlePaint.getTextBounds(TAG, 0, TAG.length(), titleRect);
            int heightSize = getPaddingTop()  // 顶部padding
                    + titleRect.height()  // 标题文本内容高度
                    + mTabTopPadding // 标签距离标题间距
                    + mTabHeight // 标签高度
                    + getPaddingBottom(); // 底部padding
            setMeasuredDimension(widthSize, heightSize);
        } else { // 非wrap_content的高度模式，不解释了
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

            final int count = mTitles.length; // 标题数量
            final int averageWidth = getWidth() / count; // 屏幕平均宽度
            final int paddingTop = getPaddingTop(); // 上边距

            for (int i = 0; i < count; i++) {
                String title = mTitles[i]; // 对应标题
                mTitlePaint.getTextBounds(title, 0, title.length(), titleRect); // 获取标题内容宽高
                // Paint.Align.LEFT
//                final int x = (averageWidth - titleRect.width()) / 2 + i * averageWidth;

                mTitlePaint.setColor(mCurrentTab == i ? mCheckedTitleColor : mUncheckedTitleColor);

                // Paint.Align.CENTER
                final int x = averageWidth / 2 + i * averageWidth;
                final int baseline = paddingTop + getTextBaseline(titleRect.height(), mTitlePaint);
                canvas.drawText(title, x, baseline, mTitlePaint);
            }

            // 标签宽度 = 屏幕平均宽度 * 标签宽度百分比
            final int tabWidth = (int) (averageWidth * mTabWidthPercent);

            // 标签起始 x = 动态设置的标签起始坐标
            int startX = mScrollStartX;

            // 标签起始 y = 上边距
            //           + 标题内容高度
            //           + 标签距离标题间距
            //           + 标签一半高度（画笔宽度一半）（为什么要加上标签一半高度？）
            //
            int startY = paddingTop
                    + titleRect.height()
                    + mTabTopPadding
                    + mTabHeight / 2;

            // 标签结束 x = 标签起始坐标 + 标签宽度
            int endX = startX + tabWidth;

            // 标签结束 y = 标签起始坐标 + 标签宽度
            int endY = startY;

            canvas.drawLine(startX, startY, endX, endY, mTabPaint);

        }
    }

    private int getTextBaseline(int rectHeight, Paint paint) {
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        return rectHeight / 2 - (fontMetrics.top + fontMetrics.bottom) / 2;
    }

    public interface OnTabChangedListener {
        /**
         * @param currentTab 当前选中了哪个标签
         */
        void onTabChanged(int currentTab);
    }

    private OnTabChangedListener onTabChangedListener;

    /**
     * 设置标签变化回调
     *
     * @param onTabChangedListener
     */
    public void setOnTabChangedListener(OnTabChangedListener onTabChangedListener) {
        this.onTabChangedListener = onTabChangedListener;
    }
}

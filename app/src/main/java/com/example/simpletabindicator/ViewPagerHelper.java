package com.example.simpletabindicator;

import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * 简化和ViewPager绑定
 * Created by hackware on 2016/8/17.
 */

public class ViewPagerHelper {
    public static final List<SimpleTabIndicator> indicators = new ArrayList<>();

    private static final ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            for (SimpleTabIndicator indicator : indicators) {
                indicator.setCurrentTab(position);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

    };

    public static void bind(final SimpleTabIndicator indicator, ViewPager viewPager) {
        indicators.add(indicator);
        viewPager.addOnPageChangeListener(onPageChangeListener);
    }
}

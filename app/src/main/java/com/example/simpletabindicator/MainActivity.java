package com.example.simpletabindicator;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    ViewPager viewPager;

    List<String> TABS = new ArrayList<>(3);

    {
        TABS.add("抵用券");
        TABS.add("加息券");
        TABS.add("提现券");
        TABS.add("什么券");
    }

    SimpleTabIndicator indicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        indicator = (SimpleTabIndicator) findViewById(R.id.tab_indicator);
        indicator.setTitles(TABS.toArray(new String[]{}));
        indicator.setOnTabChangedListener(new SimpleTabIndicator.OnTabChangedListener() {
            @Override
            public void onTabChanged(int currentTab) {
                viewPager.setCurrentItem(currentTab, false);
            }
        });
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new MyAdapter(getSupportFragmentManager(), TABS));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.w(TAG, "position: " + position);
                indicator.setCurrentTab(position, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private class MyAdapter extends FragmentPagerAdapter {

        private List<String> tabs;

        public MyAdapter(FragmentManager fm, List<String> tabs) {
            super(fm);
            this.tabs = tabs;
        }

        @Override
        public Fragment getItem(int position) {
            TestFragment fragment = new TestFragment();
            Bundle args = new Bundle();
            args.putString("title", tabs.get(position));
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabs.get(position);
        }

        @Override
        public int getCount() {
            return tabs.size();
        }
    }
}

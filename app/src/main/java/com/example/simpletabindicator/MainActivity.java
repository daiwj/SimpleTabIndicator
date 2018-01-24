package com.example.simpletabindicator;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    ViewPager viewPager;

    SimpleTabIndicator indicator;
    SimpleTabIndicator indicator2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] titles = {"等额本息", "还本付息", "到期还本付息", "其他产品"};

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new MyAdapter(getSupportFragmentManager(), Arrays.asList(titles)));

        indicator = (SimpleTabIndicator) findViewById(R.id.tab_indicator1);
        indicator.setViewPager(viewPager, titles);
        indicator.addOnTabChangedListener(new SimpleTabIndicator.OnTabChangedListener() {
            @Override
            public void onTabChanged(int currentTabIndex) {
                indicator2.setCurrentTab(currentTabIndex);
            }
        });

        indicator2 = (SimpleTabIndicator) findViewById(R.id.tab_indicator2);
        indicator2.setViewPager(viewPager, titles);
        indicator2.addOnTabChangedListener(new SimpleTabIndicator.OnTabChangedListener() {
            @Override
            public void onTabChanged(int currentTabIndex) {
                indicator.setCurrentTab(currentTabIndex);
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

# 自定义的简易的ViewPager指示器

一个简约而不简单的自定义ViewPager指示器

# 效果图：  
<image src="./image/SimpleTabIndicator_201709301730.gif" />

# **属性相关**
```xml
    <declare-styleable name="SimpleTabIndicator">
        <attr name="sti_titleSize" format="dimension" /> <!-- 标题大小 -->
        <attr name="sti_checkedTitleColor" format="color" />  <!-- 标题选中颜色 -->
        <attr name="sti_unCheckedTitleColor" format="color" />  <!-- 标题未选中颜色 -->
        <attr name="sti_tabHeight" format="dimension" /> <!-- 标签高度 -->
        <attr name="sti_tabColor" format="color" /> <!-- 标签颜色 -->
        <attr name="sti_tabTopPadding" format="dimension"/> <!-- 标签距离标题间距 -->
        <attr name="sti_tabWidthPercent" format="float"/> <!-- 标签宽度百分比 -->
        <attr name="sti_followPageScrolled" format="boolean"/> <!-- 是否与ViewPager联动 -->
    </declare-styleable>
```

# **布局中使用**

```xml
    <com.example.simpletabindicator.SimpleTabIndicator
        android:id="@+id/tab_indicator"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#ffcccc"
        android:paddingBottom="10dp"
        android:paddingTop="10dp"
        app:sti_followPageScrolled="true"
        app:sti_tabColor="#ff0000"
        app:sti_tabHeight="4dp"
        app:sti_tabTopPadding="15dp"
        app:sti_tabWidthPercent="0.8"
        app:sti_checkedTitleColor="#ff0000"
        app:sti_unCheckedTitleColor="#222222"
        app:sti_titleSize="20dp" />
```

# **代码中使用**
```java
String[] titles = {"云", "天河", "云天河", "小云云"};

indicator = (SimpleTabIndicator) findViewById(R.id.tab_indicator);
viewPager = (ViewPager) findViewById(R.id.view_pager);

// 跟随ViewPager联动
indicator.setViewPager(viewPager, titles);
viewPager.setAdapter(new MyAdapter(getSupportFragmentManager(), Arrays.asList(titles)));
  
// 不跟随ViewPager联动
indicator.setOnTabChangedListener(new SimpleTabIndicator.OnTabChangedListener() {
    @Override
    public void onTabChanged(int currentTab) {
        viewPager.setCurrentItem(currentTab); 
    }
});
indicator.setViewPager(null, titles);

viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                
    }

    @Override
    public void onPageSelected(int position) {
         indicator.setCurrentTab(position, true);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
});
```

# 如何联系我：

QQ：1298300385，QQ群：83936534

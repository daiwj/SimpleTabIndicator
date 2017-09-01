# 自定义的简易的ViewPager指示器

一个简约而不简单的自定义ViewPager指示器

**属性相关**
```
    <declare-styleable name="SimpleTabIndicator">
        <attr name="sti_titleSize" format="dimension" /> <!-- 标题大小 -->
        <attr name="sti_titleColor" format="color" />  <!-- 标题颜色 -->
        <attr name="sti_tabHeight" format="dimension" /> <!-- 标签高度 -->
        <attr name="sti_tabColor" format="color" /> <!-- 标签颜色 -->
        <attr name="sti_tabTopPadding" format="dimension"/> <!-- 标签距离标题间距 -->
        <attr name="sti_tabWidthPercent" format="float"/> <!-- 标签宽度百分比 -->
        <attr name="sti_followPageScrolled" format="boolean"/> <!-- 是否与ViewPager联动 -->
    </declare-styleable>
```

**布局中使用**

```
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
        app:sti_titleColor="#ff0000"
        app:sti_titleSize="20dp" />
```

**代码中使用**
```
String[] titles = {"云", "天河", "云天河", "小云云"};

indicator = (SimpleTabIndicator) findViewById(R.id.tab_indicator);
viewPager = (ViewPager) findViewById(R.id.view_pager);
indicator.setViewPager(viewPager, titles);
viewPager.setAdapter(new MyAdapter(getSupportFragmentManager(), Arrays.asList(titles)));
```

# 如何联系我：

QQ：129830085，QQ群：83936534

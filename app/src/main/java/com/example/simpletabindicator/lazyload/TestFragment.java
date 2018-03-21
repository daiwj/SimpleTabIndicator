package com.example.simpletabindicator.lazyload;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.simpletabindicator.R;

/**
 * @author dwj  2017/8/22 17:11
 */

public class TestFragment extends BaseFragment {

    private View contentView;
    private TextView textView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (contentView == null) {
            contentView = inflater.inflate(R.layout.item_fragment, null);
            textView = (TextView) contentView.findViewById(R.id.tv_fragment_title);
            loadData();
        }

        return contentView;
    }

    private void loadData() {
        Bundle args = getArguments();
        textView.setText(args.getString("title"));
    }
}

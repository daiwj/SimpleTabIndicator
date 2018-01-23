package com.example.simpletabindicator;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * @author dwj  2017/8/22 17:11
 */

public class TestFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.item_fragment, null);
        TextView textView = (TextView) contentView.findViewById(R.id.tv_fragment_title);

        Bundle args = getArguments();
        textView.setText(args.getString("title"));

        return contentView;
    }
}

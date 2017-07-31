package com.roy.foldablebutton;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.roy.library.FlodableButton;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ScrollingActivity extends AppCompatActivity {

    private float llOffDistance;
    private FrameLayout.LayoutParams params;
    private boolean isUp = false;   //判断是否为上滑状态
    private boolean isDown = false; //判断是否为下拉状态
    private int i = 0;


    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout toolbarLayout;
    @BindView(R.id.app_bar)
    AppBarLayout appBar;
    @BindView(R.id.text)
    FlodableButton text;
    @BindView(R.id.fl)
    FrameLayout fl;
    @BindView(R.id.tv_text)
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_scrolling);
        ButterKnife.bind(this);

        text.setFoldListener(new FlodableButton.FoldListener() {
            @Override
            public void onFold(boolean isIncrease, FlodableButton sfb) {
                if (isIncrease)
                    isUp = true;
                else
                    isDown = true;
            }
        });
        text.setOnClickListener(new FlodableButton.OnClickListener() {
            @Override
            public void onClick(FlodableButton sfb) {
                Log.i("TAG","-----");
                startActivity(new Intent(ScrollingActivity.this, MainActivity.class));
            }
        });

        appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                //防止初始化进来两次
                i++;
                if (i <= 2) {
                    return;
                }

                if (params == null) {
                    params = (FrameLayout.LayoutParams) text.getLayoutParams();
                    llOffDistance = params.topMargin;
                    isUp = true;
                    isDown = true;
                }

                float distance = llOffDistance + verticalOffset;
                //滑倒顶端状态 保持20的间距
                if (distance <= 20) {
                    distance = 20;
                    startScroll();
                }
                //滑倒底端状态
                if (verticalOffset == 0) {
                    if (isDown && !text.isIncrease()) {
                        text.startScroll();
                    }
                }
                params.topMargin = (int) distance;
                fl.requestLayout();
            }
        });


    }

    public void startScroll() {
        if (isUp) {
            isUp = false;
            text.startScroll();
        }
    }
}

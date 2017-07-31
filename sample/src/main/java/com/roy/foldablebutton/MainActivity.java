package com.roy.foldablebutton;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.roy.library.FlodableButton;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.fb)
    FlodableButton fb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        fb.setOnClickListener(new FlodableButton.OnClickListener() {
            @Override
            public void onClick(FlodableButton sfb) {
                fb.startScroll();
            }
        });

        fb.setFoldListener(new FlodableButton.FoldListener() {
            @Override
            public void onFold(boolean isIncrease, FlodableButton sfb) {
                String text = isIncrease? "展开了":"折叠了";
                Toast.makeText(MainActivity.this,text,Toast.LENGTH_SHORT).show();
            }
        });
    }
}

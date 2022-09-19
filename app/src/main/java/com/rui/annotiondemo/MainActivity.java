package com.rui.annotiondemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.rui.annotations.BindView;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.testText)
    private TextView testText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyButterKnife.bind(this);
        testText.setText("123");
    }
}
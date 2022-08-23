package com.rui.annotiondemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.rui.annotations.BindView;

public class MainActivity extends AppCompatActivity {

    @BindView(value = "value", id = 1)
    private String as;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
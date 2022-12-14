package com.rui.annotiondemo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rui.annotations.BindString;
import com.rui.annotations.BindView;
import com.rui.annotations.OnClick;

@SuppressLint("NonConstantResourceId")
public class MainActivity extends AppCompatActivity { //1.TypeElement 类节点

    @BindView(R.id.testText)
    TextView testText;//3.VariableElement 成员变量节点

    @BindView(R.id.btn1)
    Button btn1;

    @BindView(R.id.layout1)
    RelativeLayout layout1;

    @BindString(R.string.testText1)
    String test1;

    @BindString(R.string.testText2)
    String test2;

    @BindString(R.string.testText3)
    String test3;

    @BindString(R.string.testText4)
    String test4;

    @BindString(R.string.testText5)
    String test5;

    @Override
    protected void onCreate(Bundle savedInstanceState) { //2.ExecutableElement 方法节点
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyButterKnife.bind(this);
        testText.setText(test1);
    }

    @OnClick(R.id.testText)
    public void textClick(View view) {
        Toast.makeText(this, test2, Toast.LENGTH_SHORT).show();

    }

    @OnClick(R.id.btn1)
    public void btnClick(View view) {
        testText.setText(test3);

    }
}
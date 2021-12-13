package com.k2archer.k2outer.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.k2archer.lib.k2router.api.K2Router;
import com.k2archer.lib.k2router.api.Postcard;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        K2Router.init(getApplication());

        K2Router.getInstance().navigation(this, "Module1Activity");
    }
}

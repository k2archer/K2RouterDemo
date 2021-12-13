package com.k2archer.k2outer.module1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.k2archer.k2outer.common_service.IHiService;
import com.k2archer.lib.k2router.api.K2Route;
import com.k2archer.lib.k2router.api.K2Router;
import com.k2archer.lib.k2router.api.Postcard;

@K2Route("Module1Activity")
public class Module1Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moulde1);

        findViewById(R.id.btn_hi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    K2Router.getInstance().navigation(IHiService.class).hello();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

package com.example.dialogsequence;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dialogsequence.dag.DialogChainListener;
import com.example.dialogsequence.dag.DialogManager;

public class MainActivity extends AppCompatActivity {

    private final DialogChainListener mChainListener = new DialogChainListener() {
        @Override
        public void onChainStarted() {
            Log.d("DialogChain", "onChainStarted");
        }

        @Override
        public void onChainStopped() {
            Log.d("DialogChain", "onChainStopped");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogManager dialogManager = DialogManager.newInstance(MainActivity.this);
                dialogManager.setChainListener(mChainListener);
                dialogManager.start();
            }
        });

    }
}

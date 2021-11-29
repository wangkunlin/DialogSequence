package com.example.test.a;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.dag.dialog.sequence.DialogTask;

/**
 * On 2021-11-26
 */
public class DialogTaskA extends DialogTask {

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onShowDialog() {
        Log.d("Dialog", "show dialog: " + getName());

        Toast.makeText(getActivity(), "Show " + getName(), Toast.LENGTH_SHORT).show();

        // 模拟弹框显示 6s 后关闭
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("Dialog", "dismiss dialog: " + getName());
                notifyDialogDismissed();
            }
        }, 6000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}

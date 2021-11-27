package com.example.dialogsequence.dlg;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.dialogsequence.dag.DialogTask;

/**
 * On 2021-11-26
 */
public class DialogTaskD extends DialogTask {

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onShowDialog() {
        Log.d("Dialog", "show dialog: " + getName());

        Toast.makeText(getActivity(), "Show " + getName(), Toast.LENGTH_SHORT).show();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("Dialog", "dismiss dialog: " + getName());
                notifyDialogDismissed();
            }
        }, 2000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}

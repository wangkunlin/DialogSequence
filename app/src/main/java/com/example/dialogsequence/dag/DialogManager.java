package com.example.dialogsequence.dag;

import android.app.Activity;

/**
 * On 2021-11-26
 */
public interface DialogManager {

    static DialogManager newInstance(Activity activity) {
        return new DialogManagerImpl(activity);
    }

    void setChainListener(DialogChainListener listener);

    void start();

    void stop();

}

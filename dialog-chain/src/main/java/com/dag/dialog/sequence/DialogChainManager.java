package com.dag.dialog.sequence;

import android.app.Activity;

import androidx.annotation.Nullable;

/**
 * On 2021-11-26
 */
public interface DialogChainManager {

    static DialogChainManager newInstance(Activity activity) {
        return newInstance(activity, null);
    }

    static DialogChainManager newInstance(Activity activity, @Nullable String tagName) {
        return new DialogChainManagerImpl(activity, tagName);
    }

    void setChainListener(DialogChainListener listener);

    void start();

    void stop();

}

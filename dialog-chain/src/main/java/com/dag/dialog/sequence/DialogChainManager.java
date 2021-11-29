package com.dag.dialog.sequence;

import android.app.Activity;

/**
 * On 2021-11-26
 */
public interface DialogChainManager {

    static DialogChainManager newInstance(Activity activity) {
        return new DialogChainManagerImpl(activity);
    }

    void setChainListener(DialogChainListener listener);

    void start();

    void stop();

}

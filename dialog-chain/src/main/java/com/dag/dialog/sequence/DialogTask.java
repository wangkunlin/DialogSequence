package com.dag.dialog.sequence;

import android.app.Activity;

/**
 * On 2021-11-26
 */
public abstract class DialogTask {

    String mName;

    DialogChainManagerImpl mChainManager;

    private boolean mCreated = false;
    private boolean mShown = false;
    private boolean mDestroyed = false;
    private boolean mSkipped = false;

    final void callCreate() {
        if (mCreated) {
            return;
        }
        onCreate();
        mCreated = true;
    }

    public void onCreate() {
    }

    public final void requestStopChain() {
        mChainManager.stop();
    }

    public final void skipShow() {
        mSkipped = true;
    }

    public final String getName() {
        return mName;
    }

    public final Activity getActivity() {
        return mChainManager.getActivity();
    }

    public final void dependsOn(String... dependencies) {
        mChainManager.taskDependsOn(mName, dependencies);
    }

    final void callShowDialog() {
        if (mSkipped) {
            notifyDialogDismissed();
            return;
        }
        if (mShown || mDestroyed) {
            return;
        }
        mShown = true;
        onShowDialog();
    }

    public abstract void onShowDialog();

    public final void notifyDialogDismissed() {
        mChainManager.dialogDismissed(this);
    }

    public final boolean isDestroyed() {
        return mDestroyed;
    }

    final void callDestroy() {
        if (mDestroyed) {
            return;
        }
        onDestroy();
        mDestroyed = true;
    }

    public void onDestroy() {
    }
}

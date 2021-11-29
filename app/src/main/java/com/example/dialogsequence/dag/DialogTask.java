package com.example.dialogsequence.dag;

import android.app.Activity;

/**
 * On 2021-11-26
 */
public abstract class DialogTask {

    String mName;

    DialogManagerImpl mDialogManager;

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
        mDialogManager.stop();
    }

    public final void skipShow() {
        mSkipped = true;
    }

    public final String getName() {
        return mName;
    }

    public final Activity getActivity() {
        return mDialogManager.getActivity();
    }

    public final void dependsOn(String... names) {
        for (String name : names) {
            if (name == null) {
                throw new NullPointerException();
            }
            mDialogManager.nodeRelation(mName, name);
        }
    }

    final void callShowDialog() {
        if (mSkipped) {
            notifyDialogDismissed();
            return;
        }
        if (mShown) {
            return;
        }
        mShown = true;
        onShowDialog();
    }

    public abstract void onShowDialog();

    public final void notifyDialogDismissed() {
        mDialogManager.dialogDismissed(this);
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

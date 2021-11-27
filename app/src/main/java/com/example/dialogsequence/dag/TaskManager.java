package com.example.dialogsequence.dag;

import android.app.Activity;

/**
 * On 2021-11-27
 */
public interface TaskManager {

    Activity getActivity();

    <T extends DialogTask> T create(String name, Class<? extends T> type);

    void dependsOn(String target, String... dependency);

}

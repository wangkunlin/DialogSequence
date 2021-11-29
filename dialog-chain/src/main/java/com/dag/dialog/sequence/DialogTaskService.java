package com.dag.dialog.sequence;

import androidx.annotation.Nullable;

/**
 * On 2021-11-26
 */
public interface DialogTaskService {

    @Nullable
    String getTagName();

    void config(TaskManager manager);

}

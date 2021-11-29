package com.example.test.a;

import androidx.annotation.Nullable;

import com.example.base.DialogTypes;
import com.dag.dialog.sequence.DialogTaskService;
import com.dag.dialog.sequence.TaskManager;

/**
 * On 2021-11-26
 */
public class DialogTaskServiceA implements DialogTaskService {

    @Nullable
    @Override
    public String getTagName() {
        return null;
    }

    @Override
    public void config(TaskManager manager) {
        // 每个 service 可以创建多个 task
        manager.create(DialogTypes.DIALOG_A, DialogTaskA.class);

        manager.create(DialogTypes.DIALOG_B, DialogTaskB.class);
    }
}

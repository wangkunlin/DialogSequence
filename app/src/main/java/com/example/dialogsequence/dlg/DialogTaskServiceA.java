package com.example.dialogsequence.dlg;

import com.example.dialogsequence.dag.DialogTaskService;
import com.example.dialogsequence.dag.TaskManager;

/**
 * On 2021-11-26
 */
public class DialogTaskServiceA implements DialogTaskService {

    @Override
    public void config(TaskManager manager) {
        // 每个 service 可以创建多个 task
        manager.create(DialogTypes.DIALOG_A, DialogTaskA.class);

        manager.create(DialogTypes.DIALOG_B, DialogTaskB.class);
    }
}

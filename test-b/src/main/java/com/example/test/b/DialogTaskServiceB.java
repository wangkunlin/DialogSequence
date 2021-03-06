package com.example.test.b;

import com.example.base.DialogTypes;
import com.dag.dialog.sequence.DialogTaskService;
import com.dag.dialog.sequence.TaskManager;

/**
 * On 2021-11-26
 */
public class DialogTaskServiceB implements DialogTaskService {

    @Override
    public void config(TaskManager manager) {

        manager.create(DialogTypes.DIALOG_E, DialogTaskD.class);

        manager.create(DialogTypes.DIALOG_C, DialogTaskC.class);

        manager.create(DialogTypes.DIALOG_D, DialogTaskD.class);
    }
}

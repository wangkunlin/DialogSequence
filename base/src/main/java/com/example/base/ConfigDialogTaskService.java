package com.example.base;

import androidx.annotation.Nullable;

import com.dag.dialog.sequence.DialogTaskService;
import com.dag.dialog.sequence.TaskManager;

/**
 * On 2021-11-26
 */
public class ConfigDialogTaskService implements DialogTaskService {

    @Nullable
    @Override
    public String getTagName() {
        return null;
    }

    // 这里为了方便统一管理依赖关系, 而单独使用一个 service
    @Override
    public void config(TaskManager manager) {
        // a 依赖于 b, 则 b 优先于 a 展示
        manager.dependsOn(DialogTypes.DIALOG_A, DialogTypes.DIALOG_B);

        manager.dependsOn(DialogTypes.DIALOG_D, DialogTypes.DIALOG_C);
//        manager.dependsOn(DialogTypes.DIALOG_C, DialogTypes.DIALOG_D); // 这里 c 和 d 会循环依赖, 执行后, 会崩溃

        manager.dependsOn(DialogTypes.DIALOG_C, DialogTypes.DIALOG_A, DialogTypes.DIALOG_B);
    }
}

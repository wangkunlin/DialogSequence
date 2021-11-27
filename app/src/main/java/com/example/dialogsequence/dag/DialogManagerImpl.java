package com.example.dialogsequence.dag;

import android.app.Activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.Stack;

/**
 * On 2021-11-26
 */
class DialogManagerImpl implements DialogManager {

    private final Activity mActivity;

    private final Map<String, DialogTask> mTaskMap = new HashMap<>();

    // 拓扑排序结果, 弹出顺序即为排序结果顺序
    private final Stack<String> mSeq = new Stack<>();

    private boolean mStarted = false;
    private boolean mStopped = false;

    // 无序的有向图
    private final Map<String, TaskNode> mNodeMap = new HashMap<>();

    private DialogChainListener mChainListener;

    private static class TaskNode {
        private final String name; // 节点名
        private int inCount = 0; // 入度
        private List<TaskNode> outNodes; // 可到达的节点

        private TaskNode(String name) {
            this.name = name;
        }

        private void increaseCount() {
            inCount++;
        }

        private boolean isEmptyIn() {
            return inCount == 0;
        }

        private boolean addOutNode(TaskNode node) {
            if (outNodes == null) {
                outNodes = new ArrayList<>();
            }
            if (outNodes.contains(node)) {
                return false;
            }
            outNodes.add(node);
            return true;
        }

        private static TaskNode create(String name) {
            return new TaskNode(name);
        }
    }

    private TaskNode getNode(String name) {
        TaskNode node = mNodeMap.get(name);
        if (node == null) {
            node = TaskNode.create(name);
            mNodeMap.put(name, node);
        }
        return node;
    }

    // in 依赖于 out, out ----> in
    final void nodeRelation(String in, String out) {
        TaskNode inNode = getNode(in);
        TaskNode outNode = getNode(out);
        if (inNode == outNode) { // 自己不能依赖自己
            return;
        }
        if (outNode.addOutNode(inNode)) {
            inNode.increaseCount(); // 入度 +1
        }
    }

    private final TaskManager mTaskManager = new TaskManager() {

        @Override
        public Activity getActivity() {
            return mActivity;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends DialogTask> T create(String name, Class<? extends T> type) {
            DialogTask task = mTaskMap.get(name);
            if (task != null) {
                throw new RuntimeException("already has a task named: " + name);
            }

            try {
                task = type.getDeclaredConstructor().newInstance();
                task.mDialogManager = DialogManagerImpl.this;
                task.mName = name;
                getNode(name);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            task.callCreante();

            mTaskMap.put(name, task);
            return (T) task;
        }

        @Override
        public void dependsOn(String in, String... outs) {
            for (String out : outs) {
                if (out == null) {
                    throw new NullPointerException();
                }
                nodeRelation(in, out);
            }
        }
    };

    DialogManagerImpl(Activity activity) {
        mActivity = activity;
    }

    final Activity getActivity() {
        return mActivity;
    }

    @Override
    public final void setChainListener(DialogChainListener listener) {
        mChainListener = listener;
    }

    @Override
    public final void start() {
        if (mStarted) {
            throw new IllegalStateException("dialog manager is oneshot");
        }
        mStarted = true;
        if (mChainListener != null) {
            mChainListener.onChainStarted();
        }

        collectNodes();

        orderTasks();

        runNextTask();
    }

    private void runNextTask() {
        if (mStopped) {
            return;
        }
        if (!mSeq.empty()) {
            String name = mSeq.pop();
            DialogTask task = mTaskMap.get(name);
            if (task != null) {
                task.callShowDialog();
            } else {
                runNextTask();
            }
        } else {
            mStopped = true;
            onStopped();
        }
    }

    private void onStopped() {
        if (mChainListener != null) {
            mChainListener.onChainStopped();
        }
    }

    private void collectNodes() {
        // 使用 spi 获取所有的 task service, 并配置
        ServiceLoader<DialogTaskService> loader = ServiceLoader.load(DialogTaskService.class);
        for (DialogTaskService node : loader) {
            node.config(mTaskManager);
        }
    }

    // 拓扑排序
    private void orderTasks() {
        Set<String> using = new HashSet<>(); // 标记是否正在被使用
        boolean noEmptyInNode = true; // 没有入度为 0 的点, 则必有环

        for (String name : mNodeMap.keySet()) {
            if (mSeq.contains(name)) { // 已经放入序列, 跳过
                continue;
            }
            TaskNode node = mNodeMap.get(name);
            if (node == null) {
                continue;
            }
            if (node.isEmptyIn()) {
                noEmptyInNode = false;
                dfs(node, using);
            }
        }
        if (noEmptyInNode) {
            throw new IllegalStateException("Cycle detected");
        }
    }

    // 深度优先遍历
    private void dfs(TaskNode node, Set<String> using) {
        String name = node.name;
        if (mSeq.contains(name)) {
            return;
        }
        if (using.contains(name)) { // 当前节点 已经被加入过, 则代表有环
            throw new RuntimeException("Cycle detected at: " + name);
        }
        using.add(name);
        List<TaskNode> nodes = node.outNodes;
        if (nodes != null) {
            for (TaskNode outNode : nodes) {
                dfs(outNode, using);
            }
        }
        using.remove(name);
        mSeq.push(name); // 遍历完成，放入序列栈中
    }

    @Override
    public final void stop() {
        if (!mStarted) {
            return;
        }
        if (mStopped) {
            return;
        }
        mStopped = true;
        stopTasks();
        onStopped();
    }

    private void stopTasks() {
        while (!mSeq.empty()) {
            String name = mSeq.pop();
            DialogTask task = mTaskMap.get(name);
            if (task != null) {
                task.callDestroy();
            }
        }
    }

    final void dialogDismissed(DialogTask task) {
        if (task.isDestroyed()) {
            return;
        }
        task.callDestroy();
        runNextTask();
    }
}

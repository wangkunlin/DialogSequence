package com.dag.dialog.sequence;

import android.app.Activity;
import android.text.TextUtils;

import androidx.annotation.Nullable;

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
class DialogChainManagerImpl implements DialogChainManager {

    private final Activity mActivity;
    @Nullable
    private final String mTag;

    private final Map<String, DialogTask> mTaskMap = new HashMap<>();

    private boolean mStarted = false;
    private boolean mStopped = false;

    // 有向图的邻接列表
    private final Map<String, TaskNode> mNodeGraph = new HashMap<>();

    // 拓扑排序结果, 弹出顺序即为排序结果顺序
    private final Stack<String> mSeq = new Stack<>();

    private DialogChainListener mChainListener;

    private static class TaskNode {
        private final String name; // 节点名
        private int inCount = 0; // 入度
        private List<TaskNode> toNodes; // 可到达的节点

        private TaskNode(String name) {
            this.name = name;
        }

        private void increaseCount() {
            inCount++;
        }

        private boolean isEmptyIn() {
            return inCount == 0;
        }

        private void to(TaskNode toNode) {
            TaskNode fromNode = this;
            if (fromNode == toNode) { // 自己不能依赖自己
                return;
            }
            if (toNodes == null) {
                toNodes = new ArrayList<>();
            }
            if (toNodes.contains(toNode)) {
                return;
            }
            toNodes.add(toNode);
            toNode.increaseCount(); // 入度 +1
        }

        private static TaskNode create(String name) {
            return new TaskNode(name);
        }
    }

    private TaskNode getNode(String name) {
        TaskNode node = mNodeGraph.get(name);
        if (node == null) {
            node = TaskNode.create(name);
            mNodeGraph.put(name, node);
        }
        return node;
    }

    // to 依赖于 from, from ----> to
    private void nodeRelation(String to, String from) {
        TaskNode toNode = getNode(to);
        TaskNode fromNode = getNode(from);
        fromNode.to(toNode);
    }

    final void taskDependsOn(String to, String... fromNames) {
        if (to == null) {
            throw new NullPointerException();
        }
        for (String from : fromNames) {
            if (from == null) {
                throw new NullPointerException();
            }
            nodeRelation(to, from);
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
            if (name == null) {
                throw new NullPointerException("name == null");
            }
            DialogTask task = mTaskMap.get(name);
            if (task != null) {
                throw new RuntimeException("already has a task named: " + name +
                        " class: " + task.getClass().getName());
            }

            try {
                task = type.getDeclaredConstructor().newInstance();
                task.mChainManager = DialogChainManagerImpl.this;
                task.mName = name;
                getNode(name);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            task.callCreate();

            mTaskMap.put(name, task);
            return (T) task;
        }

        @Override
        public void dependsOn(String name, String... dependencies) {
            taskDependsOn(name, dependencies);
        }
    };

    DialogChainManagerImpl(Activity activity, @Nullable String tagName) {
        mActivity = activity;
        if (tagName == null) {
            mTag = "";
        } else {
            mTag = tagName.trim();
        }
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

        configServices();

        orderTasks(false);

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

    private void configServices() {
        // 使用 spi 获取所有的 task service, 并配置
        ServiceLoader<DialogTaskService> loader = ServiceLoader.load(DialogTaskService.class);
        for (DialogTaskService service : loader) {
            String tag = service.getTagName();
            if (tag == null) {
                tag = "";
            }
            if (TextUtils.equals(tag.trim(), mTag)) {
                service.config(mTaskManager);
            }
        }
    }

    // 拓扑排序
    private void orderTasks(boolean checkCycle) {
        Set<String> using = new HashSet<>(); // 标记是否正在被使用
        boolean noEmptyInNode = true; // 没有入度为 0 的点, 则必有环

        for (String name : mNodeGraph.keySet()) {
            if (mSeq.contains(name)) { // 已经放入序列, 跳过
                continue;
            }
            TaskNode node = mNodeGraph.get(name);
            if (node == null) {
                continue;
            }
            if (checkCycle || node.isEmptyIn()) {
                noEmptyInNode = false;
                dfs(node, using);
            }
        }
        if (!checkCycle && noEmptyInNode) { // 没有找到入度为 0 的点，则开始检查环
            orderTasks(true);
        }
    }

    // 深度优先遍历
    @Nullable
    private List<String> dfs(TaskNode node, Set<String> using) {
        String name = node.name;
        if (mSeq.contains(name)) {
            return null;
        }
        if (using.contains(name)) { // 当前节点 已经被加入过, 则代表有环
            List<String> cycleList = new ArrayList<>();
            cycleList.add(name);
            return cycleList;
        }
        using.add(name);
        List<TaskNode> nodes = node.toNodes;
        if (nodes != null) {
            for (TaskNode outNode : nodes) {
                List<String> cycleList = dfs(outNode, using);
                if (cycleList != null) { // Cycle detected
                    String cycleNode = cycleList.get(cycleList.size() - 1);
                    cycleList.add(0, name);
                    if (name.equals(cycleNode)) { // cycle
                        throw new RuntimeException("Cycle detected: " + Util.join(" -> ", cycleList));
                    }
                    return cycleList;
                }
            }
        }
        using.remove(name);
        mSeq.push(name); // 遍历完成，放入序列栈中
        return null;
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

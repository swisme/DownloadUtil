package com.sw.downloaddemo.ThreadPoll;

import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {

    private static ThreadPoolManager mThreadPoolManager = null;
    private ThreadPoolExecutor mThreadPoolExecutor;
    private PriorityBlockingQueue<Runnable> mQueue;

    private ThreadPoolManager() {
        mQueue = new PriorityBlockingQueue<>(20, new ComparePriority());
        mThreadPoolExecutor = new ThreadPoolExecutor(1, 3, 1, TimeUnit.MINUTES, mQueue);
    }

    public static ThreadPoolManager getInstance() {
        if (mThreadPoolManager == null) {
            mThreadPoolManager = new ThreadPoolManager();
        }
        return mThreadPoolManager;

    }

    /**
     * 添加任务
     *
     * @param task
     */
    public void addTask(Runnable task) {
        if (task != null) {
            mThreadPoolExecutor.execute(task);
        }
    }

    /**
     * 移除任务
     *
     * @param task
     */
    public void removeTask(Runnable task) {
        if (task != null) {
            mQueue.remove(task);
            mThreadPoolExecutor.remove(task);
        }
    }

    /**
     * 清除所有正在执行的线程任务
     */
    public void removeAllTask() {
        try {
            Iterator<Runnable> iterator = mQueue.iterator();
            while (iterator.hasNext()) {
                mThreadPoolExecutor.remove(iterator.next());
                mQueue.remove();
            }
        } catch (Exception e) {
        }
    }

}

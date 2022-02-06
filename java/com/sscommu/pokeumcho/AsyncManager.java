package com.sscommu.pokeumcho;

import android.os.AsyncTask;
import android.os.Handler;

import java.util.ArrayList;

public class AsyncManager {

    private ArrayList<AsyncTask> mTaskList;
    private ArrayList<Handler> mHandlerList;

    public AsyncManager() {
        mTaskList = new ArrayList<AsyncTask>();
        mHandlerList = new  ArrayList<Handler>();
    }

    public ArrayList<AsyncTask> getTaskList() { return mTaskList; }

    public void addTask(AsyncTask task) { mTaskList.add(task); }

    public void removeTask(AsyncTask task) { mTaskList.remove(task); }

    public void addHandler(Handler handler) { mHandlerList.add(handler); }

    public void removeHandler(Handler handler) { mHandlerList.remove(handler); }

    public void clear() {

        for (AsyncTask task: mTaskList)
            task.cancel(true);
        mTaskList.clear();

        for (Handler handler: mHandlerList)
            handler.removeCallbacksAndMessages(null);
        mHandlerList.clear();
    }
}

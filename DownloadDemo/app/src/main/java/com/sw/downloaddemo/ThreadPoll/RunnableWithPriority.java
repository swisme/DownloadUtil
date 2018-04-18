package com.sw.downloaddemo.ThreadPoll;

public abstract class RunnableWithPriority implements Runnable {

	public enum Priority {
		LOW, NORMAL, HIGH, IMMEDIATE
	}

	public Priority priority;

	public Priority getPriority() {
		return priority;
	}

	public RunnableWithPriority(Priority priority) {
		this.priority = priority;
	}

}
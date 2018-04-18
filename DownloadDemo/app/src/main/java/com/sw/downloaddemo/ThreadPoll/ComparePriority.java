package com.sw.downloaddemo.ThreadPoll;

import java.util.Comparator;

public class ComparePriority<T extends RunnableWithPriority> implements Comparator<T> {

	@Override
	public int compare(T lhs, T rhs) {
		RunnableWithPriority.Priority left = lhs.getPriority();
		RunnableWithPriority.Priority right = rhs.getPriority();

		//priority相同，则采用FIFO，priority不同，则优先级高的先执行
		return right.ordinal()-left.ordinal();
	}
}

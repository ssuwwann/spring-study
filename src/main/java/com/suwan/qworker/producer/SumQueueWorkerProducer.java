package com.suwan.qworker.producer;

import com.suwan.qworker.model.SumTask;
import com.suwan.qworker.worker.QueueWorker;

import java.util.stream.IntStream;

public class SumQueueWorkerProducer {
  private final QueueWorker<SumTask> queueWorker;
  private final int random;

  public SumQueueWorkerProducer(QueueWorker<SumTask> queueWorker, int random) {
    this.queueWorker = queueWorker;
    this.random = random;
  }

  // 작업 정의 및 추가 
  public void producerRandomSumTasks(int taskCount) {
    IntStream.range(0, taskCount)
            .forEach(i -> {
              SumTask task = new SumTask(i, random);

              queueWorker.submitTask(task);
            });
  }
}

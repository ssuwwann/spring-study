package com.suwan.qworker.worker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class QueueWorker<T> {
  private final BlockingQueue<T> queue; // 작업이 담김
  private final ExecutorService executor;
  private final Consumer<T> taskProcessor;

  public QueueWorker(int workerCount, Consumer<T> taskProcessor) {
    this.queue = new LinkedBlockingQueue<>(1_000); // MAX = 1000
    this.executor = Executors.newFixedThreadPool(workerCount); // count = 4 (4개의 스레드)
    this.taskProcessor = taskProcessor;

    // 워커 스레드 시작
    IntStream.range(0, workerCount)
            .forEach(i -> executor.submit(this::processQueue));
  }

  private void processQueue() {
    // 조건 = 중단 요청이 있다면
    while (!Thread.currentThread().isInterrupted()) {
      try {
        T task = queue.take(); // 작업 가져오기
        taskProcessor.accept(task); // 작업 처리
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // 중단 시키기
        break;
      }
    }
  }

  public void submitTask(T task) {
    queue.offer(task);
  }

  public void shutdown() {
    executor.shutdown();
  }

}

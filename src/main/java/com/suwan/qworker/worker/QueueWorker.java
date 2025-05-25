package com.suwan.qworker.worker;

import java.util.concurrent.*;
import java.util.function.Function;

/**
 * BlockingQueue로 작업 관리
 * 4개의 워커 스레드가 동시에 작업 처리
 * 각 스레드가 처리한 결과를 저장
 *
 * @param <T>
 */
public class QueueWorker<T, R> {

  private final BlockingQueue<T> queue;
  private final ExecutorService executor;
  private final Function<T, R> taskProcessor;
  private final String workerName;

  public QueueWorker(String workerName, int queueSize, int workerCount, Function<T, R> taskProcessor) {
    this.taskProcessor = taskProcessor;
    this.queue = new LinkedBlockingQueue<>(queueSize);
    this.executor = Executors.newFixedThreadPool(workerCount);
    this.workerName = workerName;

    System.out.printf("[%s] %d개의 워커 스레드 시작%n", workerName, workerCount);
  }

  // completableFuture로 처리
  public CompletableFuture<R> submitTaskAsync(T task) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        queue.put(task); // 큐에 추가
        T queuedTask = queue.take(); // 큐에서 가져오기

        String threadName = Thread.currentThread().getName();
        System.out.printf("[%s-%s] 작업 처리: %s%n", workerName, threadName, queuedTask);

        return taskProcessor.apply(queuedTask);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new CompletionException(e);
      }
    });
  }


  public void shutdown() {
    System.out.printf("[%s] 종료 시작\n", workerName);
    executor.shutdown();
  }

}

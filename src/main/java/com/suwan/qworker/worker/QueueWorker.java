package com.suwan.qworker.worker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * BlockingQueue로 작업 관리
 * 4개의 워커 스레드가 동시에 작업 처리
 * 각 스레드가 처리한 결과를 저장
 *
 * @param <T>
 */
public class QueueWorker<T> {

  private final BlockingQueue<T> queue;
  private final ExecutorService executor;
  private final Consumer<T> taskProcessor;
  private final String workerName;

  public QueueWorker(String workerName, int queueSize, int workerCount, Consumer<T> taskProcessor) {
    this.taskProcessor = taskProcessor;
    this.queue = new LinkedBlockingQueue<>(queueSize);
    this.executor = Executors.newFixedThreadPool(workerCount);
    this.workerName = workerName;

    System.out.printf("[%s] %d개의 워커 스레드 시작%n", workerName, workerCount);
    IntStream.range(0, workerCount)
            .forEach(i -> executor.submit(this::processQueue));
  }

  private void processQueue() {
    String threadName = Thread.currentThread().getName();
    System.out.printf("[%s-%s] 워커 스레드 시작됨%n", workerName, threadName);

    while (!Thread.currentThread().isInterrupted()) {
      try {
        T task = queue.take();
        System.out.printf("[%s-%s] 작업 처리 시작: %s%n", workerName, threadName, task);
        taskProcessor.accept(task);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        System.out.printf("[%s-%s] 워커 스레드 종료%n", workerName, threadName);
        break;
      }
    }
  }

  public boolean submitTask(T task) {
    boolean success = queue.offer(task);
    if (success) System.out.printf("[%s] 큐에 작업 추가: %s (현재 큐 크기: %d)%n", workerName, task, queue.size());
    else System.out.printf("[%s] 큐가 가득참! 작업 추가 실패: %s%n", workerName, task);

    return success;
  }

  public void shutdown() {
    System.out.printf("[%s] 종료 시작\n", workerName);
    executor.shutdown();
  }

  public int getQueueSize() {
    return queue.size();
  }
}

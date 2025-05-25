package com.suwan.qworker.producer;

import com.suwan.qworker.model.NumberTask;
import com.suwan.qworker.model.ProducerResult;
import com.suwan.qworker.worker.QueueWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 랜덤 숫자 생성(0 ~ 1000)
 * NumberTask 객체로 포장
 * QueueWorker의 큐에 추가
 */
public class RandomNumberProcessor {
  private final QueueWorker<NumberTask, Integer> queueWorker;
  private final Random random;

  public RandomNumberProcessor(QueueWorker<NumberTask, Integer> queueWorker) {
    this.queueWorker = queueWorker;
    this.random = new Random();
  }

  public CompletableFuture<ProducerResult> produceRandomNumberTasksAsync(int taskCount, int maxNumber) {
    return CompletableFuture.supplyAsync(() -> {
      System.out.println("\n[Producer] 작업 생성 시작!");
      List<CompletableFuture<Integer>> futures = new ArrayList<>();

      for (int i = 0; i < taskCount; i++) {
        int randomNumber = random.nextInt(maxNumber + 1);

        NumberTask task = new NumberTask(i, randomNumber);
        System.out.printf("[Producer] Task %d 생성: count=%d%n", i, randomNumber);

        // 비동기로 작업 제출
        CompletableFuture<Integer> future = queueWorker.submitTaskAsync(task);
        futures.add(future);

        try {
          Thread.sleep(50); // 생산 속도 조절
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }

      return new ProducerResult(result, consumerSum);
    });
  }
}

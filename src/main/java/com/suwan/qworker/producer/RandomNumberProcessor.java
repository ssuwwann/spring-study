package com.suwan.qworker.producer;

import com.suwan.qworker.model.NumberTask;
import com.suwan.qworker.worker.QueueWorker;

import java.util.Random;

/**
 * 랜덤 숫자 생성(0 ~ 1000)
 * NumberTask 객체로 포장
 * QueueWorker의 큐에 추가
 */
public class RandomNumberProcessor {
  private final QueueWorker<NumberTask> queueWorker;
  private final Random random;

  public RandomNumberProcessor(QueueWorker<NumberTask> queueWorker) {
    this.queueWorker = queueWorker;
    this.random = new Random();
  }

  public int produceRandomNumberTasks(int taskCount, int maxNumber) {
    System.out.println("\n========== PRODUCER 시작 ==========");
    System.out.printf("Producer: %d개의 작업을 생성합니다. (0~%d 범위)%n", taskCount, maxNumber);

    int totalSum = 0;

    for (int i = 0; i < taskCount; i++) {
      int randomNumber = random.nextInt(maxNumber + 1); // 0 ~ maxNumber
      totalSum += randomNumber;

      NumberTask task = new NumberTask(i, randomNumber);
      queueWorker.submitTask(task);

      // 생산 속도 조절 (시각적 효과)
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    System.out.printf("Producer: 모든 작업 생성 완료! (생성된 숫자의 합: %d)%n", totalSum);
    System.out.println("========== PRODUCER 종료 ==========\n");

    return totalSum;
  }
}

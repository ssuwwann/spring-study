package com.suwan.qworker;

import com.suwan.qworker.model.NumberTask;
import com.suwan.qworker.producer.RandomNumberProcessor;
import com.suwan.qworker.worker.QueueWorker;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class QworkerApplication {

  @Bean
  ApplicationRunner run() {
    return args -> {
      System.out.println("===== Producer-Consumer 패턴 시작 =====\n");

      // 설정
      final int TASK_COUNT = 20; // 생성할 작업 수
      final int MAX_NUMBER = 1000;   // 랜덤 숫자 범위 (0~1000)
      final int WORKER_COUNT = 4;    // 워커 스레드 수
      final int QUEUE_SIZE = 50;     // 큐 크기

      // 각 스레드별 결과 저장
      ConcurrentHashMap<String, Integer> threadResults = new ConcurrentHashMap<>();
      ConcurrentHashMap<String, Integer> threadTaskCount = new ConcurrentHashMap<>();
      AtomicInteger proccessCount = new AtomicInteger(0);
      CountDownLatch latch = new CountDownLatch(TASK_COUNT);

      // 1. consumer (QueueWorker) 생성
      QueueWorker<NumberTask> queueWorker = new QueueWorker<>(
              "NumberWorker",
              QUEUE_SIZE,
              WORKER_COUNT,
              task -> {
                String threadName = Thread.currentThread().getName();

                // 각 스레드가 처리한 숫자를 누적
                threadResults.merge(threadName, task.number(), (i1, i2) -> i1 + i2);
                threadTaskCount.merge(threadName, 1, Integer::sum);

                int currentProcessed = proccessCount.incrementAndGet();
                System.out.printf("[CONSUMER-%s] 처리 중: %s (진행률: %d/%d)%n",
                        threadName, task, currentProcessed, TASK_COUNT);

                try {
                  Thread.sleep(100);
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                }

                latch.countDown();
              }
      );

      // 2. producer 생성 및 실행
      RandomNumberProcessor producer = new RandomNumberProcessor(queueWorker);
      int producerSum = producer.produceRandomNumberTasks(TASK_COUNT, MAX_NUMBER);

      // 3. 모든 작업 완료 대기
      System.out.println("\n모든 작업이 처리될 때까지 대기 중...");
      latch.await();

      // 4. 결과 집계 및 출력
      System.out.println("\n=============최종 결과=============");

      int totalConsumerSum = 0;
      for (String thread : threadResults.keySet()) {
        Integer threadSum = threadResults.get(thread);
        Integer taskCount = threadTaskCount.get(thread);
        totalConsumerSum += threadSum;

        System.out.printf("[%s] 처리한 작업 수: %d개, 합계: %d%n",
                thread, taskCount, threadSum);
      }

      System.out.println("\n========== 검증 ==========");
      System.out.printf("Producer가 생성한 숫자들의 합: %d%n", producerSum);
      System.out.printf("Consumer들이 처리한 숫자들의 합: %d%n", totalConsumerSum);
      System.out.printf("검증 결과: %s%n",
              producerSum == totalConsumerSum ? "✓ 성공" : "✗ 실패");

      // 5. 종료
      queueWorker.shutdown();
      System.out.println("\n===== Producer-Consumer 패턴 종료 =====");
    };
  }

  public static void main(String[] args) {
    SpringApplication.run(QworkerApplication.class, args);
  }
}
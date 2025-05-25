package com.suwan.qworker;

import com.suwan.qworker.model.ParallelSumTask;
import com.suwan.qworker.model.SumTask;
import com.suwan.qworker.producer.SumQueueWorkerProducer;
import com.suwan.qworker.worker.QueueWorker;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

@SpringBootApplication
public class QworkerApplication {

  @Bean
  ApplicationRunner run() {
    return args -> {
      int randomNumber = new Random().nextInt(1000) + 1;
      int threadCount = 4;

      // thread 처리할 범위 계산
      int chunkSize = randomNumber / threadCount;
      int remainder = randomNumber % threadCount;

      // 결과 저장
      ConcurrentHashMap<Integer, Integer> results = new ConcurrentHashMap<>();
      CountDownLatch latch = new CountDownLatch(threadCount);

      // queue worker 생성
      QueueWorker<ParallelSumTask> queueWorker = new QueueWorker<>(
              threadCount,
              task -> {
                String threadName = Thread.currentThread().getName();

                // 부분 합계 계산
                int sum = IntStream.rangeClosed(task.start(), task.end()).sum();

                System.out.printf("%s: %d부터 %d까지 합계 = %d\n", threadName, task.start(), task.end(), sum);

                // 결과 저장
                results.put(task.id(), sum);
                latch.countDown(); // 카운트를 1 감소시킴
              }
      );

      // 작업 분배 및 생성
      System.out.printf("총 숫자: %d, 스레드당 약 %d개씩 처리%n", randomNumber, chunkSize);
      System.out.println("==================================");

      int currentStart = 1;
      for (int i = 0; i < threadCount; i++) {
        int currentEnd = currentStart + chunkSize - 1;

        // 마지막 스레드는 나머지도 처리
        if (i == threadCount - 1) currentEnd += remainder;

        ParallelSumTask task = new ParallelSumTask(i, currentStart, currentEnd);
        queueWorker.submitTask(task);

        currentStart = currentEnd + 1;
      }

      // 모든 작업 완료 대기, 카운터가 0이 될 때까지 현재 스레드 대기
      latch.await();

      // 최종 결과 합산
      int totalSum = results.values().stream()
              .mapToInt(Integer::intValue)
              .sum();

      System.out.println("==================================");
      System.out.printf("최종 합계: %d%n", totalSum);

      // 검증 (1부터 n까지의 합 공식)
      int expectedSum = randomNumber * (randomNumber + 1) / 2;
      System.out.printf("검증: %d (예상값: %d)%n", totalSum, expectedSum);

      queueWorker.shutdown();
    };
  }

  public static void main(String[] args) {
    SpringApplication.run(QworkerApplication.class, args);
  }
}
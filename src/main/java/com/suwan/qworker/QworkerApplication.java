package com.suwan.qworker;

import com.suwan.qworker.model.NumberTask;
import com.suwan.qworker.model.ProducerResult;
import com.suwan.qworker.producer.RandomNumberProcessor;
import com.suwan.qworker.worker.QueueWorker;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@SpringBootApplication
public class QworkerApplication {

  @Bean
  ApplicationRunner run() {
    return args -> {
      System.out.println("===== CompletableFuture");

      // 1. QueueWorker 생성(Consumer)
      // 1부터 count 합을 계산하는 function
      QueueWorker<NumberTask, Integer> queueWorker = new QueueWorker<>(
              "RandomWorker",
              1_000,
              4,
              task -> {
                // 1부터 task.count() 까지의 합 계산
                int sum = IntStream.rangeClosed(1, task.number()).sum();

                System.out.printf("[Worker-%s] Task %d: 1~%d 합계 = %d%n",
                        Thread.currentThread().getName(), task.taskId(), task.number(), sum);

                try {
                  Thread.sleep(100);
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                }

                return sum;
              }
      );

      RandomNumberProcessor producer = new RandomNumberProcessor(queueWorker);

      // 비동기로 작업 생성 및 처리
      CompletableFuture<ProducerResult> producerFuture = producer.produceRandomNumberTasksAsync(20, 50);// 20개 작업, 0 ~ 50 범위

      // 결과 대기 및 출력
      producerFuture.thenAccept(result -> {
        System.out.println("\n========== 최종 결과 ==========");
        System.out.println("생성된 숫자들: " + result.generatedNumbers());
        System.out.println("처리 결과들: " + result.processedResults());
        System.out.printf("Producer가 생성한 숫자 합: %d%n", result.producerSum());
        System.out.printf("Consumer가 계산한 결과 합: %d%n", result.consumerSum());

        int expectedSum = result.generatedNumbers().stream()
                .mapToInt(n -> n * (n + 1) / 2)
                .sum();

        System.out.printf("예상 결과 합: %d%n", expectedSum);
        System.out.printf("검증: %s%n",
                expectedSum == result.consumerSum() ? "✓ 성공!" : "✗ 실패!");
      }).join();

      queueWorker.shutdown();
      System.out.println("\n===== 프로그램 종료 =====");
    };
  }

  public static void main(String[] args) {
    SpringApplication.run(QworkerApplication.class, args);
  }
}

/*
* 1. QueueWorker 생성 (4개 스레드)
   ↓
2. Producer가 비동기로 20개 작업 생성
   - Task(id=0, count=42) → CompletableFuture<Integer>
   - Task(id=1, count=17) → CompletableFuture<Integer>
   ...
   ↓
3. 각 작업이 QueueWorker에서 처리됨
   - 1~42의 합 = 903
   - 1~17의 합 = 153
   ...
   ↓
4. 모든 Future 완료 대기 (allOf)
   ↓
5. 결과 수집 및 검증
* */
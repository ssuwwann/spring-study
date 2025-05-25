package com.suwan.qworker;

import com.suwan.qworker.model.SumTask;
import com.suwan.qworker.producer.SumQueueWorkerProducer;
import com.suwan.qworker.worker.QueueWorker;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Random;
import java.util.stream.IntStream;

@SpringBootApplication
public class QworkerApplication {

  @Bean
  ApplicationRunner run() {
    return args -> {
      QueueWorker<SumTask> sumTaskQueueWorker = new QueueWorker<>(
              4, // thread 수
              task -> {
                String threadName = Thread.currentThread().getName();

                // 1부터 random까지 더하기
                IntStream.range(0, task.random())
                        .forEach(num -> {
                          System.out.printf("%s: %d\n", threadName, num);

                          try {
                            Thread.sleep(50);
                          } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                          }
                        });

                // 합계 출력
                int sum = IntStream.rangeClosed(1, task.random()).sum();
                System.out.printf("%s: 작업 %d 완료! (합계: %d\n)", threadName, task.id(), sum);
              }
      );

      // producer 생성
      int randomNumber = new Random().nextInt(1000) + 1;

      SumQueueWorkerProducer producer = new SumQueueWorkerProducer(sumTaskQueueWorker, randomNumber);
      producer.producerRandomSumTasks(4);

      Thread.sleep(3000);
      sumTaskQueueWorker.shutdown();
    };
  }

  public static void main(String[] args) {
    SpringApplication.run(QworkerApplication.class, args);
  }
}
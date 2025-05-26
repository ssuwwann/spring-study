package com.suwan.qworker;

import com.suwan.qworker.producer.NumberProducer;
import com.suwan.qworker.worker.QueueWorker;
import org.apache.hc.core5.util.Timeout;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QworkerApplicationTests {

  @Test
  void 프로듀서_컨슈머_테스트() throws Exception{
    // given
    LinkedBlockingDeque<Integer> queue = new LinkedBlockingDeque<>(50);
    AtomicInteger sum = new AtomicInteger(0);
    NumberProducer producer = new NumberProducer(queue, 1000);

    // when
    CompletableFuture<Integer> producerFuture = producer.produce();

    CompletableFuture<Integer>[] consumerFutures = IntStream.range(0, 4)
            .mapToObj(i -> new QueueWorker(queue, sum).consume())
            .toArray(CompletableFuture[]::new);

    CompletableFuture.allOf(consumerFutures).get(5, TimeUnit.SECONDS);

    // then
    assertTrue(queue.isEmpty(),"큐가 비어야됨");

    Integer producedCount = producerFuture.get();
    assertNotNull(producedCount);

    System.out.println("테스트 완료 - 생성: " + producedCount + ", 합계: " + sum.get());
    System.out.println("Test complete - Generated: " + producedCount + ", Sum: " + sum.get());
  }

  @Test
  void 예외처리(){
    // completableFuture 예외처리
    LinkedBlockingDeque<Integer> queue = new LinkedBlockingDeque<>(1);
    AtomicInteger sum = new AtomicInteger(0);

    CompletableFuture<Integer> future = new QueueWorker(queue, sum).consume()
            .exceptionally(ex -> {
              System.out.println("예외 발생: " + ex.getMessage());
              System.out.println("Exception occurred: " + ex.getMessage());
              return -1;
            });

    assertThrows(TimeoutException.class, () -> future.get(1L, TimeUnit.SECONDS));
  }

}

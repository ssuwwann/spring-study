package com.suwan.qworker.worker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * BlockingQueue로 작업 관리
 * 4개의 워커 스레드가 동시에 작업 처리
 * 각 스레드가 처리한 결과를 저장
 *
 */
public class QueueWorker {
  private final BlockingQueue<Integer> queue;
  private final AtomicInteger sharedSum;

  public QueueWorker(BlockingQueue<Integer> queue, AtomicInteger sharedSum) {
    this.queue = queue;
    this.sharedSum = sharedSum;
  }

  // Completable를 반환하는 메서드
  public CompletableFuture<Integer> consume(){
    return CompletableFuture.supplyAsync(()->{
      String threadName = Thread.currentThread().getName();
      int localSum = 0;

      try{
        while (true) {
          //Integer value = queue.poll(1L, TimeUnit.SECONDS);
          Integer value = queue.take();
          if (value == -1) {
            System.out.println(threadName + " 종료!");
            System.out.println(threadName + " **finished**!");
            break;
          }

          localSum += value;
          int totalSum = sharedSum.addAndGet(value);
          System.out.printf("%s: %d를 더함, 전체 합계: %d%n", threadName, value, totalSum);
          System.out.printf("%s: Added %d, total sum: %d%n", threadName, value, totalSum);
        }
      }catch(InterruptedException e){
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      }

      // 컨슈머가 처리한 합계 반환
      return localSum;
    });
  }
}

package com.suwan.qworker.producer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * 랜덤 숫자 생성(0 ~ 1000)
 * NumberTask 객체로 포장
 * QueueWorker의 큐에 추가
 */
public class NumberProducer {
  private final BlockingQueue<Integer> queue;
  private final int maxValue;

  public NumberProducer(BlockingQueue<Integer> queue, int maxValue) {
    this.queue = queue;
    this.maxValue = maxValue;
  }

  // CompletableFuture 반환 메서드
  public CompletableFuture<Integer> produce() {
    return CompletableFuture.supplyAsync(()->{ // supplyAsync는 비동기(백그라운드 스레드에서 실행되는 작업)을 쉽게 시작하게 해준다.
      int count = ThreadLocalRandom.current().nextInt(1, maxValue+1);
      System.out.println("프로듀서가 " + count + "개 생성 예정");
      System.out.println("Producer will generate " + count + " numbers");

      try{
        IntStream.rangeClosed(1,count).forEach(i->{
          try{
            queue.offer(i, 1L, TimeUnit.SECONDS);
          }catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
          }
        });
      }catch(Exception e){
        Thread.currentThread().interrupt();
      }

      // 종료 신호 전송
      IntStream.range(0, 4).forEach(i->{
        try{
          queue.offer(-1, 1L, TimeUnit.SECONDS);
        }catch(InterruptedException e){
          Thread.currentThread().interrupt();
        }
      });

      return count;
    });
  }
}

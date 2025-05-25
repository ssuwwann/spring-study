### 과제 정의 (20250523)
> queue worker

- 큐워커를 상속받은 간단한 서버를 만든다 ?
- 테스트 코드를 작성하는데 다음과 같은 조건이 있다.
  1. 사이즈가 4인 스레드풀을 만든다.  
  2. 0 ~ 1000까지의 숫자를 랜덤으로 받는다.
  3. 큐에서 랜덤으로 받은 값만큼 더해 스레드 이름과 함께 출력한다.
  4. 가능하면 http 통신으로 응답받은 데이터를 가지고도 해보자,

- 다음주 월요일까지 진행하며 못할 시 할복한다.
---

### Work Queue - 비동기 작업 큐
- amqp(카프카, 래빗엠큐 등등)? 을 이용해 메시지를 큐에 담고 사용하는걸 말하는거 같다.
- 하지만 여기선 BlockingQueue를 사용하여 간단하게 producer(작업넣기) Consumer(worker, 작업처리)를 구현해보겠다.

> BlockingQueue란? https://ywoosang.tistory.com/31
- 스레드 세이프티한 큐를 구현하기 위한 인터페이스로 java 5 concurrent 패키지에 추가되었다. 
- 큐의 기본 작업에 블로킹을 추가해 큐가 가득 찼을 때 항목을 추가하려는 스레드나, 큐가 비었을 때 항목을 제거하려는 스레드를 대기 상태로 만든다. 
- 블로킹은 특정 조건이 충족될 때까지 쓰레드를 일시 중지시키는 것으로, 연산이 완료될 때까지 쓰레드를 대기 상태로 만든다. 
```text
- 큐가 비어있으면: 요소를 꺼내려는(thread가 take()를 호출) 스레드는 큐에 요소가 추가될 때까지 대기한다. 
- 큐가 가득 차면: 요소를 추가하려는 (thread가 put()을 호출) 스레드는 큐에 여유 공간이 생길 때까지 대기한다. 
```
- Blocking queue는 멀티스레드 환경에서 데이터를 생성해 큐에 추가하는 producer와 큐에서 데이터를 가져와 처리하는 consumer가 비동기적으로 실행되면서도, 동기화 문제 없이 데이터를 공유하기 위한 목적으로 주로 사용된다.
- Executors의 고정된 스레드풀의 개수를 반환하는 newFixedThreadPool이 ThreadPoolExecutor 인스턴스를 생성해 반환하는데, ThreadPoolExecutor에서 BlockingQueue를 파라미터로 받아 이용하고 있다. 
- 기본적으로 LinkedBlockingQueue가 사용된다. 

> Queue와의 차이점은 뭔가?

1. Thread-Safe
   - 일반적으로 queue를 구현하는 linkedlist나 priorityqueue 등은 여러 스레드에서 동시에 접근할 경우 데이터의 일관성이 깨질 수 있다. 
   - 즉 한 스레드가 poll()를 호출하여 요소를 제거하는 도중에, 다른 스레드가 offer()로 요소를 추가하면 데이터 손실, 중복 추가, 동시성 문제가 발생할 수 있다.
   - 반면 blockingqueue는 내부적으로 동기화 매커니즘을 제공해 여러 개의 스레드가 동시에 put()과 take()를 호출해도 데이터가 안전하게 처리된다.

2. 블로킹 기능(put/take 대기)
   - 일반 queue는 비어있거나 가득 차더라도 즉시 반환한다. 예를 들어 큐가 비어있을때 poll()를 호출하면 null를 반환하고, 큐가 가득 차면  offer()는 false를 반환한다. 
   - 반면 blockingqueue는 큐의 상태에 따라 블로킹(스레드 대기상태 blocking으로 전환) 한다. 
   - 이러한 특징 때문에 producer-consumer 패턴을 구현할 때, consumer는 take()를 호출해 데이터가 들어오 때까지 대기하고, producer는 put()을 호출해 큐가 가득 찼을 때 대기하는 방식으로 사용된다. 

3. 대기시간 설정 가능
   - 일반 queue는 블로킹 기능이 없으므로 대기 시간을 설정하는 기능을 제공하지 않는다. 
   - 반면 blockingqueue는 요소를 추가하거나 제거할 때 특정 시간 동안만 대기하도록 설정할 수 있다. 
     - offer(E e, long timeout, TimeUnit unit): 삽입시 큐가 가득 찼을 경우 지정된 시간 동안 대기하고, 시간이 지나도 공간이 생기지 않으면 false 반환
     - poll(long timeout, TimeUnit unit): 요소를 제거할 때 큐가 비어있을 경우 지정된 시간 동안 대기하고, 시간이 지나도 데이터가 들어오지 않으면 null 반환
   - 어느 정도 기다리고 처리할 수 있다. 

> Blocking Queue 구현체
- 다양하게 존재한다. 하지만 잘 알려진 ArrayBlockingQueue, LinkedBlockingQueue 와 지연기능이 있는 DelayQueue만 정리해보자

1. ArrayBlockingQueue: 크기가 고정된 배열 기반의 blocking queue이다.
   - 크기가 고정된 배열(Array) 기반으로 구현된다. 
   - fifo 방식으로 동작한다. 
     - head는 가장 오래된 요소, tail은 가장 최근에 들어온 요소가 존재한다. 
   - 생성 시 반드시 큐의 최대 크기를 지정한다. 
   - put과 take가 하나의 reentrantlock을 공유하여 동기화된다.
     - 같은 락(단일 락 -> shared lock)을 사용하므로 락 경합이 발생할 수 있다. => 동시성이 중요하고 대량의 생산자/소비자가 많다면 락 경합이 심해진다.
   - 생성자에서 객체를 생성할 때 무조건 초기용량(capacity)를 지장해야 하며, 설정한 용량은 변경 불가!
   - fair 옵션은 starvation(기아, 굶주림) 방지를 위한 공정성 정책이다. 
     - 내부적으로 retrantlock을 사용하여 put과 take시 같은 락을 공유하므로 경합이 발생하여 starvation이 일어날 가능성이 있다.
     - 따라서 생성자로 넘어온 fair 옵션을 통해 RetentionLock의 fair 여부를 설정할 수 있다. 
     - 기본값은 false이며 ture로 설저할 경우 가장 오래 기다린 스레드가 먼저 lock을 잡는다. 
       - false인 경우 jvm 스케줄러에 의해 lock은 랜덤하게 할당된다(FIFO 보장 x)
       - put이나 take을 기다리는 스레드가 있더라도 어떤 스레드가 먼저 lock을 얻을지 예측할 수 없다.
       - true인 경우 오래 기다린 스레드가 먼저 lock을 획득하여 FIFO를 보장한다. 
       - 우선순위가 높은 작업이 계속 실행되어 낮은 우선순위 작업이 실행되지 않는 starvation 문제를 방지할 수 있다. ? => jvm이 랜덤으로 lock을 주는데 우선 순위가 중요해? ...
       - fifo 순서를 강제하기 위해 추가적인 스레드 관리 비용이 발생하여 전체적인 처리량(throughput)이 감소할 수 있다.
     - array를 내부적으로 사용하지만 고정된 크기 내에서 putindex와 takeindex를 이용해 삽입, 삭제를 하기 때문에 배열 특유의 순서 이동이 발생하지 않아 오버헤드가 없다.
     - 삭제시 삭제한 원소는 null로 처리하고 takeindex로 배열을 관리한다. 
2. LinkedBlockingQueue
   - 연결 리스트 기반의 BlockingQueue다.
   - fifo 방식으로 동작한다. 
   - 최대 크기를 지정할 수 있지만 기본적으로 integer max 까지 가능하다. 
   - put, take가 각각 별도의 ReentrantLock을 사용해 동기화를 구현한다.(put lock, take lock)
   - 락 경합이 ArrayBlockingQueue보다 줄어들어 높은 동시성이 필요한 환경에서 처리량이 향상될 수 있다. 
   - 생산자와 소비자가 동시에 실행되면서 대량의 데이터를 처리해야 되는 경우
   - 고정된 크기의 제한이 필요 없거나, 동적으로 크기를 확장해야 하는 경우
   - ExecutorService의 기본 작업 큐로 사용된다. 
3. DelayQueue
   - delayed 요소를 지정하며, 설정된 지연 시간이 만료되어야 꺼낼 수 있는 블로킹 큐이다. 
   - 큐에 저장될 요소는 delayed 인터페이스를 구현해야 한다. 
   - 내부적으로 priorityqueue를 사용해 지연 시간에 따라 요소들을 자동 정렬한다. 
   - 큐에서 즉시 가져오는 것이 불가능하며 지정된 시간까지 대기해야한다. 
   - 일정 시간이 지난 후에 실행해야 하는 작업을 관리할 때, 예약된 작업의 실행 지연이 필요할때 사용한다. 
package ru.rtec;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class Program {

  private static AtomicInteger ATOMIC_INT = new AtomicInteger(0);

  public static void main(String[] args) {
    Program test = new Program();
    try {
      test.start(1, 1);
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void start(int thread, int runs) throws InterruptedException, ExecutionException {
    ExecutorService service = Executors.newFixedThreadPool(thread);
    double avgRuns = 0;
    for (int i = 0; i < runs; i++) {
      Set<Callable<Long>> callables = new HashSet<>();
      for (int j = 0; j < thread; j++) {
        callables.add(new Worker());
      }

      List<Future<Long>> futureList = service.invokeAll(callables);

      double commonTime = 0;
      for (int k = 0; k < futureList.size(); k++) {
        commonTime += futureList.get(k).get();
      }
      double averageCurrentTime = commonTime / thread;
      double averageCurrentOps = TimeUnit.SECONDS.toNanos(1000 * 100 * 100) / averageCurrentTime;
      avgRuns += averageCurrentOps;
      System.out.println(
          String.format("За один прогон на %d поток -е(-ах) имеем -> Ops/second %f", thread, averageCurrentOps));
    }

    System.out.println(String.format("Средний результат после %d прогонов -> Ops/second %f", runs, avgRuns / runs));

    service.shutdown();
  }

  class Worker implements Callable<Long> {

    @Override
    public Long call() throws Exception {
      long start = System.nanoTime();
      for (int i = 0; i < 1000 * 100 * 100; i++) {
        ATOMIC_INT.incrementAndGet();
      }
      long delta = System.nanoTime() - start;
      return delta;
    }
  }

}

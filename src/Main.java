import net.neevek.lib.java.promise.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by neevek on 04/12/2016.
 */
public class Main {
  private static ScheduledExecutorService scheduledExecutorService;

  public static void main(String[] args) {
    scheduledExecutorService = Executors.newScheduledThreadPool(3);

    test0();
  }


  private static void test0() {
    System.out.println(">>>>>>>>>>>> start test0 <<<<<<<<<<<<<");

    Promise.with(new PromiseTask<String>() {
      @Override
      public void run(final Resolver<String> resolver) {
        scheduledExecutorService.schedule(new Runnable() {
          @Override
          public void run() {
            resolver.resolve("P1");
          }
        }, 1, TimeUnit.SECONDS);
      }
    }).then(new OnFulfilledCallback<String>() {
      @Override
      public String onFulfilled(String result) {
        System.out.println("result: " + result);
        return null;
      }
    }).then(new OnFulfilledCallback<String>() {
      @Override
      public String onFulfilled(String result) {
        System.out.println(">>>>>>>>>>>> start test1 <<<<<<<<<<<<<");
        test1();
        return null;
      }
    });
  }

  private static void test1() {
    Promise.with(new PromiseTask<Object>() {
      @Override
      public void run(final Resolver resolver) {
        resolver.resolve("P1");
      }
    }).then(new OnFulfilledCallback<Object>() {
      @Override
      public Object onFulfilled(final Object result) {
        return Promise.with(new PromiseTask() {
          @Override
          public void run(final Resolver resolver) {
            scheduledExecutorService.schedule(new Runnable() {
              @Override
              public void run() {
                resolver.resolve(result + "-P2");
              }
            }, 1, TimeUnit.SECONDS);
          }
        });
      }
    }).then(new OnFulfilledCallback<Object>() {
      @Override
      public Object onFulfilled(Object result) {
        System.out.println("after delayed: " + result);
        return null;
      }
    }).then(new OnFulfilledCallback<Object>() {
      @Override
      public String onFulfilled(Object result) {
        System.out.println(">>>>>>>>>>>> start test2 <<<<<<<<<<<<<");
        test2();
        return null;
      }
    });
  }

  private static void test2() {
    Promise.with(new PromiseTask<String>() {
      @Override
      public void run(Resolver<String> resolver) {
//        resolver.reject("ERROR");
        throw new RuntimeException("test throwing exception");
      }
    }).then(new OnFulfilledCallback<String>() {
      @Override
      public String onFulfilled(String result) {
        // will not be called
        return result;
      }
    }, new OnRejectedCallback() {
      @Override
      public Object onRejected(Object result) {
        System.out.println("caught error: " + result);
        return "error already caught";
      }
    }).onFail(new OnRejectedCallback() {
      @Override
      public Object onRejected(Object result) {
        // will not be called, because the 'rejection' was already caught above
        return result;
      }
    }).then(new OnFulfilledCallback<String>() {
      @Override
      public String onFulfilled(String result) {
        System.out.println("result: " + result);
        return null;
      }
    }).then(new OnFulfilledCallback<String>() {
      @Override
      public String onFulfilled(String result) {
        System.out.println(">>>>>>>>>>>> start test3 <<<<<<<<<<<<<");
        test3();
        return null;
      }
    });
  }

  private static void test3() {
    System.out.println("start Promise.all()");
    Promise p1 = Promise.with(new PromiseTask() {
      @Override
      public void run(final Resolver resolver) {
        scheduledExecutorService.schedule(new Runnable() {
          @Override
          public void run() {
            resolver.resolve("P1(delayed for 1 second)");
          }
        }, 1, TimeUnit.SECONDS);
      }
    });
    Promise p2 = Promise.with(new PromiseTask() {
      @Override
      public void run(final Resolver resolver) {
        scheduledExecutorService.schedule(new Runnable() {
          @Override
          public void run() {
            resolver.resolve("P1(delayed for 2 seconds)");
          }
        }, 2, TimeUnit.SECONDS);
      }
    });
    Promise p3 = Promise.with(new PromiseTask() {
      @Override
      public void run(final Resolver resolver) {
        throw new RuntimeException("test throwing exception");
      }
    });
    Promise.all(p1, p2, p3, Promise.with(null), Promise.with(123), "string literal").then(new OnFulfilledCallback<Object[]>() {
      @Override
      public Object[] onFulfilled(Object[] result) {
        for (int i = 0; i < result.length; ++i) {
          System.out.printf("resolved object(%d): %s\n", i, result[i]);
        }
        return null;
      }
    });
  }
}

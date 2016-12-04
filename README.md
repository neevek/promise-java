promise-java
============

**promise-java** is a `Promise` implementation for Java, it **flattens asynchronous code**, yet, it fully supports **concurrency**.

There is also an Objective-C implementation: [promise-objc](https://github.com/neevek/promise-objc)

Features
========

**promise-java** offers the following APIs:

* Promise.with(object).then(OnFulfilledCallback, OnRejectedCallback)
* Promise.with(object).then(OnFulfilledCallback).onFail(OnRejectedCallback)
* Promise.all(varargs).then(OnFulfilledCallback);

object can be `PromiseTask` or any other kind of objects, see the examples below.

Usage
=====

```java
Promise.with(new PromiseTask<String>() {
  @Override
  public void run(final Resolver<String> resolver) {
    // intentionally delay to asynchronize the 'resolve'
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
});
```

```java
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
        // intentionally delay to asynchronize the 'resolve'
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
});
```

```java
Promise.with(new PromiseTask<String>() {
  @Override
  public void run(Resolver<String> resolver) {
    // resolver.reject("ERROR");
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
});
```

```java
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

// there are 6 objects here, their resolved results will be joined in here
Promise.all(p1, p2, p3, Promise.with(null), Promise.with(123), "string literal").then(new OnFulfilledCallback<Object[]>() {
  @Override
  public Object[] onFulfilled(Object[] result) {
    for (int i = 0; i < result.length; ++i) {
      System.out.printf("resolved object(%d): %s\n", i, result[i]);
    }
    return null;
  }
});
```

MIT License
=================
```
Copyright (c) 2016 neevek <i@neevek.net>
See the file license.txt for copying permission.
```

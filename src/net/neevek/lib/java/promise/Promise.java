package net.neevek.lib.java.promise;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by neevek on 03/12/2016.
 */
public class Promise<ResolvedObject> {
  private static ExecutorService singleThreadExecutor;

  private Object settledResult;
  private State state = State.PENDING;
  private ThenCallbackWrapper thenCallbackWrapper;
  private ThenCallbackWrapper lastThenCallbackWrapper;

  private enum State {
    PENDING,
    FULFILLED,
    REJECTED,
    ERROR_CAUGHT
  }

  public static <ResolveObject> Promise<ResolveObject> with(PromiseTask<ResolveObject> task) {
    return new Promise<>(task);
  }

  public static <ResolveObject> Promise<ResolveObject> with(ResolveObject object) {
    if (object instanceof Promise) {
      return (Promise) object;
    }
    return new Promise<>(object);
  }

  public static Promise<Object[]> all(final Object ...objects) {
    if (objects.length == 0) {
      throw new IllegalArgumentException("object cannot be empty");
    }

    return Promise.with(new PromiseTask<Object[]>() {
      @Override
      public void run(final Resolver<Object[]> resolver) {
        final Object resultArray[] = new Object[objects.length];
        final AtomicInteger count = new AtomicInteger(0);
        final ResolveHelper resolveHelper = new ResolveHelper() {
          @Override
          public void resolve(int index, Object result) {
            resultArray[index] = result;
            if (count.incrementAndGet() == objects.length) {
              resolver.resolve(resultArray);
            }
          }
        };

        for (int i = 0; i < objects.length; ++i) {
          Object object = objects[i];
          if (object instanceof Promise) {
            final int index = i;
            ((Promise) object).then(new OnFulfilledCallback() {
              @Override
              public Object onFulfilled(Object result) {
                resolveHelper.resolve(index, result);
                return result;
              }
            }, new OnRejectedCallback() {
              @Override
              public Object onRejected(Object result) {
                resolveHelper.resolve(index, result);
                return result;
              }
            });
          } else {
            resolveHelper.resolve(i, object);
          }
        }
      }
    });
  }

  private Promise(final Object object) {
    if (object instanceof PromiseTask) {
      getSingleThreadExecutor().execute(new Runnable() {
        @Override
        public void run() {
          Resolver<ResolvedObject> resolver = new Resolver<ResolvedObject>() {
            @Override
            public void resolve(ResolvedObject result) {
              settleResult(result, State.FULFILLED);
            }

            @Override
            public void reject(Object result) {
              settleResult(result, State.REJECTED);
            }
          };

          try {
            ((PromiseTask<ResolvedObject>)object).run(resolver);
          } catch (Exception e) {
            settleResult(e, State.REJECTED);
          }
        }
      });

    } else {
      getSingleThreadExecutor().execute(new Runnable() {
        @Override
        public void run() {
          settleResult(object, State.FULFILLED);
        }
      });
    }
  }

  private void settleResult(Object result, State state) {
    if (this.state == State.PENDING) {
      this.state = state;
      this.settledResult = result;
      if (thenCallbackWrapper != null) {
        thenCallbackWrapper.thenCallback.run();
      }
    }
  }

  private void feedCallbacksWithResult(OnFulfilledCallback<ResolvedObject> onFulfilledCallback,
                                       OnRejectedCallback onRejectedCallback) {
    if (settledResult instanceof Promise) {
      ((Promise) settledResult).then(new OnFulfilledCallback<ResolvedObject>() {
        @Override
        public ResolvedObject onFulfilled(ResolvedObject result) {
          settledResult = result;
          if (thenCallbackWrapper != null) {
            thenCallbackWrapper.thenCallback.run();
          }
          return result;
        }
      }, new OnRejectedCallback() {
        @Override
        public Object onRejected(Object result) {
          settledResult = result;
          if (thenCallbackWrapper != null) {
            thenCallbackWrapper.thenCallback.run();
          }
          return result;
        }
      });
    } else {
      try {
        if (state == State.REJECTED) {
          if (onRejectedCallback != null) {
            settledResult = onRejectedCallback.onRejected(settledResult);
            state = State.ERROR_CAUGHT;
          }
        } else {
          if (onFulfilledCallback != null) {
            settledResult = onFulfilledCallback.onFulfilled((ResolvedObject) settledResult);
          }
        }
      } catch (Exception e) {
        settledResult = e;
      }

      if (thenCallbackWrapper.next != null) {
        thenCallbackWrapper = thenCallbackWrapper.next;
        thenCallbackWrapper.thenCallback.run();
      } else {
        thenCallbackWrapper = null;
        lastThenCallbackWrapper = null;
      }
    }
  }

  public Promise<ResolvedObject> then(final OnFulfilledCallback<ResolvedObject> onFulfilledCallback,
                                      final OnRejectedCallback onRejectedCallback) {
    getSingleThreadExecutor().execute(new Runnable() {
      @Override
      public void run() {
        ThenCallbackWrapper wrapper = new ThenCallbackWrapper(new Runnable() {
          @Override
          public void run() {
            feedCallbacksWithResult(onFulfilledCallback, onRejectedCallback);
          }
        });
        if (thenCallbackWrapper == null) {
          // we need to store the wrapper in 'this.thenCallbackWrapper'
          // even if 'state != State.PENDING', because the current promise
          // need this to chain later 'then' calls.
          thenCallbackWrapper = wrapper;
          lastThenCallbackWrapper = wrapper;

          if (state != State.PENDING) {
            feedCallbacksWithResult(onFulfilledCallback, onRejectedCallback);
          }
        } else {
          lastThenCallbackWrapper.next = wrapper;
          lastThenCallbackWrapper = wrapper;
        }
      }
    });
    return this;
  }

  public Promise<ResolvedObject> then(OnFulfilledCallback<ResolvedObject> onFulfilledCallback) {
    return then(onFulfilledCallback, null);
  }

  public Promise<ResolvedObject> onFail(OnRejectedCallback onRejectedCallback) {
    return then(null, onRejectedCallback);
  }

  private static ExecutorService getSingleThreadExecutor() {
    if (singleThreadExecutor == null) {
      synchronized (Promise.class) {
        if (singleThreadExecutor == null) {
          singleThreadExecutor = Executors.newSingleThreadExecutor(
              new ThreadFactory() {
                public Thread newThread(Runnable r) {
                  Thread t = new Thread(null, r, "net.neevek.lib.java.promise", 0);
                  if (t.isDaemon()) {
                    t.setDaemon(false);
                  }
                  t.setPriority(Thread.NORM_PRIORITY);
                  return t;
                }
              }
          );
        }
      }
    }
    return singleThreadExecutor;
  }
}


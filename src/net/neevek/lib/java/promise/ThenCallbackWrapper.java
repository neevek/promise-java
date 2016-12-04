package net.neevek.lib.java.promise;

/**
 * Created by neevek on 03/12/2016.
 */
class ThenCallbackWrapper {
  public Runnable thenCallback;
  public ThenCallbackWrapper next;

  public ThenCallbackWrapper(Runnable thenCallback) {
    this.thenCallback = thenCallback;
  }
}

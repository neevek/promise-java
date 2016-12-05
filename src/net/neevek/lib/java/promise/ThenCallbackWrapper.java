package net.neevek.lib.java.promise;

/**
 * https://github.com/neevek/promise-java
 * Author: neevek <i@neevek.net>
 * Date: 2016-12-03
 * © 2016
 */
class ThenCallbackWrapper {
  public Runnable thenCallback;
  public ThenCallbackWrapper next;

  public ThenCallbackWrapper(Runnable thenCallback) {
    this.thenCallback = thenCallback;
  }
}

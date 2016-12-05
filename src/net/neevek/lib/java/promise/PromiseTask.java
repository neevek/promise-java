package net.neevek.lib.java.promise;

/**
 * https://github.com/neevek/promise-java
 * Author: neevek <i@neevek.net>
 * Date: 2016-12-03
 * © 2016
 */
public interface PromiseTask<ResolvedObject> {
  void run(Resolver<ResolvedObject> resolver);
}

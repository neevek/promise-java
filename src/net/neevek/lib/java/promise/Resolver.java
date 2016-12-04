package net.neevek.lib.java.promise;

/**
 * Created by neevek on 03/12/2016.
 */
public interface Resolver<ResolvedObject> {
  void resolve(ResolvedObject result);
  void reject(Object result);
}

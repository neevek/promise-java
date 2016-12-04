package net.neevek.lib.java.promise;

/**
 * Created by neevek on 03/12/2016.
 */
public interface OnFulfilledCallback<ResolvedObject> {
  ResolvedObject onFulfilled(ResolvedObject result);
}

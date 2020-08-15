/**
 * CustomCobbleGen By @author Philip Flyvholm
 * Response.java
 */
package me.phil14052.CustomCobbleGen.Utils;

/**
 * @author Philip
 *
 */
public class Response<T> {
	private final T result;
	private boolean isError = false;
	
	public Response(final T result, boolean isError) {
	    this.result = result;
	  }

	public T getResult() {
		return result;
	}

	public boolean isError() {
		return isError;
	}
	
}

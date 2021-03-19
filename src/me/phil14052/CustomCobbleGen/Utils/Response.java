
package me.phil14052.CustomCobbleGen.Utils;

/**
 * CustomCobbleGen By @author Philip Flyvholm
 * Response.java
 */
public class Response<T> {
	private final T result;
	private final boolean isError;

	public Response(final T result, boolean isError) {
	    this.result = result;
	    this.isError = isError;
	  }

	public T getResult() {
		return result;
	}

	public boolean isError() {
		return isError;
	}
	
}

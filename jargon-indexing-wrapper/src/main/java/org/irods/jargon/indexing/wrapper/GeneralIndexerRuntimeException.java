/**
 * 
 */
package org.irods.jargon.indexing.wrapper;

/**
 * Unchecked runtime exception in indexer operation
 * 
 * @author Mike Conway - DICE
 *
 */
public class GeneralIndexerRuntimeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4193706724506073688L;

	/**
	 * 
	 */
	public GeneralIndexerRuntimeException() {
	}

	/**
	 * @param message
	 */
	public GeneralIndexerRuntimeException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public GeneralIndexerRuntimeException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public GeneralIndexerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	
}

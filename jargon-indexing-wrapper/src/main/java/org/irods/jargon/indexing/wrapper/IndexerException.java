/**
 * 
 */
package org.irods.jargon.indexing.wrapper;

/**
 * General exception in indexer
 * 
 * @author Mike Conway - DICE
 *
 */
public class IndexerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8347397739084417882L;

	/**
	 * 
	 */
	public IndexerException() {
	}

	/**
	 * @param message
	 */
	public IndexerException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public IndexerException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public IndexerException(String message, Throwable cause) {
		super(message, cause);
	}


}

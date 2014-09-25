/**
 * 
 */
package org.irods.jargon.indexing.wrapper.event;

/**
 * Events relating to file add or delete
 * @author Mike Conway - DICE
 *
 */
public class FileEvent extends AbstractMessageEvent {

	private String irodsAbsolutePath;
	
	/**
	 * 
	 */
	public FileEvent() {
	}

	@Override
	public String getIrodsAbsolutePath() {
		return irodsAbsolutePath;
	}

	@Override
	public void setIrodsAbsolutePath(String irodsAbsolutePath) {
		this.irodsAbsolutePath = irodsAbsolutePath;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FileEvent [");
		if (irodsAbsolutePath != null) {
			builder.append("irodsAbsolutePath=");
			builder.append(irodsAbsolutePath);
		}
		builder.append("]");
		return builder.toString();
	}

}

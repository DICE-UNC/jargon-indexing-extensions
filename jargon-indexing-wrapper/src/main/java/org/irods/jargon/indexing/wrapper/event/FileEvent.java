/**
 * 
 */
package org.irods.jargon.indexing.wrapper.event;

/**
 * Events relating to file add or delete
 * 
 * @author Mike Conway - DICE
 * 
 */
public class FileEvent extends AbstractMessageEvent {

	private String irodsAbsolutePath;
	private long dataSize = 0L;

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

	/**
	 * @return the dataSize
	 */
	public long getDataSize() {
		return dataSize;
	}

	/**
	 * @param dataSize
	 *            the dataSize to set
	 */
	public void setDataSize(long dataSize) {
		this.dataSize = dataSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FileEvent [");
		if (irodsAbsolutePath != null) {
			builder.append("irodsAbsolutePath=");
			builder.append(irodsAbsolutePath);
			builder.append(", ");
		}
		builder.append("dataSize=");
		builder.append(dataSize);
		builder.append("]");
		return builder.toString();
	}

}

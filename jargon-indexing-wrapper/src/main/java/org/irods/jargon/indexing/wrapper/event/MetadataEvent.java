/**
 * 
 */
package org.irods.jargon.indexing.wrapper.event;

import org.irods.jargon.core.pub.domain.AvuData;

/**
 * Event for operation on an AVU to a collection or data object
 * 
 * @author Mike Conway - DICE
 *
 */
public class MetadataEvent extends AbstractMessageEvent {

	/**
	 * Avu attribute that was added
	 */
	private AvuData avuData;

	public AvuData getAvuData() {
		return avuData;
	}

	public void setAvuData(AvuData avuData) {
		this.avuData = avuData;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MetadataEvent [");
		if (avuData != null) {
			builder.append("avuData=").append(avuData).append(", ");
		}
		if (super.toString() != null) {
			builder.append("toString()=").append(super.toString());
		}
		builder.append("]");
		return builder.toString();
	}

}

/**
 * 
 */
package org.irods.jargon.indexing.wrapper.event;

import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.indexing.wrapper.IndexingConstants;

/**
 * Abstract superclass for events that occur in the indexing framework
 * translated into simple semantics
 * 
 * @author Mike Conway - DICE
 *
 */
public abstract class AbstractMessageEvent {

	/**
	 * Generic action (add, delete, modify)
	 */
	private IndexingConstants.actionsEnum actionsEnum;

	/**
	 * Absolute path
	 */
	private String irodsAbsolutePath = "";
	private CollectionAndDataObjectListingEntry.ObjectType objectType;

	public IndexingConstants.actionsEnum getActionsEnum() {
		return actionsEnum;
	}

	public void setActionsEnum(IndexingConstants.actionsEnum actionsEnum) {
		this.actionsEnum = actionsEnum;
	}

	public String getIrodsAbsolutePath() {
		return irodsAbsolutePath;
	}

	public void setIrodsAbsolutePath(String irodsAbsolutePath) {
		this.irodsAbsolutePath = irodsAbsolutePath;
	}

	public CollectionAndDataObjectListingEntry.ObjectType getObjectType() {
		return objectType;
	}

	public void setObjectType(
			CollectionAndDataObjectListingEntry.ObjectType objectType) {
		this.objectType = objectType;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AbstractMessageEvent [");
		if (actionsEnum != null) {
			builder.append("actionsEnum=").append(actionsEnum).append(", ");
		}
		if (irodsAbsolutePath != null) {
			builder.append("irodsAbsolutePath=").append(irodsAbsolutePath)
					.append(", ");
		}
		if (objectType != null) {
			builder.append("objectType=").append(objectType);
		}
		builder.append("]");
		return builder.toString();
	}

}

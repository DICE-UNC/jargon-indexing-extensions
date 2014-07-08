package org.irods.jargon.indexing.hive.metadata.utils;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.hive.external.indexer.HiveIndexerException;

import com.hp.hpl.jena.ontology.OntModel;

/**
 * Interface for a service to prepare an Jena model based on iRODS vocabulary
 * data, HIVE vocabularies, and potentially existing HIVE metadata in iRODS
 * 
 * @author Mike Conway - DICE
 * 
 */
public interface HiveMetadataIndexer {

	/**
	 * Initialize an index triple store and do a batch indexing of iRODS
	 * 
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory}
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 * @return <code>OntModel</code> initialized and open
	 * @throws HiveIndexerException
	 */
	public abstract OntModel initializeAndBatchIndexOntologyModel(
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) throws HiveIndexerException;

	/**
	 * Initialize an index triple store but do not do any indexing
	 * 
	 * @return <code>OntModel</code> as initialized
	 * @throws HiveIndexerException
	 */
	public abstract OntModel initializeBareOntologyModel()
			throws HiveIndexerException;

}
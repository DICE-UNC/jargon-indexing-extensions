/**
 * 
 */
package org.irods.jargon.indexing.hive.metadata.utils;

import java.util.Properties;

import org.irods.jargon.hive.external.indexer.HiveIndexerException;
import org.irods.jargon.hive.external.utils.JenaHiveConfiguration;
import org.irods.jargon.hive.external.utils.JenaHiveConfigurationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.OntModel;

/**
 * command line utility to intialize a hive indexer triple store by specifying a
 * properties file. This utility does not do batch indexing of iRODS data.
 * 
 * @author Mike Conway - DICE
 * 
 */
public class HiveIndexerAdminTool {

	public static final Logger log = LoggerFactory
			.getLogger(HiveIndexerAdminTool.class);

	private final Properties props;

	/**
	 * 
	 */
	public HiveIndexerAdminTool(final Properties properties)
			throws HiveIndexerException {
		if (properties == null) {
			throw new IllegalArgumentException("null properties");
		}

		this.props = properties;
	}

	public void initialize() throws HiveIndexerException {
		log.info("initialize()");
		JenaHiveConfiguration jenaHiveConfiguration = JenaHiveConfigurationHelper
				.buildJenaHiveConfigurationFromProperties(props);
		log.info("jenaHiveConfiguration:{}", jenaHiveConfiguration);
		HiveMetadataIndexerInitializer initializer = new HiveMetadataIndexerInitializerImpl(
				jenaHiveConfiguration);
		log.info("initializing");
		OntModel ontModel = initializer.initializeBareOntologyModel();
		log.info("complete");
		ontModel.close();

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length != 1) {
			System.out
					.println("requires 1 parameter which is the file path to the controlling properties file");
		}

		String path = args[0];
		log.info("looking for properties file at path:{}", path);
		try {
			Properties props = JenaHiveConfigurationHelper
					.loadPropertiesFromAbsolutePath(path);
			HiveIndexerAdminTool hiveIndexerAdminTool = new HiveIndexerAdminTool(
					props);
			hiveIndexerAdminTool.initialize();

		} catch (HiveIndexerException e) {
			log.error("exception loading properties", e);
			throw new RuntimeException("error loading props", e);
		}

	}

}

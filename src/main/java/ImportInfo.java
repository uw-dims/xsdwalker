/**
 * @author Stuart Maclean
 *
 * Simple object representation of the namespace + schemaLocation values
 * extracted from an import element in an xsd document, e.g.

 * <xs:import namespace="http://stix.mitre.org/common-1" schemaLocation="http://stix.mitre.org/XMLSchema/common/1.1.1/stix_common.xsd"/>
 *
 * Instances of this class created by the Parser class. 
 *
 * @see SchemaInfo
 * @see Parser
 */

public class ImportInfo {

	public ImportInfo( String namespace, String schemaLocation ) {
		this.namespace = namespace;
		this.schemaLocation = schemaLocation;
	}

	@Override
	public String toString() {
		return namespace + "," + schemaLocation;
	}
	
	public final String namespace, schemaLocation;
}

// eof

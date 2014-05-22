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

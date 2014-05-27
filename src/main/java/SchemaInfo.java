import java.util.ArrayList;
import java.util.List;

/**
 * @author Stuart Maclean
 *
 * Simple object representation of the following values extracted from
 * an xsd document:
 *
 * Target namespace
 *
 * List of imports, each a pair: namespace + schemaLocation (either of
 * which might be missing)
 *
 * @see ImportInfo
 */


 public class SchemaInfo {

	SchemaInfo( String targetNamespace ) {
		this.targetNamespace = targetNamespace;
		imports = new ArrayList<ImportInfo>();
	}

	void addImport( String namespace, String schemaLocation ) {
		ImportInfo ii = new ImportInfo( namespace, schemaLocation );
		imports.add( ii );
	}

	List<ImportInfo> getImports() {
		return imports;
	}

	public final String targetNamespace;
	final List<ImportInfo> imports;
}

// eof

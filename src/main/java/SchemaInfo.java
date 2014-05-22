import java.util.ArrayList;
import java.util.List;

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

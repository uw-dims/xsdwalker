import java.io.File;
import java.util.*;

import org.apache.commons.io.FileUtils;


public class STIXTest extends junit.framework.TestCase {

	/*
	  The schemas with local (i.e. file-based)
	  schemaLocations in their imports.
	*/
	public void test_stix_v1_1_offline() throws Exception {

		// produces 162 nodes, of which 88 are leaf nodes...
		test( "stix_v1.1_offline" );
	}

	/*
	  The schemas with remote (i.e. http://) schemaLocations
	  in their imports.
	*/

	private void test( String dirName ) throws Exception {
		
		XSDWalker w = new XSDWalker();
		
		File dir = new File(  dirName );
		if( !dir.isDirectory() )
			return;
		
		Collection<File> fs = FileUtils.listFiles
			( dir, new String[] { "xsd" }, true );

		//List<File> sorted = new ArrayList<File>( fs );
		//Collections.sort( sorted );
		//		System.out.println( sorted );
		
		Collection<XSDWalker.Node> ns = w.processFiles( fs );

		XSDWalker.report( ns,  new File( dirName + ".txt" ) );
		
		
	}
}

// eof

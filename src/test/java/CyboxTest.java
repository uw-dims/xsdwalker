import java.io.File;
import java.io.*;
import java.util.*;

import org.apache.commons.io.FileUtils;


public class CyboxTest extends junit.framework.TestCase {

	/*
	  The schemas with remote (i.e. http://) schemaLocations
	  in their imports.
	*/
	public void testCybox20() throws Exception {
		test( "cybox_v2.0" );
	}

	/*
	  The schemas with local (i.e. file-based)
	  schemaLocations in their imports.
	*/
	public void testCybox20Offline() throws Exception {

		// produces 85 nodes, of which 53 are leaf nodes...
		test( "cybox_v2.0_offline" );
	}

	/*
	  The schemas with remote (i.e. http://) schemaLocations
	  in their imports.
	*/
	public void testCybox21() throws Exception {
		test( "cybox_v2.1" );
	}

	public void testCybox21Offline() throws Exception {

		// produces 106 nodes, of which 66 are leaf nodes...
		test( "cybox_v2.1_offline" );
	}



	private void test( String dirName ) throws Exception {
		
		File reportFile = new File( dirName + ".dat" );
		reportFile.delete();
		
		XSDWalker w = new XSDWalker();
		
		File dir = new File(  dirName );
		if( !dir.isDirectory() )
			return;
		
		Collection<File> fs = FileUtils.listFiles
			( dir, new String[] { "xsd" }, true );

		List<File> sorted = new ArrayList<File>( fs );
		Collections.sort( sorted );
		PrintWriter pw = new PrintWriter( new FileWriter( reportFile ) );
		pw.println( "Inputs: " + sorted.size() );
		for( File f : sorted )
			pw.println( f );
		pw.println();
		pw.close();
		
		Collection<XSDWalker.Node> ns = w.processFiles( fs );


		XSDWalker.report( ns, reportFile );

		File uber = new File( dir + ".uber.xsd" );
		XSDWalker.toUberXSD( ns, "xs", uber );
		/*
		  List<String> locations = XSDWalker.locations( ns );
		System.out.println( "Nodes: " + locations.size() );
		Collections.sort( locations );
		for( String el : locations )
			System.out.println( el );

		List<String> leaves = XSDWalker.leafNodeLocations( ns );
		System.out.println( "Leaf Nodes: " + leaves.size() );
		Collections.sort( leaves );
		for( String el : leaves )
			System.out.println( el );

		XSDWalker.checkNamespaceLinkage( ns );
		*/
		
	}
}

// eof

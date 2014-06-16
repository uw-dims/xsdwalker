import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

/**
 * @author Stuart Maclean
 *
 * XSDWalker: walk a graph of XML schema documents (.xsd), producing
 *
 * (a) An 'uber' xsd file which can be passed directly to xjc.  This
 * uber file consists solely of xml schema import statements,
 * identifiying just the xsd files for xjc to scan.
 *
 * (b) A report file ($uber.txt) documenting the (import) relationships
 * between both the .xsd names on cmd line and all the ones found in
 * the graph traversal.
 *
 * (c) Also logs (log4j) to ./xsdwalker.log
 *
 * Input .xsds can be directories (then scanned for .xsd files
 * recursively), individual file names, and urls (including http://
 * ones)
 *
 *
 * Options:
 *
 * -e <arg>   exclude any file/directory matching the arg. Can be used 2+ times
 *
 * -n         dryrun, show the .xsd set but do not visit any.
 *
 * -u <arg> name of uber .xsd output, defaults to first input name
 * (file/dir/url) if not supplied.
 *
 * -v         verbose
 *
 * -g produce a 'graph file', a simple text file listing all nodes and
 * edges in the resultant 'graph'.  We can then use e.g. yfiles or
 * some other graphing package to visualize the node set.  The output
 * graph file name is $uber.graph.
 * 
 */

public class XSDWalker {

	static final Logger log = Logger.getLogger( XSDWalker.class.getName() );
	
	static private void printUsage( Options os, String usage,
									String header, String footer ) {
		HelpFormatter hf = new HelpFormatter();
		hf.setWidth( 80 );
		hf.printHelp( usage, header, os, footer );
	}

	public static void main( String[] args ) throws Exception {

		Options os = new Options();
		os.addOption( "g", false,
					  "produce edges file, used for graphing." );
		os.addOption( "n", false,
					  "dryrun, show the .xsd set but do not visit any." );
		Option excludes = OptionBuilder.
			hasArgs().
			withDescription( "exclude file/directory matching pattern(s)" ).
			create( 'e' );
		os.addOption( excludes );
		os.addOption( "u", true,
					  "name of uber .xsd output" );
		os.addOption( "v", false, "verbose" );
		final String USAGE =
			"[-e file/dir]* [-g] [-n] [-u uber] [-v] (file|dir|url)+";
		final String HEADER = "";
		final String FOOTER = "";
		
		CommandLineParser clp = new PosixParser();
		CommandLine cl = null;
		try {
			cl = clp.parse( os, args );
		} catch( Exception e ) {
			System.err.println( e );
			printUsage( os, USAGE, HEADER, FOOTER );
			System.exit(1);
		}
		boolean dryRun = cl.hasOption( "n" );
		boolean verbose = cl.hasOption( "v" );
		boolean writeGraphFile = cl.hasOption( "g" );

		final List<File> excludeFiles = new ArrayList<File>();
		final List<File> excludeDirs = new ArrayList<File>();
		if( cl.hasOption( "e" ) ) {
			log.info( "Found excludes" );
			String[] ss = cl.getOptionValues( "e" );
			for( String s : ss ) {
			log.info( "Found exclude " + s );
				File exclude = new File( s );
				if( false ) {
				} else if( exclude.isFile() ) {
					excludeFiles.add( exclude );
				} else if( exclude.isDirectory() ) {
					excludeDirs.add( exclude );
				}
			}
		}

		String uber = null;
		if( cl.hasOption( "u" ) ) {
			uber = cl.getOptionValue( "u" );
		}
		
		args = cl.getArgs();
		if( args.length < 1 ) {
			System.err.println( XSDWalker.class.getName() +
								": no input dirs/files/urls" );
			printUsage( os, USAGE, HEADER, FOOTER );
			System.exit(1);
		}
		
		List<URL>  urls = new ArrayList<URL>();
		List<File> dirs = new ArrayList<File>();
		List<File> files = new ArrayList<File>();

		for( String arg : args ) {
			File f = new File( arg );
			if( false ) {
			} else if( !f.exists() ) {
				urls.add( new URL( arg ) );
			} else if( f.isFile() ) {
				/*
				  DO NOT include as an input the very artifact
				  we are trying to produce on output
				*/
				if( f.getName().indexOf( "uber" ) > -1 )
					continue;
				files.add( f );
			} else if( f.isDirectory() ) {
				dirs.add( f );
			}
		}

		log.info( "Dirs : " + dirs );
		log.info( "Files: " + files );
		log.info( "URLs: "  + urls );

		/*
		  No output name offered via a -u option, so we pick one from
		  the input dirs/files/urls
		*/
		if( uber == null ) {
			if( false ) {
			} else if( !dirs.isEmpty() ) {
				uber = dirs.get(0).getName();
			} else if( !files.isEmpty() ) {
				String s = files.get(0).getName();
				if( s.endsWith( ".xsd" ) ) {
					s = s.substring( 0, s.length() - ".xsd".length() );
				}
				uber = s;
			} else if( !urls.isEmpty() ) {
				String s = urls.get(0).toString();
				if( s.endsWith( ".xsd" ) ) {
					s = s.substring( 0, s.length() - ".xsd".length() );
				}
				s = s.substring( s.lastIndexOf( "/" ) + 1);
				uber = s;
			}
		}

		File uberFile = new File( uber + ".uber.xsd" );
		log.info( "Uber schema: " + uberFile );

		File reportFile = new File( uber + ".txt" );
		reportFile.delete();
		log.info( "Report file: " + reportFile );


		List<File> allFiles = new ArrayList<File>();
		allFiles.addAll( files );

		if( !dirs.isEmpty() ) {
			IOFileFilter ff1 = new IOFileFilter() {
					public boolean accept( File file ) {
						// this is the method called for files...
						for( File ex : excludeFiles )  {
							if( file.getPath().contains( ex.getPath() ) )
								return false;
						}
						return file.getName().endsWith( ".xsd" );
					}
					public boolean accept( File dir, String name ) {
						// not sure this ever called ?
						return false;
					}
				};
			IOFileFilter ff2 = new IOFileFilter() {
					// this is the method called for dirs...
					public boolean accept( File file ) {
						for( File ex : excludeDirs )  {
							if( file.getPath().contains( ex.getPath() ) )
								return false;
						}
						return true;
					}
					public boolean accept( File dir, String name ) {
						// not sure this ever called ?
						return false;
					}
				};
			for( File dir : dirs ) {
				Collection<File> fs = FileUtils.listFiles( dir, ff1, ff2 );
				allFiles.addAll( fs );
			}
		}
		Collections.sort( allFiles );

		List<URL> allURLs = new ArrayList<URL>();
		allURLs.addAll( urls );
		for( File f : allFiles ) {
			URL u = f.getCanonicalFile().toURI().toURL();
			allURLs.add( u );
		}

		// The processing going forward is based a list of URLs, NOT files...
		if( verbose ) {
			for( URL u : allURLs )
				log.info( u );
		}
		log.info( "Found " + allURLs.size() + " .xsd files/urls" );
		
		if( dryRun ) {
			log.info( "Skipping processing" );
			return;
		}

		XSDWalker w = new XSDWalker();
		Collection<Node> ns = w.process( allURLs );
		System.out.println( "Nodes: " + ns.size() );
		if( ns.isEmpty() )
			return;

		if( verbose ) {
			List<Node> toSort = new ArrayList<Node>( ns );
			Collections.sort( toSort );
			for( Node n : toSort )
				System.out.println( n.location );
		}

		//		resolve( ns );

		Collection<Node> leaves = leafNodes( ns );
		System.out.println( "Leaf Nodes: " + leaves.size() );
		
		Collection<Node> remotes = remoteNodes( ns );
		System.out.println( "Remote Nodes: " + remotes.size() );
		
		//XSDWalker.checkNamespaceLinkage( ns );
		XSDWalker.report( ns, reportFile );
		XSDWalker.toUberXSD( ns, "xs", uberFile );

		if( writeGraphFile ) {
			File f = new File( uber + ".graph" );
			PrintWriter pw = new PrintWriter( new FileWriter( f ) );
			for( Node n : ns ) {
				pw.println( "N," + n.location.toString() + "," +
							n.targetNamespace );
			}
			for( Node n : ns ) {
				for( Node out : n.outs ) {
					pw.println( "E," + n.location.toString() + "," +
								out.location.toString() );
				}
			}
			pw.close();
		}
	}
	
	public XSDWalker() throws Exception {
		p = new Parser();
	}
	
	/**
	 * @return A graph of nodes created by adding all the nodes in
	 * 'us' and by following the import paths of each node in 'us',
	 * recursively.
	 */
	public Collection<Node> process( Collection<URL> us ) throws Exception {
		Map<String,Node> nodes = new HashMap<String,Node>();
		//		Set<String> visited = new HashSet<String>();
		//		Map<String,URL> byTargetNamespace = new HashMap<String,URL>();

		for( URL u : us )
			visit( u, nodes, null, null, "" );
		return nodes.values();
	}

	/**
	 * Convenience method, e.g by test cases.  Permits file lists to
	 * be specified in place of url lists.  Does the conversions then
	 * delegates.  Cannot have signature process( Collection<File> )
	 * due to type erasure, pah!
	 */
	public Collection<Node> processFiles( Collection<File> fs )
		throws Exception {
		List<URL> us = new ArrayList<URL>( fs.size() );
		for( File f : fs ) {
			URL u = f.getCanonicalFile().toURI().toURL();
			us.add( u );
		}
		return process( us );
	}
	
	private void visit( URL u, Map<String,Node> result,
						Node referrer, String referringNamespace,
						String indent )	throws Exception {

		if( result.containsKey( u.toString() ) )
			return;

		log.info( indent + "Visiting " + u );
		
		SchemaInfo si = null;

		try {
			si = p.parse( u.toString() );
		} catch( Exception e ) {
			log.warn( "Parse failure: " + u );
			return;
		}
		
		log.info( indent + "TNS " + si.targetNamespace );
		Node n = new Node( u, si.targetNamespace );
		result.put( u.toString(), n );

		// build an incoming edge if namespace linkage matches...
		if( referrer != null ) {
			if( si.targetNamespace.equals( referringNamespace ) ) {
				referrer.outs.add( n );
				n.ins.add( referrer );
			} else {
				log.warn( "Namespace mismatch: actual " + si.targetNamespace+
						  ", expected " + referringNamespace );
			}
		}
		
		for( ImportInfo ii : si.getImports() ) {
			log.info( indent + "Import " + ii );
			
			String s = ii.schemaLocation;
			URL u2;
			if( false ) {
			} else if( s.startsWith( "http:" ) ) {
				u2 = new URL( s );
			} else if( s.startsWith( "file:" ) ) {
				File f = new File( s );
				if( !f.isAbsolute() ) {
					File dir = new File( u.toURI() ).getParentFile();
					f = new File( dir, s );
				}
				f = f.getCanonicalFile();
				u2 = f.toURI().toURL();
			} else {
				// is relative, to u??
				u2 = new URL( u, s );
			}

			Node tgt = result.get( u2.toString() );
			if( tgt == null ) {
				visit( u2, result, n, ii.namespace, indent + " " );
			} else {
				// build an outgoing edge if namespace linkage matches...
				if( tgt.targetNamespace.equals( ii.namespace ) ) {
					n.outs.add( tgt );
					tgt.ins.add( n );
				} else {
					log.warn( "Namespace mismatch: actual " +
							  tgt.targetNamespace+
							  ", expected " + ii.namespace );
				}
			}
		}
	}

	/**
	 * @return The subset of nodes in 'ns' which have no in edges
	 */
	static List<Node> leafNodes( Collection<Node> ns ) {
		List<Node> result = new ArrayList<Node>();
		for( Node n : ns ) {
			if( n.ins.isEmpty() )
				result.add( n );
		}
		return result;
	}

	/**
	 * @return The subset of nodes in 'ns' which are 'remote',
	 * i.e. whose url starts http:
	 */
	static Collection<Node> remoteNodes( Collection<Node> ns ) {
		List<Node> result = new ArrayList<Node>();
		for( Node n : ns ) {
			if( n.location.getProtocol().startsWith( "http" ) )
				result.add( n );
		}
		return result;
	}

	/**
	 * For all leaf nodes L, discard L if its tns appears in a node in the
	 * remote node set H.  Essentially L-H.
	 */
	static Collection<Node> pruneLeafNodes( Collection<Node> leaves,
											Collection<Node> remotes ) {
		List<Node> result = new ArrayList<Node>();
		for( Node l : leaves ) {
			if( l.location.toString().startsWith( "http:" ) ) {
				result.add( l );
				continue;
			}
			boolean inRemote = false;
			for( Node r : remotes ) {
				if( l.targetNamespace.equals( r.targetNamespace ) ) {
					inRemote = true;
					break;
				}
			}
			if( !inRemote )
				result.add( l );
		}
		return result;
	}

	/**
	   <xs:import namespace="http://cybox.mitre.org/common-2"
	   schemaLocation="http://cybox.mitre.org/XMLSchema/common/2.0/cybox_common.xsd"/>
	*/
	static String asImportList( Collection<Node> ns, String xsdPrefix ) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter( sw );
		for( Node n : ns ) {
			// LOOK: printing xml by hand, pah! Use what?  A DOM?
			pw.println( "  <" + xsdPrefix +
						":import" +
						" namespace=\"" + n.targetNamespace + "\"" +
						" schemaLocation=\"" + n.location + "\"/>" );
		}
		return sw.toString();
	}

	static void toUberXSD( Collection<Node> ns,
						   String xsdPrefix, File outFile )
		throws IOException {
		List<Node> leaves = leafNodes( ns );
		Collections.sort( leaves );
		log.info( "Leaves : " + leaves.size() );
		
		Collection<Node> remotes = remoteNodes( ns );
		log.info( "Remotes : " + remotes.size() );
		Collection<Node> prunedLeaves = pruneLeafNodes( leaves, remotes );
		log.info( "PrunedLeaves : " + prunedLeaves.size() );
		String imports = asImportList( prunedLeaves, xsdPrefix );

		/*
		  <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
		  targetNamespace="http://uber.org" 
		  elementFormDefault="qualified" 
		  attributeFormDefault="unqualified" 
		  version="2.0">
		*/
		// LOOK: printing xml by hand, pah! Use what?  A DOM?
		String root = "<" + xsdPrefix + ":schema " +
			" xmlns:" + xsdPrefix + "=\"http://www.w3.org/2001/XMLSchema\"" +
			" targetNamespace=\"" + outFile.getName() + "\"" +
			" elementFormDefault=\"qualified\"" +
			" attributeFormDefault=\"unqualified\"" + 
			" version=\"2.0\">";
		PrintWriter pw = new PrintWriter( new FileWriter( outFile ) );
		pw.println( root );
		pw.println( imports );
		pw.println( "</" + xsdPrefix + ":schema>" );
		pw.close();

	}
	
	static List<String> leafNodeLocations( Collection<Node> ns ) {
		List<String> result = new ArrayList<String>();
		for( Node n : ns ) {
			if( n.ins.isEmpty() ) {
				//	result.add( n.location );
			}
		}
		return result;
	}


	static void reportHierarchy( Collection<Node> ns, File output )
		throws IOException {
		List<Node> leaves = leafNodes( ns );
		Collections.sort( leaves );
		PrintWriter pw = new PrintWriter( new FileWriter( output, true ) );
		for( XSDWalker.Node n : leaves ) {
			String s = n.hierarchy();
			pw.println( s );
		}
		pw.close();
	}
		
	static void report( Collection<Node> ns, File output )
		throws IOException {
		
		PrintWriter pw = new PrintWriter( new FileWriter( output, true ) );
		pw.println( "Nodes: " + ns.size() );
		List<Node> sorted = new ArrayList<Node>( ns );
		Collections.sort( sorted );
		for( Node n : sorted ) {
			pw.println( n.location );
		}
		pw.println();
		List<Node> leaves = leafNodes( ns );
		Collections.sort( leaves );
		pw.println( "Leaves: " + leaves.size() );
		for( Node n : leaves ) {
			pw.println( n.location );
		}
		pw.println();
		pw.println( "Hierarchy: " );
		for( XSDWalker.Node n : leaves ) {
			String s = n.hierarchy();
			pw.println( s );
		}

		Collection<Node> remotes = remoteNodes( ns );
		Collection<Node> prunedLeaves = pruneLeafNodes( leaves, remotes );
		pw.println();
		pw.println( "Unique Leaves: " + prunedLeaves.size() );
		for( XSDWalker.Node n : prunedLeaves ) {
			pw.println( n.location );
		}
		
		pw.println();
		pw.println( "As Import List" );
		String s = asImportList( prunedLeaves, "xs" );
		pw.println( s );
		
		pw.close();
	}
		
	private boolean haveTargetNamespace( Collection<Node> ns, String tns ) {
		for( Node n : ns )
			/*
			  node tns can be null so make tns the argument to
			  String.equals and NOT the target
			*/
			if( tns.equals( n.targetNamespace ) )
				return true;
		return false;
	}

	/*
	  static class Graph {
		Graph() {
			nodes = new ArrayList<Node>();
			edges = new ArrayList<Edge>();
		}
		List<Node> nodes;
		List<Edge> edges;
	}
	*/
	
	static class Node implements Comparable<Node> {
		Node( URL location, String tns ) {
			this.location = location;
			this.targetNamespace = tns;
			imports = new ArrayList<ImportInfo>();
			ins = new ArrayList<Node>();
			outs = new ArrayList<Node>();
		}

		public void addImport( ImportInfo ii ) {
			imports.add( ii );
		}
		
		@Override
		public int hashCode() {
			return location.hashCode();
		}

		@Override
		public boolean equals( Object o ) {
			if( o == this )
				return true;
			if( !( o instanceof Node ) )
				return false;
			Node that = (Node)o;
			return this.location.equals( that.location );
		}

		@Override
		public int compareTo( Node o ) {
			URL u1 = this.location;
			URL u2 = o.location;
			return u1.toString().compareTo( u2.toString() );
		}
		
		public String hierarchy() {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter( sw );
			Set<Node> visited = new HashSet<Node>();
			hierarchy( pw, "", visited );
			return sw.toString();
		}

		public void hierarchy( PrintWriter pw, String indent,
							   Set<Node> visited ) {
			if( visited.contains( this ) )
				return;
			visited.add( this );
			pw.println( indent + location );
			for( Node tgt : outs ) {
				tgt.hierarchy( pw, indent + " ", visited );
			}
		}
		
		@Override
		public String toString() {
			return location + "," + targetNamespace + "," +
				ins.size() + "," + outs.size();
		}
		
		final URL location;
		final String targetNamespace;
		final List<ImportInfo> imports;
		final List<Node> ins, outs;
		//		String targetNamespace;
	}

	static class Edge {
		Edge( Node source, Node target, String namespace ) {
			this.source = source;
			this.target = target;
			this.namespace = namespace;
		}
		final Node source, target;
		final String namespace;
	}

	private final Parser p;
	//	private final Logger log;
}

// eof

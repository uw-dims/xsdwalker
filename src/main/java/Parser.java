import java.io.File;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Stuart Maclean
 *
 * Parser class supporting the XSDWalker main class. Given a URL
 * (either file: or http:) representing an xsd XMLSchema file, scan
 * the content and extract a pair (which we name a SchemaInfo):
 *
 * 1 The target namespace, an attribute of the outermost <schema>
 * element itself.
 *
 * 2 A list of 'ImportInfo' objects, derived via extraction of all
 * <import> elements.
 *
 * @see SchemaInfo
 * @see ImportInfo
 */
public class Parser {
	
	public Parser() throws Exception {
		DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
		// wah, without this 'namespace awareness setting' it all fails!
		fac.setNamespaceAware( true );
		bob = fac.newDocumentBuilder();
		XPathFactory xpf = XPathFactory.newInstance();
		xp = xpf.newXPath();

		NamespaceContext nc = new NamespaceContext() {
				public String getNamespaceURI( String prefix ) {
					if( prefix == null ) {
						throw new IllegalArgumentException( "No prefix" );
					} else if( prefix.equals( "xs" ) ) {
						// LOOK: do we need other prefixes? xsd?
						return "http://www.w3.org/2001/XMLSchema";
					} else {
						return XMLConstants.NULL_NS_URI;
					}
				}
				public String getPrefix(String namespaceURI) {
					// Not needed in this context.
					return null;
				}
				public Iterator getPrefixes(String namespaceURI) {
					// Not needed in this context.
					return null;
				}
			};
		xp.setNamespaceContext( nc );
	}

	public SchemaInfo parse( File xsd ) throws Exception {

		Document d = bob.parse( xsd );
		return parse( d );
	}

	public SchemaInfo parse( String uri ) throws Exception {

		Document d = bob.parse( uri );
		return parse( d );
	}

	SchemaInfo parse( Document d ) throws Exception {
	
		String expr1 = "/xs:schema/@targetNamespace";
		String tns = xp.evaluate( expr1, d );
		
		SchemaInfo result = new SchemaInfo( tns );
		
		String expr2 = "/xs:schema/xs:import";
		NodeList nl = (NodeList)xp.evaluate( expr2, d,
											 XPathConstants.NODESET );
		for( int i = 0; i < nl.getLength(); i++ ) {
			Node n = nl.item(i);
			NamedNodeMap nnm = n.getAttributes();
			Node n1 = nnm.getNamedItem( "namespace" );
			Node n2 = nnm.getNamedItem( "schemaLocation" );
			if( n1 == null || n2 == null )
				// have seen imports with no schemaLocation
				continue;
			result.addImport( n1.getNodeValue(), n2.getNodeValue() );
		}
		return result;
	}

	// Used in development, not used in anger, use XSDWalker.main
	public static void main( String[] args ) throws Exception {
		Parser p = new Parser();
		File in = new File( args[0] );
		SchemaInfo si = p.parse( in );
		System.out.println( "TNS: " + si.targetNamespace );
		System.out.println( "Imports: " + si.getImports() );
	}

	private final DocumentBuilder bob;
	private final XPath xp;
}

// eof

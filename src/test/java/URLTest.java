import java.io.File;
import java.net.*;

public class URLTest extends junit.framework.TestCase {

	public void test1() throws Exception {

		try {
			URL u1 = new URL( "foo" );
		} catch( MalformedURLException mue ) {
		}
	}

	public void testRelativeFiles1() throws Exception {

		try {
			File cwd = new File( "." );
			//cwd = cwd.getCanonicalFile();

			File f = new File( cwd, "foo" );
			URL u1 = f.toURI().toURL();
			System.out.println( u1 );
		} catch( MalformedURLException mue ) {
		}
	}

	public void testRelativeFiles2() throws Exception {

		File cwd = new File( "." );
		//cwd = cwd.getCanonicalFile();
		URI cwdu = cwd.toURI();
		
		File f = new File( cwd, "foo" );
		URI u1 = f.toURI();
		System.out.println( u1 );

		URI u2 = cwdu.relativize( u1 );
		System.out.println( u2 );
	}

	public void testComponents() throws Exception {

		File cwd = new File( "." );
		URI uri = cwd.toURI();

		String scheme = uri.getScheme();
		System.out.println( scheme );
		assertEquals( scheme, "file" );

		URL url = cwd.toURI().toURL();
		String protocol = url.getProtocol();
		assertEquals( protocol, "file" );
	}

	public void testSchemaLocation() throws Exception {

		String schemaLocation = "foo/bar.xsd";

		URI uri = new URI( schemaLocation );
		System.out.println( uri );

		String scheme = uri.getScheme();
		System.out.println( scheme );
		assertNull( scheme );

		URI normal = uri.normalize();
		System.out.println( normal );

		assertFalse( uri.isAbsolute() );
		
	}
}

// eof

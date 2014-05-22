import java.net.*;

public class URLTest extends junit.framework.TestCase {

	public void test1() throws Exception {

		try {
			URL u1 = new URL( "foo" );
		} catch( MalformedURLException mue ) {
		}
	}
	
}
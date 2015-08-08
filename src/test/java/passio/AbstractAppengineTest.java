package passio;

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractAppengineTest {

	@Autowired
	private LocalServiceTestHelper localServiceTestHelper;

	@Before
	public void setUpLocalServiceHelper() {
		localServiceTestHelper.setUp();
	}

	@After
	public void tearDownLocalServiceHelper() {
		localServiceTestHelper.tearDown();
	}

}

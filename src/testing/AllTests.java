package testing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import testing.*;

@RunWith(Suite.class)
@SuiteClasses({TestServer.class, TestMessage.class})
public class AllTests {
	
}

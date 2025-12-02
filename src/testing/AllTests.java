package testing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import testing.TestServer;

@RunWith(Suite.class)
@SuiteClasses({TestServer.class})
public class AllTests {

}

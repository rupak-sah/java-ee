package oracle.paas.apaas.javaee.env;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by ramgarg on 9/8/2016.
 */
public class SecurityUtilTest {

    @Before
    public void setUp() throws Exception {
        System.setProperty("domain.home", System.getProperty("user.home"));
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testGenerateSecurityDatFile() throws Exception {
        SecurityUtil.generateSecurityDatFile();
    }
}
package oracle.paas.apaas.javaee.env;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import oracle.paas.apaas.envmanager.api.APaaSEnvironment;
import oracle.paas.apaas.envmanager.api.UnSupportedEnvironmentException;
import oracle.paas.platform.dao.internal.SMTestBase;
import oracle.paas.platform.service.common.model.ServiceTypeManager;
import oracle.paas.platform.service.dao.ServiceEntityManagerFactory;
import oracle.paas.platform.service.common.model.ComponentTypeManager;
import oracle.paas.platform.service.dao.ServiceTypeManagerFactory;
import oracle.paas.platform.service.dao.ComponentTypeManagerFactory;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by mmyalipu on 03/10/2015.
 */
public class JavaEEEnvironmentTest extends SMTestBase {

    boolean isRegistered = false;
    @Before
    public void registerServiceAndComponents() throws Exception{
        if(isRegistered){
            return;
        }
        JSONObject component = getJSONObject("testapaasjavaeeenvcomponent.json");
        ServiceTypeManager serviceTypeManager = ServiceTypeManagerFactory.getInstance(ServiceEntityManagerFactory.getEM(), "APaaSjavaeeEnvironment");
        ComponentTypeManager componentTypeManager = ComponentTypeManagerFactory.getInstance(ServiceEntityManagerFactory.getEM());
        componentTypeManager.registerComponentType(component);
        //component = getJSONObject("testapaasjavaeeenvcomponent1.json");
        //componentTypeManager.registerComponentType(component);
        JSONObject service = getJSONObject("testapaasjavaeeenvservice.json");
        serviceTypeManager.registerServiceType(service);
        isRegistered = true;
    }

    private JSONObject getJSONObject(String jsonFileName) throws Exception{
        String jsonString = getResource(jsonFileName);
        JSONObject jsonObject = new JSONObject(jsonString);
        return  jsonObject;
    }

    private String getResource(String rsc) {
        String val = "";
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream(rsc);
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            // reads each line
            String l;
            while ((l = r.readLine()) != null) {
                val = val + l;
            }
            in.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return val;
    }


    @Test
    public void testGetJavaEEEnvironmentForGivenMajorVersion() throws UnSupportedEnvironmentException{
        JavaEEEnvironment environment = new JavaEEEnvironment();
        APaaSEnvironment aPaaSEnvironment = environment.getAPaaSEnvironment("1");
        assertEquals("0.11", aPaaSEnvironment.getMajorVersion());
        assertEquals("0.11.2" , aPaaSEnvironment.getLatestMinorVersion());
        assertEquals("adc00pgs.us.oracle.com:8080/oracle.cloud.apaas.env.javaee.0-11-2.100315", aPaaSEnvironment.getLatestImageName());
    }

    @Test(expected = UnSupportedEnvironmentException.class)
    public void testGetJavaEEEnvironmentForGivenMajorVersionThrowsException() throws UnSupportedEnvironmentException{
        JavaEEEnvironment environment = new JavaEEEnvironment();
         environment.getAPaaSEnvironment("2");
    }

    @Ignore
    @Test
    public void testGetLatestJavaEEEnvironment() throws UnSupportedEnvironmentException{
        JavaEEEnvironment environment = new JavaEEEnvironment();
        APaaSEnvironment aPaaSEnvironment = environment.getAPaaSEnvironment();
        assertEquals("0.12", aPaaSEnvironment.getMajorVersion());
        assertEquals("0.12.0" , aPaaSEnvironment.getLatestMinorVersion());
        assertEquals("adc00pgs.us.oracle.com:8080/oracle.cloud.apaas.env.javaee.0-12-0.100315", aPaaSEnvironment.getLatestImageName());
    }

    @Test
    public void testGetGivenMinorVersionJavaEEEnvironment()throws UnSupportedEnvironmentException{
        JavaEEEnvironment environment = new JavaEEEnvironment();
        APaaSEnvironment aPaaSEnvironment = environment.getAPaaSEnvironment("0.11", "0.11.0");
        assertEquals("0.11", aPaaSEnvironment.getMajorVersion());
        assertEquals("0.11.2" , aPaaSEnvironment.getLatestMinorVersion());
        assertEquals("adc00pgs.us.oracle.com:8080/oracle.cloud.apaas.env.javaee.0-11-2.100315", aPaaSEnvironment.getLatestImageName());
        assertEquals("0.11.0" , aPaaSEnvironment.getCurrentMinorVersion());
        assertEquals("adc00pgs.us.oracle.com:8080/oracle.cloud.apaas.env.javaee.0-11-0.100315", aPaaSEnvironment.getCurrentImageName());
    }
}

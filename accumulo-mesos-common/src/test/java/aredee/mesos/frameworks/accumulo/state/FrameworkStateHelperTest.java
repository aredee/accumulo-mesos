package aredee.mesos.frameworks.accumulo.state;

import aredee.mesos.frameworks.accumulo.model.Framework;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.mesos.state.InMemoryState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FrameworkStateHelperTest {

    FrameworkStateHelper stateHelper;
    final String FID = "test-framework-uuid";

    @Before
    public void setUp() throws Exception {
        stateHelper = new FrameworkStateHelper(new InMemoryState());
    }

    @After
    public void tearDown() throws Exception {
    }


    //@Test
    public void testHasRegisteredFrameworks() throws Exception {
        assertFalse(stateHelper.hasRegisteredFrameworks());
    }

    //@Test
    public void testGetFrameworkIdMap() throws Exception {
        stateHelper.getFrameworkIdMap();
    }

    //@Test
    public void testGetFrameworkNameMap() throws Exception {
        Map<String,String> frameworks = stateHelper.getFrameworkNameMap();
        assertTrue(frameworks.isEmpty());
    }

    @Test
    public void testSaveFrameworkConfig() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        byte[] jsonBytes = readJsonResource("/state/ZkTestFramework.json");
        Framework framework = mapper.readValue(jsonBytes, Framework.class);
        framework.setId(FID);

        stateHelper.saveFrameworkConfig(framework);

        Framework fromState = stateHelper.getFrameworkConfig(FID);

        assertTrue(framework.toString().equals(fromState.toString()));
    }

    //@Test
    public void testGetFrameworkConfig() throws Exception {

    }

    private byte[] readJsonResource(String resource) throws IOException {
        URL url = this.getClass().getResource(resource);
        File jsonFile = new File(url.getFile());
        byte [] jsonBytes = IOUtils.toByteArray(new FileInputStream(jsonFile));
        return jsonBytes;
    }
}
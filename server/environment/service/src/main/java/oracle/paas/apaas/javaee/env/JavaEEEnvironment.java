/*
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.paas.apaas.javaee.env;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * APAAS Environment implementation class for javaee.
 */
import oracle.paas.apaas.common.utils.api.ArtifactVersionsComparator;
import oracle.paas.apaas.common.utils.api.LocalStringManager;
import oracle.paas.apaas.common.utils.api.VersionComparator;
import oracle.paas.apaas.common.utils.api.VersionComparatorFactory;
import oracle.paas.apaas.common.utils.api.VersionUtil;
import oracle.paas.apaas.envmanager.api.APaaSEnvironment;
import oracle.paas.apaas.envmanager.api.APaaSEnvironmentDefinition;
import oracle.paas.apaas.envmanager.api.BaseAPaaSEnvironment;
import oracle.paas.apaas.envmanager.api.UnSupportedEnvironmentException;
import oracle.paas.platform.account.Account;
import oracle.paas.platform.exception.NException;
import oracle.paas.platform.service.common.model.ComponentTypeArtifactVersion;
import oracle.paas.platform.service.common.model.ComponentTypeManager;
import oracle.paas.platform.service.common.model.ComponentTypeVersion;
import oracle.paas.platform.service.dao.ComponentTypeManagerFactory;
import oracle.paas.platform.service.dao.ServiceEntityManagerFactory;

@APaaSEnvironmentDefinition(name = JavaEEEnvironment.ENV_NAME)
public class JavaEEEnvironment extends BaseAPaaSEnvironment {

    private static final String DEFAULT_COMMAND = "startServer.sh";
    private static LocalStringManager localStrings = LocalStringManager.getLocalStringManager(JavaEEEnvironment.class);
    private static Logger logger = Logger.getLogger(JavaEEEnvironment.class.getName());
    public static final String ENV_NAME = "JAVAEE";
    private static final String JAVAEE_ARTIFACT_NAME = "JAVAEE";
    private static final String JAVAEE_ARTIFACT_DISPLAY_LABEL = "JAVAEE";
    private static final String JAVAEE_DOMAIN_TAG = "javaee";
    private static final String APAAS_ENV_JAVAEE_COMPONENT_TYPE = "ENV-JAVAEE";
    private static final String projectVersion = VersionUtil.getAPaaSVersion();
    private static final String SAMPLE_APP_LOCATION = System.getenv("MW_HOME") + File.separator + "extensions" + File.separator + "apaas" + File.separator + "samples" + File.separator + "javaee-sample-" + projectVersion.toString() + ".zip";

    /**
     * @see oracle.paas.apaas.envmanager.api.BaseAPaaSEnvironment#getAPaaSEnvironment()
     */
    @Override
    public APaaSEnvironment getAPaaSEnvironment() throws UnSupportedEnvironmentException {
        ComponentTypeManager componentTypeManager = ComponentTypeManagerFactory.getInstance(ServiceEntityManagerFactory.getEM());
        Set<ComponentTypeVersion> componentTypeVersions;
        try {
            componentTypeVersions = componentTypeManager.getComponentVersions(APAAS_ENV_JAVAEE_COMPONENT_TYPE);
        } catch (NException ex) {
            logger.log(Level.SEVERE, APAAS_ENV_JAVAEE_COMPONENT_TYPE + " not registered ", ex);
            throw new UnSupportedEnvironmentException(localStrings.getErrorString("ACCS-LCM-05001"));
        }
        String latestMajorVersion = getLatestComponentType(componentTypeVersions);
        return getAPaaSEnvironment(latestMajorVersion);
    }

    /**
     * @see oracle.paas.apaas.envmanager.api.BaseAPaaSEnvironment#getAPaaSEnvironment(String)
     */
    @Override
    public APaaSEnvironment getAPaaSEnvironment(String majorVersion) throws UnSupportedEnvironmentException {
        ComponentTypeManager componentTypeManager = ComponentTypeManagerFactory.getInstance(ServiceEntityManagerFactory.getEM());
        ComponentTypeVersion componentTypeVersion;
        try {
            componentTypeVersion = componentTypeManager.getComponentVersion(APAAS_ENV_JAVAEE_COMPONENT_TYPE, majorVersion);
        } catch (NException ex) {
            logger.log(Level.SEVERE, APAAS_ENV_JAVAEE_COMPONENT_TYPE + " not registered ", ex);
            throw new UnSupportedEnvironmentException(localStrings.getErrorString("ACCS-LCM-05001"));
        }
        if(componentTypeVersion == null)
        {
            for(ComponentTypeVersion compVer:componentTypeManager.getComponentVersions(APAAS_ENV_JAVAEE_COMPONENT_TYPE)){
                if (majorVersion.startsWith(compVer.getVersionAbbr()))
                    throw new UnSupportedEnvironmentException(localStrings.getErrorString("ACCS-LCM-05003", majorVersion, ENV_NAME));
            }
        }

        if (componentTypeVersion == null) {
            throw new UnSupportedEnvironmentException(localStrings.getErrorString("ACCS-LCM-05002", majorVersion, ENV_NAME));
        }

        JavaEEEnvironment javaeeEnvironment = new JavaEEEnvironment();
        javaeeEnvironment.setLatestImageName(componentTypeVersion.getMachineImage());
        javaeeEnvironment.setCurrentImageName(componentTypeVersion.getMachineImage());
        javaeeEnvironment.setMajorVersion(componentTypeVersion.getVersionAbbr());
        ComponentTypeArtifactVersion compTypeArtifactVersionInstance = componentTypeVersion.getArtifactVersion(JAVAEE_ARTIFACT_NAME);
        javaeeEnvironment.setLatestMinorVersion(compTypeArtifactVersionInstance.getVersion());
        javaeeEnvironment.setCurrentMinorVersion(compTypeArtifactVersionInstance.getVersion());
        javaeeEnvironment.setSampleApplicationLocation(SAMPLE_APP_LOCATION);
        javaeeEnvironment.setCloudKey(compTypeArtifactVersionInstance.getCloudKey());
        javaeeEnvironment.setCurrentMinorVersionDisplay(compTypeArtifactVersionInstance.getDescription());
        javaeeEnvironment.setLatestMinorVersionDisplay(compTypeArtifactVersionInstance.getVersionDisplayName());
        return javaeeEnvironment;
    }

    private String getLatestComponentType(Set<ComponentTypeVersion> versions) {
        List<String> verList = new ArrayList<String>();
        VersionComparator versionComparator = VersionComparatorFactory.getInstance(getArtifactName());

        for (ComponentTypeVersion componentTypeVersion : versions) {
            verList.add(componentTypeVersion.getVersionAbbr());
        }
        Collections.sort(verList, new ArtifactVersionsComparator(versionComparator, Boolean.TRUE));
        return verList.size()>0? verList.get(0):null;
    }

    /**
     * @see oracle.paas.apaas.envmanager.api.BaseAPaaSEnvironment#getAPaaSEnvironment(String, String)
     */
    @Override
    public APaaSEnvironment getAPaaSEnvironment(String majorVersion, String minorVersion) throws UnSupportedEnvironmentException {
        ComponentTypeManager componentTypeManager = ComponentTypeManagerFactory.getInstance(ServiceEntityManagerFactory.getEM());
        ComponentTypeVersion latestComponentTypeVersion;
        try {
            latestComponentTypeVersion = componentTypeManager.getComponentVersion(APAAS_ENV_JAVAEE_COMPONENT_TYPE, majorVersion);
        } catch (NException ex) {
            logger.log(Level.SEVERE, APAAS_ENV_JAVAEE_COMPONENT_TYPE + " not registered ", ex);
            throw new UnSupportedEnvironmentException(localStrings.getErrorString("ACCS-LCM-05001"));
        }

        if (latestComponentTypeVersion == null) {
            throw new UnSupportedEnvironmentException(localStrings.getErrorString("ACCS-LCM-05002", majorVersion, ENV_NAME));
        }
        ComponentTypeArtifactVersion latestJavaEEArtifactVersion = latestComponentTypeVersion.getArtifactVersion(JAVAEE_ARTIFACT_NAME);


        List<ComponentTypeArtifactVersion> allMinorVersionArtifacts = componentTypeManager.getArtifactVersions(APAAS_ENV_JAVAEE_COMPONENT_TYPE, JAVAEE_ARTIFACT_NAME, latestJavaEEArtifactVersion.getVersionAbbr());

        ComponentTypeArtifactVersion latestGivenMinorVersion = null;
        for (ComponentTypeArtifactVersion componentTypeArtifactVersion : allMinorVersionArtifacts) {
            if (minorVersion.equals(componentTypeArtifactVersion.getVersion())) {
                latestGivenMinorVersion = componentTypeArtifactVersion;
                break;
            }
        }

        if (null == latestGivenMinorVersion) {
            throw new UnSupportedEnvironmentException(localStrings.getErrorString("ACCS-LCM-05002", majorVersion, ENV_NAME));
        }

        int revisionId = latestGivenMinorVersion.getArtifact().getRevisionId();
        Set<ComponentTypeVersion> versions = componentTypeManager.getComponentVersions(APAAS_ENV_JAVAEE_COMPONENT_TYPE, revisionId);
        ComponentTypeVersion givenComponentTypeVersion = null;
        for (ComponentTypeVersion version : versions) {
            if (version.getVersionAbbr().equals(majorVersion)) {
                givenComponentTypeVersion = version;
                break;
            }
        }

        if (givenComponentTypeVersion == null) {
            throw new UnSupportedEnvironmentException(localStrings.getErrorString("ACCS-LCM-05002", majorVersion, ENV_NAME));
        }


        JavaEEEnvironment javaeeEnvironment = new JavaEEEnvironment();
        javaeeEnvironment.setMajorVersion(givenComponentTypeVersion.getVersionAbbr());
        javaeeEnvironment.setCurrentImageName(givenComponentTypeVersion.getMachineImage());
        javaeeEnvironment.setCurrentMinorVersion(latestGivenMinorVersion.getVersion());
        javaeeEnvironment.setCurrentMinorVersionDisplay(latestGivenMinorVersion.getVersionDisplayName());
        javaeeEnvironment.setCloudKey(givenComponentTypeVersion.getCloudKey());
        // get the latest minor for a major version
        if (null == latestComponentTypeVersion) {
            latestComponentTypeVersion = givenComponentTypeVersion;
        }
        ComponentTypeArtifactVersion latestJavaEEVersion = latestComponentTypeVersion.getArtifactVersion(JAVAEE_ARTIFACT_NAME);
        javaeeEnvironment.setLatestMinorVersion(latestJavaEEVersion.getVersion());
        javaeeEnvironment.setLatestImageName(latestComponentTypeVersion.getMachineImage());
        javaeeEnvironment.setLatestMinorVersionDisplay(latestJavaEEVersion.getVersionDisplayName());
        javaeeEnvironment.setSampleApplicationLocation(SAMPLE_APP_LOCATION);
        return javaeeEnvironment;

    }

    @Override
    public String getComponentType() {
        return APAAS_ENV_JAVAEE_COMPONENT_TYPE;
    }

    @Override
    public String getArtifactName() {
        return JAVAEE_ARTIFACT_NAME;
    }

    @Override
    public String getDomainTag() {
        return JAVAEE_DOMAIN_TAG;
    }

    @Override
    public String getUIEnvType() {
        return JavaEEEnvironment.ENV_NAME;
    }

    @Override
    public String getUIDisplayName() {
        return "Java EE";
    }
    @Override
    public String getArtifactDisplayLabel() {
        return JAVAEE_ARTIFACT_DISPLAY_LABEL;
    }

    @Override
    public String getNewestVersion(List<Account> accounts) {
//        ComponentTypeManager componentTypeManager = ComponentTypeManagerFactory.getInstance(ServiceEntityManagerFactory.getEM());
        Set<ComponentTypeVersion> componentTypeVersions;
        try {
            componentTypeVersions = BaseAPaaSEnvironment.getComponentVersions(APAAS_ENV_JAVAEE_COMPONENT_TYPE);
        } catch (NException ex) {
            logger.log(Level.SEVERE, APAAS_ENV_JAVAEE_COMPONENT_TYPE + " not registered ", ex);
            return null;
        }
        return getLatestComponentType(componentTypeVersions);
    }

    @Override
    public String getDefaultCommand() {
        return DEFAULT_COMMAND;
    }

    @Override
    public boolean isVisibleForAllCustomers(List<Account> accounts) {
        return isEnvEnabledForCustomer(accounts);
    }

    @Override
    public InputStream getAppSupportFiles() {
        try {
            String filePath = SecurityUtil.generateSecurityDatFile();
            java.nio.file.Path sPath = Paths.get(filePath);
            InputStream iStream = new FileInputStream(filePath);
            logger.log(Level.INFO, "Trying to upload to file  - " + filePath);
            //return  new ZipInputStream(theFile);
            return iStream;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to generate Security File." , e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean isAppSupportFileAvailable() {
        return true;
    }

    @Override
    public boolean isAllowedCustomSystemProperties(){  return true; }

    @Override
    public boolean isAllowedDefaultCmdOverride(){  return false; }

    @Override
    public boolean isJFRRecordingSupported(){  return true; }

    @Override
    public boolean isClusteringSupported() {
        return false;
    }

    @Override
    public Integer defaultStartupTime() {
        return 60;
    }


    @Override
    public String getEnvName() {
        return ENV_NAME.toLowerCase();
    }

    @Override
    public Set<String> barredNameSpaces(){
        LinkedHashSet nmSpace = new LinkedHashSet();
        nmSpace.add("weblogic");
        nmSpace.add("java");
        nmSpace.add("com.bea");
        return nmSpace;
    }
    @Override
    public Set<String> allowedBindingProperties(){
        LinkedHashSet nmSpace = new LinkedHashSet();
        //<>#d the d is to identify the datatype
        nmSpace.add("jndi-name#s");
        nmSpace.add("max-capacity#i");
        nmSpace.add("min-capacity#i");
        return nmSpace;
    }

    @Override
    public Map<EnvSettings, String> prescribedSettings() {
        Map<EnvSettings,String> map = new HashMap<>();
        map.put(EnvSettings.MIN_SIZE,"2G");
        return map;
    }

    @Override
    public boolean isBuildTimeEnvVariableNeeded() {
        return true;
    }

    @Override
    public boolean isFastAppDeploymentSupported() {
        return false;
    }
}

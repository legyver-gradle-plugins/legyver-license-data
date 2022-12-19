package com.legyver.gradle.legyverlicensedata;

import com.legyver.gradle.legyverlicensedata.data.LegyverLicenseDataCache;
import com.legyver.utils.propl.PropertyList;
import groovy.lang.Closure;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.language.jvm.tasks.ProcessResources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class LegyverLicenseDataPlugin implements Plugin<Project> {
    private static final Logger logger = Logging.getLogger(LegyverLicenseDataPlugin.class);
    private final LegyverLicenseDataCache legyverLicenseDataCache;

    public LegyverLicenseDataPlugin() {
        legyverLicenseDataCache = new LegyverLicenseDataCache();
    }

    @Override
    public void apply(Project project) {
        LegyverLicenseDataPluginExtension extension = project.getExtensions()
                .create("legyverLicenseData", LegyverLicenseDataPluginExtension.class);
        if (legyverLicenseDataCache.getMappingRules() == null) {
            initialMappingRules();
        }
        project.task("license").doLast(new Closure(null) {
            @Override
            public Object call(Object... args) {
                addLicenseData(project, extension);
                return null;
            }
        });
    }

    private void initialMappingRules() {
        Properties mappingRules = new Properties();
        InputStream mappingIS = LegyverLicenseDataPlugin.class.getResourceAsStream("mapping.properties");
        try {
            mappingRules.load(mappingIS);
            if (logger.isDebugEnabled()) {
                StringJoiner mappingRuleJoiner = new StringJoiner(File.pathSeparator);
                mappingRules.entrySet().forEach(es -> {
                    mappingRuleJoiner.add(es.getKey() + "=" + es.getValue());
                });
                logger.debug("Loaded mapping rules: {}", mappingRuleJoiner);
            }
            legyverLicenseDataCache.setMappingRules(mappingRules);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int validateInput(Project project, LegyverLicenseDataPluginExtension extension) {
        int version = -1;
        if (!extension.getTargetFile().isPresent()) {
            logger.warn("Skipping project {} as no targetFile is defined", project);
            return -1;
        }

        if (extension.getApplyV2LicenseDataForModules().isPresent() && !extension.getApplyV2LicenseDataForModules().get().isEmpty()) {
            version = 2;
        } else {
            logger.warn("No v2 license data modules supplied");
        }
        return version;
    }

    private void addLicenseData(Project project, LegyverLicenseDataPluginExtension extension) {
        Integer version = validateInput(project, extension);
        if (version > 0) {
            String targetFile = extension.getTargetFile().get();
            logger.debug("Processing v{} license data for target file: {}", version, targetFile);

            PropertyList licenseProperties = new PropertyList();
            for (String moduleName : getLicenseData(extension, version)) {
                mergeLicenseData(licenseProperties, moduleName, version);
            }

            ProcessResources processResources = (ProcessResources) project.getTasks().findByName(JavaPlugin.PROCESS_RESOURCES_TASK_NAME);
            File destinationDir = processResources.getDestinationDir();

            String targetAbsolutePath = getAbsoluteTargetPath(targetFile, destinationDir);
            File file = new File(targetAbsolutePath);
            try {
                File parent = file.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                licenseProperties.write(file);
            } catch (IOException e) {
                logger.error("Error saving properties", e);
                throw new RuntimeException(e);
            }
        }
    }

    private List<String> getLicenseData(LegyverLicenseDataPluginExtension extension, Integer version) {
        List<String> licenseData;
        if (version == 2) {
            licenseData = extension.getApplyV2LicenseDataForModules().get();
        } else {
            throw new RuntimeException("Unknown version: " + version);
        }

        if (logger.isDebugEnabled()) {
            String collected = licenseData.stream().collect(Collectors.joining(", "));
            logger.debug("Licenses to add: {}", collected);
        }
        return licenseData;
    }

    private PropertyList getModuleProperties(String moduleName, Integer version) {
        logger.debug("Processing module name: {}", moduleName);
        PropertyList moduleProperties = legyverLicenseDataCache.getLicenseData(moduleName, version);
        if (moduleProperties == null) {
            throw new RuntimeException("No license data known for module: " + moduleName);
        }
        return moduleProperties;
    }

    private void mergeLicenseData(PropertyList licenseProperties, String moduleName, Integer version) {
        PropertyList moduleProperties = getModuleProperties(moduleName, version);
        List<String> propertyNames = moduleProperties.stringPropertyNames();
        if (propertyNames.isEmpty()) {
            logger.warn("No properties found for module: {}", moduleName);
        } else {
            String propertyNameTest = propertyNames.iterator().next();
            logger.debug("Processing test property: {}", propertyNameTest);
            String currentValue = licenseProperties.getProperty(propertyNameTest);
            if (currentValue != null) {
                logger.debug("Not adding license data for module [{}].  Property key {} already present with value {}.",
                        moduleName, propertyNameTest, currentValue);
            } else {
                for (String propertyName : propertyNames) {
                    String value = moduleProperties.getProperty(propertyName);
                    logger.debug("Adding license data: {}={}", propertyName, value);
                    licenseProperties.put(propertyName, value);
                }
            }
        }
    }

    private static String getAbsoluteTargetPath(String targetFile, File destinationDir) {
        String buildResourcesMain;
        String srcMainResources;
        String buildResourcesTest;
        String srcTestResources;
        if (File.separator.equals("\\")) {
            buildResourcesMain = "\\\\build\\\\resources\\\\main";
            srcMainResources = "\\\\src\\\\main\\\\resources";
            buildResourcesTest = "\\\\build\\\\resources\\\\test";
            srcTestResources = "\\\\src\\\\test\\\\resources";
        } else {
            buildResourcesMain = "/build/resources/main";
            srcMainResources = "/src/main/resources";
            buildResourcesTest = "/build/resources/test";
            srcTestResources = "/src/test/resources";
        }

        String matchDirectory = destinationDir.getAbsolutePath()
                .replaceAll(buildResourcesMain, srcMainResources)
                .replaceAll(buildResourcesTest, srcTestResources);

        return matchDirectory + File.separator + targetFile;
    }

}

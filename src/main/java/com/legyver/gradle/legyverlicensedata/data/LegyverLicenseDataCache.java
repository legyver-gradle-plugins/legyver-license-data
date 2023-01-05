package com.legyver.gradle.legyverlicensedata.data;

import com.legyver.gradle.legyverlicensedata.LegyverLicenseDataPlugin;
import com.legyver.utils.propl.PropertyList;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LegyverLicenseDataCache {
    private static final Logger logger = Logging.getLogger(LegyverLicenseDataCache.class);
    private static final Map<String, PropertyList> propertyCache = new HashMap<>();
    private Properties mappingRules;
    private ElasticRuleMatcher elasticRuleMatcher;

    /**
     * Get the license data associated with a module
     * @param moduleName the module name
     * @param version the version of the license format
     * @return the license data
     */
    public PropertyList getLicenseData(String moduleName, Integer version) {
        PropertyList licensePropertiesForModule = propertyCache.get(moduleName);
        if (licensePropertiesForModule != null) {
            //case 1: we have the properties cached already
            logger.debug("Using cached license data for module: {}", moduleName);
        } else {
            logger.debug("No license data cached for module: {}", moduleName);
            if (elasticRuleMatcher == null) {
                elasticRuleMatcher = new ElasticRuleMatcher(mappingRules);
            }
            List<String> possibleModules = elasticRuleMatcher.getMatchingRules(moduleName);
            for (String possibleModule : possibleModules) {
                logger.debug("Evaluating possible module: {}", possibleModule);
                PropertyList temp = propertyCache.get(possibleModule);
                if (temp != null) {
                    //case 2: we have another variant of the module cached already
                    logger.debug("Using cached value for {}", possibleModule);
                    licensePropertiesForModule = temp;
                    //cache it with the specified module name so the first lookup won't return null next time
                    propertyCache.put(moduleName, licensePropertiesForModule);
                    break;
                } else {
                    String resource = possibleModule + "_v" + version + ".properties";
                    logger.debug("Attempting to load {} from stream", resource);
                    try (InputStream inputStream = LegyverLicenseDataCache.class.getResourceAsStream(resource)) {
                        if (inputStream != null) {
                            //case 3: we have found a variant of module to load
                            logger.debug("Resource [{}] found.  Loading properties", resource);
                            licensePropertiesForModule = new PropertyList();
                            licensePropertiesForModule.load(inputStream);
                            propertyCache.put(moduleName, licensePropertiesForModule);
                            break;
                        } else {
                            //case 4: we don't have the module variant
                            logger.debug("Resource not found: {}", resource);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return licensePropertiesForModule;
    }

    public Properties getMappingRules() {
        return mappingRules;
    }

    public void setMappingRules(Properties mappingRules) {
        this.mappingRules = mappingRules;
    }
}

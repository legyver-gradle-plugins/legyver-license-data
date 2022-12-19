package com.legyver.gradle.legyverlicensedata;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

/**
 *
 * <code>
 *     legyverLicenseData {
 *         targetFile: 'src\main\resources\com\company\project\license.properties'
 *         applyV2LicenseDataForModules: [
 *              'org.apache.logging.log4j',
 *              'org.apache.commons.lang3',
 *              'com.legyver.core',
 *              'com.legyver.utils'
 *         ]
 *     }
 * </code>
 */
public interface LegyverLicenseDataPluginExtension {
    Property<String> getTargetFile();
    ListProperty<String> getApplyV2LicenseDataForModules();
}

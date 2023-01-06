# legyver-license-data
Apply Legyver license data to a specified properties file from internal values.

## Currently tracked modules
- All Legyver libraries
- com.fasterxml.jackson.databind
- com.fasterxml.jackson.datatype
- com.jayway.jsonpath
- org.apache.commons.io
- org.apache.commons.lang3
- org.apache.logging.log4j
- MaterialFX

## Usage
```groovy
apply plugin: 'com.legyver.legyver-license-data'

legyverLicenseData {
    targetFile = 'path/to/license/service/license.properties'
    //below must be globally-unique module names to avoid conflicts.
    //however, in the case where they share a license location (for example a project may have many subprojects)
    //in that case, you can use the common prefix for the projects modules. 
    applyV2LicenseDataForModules = [
            'com.legyver.core',//an explicit module
            'com.legyver.utils'//a project with many sub-projects/modules
    ]
}
```

```console
:$ gradlew license
```

## Best practices
- Only declare license data for modules you explicitly define in your dependencies.  Transitive library dependencies should be communicated by transitivity.  Otherwise, this quickly becomes unmanageable.
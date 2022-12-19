package com.legyver.gradle.legyverlicensedata.data;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ElasticRuleMatcher {
    private static final Logger logger = Logging.getLogger(ElasticRuleMatcher.class);

    private final Properties mappingProperties;

    public ElasticRuleMatcher(Properties mappingProperties) {
        this.mappingProperties = mappingProperties;
    }

    /**
     * Get the list of possible module names to try sorted by decreasing strictness
     * @return
     */
    public List<String> getMatchingRules(String moduleName) {
        List<String> matchingRules = new ArrayList<>();
        matchingRules.add(moduleName);//always try the whole module name first
        String[] nameParts = moduleName.split("\\.");
        for (int i = nameParts.length; i > 0; i--) {
            StringJoiner stringJoiner = new StringJoiner(".");
            for (int j = 0; j < i; j++) {
                stringJoiner.add(nameParts[j]);
            }
            String lookup = stringJoiner.toString();

            List<String> possibleKeys = mappingProperties.stringPropertyNames().stream()
                    .filter(key -> key.startsWith(lookup))
                    .collect(Collectors.toList());
            for (String key : possibleKeys) {
                String rule = mappingProperties.getProperty(key);
                Pattern pattern = Pattern.compile(rule);
                Matcher matcher = pattern.matcher(moduleName);
                if (matcher.matches() && !matchingRules.contains(key)) {
                    matchingRules.add(key);
                }
            }
        }
        if (logger.isDebugEnabled()) {
            String conjoined = matchingRules.stream().collect(Collectors.joining(", "));
            logger.debug("Matching modules: [{}]", conjoined);
        }
        return matchingRules;
    }
}

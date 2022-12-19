package com.legyver.gradle.legyverlicensedata.data;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class ElasticRuleMatcherTest {

    @Test
    public void resolveComCompanyProject() {
        Properties properties = new Properties();
        properties.put("com.legyver.utils", "^com.legyver.utils.*");

        ElasticRuleMatcher elasticRuleMatcher = new ElasticRuleMatcher(properties);

        {
            List<String> matchingRules = elasticRuleMatcher.getMatchingRules("com.legyver.utils.subproject");
            assertThat(matchingRules).containsExactly(
                    "com.legyver.utils.subproject",
                    "com.legyver.utils"
            );
        }
        {
            List<String> matchingRules = elasticRuleMatcher.getMatchingRules("com.legyver.utils");
            assertThat(matchingRules).containsExactly(
                    "com.legyver.utils"
            );
        }

    }

    @Test
    public void resolveComCompany() {
        Properties properties = new Properties();
        properties.put("com.legyver", "^com.legyver.*");

        ElasticRuleMatcher elasticRuleMatcher = new ElasticRuleMatcher(properties);

        {
            List<String> matchingRules = elasticRuleMatcher.getMatchingRules("com.legyver.utils.subproject");
            assertThat(matchingRules).containsExactly(
                    "com.legyver.utils.subproject",
                    "com.legyver"
            );
        }
        {
            List<String> matchingRules = elasticRuleMatcher.getMatchingRules("com.legyver.fenxlib.subproject");
            assertThat(matchingRules).containsExactly(
                    "com.legyver.fenxlib.subproject",
                    "com.legyver"
            );
        }

        {
            List<String> matchingRules = elasticRuleMatcher.getMatchingRules("com.legyver.fenxlib");
            assertThat(matchingRules).containsExactly(
                    "com.legyver.fenxlib",
                    "com.legyver"
            );
        }
        {
            List<String> matchingRules = elasticRuleMatcher.getMatchingRules("com.legyver");
            assertThat(matchingRules).containsExactly(
                    "com.legyver"
            );
        }
    }


}

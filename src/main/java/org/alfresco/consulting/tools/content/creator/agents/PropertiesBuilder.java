package org.alfresco.consulting.tools.content.creator.agents;

import java.util.List;
import java.util.Properties;

/**
 * Builder class to build a Properties set
 */
public class PropertiesBuilder {

    private String type;
    private String name;
    private String title;
    private String creator;
    private String description;
    private String gradeTo;
    private String gradeFrom;
    private List<String> aspects;

    public PropertiesBuilder() {
    }

    public PropertiesBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public PropertiesBuilder withName(String cmtIntermediaryUniqueId) {
        this.name = cmtIntermediaryUniqueId;
        return this;
    }

    public PropertiesBuilder withTitle(String subject) {
        this.title = subject;
        return this;
    }

    public PropertiesBuilder withCreator(String code) {
        this.creator = code;
        return this;
    }

    public PropertiesBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public PropertiesBuilder withGradeTo(String alfrescoContentId) {
        this.gradeTo = alfrescoContentId;
        return this;
    }

    public PropertiesBuilder withGradeFrom(String cmtContentId) {
        this.gradeFrom = cmtContentId;
        return this;
    }

    public PropertiesBuilder withAspects(List<String> aspects) {
        this.aspects = aspects;
        return this;
    }

    public Properties build() {
        final Properties p = new Properties();
        p.put("type", this.type);
        p.put("cm:name", this.name);
        if (notEmpty(this.title)) {
            p.put("cm:title", this.title);
        }
        if (notEmpty(this.description)) {
            p.put("cm:description", this.description);
        }
        if (notEmpty(this.creator)) {
            p.put("cm:creator", this.creator);
        }
        if (notEmpty(this.gradeFrom)) {
            p.put("cpnals:gradeFrom", this.gradeFrom);
        }
        if (notEmpty(this.gradeTo)) {
            p.put("cpnals:gradeTo", this.gradeTo);
        }
        if (this.aspects != null) {
            // TODO join with commas p.put("aspects", this.aspects);
        }
        return p;
    }

    private boolean notEmpty(final String s) {
        return s != null && s.length() != 0;
    }
}

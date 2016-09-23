package org.alfresco.consulting.tools.content.creator.agents;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import org.alfresco.consulting.tools.content.creator.BulkImportManifestCreator;
import org.alfresco.consulting.words.RandomWords;

/**
 * Create metadata files in-place for existing folder structures, making them importable via BFSIT.<p>
 * P-NALS specific at the moment.
 */
public class PNalsAgent extends AbstractAgent implements Runnable {

    // misusing images_location for that
    protected static String treeSource;

    private static final String courseName = "PearsonCourseSmall";

    public enum NalsNodeType {
        COURSE("cpnals:course"),
        SEQUENCE_OBJECT("cpnals:sequenceObject"),
        LEARNING_BUNDLE("cpnals:learningBundle"),
        CONTENT_OBJECT("cpnals:contentObject"),
        COMPOSITE_OBJECT("cpnals:compositeObject"),
        CONTAINER("cpnals:container"),
        CM_CONTENT("cm:content"),
        ASSET("cpnals:asset");

        String value;

        private NalsNodeType(final String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }
    }

    public PNalsAgent(final String _files_deployment_location, final String _treeSource, final Properties _properties) {
        super(_files_deployment_location, null, _properties);
        treeSource = _treeSource;
    }

    public PNalsAgent(final String _max_files_per_folder, final String _files_deployment_location, final String _treeSource, final Properties _properties) {
        super(_max_files_per_folder, _files_deployment_location, null, _properties);
        treeSource = _treeSource;
    }

    @Override
    public void run() {
        RandomWords.init();

        try {
            final File sourceFolder = new File(files_deployment_location);
            processFolder(sourceFolder, NalsNodeType.COURSE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<NalsNodeType, Integer> getChildrenForType(final NalsNodeType type) {
        final Map<NalsNodeType, Integer> objects = new HashMap<>();
        switch(type) {
        case COURSE :
            objects.put(NalsNodeType.SEQUENCE_OBJECT, Integer.valueOf(10));
            break;
        case SEQUENCE_OBJECT :
            objects.put(NalsNodeType.LEARNING_BUNDLE, Integer.valueOf(8));
            break;
        case LEARNING_BUNDLE :
            objects.put(NalsNodeType.CONTENT_OBJECT, Integer.valueOf(16));
            objects.put(NalsNodeType.COMPOSITE_OBJECT, Integer.valueOf(4));
            break;
        case COMPOSITE_OBJECT :
            objects.put(NalsNodeType.CM_CONTENT, Integer.valueOf(2));
            break;
        case CONTENT_OBJECT :
            objects.put(NalsNodeType.ASSET, Integer.valueOf(1));
            break;
        case ASSET :
            break;
        default:
            break;
        }
        return objects;
    }

    protected void processFolder(final File folder, final NalsNodeType type) {
        File item = processItem(folder, type);
        final Map<NalsNodeType, Integer> childrenOfType = getChildrenForType(type);
        for (final NalsNodeType childType : childrenOfType.keySet()) {
            final Integer count = childrenOfType.get(childType);
            System.out.println("processFolder() " + childType + " = " + count);
            try {
                for (int i = 0; count != null && i < count; i++) {
                    processFolder(item, childType);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("processFolder() completed " + type);
    }

    private File processItem(final File parent, final NalsNodeType type) {
        final String randomStr = RandomWords.getWords(2);
        final String itemName = (NalsNodeType.COURSE.equals(type)) ? courseName : 
            randomStr.replaceAll("/", "-").replaceAll("\\.", "-");
        File item = null;
        if (NalsNodeType.ASSET.equals(type) || NalsNodeType.CM_CONTENT.equals(type)) {
            item = populateRandomFile(parent, itemName, type);
            createMetadataFile(item, type);
        } else {
            item = new File(parent.getAbsolutePath() + "/" + itemName);
            item.mkdirs();
            createMetadataFile(item, type);
        }
        return item;
    }

    private void createMetadataFile(final File sourceFolder, final NalsNodeType type) {
        final Properties props = buildProperties(sourceFolder, type);
        props.put("cpnals:discipline", "Science");
        switch(type) {
        case COURSE :
            props.put("cm:name", courseName);
            props.put("cm:title", courseName + " Title");
            props.put("cpnals:courseAbbreviation", sourceFolder.getName().substring(0, 5).toUpperCase());
            props.put("cpnals:productType", "Big Book/Big Shared Book");
            break;
        case SEQUENCE_OBJECT :
            props.put("cpnals:cmtContentID", UUID.randomUUID().toString());
            props.put("cpnals:mediaType", "Lesson");
            props.put("cpnals:keywords", RandomWords.getWords(3));
            break;
        case LEARNING_BUNDLE :
            props.put("cpnals:cmtContentID", UUID.randomUUID().toString());
            props.put("cpnals:keywords", RandomWords.getWords(4));
            props.put("cpnals:versionDistrict", RandomWords.getWords(1));
            props.put("cpnals:simplifiedDescription", RandomWords.getWords(1));
            break;
        case COMPOSITE_OBJECT :
            props.put("cpnals:cmtContentID", UUID.randomUUID().toString());
            props.put("cpnals:mediaType", "Lesson");
            props.put("cpnals:keywords", RandomWords.getWords(2));
            props.put("cpnals:productType", "Online Course");
            props.put("cpnals:realizeFileType", "JPG");
            props.put("cpnals:contentType", "Lab");
            props.put("cpnals:simplifiedDescription", "Simple");
            break;
        case CONTENT_OBJECT :
            props.put("cpnals:cmtContentID", UUID.randomUUID().toString());
            props.put("cpnals:mediaType", "Link");
            props.put("cpnals:keywords", RandomWords.getWords(1));
            props.put("cpnals:contentType", "Various Media");
            props.put("cpnals:simplifiedDescription", "CO");
            break;
        case CONTAINER :
            props.put("cpnals:containerType", "Learning Model");
            props.put("cpnals:productType", "Big Book/Big Shared Book");
            break;
        case ASSET :
            // add extra props for assets
            boolean hideFromStudent = Math.random() > 0.5;
            props.put("cpnals:hideFromStudent", String.valueOf(hideFromStudent));
            props.put("cpnals:teacherOnly", String.valueOf(hideFromStudent));
            props.put("cpnals:copyrightYear", "2016");
            props.put("cpnals:productType", "Little Shared Book");
            break;
        default:
            break;
        }
        BulkImportManifestCreator.createBulkManifest(sourceFolder.getName(), sourceFolder.getParent(), props);
    }

    private File populateRandomFile(final File targetFolder, final String fileName, final NalsNodeType nodeType) {
        try {
            File imagesFolder = new File(treeSource);
            File[] files = imagesFolder.listFiles();
            int size = files.length;
            Random rand = new Random();
            int number = rand.nextInt(size);
            final File randomFile = files[number];
            final String targetFileName = fileName + "." + getExtension(randomFile);
            final File targetFile = new File(targetFolder.getAbsolutePath() + "/" + targetFileName);
            createMetadataFile(targetFile, nodeType);
            copyFile(targetFile, randomFile);
            return targetFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getExtension(final File file) {
        //System.out.println(file.getName());
        final String[] parts = file.getName().split("\\.");
        return parts[parts.length - 1].toLowerCase();
    }

    protected Properties buildProperties(final File file, final NalsNodeType type) {
        int gradeTo = Double.valueOf(Math.round(Math.random() * 12)).intValue();
        int gradeDiff = Double.valueOf(Math.round(Math.random() * gradeTo)).intValue();
        int gradeFrom = Math.max(1, gradeTo - gradeDiff);
        PropertiesBuilder propsBuilder = new PropertiesBuilder()
                .withType(type.value)
                .withName(file.getName())
                .withTitle(file.getName() + " Title")
                .withDescription(RandomWords.getWords(2) + " Description")
                .withCreator("admin");
        if (!NalsNodeType.CM_CONTENT.equals(type)) {
            propsBuilder = propsBuilder
                    .withGradeFrom(String.valueOf(gradeFrom))
                    .withGradeTo(String.valueOf(Math.max(gradeFrom, gradeTo)));
        }
        return propsBuilder.build();
    }

}

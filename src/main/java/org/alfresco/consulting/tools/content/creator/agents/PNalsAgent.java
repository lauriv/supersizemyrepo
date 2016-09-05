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

    protected static final NalsNodeType[] objectTypeHierarchy = {NalsNodeType.COURSE, NalsNodeType.SEQUENCE_OBJECT, 
            NalsNodeType.LEARNING_BUNDLE, NalsNodeType.COMPOSITE_OBJECT, NalsNodeType.ASSET};
    // misusing images_location for that
    protected static String treeSource;

    private Map<NalsNodeType, Integer> objects = new HashMap<>();

    public enum NalsNodeType {
        COURSE("cpnals:course"),
        SEQUENCE_OBJECT("cpnals:sequenceObject"),
        LEARNING_BUNDLE("cpnals:learningBundle"),
        COMPOSITE_OBJECT("cpnals:compositeObject"),
        CONTAINER("cpnals:container"),
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
        initMap();
    }

    public PNalsAgent(final String _max_files_per_folder, final String _files_deployment_location, final String _treeSource, final Properties _properties) {
        super(_max_files_per_folder, _files_deployment_location, null, _properties);
        treeSource = _treeSource;
        initMap();
    }

    private void initMap() {
        // 15000 items = 1 course x 50 x 30 x 10
        // 60000 items = 1 course x 50 x 30 x 40 ?
        objects.put(NalsNodeType.COURSE, Integer.valueOf(1));
        objects.put(NalsNodeType.SEQUENCE_OBJECT, Integer.valueOf(50));
        objects.put(NalsNodeType.LEARNING_BUNDLE, Integer.valueOf(30));
        //objects.put(NalsNodeType.COMPOSITE_OBJECT, Integer.valueOf(20));
        objects.put(NalsNodeType.ASSET, Integer.valueOf(40));
    }

    @Override
    public void run() {
        RandomWords.init();

        try {
            final File sourceFolder = new File(files_deployment_location);
            processFolder(sourceFolder, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void processFolder(final File folder, final int level) {
        if (level > objectTypeHierarchy.length) return;
        final NalsNodeType containerType = objectTypeHierarchy[Math.min(level, objectTypeHierarchy.length - 1)];
        final Integer count = objects.get(containerType);
        System.out.println("processFolder() " + containerType + " = " + count);
        try {
            if (count == null) {
                processFolder(folder, level + 1);
            } else {
                for (int i = 0; i < count; i++) {
                    final String courseName = RandomWords.getWords(2);
                    final String cleanName = courseName.replaceAll("/", "-").replaceAll("\\.", "-");
                    if (!NalsNodeType.ASSET.equals(containerType)) {
                        final File courseFolder = new File(folder.getAbsolutePath() + "/" + cleanName);
                        courseFolder.mkdirs();
                        createMetadataFile(courseFolder, containerType);
                        processFolder(courseFolder, level + 1);
                    } else {
                        populateRandomFile(folder, cleanName, containerType);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createMetadataFile(final File sourceFolder, final NalsNodeType type) {
        final Properties props = buildProperties(sourceFolder, type);
        props.put("cpnals:discipline", "Science");
        switch(type) {
        case COURSE :
            props.put("cm:name", "Course60000");
            props.put("cm:title", "Course60000 Title");
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

    private void populateRandomFile(final File targetFolder, final String fileName, final NalsNodeType nodeType) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                .withCreator("admin")
                .withGradeFrom(String.valueOf(gradeFrom))
                .withGradeTo(String.valueOf(Math.max(gradeFrom, gradeTo)));
        return propsBuilder.build();
    }

}

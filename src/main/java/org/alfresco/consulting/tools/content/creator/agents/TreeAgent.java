package org.alfresco.consulting.tools.content.creator.agents;

import java.io.File;
import java.util.Properties;
import java.util.UUID;

import org.alfresco.consulting.tools.content.creator.BulkImportManifestCreator;
import org.alfresco.consulting.words.RandomWords;

/**
 * Create metadata files in-place for existing folder structures, making them importable via BFSIT.<p>
 * Pearson-NALS specific at the moment.
 */
public class TreeAgent extends AbstractAgent implements Runnable {

    protected static final NalsNodeType[] objectTypeHierarchy = {NalsNodeType.COURSE, NalsNodeType.SEQUENCE_OBJECT, NalsNodeType.CONTAINER};
    // misusing images_location for that
    protected static String treeSource;

    public enum NalsNodeType {
        COURSE("cpnals:course"),
        SEQUENCE_OBJECT("cpnals:sequenceObject"),
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

    public TreeAgent(final String _files_deployment_location, final String _treeSource, final Properties _properties) {
        super(_files_deployment_location, null, _properties);
        treeSource = _treeSource;
    }

    public TreeAgent(final String _max_files_per_folder,final String _files_deployment_location, final String _treeSource, final Properties _properties) {
        super(_max_files_per_folder, _files_deployment_location, null, _properties);
        treeSource = _treeSource;
    }

    @Override
    public void run() {
        RandomWords.init();

        try {
            final File sourceFolder = new File(treeSource);
            for (final File file : sourceFolder.listFiles()) {
                if (file.isDirectory()) {
                    processFolder(file.getAbsolutePath(), 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void processFolder(final String sourcePath, final int level) {
        final NalsNodeType containerType = objectTypeHierarchy[Math.min(level, objectTypeHierarchy.length - 1)];
        System.out.println("processFolder() " + sourcePath + " as " + containerType);
        try {
            final File sourceFolder = new File(sourcePath);
            createMetadataFileForFolder(sourceFolder, containerType);

            for (final File file : sourceFolder.listFiles()) {
                if (file.isFile()) {
                    if (!file.isHidden()){ 
                        createMetadataFile(file, NalsNodeType.ASSET);
                    }
                } else {
                    //boolean success = (new File(files_deployment_location + "/" + file.getName())).mkdirs();
                    processFolder(file.getAbsolutePath(), level + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createMetadataFileForFolder(final File sourceFolder, final NalsNodeType type) {
        final Properties props = buildProperties(sourceFolder, type);
        props.put("cpnals:discipline", "Science");
        switch(type) {
        case COURSE :
            props.put("cpnals:courseAbbreviation", sourceFolder.getName().substring(0, 5).toUpperCase());
            props.put("cpnals:productType", "Big Book/Big Shared Book");
            break;
        case SEQUENCE_OBJECT :
            props.put("cpnals:cmtContentID", UUID.randomUUID().toString());
            props.put("cpnals:mediaType", "Lesson");
            props.put("cpnals:keywords", RandomWords.getWords(3));
            break;
        case CONTAINER :
            props.put("cpnals:containerType", "Learning Model");
            props.put("cpnals:productType", "Big Book/Big Shared Book");
            break;
        default:
            break;
        }
        BulkImportManifestCreator.createBulkManifest(sourceFolder.getName(), sourceFolder.getParent(), props);
    }

    private void createMetadataFile(final File file, final NalsNodeType type) {
        Properties fileProps = buildProperties(file, type);
        // add extra props for assets
        boolean hideFromStudent = Math.random() > 0.5;
        fileProps.put("cpnals:hideFromStudent", String.valueOf(hideFromStudent));
        fileProps.put("cpnals:teacherOnly", String.valueOf(hideFromStudent));
        fileProps.put("cpnals:copyrightYear", "2016");
        fileProps.put("cpnals:productType", "Little Shared Book");

        BulkImportManifestCreator.createBulkManifest(file.getName(), file.getParent(), fileProps);
        /*
        try {
            final InputStream is = new FileInputStream(file);
            final FileOutputStream out = new FileOutputStream(files_deployment_location + "/" + file.getName());
            BulkImportManifestCreator.createBulkManifest(file.getName(), files_deployment_location, fileProps);
            IOUtils.copy(is,out);
            is.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // close streams
        }*/
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

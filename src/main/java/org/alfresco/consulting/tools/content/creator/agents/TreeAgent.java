package org.alfresco.consulting.tools.content.creator.agents;

import java.io.File;
import java.util.Properties;

import org.alfresco.consulting.tools.content.creator.BulkImportManifestCreator;
import org.alfresco.consulting.words.RandomWords;

/**
 * Create metadata files in-place for existing folder structures, making them importable 
 */
public class TreeAgent extends AbstractAgent implements Runnable {

    protected static String treeSource;

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

        processFolder(treeSource, "cpnals:course");
    }

    protected void processFolder(final String sourcePath, final String containerType) {
        System.out.println("processFolder() " + sourcePath);
        try {
            final File sourceFolder = new File(sourcePath);
            createMetadataFileForFolder(sourceFolder, containerType);

            for (final File file : sourceFolder.listFiles()) {
                if (file.isFile()) {
                    createMetadataFile(file);
                } else {
                    // TODO at this moment we handle all the folders as a Sequence Object...? create and build the subdir...
                    //boolean success = (new File(files_deployment_location + "/" + file.getName())).mkdirs();
                    processFolder(file.getAbsolutePath(), "cpnals:sequenceObject");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void createMetadataFileForFolder(final File sourceFolder, final String type) {
        PropertiesBuilder folderPropsBuilder = new PropertiesBuilder()
                .withType(type)
                .withName(sourceFolder.getName())
                .withTitle(sourceFolder.getName() + " Title")
                .withDescription(sourceFolder.getName() + " Description")
                .withCreator("Lauri")
                .withGradeFrom("2")
                .withGradeTo("7");
        BulkImportManifestCreator.createBulkManifest(sourceFolder.getName(), sourceFolder.getParent(), folderPropsBuilder.build());
    }

    private void createMetadataFile(final File file) {
        final PropertiesBuilder filePropsBuilder = new PropertiesBuilder()
                .withType("cpnals:asset")
                .withName(file.getName())
                .withTitle(file.getName() + " Title")
                .withDescription(RandomWords.getWords(2))
                .withCreator("Lauri")
                .withGradeFrom("3")
                .withGradeTo("5");
        final Properties fileProps = filePropsBuilder.build();
        // add extra props for assets
        fileProps.put("cpnals:hideFromStudent", "false");
        fileProps.put("cpnals:teacherOnly", "false");
        fileProps.put("cpnals:copyrightYear", "2016");

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

}

package org.alfresco.consulting.tools.content.creator.agents;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Properties;
import java.util.Random;

import org.alfresco.consulting.tools.content.creator.BulkImportManifestCreator;
import org.alfresco.consulting.words.RandomWords;
import org.apache.poi.util.IOUtils;

public class JpgAgent extends Thread implements Runnable {
    /**
     * @param args
     * @throws java.io.IOException
     */

    private static final int maxLevels = 10;
    private static volatile int levelDeep = 0;
    private static String originalFilesDeploymentLocation;
    private static String files_deployment_location;
    private static String images_location;
    private static String max_files_per_folder="40";   // defaults to 40, but can be a parameter of the constructor
    private static Properties properties;

    public JpgAgent(final String _files_deployment_location, final String _images_location, final Properties _properties) {
        this.originalFilesDeploymentLocation = _files_deployment_location;
        this.files_deployment_location = _files_deployment_location;
        this.images_location = _images_location;
        this.properties = _properties;
    }

    public JpgAgent(final String _max_files_per_folder,final String _files_deployment_location, final String _images_location, final Properties _properties) {
        this.originalFilesDeploymentLocation = _files_deployment_location;
        this.files_deployment_location = _files_deployment_location;
        this.images_location = _images_location;
        this.properties = _properties;
        this.max_files_per_folder = _max_files_per_folder;
    }


    @Override
    public void run() {

        RandomWords.init();
        Calendar cal = Calendar.getInstance();

        try {
            File imagesFolder = new File(images_location);
            File[] files = imagesFolder.listFiles();
            int size = files.length;
            Random rand = new Random();
            int number = rand.nextInt(size);
            File randomImage = files[number];
            //InputStream is =new URL("http://lorempixel.com/g/800/600/").openStream();
            InputStream is = new FileInputStream(randomImage);
            String fileName =  cal.getTimeInMillis() +"_JpegImageSSMR.jpg";
            try {
                File deploymentFolder = new File(files_deployment_location);
                File[] deploymentfiles =   deploymentFolder.listFiles();
                int total_deployment_size = deploymentfiles.length;
                FileOutputStream out = null;
                // checking if the deployment location is full (more than max_files_per_folder files)
                if (total_deployment_size>Integer.valueOf(max_files_per_folder)) {
                    this.files_deployment_location = createDir(files_deployment_location);
                    levelDeep++;
                    if (levelDeep > maxLevels) {
                        this.files_deployment_location = createDir(originalFilesDeploymentLocation);
                        levelDeep = 1;
                    }
                    out = new FileOutputStream(files_deployment_location + "/" + fileName);
                    BulkImportManifestCreator.createBulkManifest(fileName,files_deployment_location, properties);
                } else {
                    out = new FileOutputStream(files_deployment_location + "/" + fileName);
                    BulkImportManifestCreator.createBulkManifest(fileName,files_deployment_location, properties);
                }
                IOUtils.copy(is,out);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.out.println("First Catch");
            e.printStackTrace();
        }

    }

    private String createDir(final String deploymentLocation) {
        Calendar calendar = Calendar.getInstance();
        String dir_name = deploymentLocation + "/" + calendar.getTimeInMillis();
        boolean success = (new File(dir_name)).mkdirs();
        if (!success) {
            System.out.println("JPG - Failed to create directory " + dir_name );
            if (new File(dir_name).exists()) {
                System.out.println("JPG - Directory already exists " + dir_name );
            } else {
                System.out.println("JPG - Could not create directory, we will die " + dir_name );
            }
        }
        return dir_name;
    }

}

package org.alfresco.consulting.tools.content.creator.agents;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Properties;

import org.alfresco.consulting.tools.content.creator.BulkImportManifestCreator;
import org.apache.poi.util.IOUtils;

public abstract class AbstractAgent extends Thread {

    protected static final int MAX_LEVELS = 10;
    protected static String files_deployment_location;
    protected static String images_location;
    protected static String max_files_per_folder = "40";
    protected static volatile int levelDeep = 0;
    protected static String originalFilesDeploymentLocation;
    protected static Properties properties;

    public AbstractAgent(final String _max_files_per_folder, final String _files_deployment_location, final String _images_location, final Properties _properties) {
        this(_files_deployment_location, _images_location, _properties);
        max_files_per_folder = _max_files_per_folder;
    }

    public AbstractAgent(final String _files_deployment_location, final String _images_location, final Properties _properties) {
        originalFilesDeploymentLocation = _files_deployment_location;
        files_deployment_location = _files_deployment_location;
        images_location = _images_location;
        properties = _properties;
    }

    /*private static int findNumberOfFiles(final String dir, final String ext) {
        File file = new File(dir);
        if(!file.exists()) {
            System.out.println(dir + " Directory doesn't exists");
        }
        File[] listFiles = file.listFiles(new MyFileNameFilter(ext));
        if(listFiles.length ==0){
            System.out.println(dir + "doesn't have any file with extension "+ext);
            return 0;
        }else{
            for(File f : listFiles) {
                System.out.println("File: "+dir+File.separator+f.getName());
            }
            return listFiles.length;
        }
    }

    //FileNameFilter implementation
    public static class MyFileNameFilter implements FilenameFilter{

        private final String ext;

        public MyFileNameFilter(final String ext){
            this.ext = ext.toLowerCase();
        }
        @Override
        public boolean accept(final File dir, final String name) {
            return name.toLowerCase().endsWith(ext);
        }

    }*/

    protected String createNewSubDir(final String deploymentLocation) {
        Calendar calendar = Calendar.getInstance();
        String dir_name = deploymentLocation + "/" + calendar.getTimeInMillis();
        boolean success = (new File(dir_name)).mkdirs();
        if (!success) {
            System.out.println("Failed to create directory " + dir_name );
            if (new File(dir_name).exists()) {
                System.out.println("Directory already exists " + dir_name );
            } else {
                System.out.println("Could not create directory, we will die " + dir_name );
            }
        }
        return dir_name;
    }

    protected void changeDeploymentLocation(final String newLocation) {
        files_deployment_location = createNewSubDir(newLocation);
        levelDeep++;
        if (levelDeep > MAX_LEVELS) {
            files_deployment_location = createNewSubDir(originalFilesDeploymentLocation);
            levelDeep = 1;
        }
    }

    protected OutputStream getWritableFileStream(final String fileName) {
        FileOutputStream out = null;
        try {
            File deploymentFolder = new File(files_deployment_location);
            File[] deploymentfiles = deploymentFolder.listFiles();
            int total_deployment_size = deploymentfiles.length;
            // checking if the deployment location is full (more than max_files_per_folder files)
            if (total_deployment_size > Integer.valueOf(max_files_per_folder)) {
                files_deployment_location = createNewSubDir(files_deployment_location);
                levelDeep++;
                if (levelDeep > MAX_LEVELS) {
                    files_deployment_location = createNewSubDir(originalFilesDeploymentLocation);
                    levelDeep = 1;
                }
                out = new FileOutputStream(files_deployment_location + "/" + fileName);
                BulkImportManifestCreator.createBulkManifest(fileName,files_deployment_location, properties);
            } else {
                out = new FileOutputStream(files_deployment_location + "/" + fileName);
                BulkImportManifestCreator.createBulkManifest(fileName,files_deployment_location, properties);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return out;
    }

    protected void copyFile(final String fileName, final File originalFile) {
        try {
            InputStream is = new FileInputStream(originalFile);
            OutputStream out = getWritableFileStream(fileName);
            IOUtils.copy(is,out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

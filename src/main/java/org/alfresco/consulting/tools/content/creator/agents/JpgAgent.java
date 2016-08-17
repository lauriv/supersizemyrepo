package org.alfresco.consulting.tools.content.creator.agents;

import java.io.File;
import java.util.Calendar;
import java.util.Properties;
import java.util.Random;

import org.alfresco.consulting.words.RandomWords;

public class JpgAgent extends AbstractAgent implements Runnable {

    public JpgAgent(final String _files_deployment_location, final String _images_location, final Properties _properties) {
        super(_files_deployment_location, _images_location, _properties);
    }

    public JpgAgent(final String _max_files_per_folder,final String _files_deployment_location, final String _images_location, final Properties _properties) {
        super(_max_files_per_folder, _files_deployment_location, _images_location, _properties);
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
            String fileName = cal.getTimeInMillis() +"_JpegImageSSMR.jpg";

            copyFile(fileName, randomImage);
        } catch (Exception e) {
            System.out.println("First Catch");
            e.printStackTrace();
        }

    }

}

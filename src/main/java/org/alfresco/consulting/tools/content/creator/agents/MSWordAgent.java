package org.alfresco.consulting.tools.content.creator.agents;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.Random;

import org.alfresco.consulting.tools.content.creator.BulkImportManifestCreator;
import org.alfresco.consulting.tools.content.creator.CustomXWPFDocument;
import org.alfresco.consulting.words.RandomWords;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.BreakClear;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.LineSpacingRule;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

public class MSWordAgent extends Thread implements Runnable {

    private static final int maxLevels = 10;
    private static volatile int levelDeep = 0;
    private static String originalFilesDeploymentLocation;
    private static String files_deployment_location;
    private static String images_location;
    private static Properties properties;
    private static String max_files_per_folder="40";   // defaults to 40, but can be a parameter of the constructor

    public MSWordAgent(final String _files_deployment_location, final String _images_location, final Properties _properties) {
        this.originalFilesDeploymentLocation = _files_deployment_location;
        this.files_deployment_location = _files_deployment_location;
        this.images_location = _images_location;
        this.properties = _properties;
    }

    public MSWordAgent(final String _max_files_per_folder,final String _files_deployment_location, final String _images_location, final Properties _properties) {
        this.originalFilesDeploymentLocation = _files_deployment_location;
        this.files_deployment_location = _files_deployment_location;
        this.images_location = _images_location;
        this.properties = _properties;
        this.max_files_per_folder = _max_files_per_folder;
    }


    private static int findNumberOfFiles(final String dir, final String ext) {
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

    }

    @Override
    public void run(){

        RandomWords.init();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        // Create a document file
        CustomXWPFDocument document = null;
        try {
            document = new CustomXWPFDocument();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // insert doc details
        // Createa a para -1
        XWPFParagraph paragraphOne = document.createParagraph();
        paragraphOne.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun paragraphOneRunOne = paragraphOne.createRun();
        paragraphOneRunOne.setBold(true);
        paragraphOneRunOne.setFontSize(20);
        paragraphOneRunOne.setFontFamily("Verdana");
        paragraphOneRunOne.setColor("000070");
        paragraphOneRunOne.setText("Daily Status Report");
        // Createa a para -2
        XWPFParagraph paragraphTwo = document.createParagraph();
        paragraphTwo.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun paragraphTwoRunOne = paragraphTwo.createRun();
        paragraphTwoRunOne.setFontSize(12);
        paragraphTwoRunOne.setFontFamily("Verdana");
        paragraphTwoRunOne.setColor("000070");
        paragraphTwoRunOne.setText(date);
        paragraphTwoRunOne.addBreak();
        // Createa a para -3
        XWPFParagraph paragraphThree = document.createParagraph();
        paragraphThree.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun paragraphThreeRunOne = paragraphThree.createRun();
        paragraphThreeRunOne.setFontSize(14);
        paragraphThreeRunOne.setFontFamily("Verdana");
        paragraphThreeRunOne.setColor("000070");

        paragraphThreeRunOne.setText(date);
        paragraphThreeRunOne.addBreak();
        // Createa a para -4
        XWPFParagraph paragraphFour = document.createParagraph();
        paragraphFour.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun paragraphFourRunOne = paragraphFour.createRun();
        paragraphFourRunOne.setBold(true);
        paragraphFourRunOne.setUnderline(UnderlinePatterns.SINGLE);
        paragraphFourRunOne.setFontSize(10);
        paragraphFourRunOne.setFontFamily("Verdana");
        paragraphFourRunOne.setColor("000070");
        paragraphFourRunOne.setText("Benchmark Document with Random Words");
        // insert doc details end
        XWPFParagraph paragraphFive = document.createParagraph();
        paragraphFive.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun paragraphFiveRunOne = paragraphFive.createRun();
        paragraphFiveRunOne.addBreak();
        paragraphFourRunOne.setBold(true);
        paragraphFourRunOne.setUnderline(UnderlinePatterns.SINGLE);
        paragraphFourRunOne.setFontSize(10);
        paragraphFourRunOne.setFontFamily("Verdana");
        paragraphFourRunOne.setColor("000070");
        paragraphFourRunOne.setText(" Created with SuperSizeMyRepo");

        XWPFParagraph paragraphSix = document.createParagraph();
        paragraphFive.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun paragraphSixRunOne = paragraphSix.createRun();
        paragraphFiveRunOne.addBreak();
        paragraphFourRunOne.setBold(true);
        paragraphFourRunOne.setText("     ");
        // Adding a file
        try {
            // Working addPicture Code below...
            XWPFParagraph paragraphX = document.createParagraph();
            paragraphX.setAlignment(ParagraphAlignment.CENTER);
            File imagesFolder = new File(images_location);
            File[] files =   imagesFolder.listFiles();
            int size = files.length;
            Random rand = new Random();
            int number = rand.nextInt(size);
            File randomImage = files[number];
            // adding local random image
            String blipId = null;
            blipId = paragraphX.getDocument().addPictureData(new FileInputStream(randomImage), Document.PICTURE_TYPE_JPEG);
            document.createPicture(blipId,document.getNextPicNameNumber(Document.PICTURE_TYPE_JPEG),800, 600);
        } catch (InvalidFormatException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }   catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        XWPFParagraph p3 = document.createParagraph();
        p3.setWordWrap(true);
        p3.setPageBreak(true);
        //p3.setAlignment(ParagraphAlignment.DISTRIBUTE);
        p3.setAlignment(ParagraphAlignment.BOTH);
        p3.setSpacingLineRule(LineSpacingRule.EXACT);

        p3.setIndentationFirstLine(20);
        XWPFRun r4 = p3.createRun();
        r4.setTextPosition(20);
        r4.setText(RandomWords.getWords(6000));
        r4.addBreak(BreakType.PAGE);
        r4.setText(RandomWords.getWords(6000));
        r4.setItalic(true);
        //This would imply that this break shall be treated as a simple line break, and break the line after that word:
        XWPFRun r5 = p3.createRun();
        r5.setTextPosition(-10);
        r5.setText("For what dreams may come");
        r5.addCarriageReturn();
        r5.setText(RandomWords.getWords(4000));
        r5.addBreak();
        r5.setText(RandomWords.getWords(4000));
        r5.addBreak(BreakClear.ALL);
        r5.setText(RandomWords.getWords(4000));

        //        try {
        // Working addPicture Code below...
        XWPFParagraph paragraphY = document.createParagraph();
        paragraphY.setAlignment(ParagraphAlignment.CENTER);
        // adding http image
        //            InputStream is = null;
        //            is = new URL("http://lorempixel.com/g/800/600/").openStream();
        //            String blipIdw = paragraphY.getDocument().addPictureData(is,Document.PICTURE_TYPE_JPEG);
        //            document.createPicture(blipIdw,document.getNextPicNameNumber(Document.PICTURE_TYPE_JPEG),800, 600);
        //        } catch (InvalidFormatException e1) {
        //            // TODO Auto-generated catch block
        //            e1.printStackTrace();
        //        }  catch (IOException e) {
        //            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        //        }
        FileOutputStream outStream = null;
        try {

            String fileName =  cal.getTimeInMillis() +"_MSWordSSMR.docx";
            File deploymentFolder = new File(files_deployment_location);
            File[] deploymentfiles =   deploymentFolder.listFiles();
            int total_deployment_size = deploymentfiles.length;
            Calendar calendar = Calendar.getInstance();
            // checking if the deployment location is full (more than max_files_per_folder files)
            if (total_deployment_size>Integer.valueOf(max_files_per_folder)) {
                String dir_name = files_deployment_location + "/" + calendar.getTimeInMillis();
                boolean success = (new File(dir_name)).mkdirs();
                this.files_deployment_location = dir_name;
                if (!success) {
                    System.out.println("Failed to create directory " + dir_name );
                }
                levelDeep++;
                if (levelDeep > maxLevels) {
                    dir_name = originalFilesDeploymentLocation + "/" + calendar.getTimeInMillis();
                    success = (new File(dir_name)).mkdirs();
                    if (!success) {
                        System.out.println("Word - Failed to create directory 2 " + dir_name );
                    }
                    this.files_deployment_location = dir_name;
                    levelDeep = 1;
                }
                outStream = new FileOutputStream(files_deployment_location + "/" + fileName);
                BulkImportManifestCreator.createBulkManifest(fileName,files_deployment_location, properties);
            } else {
                outStream = new FileOutputStream(files_deployment_location + "/" + fileName);
                BulkImportManifestCreator.createBulkManifest(fileName,files_deployment_location, properties);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            document.write(outStream);
            outStream.close();
        } catch (FileNotFoundException e) {
            System.out.println("Second Catch");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Third Catch");
            e.printStackTrace();
        }

    }

}

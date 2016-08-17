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
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;

public class MSExcelAgent extends AbstractAgent implements Runnable {

    public MSExcelAgent(final String _files_deployment_location, final String _images_location, final Properties _properties) {
        super(_files_deployment_location, _images_location, _properties);
    }

    public MSExcelAgent(final String _max_files_per_folder,final String _files_deployment_location, final String _images_location, final Properties _properties) {
        super(_max_files_per_folder, _files_deployment_location, _images_location, _properties);
    }

    @Override
    public void run(){

        RandomWords.init();
        /* Create a Workbook and Worksheet */
        HSSFWorkbook my_workbook = new HSSFWorkbook();
        HSSFSheet my_sheet = my_workbook.createSheet("SuperSizeMyRepoBanner");
        /* Read the input image into InputStream */

        File imagesFolder = new File(images_location);
        File[] files = imagesFolder.listFiles();
        int size = files.length;
        Random rand = new Random();
        int number = rand.nextInt(size);
        File randomImage = files[number];

        int my_picture_id = 0;
        try {
            InputStream my_banner_image = new FileInputStream(randomImage);
            /* Convert Image to byte array */
            byte[] bytes = IOUtils.toByteArray(my_banner_image);
            /* Add Picture to workbook and get a index for the picture */
            my_picture_id = my_workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);
            /* Close Input Stream */
            my_banner_image.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /* Create the drawing container */
        HSSFPatriarch drawing = my_sheet.createDrawingPatriarch();
        /* Create an anchor point */
        ClientAnchor my_anchor = new HSSFClientAnchor();
        /* Define top left corner, and we can resize picture suitable from there */
        my_anchor.setCol1(2);
        my_anchor.setRow1(1);
        /* Invoke createPicture and pass the anchor point and ID */
        HSSFPicture my_picture = drawing.createPicture(my_anchor, my_picture_id);
        /* Call resize method, which resizes the image */
        my_picture.resize();
        /* Write changes to the workbook */

        // create a new sheet
        Sheet s = my_workbook.createSheet();
        // declare a row object reference
        Row r = null;
        // declare a cell object reference
        Cell c = null;
        // create 3 cell styles
        CellStyle cs = my_workbook.createCellStyle();
        CellStyle cs2 = my_workbook.createCellStyle();
        CellStyle cs3 = my_workbook.createCellStyle();
        DataFormat df = my_workbook.createDataFormat();
        // create 2 fonts objects
        Font f = my_workbook.createFont();
        Font f2 = my_workbook.createFont();

        //set font 1 to 12 point type
        f.setFontHeightInPoints((short) 12);
        //make it blue
        f.setColor( (short)0xc );
        // make it bold
        //arial is the default font
        f.setBoldweight(Font.BOLDWEIGHT_BOLD);

        //set font 2 to 10 point type
        f2.setFontHeightInPoints((short) 10);
        //make it red
        f2.setColor( Font.COLOR_RED );
        //make it bold
        f2.setBoldweight(Font.BOLDWEIGHT_BOLD);

        f2.setStrikeout( true );

        //set cell style
        cs.setFont(f);
        //set the cell format
        cs.setDataFormat(df.getFormat("#,##0.0"));

        //set a thin border
        cs2.setBorderBottom(CellStyle.BORDER_THIN);
        //fill w fg fill color
        cs2.setFillPattern(CellStyle.SOLID_FOREGROUND);
        //set the cell format to text see DataFormat for a full list
        cs2.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));

        // set the font
        cs2.setFont(f2);

        // set the sheet name in Unicode
        my_workbook.setSheetName(0, "SuperSizeMyRepo ");

        // create a sheet with 30 rows (0-29)
        int rownum;
        for (rownum = (short) 0; rownum < 30; rownum++)
        {
            // create a row
            r = s.createRow(rownum);
            // on every other row
            if ((rownum % 2) == 0)
            {
                // make the row height bigger  (in twips - 1/20 of a point)
                r.setHeight((short) 0x249);
            }

            //r.setRowNum(( short ) rownum);
            // create 10 cells (0-9) (the += 2 becomes apparent later
            for (short cellnum = (short) 0; cellnum < 10; cellnum += 2)
            {
                // create a numeric cell
                c = r.createCell(cellnum);
                // do some goofy math to demonstrate decimals
                c.setCellValue((rownum * 10000) + cellnum
                        + (((double) rownum / 1000)
                                + ((double) cellnum / 10000)));

                // create a string cell (see why += 2 in the
                c = r.createCell((short) (cellnum + 1));

                // on every other row
                if ((rownum % 2) == 0)
                {
                    // set this cell to the first cell style we defined
                    c.setCellStyle(cs);
                    // set the cell's string value to "Test"
                    c.setCellValue(RandomWords.getWords(10));
                }
                else
                {
                    c.setCellStyle(cs2);
                    // set the cell's string value to "\u0422\u0435\u0441\u0442"
                    c.setCellValue( RandomWords.getWords(5) );
                }


                // make this column a bit wider
                s.setColumnWidth((short) (cellnum + 1), (short) ((50 * 8) / ((double) 1 / 20)));
            }
        }

        //draw a thick black border on the row at the bottom using BLANKS
        // advance 2 rows
        rownum++;
        rownum++;

        r = s.createRow(rownum);

        // define the third style to be the default
        // except with a thick black border at the bottom
        cs3.setBorderBottom(CellStyle.BORDER_THICK);

        //create 50 cells
        for (short cellnum = (short) 0; cellnum < 50; cellnum++)
        {
            //create a blank type cell (no value)
            c = r.createCell(cellnum);
            // set it to the thick black border style
            c.setCellStyle(cs3);
        }

        //end draw thick black border


        Calendar cal = Calendar.getInstance();
        String fileName =  cal.getTimeInMillis() +"MSExcelSSMR.xls";


        try {
            File deploymentFolder = new File(files_deployment_location);
            File[] deploymentfiles =   deploymentFolder.listFiles();
            int total_deployment_size = deploymentfiles.length;
            Calendar calendar = Calendar.getInstance();
            FileOutputStream out = null;
            // checking if the deployment location is full (more than max_files_per_folder files)
            if (total_deployment_size>Integer.valueOf(max_files_per_folder)) {
                String dir_name = files_deployment_location + "/" + calendar.getTimeInMillis();
                boolean success = (new File(dir_name)).mkdirs();
                this.files_deployment_location = dir_name;
                if (!success) {
                    System.out.println("Excel - Failed to create directory " + dir_name );
                    if (new File(dir_name).exists()) {
                        System.out.println("Excel - Directory already exists " + dir_name );
                    } else {
                        System.out.println("Excel - Could not create directory, we will die " + dir_name );
                    }
                }
                levelDeep++;
                if (levelDeep > MAX_LEVELS) {
                    dir_name = originalFilesDeploymentLocation + "/" + calendar.getTimeInMillis();
                    success = (new File(dir_name)).mkdirs();
                    if (!success) {
                        System.out.println("Excel - Failed to create directory 2 " + dir_name );
                    }
                    this.files_deployment_location = dir_name;
                    levelDeep = 1;
                }
                out = new FileOutputStream(files_deployment_location + "/" + fileName);
                BulkImportManifestCreator.createBulkManifest(fileName,files_deployment_location, properties);
            } else {
                out = new FileOutputStream(files_deployment_location + "/" + fileName);
                BulkImportManifestCreator.createBulkManifest(fileName,files_deployment_location, properties);
            }
            my_workbook.write(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

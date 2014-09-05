/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.csiro.wdts.validation.utils;

import org.csiro.wdts.validation.service.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author yu021
 */
public class FileUtils {

    static String readFile() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public String readFile(String path) throws FileNotFoundException {
        StringBuilder contents = new StringBuilder();
        BufferedReader input =  new BufferedReader(new FileReader(new File(path)));

        try {
            try {
                String line = null; //not declared within while loop

                while (( line = input.readLine()) != null){
                  contents.append(line);
                  contents.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
         } catch (IOException ex){
             ex.printStackTrace();
         }

        return contents.toString();
    }

    public String readFile(File f) throws FileNotFoundException {
        StringBuilder contents = new StringBuilder();
        BufferedReader input =  new BufferedReader(new FileReader(f));

        try {
            try {
                String line = null; //not declared within while loop

                while (( line = input.readLine()) != null){
                  contents.append(line);
                  contents.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
         } catch (IOException ex){
             ex.printStackTrace();
         }

        return contents.toString();
    }
}

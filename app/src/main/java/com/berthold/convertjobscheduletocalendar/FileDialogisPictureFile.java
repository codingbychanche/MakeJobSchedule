/*
 * isPictureFile.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 8/26/18 10:21 PM
 */

/*
 * Checks filename for extension jpg etc...
 */

package com.berthold.convertjobscheduletocalendar;

public class FileDialogisPictureFile {

    /**
     * ChecK
     *
     * @param   fileName    File name
     * @return  true        If the filename contains 'jpg','gif' etc
     */

    public static boolean check(String fileName){

        String[] np=fileName.split("\\.");

        if (np.length>1) {
        }
            if      (   np[np.length-1].equals("jpg") ||
                        np[np.length-1].equals("JPG") ||
                        np[np.length-1].equals("png") ||
                        np[np.length-1].equals("jpeg")||
                        np[np.length-1].equals("gif")
                    ) {
                System.out.println("------"+np[np.length-1]);
                return true;
            }

            else return false;
    }
}

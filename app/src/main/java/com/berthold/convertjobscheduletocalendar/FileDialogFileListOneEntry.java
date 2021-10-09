/*
 * FileListOneEntry.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 8/26/18 10:19 PM
 */

/*
 *  Data model for each row in our file list
 */

package com.berthold.convertjobscheduletocalendar;

import android.graphics.Bitmap;

public class FileDialogFileListOneEntry {

    // Meta Data
    // This vars contain data to determine the nature of the row (e.g. Folder, Headline etc...)
    public int entryType;
    public static final int IS_ACTIVE=1;

    // Data
    public String fileName;
    public String filePath;
    public Bitmap fileSymbol;
    public boolean isReadable;
    public String lastModified;

    /**
     * Constructor, assign properties
     */

    FileDialogFileListOneEntry(int entryType, Bitmap fileSymbol, String fileName, String filePath, boolean isReadable, String lastModified) {

        this.entryType=entryType;
        this.fileSymbol=fileSymbol;
        this.fileName=fileName;
        this.filePath=filePath;
        this.isReadable=isReadable;
        this.lastModified=lastModified;
    }

}

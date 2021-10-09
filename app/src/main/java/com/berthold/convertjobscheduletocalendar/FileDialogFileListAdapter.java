/*
 * FileListAdapter.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 8/26/18 10:21 PM
 */

/*
 * Adapter class for File- list
 *
 * This code creates each row of our list, each time a new entry
 * is added.
 */


package com.berthold.convertjobscheduletocalendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class FileDialogFileListAdapter extends ArrayAdapter<FileDialogFileListOneEntry> {

    DecimalFormat df=new DecimalFormat("#,###,###");

    /**
     * Constructor
     */

    public FileDialogFileListAdapter(Context context, ArrayList<FileDialogFileListOneEntry> FileDialogFileListOneEntry) {
        super (context,0, FileDialogFileListOneEntry);
    }

    /*
     * Inflate layout for one row of the list
     */

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {

            final FileDialogFileListOneEntry item = getItem(position);

            // Check status of entry and inflate matching layout
            // Active item. Inflate layout and fill row with data
            if (item.entryType == FileDialogFileListOneEntry.IS_ACTIVE) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.file_chooser_file_list_active_entry, parent, false);

                // Set file name
                final TextView tvName = (TextView) convertView.findViewById(R.id.fileNname);
                tvName.setText(item.fileName);

                // Set file symbol and set ImageButton on click listener
                final ImageView fs = (ImageView) convertView.findViewById(R.id.fileSymbol);
                fs.setImageBitmap(item.fileSymbol);
                fs.setTag(position);
                fs.setOnClickListener(new View.OnClickListener() {

                    // Set imageView on click listener
                    @Override
                    public void onClick(View v) {

                        if (FileDialogisPictureFile.check(item.fileName)) {
                            FragmentManager f = ((AppCompatActivity) getContext()).getSupportFragmentManager();

                            FileDialogFragmentShowFileInfo fileDialogFragmentShowFileInfo = FileDialogFragmentShowFileInfo.newInstance(item.filePath);
                            fileDialogFragmentShowFileInfo.show  (f, "fragment_dialog_show_file_info");
                        }
                    }
                });

                // If folder/ file is !readable, then add lock- sym to file sym...
                if (!item.isReadable) {
                    final ImageView lock = (ImageView) convertView.findViewById(R.id.locksymbol);
                    Bitmap lockSym = BitmapFactory.decodeResource(convertView.getResources(), android.R.drawable.ic_delete);
                    lock.setImageBitmap(lockSym);
                }

                // Set last modified date
                final TextView d = (TextView) convertView.findViewById(R.id.lastmodified);
                d.setText(item.lastModified);
            }
        return convertView;
    }
}

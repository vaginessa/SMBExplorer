package com.sentaroh.android.SMBExplorer;

import java.io.Serializable;
import java.util.ArrayList;

public class DirectoryHistoryItem  implements Serializable {
    public String directory_name="";
    public int pos_fv =0, pos_top =0;
    public ArrayList<FileListItem> file_list=null;
}

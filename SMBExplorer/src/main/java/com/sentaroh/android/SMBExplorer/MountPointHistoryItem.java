package com.sentaroh.android.SMBExplorer;

import java.io.Serializable;
import java.util.ArrayList;

public class MountPointHistoryItem implements Serializable {
    public String mp_name="";
    public ArrayList<DirectoryHistoryItem> directory_history=new ArrayList<DirectoryHistoryItem>();
}

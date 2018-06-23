package com.sentaroh.android.SMBExplorer;

/*
The MIT License (MIT)
Copyright (c) 2011-2013 Sentaroh

Permission is hereby granted, free of charge, to any person obtaining a copy of 
this software and associated documentation files (the "Software"), to deal 
in the Software without restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
and to permit persons to whom the Software is furnished to do so, subject to 
the following conditions:

The above copyright notice and this permission notice shall be included in all copies or 
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

*/

import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.sentaroh.android.Utilities.CommonGlobalParms;
import com.sentaroh.android.Utilities.Dialog.CommonDialog;
import com.sentaroh.android.Utilities.SafManager;
import com.sentaroh.android.Utilities.ThemeColorList;
import com.sentaroh.android.Utilities.Widget.CustomTextView;

import java.util.ArrayList;

import static com.sentaroh.android.SMBExplorer.Constants.*;

public class GlobalParameters extends CommonGlobalParms{
	public Context context =null;
	public String internalRootDirectory="";
    public String internalAppSpecificDirectory="";
    public CommonDialog commonDlg=null;
    public CommonUtilities mUtil=null;

    public SafManager safMgr =null;

    public String remoteBase = "", localBase = "";
    public String remoteDir = "", localDir = "";

//    public String smbUser=null, smbPass=null;
    public SmbServerConfig currentSmbServerConfig =null;
    public ThemeColorList themeColorList;

    public ArrayList<SmbServerConfig> smbConfigList =null;

    public ISvcClient svcClient = null;
    public ServiceConnection svcConnection =null;

    public TabHost tabHost =null;
    public TabWidget tabWidget =null;

    public LinearLayout mLocalView=null, mRemoteView=null;

    public ArrayList<MountPointHistoryItem> mountPointHistoryList =new ArrayList<MountPointHistoryItem>();

    public FileListAdapter localFileListAdapter=null;
    public FileListAdapter remoteFileListAdapter=null;
    public ListView localFileListView=null;
    public ListView remoteFileListView=null;
    public String currentTabName=SMBEXPLORER_TAB_LOCAL;
    public Spinner localFileListDirSpinner=null;
    public Spinner remoteFileListDirSpinner=null;

    public Button mainPasteListClearBtn=null;

    public ImageButton localContextBtnCreate =null;
    public LinearLayout localContextBtnCreateView =null;
    public ImageButton localContextBtnCopy =null;
    public LinearLayout localContextBtnCopyView =null;
    public ImageButton localContextBtnCut =null;
    public LinearLayout localContextBtnCutView =null;
    public ImageButton localContextBtnPaste =null;
    public LinearLayout localContextBtnPasteView =null;
    public ImageButton localContextBtnRename =null;
    public LinearLayout localContextBtnRenameView =null;
    public ImageButton localContextBtnDelete =null;
    public LinearLayout localContextBtnDeleteView =null;
    public ImageButton localContextBtnSelectAll =null;
    public LinearLayout localContextBtnSelectAllView =null;
    public ImageButton localContextBtnUnselectAll =null;
    public LinearLayout localContextBtnUnselectAllView =null;

    public ImageButton remoteContextBtnCreate =null;
    public LinearLayout remoteContextBtnCreateView =null;
    public ImageButton remoteContextBtnCopy =null;
    public LinearLayout remoteContextBtnCopyView =null;
    public ImageButton remoteContextBtnCut =null;
    public LinearLayout remoteContextBtnCutView =null;
    public ImageButton remoteContextBtnPaste =null;
    public LinearLayout remoteContextBtnPasteView =null;
    public ImageButton remoteContextBtnRename =null;
    public LinearLayout remoteContextBtnRenameView =null;
    public ImageButton remoteContextBtnDelete =null;
    public LinearLayout remoteContextBtnDeleteView =null;
    public ImageButton remoteContextBtnSelectAll =null;
    public LinearLayout remoteContextBtnSelectAllView =null;
    public ImageButton remoteContextBtnUnselectAll =null;
    public LinearLayout remoteContextBtnUnselectAllView =null;

    public CustomTextView localFileListPath=null;
    public TextView localFileListEmptyView=null;
    public Button localFileListUpBtn=null, localFileListTopBtn=null;

    public int dialogBackgroundColor=0xff111111;

    public CustomTextView remoteFileListPath=null;
    public TextView remoteFileListEmptyView=null;
    public Button remoteFileListUpBtn=null;
    public Button remoteFileListTopBtn=null;

    public ArrayList<FileIoLinkParm> fileioLinkParm=new ArrayList<FileIoLinkParm>();

    public TextView localProgressMsg =null;
    public Button localProgressCancel =null;
    public TextView remoteProgressMsg =null;
    public Button remoteProgressCancel =null;
    public View.OnClickListener progressOnClickListener =null;
    public LinearLayout localProgressView =null;
    public LinearLayout remoteProgressView =null;
    public LinearLayout localDialogView =null;
    public LinearLayout remoteDialogView =null;

    public View.OnClickListener dialogOnClickListener =null;
    public TextView localDialogMsg =null;
    public Button localDialogCloseBtn =null;
    public TextView remoteDialogMsg =null;
    public Button remoteDialogCloseBtn =null;
    public String dialogMsgCat ="";

    public boolean fileIoWifiLockRequired=false;
	public boolean fileIoWakeLockRequired=true;
	
	public TextView progressMsgView=null;
	public Button progressCancelBtn=null;
	public TextView dialogMsgView=null;
	public Button dialogCloseBtn=null;

//	Settings parameter
    public boolean settingExitClean=true;
    public int     settingDebugLevel=0;
    public boolean settingUseLightTheme=false;
    public int     settingLogMaxFileCount=10;
    public String  settingLogMsgDir="", settingLogMsgFilename="SMBExplorerLog";
    public boolean settingLogOption=false;
    public boolean settingPutLogcatOption=false;

    public ISvcCallback callbackStub=null;

    public boolean activityIsBackground=false;

    public GlobalParameters() {};
	
	public void  initGlobalParameter(Context c) {
        context =c;

        internalRootDirectory= Environment.getExternalStorageDirectory().toString();
        internalAppSpecificDirectory=internalRootDirectory+"/Android/data/com.sentaroh.android.SMBExplorer/files";

		loadSettingsParm(c);

        safMgr =new SafManager(c, settingDebugLevel>0);

		setLogParms(this);
	};
	
	public void loadSettingsParm(Context c) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        settingDebugLevel=Integer.parseInt(prefs.getString(c.getString(R.string.settings_log_level), "0"));
        settingLogMaxFileCount=Integer.valueOf(prefs.getString(c.getString(R.string.settings_log_file_max_count), "10"));
        settingLogMsgDir=prefs.getString(c.getString(R.string.settings_log_dir),internalRootDirectory+"/"+APPLICATION_TAG+"/");
        settingLogOption=prefs.getBoolean(c.getString(R.string.settings_log_option), false);
        settingPutLogcatOption=prefs.getBoolean(c.getString(R.string.settings_put_logcat_option), false);

    };

    public void setLogParms(GlobalParameters gp) {
        setDebugLevel(gp.settingDebugLevel);
        setLogcatEnabled(gp.settingPutLogcatOption);
        setLogLimitSize(2*1024*1024);
        setLogMaxFileCount(gp.settingLogMaxFileCount);
        setLogEnabled(gp.settingLogOption);
        setLogDirName(gp.settingLogMsgDir);
        setLogFileName(gp.settingLogMsgFilename);
        setApplicationTag(APPLICATION_TAG);

    }

}



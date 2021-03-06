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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckedTextView;
import android.widget.Spinner;

import com.sentaroh.android.Utilities.ThreadCtrl;
import com.sentaroh.android.SMBExplorer.Log.LogUtil;

import java.io.File;
import java.util.ArrayList;

import static com.sentaroh.android.SMBExplorer.Constants.DEFAULT_PREFS_FILENAME;

public final class CommonUtilities {
	private Context mContext=null;

   	private LogUtil mLog=null;
   	
   	private GlobalParameters mGp=null;
   	
   	@SuppressWarnings("unused")
	private String mLogIdent="";
   	
	public CommonUtilities(Context c, String li, GlobalParameters gp) {
		mContext=c;// ContextはApplicationContext
		mLog=new LogUtil(c, li, gp);
		mLogIdent=li;
        mGp=gp;
	}

	final public SharedPreferences getPrefMgr() {
    	return getPrefMgr(mContext);
    }

	public static void setSpinnerBackground(Context c, Spinner spinner, boolean theme_is_light) {
		if (theme_is_light) spinner.setBackground(c.getDrawable(R.drawable.spinner_color_background_light));
		else spinner.setBackground(c.getDrawable(R.drawable.spinner_color_background));
	};

	final static public SharedPreferences getPrefMgr(Context c) {
    	return c.getSharedPreferences(DEFAULT_PREFS_FILENAME, Context.MODE_PRIVATE|Context.MODE_MULTI_PROCESS);
    }

	final public void setLogId(String li) {
		mLog.setLogId(li);
	};
	
	public static void printStackTraceElement(CommonUtilities ut, StackTraceElement[] ste) {
		for (int i=0;i<ste.length;i++) {
			ut.addLogMsg("E","",ste[i].toString());	
		}
	};

	final static public String getFileExtention(String fp) {
		String fid="";
		if (fp.lastIndexOf(".") > 0) {
			fid = fp.substring(fp.lastIndexOf(".") + 1).toLowerCase();
		}
		return fid;
	};
	
	final static public String getExecutedMethodName() {
		String name = Thread.currentThread().getStackTrace()[3].getMethodName();
		return name;
	}

	final public void resetLogReceiver() {
		mLog.resetLogReceiver();
	};

	final public void flushLog() {
		mLog.flushLog();
	};

	final public void rotateLogFile() {
		mLog.rotateLogFile();
	};

    final public void deleteLogFile() {
    	mLog.deleteLogFile();
	};

	public String buildPrintMsg(String cat, String... msg) {
		return mLog.buildPrintLogMsg(cat, msg);
	};
	
	final public void addLogMsg(String cat, String... msg) {
		mLog.addLogMsg(cat, msg); 
	};
	final public void addDebugMsg(int lvl, String cat, String... msg) {
		mLog.addDebugMsg(lvl, cat, msg);
	};

	final public boolean isLogFileExists() {
		boolean result = false;
		result=mLog.isLogFileExists();
		if (mGp.settingDebugLevel>=3) addDebugMsg(3,"I","Log file exists="+result);
		return result;
	};

	final public String getLogFilePath() {
		return mLog.getLogFilePath();
	};
	
	static public long getSettingsParmSaveDate(Context c, String dir, String fn) {
		File lf=new File(dir+"/"+fn);
		long result=0;
		if (lf.exists()) {
			result=lf.lastModified();
		} else {
			result=-1;
		}
		return result;
	};
	
	public boolean isDebuggable() {
        PackageManager manager = mContext.getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = manager.getApplicationInfo(mContext.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            return false;
        }
        if ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE)
            return true;
        return false;
    };
	
	public void initAppSpecificExternalDirectory(Context c) {
		ContextCompat.getExternalFilesDirs(c, null);
	};
	
	public boolean isWifiActive() { 
		boolean ret=false;
		WifiManager mWifi =(WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
		if (mWifi.isWifiEnabled()) ret=true;
		addDebugMsg(2,"I","isWifiActive WifiEnabled="+ret);
		return ret;
	};

	public String getConnectedWifiSsid() { 
		String ret="";
		WifiManager mWifi =(WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
		String ssid="";
		if (mWifi.isWifiEnabled()) {
			ssid=mWifi.getConnectionInfo().getSSID();
			if (ssid!=null && 
					!ssid.equals("0x") &&
					!ssid.equals("<unknown ssid>") &&
					!ssid.equals("")) ret=ssid;
//			Log.v("","ssid="+ssid);
		}
		addDebugMsg(2,"I","getConnectedWifiSsid WifiEnabled="+mWifi.isWifiEnabled()+ ", SSID="+ssid+", result="+ret);
		return ret;
	};

	static public void setCheckedTextView(final CheckedTextView ctv) {
		ctv.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				ctv.toggle();
			}
		});
	}

}

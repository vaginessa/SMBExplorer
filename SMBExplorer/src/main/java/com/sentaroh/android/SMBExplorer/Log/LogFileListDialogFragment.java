package com.sentaroh.android.SMBExplorer.Log;

import android.os.Bundle;

import com.sentaroh.android.Utilities.LogUtil.CommonLogFileListDialogFragment;

public class LogFileListDialogFragment extends CommonLogFileListDialogFragment {
	public static LogFileListDialogFragment newInstance(boolean retainInstance, String title) {
		LogFileListDialogFragment frag = new LogFileListDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("retainInstance", retainInstance);
        bundle.putString("title", title);
//        bundle.putString("msgtext", msgtext);
        frag.setArguments(bundle);
        return frag;
    }

}

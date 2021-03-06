package com.sentaroh.android.SMBExplorer;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.sentaroh.android.Utilities.ContextMenu.CustomContextMenu;
import com.sentaroh.android.Utilities.ContextMenu.CustomContextMenuItem;
import com.sentaroh.android.Utilities.Dialog.CommonDialog;
import com.sentaroh.android.Utilities.LocalMountPoint;
import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.ThreadCtrl;
import com.sentaroh.android.Utilities.Widget.CustomSpinnerAdapter;
import com.sentaroh.android.Utilities.Widget.CustomTextView;
import com.sentaroh.jcifs.JcifsException;
import com.sentaroh.jcifs.JcifsFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.sentaroh.android.SMBExplorer.Constants.FILEIO_PARM_COPY_LOCAL_TO_LOCAL;
import static com.sentaroh.android.SMBExplorer.Constants.FILEIO_PARM_COPY_LOCAL_TO_REMOTE;
import static com.sentaroh.android.SMBExplorer.Constants.FILEIO_PARM_COPY_REMOTE_TO_LOCAL;
import static com.sentaroh.android.SMBExplorer.Constants.FILEIO_PARM_COPY_REMOTE_TO_REMOTE;
import static com.sentaroh.android.SMBExplorer.Constants.FILEIO_PARM_DOWLOAD_REMOTE_FILE;
import static com.sentaroh.android.SMBExplorer.Constants.FILEIO_PARM_LOCAL_CREATE;
import static com.sentaroh.android.SMBExplorer.Constants.FILEIO_PARM_LOCAL_DELETE;
import static com.sentaroh.android.SMBExplorer.Constants.FILEIO_PARM_LOCAL_RENAME;
import static com.sentaroh.android.SMBExplorer.Constants.FILEIO_PARM_MOVE_LOCAL_TO_LOCAL;
import static com.sentaroh.android.SMBExplorer.Constants.FILEIO_PARM_MOVE_LOCAL_TO_REMOTE;
import static com.sentaroh.android.SMBExplorer.Constants.FILEIO_PARM_MOVE_REMOTE_TO_LOCAL;
import static com.sentaroh.android.SMBExplorer.Constants.FILEIO_PARM_MOVE_REMOTE_TO_REMOTE;
import static com.sentaroh.android.SMBExplorer.Constants.FILEIO_PARM_REMOTE_CREATE;
import static com.sentaroh.android.SMBExplorer.Constants.FILEIO_PARM_REMOTE_DELETE;
import static com.sentaroh.android.SMBExplorer.Constants.FILEIO_PARM_REMOTE_RENAME;
import static com.sentaroh.android.SMBExplorer.Constants.SMBEXPLORER_TAB_LOCAL;
import static com.sentaroh.android.SMBExplorer.Constants.SMBEXPLORER_TAB_POS_LOCAL;
import static com.sentaroh.android.SMBExplorer.Constants.SMBEXPLORER_TAB_POS_REMOTE;
import static com.sentaroh.android.SMBExplorer.Constants.SMBEXPLORER_TAB_REMOTE;

public class FileManager {

    private GlobalParameters mGp;
    private Context mContext;
    private MainActivity mActivity;
    private CommonUtilities mUtil=null;
    private CustomContextMenu ccMenu = null;
    private boolean mSpinnerSelectionEnabled =true;
    private Handler mUiHandler=null;

    public FileManager(MainActivity a, GlobalParameters gp, CommonUtilities mu, CustomContextMenu cc) {
        mActivity=a;
        mGp=gp;
        mContext=gp.context;
        mUtil=mu;
        ccMenu=cc;
        mUiHandler=new Handler();
    }

    public void setSpinnerSelectionEnabled(boolean enabled) {
        mSpinnerSelectionEnabled =enabled;
    }

    public boolean isSpinnerSelectionEnabled() {
        return mSpinnerSelectionEnabled;
    }

    public void setMainListener() {
        setLocalDirBtnListener();
        setRemoteDirBtnListener();

        setLocalFilelistItemClickListener();
        setLocalFilelistLongClickListener();
        setRemoteFilelistItemClickListener();
        setRemoteFilelistLongClickListener();

        setLocalContextButtonListener();
        setRemoteContextButtonListener();
    }

    public void createView() {
        LinearLayout ll_local_tab=(LinearLayout)mGp.mLocalView.findViewById(R.id.explorer_filelist_local_tab);
        mGp.localContextBtnCreate =(ImageButton)ll_local_tab.findViewById(R.id.context_button_clear);
        mGp.localContextBtnCreateView =(LinearLayout)ll_local_tab.findViewById(R.id.context_button_clear_view);
        mGp.localContextBtnCopy =(ImageButton)ll_local_tab.findViewById(R.id.context_button_copy);
        mGp.localContextBtnCopyView =(LinearLayout)ll_local_tab.findViewById(R.id.context_button_copy_view);
        mGp.localContextBtnCut =(ImageButton)ll_local_tab.findViewById(R.id.context_button_cut);
        mGp.localContextBtnCutView =(LinearLayout)ll_local_tab.findViewById(R.id.context_button_cut_view);
        mGp.localContextBtnPaste =(ImageButton)ll_local_tab.findViewById(R.id.context_button_paste);
        mGp.localContextBtnPasteView =(LinearLayout)ll_local_tab.findViewById(R.id.context_button_paste_view);
        mGp.localContextBtnRename =(ImageButton)ll_local_tab.findViewById(R.id.context_button_rename);
        mGp.localContextBtnRenameView =(LinearLayout)ll_local_tab.findViewById(R.id.context_button_rename_view);
        mGp.localContextBtnDelete =(ImageButton)ll_local_tab.findViewById(R.id.context_button_delete);
        mGp.localContextBtnDeleteView =(LinearLayout)ll_local_tab.findViewById(R.id.context_button_delete_view);
        mGp.localContextBtnSelectAll =(ImageButton)ll_local_tab.findViewById(R.id.context_button_select_all);
        mGp.localContextBtnSelectAllView =(LinearLayout)ll_local_tab.findViewById(R.id.context_button_select_all_view);
        mGp.localContextBtnUnselectAll =(ImageButton)ll_local_tab.findViewById(R.id.context_button_unselect_all);
        mGp.localContextBtnUnselectAllView =(LinearLayout)ll_local_tab.findViewById(R.id.context_button_unselect_all_view);

        LinearLayout ll_remote_tab=(LinearLayout)mGp.mRemoteView.findViewById(R.id.explorer_filelist_remote_tab);
        mGp.remoteContextBtnCreate =(ImageButton)ll_remote_tab.findViewById(R.id.context_button_clear);
        mGp.remoteContextBtnCreateView =(LinearLayout)ll_remote_tab.findViewById(R.id.context_button_clear_view);
        mGp.remoteContextBtnCopy =(ImageButton)ll_remote_tab.findViewById(R.id.context_button_copy);
        mGp.remoteContextBtnCopyView =(LinearLayout)ll_remote_tab.findViewById(R.id.context_button_copy_view);
        mGp.remoteContextBtnCut =(ImageButton)ll_remote_tab.findViewById(R.id.context_button_cut);
        mGp.remoteContextBtnCutView =(LinearLayout)ll_remote_tab.findViewById(R.id.context_button_cut_view);
        mGp.remoteContextBtnPaste =(ImageButton)ll_remote_tab.findViewById(R.id.context_button_paste);
        mGp.remoteContextBtnPasteView =(LinearLayout)ll_remote_tab.findViewById(R.id.context_button_paste_view);
        mGp.remoteContextBtnRename =(ImageButton)ll_remote_tab.findViewById(R.id.context_button_rename);
        mGp.remoteContextBtnRenameView =(LinearLayout)ll_remote_tab.findViewById(R.id.context_button_rename_view);
        mGp.remoteContextBtnDelete =(ImageButton)ll_remote_tab.findViewById(R.id.context_button_delete);
        mGp.remoteContextBtnDeleteView =(LinearLayout)ll_remote_tab.findViewById(R.id.context_button_delete_view);
        mGp.remoteContextBtnSelectAll =(ImageButton)ll_remote_tab.findViewById(R.id.context_button_select_all);
        mGp.remoteContextBtnSelectAllView =(LinearLayout)ll_remote_tab.findViewById(R.id.context_button_select_all_view);
        mGp.remoteContextBtnUnselectAll =(ImageButton)ll_remote_tab.findViewById(R.id.context_button_unselect_all);
        mGp.remoteContextBtnUnselectAllView =(LinearLayout)ll_remote_tab.findViewById(R.id.context_button_unselect_all_view);

        mGp.localFileListDirSpinner=(Spinner)mGp.mLocalView.findViewById(R.id.explorer_filelist_local_tab_dir);
        mGp.remoteFileListDirSpinner=(Spinner)mGp.mRemoteView.findViewById(R.id.explorer_filelist_remote_tab_dir);

        mGp.localFileListView=(ListView)mGp.mLocalView.findViewById(R.id.explorer_filelist_local_tab_listview);
        mGp.remoteFileListView=(ListView)mGp.mRemoteView.findViewById(R.id.explorer_filelist_remote_tab_listview);
        mGp.localFileListEmptyView=(TextView)mGp.mLocalView.findViewById(R.id.explorer_filelist_local_empty_view);
        mGp.remoteFileListEmptyView=(TextView)mGp.mRemoteView.findViewById(R.id.explorer_filelist_remote_empty_view);
        mGp.localFileListPath=(CustomTextView)mGp.mLocalView.findViewById(R.id.explorer_filelist_local_filepath);
        mGp.localFileListUpBtn=(Button)mGp.mLocalView.findViewById(R.id.explorer_filelist_local_up_btn);
        mGp.localFileListTopBtn=(Button)mGp.mLocalView.findViewById(R.id.explorer_filelist_local_top_btn);

        mGp.remoteFileListPath=(CustomTextView)mGp.mRemoteView.findViewById(R.id.explorer_filelist_remote_filepath);
        mGp.remoteFileListUpBtn=(Button)mGp.mRemoteView.findViewById(R.id.explorer_filelist_remote_up_btn);
        mGp.remoteFileListTopBtn=(Button)mGp.mRemoteView.findViewById(R.id.explorer_filelist_remote_top_btn);
        mGp.remoteFileListUpBtn.setEnabled(false);
        mGp.remoteFileListTopBtn.setEnabled(false);

        mGp.localProgressMsg =(TextView)mGp.mLocalView.findViewById(R.id.explorer_filelist_local_progress_msg);
        mGp.localProgressCancel =(Button)mGp.mLocalView.findViewById(R.id.explorer_filelist_local_progress_cancel);
        mGp.remoteProgressMsg =(TextView)mGp.mRemoteView.findViewById(R.id.explorer_filelist_remote_progress_msg);
        mGp.remoteProgressCancel =(Button)mGp.mRemoteView.findViewById(R.id.explorer_filelist_remote_progress_cancel);
        mGp.progressMsgView=null;
        mGp.progressCancelBtn=null;
        mGp.dialogMsgView=null;

        mGp.localProgressView =(LinearLayout)mGp.mLocalView.findViewById(R.id.explorer_filelist_local_progress);
        mGp.remoteProgressView =(LinearLayout)mGp.mRemoteView.findViewById(R.id.explorer_filelist_remote_progress);
        mGp.localDialogView =(LinearLayout)mGp.mLocalView.findViewById(R.id.explorer_filelist_local_dialog);
        mGp.remoteDialogView =(LinearLayout)mGp.mRemoteView.findViewById(R.id.explorer_filelist_remote_dialog);

        mGp.localDialogMsg =(TextView)mGp.mLocalView.findViewById(R.id.explorer_filelist_local_dialog_msg);
        mGp.localDialogCloseBtn =(Button)mGp.mLocalView.findViewById(R.id.explorer_filelist_local_dialog_close);
        mGp.remoteDialogMsg =(TextView)mGp.mRemoteView.findViewById(R.id.explorer_filelist_remote_dialog_msg);
        mGp.remoteDialogCloseBtn =(Button)mGp.mRemoteView.findViewById(R.id.explorer_filelist_remote_dialog_close);

        mGp.dialogBackgroundColor=mGp.themeColorList.window_background_color_content;

        mGp.localFileListAdapter=new FileListAdapter(mActivity);
        mGp.localFileListView.setAdapter(mGp.localFileListAdapter);

        mGp.remoteFileListAdapter=new FileListAdapter(mActivity);
        mGp.remoteFileListView.setAdapter(mGp.remoteFileListAdapter);

        setPasteButtonEnabled();
    }

    public void refreshFileListView() {
        if (mGp.currentTabName.equals(SMBEXPLORER_TAB_LOCAL)) {
            mGp.safMgr.loadSafFile();
            if (!mGp.localBase.equals("")) {
                if (mGp.localBase.startsWith(mGp.safMgr.getUsbRootPath())) {
                    NotifyEvent ntfy=new NotifyEvent(mContext);
                    ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                        @Override
                        public void positiveResponse(Context context, Object[] objects) {
                            ArrayList<FileListItem> tfl=(ArrayList<FileListItem>)objects[0];
                            mGp.localFileListAdapter.setDataList(tfl);
                            mGp.localFileListAdapter.notifyDataSetChanged();
                            mGp.tabHost.getTabWidget().getChildTabViewAt(SMBEXPLORER_TAB_POS_LOCAL).setEnabled(true);
                        }
                        @Override
                        public void negativeResponse(Context context, Object[] objects) {}
                    });
                    createUsbFileList(false, mGp.localBase+"/"+mGp.localDir, ntfy);
                } else {
                    int fv=0;
                    int top=0;
                    if (mGp.localFileListView.getChildAt(0)!=null) {
                        fv=mGp.localFileListView.getFirstVisiblePosition();
                        top=mGp.localFileListView.getChildAt(0).getTop();
                    }
                    if (!updateLocalDirSpinner()) {
                        String t_dir=buildFullPath(mGp.localBase,mGp.localDir);
                        loadLocalFilelist(mGp.localBase,mGp.localDir, null);
                        mGp.localFileListView.setSelectionFromTop(fv, top);
                        setEmptyFolderView();
                    }
                }
            }
        } else if (mGp.currentTabName.equals(SMBEXPLORER_TAB_REMOTE)) {
            if (!mGp.remoteBase.equals("")) {
                loadRemoteFilelist(mGp.remoteBase, mGp.remoteDir);
            }
        }
    }

    private MountPointHistoryItem getMountPointHistoryItem(String mp) {
        for(MountPointHistoryItem item:mGp.mountPointHistoryList) {
            if (item.mp_name.equals(mp)) {
                mUtil.addDebugMsg(1,"I","getMountPointHistoryItem mp="+mp+", result="+item);
                return item;
            }
        }
        mUtil.addDebugMsg(1,"I","getMountPointHistoryItem mp="+mp+", result=Not found");
        return null;
    }

    private void removeDirectoryHistoryItem(String mp, String dir) {
        MountPointHistoryItem mphi=getMountPointHistoryItem(mp);
        if (mphi!=null) {
            boolean removed=false;
            for(DirectoryHistoryItem item:mphi.directory_history) {
                if (item.directory_name.equals(dir)) {
                    mphi.directory_history.remove(item);
                    mUtil.addDebugMsg(1,"I","removeDirectoryHistoryItem removed mp="+mp+", dir="+dir);
                    removed=true;
                    break;
                }
            }
            if (!removed) mUtil.addDebugMsg(1,"I","removeDirectoryHistoryItem DIRECTORY not found mp="+mp+", dir="+dir);
        } else {
            mUtil.addDebugMsg(1,"I","removeDirectoryHistoryItem MP not found mp="+mp+", dir="+dir);
        }
    }

    private void updateDirectoryHistoryItem(String mp, String dir, ListView lv, FileListAdapter fa) {
        int pos_fv=lv.getFirstVisiblePosition();
        int pos_top=0;
        if (lv.getChildAt(0)!=null) pos_top=lv.getChildAt(0).getTop();

        MountPointHistoryItem mphi=getMountPointHistoryItem(mp);
        if (mphi!=null) {
            for(DirectoryHistoryItem item:mphi.directory_history) {
                if (item.directory_name.equals(dir)) {
                    item.pos_fv =pos_fv;
                    item.pos_top =pos_top;
                    item.file_list=fa.getDataList();
                    mUtil.addDebugMsg(1,"I","updateDirectoryHistoryItem updated mp="+mp+", dir="+dir);
                    break;
                }
            }
        } else {
            mUtil.addDebugMsg(1,"I","updateDirectoryHistoryItem MP not found mp="+mp+", dir="+dir);
        }
    }

    private DirectoryHistoryItem getDirectoryHistoryItem(String mp, String dir) {
        MountPointHistoryItem mphi=getMountPointHistoryItem(mp);
        if (mphi!=null) {
            for(DirectoryHistoryItem item:mphi.directory_history) {
                if (item.directory_name.equals(dir)) {
                    mUtil.addDebugMsg(1,"I","getDirectoryHistoryItem found mp="+mp+", dir="+dir+", result="+item.directory_name);
                    return item;
                }
            }
        }
        mUtil.addDebugMsg(1,"I","getDirectoryHistoryItem not found mp="+mp+", dir="+dir);
        return null;
    }

    private void addDirectoryHistoryItem(String mp, String dir, ArrayList<FileListItem>fl) {
        MountPointHistoryItem mphi=getMountPointHistoryItem(mp);
        if (mphi!=null) {
            DirectoryHistoryItem dhi=getDirectoryHistoryItem(mp, dir);
            if (dhi==null) {
                DirectoryHistoryItem n_dhi=new DirectoryHistoryItem();
                n_dhi.directory_name=dir;
                n_dhi.file_list=fl;
                mphi.directory_history.add(n_dhi);
                mUtil.addDebugMsg(1,"I","addDirectoryHistoryItem added mp="+mp+", dir="+dir);
            } else {
                mUtil.addDebugMsg(1,"I","addDirectoryHistoryItem updated mp="+mp+", dir="+dir);
                dhi.file_list=fl;
            }
        } else {
            mUtil.addDebugMsg(1,"I","addDirectoryHistoryItem mp not found mp="+mp+", dir="+dir);
        }

    }

    private void addMountPointHistoryItem(String mp, String dir, ArrayList<FileListItem>fl) {
        MountPointHistoryItem mp_hist=getMountPointHistoryItem(mp);
        if (mp_hist==null) {
            DirectoryHistoryItem dhi=new DirectoryHistoryItem();
            dhi.directory_name=dir;
            dhi.file_list=fl;
            MountPointHistoryItem nmp=new MountPointHistoryItem();
            nmp.mp_name=mp;
            nmp.directory_history=new ArrayList<DirectoryHistoryItem>();
            nmp.directory_history.add(dhi);
            mGp.mountPointHistoryList.add(nmp);
            mUtil.addDebugMsg(1,"I","addMountPointHistoryItem Added mp="+mp+", dir="+dir);
        } else {
            mUtil.addDebugMsg(1,"I","addMountPointHistoryItem Alread registered mp="+mp+", dir="+dir);
            DirectoryHistoryItem dhi=getDirectoryHistoryItem(mp,dir);
            if (dhi!=null) {
                dhi.file_list=fl;
            }
        }
    }

    public void loadLocalFilelist(String base, String dir, NotifyEvent p_ntfy) {
        String t_dir=buildFullPath(mGp.localBase,mGp.localDir);
//		switchTab(SMBEXPLORER_TAB_LOCAL);
        NotifyEvent ntfy=new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                ArrayList<FileListItem> tfl=(ArrayList<FileListItem>)objects[0];
                if (tfl==null) return;
                addMountPointHistoryItem(base, dir, tfl);

                mGp.localFileListAdapter.setShowLastModified(true);
                mGp.localFileListAdapter.setDataList(tfl);
                mGp.localFileListAdapter.notifyDataSetChanged();

                if (dir.equals("")) {
                    mGp.localFileListTopBtn.setEnabled(false);
                    mGp.localFileListUpBtn.setEnabled(false);
                } else {
                    mGp.localFileListTopBtn.setEnabled(true);
                    mGp.localFileListUpBtn.setEnabled(true);
                }

                mGp.tabHost.getTabWidget().getChildTabViewAt(SMBEXPLORER_TAB_POS_LOCAL).setEnabled(true);

                setFileListPathName(mGp.localFileListPath,mGp.localBase,mGp.localDir);
                setLocalContextButtonStatus();

                if (p_ntfy!=null) p_ntfy.notifyToListener(true, null);
            }

            @Override
            public void negativeResponse(Context context, Object[] objects) {}
        });
        if (mGp.localBase.startsWith(mGp.safMgr.getUsbRootPath())) createUsbFileList(false, t_dir, ntfy);
        else createLocalFileList(false,t_dir, ntfy);
    }

    private void loadRemoteFilelist(final String url, final String dir) {
        final String t_dir=buildFullPath(mGp.remoteBase,mGp.remoteDir);
        NotifyEvent ne=new NotifyEvent(mContext);
        ne.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context c,Object[] o) {
                ArrayList<FileListItem> tfl=(ArrayList<FileListItem>)o[0];
                addDirectoryHistoryItem(url, dir, tfl);
                mGp.remoteFileListAdapter.setDataList(tfl);
                mGp.remoteFileListAdapter.notifyDataSetChanged();
                setFileListPathName(mGp.remoteFileListPath,mGp.remoteBase,mGp.remoteDir);
                setRemoteContextButtonStatus();
                setEmptyFolderView();

                if (dir.equals("")) {
                    mGp.remoteFileListTopBtn.setEnabled(false);
                    mGp.remoteFileListUpBtn.setEnabled(false);
                } else {
                    mGp.remoteFileListTopBtn.setEnabled(true);
                    mGp.remoteFileListUpBtn.setEnabled(true);
                }
            }

            @Override
            public void negativeResponse(Context c,Object[] o) {
                addDirectoryHistoryItem(url, dir, new ArrayList<FileListItem>());
                mGp.remoteFileListAdapter.setDataList(new ArrayList<FileListItem>());
                mGp.remoteFileListAdapter.notifyDataSetChanged();
                setFileListPathName(mGp.remoteFileListPath,mGp.remoteBase,mGp.remoteDir);
                setEmptyFolderView();
            }
        });
        createRemoteFileList(RetrieveFileList.OPCD_FILE_LIST, url+"/"+dir+"/",ne);
    }

    public boolean updateLocalDirSpinner() {
        int sel_no=mGp.localFileListDirSpinner.getSelectedItemPosition();

        CustomSpinnerAdapter adapter = (CustomSpinnerAdapter) mGp.localFileListDirSpinner.getAdapter();
        mGp.localStorageConfig=createLocalProfileEntry();
        boolean changed=false;
        if (mGp.localStorageConfig.size()==adapter.getCount()) {
            for(int i=0;i>adapter.getCount();i++) {
                String a_item=adapter.getItem(i);
                if (!a_item.equals(mGp.localStorageConfig.get(i))) {
                    changed=true;
                    break;
                }
            }
        } else {
            changed=true;
        }
        adapter.clear();
        for (int i=0;i<mGp.localStorageConfig.size();i++) {
            adapter.add(mGp.localStorageConfig.get(i).storage_name);
        }
        if (adapter.getCount()>sel_no) mGp.localFileListDirSpinner.setSelection(sel_no);
        else mGp.localFileListDirSpinner.setSelection(0);

        return changed;
    }

    public void setLocalDirBtnListener() {
        int sel_no=mGp.localFileListDirSpinner.getSelectedItemPosition();
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(mActivity, android.R.layout.simple_spinner_item);
//        adapter.setTextColor(Color.BLACK);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        mGp.localFileListDirSpinner.setPrompt("Select Storage");
        mGp.localFileListDirSpinner.setAdapter(adapter);

        mGp.localStorageConfig=createLocalProfileEntry();
        for (int i=0;i<mGp.localStorageConfig.size();i++) {
            adapter.add(mGp.localStorageConfig.get(i).storage_name);
        }
        if (adapter.getCount()>sel_no) mGp.localFileListDirSpinner.setSelection(sel_no);
        else mGp.localFileListDirSpinner.setSelection(0);

        mGp.localFileListDirSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isSpinnerSelectionEnabled()) return;
                Spinner spinner = (Spinner) parent;
                String stg_name=(String) spinner.getSelectedItem();
                String turl="";
                for(GlobalParameters.LocalStorageConfig item:mGp.localStorageConfig) {
                    if (item.storage_name.equals(stg_name)) {
                        turl=item.storage_path;
                    }
                }
                MountPointHistoryItem mphi=getMountPointHistoryItem(turl);
                if (mphi!=null) {
                    updateDirectoryHistoryItem(mGp.localBase, mGp.localDir, mGp.localFileListView, mGp.localFileListAdapter);
                    ArrayList<DirectoryHistoryItem> dhl=mphi.directory_history;
                    DirectoryHistoryItem dhi=dhl.get(dhl.size()-1);

                    mGp.localDir=dhi.directory_name;
                    mGp.localBase=mphi.mp_name;

                    loadLocalFilelist(mGp.localBase, mGp.localDir, null);
                    mGp.localFileListView.setSelection(0);
                    for (int j = 0; j < mGp.localFileListView.getChildCount(); j++)
                        mGp.localFileListView.getChildAt(j).setBackgroundColor(Color.TRANSPARENT);
                    setEmptyFolderView();

                    //Cache
//                    mGp.localFileListAdapter.setDataList(dhi.file_list);
//                    mGp.localFileListAdapter.notifyDataSetChanged();
//                    mGp.localFileListView.setSelectionFromTop(dhi.pos_top, dhi.pos_fv);
//                    if (dhl.size()>1) {
//                        mGp.localFileListTopBtn.setEnabled(true);
//                        mGp.localFileListUpBtn.setEnabled(true);
//                    } else {
//                        mGp.localFileListTopBtn.setEnabled(false);
//                        mGp.localFileListUpBtn.setEnabled(false);
//                    }
//
//                    mGp.tabHost.getTabWidget().getChildTabViewAt(SMBEXPLORER_TAB_POS_LOCAL).setEnabled(true);
//                    setFileListPathName(mGp.localFileListPath,mGp.localBase,mGp.localDir);
//                    setLocalContextButtonStatus();
                } else {
                    updateDirectoryHistoryItem(mGp.localBase, mGp.localDir, mGp.localFileListView, mGp.localFileListAdapter);
                    mGp.localDir="";
                    mGp.localBase=turl;
                    loadLocalFilelist(mGp.localBase, mGp.localDir, null);
                    mGp.localFileListView.setSelection(0);
                    for (int j = 0; j < mGp.localFileListView.getChildCount(); j++)
                        mGp.localFileListView.getChildAt(j).setBackgroundColor(Color.TRANSPARENT);
                    setEmptyFolderView();
//                    addMountPointHistoryItem(mGp.localBase, mGp.localDir, mGp.localFileListAdapter.getDataList());
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        mGp.localFileListUpBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                processLocalUpButton();
            }
        });

        mGp.localFileListTopBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                processLocalTopButton();
            }
        });

        setLocalContextButtonStatus();
        NotifyEvent cb=new NotifyEvent(mContext);
        cb.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {setLocalContextButtonStatus();}
            @Override
            public void negativeResponse(Context context, Object[] objects) {setLocalContextButtonStatus();}
        });
        mGp.localFileListAdapter.setCbCheckListener(cb);
    }

    public void setLocalContextButtonStatus() {
        int sel_cnt=mGp.localFileListAdapter.getCheckedItemCount();
        if (sel_cnt==1) {
            mGp.localContextBtnCreateView.setVisibility(LinearLayout.INVISIBLE);
            mGp.localContextBtnCopyView.setVisibility(LinearLayout.VISIBLE);
            mGp.localContextBtnCutView.setVisibility(LinearLayout.VISIBLE);
            mGp.localContextBtnPasteView.setVisibility(LinearLayout.INVISIBLE);
            mGp.localContextBtnRenameView.setVisibility(LinearLayout.VISIBLE);
            mGp.localContextBtnDeleteView.setVisibility(LinearLayout.VISIBLE);
            mGp.localContextBtnSelectAllView.setVisibility(LinearLayout.VISIBLE);
            mGp.localContextBtnUnselectAllView.setVisibility(LinearLayout.VISIBLE);
        } else if (sel_cnt>1) {
            mGp.localContextBtnCreateView.setVisibility(LinearLayout.INVISIBLE);
            mGp.localContextBtnCopyView.setVisibility(LinearLayout.VISIBLE);
            mGp.localContextBtnCutView.setVisibility(LinearLayout.VISIBLE);
            mGp.localContextBtnPasteView.setVisibility(LinearLayout.INVISIBLE);
            mGp.localContextBtnRenameView.setVisibility(LinearLayout.INVISIBLE);
            mGp.localContextBtnDeleteView.setVisibility(LinearLayout.VISIBLE);
            mGp.localContextBtnSelectAllView.setVisibility(LinearLayout.VISIBLE);
            mGp.localContextBtnUnselectAllView.setVisibility(LinearLayout.VISIBLE);
        } else {
            mGp.localContextBtnCreateView.setVisibility(LinearLayout.VISIBLE);
            mGp.localContextBtnCopyView.setVisibility(LinearLayout.INVISIBLE);
            mGp.localContextBtnCutView.setVisibility(LinearLayout.INVISIBLE);
//            if (isPasteEnabled) localContextBtnPasteView.setVisibility(LinearLayout.VISIBLE);
//            else localContextBtnPasteView.setVisibility(LinearLayout.INVISIBLE);
            setPasteButtonEnabled();
            mGp.localContextBtnRenameView.setVisibility(LinearLayout.INVISIBLE);
            mGp.localContextBtnDeleteView.setVisibility(LinearLayout.INVISIBLE);
            mGp.localContextBtnSelectAllView.setVisibility(LinearLayout.VISIBLE);
            mGp.localContextBtnUnselectAllView.setVisibility(LinearLayout.VISIBLE);
        }
    }

    public void setRemoteContextButtonStatus() {
        int sel_cnt=mGp.remoteFileListAdapter.getCheckedItemCount();
        if (sel_cnt==1) {
            mGp.remoteContextBtnCreateView.setVisibility(LinearLayout.INVISIBLE);
            mGp.remoteContextBtnCopyView.setVisibility(LinearLayout.VISIBLE);
            mGp.remoteContextBtnCutView.setVisibility(LinearLayout.VISIBLE);
            mGp.remoteContextBtnPasteView.setVisibility(LinearLayout.INVISIBLE);
            mGp.remoteContextBtnRenameView.setVisibility(LinearLayout.VISIBLE);
            mGp.remoteContextBtnDeleteView.setVisibility(LinearLayout.VISIBLE);
            mGp.remoteContextBtnSelectAllView.setVisibility(LinearLayout.VISIBLE);
            mGp.remoteContextBtnUnselectAllView.setVisibility(LinearLayout.VISIBLE);
        } else if (sel_cnt>1) {
            mGp.remoteContextBtnCreateView.setVisibility(LinearLayout.INVISIBLE);
            mGp.remoteContextBtnCopyView.setVisibility(LinearLayout.VISIBLE);
            mGp.remoteContextBtnCutView.setVisibility(LinearLayout.VISIBLE);
            mGp.remoteContextBtnPasteView.setVisibility(LinearLayout.INVISIBLE);
            mGp.remoteContextBtnRenameView.setVisibility(LinearLayout.INVISIBLE);
            mGp.remoteContextBtnDeleteView.setVisibility(LinearLayout.VISIBLE);
            mGp.remoteContextBtnSelectAllView.setVisibility(LinearLayout.VISIBLE);
            mGp.remoteContextBtnUnselectAllView.setVisibility(LinearLayout.VISIBLE);
        } else {
            mGp.remoteContextBtnCreateView.setVisibility(LinearLayout.VISIBLE);
            mGp.remoteContextBtnCopyView.setVisibility(LinearLayout.INVISIBLE);
            mGp.remoteContextBtnCutView.setVisibility(LinearLayout.INVISIBLE);
//            if (isPasteEnabled) remoteContextBtnPasteView.setVisibility(LinearLayout.VISIBLE);
//            else remoteContextBtnPasteView.setVisibility(LinearLayout.INVISIBLE);
            setPasteButtonEnabled();
            mGp.remoteContextBtnRenameView.setVisibility(LinearLayout.INVISIBLE);
            mGp.remoteContextBtnDeleteView.setVisibility(LinearLayout.INVISIBLE);
            mGp.remoteContextBtnSelectAllView.setVisibility(LinearLayout.VISIBLE);
            mGp.remoteContextBtnUnselectAllView.setVisibility(LinearLayout.VISIBLE);
        }
    }

    private String getParentDirectory(String c_dir) {
        String result="";

        if (c_dir.lastIndexOf("/")>0) {
            result=c_dir.substring(0,c_dir.lastIndexOf("/"));
        }
        return result;
    }

    public boolean processBackKey() {
        boolean result=false;
        if (mGp.currentTabName.equals(SMBEXPLORER_TAB_LOCAL)) {
            if (mGp.localFileListAdapter.isItemSelected()) {
                mGp.localFileListAdapter.setAllItemChecked(false);
                result=true;
            }
        } else if (mGp.currentTabName.equals(SMBEXPLORER_TAB_REMOTE)) {
            if (mGp.remoteFileListAdapter.isItemSelected()) {
                mGp.remoteFileListAdapter.setAllItemChecked(false);
                result=true;
            }
        }
        return result;
    }

    private void processLocalUpButton() {
        MountPointHistoryItem mphi=getMountPointHistoryItem(mGp.localBase);
        mphi.directory_history.remove(mphi.directory_history.size()-1);
        DirectoryHistoryItem n_dhi=mphi.directory_history.get(mphi.directory_history.size()-1);

        if (mphi.directory_history.size()==1) {
            mGp.localFileListUpBtn.setEnabled(false);
            mGp.localFileListTopBtn.setEnabled(false);
        }
        NotifyEvent ntfy=new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                ArrayList<FileListItem> tfl=(ArrayList<FileListItem>)objects[0];
                if (tfl==null) return;
                mGp.localDir=n_dhi.directory_name;
                mGp.localFileListAdapter.setDataList(tfl);
                mGp.localFileListAdapter.notifyDataSetChanged();
                mGp.localFileListView.setSelectionFromTop(n_dhi.pos_fv, n_dhi.pos_top);
                setFileListPathName(mGp.localFileListPath,mGp.localBase,mGp.localDir);
                setEmptyFolderView();
            }

            @Override
            public void negativeResponse(Context context, Object[] objects) {}
        });
        if (mGp.localBase.startsWith(mGp.safMgr.getUsbRootPath())) createUsbFileList(false, mGp.localBase+"/"+n_dhi.directory_name, ntfy);
        else createLocalFileList(false,mGp.localBase+"/"+n_dhi.directory_name, ntfy);
//        ntfy.notifyToListener(true, new Object[]{n_dhi.file_list});
    }

    private void processLocalTopButton() {
        MountPointHistoryItem mphi=getMountPointHistoryItem(mGp.localBase);
        DirectoryHistoryItem n_dhi=mphi.directory_history.get(0);
        mphi.directory_history.clear();
        mphi.directory_history.add(n_dhi);

        NotifyEvent ntfy=new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                ArrayList<FileListItem> tfl=(ArrayList<FileListItem>)objects[0];
                if (tfl==null) return;
                mGp.localDir="";
                mGp.localFileListAdapter.setDataList(tfl);
                mGp.localFileListAdapter.notifyDataSetChanged();
                setFileListPathName(mGp.localFileListPath,mGp.localBase,mGp.localDir);
                mGp.localFileListView.setSelectionFromTop(n_dhi.pos_fv, n_dhi.pos_top);
                setEmptyFolderView();
                for (int j = 0; j < mGp.localFileListView.getChildCount(); j++)
                    mGp.localFileListView.getChildAt(j).setBackgroundColor(Color.TRANSPARENT);
                mGp.localFileListTopBtn.setEnabled(false);
                mGp.localFileListUpBtn.setEnabled(false);
            }

            @Override
            public void negativeResponse(Context context, Object[] objects) {}
        });
        if (mGp.localBase.startsWith(mGp.safMgr.getUsbRootPath())) createUsbFileList(false, n_dhi.directory_name, ntfy);
        else createLocalFileList(false,mGp.localBase, ntfy);
//        ntfy.notifyToListener(true, new Object[]{n_dhi.file_list});
    }

    public void setEmptyFolderView() {
        if (mGp.localFileListAdapter!=null) {
            if (mGp.localFileListAdapter.getCount()>0) {
                mGp.localFileListEmptyView.setVisibility(TextView.GONE);
                mGp.localFileListView.setVisibility(ListView.VISIBLE);
            } else {
                mGp.localFileListEmptyView.setVisibility(TextView.VISIBLE);
                mGp.localFileListView.setVisibility(ListView.GONE);
            }
        } else {
            mGp.localFileListEmptyView.setVisibility(TextView.VISIBLE);
            mGp.localFileListView.setVisibility(ListView.GONE);
        }
        if (mGp.remoteFileListAdapter!=null) {
            LinearLayout ll_context=(LinearLayout)mGp.mRemoteView.findViewById(R.id.context_view_file);
            if (mGp.remoteFileListAdapter.getCount()>0 || !mGp.remoteBase.equals("")) {
                ll_context.setVisibility(LinearLayout.VISIBLE);
                mGp.remoteFileListEmptyView.setVisibility(TextView.GONE);
                mGp.remoteFileListView.setVisibility(ListView.VISIBLE);
            } else {
                ll_context.setVisibility(LinearLayout.GONE);
                mGp.remoteFileListEmptyView.setVisibility(TextView.VISIBLE);
                mGp.remoteFileListView.setVisibility(ListView.GONE);
            }
        } else {
            mGp.remoteFileListEmptyView.setVisibility(TextView.VISIBLE);
            mGp.remoteFileListView.setVisibility(ListView.GONE);
            LinearLayout ll_context=(LinearLayout)mGp.mRemoteView.findViewById(R.id.context_view_file);
            ll_context.setVisibility(LinearLayout.GONE);
        }
    }

    static public void setRemoteTabEnabled(GlobalParameters mGp) {
        if (mGp.wifiSsid.equals("")) {
            mGp.remoteFileListDirSpinner.setEnabled(false);
            mGp.remoteFileListView.setEnabled(false);
            mGp.remoteFileListTopBtn.setEnabled(false);
            mGp.remoteFileListUpBtn.setEnabled(false);
        } else {
            mGp.remoteFileListDirSpinner.setEnabled(true);
            mGp.remoteFileListView.setEnabled(true);
            if (mGp.remoteDir.equals("")) {
                mGp.remoteFileListTopBtn.setEnabled(false);
                mGp.remoteFileListUpBtn.setEnabled(false);
            } else {
                mGp.remoteFileListTopBtn.setEnabled(true);
                mGp.remoteFileListUpBtn.setEnabled(true);
            }
        }
    }

    public void setRemoteDirBtnListener() {
        final CustomSpinnerAdapter spAdapter = new CustomSpinnerAdapter(mActivity, android.R.layout.simple_spinner_item);
        spAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        mGp.remoteFileListDirSpinner.setPrompt("リモートの選択");
        mGp.remoteFileListDirSpinner.setAdapter(spAdapter);
        if (mGp.remoteBase.equals("")) spAdapter.add("--- Not selected ---");
        int a_no=0;
        for (int i = 0; i<mGp.smbConfigList.size(); i++) {
            spAdapter.add(mGp.smbConfigList.get(i).getName());
            String surl=buildRemoteBase(mGp.smbConfigList.get(i));
            if (surl.equals(mGp.remoteBase))
                mGp.remoteFileListDirSpinner.setSelection(a_no);
            a_no++;
        }
        setRemoteTabEnabled(mGp);
        mGp.remoteFileListDirSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isSpinnerSelectionEnabled()) return;
                Spinner spinner = (Spinner) parent;
                if (((String)spinner.getSelectedItem()).startsWith("---")) return;

                String sel_item=(String)spinner.getSelectedItem();
                if (spAdapter.getItem(0).startsWith("---")) {
                    spAdapter.remove(spAdapter.getItem(0));
                    spinner.setSelection(position-1);
                }
                if (sel_item.startsWith("---")) return;

                SmbServerConfig pli=null;
                for (int i = 0; i<mGp.smbConfigList.size(); i++) {
                    if (mGp.smbConfigList.get(i).getName().equals(sel_item)) {
                        pli=mGp.smbConfigList.get(i);
                        break;
                    }
                }

                String turl=buildRemoteBase(pli);
                MountPointHistoryItem mphi=getMountPointHistoryItem(turl);
                if (mphi!=null) {
                    ArrayList<DirectoryHistoryItem> dhl=mphi.directory_history;
                    DirectoryHistoryItem dhi=dhl.get(dhl.size()-1);
                    mGp.currentSmbServerConfig =pli;
                    mGp.remoteBase=mphi.mp_name;
                    mGp.remoteDir=dhi.directory_name;

                    loadRemoteFilelist(mGp.remoteBase, mGp.remoteDir);
                    mGp.remoteFileListView.setSelection(0);
                    for (int j = 0; j < mGp.remoteFileListView.getChildCount(); j++)
                        mGp.remoteFileListView.getChildAt(j).setBackgroundColor(Color.TRANSPARENT);

                    //Cache
//                    mGp.remoteFileListAdapter.setDataList(dhi.file_list);
//                    mGp.remoteFileListAdapter.notifyDataSetChanged();
//                    mGp.remoteFileListView.setSelectionFromTop(dhi.pos_top, dhi.pos_fv);
//                    if (dhl.size()>1) {
//                        mGp.remoteFileListTopBtn.setEnabled(true);
//                        mGp.remoteFileListUpBtn.setEnabled(true);
//                    } else {
//                        mGp.remoteFileListTopBtn.setEnabled(false);
//                        mGp.remoteFileListUpBtn.setEnabled(false);
//                    }
//
//                    mGp.tabHost.getTabWidget().getChildTabViewAt(SMBEXPLORER_TAB_POS_LOCAL).setEnabled(true);
//                    setEmptyFolderView();
//                    setFileListPathName(mGp.remoteFileListPath,mGp.remoteBase,mGp.remoteDir);
//                    setRemoteContextButtonStatus();
                } else {
                    updateDirectoryHistoryItem(mGp.remoteBase, mGp.remoteDir, mGp.remoteFileListView, mGp.remoteFileListAdapter);
                    mGp.tabHost.getTabWidget().getChildTabViewAt(SMBEXPLORER_TAB_POS_REMOTE).setEnabled(true);
                    mGp.currentSmbServerConfig =pli;
                    mGp.remoteBase = turl;
                    mGp.remoteDir="";
                    addMountPointHistoryItem(mGp.remoteBase, mGp.remoteDir, new ArrayList<FileListItem>());

                    loadRemoteFilelist(mGp.remoteBase, mGp.remoteDir);
                    mGp.remoteFileListView.setSelection(0);
                    for (int j = 0; j < mGp.remoteFileListView.getChildCount(); j++)
                        mGp.remoteFileListView.getChildAt(j).setBackgroundColor(Color.TRANSPARENT);
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        mGp.remoteFileListUpBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                processRemoteUpButton();
            }
        });

        mGp.remoteFileListTopBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                processRemoteTopButton();
            }
        });

        setRemoteContextButtonStatus();
        NotifyEvent cb=new NotifyEvent(mContext);
        cb.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                setRemoteContextButtonStatus();
            }
            @Override
            public void negativeResponse(Context context, Object[] objects) {}
        });
        mGp.remoteFileListAdapter.setCbCheckListener(cb);

    }

    private void processRemoteUpButton() {
        MountPointHistoryItem mphi=getMountPointHistoryItem(mGp.remoteBase);
        mphi.directory_history.remove(mphi.directory_history.size()-1);
        DirectoryHistoryItem n_dhi=mphi.directory_history.get(mphi.directory_history.size()-1);

        if (mphi.directory_history.size()==1) {
            mGp.remoteFileListUpBtn.setEnabled(false);
            mGp.remoteFileListTopBtn.setEnabled(false);
        }
        NotifyEvent ntfy=new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                ArrayList<FileListItem> tfl=(ArrayList<FileListItem>)objects[0];
                if (tfl==null) return;
                mGp.remoteDir=n_dhi.directory_name;
                mGp.remoteFileListAdapter.setDataList(tfl);
                mGp.remoteFileListAdapter.notifyDataSetChanged();
                setFileListPathName(mGp.remoteFileListPath,mGp.remoteBase,mGp.remoteDir);
                mGp.remoteFileListView.setSelectionFromTop(n_dhi.pos_fv, n_dhi.pos_top);
                setEmptyFolderView();
                for (int j = 0; j < mGp.remoteFileListView.getChildCount(); j++)
                    mGp.remoteFileListView.getChildAt(j).setBackgroundColor(Color.TRANSPARENT);
            }

            @Override
            public void negativeResponse(Context context, Object[] objects) {}
        });
        createRemoteFileList(RetrieveFileList.OPCD_FILE_LIST, mGp.remoteBase+"/"+n_dhi.directory_name, ntfy);
    }

    private void processRemoteTopButton() {
        MountPointHistoryItem mphi=getMountPointHistoryItem(mGp.remoteBase);
        DirectoryHistoryItem n_dhi=mphi.directory_history.get(0);
        mphi.directory_history.clear();
        mphi.directory_history.add(n_dhi);

        mGp.remoteFileListUpBtn.setEnabled(false);
        mGp.remoteFileListTopBtn.setEnabled(false);

        NotifyEvent ntfy=new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                ArrayList<FileListItem> tfl=(ArrayList<FileListItem>)objects[0];
                if (tfl==null) return;
                mGp.remoteBase=n_dhi.directory_name;
                mGp.remoteDir="";
                mGp.remoteFileListAdapter.setDataList(tfl);
                mGp.remoteFileListAdapter.notifyDataSetChanged();
                setFileListPathName(mGp.remoteFileListPath,mGp.remoteBase,mGp.remoteDir);
                mGp.remoteFileListView.setSelectionFromTop(n_dhi.pos_fv, n_dhi.pos_top);
                setEmptyFolderView();
                for (int j = 0; j < mGp.remoteFileListView.getChildCount(); j++)
                    mGp.remoteFileListView.getChildAt(j).setBackgroundColor(Color.TRANSPARENT);
            }

            @Override
            public void negativeResponse(Context context, Object[] objects) {}
        });
        createRemoteFileList(RetrieveFileList.OPCD_FILE_LIST, mGp.remoteBase+"/"+n_dhi.directory_name, ntfy);
    }

    private String buildRemoteBase(SmbServerConfig pli) {
        String url="", sep="";
        if (!pli.getPort().equals("")) sep=":";
        url = "smb://"+pli.getAddr()+sep+pli.getPort()+"/"+pli.getShare() ;
        return url;
    }

    public void setLocalFilelistItemClickListener() {
        if (mGp.localFileListView==null) return;
        mGp.localFileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int j = 0; j < parent.getChildCount(); j++)
                    parent.getChildAt(j).setBackgroundColor(Color.TRANSPARENT);
                FileListItem item = mGp.localFileListAdapter.getItem(position);
                if (!mActivity.isUiEnabled()) return;
                if (mGp.localFileListAdapter.isItemSelected()) {
                    item.setChecked(!item.isChecked());
                    mGp.localFileListAdapter.notifyDataSetChanged();
                } else {
                    mActivity.setUiEnabled(false);
                    mUtil.addDebugMsg(1,"I","Local filelist item clicked :" + item.getName());
                    if (item.isDir()) {
                        updateDirectoryHistoryItem(mGp.localBase, mGp.localDir, mGp.localFileListView, mGp.localFileListAdapter);

                        NotifyEvent ntfy=new NotifyEvent(mContext);
                        ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                            @Override
                            public void positiveResponse(Context context, Object[] objects) {
                                ArrayList<FileListItem> tfl=(ArrayList<FileListItem>)objects[0];
                                if (tfl==null) return;
                                String t_dir=item.getPath()+"/"+item.getName();
                                mGp.localDir=t_dir.replace(mGp.localBase+"/", "");
                                mGp.localFileListAdapter.setDataList(tfl);
                                mGp.localFileListAdapter.notifyDataSetChanged();
                                for (int j = 0; j < mGp.localFileListView.getChildCount(); j++)
                                    mGp.localFileListView.getChildAt(j).setBackgroundColor(Color.TRANSPARENT);
//                                setFilelistCurrDir(mGp.localFileListDirSpinner,mGp.localBase, mGp.localDir);
                                setFileListPathName(mGp.localFileListPath,mGp.localBase,mGp.localDir);
                                setEmptyFolderView();
                                mGp.localFileListView.setSelection(0);
                                mGp.localFileListTopBtn.setEnabled(true);
                                mGp.localFileListUpBtn.setEnabled(true);

                                addDirectoryHistoryItem(mGp.localBase, mGp.localDir, tfl);

                                mActivity.setUiEnabled(true);
                            }

                            @Override
                            public void negativeResponse(Context context, Object[] objects) {}
                        });
                        if (mGp.localBase.startsWith(mGp.safMgr.getUsbRootPath())) {
                            if (mGp.localDir.equals("")) createUsbFileList(false, item.getPath()+"/"+item.getName(), ntfy);
                            else createUsbFileList(false, item.getPath()+"/"+item.getName(), ntfy);
                        } else {
                            createLocalFileList(false,item.getPath()+"/"+item.getName(), ntfy);
                        }
                    } else {
                        if (isFileListItemSelected(mGp.localFileListAdapter)) {
                            item.setChecked(!item.isChecked());
                            mGp.localFileListAdapter.notifyDataSetChanged();
                            mActivity.setUiEnabled(true);
                        } else {
                            mActivity.setUiEnabled(true);
                            startLocalFileViewerIntent(item, null);
                        }
                    }
                }
            }
        });
    }

    public void setFileListPathName(CustomTextView btn, String base, String dir) {
        if (dir.startsWith("/")) btn.setText(dir);
        else btn.setText("/"+dir);
        setPasteButtonEnabled();
    }

    public void setLocalFilelistLongClickListener() {
        if (mGp.localFileListView==null) return;
        mGp.localFileListView
                .setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        if (!mActivity.isUiEnabled()) return true;
                        createFilelistContextMenu(arg1, arg2,mGp.localFileListAdapter);
                        return true;
                    }
                });
    }

    public void setRemoteContextButtonListener() {
        mGp.remoteContextBtnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCopyFrom(mGp.remoteFileListAdapter);
            }
        });
        mGp.remoteContextBtnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mGp.remoteDir.length()==0) createItem(mGp.remoteFileListAdapter,"C", mGp.remoteBase);
                else createItem(mGp.remoteFileListAdapter,"C", mGp.remoteBase+"/"+mGp.remoteDir);
            }
        });
        mGp.remoteContextBtnCut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCutFrom(mGp.remoteFileListAdapter);
            }
        });
        mGp.remoteContextBtnPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String to_dir="";
                if (mGp.remoteDir.equals("")) to_dir=mGp.remoteBase;
                else to_dir=mGp.remoteBase+"/"+mGp.remoteDir;
                pasteItem(mGp.remoteFileListAdapter, to_dir, mGp.remoteBase);
            }
        });
        mGp.remoteContextBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteItem(mGp.remoteFileListAdapter);
            }
        });
        mGp.remoteContextBtnRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renameItem(mGp.remoteFileListAdapter);
                mGp.remoteFileListAdapter.setAllItemChecked(false);
            }
        });
        mGp.remoteContextBtnUnselectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGp.remoteFileListAdapter.setAllItemChecked(false);
            }
        });
        mGp.remoteContextBtnSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGp.remoteFileListAdapter.setAllItemChecked(true);
            }
        });
    }

    public void setLocalContextButtonListener() {
        mGp.localContextBtnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCopyFrom(mGp.localFileListAdapter);
            }
        });
        mGp.localContextBtnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mGp.localDir.length()==0) createItem(mGp.localFileListAdapter,"C", mGp.localBase);
                else createItem(mGp.localFileListAdapter,"C", mGp.localBase+"/"+mGp.localDir);
            }
        });
        mGp.localContextBtnCut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCutFrom(mGp.localFileListAdapter);
            }
        });
        mGp.localContextBtnPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String to_dir="";
                if (mGp.localDir.equals("")) to_dir=mGp.localBase;
                else to_dir=mGp.localBase+"/"+mGp.localDir;
                pasteItem(mGp.localFileListAdapter, to_dir, mGp.localBase);
            }
        });
        mGp.localContextBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteItem(mGp.localFileListAdapter);
            }
        });
        mGp.localContextBtnRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renameItem(mGp.localFileListAdapter);
                mGp.localFileListAdapter.setAllItemChecked(false);
            }
        });
        mGp.localContextBtnUnselectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGp.localFileListAdapter.setAllItemChecked(false);
            }
        });
        mGp.localContextBtnSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGp.localFileListAdapter.setAllItemChecked(true);
            }
        });
    }

    public void setRemoteFilelistItemClickListener() {
        if (mGp.remoteFileListView==null) return;
        mGp.remoteFileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                for (int j = 0; j < parent.getChildCount(); j++)
                    parent.getChildAt(j).setBackgroundColor(Color.TRANSPARENT);
                if (!mActivity.isUiEnabled()) return;
                final FileListItem item = mGp.remoteFileListAdapter.getItem(position);
                mUtil.addDebugMsg(1,"I","Remote filelist item clicked :" + item.getName());
                if (mGp.remoteFileListAdapter.isItemSelected()) {
                    item.setChecked(!item.isChecked());
                    mGp.remoteFileListAdapter.notifyDataSetChanged();
                } else {
                    mActivity.setUiEnabled(false);
                    if (item.isDir()) {
                        NotifyEvent ne=new NotifyEvent(mContext);
                        ne.setListener(new NotifyEvent.NotifyEventListener() {
                            @Override
                            public void positiveResponse(Context c,Object[] o) {
                                String t_dir=item.getPath()+"/"+item.getName();

                                updateDirectoryHistoryItem(mGp.remoteBase, mGp.remoteDir, mGp.remoteFileListView, mGp.remoteFileListAdapter);

                                mGp.remoteDir=t_dir.replace(mGp.remoteBase+"/", "");

                                addDirectoryHistoryItem(mGp.remoteBase, mGp.remoteDir, mGp.remoteFileListAdapter.getDataList());

                                mGp.remoteFileListAdapter.setDataList((ArrayList<FileListItem>)o[0]);
                                mGp.remoteFileListAdapter.notifyDataSetChanged();
                                for (int j = 0; j < mGp.remoteFileListView.getChildCount(); j++)
                                    mGp.remoteFileListView.getChildAt(j).setBackgroundColor(Color.TRANSPARENT);
//								setFilelistCurrDir(mGp.remoteFileListDirSpinner,mGp.remoteBase, mGp.remoteDir);
                                setFileListPathName(mGp.remoteFileListPath,mGp.remoteBase,mGp.remoteDir);
                                setEmptyFolderView();
                                mGp.remoteFileListView.setSelection(0);

                                mActivity.setUiEnabled(true);

                                mGp.remoteFileListTopBtn.setEnabled(true);
                                mGp.remoteFileListUpBtn.setEnabled(true);
                            }
                            @Override
                            public void negativeResponse(Context c,Object[] o) {
                                mActivity.setUiEnabled(true);
                            }
                        });
                        String t_dir=item.getPath()+"/"+item.getName();
                        createRemoteFileList(RetrieveFileList.OPCD_FILE_LIST, item.getPath()+"/"+item.getName(),ne);
                    } else {
                        mActivity.setUiEnabled(true);
                        if (isFileListItemSelected(mGp.remoteFileListAdapter)) {
                            item.setChecked(!item.isChecked());
                            mGp.remoteFileListAdapter.notifyDataSetChanged();
                        } else {
//				            view.setBackgroundColor(Color.DKGRAY);
                            startRemoteFileViewerIntent(mGp.remoteFileListAdapter, item);
                            //mGp.commonDlg.showCommonDialog(false,false,"E","","Remote file was not viewd.",null);
                        }
                    }
                }
            }
        });
    };

    private boolean isFileListItemSelected(FileListAdapter tfa) {
        boolean result=false;
        for (int i=0;i<tfa.getCount();i++) {
            if (tfa.getItem(i).isChecked()) {
                result=true;
                break;
            }
        }
        return result;
    }

    public void setRemoteFilelistLongClickListener() {
        if (mGp.remoteFileListView==null) return;
        mGp.remoteFileListView
                .setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        if (!mActivity.isUiEnabled()) return true;
                        createFilelistContextMenu(arg1, arg2, mGp.remoteFileListAdapter);
                        return true;
                    }
                });
    };

    private void createFilelistContextMenu(View view, int idx, final FileListAdapter fla) {
        mGp.fileioLinkParm.clear();
        final FileListItem item=fla.getItem(idx);
        ccMenu.addMenuItem("Property("+item.getName()+")",R.drawable.menu_properties).setOnClickListener(new CustomContextMenuItem.CustomContextMenuOnClickListener() {
            @Override
            public void onClick(CharSequence menuTitle) {
                showProperty(fla,"C", item.getName(), item.isDir(),idx);
//                setAllFilelistItemUnChecked(fla);
            }
        });
        if (mGp.currentTabName.equals(SMBEXPLORER_TAB_LOCAL) && item.isDir() && !item.getName().startsWith(".") && item.getPath().indexOf("/.")<0) {
            if (mGp.localBase.startsWith(mGp.internalRootDirectory) || mGp.localBase.startsWith(mGp.safMgr.getSdcardRootPath())) {
                ccMenu.addMenuItem("Scan media file", R.drawable.context_button_media_file_scan).setOnClickListener(new CustomContextMenuItem.CustomContextMenuOnClickListener() {
                    @Override
                    public void onClick(CharSequence menuTitle) {
                        scanLocalMediaFile(item);
//                    setAllFilelistItemUnChecked(fla);
                    }
                });
            }
        }
        if (mGp.currentTabName.equals(SMBEXPLORER_TAB_LOCAL) && !item.isDir()) {
            ccMenu.addMenuItem("Open with Text file("+item.getName()+")").setOnClickListener(new CustomContextMenuItem.CustomContextMenuOnClickListener() {
                @Override
                public void onClick(CharSequence menuTitle) {
                    startLocalFileViewerIntent(item, "text/plain");
//                    setAllFilelistItemUnChecked(fla);
                }
            });
            ccMenu.addMenuItem("Open with Zip file("+item.getName()+")").setOnClickListener(new CustomContextMenuItem.CustomContextMenuOnClickListener() {
                @Override
                public void onClick(CharSequence menuTitle) {
                    startLocalFileViewerIntent(item, "application/zip");
//                    setAllFilelistItemUnChecked(fla);
                }
            });
            ccMenu.addMenuItem("Share("+item.getName()+")",R.drawable.context_button_share_dark).setOnClickListener(new CustomContextMenuItem.CustomContextMenuOnClickListener() {
                @Override
                public void onClick(CharSequence menuTitle) {
                    String fid="", mt=null;
                    if (item.getName().lastIndexOf(".") > 0) {
                        fid = item.getName().substring(item.getName().lastIndexOf(".") + 1, item.getName().length());
                        fid=fid.toLowerCase();
                    }
                    mt= MimeTypeMap.getSingleton().getMimeTypeFromExtension(fid);

                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    File lf=new File(item.getPath()+"/"+item.getName());
                    Uri uri =null;
                    if (Build.VERSION.SDK_INT>=26)  uri= FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", lf);
                    else uri=Uri.parse("file://"+lf.getPath());
//                    uri=Uri.parse("file://"+lf.getPath());
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    if (mt!=null) intent.setType(mt);
                    else intent.setType("*/*");

                    try {
                        mContext.startActivity(intent);
                    } catch(Exception e) {
                        mGp.commonDlg.showCommonDialog(false, "E", "Share error", "startActivity() failed at shareItem() for send item. message="+e.getMessage(), null);
                    }

                }
            });
        }
        ccMenu.addMenuItem("Copy("+item.getName()+")",R.drawable.context_button_copy).setOnClickListener(new CustomContextMenuItem.CustomContextMenuOnClickListener() {
            @Override
            public void onClick(CharSequence menuTitle) {
                FileListAdapter tfla=new FileListAdapter(mContext);
                ArrayList<FileListItem>dl=new ArrayList<FileListItem>();
                FileListItem n_item=item.clone();
                n_item.setChecked(true);
                dl.add(n_item);
                tfla.setDataList(dl);

                setCopyFrom(tfla);
            }
        });
        ccMenu.addMenuItem("Cut("+item.getName()+")",R.drawable.context_button_cut).setOnClickListener(new CustomContextMenuItem.CustomContextMenuOnClickListener() {
            @Override
            public void onClick(CharSequence menuTitle) {
                FileListAdapter tfla=new FileListAdapter(mContext);
                ArrayList<FileListItem>dl=new ArrayList<FileListItem>();
                FileListItem n_item=item.clone();
                n_item.setChecked(true);
                dl.add(n_item);
                tfla.setDataList(dl);

                setCutFrom(tfla);
            }
        });
        ccMenu.addMenuItem("Rename("+item.getName()+")",R.drawable.context_button_rename).setOnClickListener(new CustomContextMenuItem.CustomContextMenuOnClickListener() {
            @Override
            public void onClick(CharSequence menuTitle) {
                FileListAdapter tfla=new FileListAdapter(mContext);
                ArrayList<FileListItem>dl=new ArrayList<FileListItem>();
                FileListItem n_item=item.clone();
                n_item.setChecked(true);
                dl.add(n_item);
                tfla.setDataList(dl);

                renameItem(tfla);
            }
        });
        ccMenu.addMenuItem("Delete("+item.getName()+")",R.drawable.context_button_trash).setOnClickListener(new CustomContextMenuItem.CustomContextMenuOnClickListener() {
            @Override
            public void onClick(CharSequence menuTitle) {
                FileListAdapter tfla=new FileListAdapter(mContext);
                ArrayList<FileListItem>dl=new ArrayList<FileListItem>();
                FileListItem n_item=item.clone();
                n_item.setChecked(true);
                dl.add(n_item);
                tfla.setDataList(dl);

                deleteItem(tfla);
            }
        });
//        ccMenu.addMenuItem("Select").setOnClickListener(new CustomContextMenuItem.CustomContextMenuOnClickListener() {
//            @Override
//            public void onClick(CharSequence menuTitle) {
//                item.setChecked(true);
//                fla.notifyDataSetChanged();
//            }
//        });
        ccMenu.createMenu();
    };

//    private void setFilelistItemToChecked(FileListAdapter fla, int pos, boolean p) {
//        fla.getItem(pos).setChecked(p);
//    }

//    private void invokeTextFileBrowser(FileListAdapter fla, final String item_optyp, final String item_name,
//                                       final boolean item_isdir, final int item_num) {
//        FileListItem item=fla.getItem(item_num);
//        try {
//            Intent intent;
//            intent = new Intent();
//            intent.setDataAndType( Uri.parse("file://"+item.getPath()+"/"+item.getName()), "text/plain");
//            mContext.startActivity(intent);
//        } catch(ActivityNotFoundException e) {
//            showDialogMsg("E", "Can not find the text file viewer.", " File name="+item.getName());
//        }
//    }

    private void sendMsgToProgDlg(Handler hndl, final String log_msg) {
        hndl.post(new Runnable() {// UI thread
            @Override
            public void run() {
                mGp.progressMsgView.setText(log_msg);
//					Log.v("","pop="+log_msg);
            }
        });
    }

    private int mScanDeleteCount=0, mScanUpdateCount=0, mScanAddCount=0;
    private void scanLocalMediaFile(FileListItem scan_dir) {
        mUtil.addDebugMsg(1,"I","Scan started");
        final Handler hndl=new Handler();
        mGp.progressMsgView=mGp.localProgressMsg;
        mGp.progressCancelBtn=mGp.localProgressCancel;
        showLocalProgressView();
        Thread th=new Thread(){
            @Override
            public void run() {
                sendMsgToProgDlg(hndl, "Start media file scan");
                mScanDeleteCount=mScanUpdateCount=mScanAddCount=0;
                ArrayList<File>fl=new ArrayList<File>();
                File lf=new File(scan_dir.getPath()+"/"+scan_dir.getName());
                getAllMediaFileInDirectory(fl, lf, true);

                ArrayList<FileListItem> ml=new ArrayList<FileListItem>();
                for(File item:fl ) {
                    FileListItem entry=null;
                    entry=new FileListItem(item.getName(), false, item.length(), item.lastModified(), false, item.canRead(), item.canWrite(),
                            item.isHidden(),item.getParent(), 0);
                    ml.add(entry);
                }
                Collections.sort(ml, new Comparator<FileListItem>(){
                    @Override
                    public int compare(FileListItem l, FileListItem r) {
                        return ((l.getPath()+"/"+l.getName())).compareToIgnoreCase((r.getPath()+"/"+r.getName()));
                    }
                });
                for(FileListItem entry:ml) mUtil.addDebugMsg(2,"I","Media file from FileSystem="+(entry.getPath()+"/"+entry.getName())+", size="+entry.getLength()+", lastModified="+entry.getLastModified());

                scanMediaStoreFileList(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, lf.getPath(), ml);
                sendMsgToProgDlg(hndl, "Image file scan ended");
                scanMediaStoreFileList(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, lf.getPath(), ml);
                sendMsgToProgDlg(hndl, "Audio file scan ended");
                scanMediaStoreFileList(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, lf.getPath(), ml);
                sendMsgToProgDlg(hndl, "Video file scan ended");

                for(FileListItem item:ml) {
                    if (!item.isChecked()) {
                        MediaScannerConnection.scanFile(mGp.context, new String[]{item.getPath()+"/"+item.getName()}, null, null);
                        mUtil.addDebugMsg(2,"I","Scan for add initiated. fp="+item.getPath()+"/"+item.getName());
                        mScanAddCount++;
                    }
                }
                hndl.post(new Runnable(){
                    @Override
                    public void run() {
                        hideLocalProgressView();
                        showDialogMsg("I","Scan media file was ended","Directory="+lf.getPath()+"\n"+"Scan for Add="+mScanAddCount+"\n"+"Scan for Delete="+mScanDeleteCount+"\n"+
                                "Scan for Update="+mScanUpdateCount);
                    }
                });
                mUtil.addDebugMsg(1,"I","Scan ended");
            }
        };
        th.start();
    }

    private void scanMediaStoreFileList(Uri ms_uri, String scan_dir, ArrayList<FileListItem>ml) {
        String[] msQueryProj=new String[] {
                MediaStore.MediaColumns.DATA,MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.DATE_MODIFIED,MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.SIZE};
        //MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        Cursor ci = mGp.context.getContentResolver().query(ms_uri ,msQueryProj ,null,null);//"_data=?" ,new String[]{scan_dir} ,"_data");

        if (ci!=null ) {
            while( ci.moveToNext() ){
                boolean media_file_different=false, m_s_d=false, m_m_d=false, f_n_e=false;
                String file_path=ci.getString(ci.getColumnIndex( MediaStore.Images.Media.DATA));
                if (file_path.startsWith(scan_dir)) {
                    String display_name=ci.getString(ci.getColumnIndex( MediaStore.Images.Media.DISPLAY_NAME));
                    long date_added=ci.getLong(ci.getColumnIndex( MediaStore.Images.Media.DATE_ADDED));
                    long date_modified=ci.getLong(ci.getColumnIndex( MediaStore.Images.Media.DATE_MODIFIED));
                    long media_size=ci.getLong(ci.getColumnIndex( MediaStore.Images.Media.SIZE));
                    File mf=new File(file_path);
                    FileListItem key=new FileListItem(mf.getName(), true, 0, date_modified, false, true,true,
                            false,mf.getParent(), 0);
                    mUtil.addDebugMsg(2,"I","Media info frm MediaStore="+file_path+", size="+media_size+", lastModified="+date_modified);
                    int idx=Collections.binarySearch(ml, key, new Comparator<FileListItem>(){
                        @Override
                        public int compare(FileListItem l, FileListItem r) {
                            return ((l.getPath()+"/"+l.getName())).compareToIgnoreCase((r.getPath()+"/"+r.getName()));
                        }
                    });
                    if (idx>=0) {
                        FileListItem file_info=ml.get(idx);
                        if (!file_info.isChecked()) {
                            file_info.setChecked(true);
                            if ((file_info.getLastModified()/1000)!=date_modified || file_info.getLength()!=media_size) {//Update required
                                MediaScannerConnection.scanFile(mGp.context, new String[]{file_path}, null, null);
                                mUtil.addDebugMsg(2,"I","Scan for update initiated. fp="+file_path);
                                mScanUpdateCount++;
                            }
                        }
                    } else {//Delete media file
                        MediaScannerConnection.scanFile(mGp.context, new String[]{file_path}, null, null);
                        mUtil.addDebugMsg(2,"I","Scan for delete initiated. fp="+file_path);
                        mScanDeleteCount++;
                    }
                }
            }
            ci.close();
        }
    }

    public void startLocalFileViewerIntent(FileListItem item, String mime_type) {
        String mt = null, fid = null;
        mUtil.addDebugMsg(1,"I","Start Intent: name=" + item.getName());
        if (item.getName().lastIndexOf(".") > 0) {
            fid = item.getName().substring(item.getName().lastIndexOf(".") + 1, item.getName().length());
            fid=fid.toLowerCase();
        }
        if (mime_type==null) {
            mt= MimeTypeMap.getSingleton().getMimeTypeFromExtension(fid);
            if (mt==null && fid!=null && fid.equals("log")) mt="text/plain";
        } else {
            mt=mime_type;
        }
        if (mt != null) {
            if (mt.startsWith("text")) mt="text/plain";
            try {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (Build.VERSION.SDK_INT>=23) {
                    if (mGp.localBase.startsWith(mGp.safMgr.getUsbRootPath())) {
                        SafFile sf = mGp.safMgr.createUsbItem(item.getPath() + "/" + item.getName(), false);
                        intent.setDataAndType(sf.getUri(), mt);
                    } else if (mGp.localBase.startsWith(mGp.safMgr.getSdcardRootPath())) {
//                        SafFile sf = mGp.safMgr.createSdcardItem(item.getPath() + "/" + item.getName(), false);
//                        intent.setDataAndType(sf.getUri(), mt);
                        intent.setDataAndType(Uri.parse("file://"+item.getPath()+"/"+item.getName()), mt);
                    } else {
                        intent.setDataAndType(Uri.parse("file://"+item.getPath()+"/"+item.getName()), mt);
//                        Uri uri= FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", new File(item.getPath()+"/"+item.getName()));
//                        intent.setDataAndType(uri, mt);
                    }
                } else {
                    intent.setDataAndType(Uri.parse("file://"+item.getPath()+"/"+item.getName()), mt);
                }
                if (mt.startsWith("image")) intent.setClassName("com.sentaroh.android.TinyPictureViewer", "com.sentaroh.android.TinyPictureViewer.ActivityMain");
                mContext.startActivity(intent);
            } catch(ActivityNotFoundException e) {
                showDialogMsg("E", "File viewer can not be found.", "File name="+item.getName()+", MimeType="+mt);
            }
        } else {
            showDialogMsg("E", "MIME type can not be found.", "File name="+item.getName());
        }
    }

    private void startRemoteFileViewerIntent(FileListAdapter fla, final FileListItem item) {
        mUtil.addDebugMsg(1,"I","Start Intent: name=" + item.getName());
        String fid = null;
        if (item.getName().lastIndexOf(".") > 0) {
            fid = item.getName().substring(item.getName().lastIndexOf(".") + 1, item.getName().length()).toLowerCase();
        }
        String mt_work="";
        if (mt_work==null && fid!=null && (fid.equals("log") || mt_work.startsWith("text"))) {
            mt_work="text/plain";
        } else {
            if (fid==null) mt_work=null;
            else mt_work=MimeTypeMap.getSingleton().getMimeTypeFromExtension(fid);
        }

        if (mt_work != null) {
            final String mime_type=mt_work;
            NotifyEvent ntfy=new NotifyEvent(mContext);
            ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                @Override
                public void positiveResponse(Context c,Object[] o) {
                    try {
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                        if (Build.VERSION.SDK_INT>=24) {
//                            intent.setDataAndType(Uri.parse("file://"+mGp.internalRootDirectory+"/Download/"+item.getName()), mime_type);
////                            Uri uri= FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider",
////                                    new File(mGp.internalRootDirectory+"/SMBExplorer/download/"+item.getName()));
////                            intent.setDataAndType(uri, mt);
//                        } else {
//                            intent.setDataAndType(Uri.parse("file://"+mGp.internalRootDirectory+"/Download/"+item.getName()), mime_type);
//                        }
                        intent.setDataAndType(Uri.parse("file://"+mGp.internalRootDirectory+"/Download/"+item.getName()), mime_type);
                        mContext.startActivity(intent);
                    } catch(ActivityNotFoundException e) {
                        showDialogMsg("E", "File viewer can not be found.", "File name="+item.getName()+", MimeType="+mime_type);
                    }
                }
                @Override
                public void negativeResponse(Context c,Object[] o) {
                }
            });
            downloadRemoteFile(fla, item, mGp.remoteBase, ntfy );
        } else {
            showDialogMsg("E", "MIME type can not be found.", "File name="+item.getName());
        }
    }

    private void downloadRemoteFile(FileListAdapter fla, FileListItem item, String url, NotifyEvent p_ntfy) {
        mGp.fileioLinkParm.clear();
        FileIoLinkParm fio=new FileIoLinkParm();
        fio.setFromUrl(item.getPath());
        fio.setFromName(item.getName());
        fio.setToUrl(mGp.internalRootDirectory+"/Download");
        fio.setToName(item.getName());
        fio.setFromUser(mGp.currentSmbServerConfig.getUser());
        fio.setFromPass(mGp.currentSmbServerConfig.getPass());
        mGp.fileioLinkParm.add(fio);
        startFileioTask(fla,FILEIO_PARM_DOWLOAD_REMOTE_FILE,mGp.fileioLinkParm, item.getName(),p_ntfy, mGp.internalRootDirectory);
    }

    private ThreadCtrl mTcFileIoTask=null;

    private void startFileioTask(FileListAdapter fla, final int op_cd,final ArrayList<FileIoLinkParm> alp,String item_name,
                                 final NotifyEvent p_ntfy, final String lmp) {
        fla.setAllItemChecked(false);

        String dst="";
        String dt = null;
        String nitem=item_name;
        mGp.fileIoWifiLockRequired=false;
        switch (op_cd) {
            case FILEIO_PARM_REMOTE_CREATE:
                mGp.fileIoWifiLockRequired=true;
            case FILEIO_PARM_LOCAL_CREATE:
                dt="Create";
                dst=item_name+" was created.";
                nitem="";
                break;
            case FILEIO_PARM_REMOTE_RENAME:
                mGp.fileIoWifiLockRequired=true;
            case FILEIO_PARM_LOCAL_RENAME:
                dt="Rename";
                dst=item_name+" was renamed.";
                nitem="";
                break;
            case FILEIO_PARM_REMOTE_DELETE:
                mGp.fileIoWifiLockRequired=true;
            case FILEIO_PARM_LOCAL_DELETE:
                dt="Delete";
                dst="Following dirs/files were deleted.";
                break;
            case FILEIO_PARM_COPY_REMOTE_TO_LOCAL:
            case FILEIO_PARM_COPY_REMOTE_TO_REMOTE:
            case FILEIO_PARM_COPY_LOCAL_TO_REMOTE:
                mGp.fileIoWifiLockRequired=true;
            case FILEIO_PARM_COPY_LOCAL_TO_LOCAL:
                dt="Copy";
                dst="Following dirs/files were copied.";
                break;
            case FILEIO_PARM_MOVE_REMOTE_TO_REMOTE:
            case FILEIO_PARM_MOVE_LOCAL_TO_REMOTE:
            case FILEIO_PARM_MOVE_REMOTE_TO_LOCAL:
                mGp.fileIoWifiLockRequired=true;
            case FILEIO_PARM_MOVE_LOCAL_TO_LOCAL:
                dt="Move";
                dst="Following dirs/files were moved.";
                break;
            case FILEIO_PARM_DOWLOAD_REMOTE_FILE:
                mGp.fileIoWifiLockRequired=true;
                dt="Download";
                dst="";
            default:
                break;
        }

        mTcFileIoTask=new ThreadCtrl();
        mTcFileIoTask.setEnabled();

        if (mGp.currentTabName.equals(SMBEXPLORER_TAB_LOCAL)) {
            mGp.progressMsgView=mGp.localProgressMsg;
            mGp.progressCancelBtn=mGp.localProgressCancel;
            showLocalProgressView();
        } else if (mGp.currentTabName.equals(SMBEXPLORER_TAB_REMOTE)) {
            mGp.progressMsgView=mGp.remoteProgressMsg;
            mGp.progressCancelBtn=mGp.remoteProgressCancel;
            showRemoteProgressView();
        }
        mGp.progressMsgView.setText("Preparing "+dt);

        mGp.progressCancelBtn.setEnabled(true);
        mGp.progressCancelBtn.setText("Cancel");
        mGp.progressOnClickListener =new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mTcFileIoTask.setDisabled();
                mGp.progressCancelBtn.setEnabled(false);
                mGp.progressCancelBtn.setText("Cancelling");
            }
        };
        mGp.progressCancelBtn.setOnClickListener(mGp.progressOnClickListener);

        NotifyEvent ne=new NotifyEvent(mContext);
        ne.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context c,Object[] o) {
                hideRemoteProgressView();
                hideLocalProgressView();
                String end_msg="File I/O task was ended without error.";
                if (!mTcFileIoTask.isThreadResultSuccess()) {
                    if (p_ntfy!=null) p_ntfy.notifyToListener(false, null);
                    if (mTcFileIoTask.isThreadResultCancelled()) {
                        end_msg="File I/O task was cancelled.";
                        showDialogMsg("W",end_msg,"");
                        mUtil.addLogMsg("W",end_msg);
                    } else {
                        end_msg="File I/O task was failed."+"\n"+mTcFileIoTask.getThreadMessage();
                        showDialogMsg("E",end_msg,"");
                        mUtil.addLogMsg("E",end_msg);
                    }
//                    if (!mGp.activityIsBackground)
                    refreshFileListView();
                } else {
                    if (p_ntfy!=null) p_ntfy.notifyToListener(true, null);
                    else {
//					    if (!mGp.activityIsBackground)
                        refreshFileListView();
                    }
                }
                if (mGp.activityIsBackground) {
                    try {
                        mGp.svcClient.aidlUpdateNotificationMessage(end_msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                alp.clear();
            }

            @Override
            public void negativeResponse(Context c,Object[] o) {
                hideRemoteProgressView();
                hideLocalProgressView();
            }
        });

        FileIo th = new FileIo(mGp, op_cd, alp, mTcFileIoTask, ne, mContext, lmp);
        mTcFileIoTask.initThreadCtrl();
		th.setName("FileIo");
        th.start();
    }

    public void showDialogMsg(String cat, String st, String mt) {
        mActivity.setUiEnabled(false);
        createDialogCloseBtnListener();
        String msg="";
        if (mt.equals("")) msg=st;
        else msg=st+"\n"+mt;
        if (mGp.currentTabName.equals(SMBEXPLORER_TAB_LOCAL)) {
            showLocalDialogView();
            mGp.dialogMsgView=mGp.localDialogMsg;
            mGp.dialogCloseBtn=mGp.localDialogCloseBtn;
        } else if (mGp.currentTabName.equals(SMBEXPLORER_TAB_REMOTE)) {
            showRemoteDialogView();
            mGp.dialogMsgView=mGp.remoteDialogMsg;
            mGp.dialogCloseBtn=mGp.remoteDialogCloseBtn;
        }
        if (cat.equals("E")) mGp.dialogMsgView.setTextColor(Color.RED);
        else if (cat.equals("W")) mGp.dialogMsgView.setTextColor(Color.YELLOW);
        else mGp.dialogMsgView.setTextColor(Color.WHITE);
        mGp.dialogMsgView.setText(msg);
        mGp.dialogCloseBtn.setOnClickListener(mGp.dialogOnClickListener);
        mGp.dialogMsgCat =cat;
    }

    private void createDialogCloseBtnListener() {
        mGp.dialogOnClickListener =new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                hideLocalDialogView();
                hideRemoteDialogView();
                mActivity.setUiEnabled(true);
            }
        };
    }

//    private ArrayList<FileIoLinkParm> buildFileioLinkParmx(ArrayList<FileIoLinkParm> alp,
//                                                          String tgt_url1, String tgt_url2,
//                                                          String tgt_name, String tgt_new, String username, String password,
//                                                          boolean allcopy) {
//        FileIoLinkParm fiop=new FileIoLinkParm();
//        fiop.setUrl1(tgt_url1);
//        fiop.setUrl2(tgt_url2);
//        fiop.setName(tgt_name);
//        fiop.setNew(tgt_new);
//        fiop.setUser(username);
//        fiop.setPass(password);
//        fiop.setAllCopy(allcopy);
//
//        alp.add(fiop);
//
//        return alp;
//    };
//
    private void createItem(final FileListAdapter fla, final String item_optyp, final String base_dir) {
        mUtil.addDebugMsg(1,"I","createItem entered.");

        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.file_rename_create_dlg);
        final EditText newName = (EditText) dialog.findViewById(R.id.file_rename_create_dlg_newname);
        final Button btnOk = (Button) dialog.findViewById(R.id.file_rename_create_dlg_ok_btn);
        final Button btnCancel = (Button) dialog.findViewById(R.id.file_rename_create_dlg_cancel_btn);

        CommonDialog.setDlgBoxSizeCompact(dialog);

        ((TextView)dialog.findViewById(R.id.file_rename_create_dlg_title)).setText("Create directory");
        ((TextView)dialog.findViewById(R.id.file_rename_create_dlg_subtitle)).setText("Enter new name");

        // newName.setText(item_name);

        btnOk.setEnabled(false);
        // btnCancel.setEnabled(false);
        newName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() < 1) btnOk.setEnabled(false);
                else btnOk.setEnabled(true);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before,int count) {}
        });

        // OKボタンの指定
        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                if (!checkDuplicateDir(fla,newName.getText().toString())) {
                    mGp.commonDlg.showCommonDialog(false,"E","Create","Duplicate directory name specified",null);
                } else {
                    int cmd=0;
                    if (mGp.currentTabName.equals(SMBEXPLORER_TAB_LOCAL)) {
//                        mGp.fileioLinkParm=buildFileioLinkParm(mGp.fileioLinkParm, base_dir,"",newName.getText().toString(),"", mGp.smbUser,mGp.smbPass,true);
                        FileIoLinkParm fio=new FileIoLinkParm();
                        fio.setToUrl(base_dir);
                        fio.setToName(newName.getText().toString());
                        mGp.fileioLinkParm.add(fio);
                        cmd=FILEIO_PARM_LOCAL_CREATE;
                    } else {
                        cmd=FILEIO_PARM_REMOTE_CREATE;
//                        mGp.fileioLinkParm=buildFileioLinkParm(mGp.fileioLinkParm, base_dir,"",newName.getText().toString(),"", mGp.smbUser,mGp.smbPass,true);
                        FileIoLinkParm fio=new FileIoLinkParm();
                        fio.setToUrl(base_dir);
                        fio.setToName(newName.getText().toString());
                        fio.setToUser(mGp.currentSmbServerConfig.getUser());
                        fio.setToPass(mGp.currentSmbServerConfig.getPass());
                        mGp.fileioLinkParm.add(fio);
                    }
                    mUtil.addDebugMsg(1,"I","createItem FILEIO task invoked.");
                    startFileioTask(fla,cmd,mGp.fileioLinkParm, newName.getText().toString(),null,null);
                }
            }
        });
        // CANCELボタンの指定
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                mUtil.addDebugMsg(1,"W","createItem cancelled.");
            }
        });
        // Cancelリスナーの指定
        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                btnCancel.performClick();
            }
        });
        dialog.show();
    }

    private boolean checkDuplicateDir(FileListAdapter fla,String ndir) {
        for (int i = 0; i < fla.getCount(); i++) {
            if (ndir.equals(fla.getItem(i).getName()))
                return false; // duplicate dir
        }
        return true;
    }

    private void showProperty(FileListAdapter fla, final String item_optyp, final String item_name, final boolean item_isdir, final int item_num) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        FileListItem item = fla.getItem(item_num);

        String info;

        info = "Path="+item.getPath()+"\n";
        info = info+
                "Name="+item.getName()+"\n"+
                "Directory : "+item.isDir()+"\n"+
                "Hidden : "+item.isHidden()+"\n"+
                "canRead :"+item.canRead()+"\n"+
                "canWrite :"+item.canWrite()+"\n"+
                "Length : "+item.getLength()+"\n"+
                "Last modified : "+df.format(item.getLastModified())+"\n"+
                "Last modified(ms):"+item.getLastModified();
        mGp.commonDlg.showCommonDialog(false,"I","Property",info,null);

    }

    private void renameItem(final FileListAdapter fla) {
        mUtil.addDebugMsg(1,"I","renameItem entered.");
        ArrayList<FileListItem>fl=fla.getDataList();
        FileListItem t_from_item=null;
        for(FileListItem item:fl) {
            if (item.isChecked()) {
                t_from_item=item;
                break;
            }
        }
        if (t_from_item==null) return;
        FileListItem from_item=t_from_item;
        String item_name=from_item.getName();

        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.file_rename_create_dlg);
        final EditText newName =
                (EditText) dialog.findViewById(R.id.file_rename_create_dlg_newname);
        final Button btnOk =
                (Button) dialog.findViewById(R.id.file_rename_create_dlg_ok_btn);
        final Button btnCancel =
                (Button) dialog.findViewById(R.id.file_rename_create_dlg_cancel_btn);

        CommonDialog.setDlgBoxSizeCompact(dialog);

        ((TextView) dialog.findViewById(R.id.file_rename_create_dlg_title))
                .setText("Rename");
        ((TextView) dialog.findViewById(R.id.file_rename_create_dlg_subtitle))
                .setText("Enter new name");

        newName.setText(item_name);

        btnOk.setEnabled(false);
        newName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() < 1 || item_name.equals(s.toString())) btnOk.setEnabled(false);
                else btnOk.setEnabled(true);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // OKボタンの指定
        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
//				setFixedOrientation(false);
                if (item_name.equals(newName.getText().toString())) {
                    mGp.commonDlg.showCommonDialog(false,"E","Rename", "Duplicate file name specified",null);
                } else {
                    int cmd=0;
                    if (mGp.currentTabName.equals(SMBEXPLORER_TAB_LOCAL)) {
                        FileIoLinkParm fio=new FileIoLinkParm();
                        fio.setFromUrl(from_item.getPath());
                        fio.setFromName(from_item.getName());
                        fio.setToUrl(from_item.getPath());
                        fio.setToName(newName.getText().toString());
                        mGp.fileioLinkParm.add(fio);
                        cmd=FILEIO_PARM_LOCAL_RENAME;
                    } else {
                        cmd=FILEIO_PARM_REMOTE_RENAME;
                        FileIoLinkParm fio=new FileIoLinkParm();
                        fio.setFromUrl(from_item.getPath());
                        fio.setFromName(from_item.getName());
                        fio.setFromUser(mGp.currentSmbServerConfig.getUser());
                        fio.setFromPass(mGp.currentSmbServerConfig.getPass());
                        fio.setToUrl(from_item.getPath());
                        fio.setToName(newName.getText().toString());
                        fio.setToUser(mGp.currentSmbServerConfig.getUser());
                        fio.setToPass(mGp.currentSmbServerConfig.getPass());
                        mGp.fileioLinkParm.add(fio);
                    }
                    mUtil.addDebugMsg(1,"I","renameItem FILEIO task invoked.");
                    startFileioTask(fla,cmd,mGp.fileioLinkParm,item_name,null, null);
                }
            }
        });
        // CANCELボタンの指定
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                mUtil.addDebugMsg(1,"W","renameItem cancelled.");
            }
        });
        // Cancelリスナーの指定
        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                btnCancel.performClick();
            }
        });
        dialog.show();
    }

    private void deleteItem(final FileListAdapter fla) {
        mUtil.addDebugMsg(1,"I","deleteItem entered.");
        String di ="";
        for (int i=0;i<fla.getCount();i++) {
            FileListItem item = fla.getItem(i);
            if (item.isChecked()) di=di+item.getName()+"\n";
        }

        final String item_name=di;
        NotifyEvent ne=new NotifyEvent(mContext);
        ne.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context c,Object[] t) {
                for (int i=fla.getCount()-1;i>=0;i--) {
                    FileListItem item = fla.getItem(i);
                    if (item.isChecked()) {
                        if (mGp.currentTabName.equals(SMBEXPLORER_TAB_LOCAL)) {
//                            buildFileioLinkParm(mGp.fileioLinkParm,item.getPath(), "",item.getName(),"","","",true);
                            FileIoLinkParm fio=new FileIoLinkParm();
                            fio.setToUrl(item.getPath());
                            fio.setToName(item.getName());
                            mGp.fileioLinkParm.add(fio);
                        } else {
//                            buildFileioLinkParm(mGp.fileioLinkParm,item.getPath(),
//                                    "",item.getName(),"",mGp.smbUser,mGp.smbPass,true);
                            FileIoLinkParm fio=new FileIoLinkParm();
                            fio.setToUrl(item.getPath());
                            fio.setToName(item.getName());
                            fio.setToUser(mGp.currentSmbServerConfig.getUser());
                            fio.setToPass(mGp.currentSmbServerConfig.getPass());
                            mGp.fileioLinkParm.add(fio);
                        }
                    }
                }
                fla.setAllItemChecked(false);
                mUtil.addDebugMsg(1,"I","deleteItem invokw FILEIO task.");
                if (mGp.currentTabName.equals(SMBEXPLORER_TAB_LOCAL))
                    startFileioTask(fla,FILEIO_PARM_LOCAL_DELETE,mGp.fileioLinkParm,item_name,null,null);
                else startFileioTask(fla,FILEIO_PARM_REMOTE_DELETE,mGp.fileioLinkParm,item_name,null,null);
            }
            @Override
            public void negativeResponse(Context c,Object[] o) {
                fla.setAllItemChecked(false);
                mUtil.addDebugMsg(1,"W","deleteItem canceled");
            }
        });
        mGp.commonDlg.showCommonDialog(true,"W",mContext.getString(R.string.msgs_delete_file_dirs_confirm),item_name,ne);
    }

    private ArrayList<FileListItem> pasteFromList=new ArrayList<FileListItem>();
    private String pasteFromUrl="", pasteItemList="", pasteFromBase="";
    private String pasteFromDomain="", pasteFromUser="", pasteFromPass="", pasteFromSmbLevel="";
    private boolean isPasteCopy=false,isPasteEnabled=false, isPasteFromLocal=false;
    private void setCopyFrom(FileListAdapter fla) {
        pasteItemList="";
        if (mGp.currentTabName.equals(SMBEXPLORER_TAB_LOCAL)) {
            pasteFromUrl=mGp.localBase+"/"+mGp.localDir;
            isPasteFromLocal=true;
            pasteFromBase=mGp.localBase;
        } else {
            pasteFromUrl=mGp.remoteBase+"/"+mGp.remoteDir;;
            isPasteFromLocal=false;
            pasteFromBase=mGp.remoteBase;
            pasteFromUser=mGp.currentSmbServerConfig.getUser();
            pasteFromPass=mGp.currentSmbServerConfig.getPass();
            pasteFromSmbLevel=mGp.currentSmbServerConfig.getSmbLevel();
        }
        //Get selected item names
        isPasteCopy=true;
        isPasteEnabled=true;
        FileListItem fl_item;
        pasteFromList.clear();
        String sep="";
        for (int i = 0; i < fla.getCount(); i++) {
            fl_item = fla.getItem(i);
            if (fl_item.isChecked()) {
                pasteItemList=pasteItemList+sep+fl_item.getName();
                sep=",";
                pasteFromList.add(fl_item);
            }
        }
        fla.setAllItemChecked(false);
        setPasteItemList();
        setLocalContextButtonStatus();
        setRemoteContextButtonStatus();
        mUtil.addDebugMsg(1,"I","setCopyFrom fromUrl="+pasteFromUrl+ ", num_of_list="+pasteFromList.size());
    }

    private void setCutFrom(FileListAdapter fla) {
        pasteItemList="";
        if (mGp.currentTabName.equals(SMBEXPLORER_TAB_LOCAL)) {
            pasteFromUrl=mGp.localBase+"/"+mGp.localDir;
            isPasteFromLocal=true;
            pasteFromBase=mGp.localBase;
        } else {
            pasteFromUrl=mGp.remoteBase+"/"+mGp.remoteDir;
            isPasteFromLocal=false;
            pasteFromBase=mGp.remoteBase;
            pasteFromUser=mGp.currentSmbServerConfig.getUser();
            pasteFromPass=mGp.currentSmbServerConfig.getPass();
            pasteFromSmbLevel=mGp.currentSmbServerConfig.getSmbLevel();
        }
        //Get selected item names
        isPasteCopy=false;
        isPasteEnabled=true;
        FileListItem fl_item;
        pasteFromList.clear();
        String sep="";
        for (int i = 0; i < fla.getCount(); i++) {
            fl_item = fla.getItem(i);
            if (fl_item.isChecked()) {
                pasteItemList=pasteItemList+sep+fl_item.getName();
                sep=",";
                pasteFromList.add(fl_item);
            }
        }
        fla.setAllItemChecked(false);
        setPasteItemList();
        setLocalContextButtonStatus();
        setRemoteContextButtonStatus();
        mUtil.addDebugMsg(1,"I","setCutFrom fromUrl="+pasteFromUrl+ ", num_of_list="+pasteFromList.size());
    }

    public void setPasteItemList() {
        LinearLayout lc=(LinearLayout)mActivity.findViewById(R.id.explorer_filelist_copy_paste);
        TextView pp=(TextView)mActivity.findViewById(R.id.explorer_filelist_paste_list);
        ImageView cc=(ImageView)mActivity.findViewById(R.id.explorer_filelist_paste_copycut);
        if (isPasteEnabled) {
            if (isPasteCopy) cc.setImageDrawable(mContext.getDrawable(R.drawable.context_button_copy));
            else cc.setImageDrawable(mContext.getDrawable(R.drawable.context_button_cut));
            pp.setText(pasteItemList);
            lc.setVisibility(LinearLayout.VISIBLE);
        }
        setPasteButtonEnabled();
    }

    public void setPasteButtonEnabled() {
        boolean vp=false;
        if (isPasteEnabled) {
            if (mGp.currentTabName.equals(SMBEXPLORER_TAB_LOCAL)) {
                vp=isValidPasteDestination(mGp.localBase,mGp.localDir);
                if (vp) mGp.localContextBtnPasteView.setVisibility(LinearLayout.VISIBLE);
                else mGp.localContextBtnPasteView.setVisibility(LinearLayout.INVISIBLE);
            } else {
                vp=isValidPasteDestination(mGp.remoteBase,mGp.remoteDir);
                if (vp) mGp.remoteContextBtnPasteView.setVisibility(LinearLayout.VISIBLE);
                else mGp.remoteContextBtnPasteView.setVisibility(LinearLayout.INVISIBLE);
            }
        } else {
            mGp.localContextBtnPasteView.setVisibility(LinearLayout.INVISIBLE);
            mGp.remoteContextBtnPasteView.setVisibility(LinearLayout.INVISIBLE);
        }
    }

    public void clearPasteItemList() {
        LinearLayout lc=(LinearLayout)mActivity.findViewById(R.id.explorer_filelist_copy_paste);
        isPasteEnabled=false;
        pasteItemList="";
        TextView pp=(TextView)mActivity.findViewById(R.id.explorer_filelist_paste_list);
//		TextView cc=(TextView)findViewById(R.id.explorer_filelist_paste_copycut);
        pp.setText(pasteItemList);
        lc.setVisibility(LinearLayout.GONE);
        setLocalContextButtonStatus();
        setRemoteContextButtonStatus();
        setPasteButtonEnabled();
    }

    private boolean isValidPasteDestination(String base, String dir) {
        boolean result=false;
//		Log.v("","base="+base+", dir="+dir);
//		Thread.currentThread().dumpStack();
        if (isPasteEnabled) {
            String to_dir="";
            if (dir.equals("")) to_dir=base;
            else to_dir=base+"/"+dir;
            String from_dir=pasteFromList.get(0).getPath();
            String from_path=pasteFromList.get(0).getPath()+"/"+pasteFromList.get(0).getName();
            if (!to_dir.equals(from_dir)) {
                if (!from_path.equals(to_dir)) {
                    if (!to_dir.startsWith(from_path)) {
                        result=true;
                    }
                }
            }
        }
        return result;
    }

    private void pasteItem(final FileListAdapter fla, final String to_dir, final String lmp) {
        //Get selected item names
        FileListItem fl_item;
        if (mGp.currentTabName.equals(SMBEXPLORER_TAB_LOCAL)) {
            String fl_name="",fl_exists="";
            boolean fl_conf_req=false;
            for (int i = 0; i < pasteFromList.size(); i++) {
                fl_item = pasteFromList.get(i);
                fl_name=fl_name+fl_item.getName()+"\n";
                File lf=new File(to_dir+"/"+fl_item.getName());
                if (lf.exists()) {
                    fl_conf_req=true;
                    fl_exists=fl_exists+fl_item.getName()+"\n";
                }
            }
//			Log.v("","t="+to_dir);
            pasteCreateIoParm(fla,to_dir,fl_name,fl_exists,fl_conf_req, lmp);
        } else {
            final ArrayList<String> d_list=new ArrayList<String>();
            for (int i = 0; i < pasteFromList.size(); i++)
                d_list.add(to_dir+pasteFromList.get(i).getName());
            NotifyEvent ntfy=new NotifyEvent(mContext);
            // set commonDialog response
            ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                @Override
                public void positiveResponse(Context c,Object[] o) {
                    String fl_name="",fl_exists="";
                    boolean fl_conf_req=false;
                    for (int i=0;i<d_list.size();i++)
                        if (!d_list.get(i).equals(""))
                            fl_exists=fl_exists+d_list.get(i)+"\n";
                    if (!fl_exists.equals("")) fl_conf_req=true;
                    for (int i = 0; i < pasteFromList.size(); i++)
                        fl_name=fl_name+pasteFromList.get(i).getName()+"\n";
                    pasteCreateIoParm(fla,to_dir,fl_name,fl_exists,fl_conf_req, lmp);
                }
                @Override
                public void negativeResponse(Context c,Object[] o) {	}
            });
            checkRemoteFileExists(mGp.remoteBase, mGp.currentSmbServerConfig.getUser(), mGp.currentSmbServerConfig.getPass(), d_list, ntfy);
        }
    }

    private void pasteCreateIoParm(FileListAdapter fla, String to_dir, String fl_name, String fl_exists,boolean fl_conf_req, final String lmp) {
        FileListItem fi ;
        for (int i=0;i<pasteFromList.size();i++) {
            fi=pasteFromList.get(i);
            FileIoLinkParm fio=new FileIoLinkParm();
            fio.setFromUrl(fi.getPath());
            fio.setFromName(fi.getName());
            fio.setToUrl(to_dir);
            fio.setToName(fi.getName());

            if (fi.getPath().startsWith("smb://")) {
                fio.setFromBaseUrl(fi.getBaseUrl());
                fio.setFromUser(pasteFromUser);
                fio.setFromPass(pasteFromPass);
                fio.setFromSmbLevel(pasteFromSmbLevel);
            } else {
                fio.setFromBaseUrl(fi.getBaseUrl());
            }

            if (to_dir.startsWith("smb://")) {
                fio.setToBaseUrl(mGp.remoteBase);
                fio.setToUser(mGp.currentSmbServerConfig.getUser());
                fio.setToPass(mGp.currentSmbServerConfig.getPass());
                fio.setToSmbLevel(mGp.currentSmbServerConfig.getSmbLevel());
            } else {
                fio.setToBaseUrl(mGp.localBase);
            }

            mGp.fileioLinkParm.add(fio);
        }
        // copy process
        if (isPasteCopy) {
            if (isPasteFromLocal && mGp.currentTabName.equals(SMBEXPLORER_TAB_LOCAL)) {
                // Local to Local copy localCurrDir->curr_dir
                copyConfirm(fla,FILEIO_PARM_COPY_LOCAL_TO_LOCAL,mGp.fileioLinkParm,
                        fl_name,fl_conf_req, fl_exists, lmp);
            } else if (isPasteFromLocal && mGp.currentTabName.equals(SMBEXPLORER_TAB_REMOTE)) {
                // Local to Remote copy localCurrDir->remoteUrl
                copyConfirm(fla,FILEIO_PARM_COPY_LOCAL_TO_REMOTE,mGp.fileioLinkParm,
                        fl_name,fl_conf_req, fl_exists, lmp);

            } else if (!isPasteFromLocal && mGp.currentTabName.equals(SMBEXPLORER_TAB_REMOTE)) {
                // Remote to Remote copy localCurrDir->remoteUrl
                copyConfirm(fla,FILEIO_PARM_COPY_REMOTE_TO_REMOTE,mGp.fileioLinkParm,
                        fl_name,fl_conf_req, fl_exists, lmp);

            } else {
                // Remote to Local copy localCurrDir->remoteUrl
                copyConfirm(fla,FILEIO_PARM_COPY_REMOTE_TO_LOCAL,mGp.fileioLinkParm,
                        fl_name,fl_conf_req, fl_exists, lmp);
            }
        } else {
            // process move
            clearPasteItemList();
            if (isPasteFromLocal && mGp.currentTabName.equals(SMBEXPLORER_TAB_LOCAL)) {
                // Local to Local
                moveConfirm(fla,FILEIO_PARM_MOVE_LOCAL_TO_LOCAL,mGp.fileioLinkParm,
                        fl_name,fl_conf_req, fl_exists, lmp);
            } else if (isPasteFromLocal && mGp.currentTabName.equals(SMBEXPLORER_TAB_REMOTE)) {
                // Local to Remote
                moveConfirm(fla,FILEIO_PARM_MOVE_LOCAL_TO_REMOTE,mGp.fileioLinkParm,
                        fl_name,fl_conf_req, fl_exists, lmp);

            } else if (!isPasteFromLocal && mGp.currentTabName.equals(SMBEXPLORER_TAB_REMOTE)) {
                // Remote to Remote
                moveConfirm(fla,FILEIO_PARM_MOVE_REMOTE_TO_REMOTE,mGp.fileioLinkParm,
                        fl_name,fl_conf_req, fl_exists, lmp);

            } else {
                // Remote to Local
                moveConfirm(fla,FILEIO_PARM_MOVE_REMOTE_TO_LOCAL,mGp.fileioLinkParm,
                        fl_name,fl_conf_req, fl_exists, lmp);
            }
        }

    }

    private void copyConfirm(final FileListAdapter fla, final int cmd_cd, ArrayList<FileIoLinkParm> alp,
                             final String selected_name, boolean conf_req, String conf_msg, final String lmp) {

        if (conf_req) {
            NotifyEvent ne=new NotifyEvent(mContext);
            // set commonDialog response
            ne.setListener(new NotifyEvent.NotifyEventListener() {
                @Override
                public void positiveResponse(Context c,Object[] o) {
                    mUtil.addDebugMsg(1,"I","copyConfirm File I/O task invoked.");
                    startFileioTask(fla,cmd_cd,mGp.fileioLinkParm,selected_name,null, lmp);
                }
                @Override
                public void negativeResponse(Context c,Object[] o) {
                    mUtil.addLogMsg("W","Ccopy override confirmation cancelled.");
                }
            });
            mGp.commonDlg.showCommonDialog(true,"W","Copy following dirs/files are overrides?",conf_msg,ne);

        } else {
            NotifyEvent ne=new NotifyEvent(mContext);
            // set commonDialog response
            ne.setListener(new NotifyEvent.NotifyEventListener() {
                @Override
                public void positiveResponse(Context c,Object[] o) {
                    mUtil.addDebugMsg(1,"I","copyConfirm FILE I/O task invoked.");
                    startFileioTask(fla,cmd_cd,mGp.fileioLinkParm,selected_name,null, lmp);
                }
                @Override
                public void negativeResponse(Context c,Object[] o) {
                    mUtil.addLogMsg("W","Copy cancelled."+"\n"+selected_name);
                }
            });
            mGp.commonDlg.showCommonDialog(true,"I","Following dirs/files are copy?",selected_name,ne);
        }
        return;
    }

    private void moveConfirm(final FileListAdapter fla,
                             final int cmd_cd, ArrayList<FileIoLinkParm> alp,
                             final String selected_name, boolean conf_req, String conf_msg, final String lmp) {

        if (conf_req) {
            NotifyEvent ne=new NotifyEvent(mContext);
            // set commonDialog response
            ne.setListener(new NotifyEvent.NotifyEventListener() {
                @Override
                public void positiveResponse(Context c,Object[] o) {
                    mUtil.addDebugMsg(1,"I","moveConfirm File I/O task invoked.");
                    startFileioTask(fla,cmd_cd,mGp.fileioLinkParm,selected_name,null, lmp);
                }
                @Override
                public void negativeResponse(Context c,Object[] o) {
                    mUtil.addLogMsg("W","Move override confirmation cancelled.");
                }
            });
            mGp.commonDlg.showCommonDialog(true,"W","Move following dirs/files are overrides?", conf_msg,ne);

        } else {
            NotifyEvent ne=new NotifyEvent(mContext);
            // set commonDialog response
            ne.setListener(new NotifyEvent.NotifyEventListener() {
                @Override
                public void positiveResponse(Context c,Object[] o) {
                    mUtil.addDebugMsg(1,"I","moveConfirm FILE I/O task invoked.");
                    startFileioTask(fla,cmd_cd,mGp.fileioLinkParm,selected_name,null, lmp);
                }
                @Override
                public void negativeResponse(Context c,Object[] o) {
                    mUtil.addLogMsg("W","Move cancelled."+"\n"+selected_name);
                }
            });
            mGp.commonDlg.showCommonDialog(true,"I","Following dirs/files are move?",selected_name,ne);
        }
        return;
    }

    static private boolean hasContainedNomediaFile(File lf) {
        boolean result=false;
        File nomedia=new File(lf.getAbsolutePath()+"/.nomedia");
        result=nomedia.exists();
        return result;
    }

    final static public void getAllMediaFileInDirectory(ArrayList<File>fl, File lf, boolean process_sub_directories) {
        if (lf.exists()) {
            if ((!lf.isHidden() && !hasContainedNomediaFile(lf))) {
                if (lf.isDirectory()) {
                    File[] cfl=lf.listFiles();
                    if (cfl!=null && cfl.length>0) {
                        for(File cf:cfl) {//Process file
                            if (!cf.isDirectory()) {
                                if (isMediaFile(cf)) fl.add(cf);
                            }
                        }
                        for(File cf:cfl) {
                            if (cf.isDirectory()) {
                                if (!hasContainedNomediaFile(cf)) {
                                    if (!cf.getName().equals(".thumbnails")) {
                                        if (process_sub_directories)
                                            getAllMediaFileInDirectory(fl, cf, process_sub_directories);
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    fl.add(lf);
                }
            }
        }
    }

    static private boolean isMediaFile(File lf) {
        final String[] FILE_TYPE_AUDIO=
                new String[]{".aac",".aif", ".aifc", ".aiff", ".kar", ".m3u", ".m4a", ".mid", ".midi", ".mp2",
                        ".mp3", ".mpga", ".ra", ".ram", ".wav"};
        final String[] FILE_TYPE_IMAGE=
                new String[]{".bmp", ".cgm", ".djv", ".djvu", ".gif", ".ico", ".ief", ".jpe", ".jpeg", ".jpg", ".pbm",
                        ".pgm", ".png", ".pnm", ".ppm", ".ras", ".rgb", ".svg", ".tif", ".tiff", ".wbmp", ".xbm",
                        ".xpm", ".xwd"};
        final String[] FILE_TYPE_VIDEO=
                new String[]{".avi", ".m4u", ".mov", ".mp4", ".movie", ".mpe", ".mpeg", ".mpg", ".mxu", ".qt", ".wmv"};

        boolean result=false;
        for(String ft:FILE_TYPE_AUDIO) {
            if (lf.getName().toLowerCase().endsWith(ft)) {
                result=true;
                break;
            }
        }
        if (!result) {
            for(String ft:FILE_TYPE_IMAGE) {
                if (lf.getName().toLowerCase().endsWith(ft)) {
                    result=true;
                    break;
                }
            }
        }
        if (!result) {
            for(String ft:FILE_TYPE_VIDEO) {
                if (lf.getName().toLowerCase().endsWith(ft)) {
                    result=true;
                    break;
                }
            }
        }
        return result;
    }


    final static public long getAllFileSizeInDirectory(File sd, boolean process_sub_directories) {
        long dir_size=0;
        if (sd.exists()) {
            if (sd.isDirectory()) {
                File[] cfl=sd.listFiles();
                if (cfl!=null && cfl.length>0) {
                    for(File cf:cfl) {
                        if (cf.isDirectory()) {
                            if (process_sub_directories)
                                dir_size+=getAllFileSizeInDirectory(cf, process_sub_directories);
                        } else {
                            dir_size+=cf.length();
                        }
                    }
                }
            } else {
                dir_size+=sd.length();
            }
        }
        return dir_size;
    }

    final public long getAllFileSizeInDirectory(SafFile sd, boolean process_sub_directories) {
        long dir_size=0l;
        if (sd.exists()) {
            if (sd.isDirectory()) {
//                long b_time=System.currentTimeMillis();
                SafFile[] cfl=sd.listFiles();
//                mUtil.addDebugMsg(1,"I","list children name="+sd.getName()+", elapsed="+(System.currentTimeMillis()-b_time));
                if (cfl!=null && cfl.length>0) {
                    for(SafFile cf:cfl) {
                        if (cf.isDirectory()) {
                            if (process_sub_directories)
                                dir_size+=getAllFileSizeInDirectory(cf, process_sub_directories);
                        } else {
                            dir_size+=cf.length();
                        }
                    }
                }
            } else {
                dir_size+=sd.length();
            }
        }
        return dir_size;
    }

    private void createLocalFileList(boolean dir_only, String url, NotifyEvent p_ntfy) {

        mUtil.addDebugMsg(1,"I","create local file list local url=" + url);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        ArrayList<FileListItem> dir = new ArrayList<FileListItem>();
        ArrayList<FileListItem> fls = new ArrayList<FileListItem>();

        File sf = new File(url);

        File[] file_list = sf.listFiles();
        if (file_list!=null) {
            try {
                for (File ff : file_list) {
                    FileListItem tfi=null;
                    if (ff.canRead()) {
                        if (ff.isDirectory()) {
                            File tlf=new File(url+"/"+ff.getName());
                            String[] tfl=tlf.list();
                            int sdc=0;
                            if (tfl!=null) sdc=tfl.length;
                            int ll=0;
                            tfi=createNewFilelistItem(mGp.localBase, ff, sdc, ll);
                            dir.add(tfi);
                        } else {
                            tfi=createNewFilelistItem(mGp.localBase, ff, 0, 0);
                            fls.add(tfi);
                        }
                        if (mGp.settingDebugLevel>=3) {
                            mUtil.addDebugMsg(3,"I","File :" + tfi.getName()+", "+
                                    "length: " + tfi.getLength()+", "+
                                    "Lastmod: " + sdf.format(tfi.getLastModified())+", "+
                                    "Lastmod: " + tfi.getLastModified()+", "+
                                    "isdir: " + tfi.isDir()+", "+
                                    "parent: " + ff.getParent()+", "+
                                    "path: " + tfi.getPath()+", "+
                                    "canonicalPath: " + ff.getCanonicalPath());
                        }
                    } else {
                        tfi=createNewFilelistItem(mGp.localBase, ff, 0, 0);
                        if (tfi.isDir()) dir.add(tfi);
                        else fls.add(tfi);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                mUtil.addLogMsg("E",e.toString());
                showDialogMsg("E",mContext.getString(R.string.msgs_local_file_list_create_error), e.getMessage());
                dir=null;
            }
        }

        if (dir!=null) {
            FileListAdapter.sort(dir);
            if (!dir_only) {
                FileListAdapter.sort(fls);
                dir.addAll(fls);
            }
        }
        p_ntfy.notifyToListener(true, new Object[]{dir});
    }

    private void createUsbFileList(boolean dir_only, final String url, final NotifyEvent p_ntfy) {
        mUtil.addDebugMsg(1,"I","create USB file list local url=" + url);

        final ThreadCtrl tc = new ThreadCtrl();
        tc.setEnabled();
        tc.setThreadResultSuccess();

        final Dialog dialog= CommonDialog.showProgressSpinIndicator(mActivity);
        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                tc.setDisabled();//disableAsyncTask();
                mUtil.addDebugMsg(1, "W", "CreateUsbFileList cancelled.");
            }
        });
        dialog.show();

        final Handler hndl=new Handler();
        Thread th=new Thread(){
            @Override
            public void run(){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

                ArrayList<FileListItem> dir = new ArrayList<FileListItem>();
                ArrayList<FileListItem> fls = new ArrayList<FileListItem>();
                SafFile sf=null;
                if (url.equals(mGp.safMgr.getUsbRootPath())) sf=mGp.safMgr.getUsbRootSafFile();
                else sf=mGp.safMgr.findUsbItem(url);
                if (sf!=null) {
                    SafFile[] file_list = sf.listFiles();
                    if (file_list!=null) {
                        try {
                            for (SafFile ff : file_list) {
                                FileListItem tfi=null;
                                if (ff.isDirectory()) {
                                    String s_url="";
                                    if (url.equals("")) s_url=ff.getName();
                                    else s_url=url+"/"+ff.getName();
                                    SafFile tlf=mGp.safMgr.findUsbItem(s_url);
                                    String[] sfl=tlf.list();
                                    for(String nm:sfl) mUtil.addDebugMsg(1,"I", "list="+nm);
                                    SafFile[] tfl=tlf.listFiles();
                                    int sdc=0;
                                    if (tfl!=null) sdc=tfl.length;
                                    int ll=0;
                                    tfi=createNewFilelistItem(mGp.localBase, ff, sdc, ll, true, url);
                                    dir.add(tfi);
                                } else {
                                    tfi=createNewFilelistItem(mGp.localBase, ff, 0, 0, false, url);
                                    fls.add(tfi);
                                }
                                mUtil.addDebugMsg(3,"I","File :" + tfi.getName()+", "+
                                        "length: " + tfi.getLength()+", "+
                                        "Lastmod: " + sdf.format(tfi.getLastModified())+", "+
                                        "Lastmod: " + tfi.getLastModified()+", "+
                                        "isdir: " + tfi.isDir()+", "+
                                        "parent: " + //ff.getParent()+", "+
                                        "path: " + tfi.getPath()+", "+
                                        "canonicalPath: ");//+ ff.getCanonicalPath());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            mUtil.addDebugMsg(0,"E",e.toString());
                            showDialogMsg("E",mContext.getString(R.string.msgs_local_file_list_create_error), e.getMessage());
                            dir=null;
                        }
                    }

                    if (dir!=null) {
                        FileListAdapter.sort(dir);
                        if (!dir_only) {
                            FileListAdapter.sort(fls);
                            dir.addAll(fls);
                        }
                    }
                    final Object[] result=new Object[]{dir};
                    hndl.post(new Runnable(){
                        @Override
                        public void run() {
                            p_ntfy.notifyToListener(true, result);
                            dialog.dismiss();
                        }
                    });

                }
            }
        };
        th.start();
    }

    private void createRemoteFileList(String opcd, final String url, final NotifyEvent parent_event) {
        mUtil.addDebugMsg(1,"I","Create remote filelist remote url:"+url);
        final NotifyEvent n_event=new NotifyEvent(mContext);
        n_event.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context c,Object[] o) {
                String itemname = "";
                @SuppressWarnings("unchecked")
                ArrayList<FileListItem> sf_item=(ArrayList<FileListItem>)o[0];

                ArrayList<FileListItem> dir = new ArrayList<FileListItem>();
                List<FileListItem> fls = new ArrayList<FileListItem>();

                for (int i = 0; i < sf_item.size(); i++) {
                    itemname = sf_item.get(i).getName();
                    if (itemname.equals("IPC$")) {
                        // ignore IPC$
                    } else {
                        if (sf_item.get(i).canRead()) {
                            if (sf_item.get(i).isDir()) {
                                dir.add(createNewFilelistItem(mGp.remoteBase, sf_item.get(i)));
                            } else {
                                fls.add(createNewFilelistItem(mGp.remoteBase, sf_item.get(i)));
                            }
                        } else {
                            fls.add(createNewFilelistItem(mGp.remoteBase, sf_item.get(i)));
                        }
                    }
                }

                dir.addAll(fls);
                FileListAdapter.sort(dir);
                parent_event.notifyToListener(true, new Object[]{dir});
            }

            @Override
            public void negativeResponse(Context c,Object[] o) {
                parent_event.notifyToListener(false, o);
                showDialogMsg("E",
                        mContext.getString(R.string.msgs_remote_file_list_create_error),(String)o[0]);
            }
        });
        SmbServerUtil.createSmbServerFileList(mActivity, mGp, opcd,
                mGp.currentSmbServerConfig.getSmbLevel(), url+"/", mGp.currentSmbServerConfig.getUser(), mGp.currentSmbServerConfig.getPass(),n_event);

    }

    private static String buildFullPath(String base, String dir) {
        String t_dir="";
        if (dir.equals("")) t_dir=base;
        else t_dir=base+"/"+dir;
        return t_dir;
    }


    private static String formatRemoteSmbUrl(String url) {
        String result="";
        String smb_url=url.replace("smb://", "");
        result="smb://"+smb_url.replaceAll("///", "/").replaceAll("//", "/");
        return result;
    }

    private void checkRemoteFileExists(String url, String user, String pass, ArrayList<String> d_list, final NotifyEvent n_event) {
        final ArrayList<FileListItem> remoteFileList=new ArrayList<FileListItem>();

        final ThreadCtrl tc = new ThreadCtrl();
        remoteFileList.clear();
        tc.setEnabled();

        Dialog prog_spin=CommonDialog.showProgressSpinIndicator(mActivity);
        prog_spin.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                tc.setDisabled();//disableAsyncTask();
                mUtil.addDebugMsg(1,"W","Filelist is cancelled.");
            }
        });
        prog_spin.show();

        NotifyEvent ne=new NotifyEvent(mContext);
        ne.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context c,Object[] o) {
                prog_spin.dismiss();
                if (tc.isThreadResultSuccess()) {
                    n_event.notifyToListener(true, o);
                } else {
                    String err="";
                    if (tc.isThreadResultCancelled()) err="Filelist was cancelled";
                    else err=tc.getThreadMessage();
                    n_event.notifyToListener(false, new Object[]{err});
                }
            }
            @Override
            public void negativeResponse(Context c,Object[] o) {
                prog_spin.dismiss();
            }
        });

        Thread th = new RetrieveFileList(mGp, tc, mGp.currentSmbServerConfig.getSmbLevel(), url, d_list,user,pass,ne);
        th.start();
    }

    public void showRemoteProgressView() {
        mActivity.setUiEnabled(false);
        mGp.remoteProgressView.setVisibility(LinearLayout.VISIBLE);
        mGp.remoteProgressView.setBackgroundColor(mGp.dialogBackgroundColor);
        mGp.remoteProgressView.bringToFront();
    }

    private void hideRemoteProgressView() {
        mActivity.setUiEnabled(true);
        mGp.remoteProgressView.setVisibility(LinearLayout.GONE);
    }

    public void showRemoteDialogView() {
        mActivity.setUiEnabled(false);
        mGp.remoteDialogView.setVisibility(LinearLayout.VISIBLE);
        mGp.remoteDialogView.setBackgroundColor(mGp.dialogBackgroundColor);
        mGp.remoteDialogView.bringToFront();
    }

    private void hideRemoteDialogView() {
        mActivity.setUiEnabled(true);
        mGp.remoteDialogView.setVisibility(LinearLayout.GONE);
    }

    public void showLocalDialogView() {
        mActivity.setUiEnabled(false);
        mGp.localDialogView.setVisibility(LinearLayout.VISIBLE);
        mGp.localDialogView.setBackgroundColor(mGp.dialogBackgroundColor);
        mGp.localDialogView.bringToFront();
    }

    private void hideLocalDialogView() {
        mActivity.setUiEnabled(true);
        mGp.localDialogView.setVisibility(LinearLayout.GONE);
    }

    public void showLocalProgressView() {
        mActivity.setUiEnabled(false);
        mGp.localProgressView.setVisibility(LinearLayout.VISIBLE);
        mGp.localProgressView.setBackgroundColor(mGp.dialogBackgroundColor);
        mGp.localProgressView.bringToFront();
    }

    private void hideLocalProgressView() {
        mActivity.setUiEnabled(true);
        mGp.localProgressView.setVisibility(LinearLayout.GONE);
    }

    private ArrayList<GlobalParameters.LocalStorageConfig> createLocalProfileEntry() {
        ArrayList<GlobalParameters.LocalStorageConfig> lcl = new ArrayList<GlobalParameters.LocalStorageConfig>();

        String pml= LocalMountPoint.getExternalStorageDir();
        GlobalParameters.LocalStorageConfig lstg=new GlobalParameters.LocalStorageConfig();
        lstg.storage_name="Internal Storage";
        lstg.storage_path=pml;
        lcl.add(lstg);

        if(mGp.safMgr.isSdcardMounted()) {
            lstg=new GlobalParameters.LocalStorageConfig();
            lstg.storage_name="SDCARD";
            lstg.storage_path=mGp.safMgr.getSdcardRootPath();
            lcl.add(lstg);
        }

        if(mGp.safMgr.isUsbMounted()) {
            lstg=new GlobalParameters.LocalStorageConfig();
            lstg.storage_name="USB Flash";
            lstg.storage_path=mGp.safMgr.getUsbRootPath();
            lcl.add(lstg);
        }

        return lcl;
    }

    static public FileListItem createNewFilelistItem(String base_url, FileListItem tfli) {
        FileListItem fi=null;
        if (tfli.isDir()) {
            fi= new FileListItem(tfli.getName(),
                    true,
                    0,
                    tfli.getLastModified(),
                    false,
                    tfli.canRead(),tfli.canWrite(),
                    tfli.isHidden(),tfli.getPath(),
                    tfli.getListLevel());
            fi.setSubDirItemCount(tfli.getSubDirItemCount());
            fi.setHasExtendedAttr(tfli.hasExtendedAttr());
            fi.setBaseUrl(base_url);;
        } else {
            fi=new FileListItem(tfli.getName(),
                    false,
                    tfli.getLength(),
                    tfli.getLastModified(),
                    false,
                    tfli.canRead(),tfli.canWrite(),
                    tfli.isHidden(),tfli.getPath(),
                    tfli.getListLevel());
            fi.setHasExtendedAttr(tfli.hasExtendedAttr());
            fi.setBaseUrl(base_url);;
        }
        return fi;
    }

    public FileListItem createNewFilelistItem(String base_url, SafFile tfli, int sdc, int ll, boolean dir, String parent) {
        if (dir) {
            final FileListItem fi= new FileListItem(tfli.getName(),
                    true,
                    -1,
                    tfli.lastModified(),
                    false,
                    true, false,//tfli.canRead(),tfli.canWrite(),
                    false, parent, //tfli.isHidden(),tfli.getParent(),
                    ll);
            fi.setSubDirItemCount(sdc);
            fi.setBaseUrl(base_url);;
            Thread th=new Thread(){
                @Override
                public void run() {
//                    mUtil.addDebugMsg(1,"I","calcurate started name=" + tfli.getName());
                    long dir_size=getAllFileSizeInDirectory(tfli, true);
                    fi.setLength(dir_size);
//                    mUtil.addDebugMsg(1,"I","calcurate ended name=" + tfli.getName());
                    mUiHandler.post(new Runnable(){
                        @Override
                        public void run(){
//                            mUtil.addDebugMsg(1,"I","refresh adapter name=" + tfli.getName());
                            mGp.localFileListAdapter.notifyDataSetChanged();
                        }
                    }) ;
                }
            };
            th.start();
            return fi;
        } else {
            FileListItem fi=new FileListItem(tfli.getName(),
                    false,
                    tfli.length(),
                    tfli.lastModified(),
                    false,
                    true,false,//tfli.canRead(),tfli.canWrite(),
                    false, parent,//tfli.isHidden(),tfli.getParent(),
                    ll);
            fi.setBaseUrl(base_url);;
            return fi;
        }

    }

    public FileListItem createNewFilelistItem(String base_url, final File tfli, int sdc, int ll) {
        if (tfli.isDirectory()) {
            final FileListItem fi= new FileListItem(tfli.getName(),
                    true,
                    -1,
                    tfli.lastModified(),
                    false,
                    tfli.canRead(),tfli.canWrite(),
                    tfli.isHidden(),tfli.getParent(),
                    ll);
            fi.setSubDirItemCount(sdc);
            fi.setBaseUrl(base_url);
            final Handler hndl=new Handler();
            Thread th=new Thread(){
                @Override
                public void run() {
                    long dir_size=getAllFileSizeInDirectory(tfli, true);
                    fi.setLength(dir_size);
                    hndl.post(new Runnable(){
                       @Override
                       public void run(){
                           mGp.localFileListAdapter.notifyDataSetChanged();
                       }
                    }) ;
                }
            };
            th.start();
            return fi;
        } else {
            final FileListItem fi=new FileListItem(tfli.getName(),
                    false,
                    tfli.length(),
                    tfli.lastModified(),
                    false,
                    tfli.canRead(),tfli.canWrite(),
                    tfli.isHidden(),tfli.getParent(),
                    ll);
            fi.setBaseUrl(base_url);
            return fi;
        }
    }

    static public FileListItem createNewFilelistItem(String base_url, JcifsFile tfli, int sdc, int ll) throws JcifsException {
        FileListItem fi=null;
        boolean has_ea=tfli.getAttributes()<16384?false:true;
        try {
            String fp=tfli.getParent();
            if (fp.endsWith("/")) fp=fp.substring(0,fp.lastIndexOf("/"));
            if (tfli.isDirectory()) {
                fi= new FileListItem(tfli.getName(),
                        true,
                        0,
                        tfli.getLastModified(),
                        false,
                        tfli.canRead(),tfli.canWrite(),
                        tfli.isHidden(),fp,
                        ll);
                fi.setSubDirItemCount(sdc);
                fi.setHasExtendedAttr(has_ea);
                fi.setBaseUrl(base_url);
            } else {
                fi=new FileListItem(tfli.getName(),
                        false,
                        tfli.length(),
                        tfli.getLastModified(),
                        false,
                        tfli.canRead(),tfli.canWrite(),
                        tfli.isHidden(),fp,
                        ll);
                fi.setHasExtendedAttr(has_ea);
                fi.setBaseUrl(base_url);
            }
        } catch(JcifsException e) {

        }
        return fi;
    }


}

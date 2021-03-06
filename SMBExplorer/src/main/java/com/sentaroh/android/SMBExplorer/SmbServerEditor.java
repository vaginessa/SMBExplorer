package com.sentaroh.android.SMBExplorer;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.sentaroh.android.Utilities.Dialog.CommonDialog;
import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.Widget.CustomSpinnerAdapter;

import java.util.ArrayList;
import java.util.Collections;

public class SmbServerEditor {
    private GlobalParameters mGp;
    private MainActivity mActivity;
    private Context mContext;
    private SmbServerConfig mSmbServerConfigitem;
    private NotifyEvent mParentNotify;
    private Dialog mDialog;
    private String mOpCode;

    public SmbServerEditor(String op, MainActivity a, GlobalParameters gp, SmbServerConfig item, NotifyEvent ntfy) {
        mGp=gp;
        mActivity=a;
        mContext=gp.context;
        mSmbServerConfigitem =item;
        mParentNotify=ntfy;
        mOpCode=op;

        initDialog();
    }

    private void initDialog() {
        // カスタムダイアログの生成
        mDialog = new Dialog(mActivity);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mDialog.setContentView(R.layout.smb_server_item_edit_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) mDialog.findViewById(R.id.smb_server_item_edit_dlg_view);
        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        LinearLayout title_view = (LinearLayout) mDialog.findViewById(R.id.smb_server_item_edit_dlg_title_view);
        title_view.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
        TextView dlg_title = (TextView) mDialog.findViewById(R.id.smb_server_item_edit_dlg_title);
        dlg_title.setTextColor(mGp.themeColorList.text_color_dialog_title);

        CommonDialog.setDlgBoxSizeLimit(mDialog, true);

        final Button btn_ok = (Button) mDialog.findViewById(R.id.smb_server_item_edit_dlg_ok);
        final Button btn_cancel = (Button) mDialog.findViewById(R.id.smb_server_item_edit_dlg_cancel);
        final TextView tv_msg = (TextView) mDialog.findViewById(R.id.smb_server_item_edit_dlg_msg);

        final EditText et_smb_name = (EditText) mDialog.findViewById(R.id.smb_server_item_edit_dlg_name);
        final Button btn_scan = (Button) mDialog.findViewById(R.id.smb_server_item_edit_dlg_scan_server);
        if (mOpCode.equals("EDIT")) et_smb_name.setEnabled(false);

        final Spinner sp_smb_level = (Spinner) mDialog.findViewById(R.id.smb_server_item_edit_dlg_smb_protocol);
        setSpinnerSyncFolderSmbProto(sp_smb_level, mSmbServerConfigitem.getSmbLevel());
        sp_smb_level.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                checkValidation(mDialog);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        final EditText et_smb_addr = (EditText) mDialog.findViewById(R.id.smb_server_item_edit_dlg_addr);
        et_smb_addr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                checkValidation(mDialog);
            }
        });
        et_smb_addr.setText(mSmbServerConfigitem.getAddr());

        final EditText et_smb_port = (EditText) mDialog.findViewById(R.id.smb_server_item_edit_dlg_port);
        et_smb_port.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                checkValidation(mDialog);
            }
        });
        et_smb_port.setText(mSmbServerConfigitem.getPort());

        final EditText et_smb_user = (EditText) mDialog.findViewById(R.id.smb_server_item_edit_dlg_user);
        et_smb_user.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                checkValidation(mDialog);
            }
        });
        et_smb_user.setText(mSmbServerConfigitem.getUser());

        final EditText et_smb_pass = (EditText) mDialog.findViewById(R.id.smb_server_item_edit_dlg_pass);
        et_smb_pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                checkValidation(mDialog);
            }
        });
        et_smb_pass.setText(mSmbServerConfigitem.getPass());

        final Button btn_list_share = (Button) mDialog.findViewById(R.id.smb_server_item_edit_dlg_list_share);
        final EditText et_smb_share_name = (EditText) mDialog.findViewById(R.id.smb_server_item_edit_dlg_share_name);
        et_smb_share_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                checkValidation(mDialog);
            }
        });
        et_smb_share_name.setText(mSmbServerConfigitem.getShare());

        if (mOpCode.equals("ADD")) {
            et_smb_name.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void afterTextChanged(Editable editable) {
                    if(SmbServerUtil.getSmbServerConfigItem(editable.toString(), mGp.smbConfigList)==null) {
                        btn_ok.setEnabled(true);
                        tv_msg.setText("");
                        tv_msg.setVisibility(TextView.GONE);
                        checkValidation(mDialog);
                    } else {
                        btn_ok.setEnabled(false);
                        tv_msg.setText("SMB server already exists, please specify new name.");
                        tv_msg.setVisibility(TextView.VISIBLE);
                    }
                }
            });
        }
        et_smb_name.setText(mSmbServerConfigitem.getName());

        btn_list_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotifyEvent ntfy=new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        String new_share_name=(String)objects[0];
                        et_smb_share_name.setText(new_share_name);
                        checkValidation(mDialog);
                    }
                    @Override
                    public void negativeResponse(Context context, Object[] objects) {}
                });
                String port="";
                if (et_smb_port.getText().length()>0) port=":"+et_smb_port.getText().toString();
                String url="smb://"+et_smb_addr.getText().toString()+port+"/";
                String smb_level=""+(sp_smb_level.getSelectedItemPosition()+1);
                selectShareName(smb_level, url, et_smb_user.getText().toString(), et_smb_pass.getText().toString(), ntfy);
            }
        });

        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotifyEvent ntfy=new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        String r_type=(String)objects[0];
                        String r_data=(String)objects[1];
                        et_smb_addr.setText(r_data);
                        checkValidation(mDialog);
                    }
                    @Override
                    public void negativeResponse(Context context, Object[] objects) {}
                });
                String smb_level=""+(sp_smb_level.getSelectedItemPosition()+1);
                ScanSmbServer ss=new ScanSmbServer(mActivity, mGp, smb_level);
                ss.scanSmbServerDlg(ntfy, et_smb_port.getText().toString(), false);
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOpCode.equals("ADD")) {
                    mSmbServerConfigitem.setName(et_smb_name.getText().toString());
                    mSmbServerConfigitem.setAddr(et_smb_addr.getText().toString());
                    mSmbServerConfigitem.setPort(et_smb_port.getText().toString());
                    mSmbServerConfigitem.setUser(et_smb_user.getText().toString());
                    mSmbServerConfigitem.setPass(et_smb_pass.getText().toString());
                    mSmbServerConfigitem.setShare(et_smb_share_name.getText().toString());
                    mSmbServerConfigitem.setSmbLevel(""+(sp_smb_level.getSelectedItemPosition()+1));
                } else if (mOpCode.equals("COPY")) {
                    mSmbServerConfigitem.setName(et_smb_name.getText().toString());
                    mSmbServerConfigitem.setAddr(et_smb_addr.getText().toString());
                    mSmbServerConfigitem.setPort(et_smb_port.getText().toString());
                    mSmbServerConfigitem.setUser(et_smb_user.getText().toString());
                    mSmbServerConfigitem.setPass(et_smb_pass.getText().toString());
                    mSmbServerConfigitem.setShare(et_smb_share_name.getText().toString());
                    mSmbServerConfigitem.setSmbLevel(""+(sp_smb_level.getSelectedItemPosition()+1));
                } else if (mOpCode.equals("EDIT")) {
                    mSmbServerConfigitem.setAddr(et_smb_addr.getText().toString());
                    mSmbServerConfigitem.setPort(et_smb_port.getText().toString());
                    mSmbServerConfigitem.setUser(et_smb_user.getText().toString());
                    mSmbServerConfigitem.setPass(et_smb_pass.getText().toString());
                    mSmbServerConfigitem.setShare(et_smb_share_name.getText().toString());
                    mSmbServerConfigitem.setSmbLevel(""+(sp_smb_level.getSelectedItemPosition()+1));
                }
                mParentNotify.notifyToListener(true,new Object[]{mSmbServerConfigitem});
                mDialog.dismiss();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSmbServerConfigChanged(mDialog)) {
                    NotifyEvent ntfy=new NotifyEvent(mContext);
                    ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                        @Override
                        public void positiveResponse(Context context, Object[] objects) {
                            mDialog.dismiss();
                        }
                        @Override
                        public void negativeResponse(Context context, Object[] objects) {}
                    });
                    CommonDialog cd=new CommonDialog(mContext, mActivity.getSupportFragmentManager());
                    cd.showCommonDialog(true, "W", "Some parameters were changed, do you want to quit without save.", "", ntfy);
                } else {
                    mDialog.dismiss();
                }
            }
        });

        mDialog.show();
    }

    public static void setSpinnerBackground(Context c, Spinner spinner, boolean theme_is_light) {
        if (theme_is_light)
            spinner.setBackground(c.getDrawable(R.drawable.spinner_color_background_light));
        else spinner.setBackground(c.getDrawable(R.drawable.spinner_color_background));
    }

    private void setSpinnerSyncFolderSmbProto(Spinner spinner, String cv) {
        setSpinnerBackground(mContext, spinner, false);
        final CustomSpinnerAdapter adapter =
                new CustomSpinnerAdapter(mContext, android.R.layout.simple_spinner_item);
        mGp.safMgr.loadSafFile();
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setPrompt("Select SMB protocol");
        spinner.setAdapter(adapter);

//        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_smb_protocol_system));
        adapter.add("SMB1");
        adapter.add("SMB2");

        if (cv.equals("1")) spinner.setSelection(0);
        else if (cv.equals("2")) spinner.setSelection(1);
        else spinner.setSelection(0);
    }

    private boolean isSmbServerConfigChanged(Dialog dialog) {
        boolean result=true;
        final EditText et_smb_addr = (EditText) mDialog.findViewById(R.id.smb_server_item_edit_dlg_addr);
        final Button btn_ok = (Button) mDialog.findViewById(R.id.smb_server_item_edit_dlg_ok);
        final EditText et_smb_port = (EditText) mDialog.findViewById(R.id.smb_server_item_edit_dlg_port);
        final EditText et_smb_user = (EditText) mDialog.findViewById(R.id.smb_server_item_edit_dlg_user);
        final EditText et_smb_pass = (EditText) mDialog.findViewById(R.id.smb_server_item_edit_dlg_pass);
        final EditText et_smb_share_name = (EditText) mDialog.findViewById(R.id.smb_server_item_edit_dlg_share_name);
        final Spinner sp_smb_level = (Spinner) mDialog.findViewById(R.id.smb_server_item_edit_dlg_smb_protocol);

        if (et_smb_addr.getText().toString().equals(mSmbServerConfigitem.getAddr()) &&
                et_smb_port.getText().toString().equals(mSmbServerConfigitem.getPort()) &&
                et_smb_user.getText().toString().equals(mSmbServerConfigitem.getUser()) &&
                et_smb_pass.getText().toString().equals(mSmbServerConfigitem.getPass()) &&
                et_smb_share_name.getText().toString().equals(mSmbServerConfigitem.getShare()) &&
                mSmbServerConfigitem.getSmbLevel().equals(""+(sp_smb_level.getSelectedItemPosition()+1))
                ) result=false;
        return result;
    }

    private void checkValidation(Dialog dialog) {
        final TextView tv_msg = (TextView) mDialog.findViewById(R.id.smb_server_item_edit_dlg_msg);
        final EditText et_smb_addr = (EditText) mDialog.findViewById(R.id.smb_server_item_edit_dlg_addr);
        final Button btn_ok = (Button) mDialog.findViewById(R.id.smb_server_item_edit_dlg_ok);
        final EditText et_smb_port = (EditText) mDialog.findViewById(R.id.smb_server_item_edit_dlg_port);
        final EditText et_smb_user = (EditText) mDialog.findViewById(R.id.smb_server_item_edit_dlg_user);
        final EditText et_smb_pass = (EditText) mDialog.findViewById(R.id.smb_server_item_edit_dlg_pass);
        final EditText et_smb_share_name = (EditText) mDialog.findViewById(R.id.smb_server_item_edit_dlg_share_name);
        final Spinner sp_smb_level = (Spinner) mDialog.findViewById(R.id.smb_server_item_edit_dlg_smb_protocol);
        if (et_smb_addr.getText().length()>0) {
            if (et_smb_share_name.getText().length()>0) {
                tv_msg.setVisibility(TextView.GONE);
                tv_msg.setText("");
                btn_ok.setEnabled(isSmbServerConfigChanged(dialog));
            } else {
                tv_msg.setVisibility(TextView.VISIBLE);
                tv_msg.setText("Share name not specified");
                btn_ok.setEnabled(false);
            }
        } else {
            tv_msg.setVisibility(TextView.VISIBLE);
            tv_msg.setText("IP Address not specified");
            btn_ok.setEnabled(false);
        }
    }

    private void selectShareName(String smb_level, String url, String user, String pass, final NotifyEvent p_ntfy) {
        NotifyEvent ntfy=new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                ArrayList<FileListItem>sfl=(ArrayList<FileListItem>)objects[0];
                if (sfl!=null) {
                    Dialog dialog = new Dialog(mActivity);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setContentView(R.layout.item_select_list_dlg);

                    LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.item_select_list_dlg);
                    ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

                    LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.item_select_list_dlg_title_view);
                    title_view.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
                    TextView dlg_title = (TextView) dialog.findViewById(R.id.item_select_list_dlg_title);
                    dlg_title.setTextColor(mGp.themeColorList.text_color_dialog_title);
                    dlg_title.setText("Select SMB share name");
                    TextView dlg_subtitle = (TextView) dialog.findViewById(R.id.item_select_list_dlg_subtitle);
                    dlg_subtitle.setVisibility(TextView.GONE);
                    TextView dlg_msg = (TextView) dialog.findViewById(R.id.item_select_list_dlg_msg);
                    dlg_msg.setVisibility(TextView.GONE);

                    CommonDialog.setDlgBoxSizeLimit(dialog, true);

                    final Button btn_ok = (Button) dialog.findViewById(R.id.item_select_list_dlg_ok_btn);
                    btn_ok.setVisibility(Button.VISIBLE);
                    final Button btn_cancel = (Button) dialog.findViewById(R.id.item_select_list_dlg_cancel_btn);

                    ArrayList<String> rows=new ArrayList<String>();
                    for(FileListItem item:sfl) rows.add(item.getName());
                    Collections.sort(rows);

                    final ListView lv = (ListView) dialog.findViewById(android.R.id.list);
                    lv.setAdapter(new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_single_choice, rows));
                    lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    lv.setScrollingCacheEnabled(false);
                    lv.setScrollbarFadingEnabled(false);

                    btn_ok.setEnabled(false);
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> items, View view, int idx, long id) {
                            btn_ok.setEnabled(true);
                        }
                    });

                    btn_ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SparseBooleanArray checked = lv.getCheckedItemPositions();
                            for (int i = 0; i <= rows.size(); i++) {
                                if (checked.get(i) == true) {
                                    p_ntfy.notifyToListener(true, new Object[]{rows.get(i)});
                                    break;
                                }
                            }
                            dialog.dismiss();
                        }
                    });

                    btn_cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                } else {

                }
            }

            @Override
            public void negativeResponse(Context context, Object[] objects) {
                String error_msg=(String)objects[0];
                mGp.commonDlg.showCommonDialog(false,"W", "Error",error_msg,null);
            }
        });
        SmbServerUtil.createSmbServerFileList(mActivity, mGp, RetrieveFileList.OPCD_SHARE_LIST, smb_level, url, user, pass, ntfy);
    }


}

package com.scatl.uestcbbs.module.account.view;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.just.agentweb.AgentWebConfig;
import com.scatl.uestcbbs.R;
import com.scatl.uestcbbs.annotation.ResetPswType;
import com.scatl.uestcbbs.annotation.ToastType;
import com.scatl.uestcbbs.api.ApiConstant;
import com.scatl.uestcbbs.base.BaseActivity;
import com.scatl.uestcbbs.base.BaseEvent;
import com.scatl.uestcbbs.base.BasePresenter;
import com.scatl.uestcbbs.custom.MyLinearLayoutManger;
import com.scatl.uestcbbs.entity.AccountBean;
import com.scatl.uestcbbs.entity.LoginBean;
import com.scatl.uestcbbs.module.account.adapter.AccountManagerAdapter;
import com.scatl.uestcbbs.module.account.presenter.AccountManagerPresenter;
import com.scatl.uestcbbs.services.HeartMsgService;
import com.scatl.uestcbbs.util.CommonUtil;
import com.scatl.uestcbbs.util.Constant;
import com.scatl.uestcbbs.util.ServiceUtil;
import com.scatl.uestcbbs.util.SharePrefUtil;
import com.scatl.uestcbbs.util.TimeUtil;

import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;
import org.w3c.dom.Text;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class AccountManagerActivity extends BaseActivity implements AccountManagerView{

    private CoordinatorLayout coordinatorLayout;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private AccountManagerAdapter accountManagerAdapter;
    private TextView hint;

    private AccountManagerPresenter accountManagerPresenter;

    @Override
    protected int setLayoutResourceId() {
        return R.layout.activity_account_manager;
    }

    @Override
    protected void findView() {
        coordinatorLayout = findViewById(R.id.account_manager_coor_lyout);
        toolbar = findViewById(R.id.account_manager_toolbar);
        recyclerView = findViewById(R.id.account_manager_rv);
        hint = findViewById(R.id.account_manager_hint);
    }

    @Override
    protected void initView() {

        accountManagerPresenter = (AccountManagerPresenter) presenter;

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        accountManagerAdapter = new AccountManagerAdapter(R.layout.item_account_manager);
        recyclerView.setLayoutManager(new MyLinearLayoutManger(this));
        recyclerView.setAdapter(accountManagerAdapter);

        initAccountData();
    }

    @Override
    protected BasePresenter initPresenter() {
        return new AccountManagerPresenter();
    }

    /**
     * author: sca_tl
     * description:
     */
    private void initAccountData() {

        AccountBean a = new AccountBean();
        a.isLogin = SharePrefUtil.isLogin(this);
        a.userName = SharePrefUtil.getName(this);
        a.uid = SharePrefUtil.getUid(this);
        a.token = SharePrefUtil.getToken(this);
        a.secret = SharePrefUtil.getSecret(this);
        a.avatar = SharePrefUtil.getAvatar(this);

        List<AccountBean> list = LitePal
                .where("uid = ?", String.valueOf(a.uid))
                .find(AccountBean.class);
        if (list.size() == 0 && a.isLogin) a.save(); //???????????????????????????????????????????????????????????????????????????????????????

        List<AccountBean> data = LitePal.findAll(AccountBean.class);
        accountManagerAdapter.setCurrentLoginUid(a.uid);
        accountManagerAdapter.setNewData(data);

        hint.setText(data.size() == 0 ? "???????????????????????????" : "");
    }

    @Override
    protected void setOnItemClickListener() {
        accountManagerAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.item_account_manager_layout) {
                AccountBean accountBean = new AccountBean();
                accountBean.isLogin = true;
                accountBean.avatar = accountManagerAdapter.getData().get(position).avatar;
                accountBean.secret = accountManagerAdapter.getData().get(position).secret;
                accountBean.token = accountManagerAdapter.getData().get(position).token;
                accountBean.uid = accountManagerAdapter.getData().get(position).uid;
                accountBean.userName = accountManagerAdapter.getData().get(position).userName;
                SharePrefUtil.setLogin(this, true, accountBean);

                EventBus.getDefault().post(new BaseEvent<>(BaseEvent.EventCode.LOGIN_SUCCESS));

                accountManagerAdapter.setCurrentLoginUid(accountBean.uid);
                accountManagerAdapter.notifyItemRangeChanged(0 , accountManagerAdapter.getData().size());
                accountBean.saveOrUpdate("uid = ?", String.valueOf(accountBean.uid));

                HeartMsgService.private_me_msg_count = 0;
                HeartMsgService.at_me_msg_count = 0;
                HeartMsgService.reply_me_msg_count = 0;

                //????????????????????????
                if (! ServiceUtil.isServiceRunning(this, HeartMsgService.serviceName)) {
                    Intent intent1 = new Intent(this, HeartMsgService.class);
                    startService(intent1);
                }

                showToast("???????????????" + accountBean.userName, ToastType.TYPE_SUCCESS);


                if (!SharePrefUtil.isSuperLogin(this, accountBean.userName)) {
                    final AlertDialog dialog = new AlertDialog.Builder(this)
                            .setNegativeButton("??????", null)
                            .setPositiveButton("????????????", null )
                            .setTitle("????????????")
                            .setMessage("?????????????????????????????????????????????????????????????????????????????????")
                            .create();
                    dialog.setOnShowListener(d -> {
                        Button p = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        p.setOnClickListener(v -> {
                            Bundle bundle = new Bundle();
                            bundle.putString(Constant.IntentKey.LOGIN_TYPE, LoginFragment.LOGIN_FOR_SUPER_ACCOUNT);
                            bundle.putString(Constant.IntentKey.USER_NAME, accountBean.userName);
                            LoginFragment.getInstance(bundle).show(getSupportFragmentManager(), TimeUtil.getStringMs());
                            dialog.dismiss();
                        });
                    });
                    dialog.show();
                } else {  //????????????????????????webview??????cookies
                    for (String s : SharePrefUtil.getCookies(this, accountBean.userName)) {
                        AgentWebConfig.syncCookie(ApiConstant.BBS_BASE_URL, s);
                    }
                }
            }
        });

        accountManagerAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.item_account_manager_delete_btn) {
                deleteAccountDialog(position);
            }
            if (view.getId() == R.id.item_account_manager_super_login_btn) {
                //??????????????????????????????????????????????????????????????????????????????
                //????????????????????????
                if (!SharePrefUtil.isSuperLogin(this, accountManagerAdapter.getData().get(position).userName)){
                    Bundle bundle = new Bundle();
                    bundle.putString(Constant.IntentKey.LOGIN_TYPE, LoginFragment.LOGIN_FOR_SUPER_ACCOUNT);
                    bundle.putString(Constant.IntentKey.USER_NAME, accountManagerAdapter.getData().get(position).userName);
                    LoginFragment.getInstance(bundle).show(getSupportFragmentManager(), TimeUtil.getStringMs());
                } else {
                    superLoginDialog(accountManagerAdapter.getData().get(position).userName);
                }
            }
            if (view.getId() == R.id.item_account_manager_realname) {
                showToast("?????????????????????...", ToastType.TYPE_NORMAL);
                accountManagerPresenter.getRealNameInfo();
            }
        });
    }

    @Override
    protected void onClickListener(View view) {
    }

    private void superLoginDialog(String userName) {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setNegativeButton("????????????", null)
                .setPositiveButton("????????????", null )
                .setTitle("????????????")
                .setMessage("??????????????????????????????\n1????????????????????????????????????????????????\n2???????????????????????????")
                .create();
        dialog.setOnShowListener(d -> {
            Button p = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            p.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString(Constant.IntentKey.LOGIN_TYPE, LoginFragment.LOGIN_FOR_SUPER_ACCOUNT);
                bundle.putString(Constant.IntentKey.USER_NAME, userName);
                LoginFragment.getInstance(bundle).show(getSupportFragmentManager(), TimeUtil.getStringMs());
                dialog.dismiss();
            });

            Button n = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            n.setOnClickListener(v -> {
                SharePrefUtil.setCookies(AccountManagerActivity.this, new HashSet<>(), userName);
                SharePrefUtil.setSuperAccount(AccountManagerActivity.this, false, userName);
                SharePrefUtil.setUploadHash(this, "", userName);
                dialog.dismiss();
                showToast("??????????????????", ToastType.TYPE_SUCCESS);
                accountManagerAdapter.notifyDataSetChanged();
                EventBus.getDefault().post(new BaseEvent<>(BaseEvent.EventCode.LOGIN_SUCCESS));
            });

        });
        dialog.show();

    }

    @Override
    public void onGetRealNameInfoSuccess(String info) {
        showToast(info, ToastType.TYPE_SUCCESS);
    }

    @Override
    public void onGetRealNameInfoError(String msg) {
        showToast(msg, ToastType.TYPE_ERROR);
    }

    @Override
    public void onGetUploadHashSuccess(String hash, String msg) {
        SharePrefUtil.setUploadHash(this, hash, SharePrefUtil.getName(this));
        showToast(msg, ToastType.TYPE_SUCCESS);
    }

    @Override
    public void onGetUploadHashError(String msg) {
        showToast(msg, ToastType.TYPE_ERROR);
    }

    private void deleteAccountDialog(int position) {
        AccountBean accountBean = accountManagerAdapter.getData().get(position);

        String msg1 = "????????????????????????" + accountBean.userName + " ??????????????????????????????????????????????????????\n??????????????????????????????????????????????????????????????????";
        String msg2 = "????????????????????????" + accountBean.userName + " ??????????????????????????????????????????????????????";
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setNegativeButton("??????", null)
                .setPositiveButton("??????", null )
                .setTitle("????????????")
                .setMessage(accountBean.uid == SharePrefUtil.getUid(this) ? msg1 : msg2)
                .create();
        dialog.setOnShowListener(dialogInterface -> {
            Button p = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            p.setOnClickListener(v -> {
                dialog.dismiss();
                int i = LitePal.delete(AccountBean.class, accountBean.id);
                if (i != 0) {
                    HeartMsgService.private_me_msg_count = 0;
                    HeartMsgService.at_me_msg_count = 0;
                    HeartMsgService.reply_me_msg_count = 0;
                    SharePrefUtil.setCookies(this, new HashSet<>(), accountBean.userName);
                    SharePrefUtil.setSuperAccount(this, false, accountBean.userName);
                    SharePrefUtil.setUploadHash(this, "", accountBean.userName);
                    accountManagerAdapter.getData().remove(position);
                    accountManagerAdapter.notifyItemRemoved(position);
                    EventBus.getDefault().post(new BaseEvent<>(BaseEvent.EventCode.LOGOUT_SUCCESS));
                    showToast("????????????", ToastType.TYPE_SUCCESS);
                    if (accountBean.uid == SharePrefUtil.getUid(this)) SharePrefUtil.setLogin(this, false, new AccountBean());
                    hint.setText(accountManagerAdapter.getData().size() == 0 ? "???????????????????????????" : "");
                } else {
                    showToast("?????????????????????????????????", ToastType.TYPE_ERROR);
                }

            });
        });
        dialog.show();
    }

    @Override
    protected int setMenuResourceId() {
        return R.menu.menu_account_manager;
    }

    @Override
    protected void onOptionsSelected(MenuItem item) {
        super.onOptionsSelected(item);
        if (item.getItemId() == R.id.menu_account_manager_add_account) {
            Bundle bundle = new Bundle();
            bundle.putString(Constant.IntentKey.LOGIN_TYPE, LoginFragment.LOGIN_FOR_SIMPLE_ACCOUNT);
            LoginFragment.getInstance(bundle).show(getSupportFragmentManager(), TimeUtil.getStringMs());
        }

        if (item.getItemId() == R.id.menu_account_manager_reset_psw) {
            Bundle bundle = new Bundle();
            bundle.putString(Constant.IntentKey.TYPE, ResetPswType.TYPE_RESET);
            ResetPasswordFragment.getInstance(bundle).show(getSupportFragmentManager(), TimeUtil.getStringMs());
        }

        if (item.getItemId() == R.id.menu_account_manager_find_username) {
            Bundle bundle = new Bundle();
            bundle.putString(Constant.IntentKey.TYPE, ResetPswType.TYPE_FIND);
            ResetPasswordFragment.getInstance(bundle).show(getSupportFragmentManager(), TimeUtil.getStringMs());
        }

        if (item.getItemId() == R.id.menu_account_manager_register_account) {
            CommonUtil.openBrowser(this, Constant.REGISTER_URL);
        }

        if (item.getItemId() == R.id.menu_account_manager_get_upload_hash) {
            if (!SharePrefUtil.isLogin(this)) {
                showToast("????????????", ToastType.TYPE_WARNING);
            } else {
                accountManagerPresenter.showUploadHashDialog(this);
            }
        }

    }

    @Override
    protected boolean registerEventBus() {
        return true;
    }

    @Override
    public void onEventBusReceived(BaseEvent baseEvent) {
        if (baseEvent.eventCode == BaseEvent.EventCode.ADD_ACCOUNT_SUCCESS) {
            LoginBean loginBean = (LoginBean) baseEvent.eventData;

            boolean sameAccount = false;
            List<AccountBean> data = LitePal.findAll(AccountBean.class);
            for (AccountBean a : data){
                if (a.uid == loginBean.uid) {
                    sameAccount = true;
                    break;
                }
            }
            if (!sameAccount) {
                AccountBean accountBean = new AccountBean();
                accountBean.isLogin = false;
                accountBean.avatar = loginBean.avatar;
                accountBean.secret = loginBean.secret;
                accountBean.token = loginBean.token;
                accountBean.uid = loginBean.uid;
                accountBean.userName = loginBean.userName;

                accountBean.save();
                accountManagerAdapter.addData(accountBean);
                accountManagerAdapter.notifyItemInserted(accountManagerAdapter.getData().size());
                showToast("????????????????????????????????????", ToastType.TYPE_SUCCESS);
                hint.setText("");
            } else {
                showToast("????????????????????????????????????", ToastType.TYPE_NORMAL);
            }
        }

        if (baseEvent.eventCode == BaseEvent.EventCode.SUPER_LOGIN_SUCCESS) {
            showToast("??????????????????", ToastType.TYPE_SUCCESS);
            accountManagerAdapter.notifyDataSetChanged();
            EventBus.getDefault().post(new BaseEvent<>(BaseEvent.EventCode.LOGIN_SUCCESS));
        }
    }

}


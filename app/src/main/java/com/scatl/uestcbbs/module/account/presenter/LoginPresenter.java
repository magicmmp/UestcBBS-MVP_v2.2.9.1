package com.scatl.uestcbbs.module.account.presenter;

import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.alibaba.fastjson.JSONObject;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.AgentWebConfig;
import com.scatl.uestcbbs.R;
import com.scatl.uestcbbs.api.ApiConstant;
import com.scatl.uestcbbs.base.BasePresenter;
import com.scatl.uestcbbs.entity.LoginBean;
import com.scatl.uestcbbs.helper.ExceptionHelper;
import com.scatl.uestcbbs.helper.rxhelper.Observer;
import com.scatl.uestcbbs.module.account.model.AccountModel;
import com.scatl.uestcbbs.module.account.view.LoginView;
import com.scatl.uestcbbs.util.SharePrefUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Response;

/**
 * author: sca_tl
 * description:
 * date: 2020/1/24 13:04
 */
public class LoginPresenter extends BasePresenter<LoginView> {

    private AccountModel accountModel = new AccountModel();

    public void simpleLogin(String userName, String userPsw) {
        accountModel.simpleLogin(userName, userPsw, new Observer<LoginBean>() {
            @Override
            public void OnSuccess(LoginBean loginBean) {
                if (loginBean.rs == ApiConstant.Code.SUCCESS_CODE) {
                    view.onSimpleLoginSuccess(loginBean);
                }

                if (loginBean.rs == ApiConstant.Code.ERROR_CODE) {
                    view.onSimpleLoginError(loginBean.head.errInfo);
                }
            }

            @Override
            public void onError(ExceptionHelper.ResponseThrowable e) {
                view.onSimpleLoginError(e.message);
            }

            @Override
            public void OnCompleted() {

            }

            @Override
            public void OnDisposable(Disposable d) {
                disposable.add(d);
            }
        });
    }

    public void getCookies(Context context, String userName, String userPsw, int answerId, String answer) {
        accountModel.loginForCookies(userName, userPsw, answerId, answer, new Observer<Response<ResponseBody>>() {
            @Override
            public void OnSuccess(Response<ResponseBody> responseBodyResponse) {

                try {
                    ResponseBody responseBody = responseBodyResponse.body();
                    BufferedSource source;
                    if (responseBody != null) {
                        source = responseBody.source();
                        source.request(Long.MAX_VALUE);
                        Buffer buffer = source.getBuffer();
                        String responseBodyString = buffer.clone().readString(StandardCharsets.UTF_8);

                        //????????????
                        if (responseBodyString.contains("??????") && responseBodyString.contains(userName)) {
                            HashSet<String> cookies = new HashSet<>(responseBodyResponse.headers().values("Set-Cookie"));
                            SharePrefUtil.setCookies(context, cookies, userName);
                            SharePrefUtil.setSuperAccount(context, true, userName);
                            view.onGetCookiesSuccess("??????????????????");
                        } else if (responseBodyString.contains("?????????")){
                            view.onGetCookiesError("??????cookies??????????????????????????????????????????????????????");
                        } else {
                            view.onGetCookiesError("??????cookies????????????????????????????????????????????????????????????????????????????????????????????????");
                        }

                    } else {
                        view.onGetCookiesError("??????cookies?????????????????????");
                    }

                } catch (Exception e) {
                    view.onGetCookiesError("??????cookies?????????" + e.getMessage());
                }
            }

            @Override
            public void onError(ExceptionHelper.ResponseThrowable e) {
                view.onGetCookiesError("??????cookies??????:" + e.message);
            }

            @Override
            public void OnCompleted() { }

            @Override
            public void OnDisposable(Disposable d) {
                disposable.add(d);
            }
        });
    }

    public void getUploadHash(int tid) {
        accountModel.getUploadHash(tid, new Observer<String>() {
            @Override
            public void OnSuccess(String s) {
                try {

                    Document document = Jsoup.parse(s);

                    String ss = document.select("div[class=upfl hasfsl]").select("script").last().html().replaceAll("\\r|\\t|\\n|\\a","");

                    Matcher matcher = Pattern.compile("var upload = new SWFUpload(.*?)post_params: (.*?),file_size_limit ").matcher(ss);

                    if (matcher.find()) {

                        String hash = JSONObject.parseObject(matcher.group(2)).getString("hash");

                        if (hash != null && hash.length() == 32) {
                            view.onGetUploadHashSuccess(hash, "?????????????????????");
                        } else {
                            view.onGetUploadHashError("??????cookies?????????????????????hash?????????????????????????????????????????????????????????????????????????????????");
                        }
                    } else {
                        view.onGetUploadHashError("??????cookies?????????????????????hash?????????????????????????????????????????????");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    view.onGetUploadHashError("??????cookies?????????????????????hash????????????????????????????????????????????????" + e.getMessage());
                }
            }

            @Override
            public void onError(ExceptionHelper.ResponseThrowable e) {
                e.printStackTrace();
                view.onGetUploadHashError("??????cookies?????????????????????hash????????????????????????????????????????????????" + e.message);
            }

            @Override
            public void OnCompleted() {

            }

            @Override
            public void OnDisposable(Disposable d) {
                disposable.add(d);
            }
        });
    }

    public void showLoginReasonDialog(Context context, int selected) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("????????????")
                .setSingleChoiceItems(R.array.login_question, selected, (dialog1, which) -> {
                    view.onLoginReasonSelected(which);
                    dialog1.dismiss();
                })
                .create();
        dialog.show();
    }

}

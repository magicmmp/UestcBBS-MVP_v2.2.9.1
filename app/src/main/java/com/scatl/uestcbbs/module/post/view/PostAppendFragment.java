package com.scatl.uestcbbs.module.post.view;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatEditText;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.scatl.uestcbbs.R;
import com.scatl.uestcbbs.annotation.PostAppendType;
import com.scatl.uestcbbs.annotation.ToastType;
import com.scatl.uestcbbs.base.BaseDialogFragment;
import com.scatl.uestcbbs.base.BaseEvent;
import com.scatl.uestcbbs.base.BasePresenter;
import com.scatl.uestcbbs.module.post.presenter.PostAppendPresenter;
import com.scatl.uestcbbs.util.CommonUtil;
import com.scatl.uestcbbs.util.Constant;

import org.greenrobot.eventbus.EventBus;

import java.nio.charset.StandardCharsets;


public class PostAppendFragment extends BaseDialogFragment implements TextWatcher, PostAppendView{

    private AppCompatEditText content;
    private TextView contentLength, hint, title, dsp;
    private Button submit;
    private View layout;
    private LottieAnimationView loading;

    private int tid, pid;
    private String formHash, type;

    private PostAppendPresenter postAppendPresenter;

    public static PostAppendFragment getInstance(Bundle bundle) {
        PostAppendFragment postAppendFragment = new PostAppendFragment();
        postAppendFragment.setArguments(bundle);
        return postAppendFragment;
    }

    @Override
    protected void getBundle(Bundle bundle) {
        super.getBundle(bundle);
        if (bundle != null) {
            tid = bundle.getInt(Constant.IntentKey.TOPIC_ID, Integer.MAX_VALUE);
            pid = bundle.getInt(Constant.IntentKey.POST_ID, Integer.MAX_VALUE);
            type = bundle.getString(Constant.IntentKey.TYPE);
        }
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.fragment_post_append;
    }

    @Override
    protected void findView() {
        content = view.findViewById(R.id.post_append_fragment_content);
        contentLength = view.findViewById(R.id.post_append_fragment_content_length);
        submit = view.findViewById(R.id.post_append_fragment_submit);
        layout = view.findViewById(R.id.post_append_fragment_layout);
        hint = view.findViewById(R.id.post_append_fragment_hint);
        loading = view.findViewById(R.id.post_append_fragment_loading);
        title = view.findViewById(R.id.post_append_fragment_title);
        dsp = view.findViewById(R.id.post_append_fragment_content_dsp);
    }

    @Override
    protected void initView() {
        postAppendPresenter = (PostAppendPresenter) presenter;

        submit.setOnClickListener(this);
        content.addTextChangedListener(this);

        layout.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        hint.setText("");

        if (type.equals(PostAppendType.APPEND)){
            title.setText("??????");
            content.setHint("?????????????????????");
            dsp.setText(getString(R.string.append_desp));
            postAppendPresenter.getAppendFormHash(tid, pid);
        } else if (type.equals(PostAppendType.DIANPING)) {
            title.setText("??????");
            content.setHint("?????????????????????");
            dsp.setText(getString(R.string.dian_ping_desp));
            postAppendPresenter.getDianPingFormHash(tid, pid);
        }
    }

    @Override
    protected BasePresenter initPresenter() {
        return new PostAppendPresenter();
    }

    @Override
    protected void onClickListener(View view) {
        if (view.getId() == R.id.post_append_fragment_submit) {
            if (content.getText().toString().getBytes(StandardCharsets.UTF_8).length > 200) {
                showToast("???????????????", ToastType.TYPE_ERROR);
            } else if (content.getText().toString().isEmpty()) {
                showToast("???????????????", ToastType.TYPE_ERROR);
            } else {
                submit.setText("?????????...");
                submit.setEnabled(false);
                if (type.equals(PostAppendType.APPEND)){
                    postAppendPresenter.postAppendSubmit(tid, pid, formHash, content.getText().toString());
                } else if (type.equals(PostAppendType.DIANPING)) {
                    postAppendPresenter.sendDianPing(tid, pid, formHash, content.getText().toString());
                }
            }
        }
    }

    @Override
    public void onGetFormHashSuccess(String formHash) {
        this.formHash = formHash;
        layout.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        hint.setText("");
        CommonUtil.showSoftKeyboard(mActivity, content, 0);
    }

    @Override
    public void onGetFormHashError(String msg) {
        layout.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        hint.setText(msg);
    }

    @Override
    public void onPostAppendSuccess(String msg) {
        CommonUtil.hideSoftKeyboard(mActivity, content);
        showToast(msg, ToastType.TYPE_SUCCESS);
        dismiss();
    }

    @Override
    public void onPostAppendError(String msg) {
        showToast(msg, ToastType.TYPE_ERROR);
        submit.setText("????????????");
        submit.setEnabled(true);
    }

    @Override
    public void onSubmitDianPingSuccess(String msg) {
        CommonUtil.hideSoftKeyboard(mActivity, content);
        EventBus.getDefault().post(new BaseEvent<>(BaseEvent.EventCode.DIANPING_SUCCESS));
        showToast(msg, ToastType.TYPE_SUCCESS);
        dismiss();
    }

    @Override
    public void onSubmitDianPingError(String msg) {
        showToast(msg, ToastType.TYPE_ERROR);
        submit.setText("????????????");
        submit.setEnabled(true);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().getBytes(StandardCharsets.UTF_8).length <= 200) {
            contentLength.setText(new StringBuilder().append("????????????").append(200 - s.toString().getBytes(StandardCharsets.UTF_8).length).append("?????????"));
            contentLength.setTextColor(mActivity.getColor(R.color.text_color));
        } else {
            contentLength.setText(new StringBuilder().append("?????????").append(s.toString().getBytes(StandardCharsets.UTF_8).length - 200).append("?????????"));
            contentLength.setTextColor(Color.RED);
        }
    }
}

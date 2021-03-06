package com.scatl.uestcbbs.module.post.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.hendraanggrian.reveallayout.Radius;
import com.hendraanggrian.reveallayout.RevealableLayout;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.scatl.uestcbbs.R;
import com.scatl.uestcbbs.annotation.ToastType;
import com.scatl.uestcbbs.base.BaseActivity;
import com.scatl.uestcbbs.base.BaseEvent;
import com.scatl.uestcbbs.base.BasePresenter;
import com.scatl.uestcbbs.custom.MyLinearLayoutManger;
import com.scatl.uestcbbs.custom.emoticon.EmoticonPanelLayout;
import com.scatl.uestcbbs.custom.posteditor.ContentEditor;
import com.scatl.uestcbbs.entity.AttachmentBean;
import com.scatl.uestcbbs.entity.PostDraftBean;
import com.scatl.uestcbbs.entity.SendPostBean;
import com.scatl.uestcbbs.entity.UploadResultBean;
import com.scatl.uestcbbs.entity.UserPostBean;
import com.scatl.uestcbbs.helper.glidehelper.GlideEngineForPictureSelector;
import com.scatl.uestcbbs.module.post.adapter.AttachmentAdapter;
import com.scatl.uestcbbs.module.post.adapter.CreatePostPollAdapter;
import com.scatl.uestcbbs.module.post.presenter.CreatePostPresenter;
import com.scatl.uestcbbs.module.post.view.postdetail2.PostDetail2Activity;
import com.scatl.uestcbbs.module.user.view.AtUserListActivity;
import com.scatl.uestcbbs.module.user.view.AtUserListFragment;
import com.scatl.uestcbbs.module.user.view.UserDetailActivity;
import com.scatl.uestcbbs.util.CommonUtil;
import com.scatl.uestcbbs.util.Constant;
import com.scatl.uestcbbs.util.FileUtil;
import com.scatl.uestcbbs.util.SharePrefUtil;
import com.scatl.uestcbbs.util.TimeUtil;
import com.scatl.uestcbbs.util.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import am.widget.smoothinputlayout.SmoothInputLayout;

public class CreatePostActivity extends BaseActivity implements CreatePostView{

    private Toolbar toolbar;
    private CoordinatorLayout coordinatorLayout;
    private ImageView addEmotionBtn, atBtn, addPhotoBtn, sendBtn, addPollBtn, addAttachmentBtn, moreOptionsBtn;
    private EmoticonPanelLayout emoticonPanelLayout;
    private AppCompatEditText postTitle;
    private TextView boardName, autoSaveText;
    private ContentEditor contentEditor;
    private ProgressDialog progressDialog;
    private View sendBtn1;
    private RevealableLayout revealableLayout;

    private RecyclerView pollRv, attachmentRv;
    private CreatePostPollAdapter createPostPollAdapter;
    private AttachmentAdapter attachmentAdapter;
    private LinearLayout pollLayout;
    private TextView pollDesp;

    private SmoothInputLayout lytContent;

    private CreatePostPresenter createPostPresenter;

    private Rect rect;

    private static final int ACTION_ADD_PHOTO = 14;
    private static final int AT_USER_REQUEST = 110;
    private static final int ACTION_ADD_ATTACHMENT = 119;
    private static final int ADD_ATTACHMENT_REQUEST = 120;

    private int currentBoardId, currentFilterId;
    private String currentBoardName, currentFilterName;
    private long createTime;
    private String currentTitle, currentContent;

    private List<String> currentPollOptions;
    private int currentPollExp, currentPollChoice;
    private boolean currentPollVisible, currentPollShowVoters, currentAnonymous, currentOnlyAuthor, currentOriginalPic;

    private boolean sendPostSuccess;

    private Map<String, Integer> attachments = new LinkedHashMap<>(); //??????aid

    @Override
    protected void getIntent(Intent intent) {
        super.getIntent(intent);
        rect = intent.getParcelableExtra(Constant.IntentKey.DATA_1);
        createTime = TimeUtil.getLongMs();
        PostDraftBean postDraftBean = (PostDraftBean) intent.getSerializableExtra(Constant.IntentKey.DATA_2);
        if (postDraftBean != null) {
            currentBoardId = postDraftBean.board_id;
            currentFilterId = postDraftBean.filter_id;
            currentBoardName = postDraftBean.board_name;
            currentFilterName = postDraftBean.filter_name;
            currentTitle = postDraftBean.title;
            currentContent = postDraftBean.content;
            createTime = postDraftBean.time;
            currentPollOptions = CommonUtil.toList(postDraftBean.poll_options);
            currentPollExp = postDraftBean.poll_exp;
            currentPollChoice = postDraftBean.poll_choices;
            currentPollVisible = postDraftBean.poll_visible;
            currentPollShowVoters = postDraftBean.poll_show_voters;
            currentAnonymous = postDraftBean.anonymous;
            currentOnlyAuthor = postDraftBean.only_user;
        }
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.activity_create_post;
    }

    @Override
    protected void findView() {
        coordinatorLayout = findViewById(R.id.create_post_coor_layout);
        toolbar = findViewById(R.id.create_post_toolbar);
        addEmotionBtn = findViewById(R.id.create_post_add_emotion_btn);
        atBtn = findViewById(R.id.create_post_at_btn);
        addPhotoBtn = findViewById(R.id.create_post_add_image_btn);
        sendBtn = findViewById(R.id.create_post_send_btn);
        emoticonPanelLayout = findViewById(R.id.create_post_emoticon_layout);
        postTitle = findViewById(R.id.create_post_title);
        contentEditor = findViewById(R.id.create_post_content_editor);
        boardName = findViewById(R.id.create_post_board_name);
        autoSaveText = findViewById(R.id.create_post_auto_save_text);
        addPollBtn = findViewById(R.id.create_post_add_poll_btn);
        pollRv = findViewById(R.id.create_post_poll_rv);
        pollLayout = findViewById(R.id.create_post_poll_info);
        pollDesp = findViewById(R.id.create_post_poll_desp);
        addAttachmentBtn = findViewById(R.id.create_post_add_attachment_btn);
        moreOptionsBtn = findViewById(R.id.create_post_more_options_btn);
        attachmentRv = findViewById(R.id.create_post_attachment_rv);
        lytContent = findViewById(R.id.sil_lyt_content);
        revealableLayout = findViewById(R.id.create_post_reveal_layout);
        sendBtn1 = findViewById(R.id.create_post_send_btn_1);
    }

    @Override
    protected void initView() {

        createPostPresenter = (CreatePostPresenter) presenter;
        coordinatorLayout.post(() -> {
            Animator animator = revealableLayout.reveal(coordinatorLayout, rect == null ? 0 : rect.centerX(), rect == null ? 0 : rect.centerY(), Radius.GONE_ACTIVITY);
            animator.setDuration(500);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    coordinatorLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) { }
            });
            animator.start();
        });

        CommonUtil.showSoftKeyboard(this, postTitle, 100);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("????????????");
        progressDialog.setCancelable(false);

        addEmotionBtn.setOnClickListener(this);
        atBtn.setOnClickListener(this::onClickListener);
        addPhotoBtn.setOnClickListener(this::onClickListener);
        sendBtn.setOnClickListener(this::onClickListener);
        boardName.setOnClickListener(this::onClickListener);
        addPollBtn.setOnClickListener(this::onClickListener);
        addAttachmentBtn.setOnClickListener(this::onClickListener);
        moreOptionsBtn.setOnClickListener(this);
        sendBtn1.setOnClickListener(this);

        //??????
        createPostPollAdapter = new CreatePostPollAdapter(R.layout.item_create_post_poll);
        pollRv.setLayoutManager(new MyLinearLayoutManger(this));
        pollRv.setAdapter(createPostPollAdapter);

        //??????
        attachmentAdapter = new AttachmentAdapter(R.layout.item_attachment);
        LinearLayoutManager linearLayoutManager1 = new MyLinearLayoutManger(this);
        linearLayoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        attachmentRv.setLayoutManager(linearLayoutManager1);
        attachmentRv.setAdapter(attachmentAdapter);

        postTitle.setText(TextUtils.isEmpty(currentTitle) ? "" : currentTitle);
        boardName.setText(TextUtils.isEmpty(currentBoardName) && TextUtils.isEmpty(currentFilterName) ? "???????????????" :
                currentBoardName + "->" + currentFilterName);

        //????????????????????????????????????????????????????????????
        if (! TextUtils.isEmpty(currentContent)) {
            contentEditor.setEditorData(currentContent);
        }
        if (currentPollOptions != null && currentPollOptions.size() > 0) {
            pollLayout.setVisibility(View.VISIBLE);
            createPostPollAdapter.setNewData(currentPollOptions);
            String a = "??????" + currentPollChoice + "??????";
            String b = "?????????" + currentPollExp + "??????";
            String c = "??????" + (currentPollVisible ? "??????????????????" : "???????????????");
            String d = (currentPollShowVoters ? "??????" : "?????????") + "???????????????";

            pollDesp.setText(new StringBuilder().append(a).append(b).append(c).append(d));
        } else {
            currentPollOptions = new ArrayList<>();
        }

        countDownTimer.start();
    }

    @Override
    protected BasePresenter initPresenter() {
        return new CreatePostPresenter();
    }

    @Override
    protected void onClickListener(View view) {
        if (view.getId() == R.id.create_post_add_emotion_btn) {
            if (emoticonPanelLayout.getVisibility() == View.GONE) {
                lytContent.closeKeyboard(true);// ????????????
                lytContent.showInputPane(true);//????????????
            } else {
                lytContent.closeInputPane();// ????????????
                lytContent.showKeyboard();// ????????????
            }
        }
        if (view.getId() == R.id.create_post_at_btn) {
            Intent intent = new Intent(this, AtUserListActivity.class);
            startActivityForResult(intent, AT_USER_REQUEST);
        }
        if (view.getId() == R.id.create_post_add_image_btn) {
            createPostPresenter.requestPermission(this, ACTION_ADD_PHOTO, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (view.getId() == R.id.create_post_add_attachment_btn) {
            createPostPresenter.requestPermission(this, ACTION_ADD_ATTACHMENT, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (view.getId() == R.id.create_post_add_poll_btn) {
            if (createPostPollAdapter.getData().size() == 0) {
                startActivity(new Intent(this, AddPollActivity.class));
            } else {
                Intent intent = new Intent(this, AddPollActivity.class);
                intent.putStringArrayListExtra(Constant.IntentKey.POLL_OPTIONS, (ArrayList<String>) createPostPollAdapter.getData());
                intent.putExtra(Constant.IntentKey.POLL_EXPIRATION, currentPollExp);
                intent.putExtra(Constant.IntentKey.POLL_CHOICES, currentPollChoice);
                intent.putExtra(Constant.IntentKey.POLL_VISIBLE, currentPollVisible);
                intent.putExtra(Constant.IntentKey.POLL_SHOW_VOTERS, currentPollShowVoters);
                startActivity(intent);
            }
        }

        if (view.getId() == R.id.create_post_board_name) {
            SelectBoardFragment.getInstance(null)
                    .show(getSupportFragmentManager(), TimeUtil.getStringMs());
        }
        if (view.getId() == R.id.create_post_send_btn || view.getId() == R.id.create_post_send_btn_1) {
            if (currentBoardId == 0){
                showToast("???????????????", ToastType.TYPE_WARNING);
            } else if (currentAnonymous && currentBoardId != 371) {
                showToast("?????????????????????????????????????????????????????????->????????????->?????????", ToastType.TYPE_WARNING);
            } else {
                if (contentEditor.getImgPathList().size() == 0){//????????????
                    progressDialog.setMessage("??????????????????????????????...");
                    progressDialog.show();

                    createPostPresenter.sendPost(contentEditor,
                            currentBoardId, currentFilterId, postTitle.getText().toString(),
                            new ArrayList<>(), new ArrayList<>(),
                            currentPollOptions, attachments, currentPollChoice, currentPollExp,
                            currentPollVisible, currentPollShowVoters, currentAnonymous, currentOnlyAuthor,
                            this);
                } else {//?????????
                    if (!currentOriginalPic) {
                        progressDialog.setMessage("??????????????????????????????...");
                        progressDialog.show();

                        createPostPresenter.compressImage(this, contentEditor.getImgPathList());
                    } else {

                        progressDialog.setMessage("??????????????????????????????...");
                        progressDialog.show();

                        List<File> originalPicFiles = new ArrayList<>();
                        List<String> imgs = contentEditor.getImgPathList();
                        for (int i = 0; i < imgs.size(); i ++) {
                            File file = new File(imgs.get(i));
                            originalPicFiles.add(file);
                        }
                        createPostPresenter.upload(originalPicFiles, "forum", "image", this);
                    }

                }
            }
        }

        if (view.getId() == R.id.create_post_more_options_btn) {
            createPostPresenter.showCreatePostMoreOptionsDialog(this, currentAnonymous, currentOnlyAuthor, currentOriginalPic);
        }
    }

    @Override
    protected void setOnItemClickListener() {
        attachmentAdapter.setOnItemChildClickListener((adapter, view1, position) -> {
            if (view1.getId() == R.id.item_attachment_delete_file) {
                attachments.remove(attachmentAdapter.getData().get(position).localPath);
                attachmentAdapter.delete(position);
            }
        });
    }

    @Override
    public void onSendPostSuccessBack() {
        CommonUtil.hideSoftKeyboard(this, contentEditor);
        startRevealAnimation();
    }

    @Override
    public void onSendPostSuccessViewPost() {
        progressDialog.show();
        progressDialog.setMessage("?????????...");
        createPostPresenter.userPost(SharePrefUtil.getUid(this), this);
    }

    @Override
    public void onSendPostSuccess(SendPostBean sendPostBean) {
        sendPostSuccess = true;
        progressDialog.dismiss();
        createPostPresenter.showCreatePostSuccessDialog(this);
    }

    @Override
    public void onSendPostError(String msg) {
        progressDialog.dismiss();
        showToast("?????????????????????" + msg, ToastType.TYPE_ERROR);
    }

    @Override
    public void onUploadSuccess(UploadResultBean uploadResultBean) {
        progressDialog.setMessage("??????????????????????????????...");

        List<Integer> imgIds = new ArrayList<>();
        List<String> imgUrls = new ArrayList<>();

        for (int i = 0; i < uploadResultBean.body.attachment.size(); i ++) {
            imgIds.add(uploadResultBean.body.attachment.get(i).id);
            imgUrls.add(uploadResultBean.body.attachment.get(i).urlName);
        }

        if (imgUrls.size() != contentEditor.getImgPathList().size()) {
            onUploadError("??????????????????????????????????????????????????????????????????????????????????????????");
        } else {
            createPostPresenter.sendPost(contentEditor,
                    currentBoardId, currentFilterId,
                    postTitle.getText().toString(),
                    imgUrls, imgIds,
                    currentPollOptions, attachments, currentPollChoice, currentPollExp,
                    currentPollVisible, currentPollShowVoters, currentAnonymous, currentOnlyAuthor,
                    this);
        }
    }

    @Override
    public void onUploadError(String msg) {
        progressDialog.dismiss();
        showToast("?????????????????????" + msg, ToastType.TYPE_ERROR);
    }

    @Override
    public void onCompressImageSuccess(List<File> compressedFiles) {
        progressDialog.setMessage("???????????????????????????????????????????????????...");

        createPostPresenter.upload(compressedFiles, "forum", "image", this);
    }

    @Override
    public void onCompressImageFail(String msg) {
        progressDialog.dismiss();
        showToast("?????????????????????" + msg, ToastType.TYPE_ERROR);
    }

    @Override
    public void onPermissionGranted(int action) {
        if (action == ACTION_ADD_PHOTO) {
            PictureSelector.create(this)
                    .openGallery(PictureMimeType.ofImage())
                    .isCamera(true)
                    .isGif(false)
                    .showCropFrame(false)
                    .hideBottomControls(false)
                    .maxSelectNum(20)
                    .isEnableCrop(false)
                    .imageEngine(GlideEngineForPictureSelector.createGlideEngine())
                    .forResult(action);
        } else if (action == ACTION_ADD_ATTACHMENT) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            this.startActivityForResult(intent, ADD_ATTACHMENT_REQUEST);
        }

    }

    @Override
    public void onPermissionRefused() {
        showToast(getString(R.string.permission_request), ToastType.TYPE_WARNING);
    }

    @Override
    public void onPermissionRefusedWithNoMoreRequest() {
        showToast(getString(R.string.permission_refuse), ToastType.TYPE_ERROR);
    }

    @Override
    public void onStartUploadAttachment() {
        progressDialog.show();
        progressDialog.setMessage("??????????????????????????????...");
    }

    @Override
    public void onUploadAttachmentSuccess(AttachmentBean attachmentBean, String msg) {
        progressDialog.dismiss();
        attachments.put(attachmentBean.localPath, attachmentBean.aid);
        attachmentAdapter.addData(attachmentBean);
    }

    @Override
    public void onUploadAttachmentError(String msg) {
        progressDialog.dismiss();
        showToast(msg, ToastType.TYPE_ERROR);
    }

    @Override
    public void onGetUserPostSuccess(UserPostBean userPostBean) {
        if (userPostBean != null && userPostBean.list != null && userPostBean.list.size() > 0) {
            int tid = userPostBean.list.get(0).topic_id;
            Intent intent = new Intent(this, SharePrefUtil.isPostDetailNewStyle(this) ? PostDetail2Activity.class : PostDetailActivity.class);
            intent.putExtra(Constant.IntentKey.TOPIC_ID, tid);
            startActivity(intent);
        }
        progressDialog.dismiss();
        finish();
    }

    @Override
    public void onGetUserPostError(String msg) {
        Intent intent = new Intent(this, UserDetailActivity.class);
        intent.putExtra(Constant.IntentKey.USER_ID, SharePrefUtil.getUid(this));
        startActivity(intent);
        progressDialog.dismiss();
        finish();
    }

    @Override
    public void onMoreOptionsChanged(boolean isAnonymous, boolean isOnlyAuthor, boolean originalPic) {
        this.currentAnonymous = isAnonymous;
        this.currentOnlyAuthor = isOnlyAuthor;
        this.currentOriginalPic = originalPic;
    }

    @Override
    protected boolean registerEventBus() {
        return true;
    }

    @Override
    public void onEventBusReceived(BaseEvent baseEvent) {
        if (baseEvent.eventCode == BaseEvent.EventCode.INSERT_EMOTION) {
            contentEditor.insertEmotion((String) baseEvent.eventData);
        }
        if (baseEvent.eventCode == BaseEvent.EventCode.BOARD_SELECTED) {
            BaseEvent.BoardSelected boardSelected = (BaseEvent.BoardSelected)baseEvent.eventData;
            currentBoardId = boardSelected.boardId;
            currentBoardName = boardSelected.boardName;
            currentFilterId = boardSelected.filterId;
            currentFilterName = boardSelected.filterName;
            boardName.setText(new StringBuilder().append(currentBoardName).append("->").append(currentFilterName));
        }
        if (baseEvent.eventCode == BaseEvent.EventCode.DELETE_POLL) {
            pollLayout.setVisibility(View.GONE);
            currentPollOptions = new ArrayList<>();
            createPostPollAdapter.setNewData(currentPollOptions);
        }
        if (baseEvent.eventCode == BaseEvent.EventCode.ADD_POLL) {
            pollLayout.setVisibility(View.VISIBLE);
            BaseEvent.AddPoll addPoll = (BaseEvent.AddPoll)baseEvent.eventData;
            currentPollOptions = addPoll.pollOptions;
            currentPollChoice = addPoll.pollChoice;
            currentPollExp = addPoll.pollExp;
            currentPollVisible = addPoll.pollVisible;
            currentPollShowVoters = addPoll.showVoters;

            createPostPollAdapter.setNewData(addPoll.pollOptions);

            String a = "??????" + addPoll.pollChoice + "??????";
            String b = "?????????" + addPoll.pollExp + "??????";
            String c = "??????" + (addPoll.pollVisible ? "??????????????????" : "??????????????????");
            String d = (addPoll.showVoters ? "??????" : "?????????") + "???????????????";
            pollDesp.setText(new StringBuilder().append(a).append(b).append(c).append(d));
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_ADD_PHOTO && resultCode == Activity.RESULT_OK && data != null) {
            List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
            for (int i = 0; i < selectList.size(); i ++) {
                contentEditor.insertImage(selectList.get(i).getRealPath(), 1000);
            }
        }
        if (requestCode == AT_USER_REQUEST && resultCode == AtUserListFragment.AT_USER_RESULT && data != null) {
            contentEditor.insertText(data.getStringExtra(Constant.IntentKey.AT_USER));
        }
        if (requestCode == ADD_ATTACHMENT_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            String path = FileUtil.getRealPathFromUri(this, uri);
            if (! attachments.containsKey(path)) {
                createPostPresenter.readyUploadAttachment(this, path, currentBoardId);
            } else {
                showToast("???????????????????????????????????????", ToastType.TYPE_NORMAL);
            }
        }

    }


    /**
     * author: sca_tl
     * description: ????????????
     */
    private void onSaveDraftData() {
        List<ContentEditor.EditData> dataList = contentEditor.buildEditorData();
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < dataList.size(); i ++) {
            JSONObject content_json = new JSONObject();
            if (dataList.get(i).content_type == ContentEditor.CONTENT_TYPE_TEXT) {
                content_json.put("content_type", ContentEditor.CONTENT_TYPE_TEXT);
                content_json.put("content", dataList.get(i).inputStr);
            }
            if (dataList.get(i).content_type == ContentEditor.CONTENT_TYPE_IMAGE) {
                content_json.put("content_type", ContentEditor.CONTENT_TYPE_IMAGE);
                content_json.put("content", dataList.get(i).imagePath);
            }
            jsonArray.add(content_json);
        }

        PostDraftBean postDraftBean = new PostDraftBean();
        postDraftBean.board_id = currentBoardId;
        postDraftBean.filter_id = currentFilterId;
        postDraftBean.title = postTitle.getText().toString();
        postDraftBean.content = jsonArray.toJSONString();
        postDraftBean.board_name = currentBoardName;
        postDraftBean.filter_name = currentFilterName;
        postDraftBean.time = createTime;
        postDraftBean.poll_options = currentPollOptions.toString();
        postDraftBean.poll_choices = currentPollChoice;
        postDraftBean.poll_exp = currentPollExp;
        postDraftBean.poll_visible = currentPollVisible;
        postDraftBean.poll_show_voters = currentPollShowVoters;
        postDraftBean.anonymous = currentAnonymous;
        postDraftBean.only_user = currentOnlyAuthor;

        postDraftBean.saveOrUpdate("time = ?", String.valueOf(createTime));
    }

    private CountDownTimer countDownTimer = new CountDownTimer(3000, 1000) {
        @Override
        public void onTick(long l) { }

        @Override
        public void onFinish() {
            onSaveDraftData();
            autoSaveText.setText(String.valueOf(TimeUtil.getFormatDate(TimeUtil.getLongMs(), "HH:mm:ss") + "  ???????????????"));
            countDownTimer.start();
        }
    };

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (sendPostSuccess) {
            //??????????????????
            LitePal.deleteAll(PostDraftBean.class, "time = " + createTime);
        } else {
            onSaveDraftData();
            showToast("??????????????????", ToastType.TYPE_SUCCESS);
        }
        EventBus.getDefault().post(new BaseEvent<>(BaseEvent.EventCode.EXIT_CREATE_POST));
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        startRevealAnimation();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startRevealAnimation();
        return false;
    }

    private void startRevealAnimation() {
        Animator animator = revealableLayout.reveal(coordinatorLayout, rect == null ? 0 : rect.centerX(), rect == null ? 0 : rect.centerY(), Radius.ACTIVITY_GONE);
        animator.setDuration(500);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                CommonUtil.hideSoftKeyboard(CreatePostActivity.this, contentEditor);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                coordinatorLayout.setVisibility(View.INVISIBLE);
                finish();
                overridePendingTransition(0, 0);
            }
        });
        animator.start();
    }

    @Override
    protected void setStatusBar() {
        super.setStatusBar();
    }
}

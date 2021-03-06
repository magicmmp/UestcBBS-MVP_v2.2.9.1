package com.scatl.uestcbbs.module.post.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.scatl.uestcbbs.module.post.view.postdetail2.P2CommentFragment;
import com.scatl.uestcbbs.module.post.view.postdetail2.P2DaShangFragment;
import com.scatl.uestcbbs.module.post.view.postdetail2.P2DianPingFragment;
import com.scatl.uestcbbs.module.post.view.postdetail2.P2DianZanFragment;
import com.scatl.uestcbbs.module.post.view.postdetail2.P2HotCommentFragment;
import com.scatl.uestcbbs.util.Constant;

import java.util.ArrayList;

public class PostDetail2ViewPagerAdapter extends FragmentStatePagerAdapter{
    private ArrayList<Fragment> fragments;
    private int topicId, pid;
    private String formhash;

    public PostDetail2ViewPagerAdapter(@NonNull FragmentManager fm, int behavior, int tid, int pid, String formhash) {
        super(fm, behavior);
        this.topicId = tid;
        this.pid = pid;
        this.formhash = formhash;
        init();
    }

    private void init() {
        fragments = new ArrayList<>();

        Bundle bundle_0 = new Bundle();
        bundle_0.putInt(Constant.IntentKey.TOPIC_ID, topicId);
        bundle_0.putString(Constant.IntentKey.FORM_HASH, formhash);
        fragments.add(P2HotCommentFragment.getInstance(bundle_0));

        Bundle bundle = new Bundle();
        bundle.putInt(Constant.IntentKey.TOPIC_ID, topicId);
        bundle.putString(Constant.IntentKey.FORM_HASH, formhash);
        fragments.add(P2CommentFragment.getInstance(bundle));

        Bundle bundle_1 = new Bundle();
        bundle_1.putInt(Constant.IntentKey.TOPIC_ID, topicId);
        bundle_1.putInt(Constant.IntentKey.POST_ID, pid);
        bundle_1.putString(Constant.IntentKey.FORM_HASH, formhash);
        fragments.add(P2DianPingFragment.getInstance(bundle_1));

        Bundle bundle_2 = new Bundle();
        bundle_2.putInt(Constant.IntentKey.TOPIC_ID, topicId);
        fragments.add(P2DianZanFragment.getInstance(bundle_2));

        Bundle bundle_3 = new Bundle();
        bundle_3.putInt(Constant.IntentKey.TOPIC_ID, topicId);
        bundle_3.putInt(Constant.IntentKey.POST_ID, pid);
        fragments.add(P2DaShangFragment.getInstance(bundle_3));

    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}

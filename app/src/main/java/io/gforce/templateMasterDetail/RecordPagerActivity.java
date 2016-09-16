package io.gforce.templateMasterDetail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.List;
import java.util.UUID;

public class RecordPagerActivity extends AppCompatActivity implements RecordFragment.Callbacks {

    private ViewPager mViewPager;
    private List<Record> mRecords;
    // The newIntent method is used by the RecordHolder (RecordListFragment.java)
    // to pass fragment arguments (in this case, RecordID) to the ViewHolder
    private static final String EXTRA_RECORD_ID =
            "gforce.analog.templateMasterDetail.record_id";

    public static Intent newIntent(Context packageContext, UUID RecordId) {
        Intent intent = new Intent(packageContext, RecordPagerActivity.class);
        intent.putExtra(EXTRA_RECORD_ID, RecordId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_pager);

        UUID RecordId = (UUID) getIntent().getSerializableExtra(EXTRA_RECORD_ID);

        mViewPager = (ViewPager) findViewById(R.id.activity_record_pager_view_pager);

        mRecords = RecordLab.get(this).getRecords();
        //Get handle to your fragment manager
        //Create new page adapter and override two methods
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Record record = mRecords.get(position);
                return RecordFragment.newInstance(record.getId());
            }
            @Override
            public int getCount() {
                return mRecords.size();
            }
        });
        for (int i = 0; i < mRecords.size(); i++) {
            if (mRecords.get(i).getId().equals(RecordId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }
    @Override
    public void onRecordUpdated(Record record){

    }
}
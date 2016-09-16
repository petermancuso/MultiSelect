package io.gforce.templateMasterDetail;


import android.content.Intent;
import android.support.v4.app.Fragment;

public class RecordListActivity extends SingleFragmentActivity implements RecordListFragment.Callbacks, RecordFragment.Callbacks {

    @Override
    protected Fragment createFragment() {
        return new RecordListFragment();
    }
    @Override
    protected int getLayoutResId(){
        return R.layout.activity_masterdetail;
    }

    @Override
    public void onRecordSelected(Record record) {
        //If detail_fragment_container not present, start regular pager activity.
        if (findViewById(R.id.detail_fragment_container) == null) {
            Intent intent = RecordPagerActivity.newIntent(this, record.getId());
            startActivity(intent);
        } else {
            //Create fragment transaction that removes the existing RecordFragment from detail_fragment_container
            //(if there is one in there) and adds the RecordFragment that you want to see
            Fragment newDetail = RecordFragment.newInstance(record.getId());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, newDetail)
                    .commit();
        }

    }

    public void onRecordUpdated(Record record) {
        RecordListFragment listFragment = (RecordListFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
                        listFragment.updateUI(0);
    }
}

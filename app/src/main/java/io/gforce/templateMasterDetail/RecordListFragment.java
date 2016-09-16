package io.gforce.templateMasterDetail;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

public class RecordListFragment extends Fragment{

    private RecyclerView mRecordRecyclerView;
    private RecordAdapter mAdapter;
    private static final int REQUEST_RECORD = 1;
    private static final String ARG_RECORD_ID = "record_id";
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";
    private int recordChanged;
    private boolean mSubtitleVisible;
    private Callbacks mCallbacks;

    /**
     * Required interface for hosting activities.
     */
    public interface Callbacks {
        void onRecordSelected(Record record);
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks) activity;
    }

    //Lets FragmentManager know that RecordListFragment needs to receive menu callbacks.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_list, container, false);
        mRecordRecyclerView = (RecyclerView) view.findViewById(R.id.record_recycler_view);
        // RecyclerView requires a LayoutManager to work.
        mRecordRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // Connects Adaptor to RecyclerView

        // Retrieve State of subtitle after rotation
        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        updateUI(0);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_RECORD){
            recordChanged = data.getIntExtra(ARG_RECORD_ID, 0);
         }
    }

    // Called when RecordListActivity is resumed from detailed view.
    // Detail activity is destroyed and back stack from layout manager resumes RecordListFragment.
    @Override
    public void onResume() {
        super.onResume();
        updateUI(recordChanged);
    }

    //Save state of subtitle for use after rotation
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    // Part of the Callback interface, along with onAttach.
    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    //Inflates menu resource
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_record_list, menu);

        //Trigger a re-creation of the action items
        //when the user presses on the Show Subtitle action item.
        //Changes text according to current state.
        MenuItem subtitleItem = menu.findItem(R.id.menu_item_show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    //
    // Called when user click toolbar icon
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Check id that you assigned
        switch (item.getItemId()) {
            case R.id.menu_item_new_record:
                Record record = new Record();
                RecordLab.get(getActivity()).addRecord(record);
                updateUI(0);
                mCallbacks.onRecordSelected(record);;
                return true;
            case R.id.menu_item_show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle() {
        RecordLab recordLab = RecordLab.get(getActivity());
        int recordCount = recordLab.getRecords().size();
        //Generate subtitle string
        String subtitle = getString(R.string.subtitle_format, recordCount);

        // Shows or hide subtitle based on current state.
        if (!mSubtitleVisible){
            subtitle = null;
        }
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        //The toolbar is still refered to as 'action bar' for legacy reasons
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    //Connects the Adapter to RecyclerView. Creates a RecordAdapter and set it on the RecyclerView.
    public void updateUI(int c) {
        RecordLab recordLab = RecordLab.get(getActivity());
        List<Record> records = recordLab.getRecords();
        if(mAdapter == null) {
            mAdapter = new RecordAdapter(records);
            mRecordRecyclerView.setAdapter(mAdapter);
        // If returning from detail view, notify adaptor of data change
        } else{

            //mAdapter.notifyItemChanged(c);
            mAdapter.setRecords(records);
            mAdapter.notifyDataSetChanged();
        }
        //Updates subtitle upon return from RecordFragment
        updateSubtitle();
    }

    private class RecordHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Record mRecord;
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private CheckBox mSolvedCheckBox;

        public RecordHolder(View itemView) {
            super(itemView);
            // itemView, which is the View for the entire wor, is set as the receiver of click events.
            itemView.setOnClickListener(this);
            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_record_title_text_view);
            mDateTextView = (TextView) itemView.findViewById(R.id.list_item_record_date_text_view);
            mSolvedCheckBox = (CheckBox) itemView.findViewById(R.id.list_item_record_empty_check_box);
        }
        // Update fields to reflect the state of record
        // Called by RecordAdapter.onBindViewHolder
        public void bindRecord(Record record) {
            mRecord = record;
            mTitleTextView.setText(mRecord.getTitle());
            mDateTextView.setText(mRecord.getDate().toString());
            mSolvedCheckBox.setChecked(mRecord.isCheck0());
        }
        @Override
        public void onClick(View v) {
            mCallbacks.onRecordSelected(mRecord);
        }
    }

    // The RecyclerView will communicate with this adapter when a ViewHolder needs to be created or
    // connected with a Record object.The RecyclerView itself will not know anything about the Record
    // object, but the Adapter will know all the details
    private class RecordAdapter extends RecyclerView.Adapter<RecordHolder> {
        private List<Record> mRecords;
        public RecordAdapter(List<Record> records) {
            mRecords = records;
        }
        // onCreateViewHolder is called by the RecyclerView when it needs a new View to display an item.
        @Override
        public RecordHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            //inflate a layout list_item_record.xml.
            View view = layoutInflater.inflate(R.layout.list_item_record, parent, false);
            return new RecordHolder(view);
        }

        // Method will bind a ViewHolder’s View to your model object. It receives the ViewHolder
        // and a position in your data set. To bind your View, you use that position to find the
        // right model data. Then you update the View to reflect that model data.
        // that position is the index of the Record in your array. Once you pull it out, you
        // bind that Record to your View by sending its title to your ViewHolder’s TextView.
        @Override
        public void onBindViewHolder(RecordHolder holder, int position) {
            Record record = mRecords.get(position);
            holder.bindRecord(record);
        }
        @Override
        public int getItemCount() {
            return mRecords.size();
        }

        //swaps out the records displayed.
        public void setRecords(List<Record> records){
            mRecords = records;
        }
    }
}

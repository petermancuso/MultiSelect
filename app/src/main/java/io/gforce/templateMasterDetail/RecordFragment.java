package io.gforce.templateMasterDetail;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.Date;
import java.util.UUID;

public class RecordFragment extends Fragment {

    private static final String ARG_RECORD_ID = "record_id";
    private  static final String DIALOG_DATE = "DialogDate";
    private  static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;
    private Record mRecord;
    private File mPhotoFile;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mBooleanCheckBox;
    private Button mContactButton;
    private Button mReportButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private Callbacks mCallbacks;

    /**
     * Required interface for hosting activities.
     */
    public interface Callbacks {
        void onRecordUpdated(Record record);
    }

    public void returnResult(){
        Intent data = new Intent();
        data.putExtra(ARG_RECORD_ID, mRecord.getPosition());
        getActivity().setResult(Activity.RESULT_OK, data);
    }

    // newInstance is called to instantiate objects, instead of the constructor
    // Method that accepts a UUID, creates an arguments bundle, creates a fragment instance,
    // and then attaches the arguments to the fragment.
    public static RecordFragment newInstance(UUID RecordId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_RECORD_ID, RecordId);

        RecordFragment fragment = new RecordFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks)activity;
    }

    // Called by Hosting Activity. Can be used to save and retrieve state.
    // Note: Fragement.onCreate() does not inflate the fragments view.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID RecordId = (UUID) getArguments().getSerializable((ARG_RECORD_ID));
        mRecord = RecordLab.get(getActivity()).getRecord(RecordId);
        mPhotoFile = RecordLab.get(getActivity()).getPhotoFile(mRecord);
        returnResult();
    }

    //Record instances get written to RecordLab when RecordFragment is done.
    @Override
    public void onPause() {
        super.onPause();
        RecordLab.get(getActivity())
                .updateRecord(mRecord);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    // Inflate the layout for the fragment's view and return the inflated view.

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Parameters (layout resource ID, view's parent, add/not add to parent)
        View v = inflater.inflate(R.layout.fragment_record, container, false);

        // After inflation, get a reference to EditText
        mTitleField =(EditText)v.findViewById(R.id.record_title);

        mTitleField.setText(mRecord.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mRecord.setTitle(s.toString());
                updateRecord();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Wire up the EditText to respond to user input
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // This space intentionally left blank
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mRecord.setTitle(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {
            // This one too
            }
        });

        // Wire up widgets
        mDateButton = (Button)v.findViewById(R.id.record_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {

            //Opens dialog with DatePickerFragment loaded
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                        .newInstance(mRecord.getDate());
                dialog.setTargetFragment(RecordFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mBooleanCheckBox = (CheckBox)v.findViewById(R.id.booleanCheckBox);
        mBooleanCheckBox.setChecked(mRecord.isCheck0());
        mBooleanCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Set the Record's boolean
                mRecord.setCheck0(isChecked);
                updateRecord();
            }
        });

        //Launch implicit intent to send report
        mReportButton = (Button) v.findViewById(R.id.record_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getRecordReport());
                i.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.record_report_subject));
                //Forces choice for each new send
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);
            }
        });
        // Intent to pick contact
        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        //Launch contacts to choose a Contact
        mContactButton = (Button) v.findViewById(R.id.record_contact);
        mContactButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });
        if (mRecord.getContact() != null) {
            mContactButton.setText(mRecord.getContact());
        }
        //Check for presence of contacts application and disable button if absent.
        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mContactButton.setEnabled(false);
        }

        mPhotoButton = (ImageButton) v.findViewById(R.id.record_camera);

        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Check to see there is camera and external storage present.
        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);
        if (canTakePhoto) {
            Uri uri = Uri.fromFile(mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        mPhotoView = (ImageView) v.findViewById(R.id.record_photo);
        updatePhotoView();
        return v;
    }

    // Process date received from DataPickerFragment
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data
                    .getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mRecord.setDate(date);
            updateRecord();
            updateDate();
        //Pull contact name out of contact
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            // Specify which fields you want your query to return
            // values for.
            String[] queryFields = new String[] {
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            // Perform your query - the contactUri is like a "where"
            // clause here
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);
            try {
            // Double-check that you actually got results
                if (c.getCount() == 0) {
                    return;
                }
                // Pull out the first column of the first row of data -
                // that is your contacts name.
                c.moveToFirst();
                String contact = c.getString(0);
                mRecord.setContact(contact);
                updateRecord();
                mContactButton.setText(contact);
            } finally {
                c.close();
            }
        } else if (requestCode == REQUEST_PHOTO){
            updateRecord();
            updatePhotoView();
        }

    }

    private void updateRecord() {
        RecordLab.get(getActivity()).updateRecord(mRecord);
        mCallbacks.onRecordUpdated(mRecord);
    }
    private void updateDate(){
        mDateButton.setText(mRecord.getDate().toString());
    }
    private String getRecordReport() {
        String solvedString = null;
        if (mRecord.isCheck0()) {
            solvedString = getString(R.string.record_report_boolean);
        } else {
            solvedString = getString(R.string.record_report_negative);
        }
        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mRecord.getDate()).toString();
        String contact = mRecord.getContact();
        if (contact == null) {
            contact = getString(R.string.record_report_empty);
        } else {
            contact = getString(R.string.record_report_contact, contact);
        }
        String report = getString(R.string.record_report,
                mRecord.getTitle(), dateString, solvedString, contact);
        return report;
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(
                    mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }

}


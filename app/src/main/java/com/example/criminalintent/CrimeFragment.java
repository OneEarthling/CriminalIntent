package com.example.criminalintent;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.sql.Time;
import java.util.Date;
import java.util.UUID;

import static android.widget.CompoundButton.*;

public class CrimeFragment extends Fragment {
    private static final String ARG_CRIME_ID = "crime_id";
    private static final String ARG_CRIME_POSITION = "crime_position";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;
    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;
    private boolean mIsTabletLayout;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mSuspectCallButton;

    public static CrimeFragment newInstance(UUID crimeId, int position){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        args.putInt(ARG_CRIME_POSITION, position);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //UUID crimeID = (UUID)getActivity().getIntent().getSerializableExtra(CrimeActivity.EXTRA_CRIME_ID);
        UUID crimeID = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeID);
//        int crimePosition =  getArguments().getInt(ARG_CRIME_POSITION);
//        mCrime = CrimeLab.get(getActivity()).getCrime(crimePosition);

        setHasOptionsMenu(true);
        mIsTabletLayout = getResources().getBoolean(R.bool.large_layout);
    }

    @Override
    public void onPause(){
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton = v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsTabletLayout) {
                    FragmentManager manager = getFragmentManager();
                    DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                    dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                    dialog.show(manager, DIALOG_DATE);
                }else {
                    Intent intent = DatePickerActivity.newIntent(getActivity(), mCrime.getDate());
                    startActivityForResult(intent, REQUEST_DATE);
                }
               /* !!!
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                ft.add(R.id.fragment_container, dialog); ??
                ft.commit();*/
            }
        });

        mTimeButton = v.findViewById(R.id.crime_time);
        updateTime();
        mTimeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
            }
        });
        mSolvedCheckBox = v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });
        mReportButton = v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

//                Intent i = new Intent(Intent.ACTION_SEND);
//                i.setType("text/plain");
//                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
//                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
//                i = Intent.createChooser(i, getString(R.string.send_report));
                Intent i = ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText(getCrimeReport())
                        .setSubject(getString(R.string.crime_report_subject))
                        .setChooserTitle(R.string.send_report)
                        .createChooserIntent();
                startActivity(i);
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        //pickContact.addCategory(Intent.CATEGORY_HOME);
        mSuspectButton = v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        mSuspectCallButton = v.findViewById(R.id.crime_suspect_call);
        mSuspectCallButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String suspectPhone = getSuspectPhoneNumber();
                Uri number = Uri.parse("tel:" + suspectPhone);
                Intent i = new Intent(Intent.ACTION_DIAL, number);
                startActivity(i);
            }
        });

        if (mCrime.getSuspect() != null){
            mSuspectButton.setText(mCrime.getSuspect());
            mSuspectCallButton.setEnabled(true);
        }


        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null){
            mSuspectButton.setEnabled(false);
            mSuspectCallButton.setEnabled(false);
        }

        return v;
    }

    /*public void returnResult() {
        //getActivity().setResult(Activity.RESULT_OK);
        int crimePosition =  getArguments().getInt(ARG_CRIME_POSITION);

        getActivity().setResult(Activity.RESULT_OK, crimePosition);
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK){
            return;
        }
        if (requestCode == REQUEST_DATE){
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        } else if (requestCode == REQUEST_TIME){
            Date time = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mCrime.setDate(time);
            updateTime();
        } else if (requestCode == REQUEST_CONTACT && data != null){
            String suspectName = getSuspectName(data);
            mSuspectButton.setText(suspectName);
            mSuspectCallButton.setEnabled(true);
        }
    }

    private void updateDate() {
//        DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd yyyy");
//        mDateButton.setText(dateFormat.format(mCrime.getDate()));
//        mDateButton.setText(mCrime.getDate().toString());
        String dateFormat = "EEE MMM dd yyyy";
        mDateButton.setText(DateFormat.format(dateFormat, mCrime.getDate()).toString());
    }

    private void updateTime(){
//        DateFormat timeFormat = new SimpleDateFormat("hh:mm a z");
//        mTimeButton.setText(timeFormat.format(mCrime.getDate().getTime()));
        String timeFormat = "hh:mm a z";
        mTimeButton.setText(DateFormat.format(timeFormat, mCrime.getDate()).toString());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_crime:
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                getActivity().finish();
//                Intent intent = new Intent(getActivity(), CrimeListActivity.class);
//                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getCrimeReport(){
        String solvedString = null;
        if (mCrime.isSolved()){
            solvedString = getString(R.string.crime_report_solved);
        }else{
            solvedString = getString(R.string.crime_report_unsolved);
        }
        String dateFormat = "EE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();
        String suspect = mCrime.getSuspect();
        if (suspect == null){
            suspect = getString(R.string.crime_report_no_suspect);
        }else{
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);
        return report;
    }

    private String getSuspectName(Intent data){
        Uri contactUri = data.getData();
        // Определение полей, значения которых дб возвращены запросом
        String[] queryFields = new String[]{
//                    ContactsContract.Data.CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts._ID
        };
        //выполнение запроса - contactUri - это where
        Cursor c = getActivity().getContentResolver()
                .query(contactUri, queryFields, null,null,null);
        try{
            if(c.getCount() == 0){
                return null;
            }
            c.moveToFirst();
//          String suspect = c.getString(0);
            String suspect = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String suspectId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
            mCrime.setSuspect(suspect);
            mCrime.setSuspectId(suspectId);
            return suspect;
        } finally {
            c.close();
        }
    }

    private String getSuspectPhoneNumber(){
        String suspectID =  mCrime.getSuspectId();
        // выполнение запроса на поик телефона
        Uri phoneNumber = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        // Определение полей, значения которых дб возвращены запросом
        String[] queryFields = new String[]{
                ContactsContract.Data.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        String[] selectionArgs = new String[]{suspectID};
        //выполнение запроса на поиск телефона с заданным ID
        Cursor c2 = getActivity().getContentResolver()
                .query(phoneNumber, queryFields,
                        ContactsContract.Data.CONTACT_ID + " = ?",
                        selectionArgs,
                        null);
        String suspectPhone;
        try{
            if(c2.getCount() == 0){
                return null;
            }
            c2.moveToFirst();
            suspectPhone = c2.getString(1);
//                suspectPhone = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
            return suspectPhone;
//                mSuspectCallButton.setText(suspectPhone);
        } finally {
            c2.close();
        }
    }
}

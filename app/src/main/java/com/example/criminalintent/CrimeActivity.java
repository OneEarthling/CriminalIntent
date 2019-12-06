package com.example.criminalintent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.UUID;

public class CrimeActivity extends SingleFragmentActivity {
    private static final String EXTRA_CRIME_ID = "com.example.android.criminalintent.crime_id";
    private static final String EXTRA_CRIME_POSITION = "com.example.android.criminalintent.crime_position";

    public static Intent newIntent(Context packageContext, UUID crimeId, int position){
        Intent intent = new Intent(packageContext, CrimeActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);
        intent.putExtra(EXTRA_CRIME_POSITION, position);
        return intent;
    }
    @Override
    protected Fragment createFragment() {
        //return new CrimeFragment();
        UUID crimeId = (UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);
        int crimePosition =  getIntent().getIntExtra(EXTRA_CRIME_POSITION,0);
        return CrimeFragment.newInstance(crimeId, crimePosition);
    }

}

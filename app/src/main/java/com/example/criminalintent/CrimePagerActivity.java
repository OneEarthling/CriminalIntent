package com.example.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.List;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity {
    private static final String EXTRA_CRIME_ID = "com.example.android.criminalintent.crime_id";
    private static final String EXTRA_CRIME_POSITION = "com.example.android.criminalintent.crime_position";
    private ViewPager mViewPager;
    private List<Crime> mCrimes;
    private Button mToFirstPageButton;
    private Button mToLastPageButton;

    public static Intent newIntent(Context packageContext, UUID crimeId, int position){
        Intent intent = new Intent(packageContext, CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);
        intent.putExtra(EXTRA_CRIME_POSITION, position);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button mButton = new Button(this);
        setContentView(R.layout.activity_crime_pager);

        final UUID crimeId = (UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);
        int crimePosition =  getIntent().getIntExtra(EXTRA_CRIME_POSITION,0);

        mViewPager = findViewById(R.id.crime_view_pager);

        mToFirstPageButton = findViewById(R.id.to_first_button);
        mToFirstPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(0);
                //mToFirstPageButton.setEnabled(false);
                mToFirstPageButton.setVisibility(View.INVISIBLE);
            }
        });

        mToLastPageButton = findViewById(R.id.to_last_button);
        mToLastPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(mCrimes.size());
                mToLastPageButton.setVisibility(View.INVISIBLE);
            }
        });

        mCrimes = CrimeLab.get(this).getCrimes();
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Crime crime = mCrimes.get(position);
                //Toast.makeText(getBaseContext(), position + "hello", Toast.LENGTH_SHORT).show();

                return CrimeFragment.newInstance(crime.getId(), position);
            }

            @Override
            public int getCount() {
                return mCrimes.size();
            }
        });

        for (int i = 0; i < mCrimes.size(); i++){
            if (mCrimes.get(i).getId().equals(crimeId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (mViewPager.getCurrentItem() == 0)
                    mToFirstPageButton.setVisibility(View.INVISIBLE);
                else  mToFirstPageButton.setVisibility(View.VISIBLE);
                if (mViewPager.getCurrentItem() == mCrimes.size()-1)
                    mToLastPageButton.setVisibility(View.INVISIBLE);
                else  mToLastPageButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}

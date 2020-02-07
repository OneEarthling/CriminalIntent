package com.example.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class CrimeListFragment extends Fragment {
    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private boolean mSubtitleVisible;
    private Callbacks mCallbacks;
    public static final int NORMAL_TYPE = 0;
    public static final int POLICE_TYPE = 1;
    private static final int REQUEST_CRIME_POSITION = 2;
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";
    private OnDeleteCrime mDeleteCallBack;

    public interface OnDeleteCrime {
        void onCrimeSelectedtoDelete(Crime crime);
    }

    public interface Callbacks{
        void onCrimeSelected(Crime crime);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
        mDeleteCallBack = (OnDeleteCrime) context;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mCallbacks = null;
        mDeleteCallBack = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);
        mCrimeRecyclerView =  view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (savedInstanceState != null){
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        if (CrimeLab.get(getActivity()).getCrimes().size() == 0){
            TextView mTextView = view.findViewById(R.id.no_crimes);
            Button mButton = view.findViewById(R.id.btn_new_crime);
            mTextView.setVisibility(View.VISIBLE);
            mButton.setVisibility(View.VISIBLE);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Crime crime = new Crime();
                    CrimeLab.get(getActivity()).addCrime(crime);
                    Intent intent = CrimePagerActivity
                            .newIntent(getActivity(), crime.getId());
                    startActivity(intent);
                }
            });
        }
        updateUI();
        setCrimeRecyclerViewItemTouchListener();
        return view;
    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private Crime mCrime;
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private Button mRequiresPolice;
        private ImageView mSolvedImageView;

        public CrimeHolder(View view) {
            super(view);
            itemView.setOnClickListener(this);

            mTitleTextView =  itemView.findViewById(R.id.crime_title);
            mDateTextView = itemView.findViewById(R.id.crime_date);
            mRequiresPolice = itemView.findViewById(R.id.requires_police);
            if (mRequiresPolice != null) {
                mRequiresPolice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), mCrime.getTitle() + " полиция вызвана!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            mSolvedImageView = itemView.findViewById(R.id.crime_solved);
        }

        public void bind(Crime crime){
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            //mDateTextView.setText(new SimpleDateFormat("EEEE, dd.MM.yyyy HH:mm").format(mCrime.getDate()));
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
            String dayOfTheWeek = sdf.format(mCrime.getDate());
            mDateTextView.setText(dayOfTheWeek + ", " +DateFormat.getDateInstance().format(mCrime.getDate()));
            mSolvedImageView.setVisibility(crime.isSolved()?View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View v) {
            //Toast.makeText(getActivity(), mCrime.getTitle() + " clicked!", Toast.LENGTH_SHORT).show();
            //Intent intent = new Intent(getActivity(), CrimeActivity.class);
            //Intent intent = CrimeActivity.newIntent(getActivity(), mCrime.getId());
//            int position = getAdapterPosition();
//            //Intent intent = CrimeActivity.newIntent(getActivity(), mCrime.getId(), getAdapterPosition());
//            Intent intent = CrimePagerActivity.newIntent(getActivity(), mCrime.getId(), getAdapterPosition());
//            startActivityForResult(intent, position);
            mCallbacks.onCrimeSelected(mCrime);
        }

    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder>{
        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes){
            mCrimes = crimes;
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = null;
            switch(viewType){
                case NORMAL_TYPE:
                    view = layoutInflater.inflate(R.layout.list_item_crime, parent, false);
                    break;
                case POLICE_TYPE:
                    view = layoutInflater.inflate(R.layout.list_item_crime_police, parent, false);
                    break;
            }
            return new CrimeHolder(view);
        }

        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bind(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        @Override
        public int getItemViewType(int position){
            Crime crime = mCrimes.get(position);
            return crime.isRequiresPolice() ? 1 : 0;
        }

        public void setCrimes(List<Crime> crimes){
            mCrimes = crimes;
        }

    }

    @Override
    public void onResume(){
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible){
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else{
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
//                Intent intent = CrimePagerActivity
//                        .newIntent(getActivity(), crime.getId());
//                startActivity(intent);
                updateUI();
                mCallbacks.onCrimeSelected(crime);
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeSize = crimeLab.getCrimes().size();
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, crimeSize, crimeSize);
        //String subtitle = getString(R.string.subtitle_format, crimeCount);

        if (!mSubtitleVisible){
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    public void updateUI(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();
        /*mAdapter = new CrimeAdapter(crimes);
        mCrimeRecyclerView.setAdapter(mAdapter);*/

        if (mAdapter == null){
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        }else{
            //mAdapter.notifyDataSetChanged();
            //mAdapter.notifyItemChanged(position);
            mAdapter.setCrimes(crimes);
            mAdapter.notifyDataSetChanged();
        }
        updateSubtitle();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        //if (requestCode == resultCode){
            mAdapter.notifyItemChanged(requestCode);
        //}
    }

    public void setCrimeRecyclerViewItemTouchListener() {
        ItemTouchHelper.SimpleCallback itemTouchCallback =  new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        int position = viewHolder.getAdapterPosition();
                        Crime crime = mAdapter.mCrimes.get(position);
                        mDeleteCallBack.onCrimeSelectedtoDelete(crime);
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mCrimeRecyclerView);
    }

    public void deleteCrime(Crime crime) {
//        Crime crime = CrimeLab.get(getActivity()).getCrime(crimeId);
        CrimeLab.get(getActivity()).deleteCrime(crime);
    }
}

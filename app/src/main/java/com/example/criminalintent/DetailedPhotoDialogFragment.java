package com.example.criminalintent;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TimePicker;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.Date;

public class DetailedPhotoDialogFragment extends DialogFragment {
    private static final String ARG_IMAGE = "image";
    ImageView mDetailedPhotoView;

    public static DetailedPhotoDialogFragment newInstance(String path){
        Bundle args = new Bundle();
        args.putSerializable(ARG_IMAGE, path);

        DetailedPhotoDialogFragment fragment = new DetailedPhotoDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String path = getArguments().getString(ARG_IMAGE);
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_detailed_photo, null);

        Bitmap bitmap = PictureUtils.getScaledBitmap(path, getActivity());
        mDetailedPhotoView = v.findViewById(R.id.detailed_photo);
        mDetailedPhotoView.setImageBitmap(bitmap);

        return new AlertDialog.Builder(getActivity(), R.style.ThemeOverlay_AppCompat_Dialog)
                .setView(v)
                .create();

    }
}

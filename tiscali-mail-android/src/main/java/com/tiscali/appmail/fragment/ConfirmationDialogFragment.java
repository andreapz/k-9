package com.tiscali.appmail.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ConfirmationDialogFragment extends DialogFragment
        implements OnClickListener, OnCancelListener {
    private ConfirmationDialogFragmentListener mListener;

    private static final String ARG_DIALOG_ID = "dialog_id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_CONFIRM_TEXT = "confirm";
    private static final String ARG_CANCEL_TEXT = "cancel";
    private static final String ARG_DISMISS_ON_TOUCH_OUTSIDE = "dismiss_on_touch_outside";


    public static ConfirmationDialogFragment newInstance(int dialogId, String title, String message,
            String confirmText, String cancelText) {

        return newInstance(dialogId, title, message, confirmText, cancelText, true);
    }

    public static ConfirmationDialogFragment newInstance(int dialogId, String title, String message,
            String confirmText, String cancelText, boolean dismissOnTouchOutside) {
        ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_DIALOG_ID, dialogId);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_CONFIRM_TEXT, confirmText);
        args.putString(ARG_CANCEL_TEXT, cancelText);
        args.putBoolean(ARG_DISMISS_ON_TOUCH_OUTSIDE, dismissOnTouchOutside);
        fragment.setArguments(args);

        return fragment;
    }

    public static ConfirmationDialogFragment newInstance(int dialogId, String title, String message,
            String cancelText) {
        return newInstance(dialogId, title, message, null, cancelText);
    }


    public interface ConfirmationDialogFragmentListener {
        void doPositiveClick(int dialogId);

        void doNegativeClick(int dialogId);

        void dialogCancelled(int dialogId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(
                getArguments().getBoolean(ARG_DISMISS_ON_TOUCH_OUTSIDE, true));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String title = args.getString(ARG_TITLE);
        String message = args.getString(ARG_MESSAGE);
        String confirmText = args.getString(ARG_CONFIRM_TEXT);
        String cancelText = args.getString(ARG_CANCEL_TEXT);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        if (confirmText != null && cancelText != null) {
            builder.setPositiveButton(confirmText, this);
            builder.setNegativeButton(cancelText, this);
        } else if (cancelText != null) {
            builder.setNeutralButton(cancelText, this);
        } else {
            throw new RuntimeException("Set at least cancelText!");
        }

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE: {
                getListener().doPositiveClick(getDialogId());
                break;
            }
            case DialogInterface.BUTTON_NEGATIVE: {
                getListener().doNegativeClick(getDialogId());
                break;
            }
            case DialogInterface.BUTTON_NEUTRAL: {
                getListener().doNegativeClick(getDialogId());
                break;
            }
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        getListener().dialogCancelled(getDialogId());
    }

    private int getDialogId() {
        return getArguments().getInt(ARG_DIALOG_ID);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (getActivity() instanceof ConfirmationDialogFragmentListener) {
            mListener = (ConfirmationDialogFragmentListener) getActivity();

        }
        super.onActivityCreated(savedInstanceState);
    }

    private ConfirmationDialogFragmentListener getListener() {
        if (mListener != null) {
            return mListener;
        }

        // fallback to getTargetFragment...
        try {
            return (ConfirmationDialogFragmentListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(getTargetFragment().getClass()
                    + " must implement ConfirmationDialogFragmentListener");
        }
    }
}

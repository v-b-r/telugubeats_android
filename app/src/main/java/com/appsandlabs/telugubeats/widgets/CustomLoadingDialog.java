package com.appsandlabs.telugubeats.widgets;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.appsandlabs.telugubeats.R;


/**
 * Created by abhinav on 7/12/15.
 */
public class CustomLoadingDialog extends ProgressDialog {
    private TextView messageTextView;
    private CharSequence message;
    public CustomLoadingDialog(Context context, CharSequence text) {
        super(context);
        setIndeterminate(true);
        setCancelable(false);
        message = text;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_dialog_ui);
        messageTextView = (TextView)findViewById(R.id.progress_bar_text);
        if(message!=null)
            messageTextView.setText(message);
    }

    @Override
    public void setMessage(CharSequence message) {
        if(messageTextView!=null)
            messageTextView.setText(message);
    }
}
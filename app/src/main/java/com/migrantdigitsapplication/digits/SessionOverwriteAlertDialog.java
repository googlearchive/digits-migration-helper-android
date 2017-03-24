package com.migrantdigitsapplication.digits;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class SessionOverwriteAlertDialog extends AlertDialog.Builder {
    public SessionOverwriteAlertDialog(Context context) {
        super(context);
        setMessage("Are you sure you want to overwrite the session?");
        setCancelable(true);
        setNegativeButton(
            "No",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
    }
}

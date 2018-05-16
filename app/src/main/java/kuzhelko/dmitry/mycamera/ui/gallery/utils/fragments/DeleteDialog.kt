package kuzhelko.dmitry.mycamera.ui.gallery.utils.fragments

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import kuzhelko.dmitry.mycamera.PHOTO_ID
import kuzhelko.dmitry.mycamera.R

class DeleteDialog : DialogFragment() {

    companion object {
        fun newInstance(id: Int) =
                DeleteDialog().apply {
                    arguments = Bundle(1).apply {
                        putInt(PHOTO_ID, id)
                    }
                }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val id: Int = arguments!!.getInt(PHOTO_ID)

        val dialogListener = activity

        return if (dialogListener is DeleteDialogListener) {
            AlertDialog.Builder(dialogListener)
                    .setMessage(R.string.dialog_message_delete)
                    .setPositiveButton(R.string.yes, { _, _ ->
                        dialogListener.onDeleteDialogPositiveClick(id)
                    })
                    .setNegativeButton(R.string.cancel, { _, _ ->
                        dismiss()
                    })
                    .setCancelable(false)
                    .create()
        } else {
            super.onCreateDialog(savedInstanceState)
        }
    }

    interface DeleteDialogListener {
        fun onDeleteDialogPositiveClick(id: Int)
    }
}
package kuzhelko.dmitry.mycamera.ui.gallery.utils.fragments

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import kuzhelko.dmitry.mycamera.HTTP
import kuzhelko.dmitry.mycamera.HTTPS
import kuzhelko.dmitry.mycamera.QR_RESULT
import kuzhelko.dmitry.mycamera.R

class ResultQRCodeDialog : DialogFragment() {

    companion object {
        fun newInstance(result: String) =
                ResultQRCodeDialog().apply {
                    arguments = Bundle(1).apply {
                        putString(QR_RESULT, result)
                    }
                }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val result: String = arguments!!.getString(QR_RESULT)
                ?: activity!!.getString(R.string.nothing_found)

        val dialog = AlertDialog.Builder(activity!!)
                .setMessage(result)
        if (result.startsWith(HTTP) || result.startsWith(HTTPS)) {
            dialog.setPositiveButton(R.string.go_to_site, { _, _ ->
                openLink(result)
            })
        }
        dialog.setNegativeButton(R.string.cancel, { _, _ ->
            dismiss()
        })
                .setCancelable(false)
        return dialog.create()
    }

    private fun openLink(link: String) {
        CustomTabsIntent.Builder().build().launchUrl(activity, Uri.parse(link))
    }
}
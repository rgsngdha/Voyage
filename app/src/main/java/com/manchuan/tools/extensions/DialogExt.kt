package com.manchuan.tools.extensions

import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.dylanc.longan.textString
import com.lxj.androidktx.core.string
import com.manchuan.tools.base.BottomSheet
import com.manchuan.tools.databinding.DialogEditBinding

fun AppCompatActivity.sheetDialog(
    binding: View, title: String,
    event: (View) -> Unit,
) {
    BottomSheet.initLayoutRes(binding)
    BottomSheet.title = title
    event(binding)
    BottomSheet().show(supportFragmentManager, title)
}

fun FragmentActivity.sheetDialog(
    binding: View, title: String,
    event: (View) -> Unit,
) {
    BottomSheet.initLayoutRes(binding)
    BottomSheet.title = title
    event(binding)
    BottomSheet().show(supportFragmentManager, title)
}

fun Context.inputDialog(
    dialogTitle: String,
    content: String,
    okString: Int? = android.R.string.ok,
    inputContent: String? = null,
    okUnit: (String) -> Unit,
) {
    alertDialog {
        title = dialogTitle
        if (content != null) {
            message = content
        }
        val view = DialogEditBinding.inflate(this@inputDialog.layoutInflater)
        customView = view.root
        if (inputContent != null) view.textInputEditText.setText(inputContent)
        if (okString != null) {
            okButton(okString) {
                okUnit.invoke(view.textInputEditText.textString)
                toast(view.textInputEditText.textString)
            }
        }
        cancelButton { }
    }.build()
}

fun Fragment.inputDialog(
    dialogTitle: String,
    content: String,
    okString: Int? = android.R.string.ok,
    inputContent: String? = null,
    okUnit: (String) -> Unit,
) {
    requireContext().inputDialog(dialogTitle, content, okString, inputContent, okUnit)
}

fun Fragment.inputDialog(
    dialogTitle: String,
    content: String?,
    okString: String? = string(android.R.string.ok),
    inputContent: String? = null,
    okUnit: (String) -> Unit,
) {
    requireContext().inputDialog(dialogTitle, content, okString, inputContent, okUnit)
}

fun Context.inputDialog(
    dialogTitle: String,
    content: String?,
    okString: String? = string(android.R.string.ok),
    inputContent: String? = null,
    okUnit: (String) -> Unit,
) {
    alertDialog {
        title = dialogTitle
        if (content != null) {
            message = content
        }
        val view = DialogEditBinding.inflate(this@inputDialog.layoutInflater)
        customView = view.root
        if (inputContent != null) view.textInputEditText.setText(inputContent)
        if (okString != null) {
            okButton(okString) {
                okUnit.invoke(view.textInputEditText.textString)
                toast(view.textInputEditText.textString)
            }
        }
        cancelButton { }
    }.build()
}

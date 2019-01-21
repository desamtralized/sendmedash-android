package com.sendmedash

import android.content.Intent

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.content.pm.PackageManager


class MainActivity : AppCompatActivity() {

    companion object {
        private const val SEND_ME_DASH_URL = "sendmedash.com/#"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        handleIntent(intent)

        address.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                shareButton.isEnabled = s?.isNotEmpty() ?: false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        shareButton.setOnClickListener { share() }
    }

    private fun handleIntent(intent: Intent) {
        val text = intent.extras?.getString(Intent.EXTRA_TEXT, "") ?: ""

        if (text.isEmpty()) {
            return
        } else if (text.startsWith("X") || text.startsWith("Y")) {
            address.setText(text)
            shareSendMeDashLink(SEND_ME_DASH_URL + "dash:$text")
            finish()
        } else if (text.startsWith("dash:")){
            try {
                val uri = Uri.parse(text)

                address.setText(uri.path)
                amount.setText(uri.getQueryParameter("amount"))
                label.setText(uri.getQueryParameter("label"))

                val isValue = uri.getQueryParameter("IS") ?: uri.getQueryParameter("is") ?: "0"
                instantSend.isChecked = isValue.toBoolean()
            } catch (e: Exception) {}
            shareSendMeDashLink("$SEND_ME_DASH_URL$text")
            finish()
        } else {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun share() {
        if (address.text?.isEmpty() == true) {
            return
        }

        val linkBuilder = StringBuilder(SEND_ME_DASH_URL)
        linkBuilder.append("dash:${address.text.toString()}")

        var prefix = "?"

        if (amount.text?.isNotEmpty() == true) {
            linkBuilder.append("${prefix}amount=${amount.text.toString()}")
            prefix = "&"
        }
        if (label.text?.isNotEmpty() == true) {
            linkBuilder.append("${prefix}label=${label.text.toString()}")
            prefix = "&"
        }
        if (instantSend.isChecked) {
            linkBuilder.append("${prefix}IS=1")
        }
        shareSendMeDashLink(linkBuilder.toString())
    }

    private fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        var found = true
        try {
            packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            found = false
        }
        return found
    }

    private fun shareSendMeDashLink(link: String) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, link)
        sendIntent.type = "text/plain"

        val whatsAppPackageName = "com.whatsapp"
        if (isPackageInstalled(whatsAppPackageName, packageManager)) {
            sendIntent.setPackage(whatsAppPackageName)
        }

        startActivity(sendIntent)
    }

}

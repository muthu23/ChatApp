package com.dubaipolice.pickers.contact

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment


data class PickedContact(val number: String, val name: String?)

class ContactPicker constructor(private val requestCode: Int = 23) : Fragment() {

    private lateinit var onContactPicked: (PickedContact) -> Unit
    private lateinit var onFailure: (Throwable) -> Unit

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                getContact(data)
            }
        }

    companion object {

        private const val TAG = "ContactPicker"

        fun create(
            activity: AppCompatActivity,
            onContactPicked: (PickedContact) -> Unit,
            onFailure: (Throwable) -> Unit
        ): ContactPicker? {

            return try {
                val picker = ContactPicker()
                picker.onContactPicked = onContactPicked
                picker.onFailure = onFailure
                activity.supportFragmentManager.beginTransaction()
                    .add(picker, TAG)
                    .commitNowAllowingStateLoss()

                picker
            } catch (e: Exception) {
                onFailure(e)
                null
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return null
    }

    fun pick() {
        try {
            Intent().apply {
                data = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                action = Intent.ACTION_PICK
                resultLauncher.launch(this)
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    @SuppressLint("Range")
    fun getContact(data: Intent?) {
        var cursor: Cursor? = null
        try {
            cursor = data?.data.let { uri ->
                uri as Uri
                activity?.contentResolver?.query(uri, null, null, null, null)
            }
            cursor?.let {
                it.moveToFirst()
                val phoneNumber =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val name =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                it.close()
                onContactPicked(PickedContact(phoneNumber, name))
            }

        } catch (e: Exception) {
            onFailure(e)
            cursor?.close()
        }
    }
}
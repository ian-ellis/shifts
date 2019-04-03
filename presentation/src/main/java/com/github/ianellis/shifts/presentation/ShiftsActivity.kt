package com.github.ianellis.shifts.presentation

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.github.ianellis.shifts.presentation.databinding.ActivityShiftsBinding
import dagger.android.AndroidInjection
import javax.inject.Inject

class ShiftsActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var vm: ShiftsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        vm = ViewModelProviders.of(this, viewModelFactory)[ShiftsViewModel::class.java]
        vm.requestPermission.observe(this, Observer {
            it?.let(this::handlePermissions)
        })
        vm.showGenericError.observe(this, Observer {
            showGenericErrorDialog()
        })
        
        val binding = DataBindingUtil.setContentView<ActivityShiftsBinding>(this, R.layout.activity_shifts)
        binding.lifecycleOwner = this
        binding.viewModel = vm

    }


    private fun handlePermissions(permission: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(permission)) {
                showPermissionsDialog(permission)
            } else {
                requestPermission(permission)
            }
        }
    }

    private fun requestPermission(permission: String) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            vm.onPermissionGranted?.invoke()
        }
    }

    private fun showPermissionsDialog(permission: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.shift_location_permission_dialog_title)
            .setMessage(R.string.shift_location_permission_dialog_message)
            .setPositiveButton(R.string.shift_location_permission_dialog_positive) { dialog, which ->
                requestPermission(permission)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.shift_location_permission_dialog_negative) { dialog, which ->
                dialog.dismiss()
            }.show()
    }

    private fun showGenericErrorDialog(){
        AlertDialog.Builder(this)
            .setTitle(R.string.generic_error_dialog_title)
            .setMessage(R.string.generic_error_dialog_title)
            .setPositiveButton(R.string.shift_location_permission_dialog_positive) { dialog, which ->
                dialog.dismiss()
            }.show()
    }


}

package com.example.qr_reader_app.ui;

import android.Manifest;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.qr_reader_app.R;
import com.example.qr_reader_app.databinding.ActivityMainBinding;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    //ask a single camera permission & check
    private ActivityResultLauncher<String> resultPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission() // <- request the permission
                    , isGranted -> {
                if (isGranted) {
                    showCamera();
                } else {
                    Toast.makeText(this, "Camera permission is required for scanning QR codes", Toast.LENGTH_SHORT).show();
                }
            });

    //scanning activity
    //use barcode scanner library
    private ActivityResultLauncher<ScanOptions> qrCodeLauncher = registerForActivityResult(new ScanContract(), result -> {
       if (result.getContents() == null){
           Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
       } else {
           setResult(result.getContents());
       }
    });

    private void setResult(String contents) {
        binding.textResult.setText(contents); //output the result in the app
    }

    private void showCamera(){
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE); //only qr
        options.setPrompt("Scan QR code");
        options.setCameraId(0); //back camera
        options.setBeepEnabled(false); //no sound
        options.setBarcodeImageEnabled(true); //barcode img generation
        options.setOrientationLocked(false);

        qrCodeLauncher.launch(options);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBiding();
        initViews(); //ui event listener
    }

    private void initViews() {
        binding.fabStandard.setOnClickListener(view -> {
           checkPermissionAndShowActivity(this);
        });
    }

    private void checkPermissionAndShowActivity(Context context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {
            showCamera();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show();
        } else {
            resultPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }
    private void initBiding(){
        binding = ActivityMainBinding.inflate(getLayoutInflater()); // interact wit views w/o findViewById
        setContentView(binding.getRoot());
    }
}
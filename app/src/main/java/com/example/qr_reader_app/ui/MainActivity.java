package com.example.qr_reader_app.ui;

import android.Manifest;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
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
        //check url validity
        if (contents.startsWith("http://") || contents.startsWith("https://")) {
            //alert window to confirm redirect
            new AlertDialog.Builder(this)
                    .setTitle("Confirm redirect")
                    .setMessage("Do you want to open this link?\n"+contents)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        //open url in browser
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(contents));
                        startActivity(browserIntent);
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        } else {
            int maxSymbols = 75;
            if (contents.length() > maxSymbols) {
                //formatting too long message
                String truncatedMessage = contents.substring(0, maxSymbols-2) + "...";
                binding.textResult.setText("«" + truncatedMessage + "»");

                //notify the user the message is too long
                Toast.makeText(this, "Message is too long", Toast.LENGTH_SHORT).show();

                //click listener to show the full message
                binding.textResult.setVisibility(View.VISIBLE);
                binding.viewFullMessage.setVisibility(View.VISIBLE);
                binding.viewFullMessage.setOnClickListener(view -> showFullMessageDialog(contents));
            } else {
                binding.textResult.setText("«" + contents + "»");
                binding.textResult.setOnClickListener(null);
            }
            binding.textResult.setVisibility(View.VISIBLE);
        }
    }

    private void showFullMessageDialog(String message) {
        // Create a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Full Message");

        //scrollable TextView
        ScrollView scrollView = new ScrollView(this);
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setPadding(16, 16, 16, 16);
        textView.setTextSize(16);
        scrollView.addView(textView);

        builder.setView(scrollView);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.create().show();
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
        binding.viewFullMessage.setVisibility(View.INVISIBLE);
    }

    private void initViews() {
        binding.fabStandard.setOnClickListener(view -> {
           checkPermissionAndShowActivity(this);
            binding.viewFullMessage.setVisibility(View.INVISIBLE);
            binding.textResult.setText(null);
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

    @Override
    protected void onStop() {
        super.onStop();
        //cancel any operations related to the UI or activity lifecycle
    }
}
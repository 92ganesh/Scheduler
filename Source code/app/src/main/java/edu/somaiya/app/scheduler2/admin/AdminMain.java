package edu.somaiya.app.scheduler2.admin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import edu.somaiya.app.scheduler2.GlobalVariables;
import edu.somaiya.app.scheduler2.MainActivity;
import edu.somaiya.app.scheduler2.R;
import edu.somaiya.app.scheduler2.Welcome;
import edu.somaiya.app.scheduler2.user.UserForms;

public class AdminMain extends AppCompatActivity {
    private final int WRITE_PERMISSION_CODE=102;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_PERMISSION_CODE);
        }else{
            //  startProcess();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WRITE_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //startProcess();
                } else {
                    Toast.makeText(this,"Write permission denied",Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent it;
        if(!GlobalVariables.onEmulator){
            it = new Intent(getApplicationContext(), Welcome.class);
        }else{
            it = new Intent(getApplicationContext(), MainActivity.class);
        }
        startActivity(it);
    }

    public void radioSelect(View view){
        RadioGroup rg = findViewById(R.id.radioGroup);
        int selectedId = rg.getCheckedRadioButtonId();
        RadioButton rd = findViewById(selectedId);
        String selected = rd.getText().toString();

        if(selected.equals("Create Form")){
            Intent i = new Intent(getApplicationContext(), AdminCreateForm.class);
            startActivity(i);
        }else if(selected.equals("View Live forms")){
            Intent i = new Intent(getApplicationContext(), AdminLiveForms.class);
            startActivity(i);
        }else if(selected.equals("View Past forms")){
            Intent i = new Intent(getApplicationContext(), AdminPastForms.class);
            startActivity(i);
        }else if(selected.equals("Approve Member")){
            Intent i = new Intent(getApplicationContext(), MemberApproval.class);
            startActivity(i);
        }else if(selected.equals("View/Remove Member")){
            Intent i = new Intent(getApplicationContext(), MemberList.class);
            startActivity(i);
        }else if(selected.equals("Add Admin")){
            Intent i = new Intent(getApplicationContext(), AddAdmin.class);
            startActivity(i);
        }else if(selected.equals("View/Remove Admin")){
            Intent i = new Intent(getApplicationContext(), RemoveAdmin.class);
            startActivity(i);
        }

    }
}

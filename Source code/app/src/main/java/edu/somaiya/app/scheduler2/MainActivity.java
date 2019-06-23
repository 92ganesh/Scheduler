package edu.somaiya.app.scheduler2;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import edu.somaiya.app.scheduler2.admin.AdminMain;
import edu.somaiya.app.scheduler2.user.Registration;
import edu.somaiya.app.scheduler2.user.UserForms;

public class MainActivity extends AppCompatActivity {
    private final int WRITE_PERMISSION_CODE=102;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        if (ContextCompat.checkSelfPermission(this,
//                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_PERMISSION_CODE);
//        }else{
//          //  startProcess();
//        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case WRITE_PERMISSION_CODE: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    //startProcess();
//                } else {
//                    Toast.makeText(this,"Write permission denied",Toast.LENGTH_LONG).show();
//                }
//                break;
//            }
//        }
//    }

    public void launchAdmin(View view){
        checkSwitch();
        GlobalVariables.currUser = "admin";
        Intent i = new Intent(getApplicationContext(), AdminMain.class);
        startActivity(i);
    }

    public void launchUser(View view){
        checkSwitch();
        EditText ed = findViewById(R.id.userId);
        String userName = ed.getText().toString();
        if(!userName.equals("")) {
            GlobalVariables.currUser = userName;
        }

        ed = findViewById(R.id.designation);
        String authority = ed.getText().toString();
        if(authority.equals("pro")){
            GlobalVariables.userDesignation = "professor";
        }else if(authority.equals("aso")){
            GlobalVariables.userDesignation = "associate";
        }else if(authority.equals("asi")){
            GlobalVariables.userDesignation = "assistant";
        }else if(authority.equals("lab")){
            GlobalVariables.userDesignation = "labAssistant";
        }

        Intent i = new Intent(getApplicationContext(), UserForms.class);
        startActivity(i);
    }

    public void launchWelcome(View view){
        checkSwitch();
        Intent i = new Intent(getApplicationContext(), Welcome.class);
        startActivity(i);
    }

    public void launchRegistration(View view){
        checkSwitch();
        EditText ed = findViewById(R.id.userId);
        String userName = ed.getText().toString();
        if(!userName.equals("")) {
            GlobalVariables.currUser = userName;
        }
        Intent i = new Intent(getApplicationContext(), Registration.class);
        startActivity(i);
    }

    public void checkSwitch(){
        Switch simpleSwitch = (Switch) findViewById(R.id.mode);
        Boolean switchState = simpleSwitch.isChecked();
        if(switchState){
            GlobalVariables.onEmulator=false;
        }else{
            GlobalVariables.onEmulator=true;
        }
        Log.e("mainact",switchState+"");
    }
}


















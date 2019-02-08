package edu.somaiya.app.scheduler2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

public class MobileVerification extends AppCompatActivity {
    private String TAG = "MainActivity",mobileVerificationPurpose="";
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String verId="";
    private long timeout=30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_verification);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        mobileVerificationPurpose = extras.getString("mobileVerificationPurpose");
    }

//    private void addLog(String msg){
//        TextView tx = findViewById(R.id.logs);
//        String s = tx.getText().toString();
//        tx.setText(s+"\n*"+msg);
//    }

    public void enterOTP(View view){
        EditText ed = findViewById(R.id.mobile);
        String phoneNum = ed.getText().toString();

        if(phoneNum!=""){
            phoneNum="+91"+phoneNum;
            Intent i = new Intent(getApplicationContext(), VerifyOTP.class);
            i.putExtra("mobile",phoneNum);
            i.putExtra("mobileVerificationPurpose",mobileVerificationPurpose);
            startActivity(i);
        }
    }
}

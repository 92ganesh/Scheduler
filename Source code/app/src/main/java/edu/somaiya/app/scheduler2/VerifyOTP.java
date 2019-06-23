package edu.somaiya.app.scheduler2;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;

import java.util.concurrent.TimeUnit;

import edu.somaiya.app.scheduler2.GlobalVariables;
import edu.somaiya.app.scheduler2.MobileVerification;
import edu.somaiya.app.scheduler2.R;
import edu.somaiya.app.scheduler2.Welcome;
import edu.somaiya.app.scheduler2.admin.AdminMain;
import edu.somaiya.app.scheduler2.user.Registration;
import edu.somaiya.app.scheduler2.user.UserForms;


public class VerifyOTP extends AppCompatActivity {
    DatabaseReference fireRef;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String TAG = "OTPVerification";
    private String verId="",phoneNum,mobileVerificationPurpose="";
    Long totalUsers;
    EditText optDigit1,optDigit2,optDigit3,optDigit4,optDigit5,optDigit6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        phoneNum = extras.getString("mobile");
        mobileVerificationPurpose = extras.getString("mobileVerificationPurpose");
        Log.e(TAG,"mobile "+phoneNum);
        err("mobile "+phoneNum);


        optDigit1 = findViewById(R.id.optDigit1);
        optDigit2 = findViewById(R.id.optDigit2);
        optDigit3 = findViewById(R.id.optDigit3);
        optDigit4 = findViewById(R.id.optDigit4);
        optDigit5 = findViewById(R.id.optDigit5);
        optDigit6 = findViewById(R.id.optDigit6);

        optDigit1.addTextChangedListener(new GenericTextWatcher(optDigit1));
        optDigit2.addTextChangedListener(new GenericTextWatcher(optDigit2));
        optDigit3.addTextChangedListener(new GenericTextWatcher(optDigit3));
        optDigit4.addTextChangedListener(new GenericTextWatcher(optDigit4));
        optDigit5.addTextChangedListener(new GenericTextWatcher(optDigit5));
        optDigit6.addTextChangedListener(new GenericTextWatcher(optDigit6));

        sendOTP();
    }

    public class GenericTextWatcher implements TextWatcher
    {
        private View view;
        private GenericTextWatcher(View view)
        {
            this.view = view;
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // TODO Auto-generated method stub
            String text = editable.toString();
            switch(view.getId())
            {

                case R.id.optDigit1:
                    if(text.length()==1)
                        optDigit2.requestFocus();
                    break;
                case R.id.optDigit2:
                    if(text.length()==1)
                        optDigit3.requestFocus();
                    break;
                case R.id.optDigit3:
                    if(text.length()==1)
                        optDigit4.requestFocus();
                    break;
                case R.id.optDigit4:
                    if(text.length()==1)
                        optDigit5.requestFocus();
                    break;
                case R.id.optDigit5:
                    if(text.length()==1)
                        optDigit6.requestFocus();
                    break;
                case R.id.optDigit6:
                    verfifyOTP();
                    break;
            }
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
        }
    }

    private void verfifyOTP(){
        String otp = optDigit1.getText().toString()
                +optDigit2.getText().toString()
                +optDigit3.getText().toString()
                +optDigit4.getText().toString()
                +optDigit5.getText().toString()
                +optDigit6.getText().toString();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void sendOTP() {
        if (phoneNum == null) {
            Intent i = new Intent(getApplicationContext(), MobileVerification.class);
            startActivity(i);
        } else {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNum, 30L /*timeout*/, TimeUnit.SECONDS,
                    this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                        @Override
                        public void onCodeSent(String verificationId,
                                               PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                            // Save the verification id somewhere
                            verId = verificationId;
                            Log.e(TAG,"onCodeSent");
                            err("onCodeSent");
                            // The corresponding whitelisted code above should be used to complete sign-in.
                            //MainActivity.this.enableUserManuallyInputCode();
                        }

                        @Override
                        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                            // This callback will be invoked in two situations:
                            // 1 - Instant verification. In some cases the phone number can be instantly
                            //     verified without needing to send or enter a verification code.
                            // 2 - Auto-retrieval. On some devices Google Play services can automatically
                            //     detect the incoming verification SMS and perform verification without
                            //     user action.
                            Log.d(TAG, "onVerificationCompleted:" + phoneAuthCredential);

                            signInWithPhoneAuthCredential(phoneAuthCredential);
                        }

                        @Override
                        public void onVerificationFailed(FirebaseException e) {
                            Log.e(TAG,"onVerificationFailed");
                            err("onVerificationFailed"+e.toString());
                        }
                    }
            );
        }
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.e(TAG,"sign in success");
                            err("sign in success");
                            Intent it;
                            if(mobileVerificationPurpose.equals("registration")){
                                it = new Intent(getApplicationContext(), Registration.class);
                                it.putExtra("mobile",phoneNum);
                                startActivity(it);
                            }else if(mobileVerificationPurpose.equals("login")){
                                if(Welcome.loadedMemberList){
                                    if(Welcome.mobileCode.containsKey(phoneNum)){
                                        GlobalVariables.currUser = Welcome.mobileCode.get(phoneNum);
                                        if(Welcome.codeAuthority.get(GlobalVariables.currUser).equals("admin")){
                                            it = new Intent(getApplicationContext(), AdminMain.class);
                                            startActivity(it);
                                        }else{
                                            it = new Intent(getApplicationContext(), UserForms.class);
                                            startActivity(it);
                                        }
                                    }else{
                                        Toast.makeText(getApplicationContext(),"You are not a member",Toast.LENGTH_SHORT).show();
                                        it = new Intent(getApplicationContext(), Welcome.class);
                                        startActivity(it);
                                    }
                                }else{
                                    Toast.makeText(getApplicationContext(),"Error occured while verifying your account",Toast.LENGTH_SHORT).show();
                                    it = new Intent(getApplicationContext(), Welcome.class);
                                    startActivity(it);
                                }
                            }
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Log.e(TAG,"sign in failed");
                            err("sign in failed");
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(getApplicationContext(),"Invalid OTP",Toast.LENGTH_SHORT).show();
                                Log.e(TAG,"invalid otp");
                               err("invalid otp");
                            }
                            Intent it = new Intent(getApplicationContext(), Welcome.class);
                            startActivity(it);
                        }

                    }
                });
    }

    private void err(String msg){
        TextView tx = findViewById(R.id.errorLog);
        String s = tx.getText().toString();
        tx.setText(s+"\n*"+msg);
    }
}

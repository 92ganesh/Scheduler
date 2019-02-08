package edu.somaiya.app.scheduler2.user;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import edu.somaiya.app.scheduler2.MobileVerification;
import edu.somaiya.app.scheduler2.R;
import edu.somaiya.app.scheduler2.Welcome;

public class Registration extends AppCompatActivity {
    GoogleSignInClient mGoogleSignInClient;
    static int RC_SIGN_IN=101;
    public static String name,code,email="NA",phoneNum="NA";
    public static boolean isFirebaseConnected=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(name!=null){
            EditText ed = findViewById(R.id.userName);  ed.setText(name);
        }
        if(name!=null){
            EditText ed = findViewById(R.id.userCode);  ed.setText(code);
        }

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if(account!=null) {
            Log.e("Main", "signed in");
            Button bt = findViewById(R.id.userEmail);
            bt.setText("VERIFIED");
            bt.setTextColor(getResources().getColor(R.color.colorDarkGreen));
            bt.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    // do nothing as email is verified already
                }
            });
            email = account.getEmail();
        }else {
            Log.e("Main", "not signed in");
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String phoneNumVerified = extras.getString("mobile");
            if(phoneNumVerified!=null&&(!phoneNumVerified.equals("NA"))){
                // user signed in with mobile.
                phoneNum=phoneNumVerified;
                Button bt = findViewById(R.id.userMobile);
                bt.setText("VERIFIED");
                bt.setTextColor(getResources().getColor(R.color.colorDarkGreen));
                bt.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        // do nothing as mobile is verified already
                    }
                });
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    public void verifyEmail(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            Button bt = findViewById(R.id.userEmail);
            bt.setText("VERIFIED");
            bt.setTextColor(getResources().getColor(R.color.colorDarkGreen));
            bt.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    // do nothing as email is verified already
                }
            });

            email = account.getEmail();
        } catch (ApiException e) {
            Log.e("Main", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    public void verifyMobile(View view) {
        EditText ed=findViewById(R.id.userName);    name=ed.getText().toString();
                 ed=findViewById(R.id.userCode);    code=ed.getText().toString();


        Intent it = new Intent(getApplicationContext(), MobileVerification.class);
        it.putExtra("mobileVerificationPurpose","registration");
        startActivity(it);
    }

    public void submit(View view) {
        EditText ed=findViewById(R.id.userName);    name=ed.getText().toString();
                 ed=findViewById(R.id.userCode);    code=ed.getText().toString();

        if(name==null||name.equals("")){
            Toast.makeText(getApplicationContext(),"Enter name",Toast.LENGTH_SHORT).show();
        }else if(code==null||code.equals("")){
            Toast.makeText(getApplicationContext(),"Enter code",Toast.LENGTH_SHORT).show();
        }else if(email.equals("NA")){
            Toast.makeText(getApplicationContext(),"Verify email",Toast.LENGTH_SHORT).show();
        }else if(Welcome.loadedMemberList&&Welcome.emailCode.containsKey(email)){
            Toast.makeText(getApplicationContext(),"This email has been registered already",Toast.LENGTH_SHORT).show();
        }else if(Welcome.loadedMemberList&&(!phoneNum.equals("NA"))&&Welcome.mobileCode.containsKey(phoneNum)){
            Toast.makeText(getApplicationContext(),"This mobile number has been registered already",Toast.LENGTH_SHORT).show();
        }else{
            final HashMap<String,Object> user = new HashMap<>();
            user.put("name",name);
            user.put("code",code);
            user.put("email",email);
            user.put("mobile",phoneNum);
            user.put("authority","user");

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference();
            myRef.child("membersForApproval").child(code).setValue(user);
            Toast.makeText(getApplicationContext(),"Registration successful",Toast.LENGTH_SHORT).show();
            Intent it = new Intent(getApplicationContext(), Welcome.class);
            startActivity(it);
//            FirebaseDatabase database = FirebaseDatabase.getInstance();
//            DatabaseReference myRef = database.getReference();
//            if(isFirebaseConnected){
//                myRef.child("membersForApproval").child(code).setValue(user);
//                Toast.makeText(getApplicationContext(),"Registration successful",Toast.LENGTH_SHORT).show();
//                Intent it = new Intent(getApplicationContext(), Welcome.class);
//                startActivity(it);
//            }else{
//                Toast.makeText(getApplicationContext(),"Check your internet connection",Toast.LENGTH_SHORT).show();
//            }
        }
    }
}

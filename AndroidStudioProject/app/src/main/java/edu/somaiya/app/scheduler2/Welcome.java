package edu.somaiya.app.scheduler2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import edu.somaiya.app.scheduler2.admin.AdminMain;
import edu.somaiya.app.scheduler2.user.Registration;
import edu.somaiya.app.scheduler2.user.UserForms;

public class Welcome extends AppCompatActivity {
    GoogleSignInClient mGoogleSignInClient;
    static int RC_SIGN_IN=101;
    public static String name,code,email="NA",phoneNum="NA";
    public static HashMap<String,String> emailCode,mobileCode,codeAuthority;
    public static boolean loadedMemberList=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        emailCode=new HashMap<>();
        mobileCode=new HashMap<>();
        codeAuthority=new HashMap<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        myRef.child("membersList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();
                for (DataSnapshot contact : contactChildren) {
                    emailCode.put((String)contact.child("email").getValue(),contact.getKey());
                    mobileCode.put((String)contact.child("mobile").getValue(),contact.getKey());
                    codeAuthority.put(contact.getKey(),(String)contact.child("authority").getValue());
                }
                loadedMemberList=true;
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(getApplicationContext(),"cannot connect to database",Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(Welcome.this)
                .setTitle("Warning")
                .setMessage("Exit the app?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        finishAffinity();
                        System.exit(0);
                    }})
                .setNegativeButton(android.R.string.no, null).show();
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

    public void signInMobile(View view ){
        if(loadedMemberList) {
            Intent it = new Intent(getApplicationContext(), MobileVerification.class);
            it.putExtra("mobileVerificationPurpose", "login");
            startActivity(it);
        }else{
            Toast.makeText(getApplicationContext(),"Check your internet connection",Toast.LENGTH_SHORT).show();
        }
    }

    public void register(View view ){
        if(loadedMemberList) {
            Intent it = new Intent(getApplicationContext(), Registration.class);
            startActivity(it);
        }else{
            Toast.makeText(getApplicationContext(),"Check your internet connection",Toast.LENGTH_SHORT).show();
        }
    }

    public void signInGoogle(View view) {
        if(loadedMemberList){
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }else{
            Toast.makeText(getApplicationContext(),"Check your internet connection",Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            email = account.getEmail();
            if(loadedMemberList){
                if(emailCode.containsKey(email)){
                    GlobalVariables.currUser = emailCode.get(email);
                    if(codeAuthority.get(GlobalVariables.currUser).equals("admin")){
                        Intent it = new Intent(getApplicationContext(), AdminMain.class);
                        startActivity(it);
                    }else{
                        Intent it = new Intent(getApplicationContext(), UserForms.class);
                        startActivity(it);
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"You are not a member",Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getApplicationContext(),"Error occured while verifying your account",Toast.LENGTH_SHORT).show();
            }
        } catch (ApiException e) {
            Log.e("Main", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    public void logout(View view){
        mGoogleSignInClient.signOut();
        Toast.makeText(getApplicationContext(),"Signed out",Toast.LENGTH_SHORT).show();
    }
}

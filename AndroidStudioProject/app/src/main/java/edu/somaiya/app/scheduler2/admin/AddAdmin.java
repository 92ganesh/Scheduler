package edu.somaiya.app.scheduler2.admin;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import edu.somaiya.app.scheduler2.GlobalVariables;
import edu.somaiya.app.scheduler2.R;
import edu.somaiya.app.scheduler2.Welcome;

public class AddAdmin extends AppCompatActivity {
    boolean isFirebaseConnected=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_admin);

        // check connectivity
        isFirebaseConnected=false;
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    Log.d("MemberApproval", "firebase connected");
                    isFirebaseConnected=true;
                } else {
                    Log.d("MemberApproval", "firebase not connected");
                    isFirebaseConnected=false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("MemberApproval", "Listener was cancelled");
                isFirebaseConnected=false;
            }
        });
    }

    public void addAdmin(View view){
        String name = ((EditText)findViewById(R.id.Name)).getText().toString();
        String code = ((EditText)findViewById(R.id.Code)).getText().toString();
        String email = ((EditText)findViewById(R.id.Email)).getText().toString();
        String mobile = ((EditText)findViewById(R.id.Mobile)).getText().toString();
        if(mobile.equals(""))
            mobile="NA";

        if(name.equals("")||code.equals("")||email.equals("")){
            Toast.makeText(getApplicationContext(),"Fill necessary details",Toast.LENGTH_SHORT).show();
        }else if(Welcome.loadedMemberList&&Welcome.emailCode.containsKey(email)){
            Toast.makeText(getApplicationContext(),"This email has been registered already",Toast.LENGTH_SHORT).show();
        }else if(Welcome.loadedMemberList&&(!mobile.equals("NA"))&&Welcome.mobileCode.containsKey(mobile)){
            Toast.makeText(getApplicationContext(),"This mobile number has been registered already",Toast.LENGTH_SHORT).show();
        }else{
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference fireRef = database.getReference().child("membersList");

            HashMap<String,Object> admin = new HashMap<>();
            admin.put("name",name);
            admin.put("code",code);
            admin.put("email",email);
            admin.put("mobile",mobile);
            admin.put("authority","admin");

            if(isFirebaseConnected){
                fireRef.child(code).setValue(admin);
            }else{
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
            }
        }
    }
}

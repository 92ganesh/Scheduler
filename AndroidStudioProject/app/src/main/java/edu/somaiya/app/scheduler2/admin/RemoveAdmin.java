package edu.somaiya.app.scheduler2.admin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.HashSet;

import edu.somaiya.app.scheduler2.R;

public class RemoveAdmin extends AppCompatActivity {
    public int gridWidth=-10,rowNum=-1;
    public float textSize=20;
    public DatabaseReference myRef;
    private static HashSet<String> oldMems;
    private static HashMap<String,String> emailNameMap;
    private static String selectedMemberCode;
    boolean isFirebaseConnected=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_admin);


        // check connectivity
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    Log.d("MemberList", "firebase connected");
                    isFirebaseConnected=true;
                } else {
                    Log.d("MemberList", "firebase not connected");
                    isFirebaseConnected=false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("MemberList", "Listener was cancelled");
                isFirebaseConnected=false;
            }
        });


        oldMems=new HashSet<>();
        emailNameMap=new HashMap<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("membersList");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();
                for (DataSnapshot contact : contactChildren) {
                    String memId= contact.getKey();
                    if(!oldMems.contains(memId)&&contact.child("designation").getValue().equals("admin")) {
                        oldMems.add(memId);
                        String memName = (String)contact.child("code").getValue();
                        String memEmail = (String)contact.child("email").getValue();
                        emailNameMap.put(memEmail,memName);
                        rowNum++;
                        addFormRow(memName, memEmail,false);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(getApplicationContext(),"cannot connect to database",Toast.LENGTH_SHORT).show();
            }
        });

        // Add titles
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        gridWidth=size.x;
        rowNum++; addFormRow("NAME","Email",true);
        Log.e("mem","create");

    }


    public void addFormRow(String name, String email, boolean isTitle){
        addCell(name, 0, gridWidth/3,isTitle);  // Name
        addCell(email, 1, gridWidth/3*2,isTitle);  // Due
    }

    public void addCell(final String text, final int col, int cellSize, boolean isTitle){
        GridLayout gd = findViewById(R.id.memTableGrid);

        TextView tx = new TextView(this);
        tx.setText(text);
        tx.setTextSize(textSize);
        tx.setWidth(cellSize);
        tx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedMemberCode=text;
                if(col==1){
                    selectedMemberCode = emailNameMap.get(text);
                }

                new AlertDialog.Builder(RemoveAdmin.this)
                        .setTitle("Title")
                        .setMessage("Remove "+selectedMemberCode+" from member list?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                if(isFirebaseConnected){
                                    myRef.child(selectedMemberCode).setValue(null);
                                    Toast.makeText(getApplicationContext(), "Removed "+selectedMemberCode, Toast.LENGTH_SHORT).show();
                                    recreate();
                                }else{
                                    Toast.makeText(getApplicationContext(),"Check your internet connection",Toast.LENGTH_SHORT).show();
                                }
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        if(isTitle){
            tx.setTextColor(getResources().getColor(R.color.colorFormSelection));
            tx.setTextSize(textSize);
            tx.setTypeface(null, Typeface.BOLD);
        }

        GridLayout.LayoutParams param =new GridLayout.LayoutParams();
        param.height = GridLayout.LayoutParams.WRAP_CONTENT;
        param.width = GridLayout.LayoutParams.WRAP_CONTENT;
        param.columnSpec = GridLayout.spec(col);
        param.rowSpec = GridLayout.spec(rowNum);
        tx.setLayoutParams (param);
        gd.addView(tx);
    }
}

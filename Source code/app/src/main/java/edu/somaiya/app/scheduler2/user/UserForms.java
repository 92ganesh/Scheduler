package edu.somaiya.app.scheduler2.user;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import edu.somaiya.app.scheduler2.GlobalVariables;
import edu.somaiya.app.scheduler2.MainActivity;
import edu.somaiya.app.scheduler2.R;
import edu.somaiya.app.scheduler2.Welcome;
import edu.somaiya.app.scheduler2.admin.AdminCreateForm;
import edu.somaiya.app.scheduler2.admin.MemberList;

public class UserForms extends AppCompatActivity {
    public int gridWidth=-10,rowNum=-1;
    public float textSize=20;
    public DatabaseReference myRef,serverTimeOffsetRef;
    private static HashSet<String> oldForms;
    private static boolean firstTimeDataload=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_forms);

        oldForms=new HashSet<>();
        firebaseStuff();

        // Add titles
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        gridWidth=size.x;
        rowNum++; addFormRow("ID","NAME","DUE",true);
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

    public void addFormRow(final String id, String name, String due, boolean isTitle){
        addCell(id, id, 0, gridWidth/8,isTitle);  // ID
        addCell(name, id, 1, gridWidth/8*3,isTitle);  // Name
        addCell(due, id, 2, gridWidth/8*2,isTitle);  // Due

        if(rowNum!=0) {
            GridLayout gd = findViewById(R.id.userFormGrid);
            Button btnTag = new Button(this);
            btnTag.setWidth(gridWidth/8);
            btnTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GlobalVariables.currForm=id;
                    GlobalVariables.typeForm="formLive";

                    Intent i = new Intent(getApplicationContext(), UserGrid.class);
                    startActivity(i);
                }
            });
            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.height = GridLayout.LayoutParams.WRAP_CONTENT;
            param.width = GridLayout.LayoutParams.WRAP_CONTENT;
            param.columnSpec = GridLayout.spec(3);
            param.rowSpec = GridLayout.spec(rowNum);
            btnTag.setLayoutParams(param);
            btnTag.setText("FILL");

            gd.addView(btnTag);
        }
    }

    public void addCell(String text,final String tag, final int col, int cellSize,boolean isTitle){
        GridLayout gd = findViewById(R.id.userFormGrid);


        TextView tx = new TextView(this);
        tx.setText(text);
        tx.setTextSize(textSize);
        tx.setTag(tag);
        tx.setWidth(cellSize);

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

    private void firebaseStuff(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        myRef.child("formLive").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(firstTimeDataload){
                    firstTimeDataload=false;
                    Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();
                    for (DataSnapshot contact : contactChildren) {
                        String formId= contact.getKey();
                        if(!oldForms.contains(formId)) {
                            oldForms.add(formId);
                            String formName = (String)contact.child("name").getValue();
                            String formDue = (String)contact.child("due").getValue();

                            if(isFormWithinDue(contact,formId,formName,formDue)){
                                rowNum++;
                                addFormRow(formId, formName, formDue,false);
                            }
                        }
                    }
                }else{
                    firstTimeDataload=true;
                    recreate();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(getApplicationContext(),"cannot connect to database",Toast.LENGTH_SHORT).show();
            }
        });

        serverTimeOffsetRef = database.getReference(".info/serverTimeOffset");
        serverTimeOffsetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GlobalVariables.serverTimeOffset = (long)dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(getApplicationContext(),"cannot connect to database",Toast.LENGTH_LONG).show();
            }
        });
    }

    public boolean isFormWithinDue(DataSnapshot contact ,String formId, String formName, String formDue){
        // check due date
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yy");
        Date date = null;
        try {
            date = sdf.parse(formDue);
            long dueDateMillis = date.getTime()+5184000;    // set time till end of due date
            long serverTimeMillis = System.currentTimeMillis()+GlobalVariables.serverTimeOffset;
            if(serverTimeMillis<=dueDateMillis){
                // form is still within the due date
                Log.e("UserForms.java",formName+" within due");
                return true;
            }else{
                // form has crossed the due date
                Log.e("UserForms.java",formName+" out of due");
                HashMap<String,Object> formBackup = (HashMap<String, Object>) contact.getValue();
                myRef.child("formPast").child(formId).setValue(formBackup);
                myRef.child("formLive").child(formId).setValue(null);
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("UserForms.java",e.toString());
            return false;
        }
    }
}

package edu.somaiya.app.scheduler2.admin;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;

import edu.somaiya.app.scheduler2.GlobalVariables;
import edu.somaiya.app.scheduler2.R;
import edu.somaiya.app.scheduler2.user.UserGrid;

public class AdminLiveForms extends AppCompatActivity {
    public int gridWidth=-10,rowNum=-1;
    public float textSize=20;
    private static HashSet<String> oldForms;
    public DatabaseReference myRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_live_forms);

        oldForms=new HashSet<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("formLive");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                // String value = dataSnapshot.getValue(String.class);
                Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();
                for (DataSnapshot contact : contactChildren) {
                    String formId= contact.getKey();
                    if(!oldForms.contains(formId)) {
                        oldForms.add(formId);
                        String formName = (String) contact.child("name").getValue();
                        String formDue = (String) contact.child("due").getValue();
                        rowNum++;
                        addFormRow(formId, formName, formDue,false);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(getApplicationContext(),"cannot connect to database",Toast.LENGTH_SHORT).show();
            }
        });

        addForms();
    }

    public void addForms(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        gridWidth=size.x;
        rowNum++; addFormRow("ID","NAME","DUE",true);
    }

    public void addFormRow(final String id, String name, String due,boolean isTitle){
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

                    Intent i = new Intent(getApplicationContext(), AdminGrid.class);
                    startActivity(i);
                }
            });
            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.height = GridLayout.LayoutParams.WRAP_CONTENT;
            param.width = GridLayout.LayoutParams.WRAP_CONTENT;
            param.columnSpec = GridLayout.spec(3);
            param.rowSpec = GridLayout.spec(rowNum);
            btnTag.setLayoutParams(param);
            btnTag.setText("VIEW");

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





}

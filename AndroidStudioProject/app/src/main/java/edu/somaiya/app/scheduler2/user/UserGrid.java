package edu.somaiya.app.scheduler2.user;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.opengles.GL;

import edu.somaiya.app.scheduler2.GlobalVariables;
import edu.somaiya.app.scheduler2.R;

public class UserGrid extends AppCompatActivity {
    public int gridWidth=-10,gridHeigth=-10,totalSelected,rows,cols, maxInTotal=3;
    Map<String,Object> formDetails, formDetailsSecondary;
    String rowNames="", colNames="", userSelectionStr;
    public DatabaseReference myRef;
    int[] selectedInRow, selectedInCol;
    boolean dataReadOnce=false, isFirebaseConnected=false, submitStatus;
    boolean[][] userSelection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_grid);

        // get window size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        gridWidth=size.x;
        gridHeigth=size.y;

        // check connectivity
        isFirebaseConnected=false;
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    Log.d("UserGrid", "firebase connected");
                    isFirebaseConnected=true;
                } else {
                    Log.d("UserGrid", "firebase not connected");
                    isFirebaseConnected=false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("main", "Listener was cancelled");
                isFirebaseConnected=false;
            }
        });

        // get firebase instance
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
                    if(formId.equals(GlobalVariables.currForm)){
                        if(!dataReadOnce){
                            rows = Integer.parseInt((String)contact.child("totalRows").getValue() );
                            cols = Integer.parseInt((String)contact.child("totalCols").getValue() );
                            selectedInRow=new int[rows];
                            selectedInCol=new int[cols];
                            rowNames = (String)contact.child("rowNames").getValue();
                            colNames = (String)contact.child("colNames").getValue();
                            if(GlobalVariables.userDesignation.equals(GlobalVariables.professor)){
                                maxInTotal = Integer.parseInt((String)contact.child("totalSelectionProfessor").getValue() );
                            }else  if(GlobalVariables.userDesignation.equals(GlobalVariables.associate)){
                                maxInTotal = Integer.parseInt((String)contact.child("totalSelectionAssociate").getValue() );
                            }else  if(GlobalVariables.userDesignation.equals(GlobalVariables.assistant)){
                                maxInTotal = Integer.parseInt((String)contact.child("totalSelectionAssistant").getValue() );
                            }else  if(GlobalVariables.userDesignation.equals(GlobalVariables.labAssistant)){
                                maxInTotal = Integer.parseInt((String)contact.child("totalSelectionLabAssistant").getValue() );
                            }else{
                                maxInTotal = 0; // if any new/unknown designation has been added
                            }

                            String memberActivity = (String)contact.child(GlobalVariables.currUser).getValue();
                            if(memberActivity!=null){
                                Toast.makeText(getApplicationContext(),"Form has been filled already",Toast.LENGTH_SHORT).show();
                                ((Button)findViewById(R.id.submit)).setVisibility(View.INVISIBLE);
                            }

                            userSelection=new boolean[rows][cols];
                            dataReadOnce=true;
                            TextView tx = findViewById(R.id.maxInTotal);  tx.setText("Number of choices required: "+maxInTotal);
                        }

                        if(GlobalVariables.userDesignation.equals(GlobalVariables.labAssistant)){
                            formDetails = (HashMap)contact.child("formTableDetailsLab").getValue();
                            formDetailsSecondary = (HashMap)contact.child("formTableDetails").getValue();
                        }else{
                            formDetails = (HashMap)contact.child("formTableDetails").getValue();
                            formDetailsSecondary = (HashMap)contact.child("formTableDetailsLab").getValue();
                        }

                        makeGrid();
                     }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(getApplicationContext(),"cannot connect to database",Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void makeGrid(){
        String[] rn = rowNames.split("!");
        String[] cn = colNames.split("!");

        for(int i=1; i<=cols; i++){
            addButton(0,i,cn[i-1],true);
        }
        for(int i=1; i<=rows; i++){
            addButton(i,0,rn[i-1],true);
        }

        for(int i=1; i<=rows; i++){
            for(int j=1; j<=cols; j++){
                String loc = i+","+j;
                addButton(i,j,(String)formDetails.get(loc), false);
            }
        }

    }

    public void addButton(int r, int c,String label,boolean NamePlates){
        GridLayout gd = findViewById(R.id.userTableGrid);
        Button btnTag = new Button(this);
        btnTag.setId( ((r-1)*cols) + (c-1) + 1 );
        btnTag.setText(label);
        if(!NamePlates) {
            if (label.equals("0")) {
                btnTag.setBackground(this.getResources().getDrawable(R.drawable.red));
            }else if (userSelection[r-1][c-1]) {
                btnTag.setBackground(this.getResources().getDrawable(R.drawable.green));
            }else  {
                btnTag.setBackground(this.getResources().getDrawable(R.drawable.grey));
            }
            btnTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = ((int)v.getId()) - 1 ;
                    int rowN = id/cols;
                    int colN = id%cols;
                    String loc = (rowN+1)+","+(colN+1);
                    int freeCount = Integer.parseInt( (String)formDetails.get(loc));

                    if(userSelection[rowN][colN]){
                        userSelection[rowN][colN]=false;
                        v.setBackground(getResources().getDrawable(R.drawable.grey));
                        totalSelected--;
                    }else if(totalSelected==maxInTotal){
                        Toast.makeText(getApplicationContext(),"Reached selection limit",Toast.LENGTH_SHORT).show();
                    }else if(freeCount<=0){
                        Toast.makeText(getApplicationContext(),"No free slot available",Toast.LENGTH_SHORT).show();
                    }else{
                        userSelection[rowN][colN]=true;
                        v.setBackground(getResources().getDrawable(R.drawable.green));
                        totalSelected++;
                    }
                }
            });
        }

        btnTag.setWidth(gridWidth/6);
        btnTag.setHeight(gridHeigth/15);

        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.height = GridLayout.LayoutParams.WRAP_CONTENT;
        param.width = GridLayout.LayoutParams.WRAP_CONTENT;
        param.columnSpec = GridLayout.spec(c);
        param.rowSpec = GridLayout.spec(r);
        btnTag.setLayoutParams(param);

        gd.addView(btnTag);
    }

    public void submit(View view){
        if(totalSelected<maxInTotal){
            Toast.makeText(getApplicationContext(),"Select "+(maxInTotal-totalSelected)+" more choices",Toast.LENGTH_SHORT).show();
            return;
        }

        userSelectionStr="";
        for(int i=0;i<userSelection.length;i++){
            for(int j=0;j<userSelection[i].length;j++){
                if(userSelection[i][j]){
                    String loc = (i+1)+","+(j+1);
                    if(userSelectionStr.equals("")){
                        userSelectionStr+=loc;
                    }else{
                        userSelectionStr+="!"+loc;
                    }

                    if(GlobalVariables.userDesignation.equals(GlobalVariables.labAssistant)) {
                        int freeCount = Integer.parseInt((String) formDetails.get(loc));
                        int freeCountSecondary = Integer.parseInt((String) formDetailsSecondary.get(loc));
                        if (freeCount > 0) {
                            formDetails.put(loc, (freeCount - 1) + "");
                        }
                        if (freeCountSecondary > 0) {
                            formDetailsSecondary.put(loc, (freeCountSecondary - 1) + "");
                        }
                    }else{  // for professor, associate and assitant
                        int freeCount = Integer.parseInt((String) formDetails.get(loc));
                        int freeCountSecondary = Integer.parseInt((String) formDetailsSecondary.get(loc));
                        if(freeCount==freeCountSecondary){
                            if (freeCount > 0) {
                                formDetails.put(loc, (freeCount - 1) + "");
                            }
                            if (freeCountSecondary > 0) {
                                formDetailsSecondary.put(loc, (freeCountSecondary - 1) + "");
                            }
                        }else if(freeCount>freeCountSecondary){
                            if (freeCount > 0) {
                                formDetails.put(loc, (freeCount - 1) + "");
                            }
                        }

                    }
                }
            }
        }

        if(isFirebaseConnected){
            if(GlobalVariables.userDesignation.equals(GlobalVariables.labAssistant)){
                myRef.child(GlobalVariables.currForm).child("formTableDetailsLab").setValue(formDetails);
                myRef.child(GlobalVariables.currForm).child("formTableDetails").setValue(formDetailsSecondary);
            }else{
                myRef.child(GlobalVariables.currForm).child("formTableDetails").setValue(formDetails);
                myRef.child(GlobalVariables.currForm).child("formTableDetailsLab").setValue(formDetailsSecondary);
            }
            myRef.child(GlobalVariables.currForm).child("memberActivity").child(GlobalVariables.currUser).setValue(userSelectionStr);
            Toast.makeText(getApplicationContext(),"Form submitted",Toast.LENGTH_SHORT).show();
            ((Button)findViewById(R.id.submit)).setVisibility(View.INVISIBLE);
        }else{
            Toast.makeText(getApplicationContext(),"Check your internet connection",Toast.LENGTH_SHORT).show();
        }
    }
}

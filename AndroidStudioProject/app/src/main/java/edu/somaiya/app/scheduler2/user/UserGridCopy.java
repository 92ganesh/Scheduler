package edu.somaiya.app.scheduler2.user;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.net.ConnectivityManager;
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

public class UserGridCopy extends AppCompatActivity {
    public int gridWidth=-10,gridHeigth=-10,totalSelected,rows,cols,maxInRow=1,maxInCol=1,maxInTotal=1;
    Map<String,Object> formDetails;
    String rowNames="", colNames="";
    public DatabaseReference myRef;
    int[] selectedInRow, selectedInCol;
    boolean dataReadOnce=false, isFirebaseConnected=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_user_grid);


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
                            maxInRow = Integer.parseInt((String)contact.child("maxInRow").getValue() );
                            maxInCol = Integer.parseInt((String)contact.child("maxInCol").getValue() );
                            maxInTotal = Integer.parseInt((String)contact.child("totalSelection").getValue() );
                            dataReadOnce=true;

                            TextView tx = findViewById(R.id.maxInRow);  tx.setText("Max selection allowed in a row: "+maxInRow);
                            tx = findViewById(R.id.maxInCol);  tx.setText("Max selection allowed in a column: "+maxInCol);
                            tx = findViewById(R.id.maxInTotal);  tx.setText("Max selection allowed in total: "+maxInTotal);
                        }
                        formDetails = (HashMap)contact.child("formTableDetails").getValue();
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

    public void addButton(int r, int c,String owner,boolean NamePlates){
        GridLayout gd = findViewById(R.id.userTableGrid);
        Button btnTag = new Button(this);
        btnTag.setId( ((r-1)*cols) + (c-1) + 1 );

        if(!NamePlates) {
            if (owner.equals("free")) {
                btnTag.setBackground(this.getResources().getDrawable(R.drawable.grey));
            } else if (owner.equals(GlobalVariables.currUser)) {
                btnTag.setBackground(this.getResources().getDrawable(R.drawable.green));
            } else {
                btnTag.setBackground(this.getResources().getDrawable(R.drawable.red));
            }
            btnTag.setText("");
            btnTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                int id = ((int)v.getId()) - 1 ;
                int rowN = id/cols;
                int colN = id%cols;
                String loc = (rowN+1)+","+(colN+1);

                if(totalSelected==maxInTotal){
                    Toast.makeText(getApplicationContext(),"Reached selection limit",Toast.LENGTH_SHORT).show();
                }else if(selectedInRow[rowN]==maxInRow){
                    Toast.makeText(getApplicationContext(),"Reached limit of max selection in a row",Toast.LENGTH_SHORT).show();
                }else if(selectedInCol[colN]==maxInCol){
                    Toast.makeText(getApplicationContext(),"Reached limit of max selection in a column",Toast.LENGTH_SHORT).show();
                }else{
                    if(isFirebaseConnected){
                        if(formDetails.get(loc).equals("free")){
                            myRef.child(GlobalVariables.currForm).child("formTableDetails").child(loc).setValue(GlobalVariables.currUser);
                            selectedInRow[rowN]++;  selectedInCol[colN]++;  totalSelected++;

                            if(totalSelected==maxInTotal){
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UserGridCopy.this);
                                alertDialogBuilder.setMessage("Form filled successfully");
                                alertDialogBuilder.setPositiveButton("OK",null);

                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            }
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"Check your internet connection",Toast.LENGTH_SHORT).show();
                    }
                }
                }
            });
        }else{
            btnTag.setText(owner);
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
}

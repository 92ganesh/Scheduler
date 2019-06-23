package edu.somaiya.app.scheduler2.admin;

import android.graphics.Point;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
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

import org.w3c.dom.Text;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import edu.somaiya.app.scheduler2.GlobalVariables;
import edu.somaiya.app.scheduler2.R;

public class AdminGrid extends AppCompatActivity {

    public int gridWidth=-10,gridHeigth=-10,rows,cols;
    public int totalSelectionAssistant,totalSelectionAssociate,totalSelectionLabAssistant,totalSelectionProfessor;
    HashMap<String,Object> formDetails,formDetailsLab,memberListMap;
    HashMap<String,String> memberActivityMap;
    TreeMap<String,String> memberActivityMapSorted;
    String rowNames="", colNames="", csvString="", groupLabelNames="", formName="";
    public DatabaseReference myRef;
    boolean isFirebaseConnected=false;
    String[][] gridMat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_grid);

        // get window size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        gridWidth=size.x;
        gridHeigth=size.y;

        Log.e("grid", "cre");

        // get firebase instance
        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        final DatabaseReference memberListRef = database.getReference().child("membersList");
//        memberListRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                memberListMap = (HashMap<String, Object>) dataSnapshot.getValue();
//                if(memberListMap!=null) {
//                    gridMat = new String[memberListMap.size()+5][rows+1];
//                }else{
//                    gridMat = new String[5][rows+1];
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                // Failed to read value
//                Toast.makeText(getApplicationContext(),"cannot connect to database",Toast.LENGTH_SHORT).show();
//            }
//        });

        myRef = database.getReference();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                // String value = dataSnapshot.getValue(String.class);


                memberListMap = (HashMap<String, Object>) dataSnapshot.child("membersList").getValue();
                Iterable<DataSnapshot> contactChildren = dataSnapshot.child(GlobalVariables.typeForm).getChildren();
                for (DataSnapshot contact : contactChildren) {
                    String formId= contact.getKey();
                    if(formId.equals(GlobalVariables.currForm)){
                        rows = Integer.parseInt((String)contact.child("totalRows").getValue() );
                        cols = Integer.parseInt((String)contact.child("totalCols").getValue() );
                        formDetails = (HashMap)contact.child("formTableDetails").getValue();
                        formDetailsLab = (HashMap)contact.child("formTableDetailsLab").getValue();
                        rowNames = (String)contact.child("rowNames").getValue();
                        colNames = (String)contact.child("colNames").getValue();
                        groupLabelNames = (String)contact.child("groupLabelNames").getValue();
                        formName = (String)contact.child("name").getValue();
                        memberActivityMap = (HashMap<String, String>) contact.child("memberActivity").getValue();
                        totalSelectionAssistant = Integer.parseInt((String)contact.child("totalSelectionAssistant").getValue());
                        totalSelectionAssociate = Integer.parseInt((String)contact.child("totalSelectionAssociate").getValue());
                        totalSelectionLabAssistant = Integer.parseInt((String)contact.child("totalSelectionLabAssistant").getValue());
                        totalSelectionProfessor = Integer.parseInt((String)contact.child("totalSelectionProfessor").getValue());
//                        if(memberActivityMap!=null) {
//                            gridMat = new String[memberActivityMap.size()+4][rows+1];
//                        }else{
//                            gridMat = new String[4][rows+1];
//                        }
                        if(memberListMap!=null) {
                            gridMat = new String[memberListMap.size()+5+5][rows+1];
                        }else{
                            gridMat = new String[5+5][rows+1];
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
        String[] gn = groupLabelNames.split("!");
        int gridCols = rows;

        for(int i=0;i<gridMat.length;i++){
            for(int j=0;j<gridMat[i].length;j++){
                addButton(i,j,"",false);
                gridMat[i][j] = "";
            }
        }

        addButton(0,0,"",true);

        // rows contains num of slots
        // Note:- slot are represented as columns here.
        for(int i=1; i<=gridCols; i++){
            addButton(0,i,gn[i-1],true);
            addButton(1,i,rn[i-1],true);
            gridMat[0][i] = gn[i-1];
            gridMat[1][i] = rn[i-1];
        }

        int i=2;
        if(memberActivityMap!=null){
            memberActivityMapSorted=new TreeMap<>();
            for(Map.Entry<String,String> entry: memberActivityMap.entrySet()) {
                memberActivityMapSorted.put(entry.getKey(),entry.getValue());
            }
            for(Map.Entry<String,String> entry: memberActivityMapSorted.entrySet()){
                addButton(i,0,entry.getKey(),true);
                gridMat[i][0] = entry.getKey();
                String[] userSelection = entry.getValue().split("!");
                for(String each:userSelection){
                    int colNum = Integer.parseInt( (each.split(","))[0]);
                    addButton(i,colNum,"X",false);
                    gridMat[i][colNum] = "X";
                }
                i++;
            }
        }

        Log.e("grid", "ran");
        boolean firstNotFilled=true;
        // members those who did not fill form
        for(Map.Entry<String,Object> entry: memberListMap.entrySet()){
            String userDesignation = ((HashMap<String,String>)entry.getValue()).get("designation");
            if(!userDesignation.equals("admin")){
                if(memberActivityMap==null || (!memberActivityMap.containsKey(entry.getKey())&&
                        (userDesignation.equals(GlobalVariables.labAssistant)||userDesignation.equals(GlobalVariables.assistant)||
                                userDesignation.equals(GlobalVariables.associate)||userDesignation.equals(GlobalVariables.professor))) ){
                    if(firstNotFilled){
                        addButton(i,0,"",true);
                        gridMat[i][0] = "";
                        firstNotFilled=false; i++;
                    }
                    addButton(i,0,entry.getKey(),true);
                    gridMat[i][0] = entry.getKey();
                    i++;
                }
            }
        }

        addButton(i,0,"Total Assigned",true);
        gridMat[i][0] = "Total Assigned";
        for(int j=1; j<=gridCols; j++){
            int xCount=0;
            for(int k=2; k<i; k++) {
                if(gridMat[k][j].equals("X")){
                    xCount++;
                }
            }
            gridMat[i][j] = xCount+"";
            addButton(i,j,xCount+"",false);
        }

        addButton(i+1,0,"Free",true);
        gridMat[i+1][0] = "Free";
        for(int j=1; j<=gridCols; j++){
            gridMat[i+1][j] = formDetails.get(j+",1")+"";
            addButton(i+1,j,formDetails.get(j+",1")+"",false);
        }

        addButton(i+2,0,"Designation",true);
        gridMat[i+1][0] = "Designation";
        addButton(i+2,1,"Limit",true);
        gridMat[i+1][0] = "Limit";

        addButton(i+3,0,"Professor",true);
        gridMat[i+3][0] = "Professor";
        addButton(i+3,1,""+totalSelectionProfessor,true);
        gridMat[i+3][0] = ""+totalSelectionProfessor;

        addButton(i+4,0,"Associate",true);
        gridMat[i+4][0] = "Associate";
        addButton(i+4,1,""+totalSelectionAssociate,true);
        gridMat[i+4][0] = ""+totalSelectionAssociate;

        addButton(i+5,0,"Assistant",true);
        gridMat[i+5][0] = "Assistant";
        addButton(i+5,1,""+totalSelectionAssistant,true);
        gridMat[i+5][0] = ""+totalSelectionAssistant;

        addButton(i+6,0,"Lab assist",true);
        gridMat[i+6][0] = "Lab assist";
        addButton(i+6,1,""+totalSelectionLabAssistant,true);
        gridMat[i+6][0] = ""+totalSelectionLabAssistant;
    }

    public void addButton(int r, int c,String owner,boolean NamePlates){
        GridLayout gd = findViewById(R.id.userTableGrid);
        TextView txt = new TextView(this);
        txt.setWidth(gridWidth/6);
        txt.setHeight(gridHeigth/15);

        txt.setTextSize(20);
        txt.setPadding(0,10,0,10);
        txt.setBackground(getResources().getDrawable(R.drawable.border));
        txt.setId( ((r-1)*cols) + (c-1) +1 );


        if(!NamePlates) {
            if (owner.equals("free")) {
                txt.setTextColor(getResources().getColor(R.color.colorFree));
            } else {
                txt.setTextColor(getResources().getColor(R.color.colorAssigned));
            }
            txt.setGravity(Gravity.CENTER_HORIZONTAL);
        }else{
            txt.setTextColor(getResources().getColor(R.color.colorGridlabels));
        }
        txt.setText(owner);
        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.height = GridLayout.LayoutParams.WRAP_CONTENT;
        param.width = GridLayout.LayoutParams.WRAP_CONTENT;
        param.columnSpec = GridLayout.spec(c);
        param.rowSpec = GridLayout.spec(r);
        txt.setLayoutParams(param);
        gd.addView(txt);
    }

    public void saveToCSV(View view){
        csvString="";
        for(int i=0;i<gridMat.length;i++){
            for(int j=0;j<gridMat[i].length;j++){
                csvString+=gridMat[i][j];
                if(i<gridMat[i].length){ csvString+=" ,";}
            }
            csvString+="\n";
        }

        Log.e("csv",csvString);
        String sdcardFolder = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"
                            +GlobalVariables.currForm+"_"+formName+".csv";
        FileWriter sb = null;
        try {
            sb = new FileWriter(sdcardFolder);
            sb.append(csvString);
            sb.close();
            Toast.makeText(getApplicationContext(),"saved to disk",Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("AdminGrid.java",e.toString());
            Toast.makeText(getApplicationContext(),"failed to save",Toast.LENGTH_LONG).show();
        }
    }

    public void autoAssign(View view){
        if(memberListMap==null){
            Toast.makeText(getApplicationContext(),"cannot connect to database",Toast.LENGTH_SHORT).show();
        }else if(formDetails!=null){
            String[] gn = groupLabelNames.split("!");
            int[] freeArray = new int[gn.length];
            for(int i=0;i<freeArray.length;i++){
                freeArray[i] = Integer.parseInt( (String)formDetails.get((i+1)+",1"));
            }

            if(memberActivityMap!=null){
                for(Map.Entry<String,String> entry: memberActivityMap.entrySet()){
                    if(memberListMap.containsKey(entry.getKey())){
                        memberListMap.remove(entry.getKey());
                    }
                }
            }

            for(Map.Entry<String,Object> entry: memberListMap.entrySet()){
                String code = entry.getKey();
                String userDesignation = ((HashMap<String,String>)entry.getValue()).get("designation");
                int maxInTotal = 0,totalSelected=0;
                if(userDesignation.equals(GlobalVariables.professor)){
                    maxInTotal = totalSelectionProfessor;
                }else  if(userDesignation.equals(GlobalVariables.associate)){
                    maxInTotal = totalSelectionAssociate;
                }else  if(userDesignation.equals(GlobalVariables.assistant)){
                    maxInTotal = totalSelectionAssistant;
                }else  if(userDesignation.equals(GlobalVariables.labAssistant)){
                    maxInTotal = totalSelectionLabAssistant;
                }else{
                    maxInTotal = 0; // if any new/unknown designation has been added
                }

                Log.e("auto",code+" "+userDesignation+" "+maxInTotal );
                boolean[] userSelection = new boolean[freeArray.length];
                for(int i=0; i<freeArray.length&&totalSelected<maxInTotal ;i++){
                    boolean preSlot=true,postSlot=true;
                    if(freeArray[i]>0){
                        if(i-1>=0&&userSelection[i-1]){
                            if(gn[i].equals(gn[i-1])){
                                preSlot=false;
                            }
                        }
                        if(i+1<=freeArray.length&&userSelection[i+1]){
                            if(gn[i].equals(gn[i+1])){
                                postSlot=false;
                            }
                        }
                        if(preSlot&&postSlot){
                            userSelection[i]=true;
                            totalSelected++;
                        }
                    }
                }
                String userSelectionStr = "";
                Log.e("auto select",totalSelected+" "+maxInTotal);
                if(totalSelected==maxInTotal&&maxInTotal>0){
                    for(int i=0; i<userSelection.length;i++) {
                        if(userSelection[i]){
                            freeArray[i]--;
                            formDetails.put((i+1)+",1",freeArray[i]+"");
                            userSelectionStr+=(i+1)+",1!";
                        }
                    }
                    userSelectionStr = userSelectionStr.substring(0,userSelectionStr.length()-1);
                    myRef.child(GlobalVariables.typeForm).child(GlobalVariables.currForm).child("memberActivity").child(code).setValue(userSelectionStr);
                    myRef.child(GlobalVariables.typeForm).child(GlobalVariables.currForm).child("formTableDetails").setValue(formDetails);
                }

            }
        }else{
            Toast.makeText(getApplicationContext(),"Form might have not loaded properly\ntry restarting app",Toast.LENGTH_SHORT).show();
        }
    }

}

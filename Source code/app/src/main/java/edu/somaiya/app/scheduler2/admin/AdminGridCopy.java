//package edu.somaiya.app.scheduler2.admin;
//
//import android.graphics.Point;
//import android.os.Bundle;
//import android.os.Environment;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.view.Display;
//import android.view.View;
//import android.widget.GridLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//import edu.somaiya.app.scheduler2.GlobalVariables;
//import edu.somaiya.app.scheduler2.R;
//
//public class AdminGridCopy extends AppCompatActivity {
//
//    public int gridWidth=-10,gridHeigth=-10,rows,cols;
//    Map<String,Object> formDetails;
//    String rowNames="", colNames="", csvString="", formName="";
//    public DatabaseReference myRef;
//    boolean isFirebaseConnected=false;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_admin_grid);
//
//        // get window size
//        Display display = getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        gridWidth=size.x;
//        gridHeigth=size.y;
//
//
//        // get firebase instance
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        myRef = database.getReference().child(GlobalVariables.typeForm);
//        myRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                // String value = dataSnapshot.getValue(String.class);
//                Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();
//                for (DataSnapshot contact : contactChildren) {
//                    String formId= contact.getKey();
//                    if(formId.equals(GlobalVariables.currForm)){
//                        rows = Integer.parseInt((String)contact.child("totalRows").getValue() );
//                        cols = Integer.parseInt((String)contact.child("totalCols").getValue() );
//                        formDetails = (HashMap)contact.child("formTableDetails").getValue();
//                        rowNames = (String)contact.child("rowNames").getValue();
//                        colNames = (String)contact.child("colNames").getValue();
//                        formName = (String)contact.child("name").getValue();
//                        makeGrid();
//                    }
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Failed to read value
//                Toast.makeText(getApplicationContext(),"cannot connect to database",Toast.LENGTH_SHORT).show();
//            }
//        });
//
//    }
//
//    public void makeGrid(){
//        String[] rn = rowNames.split("!");
//        String[] cn = colNames.split("!");
//
//        csvString="";
//        addButton(0,0,"",true);    csvString+=" ,";
//
//        for(int i=1; i<=cols; i++){
//            addButton(0,i,cn[i-1],true);
//            csvString+=cn[i-1];  if(i<cols){ csvString+=" ,";}
//        }    csvString+="\n";
//        for(int i=1; i<=rows; i++){
//            addButton(i,0,rn[i-1],true);
//        }
//
//        for(int i=1; i<=rows; i++){
//            csvString+=rn[i-1]+",";
//            for(int j=1; j<=cols; j++){
//                String loc = i+","+j;
//                addButton(i,j,(String)formDetails.get(loc), false);
//                csvString+=(String)formDetails.get(loc);    if(j<cols){ csvString+=" ,";}
//            }
//            csvString+="\n";
//        }
//
//    }
//
//    public void addButton(int r, int c,String owner,boolean NamePlates){
//        GridLayout gd = findViewById(R.id.userTableGrid);
//        TextView txt = new TextView(this);
//        txt.setWidth(gridWidth/6);
//        txt.setHeight(gridHeigth/15);
//
//        txt.setTextSize(20);
//        txt.setPadding(0,10,0,10);
//        txt.setBackground(getResources().getDrawable(R.drawable.border));
//        txt.setId( ((r-1)*cols) + (c-1) +1 );
//
//
//        if(!NamePlates) {
//            if (owner.equals("free")) {
//                txt.setTextColor(getResources().getColor(R.color.colorFree));
//            } else {
//                txt.setTextColor(getResources().getColor(R.color.colorAssigned));
//            }
//        }else{
//            txt.setTextColor(getResources().getColor(R.color.colorGridlabels));
//        }
//        txt.setText(owner);
//        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
//        param.height = GridLayout.LayoutParams.WRAP_CONTENT;
//        param.width = GridLayout.LayoutParams.WRAP_CONTENT;
//        param.columnSpec = GridLayout.spec(c);
//        param.rowSpec = GridLayout.spec(r);
//        txt.setLayoutParams(param);
//        gd.addView(txt);
//    }
//
//    public void saveToCSV(View view){
//        Log.e("csv",csvString);
//
//        String sdcardFolder = Environment.getExternalStorageDirectory().getAbsolutePath()+"/formName.csv";
//        FileWriter sb = null;
//        try {
//            sb = new FileWriter(sdcardFolder);
//            sb.append(csvString);
//            sb.close();
//            Toast.makeText(getApplicationContext(),"saved to disk",Toast.LENGTH_LONG).show();
//        } catch (IOException e) {
//            Log.e("AdminGrid.java",e.toString());
//            Toast.makeText(getApplicationContext(),"failed to save",Toast.LENGTH_LONG).show();
//        }
//    }
//
//}

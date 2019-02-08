package edu.somaiya.app.scheduler2.admin;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.somaiya.app.scheduler2.GlobalVariables;
import edu.somaiya.app.scheduler2.R;

public class AdminCreateForm extends AppCompatActivity {
    public DatabaseReference myRef, connectedRef ;
    public ArrayList<Integer> rowIDs, colIDs, slotLimitIDs;
    public int rowCount=0, colCount=0, idCount=0, gridWidth=-10,gridHeigth=-10;
    private int GRID_TEXT_COLOR;
    boolean isFirebaseConnected=false;
    //form attributes
    String formId,formName,rowNames,colNames,formDue,totalRows,totalCols,
            totalSelectionProfessor,totalSelectionAssociate,totalSelectionAssistant,totalSelectionLabAssistant;;
    public static Map<String, Object> formTableDetails, formTableDetailsLab, form;

    // for notification
    public static String CHANNEL_ID="CHANNEL_ID";
    public static String textTitle="Scheduler";
    public static String textContent="Form created";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_form);

        rowIDs=new ArrayList<>(); colIDs=new ArrayList<>(); slotLimitIDs=new ArrayList<>(); isFirebaseConnected=false;
        formId="";rowCount=0; colCount=0; idCount=0; gridWidth=-10;gridHeigth=-10;
        createNotificationChannel();
        firebaseStuff();

        // get window size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        gridWidth=size.x;
        gridHeigth=size.y;
        GRID_TEXT_COLOR = getResources().getColor(R.color.colorAssigned);

        addRow();
        addCol();

        EditText fn = findViewById(R.id.formName);
        fn.setWidth(gridWidth/2);

        TextView fd = findViewById(R.id.formDue);
        fd.setWidth(gridWidth/2);
    }

    public void createForm(View view){
        GlobalVariables.showLoadingImage(this);
        EditText ed = findViewById(R.id.formName);
        formName = ed.getText().toString();

        rowNames="";
        for(int i=0; i<rowIDs.size(); i++){
            ed = findViewById(rowIDs.get(i));
            rowNames += ed.getText().toString();
            if(i!=rowIDs.size()-1)
                rowNames+="!";
        }

        colNames="";
        for(int i=0; i<colIDs.size(); i++){
            ed = findViewById(colIDs.get(i));
            colNames += ed.getText().toString();
            if(i!=colIDs.size()-1)
                colNames+="!";
        }

        TextView tx = findViewById(R.id.formDue);   formDue = tx.getText().toString();

        totalRows=rowCount+"";
        totalCols=colCount+"";

        formTableDetails=new HashMap<>();
        for(int i=1;i<=rowCount;i++){
            for(int j=1;j<=colCount;j++){
                formTableDetails.put(i+","+j,"4");
            }
        }

        formTableDetailsLab=new HashMap<>();
        for(Map.Entry<String,Object> entry:formTableDetails.entrySet()){
            formTableDetailsLab.put((String)entry.getKey(),Integer.parseInt((String)entry.getValue())/2 + "");
        }


        tx = findViewById(R.id.professor);  totalSelectionProfessor = tx.getText().toString();
        tx = findViewById(R.id.Associate);  totalSelectionAssociate = tx.getText().toString();
        tx = findViewById(R.id.Assistant);  totalSelectionAssistant = tx.getText().toString();
        tx = findViewById(R.id.lab);        totalSelectionLabAssistant = tx.getText().toString();

        // add all form components
        form = new HashMap<>();
        form.put("name",formName);
        form.put("due",formDue);
        form.put("rowNames",rowNames);
        form.put("colNames",colNames);
        form.put("totalRows",totalRows);
        form.put("totalCols",totalCols);
        form.put("totalSelectionProfessor",totalSelectionProfessor);
        form.put("totalSelectionAssociate",totalSelectionAssociate);
        form.put("totalSelectionAssistant",totalSelectionAssistant);
        form.put("totalSelectionLabAssistant",totalSelectionLabAssistant);
        form.put("formTableDetails",formTableDetails);
        form.put("formTableDetailsLab",formTableDetailsLab);

        if(isFirebaseConnected&&formId!=null){
            myRef.child("formLive").child(formId).setValue(form);
            myRef.child("globalData").child("totalForms").setValue(formId);
        }else{
            Toast.makeText(getApplicationContext(),"Check your internet connection",Toast.LENGTH_SHORT).show();
            GlobalVariables.stopLoadingImage();
        }
    }

    // selection limit
//    public void srPlus(View view){
//        TextView tx = findViewById(R.id.srow);
//        int count = Integer.parseInt(tx.getText().toString());
//        tx.setText((count+1)+"");
//    }
//    public void scPlus(View view){
//        TextView tx = findViewById(R.id.scol);
//        int count = Integer.parseInt(tx.getText().toString());
//        tx.setText((count+1)+"");
//    }
//    public void srMinus(View view){
//        TextView tx = findViewById(R.id.srow);
//        int count = Integer.parseInt(tx.getText().toString());
//        if(count>1) count--;
//        tx.setText(count+"");
//    }
//    public void scMinus(View view){
//        TextView tx = findViewById(R.id.scol);
//        int count = Integer.parseInt(tx.getText().toString());
//        if(count>1) count--;
//        tx.setText(count+"");
//    }
//    public void tPlus(View view){
//        TextView tx = findViewById(R.id.totalSelection);
//        int count = Integer.parseInt(tx.getText().toString());
//        tx.setText((count+1)+"");
//    }
//    public void tMinus(View view) {
//        TextView tx = findViewById(R.id.totalSelection);
//        int count = Integer.parseInt(tx.getText().toString());
//        if (count > 1) count--;
//        tx.setText(count + "");
//    }

    // num of rows and cols
    public void rPlus(View view)  {
        addRow();
        TextView tx = findViewById(R.id.rowCount);
        int count = Integer.parseInt(tx.getText().toString());
        tx.setText((count+1)+"");
    }
    public void rMinus(View view){
        subRow();
        TextView tx = findViewById(R.id.rowCount);
        int count = Integer.parseInt(tx.getText().toString());
        if (count > 1) count--;
        tx.setText(count + "");
    }
    public void profPlus(View view){
        TextView tx = findViewById(R.id.professor);
        int count = Integer.parseInt(tx.getText().toString());
        tx.setText((count+1)+"");
    }
    public void profMinus(View view){
        TextView tx = findViewById(R.id.professor);
        int count = Integer.parseInt(tx.getText().toString());
        if(count>0) count--;
        tx.setText(count+"");
    }
    public void assoPlus(View view){
        TextView tx = findViewById(R.id.Associate);
        int count = Integer.parseInt(tx.getText().toString());
        tx.setText((count+1)+"");
    }
    public void assoMinus(View view){
        TextView tx = findViewById(R.id.Associate);
        int count = Integer.parseInt(tx.getText().toString());
        if(count>0) count--;
        tx.setText(count+"");
    }
    public void assisPlus(View view){
        TextView tx = findViewById(R.id.Assistant);
        int count = Integer.parseInt(tx.getText().toString());
        tx.setText((count+1)+"");
    }
    public void assisMinus(View view){
        TextView tx = findViewById(R.id.Assistant);
        int count = Integer.parseInt(tx.getText().toString());
        if(count>0) count--;
        tx.setText(count+"");
    }
    public void labPlus(View view){
        TextView tx = findViewById(R.id.lab);
        int count = Integer.parseInt(tx.getText().toString());
        tx.setText((count+1)+"");
    }
    public void labMinus(View view){
        TextView tx = findViewById(R.id.lab);
        int count = Integer.parseInt(tx.getText().toString());
        if(count>0) count--;
        tx.setText(count+"");
    }


    public void subRow(){
        GridLayout gd = findViewById(R.id.createTableGrid);
        if(gd.getRowCount()>2) {
            rowCount--;
            View re = findViewById(rowIDs.get(rowIDs.size() - 1));
            gd.removeView(re);
            rowIDs.remove(rowIDs.size() - 1);

            re = findViewById(slotLimitIDs.get(slotLimitIDs.size() - 1));
            gd.removeView(re);
            slotLimitIDs.remove(slotLimitIDs.size() - 1);
        }
    }
    public void subCol(){
        GridLayout gd = findViewById(R.id.createTableGrid);
        if(gd.getColumnCount()>2) {
            colCount--;
            View re = findViewById(colIDs.get(colIDs.size() - 1));
            gd.removeView(re);
            colIDs.remove(colIDs.size() - 1);
        }
    }
    public void addRow(){
        idCount++; rowCount++;
        GridLayout gd = findViewById(R.id.createTableGrid);
        EditText txt = new EditText(this);
        txt.setWidth(gridWidth/6);
      //  txt.setHeight(gridHeigth/15);
        txt.setTextSize(20);
        txt.setPadding(0,10,0,10);
        txt.setBackground(getResources().getDrawable(R.drawable.border));
        txt.setId(idCount);
        rowIDs.add(idCount);
        txt.setText("R"+rowCount);
        txt.setTextColor(GRID_TEXT_COLOR);

        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.height = GridLayout.LayoutParams.WRAP_CONTENT;
        param.width = GridLayout.LayoutParams.WRAP_CONTENT;
        param.columnSpec = GridLayout.spec(0);
        param.rowSpec = GridLayout.spec(rowCount);
        txt.setLayoutParams(param);
        gd.addView(txt);



        idCount++;
        txt = new EditText(this);
        txt.setWidth(gridWidth/6);
        //  txt.setHeight(gridHeigth/15);
        txt.setTextSize(20);
        txt.setPadding(0,10,0,10);
        txt.setBackground(getResources().getDrawable(R.drawable.border));
        txt.setId(idCount);
        slotLimitIDs.add(idCount);
        txt.setText("0");
        txt.setInputType(InputType.TYPE_CLASS_NUMBER);
        txt.setTextColor(GRID_TEXT_COLOR);

        param = new GridLayout.LayoutParams();
        param.height = GridLayout.LayoutParams.WRAP_CONTENT;
        param.width = GridLayout.LayoutParams.WRAP_CONTENT;
        param.columnSpec = GridLayout.spec(1);
        param.rowSpec = GridLayout.spec(rowCount);
        txt.setLayoutParams(param);
        gd.addView(txt);
    }
    public void addCol(){
        idCount++; colCount++;
        GridLayout gd = findViewById(R.id.createTableGrid);
        EditText txt = new EditText(this);
        txt.setMinWidth(gridWidth/6);
     //   txt.setWidth(gridWidth/6);
        txt.setHeight(gridHeigth/15);
        txt.setTextSize(20);
        txt.setPadding(0,10,0,10);
        txt.setBackground(getResources().getDrawable(R.drawable.border));
        txt.setId(idCount);
        colIDs.add(idCount);
        txt.setText("C"+colCount);
        txt.setTextColor(GRID_TEXT_COLOR);

        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.height = GridLayout.LayoutParams.WRAP_CONTENT;
        param.width = GridLayout.LayoutParams.WRAP_CONTENT;
        param.columnSpec = GridLayout.spec(colCount);
        param.rowSpec = GridLayout.spec(0);
        txt.setLayoutParams(param);
        gd.addView(txt);
    }

    private void firebaseStuff(){
        // check connectivity
        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
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

        // get firebase instance for form
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        myRef.child("globalData").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();
                for (DataSnapshot contact : contactChildren) {
                    if(contact.getKey().equals("totalForms")){
                        String formIdStr =  (String)contact.getValue();
                        if(formIdStr.equals(formId)) {
                            Toast.makeText(getApplicationContext(), "FORM CREATED", Toast.LENGTH_LONG).show();
                            GlobalVariables.stopLoadingImage();
                            raiseNotification();
                            recreate();
                        }
                        int formIdint =Integer.parseInt(formIdStr)+1;
                        formId = formIdint+"";
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(getApplicationContext(),"cannot connect to database",Toast.LENGTH_LONG).show();
                GlobalVariables.stopLoadingImage();
            }
        });
    }

    public void pickDate(View view){
        int mYear, mMonth, mDay, mHour, mMinute;
        final TextView ed = findViewById(R.id.formDue);

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        String s = dayOfMonth + "/" + (monthOfYear + 1) + "/";
                        year = year%100;
                        if(year<10){
                             s=s+"0"+year;
                        }else{
                             s=s+year;
                        }
                        ed.setText(s);
                    }
                }, mYear, mMonth, mDay);

        datePickerDialog.show();
    }

    public void raiseNotification(){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.dove)
                .setContentTitle(textTitle)
                .setContentText(textContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(101, mBuilder.build());
    }

    // for api 26+
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

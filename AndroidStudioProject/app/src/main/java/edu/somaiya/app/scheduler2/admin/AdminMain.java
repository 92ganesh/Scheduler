package edu.somaiya.app.scheduler2.admin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import edu.somaiya.app.scheduler2.R;
import edu.somaiya.app.scheduler2.Welcome;
import edu.somaiya.app.scheduler2.user.UserForms;

public class AdminMain extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);
    }

    @Override
    public void onBackPressed() {
        Intent it = new Intent(getApplicationContext(), Welcome.class);
        startActivity(it);
    }

    public void radioSelect(View view){
        RadioGroup rg = findViewById(R.id.radioGroup);
        int selectedId = rg.getCheckedRadioButtonId();
        RadioButton rd = findViewById(selectedId);
        String selected = rd.getText().toString();

        if(selected.equals("Create Form")){
            Intent i = new Intent(getApplicationContext(), AdminCreateForm.class);
            startActivity(i);
        }else if(selected.equals("View Live forms")){
            Intent i = new Intent(getApplicationContext(), AdminLiveForms.class);
            startActivity(i);
        }else if(selected.equals("View Past forms")){
            Intent i = new Intent(getApplicationContext(), AdminPastForms.class);
            startActivity(i);
        }else if(selected.equals("Approve Member")){
            Intent i = new Intent(getApplicationContext(), MemberApproval.class);
            startActivity(i);
        }else if(selected.equals("View/Remove Member")){
            Intent i = new Intent(getApplicationContext(), MemberList.class);
            startActivity(i);
        }else if(selected.equals("Add Admin")){
            Intent i = new Intent(getApplicationContext(), AddAdmin.class);
            startActivity(i);
        }else if(selected.equals("View/Remove Admin")){
            Intent i = new Intent(getApplicationContext(), RemoveAdmin.class);
            startActivity(i);
        }

    }
}

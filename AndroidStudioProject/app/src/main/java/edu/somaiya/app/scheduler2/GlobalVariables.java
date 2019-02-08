package edu.somaiya.app.scheduler2;

import android.app.ProgressDialog;
import android.content.Context;

public class GlobalVariables {
    public static String professor="professor";
    public static String associate="associate";
    public static String assistant="assistant";
    public static String labAssistant="labAssistant";
    public static String currForm="2";
    public static String typeForm="formLive";
    public static String currUser="dummyUser";
    public static String userDesignation ="professor";
    public static int minSelection=3;
    public static ProgressDialog dialog;
    public static boolean loading=false;
    public static boolean onEmulator=true;
    public static long serverTimeOffset=0;


    public static void showLoadingImage(Context ctx){
        dialog=new ProgressDialog(ctx);
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        loading = true;
        dialog.show();


        //dialog.dismiss();
    }

    public static void stopLoadingImage() {
        if(loading){
            dialog.hide();   loading = false;
        }
    }


    }

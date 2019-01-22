package com.julioflores.prueba;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class Dialogo extends AppCompatActivity {

    AsyncHttpClient cliente;
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder build = new AlertDialog.Builder(Dialogo.this);
        build.setTitle("Supervisor");

        build.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Obteneractual();
            }
        });
        build.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return build.create();
    }
    private void Obteneractual(){
        //String getArgument = getArguments().getString("iden");
        Toast.makeText(Dialogo.this, "Hola", Toast.LENGTH_SHORT).show();
//        String asas = "Hola";
//        String url = "https://appsionmovil.000webhostapp.com/asignar_pedidoenvasar_nombre.php?PersonaAsignada="+ "nbjhb" + "&ID="+ getArgument;
//        cliente.post(url, new AsyncHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                if(statusCode == 200){
//
//                }
//            }
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//            }
//        });
    }
}

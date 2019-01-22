package com.julioflores.prueba;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.loopj.android.http.*;
import org.json.JSONArray;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity{
    SwipeRefreshLayout swipeRefreshLayout;
    ListAdapter adapter;
    SwipeMenuListView lvlista1;
    AsyncHttpClient cliente;
    private AdaptadorEnvase adaptadores;
    int acceso;
    Calendar calendariocompleto;
    android.os.Handler customHandler;

    public class contar extends CountDownTimer{
        public contar(long milienfuturo, long countdowninterval){
            super(milienfuturo,countdowninterval);
        }
        @Override
        public void onTick(long millisUntilFinished) { }
        public void onFinish(){
            if (acceso == 1) {
                ObtenerEnvases();
            }//Supervisor
            if (acceso == 2) {
                ObtenerEnvases2();
            }//Envasador
            if (acceso == 3) {
                ObtenerEnvases3();
            }
            //Toast.makeText(MainActivity.this, "Actualizado",Toast.LENGTH_SHORT).show();
        }
    }
    private Runnable actualizartimer = new Runnable() {
        @Override
        public void run() {
            contar tiempo = new contar(25000, 25000);
            tiempo.start();
            customHandler.postDelayed(this, 25000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        customHandler = new android.os.Handler();
        customHandler.postDelayed(actualizartimer, 0);
        lvlista1 = (SwipeMenuListView) findViewById(R.id.lista1);
        cliente = new AsyncHttpClient();

        //Coordinador 1, supervisor 2, envasador 3
        acceso=3;

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshlayout);
        ConnectivityManager conectividad = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo lanet = conectividad.getActiveNetworkInfo();
        if(lanet != null && lanet.isConnected()){
            if (acceso == 1) {
                ObtenerEnvases();
            }//Supervisor
            if (acceso == 2) {
                ObtenerEnvases2();
            }//Envasador
            if (acceso == 3) {
                ObtenerEnvases3();
            }
        }else{
            lvlista1.setVisibility(View.INVISIBLE);
            Intent intentar = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intentar);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ConnectivityManager conectividad = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo lanet = conectividad.getActiveNetworkInfo();
                if(lanet != null && lanet.isConnected()){
                    if (acceso == 1) {
                        ObtenerEnvases();
                    }//Supervisor
                    if (acceso == 2) {
                        ObtenerEnvases2();
                    }//Envasador
                    if (acceso == 3) {
                        ObtenerEnvases3();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }else{
                    Intent intentar = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intentar);
                    Toast.makeText(MainActivity.this, "No hay Internet, intentarlo más tarde o verifica su conexión",Toast.LENGTH_SHORT).show();
                    lvlista1.setVisibility(View.INVISIBLE);
                }
            }
        });
        //Coordinador
        if (acceso == 1) {
            ObtenerEnvases();
        }//Supervisor
        if (acceso == 2) {
            ObtenerEnvases2();
        }//Envasador
        if (acceso == 3) {
            ObtenerEnvases3();
        }
    }
    private void ObtenerEnvases(){
        String url = "https://appsionmovil.000webhostapp.com/consultar_envase.php";
        cliente.post(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(statusCode == 200){
                    listarEnvases(new String(responseBody));
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            }
        });
    }
    private void ObtenerEnvases2(){
        String url = "https://appsionmovil.000webhostapp.com/consultar_envase_nombre.php";
        cliente.post(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(statusCode == 200){
                    listarEnvases2(new String(responseBody));
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }
    private void ObtenerEnvases3(){
        String url = "https://appsionmovil.000webhostapp.com/consultar_envase_envasador.php";
        cliente.post(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(statusCode == 200){
                    listarEnvases3(new String(responseBody));
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }
    public void listarEnvases(String respuesta){
        final ArrayList<Envases> lista = new ArrayList<Envases>();

        try{
            final JSONArray jsonarreglo = new JSONArray(respuesta);
            for (int i=0; i<jsonarreglo.length(); i++){
                Envases t = new Envases();
                t.setIds(jsonarreglo.getJSONObject(i).getInt("ID"));
                t.setNopedidos(jsonarreglo.getJSONObject(i).getInt("NoPedidos"));
                t.setProductos(jsonarreglo.getJSONObject(i).getString("Producto"));
                t.setCantidades(jsonarreglo.getJSONObject(i).getInt("Cantidad"));
                t.setEtapa1s(jsonarreglo.getJSONObject(i).getString("Etapa1"));
                t.setFechaCapturas(jsonarreglo.getJSONObject(i).getString("FechaCaptura"));
                t.setFechaaprobaciones(jsonarreglo.getJSONObject(i).getString("FechaAprobacion"));
                t.setFechaasignadas(jsonarreglo.getJSONObject(i).getString("FechaAsignacion"));
                t.setFechaenvases(jsonarreglo.getJSONObject(i).getString("FechaEnvase"));
                t.setPersonaasignadas(jsonarreglo.getJSONObject(i).getString("PersonaAsignada"));
                t.setTipoenvases(jsonarreglo.getJSONObject(i).getString("TipoEnvase"));
                lista.add(t);
            }
            adaptadores = new AdaptadorEnvase(this, lista);
            //ArrayAdapter<Envases> a = new ArrayAdapter<Envases>(this,android.R.layout.simple_list_item_1, lista);
            lvlista1.setAdapter(adaptadores);

            lvlista1.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                public void onItemClick(AdapterView<?> parent, View view, int position, long id){

                }
            });

            SwipeMenuCreator creator = new SwipeMenuCreator() {
                @Override
                public void create(SwipeMenu menu) {
                    // create "open" item
                    SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
                    // set item background
                    openItem.setBackground(new ColorDrawable(Color.rgb(176,196,222)));
                    // set item width
                    openItem.setWidth(170);
                    // set item title
                    openItem.setTitle("Proceso");
                    // set item title fontsize
                    openItem.setTitleSize(18);
                    // set item title font color
                    openItem.setTitleColor(Color.WHITE);
                    // add to menu
                    menu.addMenuItem(openItem);
                    SwipeMenuItem openItem2 = new SwipeMenuItem(getApplicationContext());
                    // set item background
                    openItem2.setBackground(new ColorDrawable(Color.rgb(178,34,34)));
                    // set item width
                    openItem2.setWidth(170);
                    // set item title
                    openItem2.setTitle("Problema");
                    // set item title fontsize
                    openItem2.setTitleSize(18);
                    // set item title font color
                    openItem2.setTitleColor(Color.WHITE);
                    // add to menu
                    menu.addMenuItem(openItem2);
                }
            };
            lvlista1.setMenuCreator(creator);
            lvlista1.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                    final Envases estePedido = (Envases) lvlista1.getItemAtPosition(position);
                    String id1 = String.valueOf(estePedido.getIds());
                    String fase1 = "En Proceso";
                    switch (index) {
                        case 0:
                            String url = "https://appsionmovil.000webhostapp.com/asignar_envase.php?Etapa1="+
                                    fase1.replaceAll(" ","%20") +"&ID="+ id1;
                            Toast.makeText(MainActivity.this, "En Proceso",Toast.LENGTH_SHORT).show();
                            cliente.post(url, new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    if(statusCode == 200){
                                    }
                                }
                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                                }
                            });
                            break;
                        case 1:
                            String url2 = "https://appsionmovil.000webhostapp.com/asignar_envase.php?Etapa1=Problema&ID="+ id1;
                            Toast.makeText(MainActivity.this, "Problema",Toast.LENGTH_SHORT).show();
                            cliente.post(url2, new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    if(statusCode == 200){
                                    }
                                }
                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                }
                            });
                            break;
                    }
                    // false : close the menu; true : not close the menu
                    return false;
                }
            });
        }catch (Exception e1){
            e1.printStackTrace();
        }
    }
    public void listarEnvases2(String respuesta){
        final ArrayList<Envases> lista = new ArrayList<Envases>();

        try{
            final JSONArray jsonarreglo = new JSONArray(respuesta);
            for (int i=0; i<jsonarreglo.length(); i++){
                Envases t = new Envases();
                t.setIds(jsonarreglo.getJSONObject(i).getInt("ID"));
                t.setNopedidos(jsonarreglo.getJSONObject(i).getInt("NoPedidos"));
                t.setProductos(jsonarreglo.getJSONObject(i).getString("Producto"));
                t.setCantidades(jsonarreglo.getJSONObject(i).getInt("Cantidad"));
                t.setEtapa1s(jsonarreglo.getJSONObject(i).getString("Etapa1"));
                t.setFechaCapturas(jsonarreglo.getJSONObject(i).getString("FechaCaptura"));
                t.setFechaaprobaciones(jsonarreglo.getJSONObject(i).getString("FechaAprobacion"));
                t.setFechaasignadas(jsonarreglo.getJSONObject(i).getString("FechaAsignacion"));
                t.setFechaenvases(jsonarreglo.getJSONObject(i).getString("FechaEnvase"));
                t.setPersonaasignadas(jsonarreglo.getJSONObject(i).getString("PersonaAsignada"));
                t.setTipoenvases(jsonarreglo.getJSONObject(i).getString("TipoEnvase"));
                lista.add(t);
            }
            adaptadores = new AdaptadorEnvase(this, lista);
            //ArrayAdapter<Envases> a = new ArrayAdapter<Envases>(this,android.R.layout.simple_list_item_1, lista);
            lvlista1.setAdapter(adaptadores);

            lvlista1.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                }
            });

            SwipeMenuCreator creator = new SwipeMenuCreator() {
                @Override
                public void create(SwipeMenu menu) {
                    SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
                    openItem.setBackground(new ColorDrawable(Color.rgb(100,149,237)));
                    openItem.setWidth(170);
                    openItem.setTitle("Asignar");
                    openItem.setTitleSize(18);
                    openItem.setTitleColor(Color.WHITE);
                    menu.addMenuItem(openItem);
                    SwipeMenuItem openItem2 = new SwipeMenuItem(getApplicationContext());
                    openItem2.setBackground(new ColorDrawable(Color.rgb(178,34,34)));
                    openItem2.setWidth(170);
                    openItem2.setTitle("Problema");
                    openItem2.setTitleSize(18);
                    openItem2.setTitleColor(Color.WHITE);
                    menu.addMenuItem(openItem2);
                }
            };
            lvlista1.setMenuCreator(creator);
            lvlista1.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                    switch (index) {
                        case 0:
                            final Envases estePedido = (Envases) lvlista1.getItemAtPosition(position);
                            final String ide = String.valueOf(estePedido.getIds());
                            AlertDialog.Builder mibuild = new AlertDialog.Builder(MainActivity.this);
                            View mview = getLayoutInflater().inflate(R.layout.spinnerdialog, null);
                            mibuild.setTitle("Supervisor");
                            mibuild.setMessage("Escoje Nombre");
                            final Spinner mmispin = (Spinner) mview.findViewById(R.id.mispin);
                            ArrayAdapter<String> nomb = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.Nombres));
                            nomb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            mibuild.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String url = "https://appsionmovil.000webhostapp.com/asignar_pedidoenvasar_nombre.php?PersonaAsignada="+
                                            mmispin.getSelectedItem().toString().replaceAll(" ","%20") +
                                            "&ID="+ ide;
                                    Toast.makeText(MainActivity.this, ide,Toast.LENGTH_SHORT).show();
                                    cliente.post(url, new AsyncHttpResponseHandler() {
                                        @Override
                                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                            if(statusCode == 200){
                                            }
                                        }
                                        @Override
                                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                                        }
                                    });
                                }
                            });
                            mibuild.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            mibuild.setView(mview);
                            AlertDialog dialog = mibuild.create();
                            dialog.show();
                            break;
                        case 1:
                            final Envases estePedido1 = (Envases) lvlista1.getItemAtPosition(position);
                            final String id2 = String.valueOf(estePedido1.getIds());
                            String url2 = "https://appsionmovil.000webhostapp.com/asignar_envase.php?Etapa1=Problema&ID="+ id2;
                            Toast.makeText(MainActivity.this, "Problema",Toast.LENGTH_SHORT).show();
                            cliente.post(url2, new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    if(statusCode == 200){
                                    }
                                }
                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                }
                            });
                            break;
                    }
                    return false;
                }
            });
        }catch (Exception e1){
            e1.printStackTrace();
        }
    }
    public void listarEnvases3(String respuesta){
        final ArrayList<Envases> lista = new ArrayList<Envases>();

        try{
            final JSONArray jsonarreglo = new JSONArray(respuesta);
            for (int i=0; i<jsonarreglo.length(); i++){
                Envases t = new Envases();
                t.setIds(jsonarreglo.getJSONObject(i).getInt("ID"));
                t.setNopedidos(jsonarreglo.getJSONObject(i).getInt("NoPedidos"));
                t.setProductos(jsonarreglo.getJSONObject(i).getString("Producto"));
                t.setCantidades(jsonarreglo.getJSONObject(i).getInt("Cantidad"));
                t.setEtapa1s(jsonarreglo.getJSONObject(i).getString("Etapa1"));
                t.setFechaCapturas(jsonarreglo.getJSONObject(i).getString("FechaCaptura"));
                t.setFechaaprobaciones(jsonarreglo.getJSONObject(i).getString("FechaAprobacion"));
                t.setFechaasignadas(jsonarreglo.getJSONObject(i).getString("FechaAsignacion"));
                t.setFechaenvases(jsonarreglo.getJSONObject(i).getString("FechaEnvase"));
                t.setPersonaasignadas(jsonarreglo.getJSONObject(i).getString("PersonaAsignada"));
                t.setTipoenvases(jsonarreglo.getJSONObject(i).getString("TipoEnvase"));
                lista.add(t);
            }
            adaptadores = new AdaptadorEnvase(this, lista);
            //ArrayAdapter<Envases> a = new ArrayAdapter<Envases>(this,android.R.layout.simple_list_item_1, lista);
            lvlista1.setAdapter(adaptadores);

            lvlista1.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                public void onItemClick(AdapterView<?> parent, View view, int position, long id){

                }
            });

            SwipeMenuCreator creator = new SwipeMenuCreator() {
                @Override
                public void create(SwipeMenu menu) {
                    // create "open" item
                    SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
                    // set item background
                    openItem.setBackground(new ColorDrawable(Color.rgb(50,205,50)));
                    // set item width
                    openItem.setWidth(200);
                    // set item title
                    openItem.setTitle("Terminado");
                    // set item title fontsize
                    openItem.setTitleSize(18);
                    // set item title font color
                    openItem.setTitleColor(Color.WHITE);
                    // add to menu
                    menu.addMenuItem(openItem);
                    SwipeMenuItem openItem2 = new SwipeMenuItem(getApplicationContext());
                    // set item background
                    openItem2.setBackground(new ColorDrawable(Color.rgb(178,34,34)));
                    // set item width
                    openItem2.setWidth(170);
                    // set item title
                    openItem2.setTitle("Problema");
                    // set item title fontsize
                    openItem2.setTitleSize(18);
                    // set item title font color
                    openItem2.setTitleColor(Color.WHITE);
                    // add to menu
                    menu.addMenuItem(openItem2);
                }
            };
            lvlista1.setMenuCreator(creator);
            lvlista1.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                    Date fechahora = calendariocompleto.getInstance().getTime();
                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                    String dias = dateFormat.format(fechahora);
                    final Envases estePedido = (Envases) lvlista1.getItemAtPosition(position);
                    String ids = String.valueOf(estePedido.getIds());
                    String fase = "Terminado";
                    switch (index) {
                        case 0:
                            String url = "https://appsionmovil.000webhostapp.com/asignar_pedidoenvasar.php?FechaAprobacion="+ dias.replaceAll(" ","%20") +
                                    "&Etapa1="+ fase +"&ID="+ ids;
                            Toast.makeText(MainActivity.this, fase,Toast.LENGTH_SHORT).show();
                            cliente.post(url, new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    if(statusCode == 200){
                                    }
                                }
                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                                }
                            });
                            break;
                        case 1:
                            String url2 = "https://appsionmovil.000webhostapp.com/asignar_pedidoenvasar.php?FechaAprobacion="+ dias.replaceAll(" ","%20") +
                                    "&Etapa1=Problema&ID="+ ids;
                            Toast.makeText(MainActivity.this, "Problema",Toast.LENGTH_SHORT).show();
                            cliente.post(url2, new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    if(statusCode == 200){
                                    }
                                }
                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                }
                            });
                            break;
                    }
                    // false : close the menu; true : not close the menu
                    return false;
                }
            });
        }catch (Exception e1){
            e1.printStackTrace();
        }
    }
}
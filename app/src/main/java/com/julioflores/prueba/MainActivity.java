package com.julioflores.prueba;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
    SwipeMenuListView lvlista1;
    AsyncHttpClient cliente;
    private AdaptadorEnvase adaptadores;
    int acceso;
    Calendar calendariocompleto;
    Handler customHandler = new Handler();

    public class contar extends CountDownTimer{
        public contar(long milienfuturo, long countdowninterval){
            super(milienfuturo,countdowninterval);
        }
        @Override
        public void onTick(long millisUntilFinished) { }
        public void onFinish(){
            if (acceso == 1) { ObtenerEnvases(); }
            //Supervisor
            if (acceso == 2) { ObtenerEnvases2(); }
            //Envasador
            if (acceso == 3) { ObtenerEnvases3(); }
           //Toast.makeText(MainActivity.this, "Actualizado",Toast.LENGTH_SHORT).show();
        }
    }
    private Runnable actualizartimer = new Runnable() {
        @Override
        public void run() {
            contar tiempo = new contar(45000, 45000);
            tiempo.start();
            customHandler.postDelayed(this, 45000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvlista1 = (SwipeMenuListView) findViewById(R.id.lista1);
        cliente = new AsyncHttpClient();
        customHandler.postDelayed(actualizartimer, 0);
        //Coordinador 1, supervisor 2, envasador 3
        acceso=2;

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
                        lvlista1.setVisibility(View.VISIBLE);
                    }//Supervisor
                    if (acceso == 2) {
                        ObtenerEnvases2();
                        lvlista1.setVisibility(View.VISIBLE);
                    }//Envasador
                    if (acceso == 3) {
                        ObtenerEnvases3();
                        lvlista1.setVisibility(View.VISIBLE);
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }else{
                    Toast.makeText(MainActivity.this, "No hay Internet, intentarlo más tarde o verifica su conexión",Toast.LENGTH_SHORT).show();
                    lvlista1.setVisibility(View.INVISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
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
        String url = "https://appsionmovil.000webhostapp.com/consultar_envase_envasador.php?Persona=Silver";
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
                t.setLote(jsonarreglo.getJSONObject(i).getInt("Lote"));
                t.setCantidades2(jsonarreglo.getJSONObject(i).getInt("Cantidad2"));
                t.setLote2(jsonarreglo.getJSONObject(i).getInt("Lote2"));
                t.setCantidades3(jsonarreglo.getJSONObject(i).getInt("Cantidad3"));
                t.setLote3(jsonarreglo.getJSONObject(i).getInt("Lote3"));
                lista.add(t);
            }
            adaptadores = new AdaptadorEnvase(this, lista);
            //ArrayAdapter<Envases> a = new ArrayAdapter<Envases>(this,android.R.layout.simple_list_item_1, lista);
            lvlista1.setAdapter(adaptadores);
            lvlista1.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                    customHandler.removeCallbacks(actualizartimer);
                    customHandler.postDelayed(actualizartimer, 90000);
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
                    ConnectivityManager conectividad = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo lanet = conectividad.getActiveNetworkInfo();
                    switch (index) {
                        case 0:
                            if(lanet != null && lanet.isConnected()){
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
                            }else{
                                Toast.makeText(MainActivity.this, "No hay Internet, intentarlo más tarde o verifica su conexión",Toast.LENGTH_SHORT).show();
                                lvlista1.setVisibility(View.INVISIBLE);
                            }
                            break;
                        case 1:
                            if(lanet != null && lanet.isConnected()){
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
                            }else{
                                Toast.makeText(MainActivity.this, "No hay Internet, intentarlo más tarde o verifica su conexión",Toast.LENGTH_SHORT).show();
                                lvlista1.setVisibility(View.INVISIBLE);
                            }
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
                t.setLote(jsonarreglo.getJSONObject(i).getInt("Lote"));
                t.setCantidades2(jsonarreglo.getJSONObject(i).getInt("Cantidad2"));
                t.setLote2(jsonarreglo.getJSONObject(i).getInt("Lote2"));
                t.setCantidades3(jsonarreglo.getJSONObject(i).getInt("Cantidad3"));
                t.setLote3(jsonarreglo.getJSONObject(i).getInt("Lote3"));
                lista.add(t);
            }
            adaptadores = new AdaptadorEnvase(this, lista);
            //ArrayAdapter<Envases> a = new ArrayAdapter<Envases>(this,android.R.layout.simple_list_item_1, lista);
            lvlista1.setAdapter(adaptadores);

            lvlista1.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                    customHandler.removeCallbacks(actualizartimer);
                    customHandler.postDelayed(actualizartimer, 90000);
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
                    ConnectivityManager conectividad = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo lanet = conectividad.getActiveNetworkInfo();
                    switch (index) {
                        case 0:
                            if(lanet != null && lanet.isConnected()){
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
                                            ConnectivityManager conectividad1 = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                                            NetworkInfo lanet1 = conectividad1.getActiveNetworkInfo();
                                            if(lanet1 != null && lanet1.isConnected()){
                                            String url = "https://appsionmovil.000webhostapp.com/asignar_pedidoenvasar_nombre.php?PersonaAsignada="+
                                                    mmispin.getSelectedItem().toString().replaceAll(" ","%20") +
                                                    "&ID="+ ide;
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
                                        }else{
                                                Toast.makeText(MainActivity.this, "No hay Internet, intentarlo más tarde o verifica su conexión",Toast.LENGTH_SHORT).show();
                                                lvlista1.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });
                                    mibuild.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ConnectivityManager conectividad1 = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                                            NetworkInfo lanet1 = conectividad1.getActiveNetworkInfo();
                                            if(lanet1 != null && lanet1.isConnected()) {
                                                dialog.cancel();
                                            }else{
                                                Toast.makeText(MainActivity.this, "No hay Internet, intentarlo más tarde o verifica su conexión",Toast.LENGTH_SHORT).show();
                                                lvlista1.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });
                                mibuild.setView(mview);
                                AlertDialog dialog = mibuild.create();
                                dialog.show();
                            }else{
                                Toast.makeText(MainActivity.this, "No hay Internet, intentarlo más tarde o verifica su conexión",Toast.LENGTH_SHORT).show();
                                lvlista1.setVisibility(View.INVISIBLE);
                            }
                            break;
                        case 1:
                            if(lanet != null && lanet.isConnected()){
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
                            }else{
                                Toast.makeText(MainActivity.this, "No hay Internet, intentarlo más tarde o verifica su conexión",Toast.LENGTH_SHORT).show();
                                lvlista1.setVisibility(View.INVISIBLE);
                            }
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
                t.setLote(jsonarreglo.getJSONObject(i).getInt("Lote"));
                t.setCantidades2(jsonarreglo.getJSONObject(i).getInt("Cantidad2"));
                t.setLote2(jsonarreglo.getJSONObject(i).getInt("Lote2"));
                t.setCantidades3(jsonarreglo.getJSONObject(i).getInt("Cantidad3"));
                t.setLote3(jsonarreglo.getJSONObject(i).getInt("Lote3"));
                lista.add(t);
            }
            adaptadores = new AdaptadorEnvase(this, lista);
            //ArrayAdapter<Envases> a = new ArrayAdapter<Envases>(this,android.R.layout.simple_list_item_1, lista);
            lvlista1.setAdapter(adaptadores);
            lvlista1.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                    customHandler.removeCallbacks(actualizartimer);
                    customHandler.postDelayed(actualizartimer, 60000);
                }
            });
            SwipeMenuCreator creator = new SwipeMenuCreator() {
                @Override
                public void create(SwipeMenu menu) {
                    SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
                    openItem.setBackground(new ColorDrawable(Color.rgb(50,205,50)));
                    openItem.setWidth(200);
                    openItem.setTitle("Terminado");
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
                    Date fechahora = calendariocompleto.getInstance().getTime();
                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                    final String dias = dateFormat.format(fechahora);
                    final Envases esteenvase = (Envases) lvlista1.getItemAtPosition(position);
                    final String ids = String.valueOf(esteenvase.getIds());
                    ConnectivityManager conectividad = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo lanet = conectividad.getActiveNetworkInfo();
                    switch (index) {
                        case 0:
                            if(lanet != null && lanet.isConnected()){
                                String cant = String.valueOf(esteenvase.getCantidades());
                                String cant2 = String.valueOf(esteenvase.getCantidades2());
                                String cant3 = String.valueOf(esteenvase.getCantidades3());
                                String lots = String.valueOf(esteenvase.getLote());
                                String lots2 = String.valueOf(esteenvase.getLote2());
                                String lots3 = String.valueOf(esteenvase.getLote3());
                                final int c11 = Integer.parseInt(cant);
                                final int l11 = Integer.parseInt(lots);
                                final int c22 = Integer.parseInt(cant2);
                                final int l22 = Integer.parseInt(lots2);
                                int c33 = Integer.parseInt(cant3);
                                int l33 = Integer.parseInt(lots3);
                                if(c11 == 0 || l11 == 0) {
                                    AlertDialog.Builder mibuild1 = new AlertDialog.Builder(MainActivity.this);
                                    View mview1 = getLayoutInflater().inflate(R.layout.terminado_dialogo, null);
                                    mibuild1.setTitle("Terminado");
                                    final EditText t1 = (EditText) mview1.findViewById(R.id.cantis);
                                    t1.setText(cant);
                                    final EditText t2 = (EditText) mview1.findViewById(R.id.lotes);
                                    t2.setText(lots);
                                    final AlertDialog.Builder builder1 = mibuild1.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ConnectivityManager conectividad1 = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                                            NetworkInfo lanet1 = conectividad1.getActiveNetworkInfo();
                                            if(lanet1 != null && lanet1.isConnected()){
                                                String vacio = "";
                                                String lt1 = t1.getText().toString();
                                                t1.setText(lt1);
                                                String rlots = t2.getText().toString();
                                                t2.setText(rlots);
                                                int cant1 = Integer.parseInt(lt1);
                                                int lote1 = Integer.parseInt(rlots);
                                                if(cant1 != 0 && lote1 != 0){
                                                    String url = "https://appsionmovil.000webhostapp.com/asignar_pedidoenvasar.php?FechaAprobacion=" + dias.replaceAll(" ", "%20") +
                                                            "&Etapa1=Terminado&Cantidad=" + lt1 +
                                                            "&DetalleProblema=" + vacio.replaceAll("", "%20") +
                                                            "&Lote=" + rlots + "&Cantidad2=0&Lote2=0&Cantidad3=0&Lote3=0" +
                                                            "&ID=" + ids;
                                                    Toast.makeText(MainActivity.this, "Terminado", Toast.LENGTH_SHORT).show();
                                                    cliente.post(url, new AsyncHttpResponseHandler() {
                                                        @Override
                                                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) { if (statusCode == 200) { } }
                                                        @Override
                                                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) { }
                                                    });
                                                }else{
                                                    Toast.makeText(MainActivity.this, "Ingrese un valor mayor que 0",Toast.LENGTH_SHORT).show();
                                                }
                                            }else{
                                                Toast.makeText(MainActivity.this, "No hay Internet, intentarlo más tarde o verifica su conexión",Toast.LENGTH_SHORT).show();
                                                lvlista1.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });
                                    mibuild1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ConnectivityManager conectividad1 = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                                            NetworkInfo lanet1 = conectividad1.getActiveNetworkInfo();
                                            if(lanet1 != null && lanet1.isConnected()) {
                                                dialog.cancel();
                                            }else{
                                                Toast.makeText(MainActivity.this, "No hay Internet, intentarlo más tarde o verifica su conexión",Toast.LENGTH_SHORT).show();
                                                lvlista1.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });
                                    mibuild1.setView(mview1);
                                    AlertDialog dialog = mibuild1.create();
                                    dialog.show();
                                } else if (c22 == 0 || l22 == 0){
                                    AlertDialog.Builder mibuild1 = new AlertDialog.Builder(MainActivity.this);
                                    View mview1 = getLayoutInflater().inflate(R.layout.terminado_dialogo, null);
                                    mibuild1.setTitle("Terminado");
                                    final EditText t1 = (EditText) mview1.findViewById(R.id.cantis);
                                    t1.setText(cant);
                                    final EditText t2 = (EditText) mview1.findViewById(R.id.lotes);
                                    t2.setText(lots);
                                    final AlertDialog.Builder builder1 = mibuild1.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ConnectivityManager conectividad1 = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                                            NetworkInfo lanet1 = conectividad1.getActiveNetworkInfo();
                                            if(lanet1 != null && lanet1.isConnected()){
                                                String vacio = "";
                                                String lt2 = t1.getText().toString();
                                                t1.setText(lt2);
                                                String rlots2 = t2.getText().toString();
                                                t2.setText(rlots2);
                                                int cant1 = Integer.parseInt(lt2);
                                                int lote1 = Integer.parseInt(rlots2);
                                                if(cant1 != 0 && lote1 != 0){
                                                String url = "https://appsionmovil.000webhostapp.com/asignar_pedidoenvasar.php?FechaAprobacion=" + dias.replaceAll(" ", "%20") +
                                                        "&Etapa1=Terminado&Cantidad="+ c11 +
                                                        "&DetalleProblema=" + vacio.replaceAll("","%20") +
                                                        "&Lote=" + l11 + "&Cantidad2="+ lt2 + "&Lote2="+ rlots2 +
                                                        "&Cantidad3=0&Lote3=0"+
                                                        "&ID=" + ids;
                                                Toast.makeText(MainActivity.this, "Terminado", Toast.LENGTH_SHORT).show();
                                                cliente.post(url, new AsyncHttpResponseHandler() {
                                                    @Override
                                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) { if (statusCode == 200) { } }
                                                    @Override
                                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) { }
                                                });
                                                }else{
                                                    Toast.makeText(MainActivity.this, "Ingrese un valor mayor que 0",Toast.LENGTH_SHORT).show();
                                                }
                                            }else{
                                                Toast.makeText(MainActivity.this, "No hay Internet, intentarlo más tarde o verifica su conexión",Toast.LENGTH_SHORT).show();
                                                lvlista1.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });
                                    mibuild1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    mibuild1.setView(mview1);
                                    AlertDialog dialog = mibuild1.create();
                                    dialog.show();
                                }else if (c33 == 0 || l33 == 0){
                                    AlertDialog.Builder mibuild1 = new AlertDialog.Builder(MainActivity.this);
                                    View mview1 = getLayoutInflater().inflate(R.layout.terminado_dialogo, null);
                                    mibuild1.setTitle("Terminado");
                                    final EditText t1 = (EditText) mview1.findViewById(R.id.cantis);
                                    t1.setText(cant2);
                                    final EditText t2 = (EditText) mview1.findViewById(R.id.lotes);
                                    t2.setText(lots2);
                                    final AlertDialog.Builder builder1 = mibuild1.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ConnectivityManager conectividad1 = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                                            NetworkInfo lanet1 = conectividad1.getActiveNetworkInfo();
                                            if(lanet1 != null && lanet1.isConnected()){
                                                String vacio = "";
                                                String lt3 = t1.getText().toString();
                                                t1.setText(lt3);
                                                String rlots3 = t2.getText().toString();
                                                t2.setText(rlots3);
                                                int cant1 = Integer.parseInt(lt3);
                                                int lote1 = Integer.parseInt(rlots3);
                                                if(cant1 != 0 && lote1 != 0){
                                                String url = "https://appsionmovil.000webhostapp.com/asignar_pedidoenvasar.php?FechaAprobacion=" + dias.replaceAll(" ", "%20") +
                                                        "&Etapa1=Terminado&Cantidad="+ c11 + "&DetalleProblema=" + vacio.replaceAll("","%20") +
                                                        "&Lote=" + l11 + "&Cantidad2="+ c22 + "&Lote2="+ l22 + "&Cantidad3="+ lt3 +"&Lote3="+ rlots3 +
                                                        "&ID=" + ids;
                                                Toast.makeText(MainActivity.this, "Terminado", Toast.LENGTH_SHORT).show();
                                                cliente.post(url, new AsyncHttpResponseHandler() {
                                                    @Override
                                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) { if (statusCode == 200) { } }
                                                    @Override
                                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) { }
                                                });
                                                }else{
                                                    Toast.makeText(MainActivity.this, "Ingrese un valor mayor que 0",Toast.LENGTH_SHORT).show();
                                                }
                                            }else{
                                                Toast.makeText(MainActivity.this, "No hay Internet, intentarlo más tarde o verifica su conexión",Toast.LENGTH_SHORT).show();
                                                lvlista1.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });
                                    mibuild1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ConnectivityManager conectividad1 = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                                            NetworkInfo lanet1 = conectividad1.getActiveNetworkInfo();
                                            if(lanet1 != null && lanet1.isConnected()){
                                            dialog.cancel();
                                            }else{
                                                Toast.makeText(MainActivity.this, "No hay Internet, intentarlo más tarde o verifica su conexión",Toast.LENGTH_SHORT).show();
                                                lvlista1.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });
                                    mibuild1.setView(mview1);
                                    AlertDialog dialog = mibuild1.create();
                                    dialog.show();
                                }else if(c33 != 0 && l33 != 0 && c33 != 0 && l33 != 0 && c33 != 0 && l33 != 0){
                                    Toast.makeText(MainActivity.this, "Ha llego al limite de Intentos de Cantidad/Lote", Toast.LENGTH_SHORT).show();
                                } else { }
                            }else{
                                Toast.makeText(MainActivity.this, "No hay Internet, intentarlo más tarde o verifica su conexión",Toast.LENGTH_SHORT).show();
                                lvlista1.setVisibility(View.INVISIBLE);
                            }
                            break;
                        case 1:
                            if(lanet != null && lanet.isConnected()){
                                final String cant1 = String.valueOf(esteenvase.getCantidades());
                                final String cant21 = String.valueOf(esteenvase.getCantidades2());
                                final String cant31 = String.valueOf(esteenvase.getCantidades3());
                                final String lots1 = String.valueOf(esteenvase.getLote());
                                final String lots21 = String.valueOf(esteenvase.getLote2());
                                final String lots31 = String.valueOf(esteenvase.getLote3());
                                //Toast.makeText(MainActivity.this, cant1+cant21+cant31,Toast.LENGTH_SHORT).show();
                                AlertDialog.Builder mibuild = new AlertDialog.Builder(MainActivity.this);
                                final View mview = getLayoutInflater().inflate(R.layout.problema_dialogo, null);
                                mibuild.setTitle("Problema");
                                mibuild.setMessage("Anota su problema:");
                                final AlertDialog.Builder builder = mibuild.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ConnectivityManager conectividad1 = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                                        NetworkInfo lanet1 = conectividad1.getActiveNetworkInfo();
                                        if(lanet1 != null && lanet1.isConnected()) {
                                            EditText p1 = (EditText) mview.findViewById(R.id.problemas);
                                            Date fechahora = Calendar.getInstance().getTime();
                                            String pr1 = p1.getText().toString();
                                            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                                            String url2 = "https://appsionmovil.000webhostapp.com/asignar_pedidoenvasar.php?FechaAprobacion=" + dias.replaceAll(" ", "%20") +
                                                    "&Etapa1=Terminado&Cantidad="+ cant1 + "&DetalleProblema=" + pr1.replaceAll(" ","%20") +
                                                    "&Lote=" + lots1 + "&Cantidad2="+ cant21 + "&Lote2="+ lots21 + "&Cantidad3="+ cant31 +"&Lote3="+ lots31 +
                                                    "&ID=" + ids;
                                            Toast.makeText(MainActivity.this, "Problema Enviada", Toast.LENGTH_SHORT).show();
                                            cliente.post(url2, new AsyncHttpResponseHandler() {
                                                @Override
                                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) { if (statusCode == 200) { } }
                                                @Override
                                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) { }
                                            });
                                        }else {
                                            Toast.makeText(MainActivity.this, "Ha llego al limite de Intentos de Cantidad/Lote", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                mibuild.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ConnectivityManager conectividad1 = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                                        NetworkInfo lanet1 = conectividad1.getActiveNetworkInfo();
                                        if(lanet1 != null && lanet1.isConnected()) {
                                        dialog.cancel();
                                        } else {
                                            Toast.makeText(MainActivity.this, "Ha llego al limite de Intentos de Cantidad/Lote", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                mibuild.setView(mview);
                                AlertDialog dialog1 = mibuild.create();
                                dialog1.show();
                            } else{
                                Toast.makeText(MainActivity.this, "No hay Internet, intentarlo más tarde o verifica su conexión",Toast.LENGTH_SHORT).show();
                                lvlista1.setVisibility(View.INVISIBLE);
                            }
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
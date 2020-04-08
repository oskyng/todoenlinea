package com.example.oscarsanzana.todoenlineav2.fragmentos;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.icu.text.UnicodeSetSpanner;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.oscarsanzana.todoenlineav2.ConexionSqLiteHelper;
import com.example.oscarsanzana.todoenlineav2.R;
import com.example.oscarsanzana.todoenlineav2.entidades.Categoria;
import com.example.oscarsanzana.todoenlineav2.entidades.Marca;
import com.example.oscarsanzana.todoenlineav2.entidades.Producto;
import com.example.oscarsanzana.todoenlineav2.entidades.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AgregarProductoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AgregarProductoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AgregarProductoFragment extends Fragment implements RadioGroup.OnCheckedChangeListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String CARPETA_PRINCIPAL = "misImagenesApp/";//directorio principal
    private static final String CARPETA_IMAGEN = "imagenes";//carpeta donde se guardan las fotos
    private static final String DIRECTORIO_IMAGEN = CARPETA_PRINCIPAL + CARPETA_IMAGEN;//ruta carpeta de directorios
    private String path;//almacena la ruta de la imagen
    File fileImagen;
    Bitmap bitmap;

    private final int MIS_PERMISOS = 100;
    private static final int COD_SELECCIONA = 10;
    private static final int COD_FOTO = 20;

    private Producto producto;

    private CalendarView calFecha;
    private String date;
    private String sku;
    private String valorCorrelativo;

    private EditText txNombre;
    private EditText txPrecio;
    private EditText txSku;
    private EditText txStock;
    private ImageView imgFoto;
    private Spinner spMarca;
    private Spinner spCategoria;
    private Button btnAgregar;
    private Button btnFoto;
    private RadioGroup rgDietetico;
    private RadioButton rgTrue;
    private RadioButton rgFalse;
    private Categoria categoria = new Categoria();
    private Marca marca = new Marca();
    private char dietetico = 'N';
    private final ArrayList<Marca> listaMarca = new ArrayList<>();
    private final ArrayList<Categoria> listaCategoria = new ArrayList<>();

    private ProgressDialog progressDialog;

    private RequestQueue request;
    private JsonObjectRequest jsonObjectRequest;
    StringRequest stringRequest;
    private final String ip = "https://oscarsanzana97.000webhostapp.com";

    private View view;

    private OnFragmentInteractionListener mListener;

    public AgregarProductoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AgregarProductoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AgregarProductoFragment newInstance(String param1, String param2) {
        AgregarProductoFragment fragment = new AgregarProductoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // TODO: Rename and change types of parameters
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_agregar_producto, container, false);
        producto = new Producto();
        String url1 = "https://oscarsanzana97.000webhostapp.com/wsTodoEnLineaMarcasListar.php";
        String url2 = "https://oscarsanzana97.000webhostapp.com/wsTodoEnLineaCategoriasListar.php";

        imgFoto = view.findViewById(R.id.imgFoto);
        rgDietetico = view.findViewById(R.id.rgDietetico);
        rgTrue = view.findViewById(R.id.rbtrue);
        rgFalse = view.findViewById(R.id.rbfalse);
        txNombre = view.findViewById(R.id.txNombre);
        txPrecio = view.findViewById(R.id.txPrecio);
        txSku = view.findViewById(R.id.txSku);
        txStock = view.findViewById(R.id.txStock);

        calFecha = view.findViewById(R.id.calFecha);
        calFecha.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                int newMonth = month+1;
                date = dayOfMonth+"/"+newMonth+"/"+year;
                Toast.makeText(getContext(),date,Toast.LENGTH_SHORT).show();
            }
        });

        rgDietetico.setOnCheckedChangeListener(this);
        btnAgregar = view.findViewById(R.id.btnAgregar);
        btnFoto = view.findViewById(R.id.btnFoto);
        spMarca = view.findViewById(R.id.spMarca);
        spCategoria = view.findViewById(R.id.spCategoria);

        //SPINNER PARA MARCAS
        loadSpinnerDataMarcas(url1);
        spMarca.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                marca = listaMarca.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //SPINNER PARA CATEGORIAS
        loadSpinnerDataCategorias(url2);
        spCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoria = listaCategoria.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Permisos
        if(solicitaPermisosVersionesSuperiores()){
            btnFoto.setEnabled(true);
        }else{
            btnFoto.setEnabled(false);
        }

        btnFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogOpciones();
            }
        });

        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txNombre.getText().toString().length() > 0 && txNombre.getText().toString().length() < 100){
                    producto.setNombre_producto(txNombre.getText().toString());
                } else {
                    Toast.makeText(getContext(),"Nombre debe ser mayor a 0 y menor a 100 caracteres",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (txSku.getText().toString().length() == 6){
                    producto.setSku(txSku.getText().toString());
                } else {
                    Toast.makeText(getContext(),"SKU debe contener 6 caracteres",Toast.LENGTH_SHORT).show();
                    return;
                }


                producto.setCategoria_id(categoria);
                producto.setDietetico(dietetico);

                if (ValidarFecha()){
                    String[] parts = date.split("/");
                    String part1 = parts[0];
                    String part2 = parts[1];
                    String part3 = parts[2];
                    producto.setFecha_vencimiento(part3+"-"+part2+"-"+part1);
                } else {
                    Toast.makeText(getContext(),"Fecha es menor a la actual",Toast.LENGTH_SHORT).show();
                    return;
                }

                producto.setMarca_id(marca);

                if (txPrecio.getText().toString().length() == 0){
                    Toast.makeText(getContext(),"Precio no puede ser vacio",Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    if (Integer.parseInt(txPrecio.getText().toString()) < 100){
                        Toast.makeText(getContext(),"Precio debe ser mayor o igual a 100",Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        producto.setPrecio_producto(Integer.parseInt(txPrecio.getText().toString()));
                    }
                }

                if (txStock.getText().toString().length() == 0){
                    Toast.makeText(getContext(),"Stock no puede ser vacio",Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    if (Integer.parseInt(txStock.getText().toString()) <= 0){
                        Toast.makeText(getContext(),"Stock debe ser mayor a 0",Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        producto.setStock_producto(Integer.parseInt(txStock.getText().toString()));
                    }
                }

                ConnectivityManager con = (ConnectivityManager) Objects.requireNonNull(getContext()).getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = con.getActiveNetworkInfo();

                if (info != null && info.isConnected()){
                    producto.setStatus("T");
                    ingresarProductoWs();
                } else {
                    producto.setStatus("F");
                    registrarProductoSql(producto);
                }

            }
        });

        generarSku();

//        request = Volley.newRequestQueue(getContext());
//        VolleySingleton.getIntanciaVolley(getContext()).addToRequestQueue(jsonObjectRequest);
        return view;
    }

    //Opciones de Fotos

    private void mostrarDialogOpciones() {
        final CharSequence[] opciones={"Tomar Foto","Elegir de Galeria","Cancelar"};
        final AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
        builder.setTitle("Elige una Opción");
        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (opciones[i].equals("Tomar Foto")){
                    abriCamara();
                }else{
                    if (opciones[i].equals("Elegir de Galeria")){
                        Intent intent=new Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/");
                        startActivityForResult(intent.createChooser(intent,"Seleccione"),COD_SELECCIONA);
                    }else{
                        dialogInterface.dismiss();
                    }
                }
            }
        });
        builder.show();
    }

    private void abriCamara() {
        File miFile=new File(Environment.getExternalStorageDirectory(),DIRECTORIO_IMAGEN);
        boolean isCreada=miFile.exists();

        if(isCreada==false){
            isCreada=miFile.mkdirs();
        }

        if(isCreada==true){
            Long consecutivo= System.currentTimeMillis()/1000;
            String nombre=consecutivo.toString()+".jpg";

            path=Environment.getExternalStorageDirectory()+File.separator+DIRECTORIO_IMAGEN
                    +File.separator+nombre;//indicamos la ruta de almacenamiento

            fileImagen=new File(path);

            Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(fileImagen));

            ////
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)
            {
                String authorities=getContext().getPackageName()+".provider";
                Uri imageUri= FileProvider.getUriForFile(getContext(),authorities,fileImagen);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            }else
            {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileImagen));
            }
            startActivityForResult(intent,COD_FOTO);

            ////

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case COD_SELECCIONA:
                Uri miPath=data.getData();
                imgFoto.setImageURI(miPath);

                try {
                    bitmap=MediaStore.Images.Media.getBitmap(getContext().getContentResolver(),miPath);
                    imgFoto.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            case COD_FOTO:
                MediaScannerConnection.scanFile(getContext(), new String[]{path}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("Path",""+path);
                            }
                        });

                bitmap= BitmapFactory.decodeFile(path);
                imgFoto.setImageBitmap(bitmap);

                break;
        }
        bitmap=redimensionarImagen(bitmap,600,800);
    }

    private Bitmap redimensionarImagen(Bitmap bitmap, float anchoNuevo, float altoNuevo) {

        int ancho=bitmap.getWidth();
        int alto=bitmap.getHeight();

        if(ancho>anchoNuevo || alto>altoNuevo){
            float escalaAncho=anchoNuevo/ancho;
            float escalaAlto= altoNuevo/alto;

            Matrix matrix=new Matrix();
            matrix.postScale(escalaAncho,escalaAlto);

            return Bitmap.createBitmap(bitmap,0,0,ancho,alto,matrix,false);

        }else{
            return bitmap;
        }


    }

    private boolean solicitaPermisosVersionesSuperiores() {
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.M){//validamos si estamos en android menor a 6 para no buscar los permisos
            return true;
        }

        //validamos si los permisos ya fueron aceptados
        if((getContext().checkSelfPermission(WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)&&getContext().checkSelfPermission(CAMERA)==PackageManager.PERMISSION_GRANTED){
            return true;
        }


        if ((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)||(shouldShowRequestPermissionRationale(CAMERA)))){
            cargarDialogoRecomendacion();
        }else{
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MIS_PERMISOS);
        }

        return false;//implementamos el que procesa el evento dependiendo de lo que se defina aqui
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==MIS_PERMISOS){
            if(grantResults.length==2 && grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){//el dos representa los 2 permisos
                Toast.makeText(getContext(),"Permisos aceptados",Toast.LENGTH_SHORT);
                btnFoto.setEnabled(true);
            }
        }else{
            solicitarPermisosManual();
        }
    }

    private void solicitarPermisosManual() {
        final CharSequence[] opciones={"si","no"};
        final AlertDialog.Builder alertOpciones=new AlertDialog.Builder(getContext());//estamos en fragment
        alertOpciones.setTitle("¿Desea configurar los permisos de forma manual?");
        alertOpciones.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (opciones[i].equals("si")){
                    Intent intent=new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri=Uri.fromParts("package",getContext().getPackageName(),null);
                    intent.setData(uri);
                    startActivity(intent);
                }else{
                    Toast.makeText(getContext(),"Los permisos no fueron aceptados",Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                }
            }
        });
        alertOpciones.show();
    }

    private void cargarDialogoRecomendacion() {
        AlertDialog.Builder dialogo=new AlertDialog.Builder(getContext());
        dialogo.setTitle("Permisos Desactivados");
        dialogo.setMessage("Debe aceptar los permisos para el correcto funcionamiento de la App");

        dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},100);
            }
        });
        dialogo.show();
    }

    //FIN DE LAS OPCIONES DE FOTOS

    private void generarSku() {
        sku = "PTL";
        final String ip = getString(R.string.ip);

        String url = ip + "/wsTodoEnLineaProductosListar.php";

        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Producto producto;

                JSONArray json=response.optJSONArray("producto");
                JSONObject jsonObject;
                try {

                    for (int i=0;i<json.length();i++){
                        jsonObject=json.getJSONObject(i);
                        int id = jsonObject.getInt("id");
                        valorCorrelativo = String.format("%03d",id+1);
                    }

                    sku = sku + valorCorrelativo;
                    txSku.setText(sku);
                    txSku.setEnabled(false);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "No se ha podido establecer conexión con el servidor" +
                            " "+response, Toast.LENGTH_LONG).show();
                    progressDialog.hide();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "No se puede conectar " + error.toString(), Toast.LENGTH_LONG).show();
                System.out.println();
//                progressDialog.hide();
                Log.d("ERROR: ", error.toString());
            }
        });
        VolleySingleton.getIntanciaVolley(getContext()).addToRequestQueue(jsonObjectRequest);
    }

    private boolean ValidarFecha(){
        Date fechaActual = new Date();
        SimpleDateFormat formateador = new SimpleDateFormat("dd/MM/yyyy");
        String fechaSistema=formateador.format(fechaActual);
        try {
            Date fechaDate1 = formateador.parse(date);
            Date fechaDate2 = formateador.parse(fechaSistema);
            if (fechaDate1.compareTo(fechaDate2)>0){
                return true;
            } else {
                return false;
            }
        } catch (Exception e){
            e.getMessage();
            return false;
        }
    }

    private void loadSpinnerDataMarcas(String url) {
        RequestQueue requestQueue=Volley.newRequestQueue(getContext());
        StringRequest stringRequest=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonObject=new JSONObject(response);
                            JSONArray jsonArray=jsonObject.getJSONArray("marca");
                            for(int i=0;i<jsonArray.length();i++){
                                JSONObject jsonObject1=jsonArray.getJSONObject(i);

                                listaMarca.add(new Marca(jsonObject1.getInt("marca_id"),jsonObject1.getString("nombre_marca")));
                            }
                            String[] arreglo = new String[listaMarca.size()];
                            for (int i = 0; i <arreglo.length;i++){
                                arreglo[i]= listaMarca.get(i).getNombcre_marca();
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, arreglo);
//                            spMarca.setAdapter(new ArrayAdapter<Marca>(getContext(), android.R.layout.simple_spinner_dropdown_item, listaMarca));
                            spMarca.setAdapter(adapter);
                        }catch (JSONException e){e.printStackTrace();}
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        requestQueue.add(stringRequest);
    }

    private void loadSpinnerDataCategorias(String url) {
        RequestQueue requestQueue=Volley.newRequestQueue(getContext());
        StringRequest stringRequest=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonObject=new JSONObject(response);
                            JSONArray jsonArray=jsonObject.getJSONArray("categoria");
                            for(int i=0;i<jsonArray.length();i++){
                                JSONObject jsonObject1=jsonArray.getJSONObject(i);
                                listaCategoria.add(new Categoria(jsonObject1.getInt("categoria_id"),jsonObject1.getString("nombre_categoria"),jsonObject1.getString("descripcion_categoria")));
                            }
                            String[] arreglo = new String[listaCategoria.size()];
                            for (int i = 0; i <arreglo.length;i++){
                                arreglo[i]= listaCategoria.get(i).getNombre_categoria();
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, arreglo);
                            spCategoria.setAdapter(adapter);

                            //spCategoria.setAdapter(new ArrayAdapter<Categoria>(getContext(), android.R.layout.simple_spinner_dropdown_item, listaCategoria));
                        }catch (JSONException e){e.printStackTrace();}
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        requestQueue.add(stringRequest);
    }

    private void ingresarProductoWs(){
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Cargando...");
        progressDialog.show();

        String url = ip+"/wsTodoEnLineaProductosInsertar.php?";
        stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.hide();
                if (response.trim().equalsIgnoreCase("registra")){
                    Toast.makeText(getContext(),"Se ha registrado con exito",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getContext(),"No se ha registrado ",Toast.LENGTH_SHORT).show();
                    Log.i("RESPUESTA: ",""+response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(),error.getMessage(),Toast.LENGTH_SHORT).show();
                progressDialog.hide();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String sku=txSku.getText().toString();
                String nombre_producto = txNombre.getText().toString();
                String precio_producto = txPrecio.getText().toString();
                String stock_producto = txStock.getText().toString();
                String fecha_vencimiento = producto.getFecha_vencimiento();
                String dietetico_producto = Character.toString(dietetico);
                String categoria_id = categoria.getCategoria_id().toString();
                String marca_id = marca.getId().toString();

                String imagen = convertirImgString(bitmap);
                String status = producto.getStatus();

                Map<String,String> parametros=new HashMap<>();
                parametros.put("sku",sku);
                parametros.put("nombre_producto",nombre_producto);
                parametros.put("precio_producto",precio_producto);
                parametros.put("stock_producto",stock_producto);
                parametros.put("fecha_vencimiento",fecha_vencimiento);
                parametros.put("dietetico",dietetico_producto);
                parametros.put("categoria_id",categoria_id);
                parametros.put("marca_id",marca_id);
                parametros.put("imagen_producto",imagen);
                parametros.put("status",status);

                return parametros;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getIntanciaVolley(getContext()).addToRequestQueue(stringRequest);
    }

    private String convertirImgString(Bitmap bitmap) {

        ByteArrayOutputStream array=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,array);
        byte[] imagenByte=array.toByteArray();
        String imagenString= Base64.encodeToString(imagenByte,Base64.DEFAULT);

        return imagenString;
    }



    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if(checkedId == R.id.rbtrue){
            dietetico = 'S';
        }

        if(checkedId == R.id.rbfalse){
            dietetico = 'N';
        }
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void registrarProductoSql(Producto producto) {

        ConexionSqLiteHelper conn = new ConexionSqLiteHelper(getContext(), "bd_productos", null, 1);
        try (SQLiteDatabase db = conn.getWritableDatabase()) {
            String insert = "INSERT INTO productos(sku,nombre_producto,precio_producto,stock_producto,fecha_vencimiento,dietetico,categoria_id,marca_id,imagen_producto,status) " +
                    "VALUES ('" + producto.getSku() + "','" + producto.getNombre_producto() + "','" + producto.getPrecio_producto() + "','" + producto.getStock_producto() + "'" +
                    ",'" + producto.getFecha_vencimiento() + "','" + producto.getDietetico() + "','" + producto.getCategoria_id().getCategoria_id() + "','" + producto.getMarca_id().getId() +"','" +producto.getRutaImagen()+ "','"+ producto.getStatus() +"')";
            db.execSQL(insert);
            Toast.makeText(getContext(), "Producto agregado", Toast.LENGTH_SHORT).show();
            db.close();

        } catch (Exception ex) {
            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            conn.getWritableDatabase().close();

        }
    }

    private void llenarListaMarcas(){
        ConexionSqLiteHelper conn = new ConexionSqLiteHelper(getContext(),"bd_marcas",null,1);
        SQLiteDatabase db = conn.getReadableDatabase();

        try {
            Cursor cursor= db.rawQuery("SELECT * FROM marcas",null );

            if(cursor.moveToNext()){
                do {
                    listaMarca.add(new Marca(cursor.getInt(0),cursor.getString(1)));
                } while (cursor.moveToNext());
            }
            String[] arreglo = new String[listaMarca.size()];
            for (int i = 0; i <arreglo.length;i++){
                arreglo[i]= listaMarca.get(i).getNombcre_marca();
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,arreglo);
            spMarca.setAdapter(adapter);
        }catch (Exception ex){
            db.close();
        }finally {
            db.close();
        }
    }

    private void llenarListaCategoria(){
        ConexionSqLiteHelper conn = new ConexionSqLiteHelper(getContext(),"bd_categorias",null,1);
        SQLiteDatabase db = conn.getReadableDatabase();

        try {
            Cursor cursor= db.rawQuery("SELECT * FROM categorias",null );

            if(cursor.moveToNext()){
                do {
                    listaCategoria.add(new Categoria(cursor.getInt(0),cursor.getString(1),cursor.getString(2)));
                } while (cursor.moveToNext());
            }
            String[] arreglo = new String[listaCategoria.size()];
            for (int i = 0; i <arreglo.length;i++){
                arreglo[i]= listaCategoria.get(i).getNombre_categoria();
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,arreglo);
            spCategoria.setAdapter(adapter);
        }catch (Exception ex){
            db.close();
        }finally {
            db.close();
        }
    }
}

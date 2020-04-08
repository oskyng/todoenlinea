package com.example.oscarsanzana.todoenlineav2.fragmentos;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.oscarsanzana.todoenlineav2.ConexionSqLiteHelper;
import com.example.oscarsanzana.todoenlineav2.R;
import com.example.oscarsanzana.todoenlineav2.entidades.Producto;
import com.example.oscarsanzana.todoenlineav2.entidades.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BuscarProductoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BuscarProductoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BuscarProductoFragment extends Fragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ProgressDialog progressDialog;

    RequestQueue request;
    private JsonObjectRequest jsonObjectRequest;

    //Segunda forma
    private StringRequest stringRequest;

    private EditText txIdConsultar;
    private EditText txPrecio_producto;
    private EditText txStock_producto;
    private EditText txNombre_producto;
    private EditText txFecha_vencimiento;
    private ImageView imgFoto;
    private Button btnBuscar;
    private Button btnFoto;
    private Button btnEliminarProducto;
    private Button btnModificarProducto;

    private final int MIS_PERMISOS = 100;
    private static final int COD_SELECCIONA = 10;
    private static final int COD_FOTO = 20;

    private static final String CARPETA_PRINCIPAL = "misImagenesApp/";//directorio principal
    private static final String CARPETA_IMAGEN = "imagenes";//carpeta donde se guardan las fotos
    private static final String DIRECTORIO_IMAGEN = CARPETA_PRINCIPAL + CARPETA_IMAGEN;//ruta carpeta de directorios
    private String path;//almacena la ruta de la imagen
    File fileImagen;
    Bitmap bitmap;



    private View view;

    private OnFragmentInteractionListener mListener;

    public BuscarProductoFragment() {
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
    public static BuscarProductoFragment newInstance(String param1, String param2) {
        BuscarProductoFragment fragment = new BuscarProductoFragment();
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
        view = inflater.inflate(R.layout.fragment_buscar_producto, container, false);

        txIdConsultar = view.findViewById(R.id.txIdConsultar);
        txPrecio_producto = view.findViewById(R.id.txPrecio_producto);
        txStock_producto = view.findViewById(R.id.txStock_producto);
        txNombre_producto = view.findViewById(R.id.txNombre_producto);
        txFecha_vencimiento = view.findViewById(R.id.txFecha_vencimiento);
        imgFoto = view.findViewById(R.id.imgFoto);

        btnEliminarProducto  = view.findViewById(R.id.btnEliminarProducto);
        btnBuscar = view.findViewById(R.id.btnConsultarProducto);
        btnModificarProducto = view.findViewById(R.id.btnModificarProducto) ;
        btnFoto = view.findViewById(R.id.btnFoto);

        txPrecio_producto.setEnabled(false);
        txNombre_producto.setEnabled(false);
        txStock_producto.setEnabled(false);
        txFecha_vencimiento.setEnabled(false);
        btnEliminarProducto.setEnabled(false);
        btnModificarProducto.setEnabled(false);

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

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txIdConsultar.getText().toString().length() == 0){
                    Toast.makeText(getContext(),"El campo SKU no debe ir vacio",Toast.LENGTH_SHORT).show();
                    return;
                }
                ConnectivityManager con = (ConnectivityManager) Objects.requireNonNull(getContext()).getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = con.getActiveNetworkInfo();
                if (info != null && info.isConnected()){
                    cargarWebService();
                    txIdConsultar.setEnabled(false);
                } else {
                    Toast.makeText(getContext(),"No hay conexion a internet",Toast.LENGTH_SHORT).show();
                    return;
                }


            }
        });
        btnEliminarProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                builder.setMessage("¿Eliminar Producto "+ txNombre_producto.getText().toString() +"?")
                        .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ConnectivityManager con = (ConnectivityManager) Objects.requireNonNull(getContext()).getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo info = con.getActiveNetworkInfo();
                                if (info != null && info.isConnected()){
                                    webServiceEliminar();
                                } else {
                                    Toast.makeText(getContext(),"No hay conexion a internet",Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getContext(), "No se ha eliminado", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();

            }
        });
        btnModificarProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txNombre_producto.getText().toString().length() == 0){
                    Toast.makeText(getContext(),"El campo Nombre del producto no debe ir vacio",Toast.LENGTH_SHORT).show();
                    return;
                } else if (txNombre_producto.getText().toString().length() > 25){
                    Toast.makeText(getContext(),"El campo Nombre del producto no debe superar los 25 caracteres",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (txPrecio_producto.getText().toString().length() == 0){
                    Toast.makeText(getContext(),"El campo Precio del producto no debe ir vacio",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (txStock_producto.getText().toString().length() == 0){
                    Toast.makeText(getContext(),"El campo Stock del producto no debe ir vacio",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (txFecha_vencimiento.getText().toString().length() == 0){
                    Toast.makeText(getContext(),"El campo Fecha de vencimiento no debe ir vacio",Toast.LENGTH_SHORT).show();
                    return;
                }

                ConnectivityManager con = (ConnectivityManager) Objects.requireNonNull(getContext()).getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = con.getActiveNetworkInfo();
                if (info != null && info.isConnected()){
                    webServiceActualizar();
                } else {
                    Toast.makeText(getContext(),"No hay conexion a internet",Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });
//        request = Volley.newRequestQueue(getContext());
        return view;
    }

    private void cargarWebService() {
        progressDialog=new ProgressDialog(getContext());
        progressDialog.setMessage("Cargando...");
        progressDialog.show();

        final String ip=getString(R.string.ip);

        String url=ip+"/wsTodoEnLineaProductosConsultar.php?id="+txIdConsultar.getText().toString().toUpperCase();

        jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.hide();

                Producto producto=new Producto();

                JSONArray json=response.optJSONArray("producto");
                JSONObject jsonObject;

                try {
                    jsonObject=json.getJSONObject(0);
                    if (jsonObject.optInt("id") == 0){
                        Toast.makeText(getContext(),"El producto que esta buscando no existe",Toast.LENGTH_SHORT).show();
                        txNombre_producto.setText("");
                        txPrecio_producto.setText("");
                        txStock_producto.setText("");
                        txFecha_vencimiento.setText("");
                        imgFoto.setImageResource(R.drawable.img_base);
                        btnEliminarProducto.setEnabled(false);
                        btnModificarProducto.setEnabled(true);
                        btnFoto.setEnabled(false);
                        return;
                    }
                    producto.setNombre_producto(jsonObject.optString("nombre_producto"));
                    producto.setPrecio_producto(jsonObject.optInt("precio_producto"));
                    producto.setStock_producto(jsonObject.optInt("stock_producto"));
                    producto.setFecha_vencimiento(jsonObject.optString("fecha_vencimiento"));
                    producto.setRutaImagen(jsonObject.optString("imagen_producto"));
                    btnEliminarProducto.setEnabled(true);
                    btnModificarProducto.setEnabled(true);
                    btnFoto.setEnabled(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                txNombre_producto.setText(producto.getNombre_producto());
                txPrecio_producto.setText(producto.getPrecio_producto().toString());
                txStock_producto.setText(producto.getStock_producto().toString());
                txFecha_vencimiento.setText(producto.getFecha_vencimiento());

                String urlImagen = ip+"/imagenes/"+producto.getRutaImagen();
                cargarWebServiceImagen(urlImagen);

                txPrecio_producto.setEnabled(true);
                txNombre_producto.setEnabled(true);
                txStock_producto.setEnabled(true);
                txFecha_vencimiento.setEnabled(true);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "No se puede conectar "+error.toString(), Toast.LENGTH_LONG).show();
                System.out.println();
                progressDialog.hide();
                Log.d("ERROR: ", error.toString());
            }
        });

        // request.add(jsonObjectRequest);
        VolleySingleton.getIntanciaVolley(getContext()).addToRequestQueue(jsonObjectRequest);
    }

    private void cargarWebServiceImagen(String urlImagen) {
        urlImagen = urlImagen.replace(" ","%20");

        ImageRequest imageRequest = new ImageRequest(urlImagen, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                bitmap = response;
                imgFoto.setImageBitmap(response);
            }
        }, 0, 0, ImageView.ScaleType.CENTER, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(),"Error al cargar la imagen"+error,Toast.LENGTH_SHORT).show();
                Log.i("ERROR IMAGEN","Response -> "+error);
            }
        });
        //  request.add(imageRequest);
        VolleySingleton.getIntanciaVolley(getContext()).addToRequestQueue(imageRequest);
    }

    private void webServiceActualizar() {
        progressDialog=new ProgressDialog(getContext());
        progressDialog.setMessage("Cargando...");
        progressDialog.show();

        String ip=getString(R.string.ip);

        String url=ip+"/wsTodoEnLineaProductosEditar.php?";

        stringRequest=new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.hide();

                if (response.trim().equalsIgnoreCase("actualizado")){
                    Toast.makeText(getContext(),"Se ha Actualizado con exito",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getContext(),"No se ha Actualizado ",Toast.LENGTH_SHORT).show();
                    Log.i("RESPUESTA: ",""+response);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(),"No se ha podido conectar",Toast.LENGTH_SHORT).show();
                progressDialog.hide();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                String nombreProducto=txNombre_producto.getText().toString();
                String precio=txPrecio_producto.getText().toString();
                String stock=txStock_producto.getText().toString();
                String fecha = txFecha_vencimiento.getText().toString();
                String id = txIdConsultar.getText().toString();
                String imagen = convertirImgString(bitmap);

                Map<String,String> parametros=new HashMap<>();
                parametros.put("nombre_producto",nombreProducto);
                parametros.put("precio_producto",precio);
                parametros.put("stock_producto",stock);
                parametros.put("fecha_vencimiento",fecha);
                parametros.put("imagen",imagen);
                parametros.put("id",id);

                return parametros;
            }
        };
        VolleySingleton.getIntanciaVolley(getContext()).addToRequestQueue(stringRequest);
    }

    private String convertirImgString(Bitmap bitmap) {

        ByteArrayOutputStream array=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,array);
        byte[] imagenByte=array.toByteArray();
        String imagenString= Base64.encodeToString(imagenByte,Base64.DEFAULT);

        return imagenString;
    }

    private void webServiceEliminar() {
        progressDialog=new ProgressDialog(getContext());
        progressDialog.setMessage("Cargando...");
        progressDialog.show();

        String ip=getString(R.string.ip);

        String url=ip+"/wsTodoEnLineaProductosEliminar.php?id="+txIdConsultar.getText().toString();

        stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.hide();

                if (response.trim().equalsIgnoreCase("eliminado")){
                    txNombre_producto.setText("");
                    txPrecio_producto.setText("");
                    txStock_producto.setText("");
                    txFecha_vencimiento.setText("");
                    imgFoto.setImageResource(R.drawable.img_base);
                    btnEliminarProducto.setEnabled(false);
                    btnModificarProducto.setEnabled(false);
                    btnFoto.setEnabled(false);
                    imgFoto.setImageResource(R.drawable.img_base);
                    Toast.makeText(getContext(),"Se ha Eliminado con exito",Toast.LENGTH_SHORT).show();
                }else{
                    if (response.trim().equalsIgnoreCase("noEliminado")){
                        Toast.makeText(getContext(),"No se encuentra el producto ",Toast.LENGTH_SHORT).show();
                        Log.i("RESPUESTA: ",""+response);
                    }else{
                        Toast.makeText(getContext(),"No se ha Eliminado ",Toast.LENGTH_SHORT).show();
                        Log.i("RESPUESTA: ",""+response);
                    }

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(),"No se ha podido conectar",Toast.LENGTH_SHORT).show();
                progressDialog.hide();
            }
        });
        //request.add(stringRequest);
        VolleySingleton.getIntanciaVolley(getContext()).addToRequestQueue(stringRequest);
    }

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

    //PERMISOS
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
                imgFoto.setEnabled(true);//se vincula el evento a la imagen
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

    private boolean consultarProducto(String id) {

        ConexionSqLiteHelper conn = new ConexionSqLiteHelper(getContext(),"bd_productos",null,1);
        SQLiteDatabase db = conn.getReadableDatabase();

        try{
            String[] parametros = {id};
            String[] campos = {"nombre_producto","precio_producto","stock_producto","fecha_vencimiento"};

            Cursor cursorConsulta = db.query("productos",campos,"id=?",parametros,null,null,null);
            return cursorConsulta.moveToFirst();
        }
        catch (Exception ex){
            conn.close();
            return false;
        }
        finally {
            conn.close();
        }
    }

    private void eliminarProducto(String id){
        ConexionSqLiteHelper conn = new ConexionSqLiteHelper(getContext(),"bd_productos",null,1);
        SQLiteDatabase db = conn.getReadableDatabase();

        try{
            String[] parametros = {id};
            int consulta = db.delete("productos","id=?",parametros);
            Toast.makeText(getContext(),"Producto Eliminado", Toast.LENGTH_SHORT).show();
            db.close();
        }
        catch (Exception ex){
            conn.close();
        }
        finally {
            conn.close();
        }
    }

    private void mostrarCampos(){
        ConexionSqLiteHelper conn = new ConexionSqLiteHelper(getContext(),"bd_productos",null,1);

        try (SQLiteDatabase db = conn.getReadableDatabase()) {
            Cursor cursor = db.rawQuery("SELECT nombre_producto, precio_producto, stock_producto, fecha_vencimiento FROM productos WHERE  id=" + txIdConsultar.getText().toString() + "", null);

            if (cursor.moveToNext()) {
                txNombre_producto.setText(cursor.getString(0));
                txPrecio_producto.setText(cursor.getString(1));
                txStock_producto.setText(cursor.getString(2));
                txFecha_vencimiento.setText(cursor.getString(3));
            }
        } catch (Exception ex) {
            conn.getWritableDatabase().close();
        }
    }

    private void modifcarProducto(){
        ConexionSqLiteHelper conn = new ConexionSqLiteHelper(getContext(),"bd_productos",null,1);
        SQLiteDatabase db = conn.getReadableDatabase();

        String id = txIdConsultar.getText().toString();
        try{

            ContentValues args = new ContentValues();
            args.put("nombre_producto", txNombre_producto.getText().toString());
            args.put("precio_producto", Integer.parseInt(txPrecio_producto.getText().toString()));
            args.put("stock_producto", Integer.parseInt(txStock_producto.getText().toString()));
            args.put("fecha_vencimiento", txFecha_vencimiento.getText().toString());

            String[] valor = {id};

            int update = db.update("productos",args,"id=?",valor);

            if (update > 0){
                Toast.makeText(getContext(),"Modificado correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(),"No correctamente", Toast.LENGTH_SHORT).show();
            }


            db.close();
        } catch (Exception ex){
            Toast.makeText(getContext(),ex.getMessage(),Toast.LENGTH_SHORT).show();
            conn.close();
        }
        finally {
            conn.close();
        }
    }
}

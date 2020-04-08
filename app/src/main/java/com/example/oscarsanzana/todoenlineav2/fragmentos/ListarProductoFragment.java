package com.example.oscarsanzana.todoenlineav2.fragmentos;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.oscarsanzana.todoenlineav2.R;
import com.example.oscarsanzana.todoenlineav2.adapter.ProductosAdapter;
import com.example.oscarsanzana.todoenlineav2.entidades.Producto;
import com.example.oscarsanzana.todoenlineav2.entidades.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ListarProductoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ListarProductoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListarProductoFragment extends Fragment implements Response.Listener<JSONObject>, Response.ErrorListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private final ArrayList<Producto> listaProducto = new ArrayList<>();
    private RecyclerView lista;
    private ProgressDialog progressDialog;

    private RequestQueue request;
    private JsonObjectRequest jsonObjectRequest;

    //Segunda forma
    StringRequest stringRequest;

    private View view;

    private OnFragmentInteractionListener mListener;

    public ListarProductoFragment() {
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
    public static ListarProductoFragment newInstance(String param1, String param2) {
        ListarProductoFragment fragment = new ListarProductoFragment();
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
        view = inflater.inflate(R.layout.fragment_listar_producto, container, false);
        lista = view.findViewById(R.id.listaProductos);
        lista.setLayoutManager(new LinearLayoutManager(this.getContext()));
        lista.setHasFixedSize(true);
        request = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
        ConnectivityManager con = (ConnectivityManager) Objects.requireNonNull(getContext()).getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = con.getActiveNetworkInfo();
        if (info != null && info.isConnected()){
            cargarWebService();
        } else {
            Toast.makeText(getContext(),"No hay conexion a internet",Toast.LENGTH_SHORT).show();
        }


        return view;
    }

    private void cargarWebService() {

        progressDialog=new ProgressDialog(getContext());
        progressDialog.setMessage("Consultando...");
        progressDialog.show();

        String ip=getString(R.string.ip);

        String url=ip+"/wsTodoEnLineaProductosListar.php";

        jsonObjectRequest=new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        // request.add(jsonObjectRequest);
        VolleySingleton.getIntanciaVolley(getContext()).addToRequestQueue(jsonObjectRequest);
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

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(getContext(), "No se puede conectar "+error.toString(), Toast.LENGTH_LONG).show();
        System.out.println();
        Log.d("ERROR: ", error.toString());
        progressDialog.hide();
    }

    @Override
    public void onResponse(JSONObject response) {
        Producto producto;

        JSONArray json=response.optJSONArray("producto");

        try {

            for (int i=0;i<json.length();i++){
                producto=new Producto();
                JSONObject jsonObject;
                jsonObject=json.getJSONObject(i);

                producto.setSku(jsonObject.optString("sku"));
                producto.setNombre_producto(jsonObject.optString("nombre_producto"));
                producto.setPrecio_producto(jsonObject.optInt("precio_producto"));
                String[] parts = jsonObject.optString("fecha_vencimiento").split("-");
                String part1 = parts[0];
                String part2 = parts[1];
                String part3 = parts[2];
                producto.setFecha_vencimiento(part3+"/"+part2+"/"+part1);
                producto.setRutaImagen(jsonObject.optString("imagen_producto"));
                listaProducto.add(producto);
            }
            progressDialog.hide();
            ProductosAdapter adapter=new ProductosAdapter(listaProducto, getContext());
            lista.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "No se ha podido establecer conexión con el servidor" +
                    " "+response, Toast.LENGTH_LONG).show();
            progressDialog.hide();
        }
    }



}

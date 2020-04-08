package com.example.oscarsanzana.todoenlineav2.fragmentos;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.oscarsanzana.todoenlineav2.ConexionSqLiteHelper;
import com.example.oscarsanzana.todoenlineav2.R;
import com.example.oscarsanzana.todoenlineav2.entidades.Marca;

import org.json.JSONObject;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AgregarMarcaFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AgregarMarcaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AgregarMarcaFragment extends Fragment implements Response.Listener<JSONObject>, Response.ErrorListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private View view;
    private Marca marca;

    private EditText txNombreMarca;
    private Button btnAgregarM;

    private ProgressDialog progressDialog;

    private RequestQueue request;
    private JsonObjectRequest jsonObjectRequest;
    private final String ip = "https://oscarsanzana97.000webhostapp.com";

    private OnFragmentInteractionListener mListener;

    public AgregarMarcaFragment() {
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
    public static AgregarMarcaFragment newInstance(String param1, String param2) {
        AgregarMarcaFragment fragment = new AgregarMarcaFragment();
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
        view = inflater.inflate(R.layout.fragment_agregar_marca, container, false);

        marca = new Marca();

        txNombreMarca = view.findViewById(R.id.txNombreMarca);

        btnAgregarM = view.findViewById(R.id.btnAgregar);
        btnAgregarM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txNombreMarca.getText().toString().length()==0){
                    Toast.makeText(getContext(), "Marca no debe ser vacia",Toast.LENGTH_SHORT).show();
                    return;
                }else if(txNombreMarca.getText().toString().length()>3 && txNombreMarca.getText().toString().length()>50){
                    Toast.makeText(getContext(), "el largo del nombre debe superar los 3 caracteres y menor a 50 caracteres", Toast.LENGTH_SHORT).show();
                    return;
                }
                marca.setNombre_marca(txNombreMarca.getText().toString());

                ConnectivityManager con = (ConnectivityManager) Objects.requireNonNull(getContext()).getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = con.getActiveNetworkInfo();

                if (info != null && info.isConnected()){
                    ingresarMarcasWs();
                } else {
                    Toast.makeText(getContext(),"No hay conexion a internet",Toast.LENGTH_SHORT).show();
                    registrarMarcaSql(marca);
                    return;
                }
            }
        });
        request = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
        return view;
    }

    private void ingresarMarcasWs(){
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Cargando...");
        progressDialog.show();

        String url = ip+"/wsTodoEnLineaMarcasInsertar.php?nombre="
                +txNombreMarca.getText().toString();
        url = url.replace(" ","%20");

        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        request.add(jsonObjectRequest);
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
    public void onErrorResponse(VolleyError error) {
        progressDialog.hide();
        Toast.makeText(getContext(),"Ha ocurrido en error: "+ error.toString(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponse(JSONObject response) {
        Toast.makeText(getContext(),"Marca ingresado correctamente",Toast.LENGTH_SHORT).show();
        progressDialog.hide();
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

    private void registrarMarcaSql(Marca marca){
        ConexionSqLiteHelper conn = new ConexionSqLiteHelper(getContext(),"bd_marcas",null,1);
        try (SQLiteDatabase db = conn.getWritableDatabase()) {
            //"CREATE TABLE marcas(marca_id INTEGER PRIMARY KEY AUTOINCREMENT,nombre_marca VARCHAR(100))"

            String insert = "INSERT INTO marcas(nombre_marca) VALUES ('" + marca.getNombcre_marca() + "')";

            db.execSQL(insert);
            Toast.makeText(getContext(), "Marca agregada", Toast.LENGTH_SHORT).show();
            db.close();
        } catch (Exception ex) {
            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            conn.getWritableDatabase().close();
        }
    }
}

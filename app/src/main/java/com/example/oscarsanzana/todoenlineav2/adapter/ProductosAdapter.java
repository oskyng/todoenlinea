package com.example.oscarsanzana.todoenlineav2.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.example.oscarsanzana.todoenlineav2.R;
import com.example.oscarsanzana.todoenlineav2.entidades.Producto;
import com.example.oscarsanzana.todoenlineav2.entidades.VolleySingleton;

import java.util.List;

public class ProductosAdapter extends RecyclerView.Adapter<ProductosAdapter.ProductosHolder> {
    private final List<Producto> listaProductos;
    private final Context context;

    public ProductosAdapter(List<Producto> listaProductos, Context context) {
        this.listaProductos = listaProductos;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductosHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista= LayoutInflater.from(parent.getContext()).inflate(R.layout.producto_list,parent,false);
        RecyclerView.LayoutParams layoutParams=new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        vista.setLayoutParams(layoutParams);
        return new ProductosHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductosAdapter.ProductosHolder holder, int position) {
        holder.txSku.setText(listaProductos.get(position).getSku());
        holder.txNombre_producto.setText(listaProductos.get(position).getNombre_producto());
        holder.txPrecioProducto.setText("$"+listaProductos.get(position).getPrecio_producto().toString());
        holder.txFecha_vencimiento.setText(listaProductos.get(position).getFecha_vencimiento());

        if (listaProductos.get(position).getRutaImagen() != null){
            cargarImagenWebService(listaProductos.get(position).getRutaImagen(),holder);
        } else {
            holder.imagen.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    private void cargarImagenWebService(String rutaImagen, final ProductosHolder holder) {

        String ip=context.getString(R.string.ip);

        String urlImagen=ip+"/imagenes/"+rutaImagen;
        urlImagen=urlImagen.replace(" ","%20");

        ImageRequest imageRequest=new ImageRequest(urlImagen, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                holder.imagen.setImageBitmap(response);
            }
        }, 0, 0, ImageView.ScaleType.CENTER, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context,"Error al cargar la imagen",Toast.LENGTH_SHORT).show();
            }
        });
        //request.add(imageRequest);
        VolleySingleton.getIntanciaVolley(context).addToRequestQueue(imageRequest);
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    public class ProductosHolder extends RecyclerView.ViewHolder{

        final TextView txSku;
        final TextView txNombre_producto;
        final TextView txPrecioProducto;
        final TextView txFecha_vencimiento;
        final ImageView imagen;

        ProductosHolder(View itemView) {
            super(itemView);
            txSku = itemView.findViewById(R.id.txSku);
            txNombre_producto = itemView.findViewById(R.id.txNombre_producto);
            txPrecioProducto = itemView.findViewById(R.id.txPrecioProducto);
            txFecha_vencimiento = itemView.findViewById(R.id.txFecha_vencimiento);
            imagen = itemView.findViewById(R.id.idImagen);
        }
    }
}

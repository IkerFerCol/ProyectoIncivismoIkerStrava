package com.example.proyectoincivismoikerstrava.ui.notifications;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


import com.example.proyectoincivismoikerstrava.R;
import com.example.proyectoincivismoikerstrava.databinding.FragmentNotificationsBinding;
import com.example.proyectoincivismoikerstrava.ui.Ruta;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private FirebaseAuth auth;
    private DatabaseReference rutas;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Context ctx = requireActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        binding.map.setTileSource(TileSourceFactory.MAPNIK);
        binding.map.setMultiTouchControls(true);
        IMapController mapController = binding.map.getController();
        mapController.setZoom(14.5);


        GeoPoint vila = new GeoPoint(39.92991796803797, -0.1061766132788344);
        mapController.setCenter(vila);

        Marker startMarker = new Marker(binding.map);
        startMarker.setPosition(vila);
        startMarker.setTitle("Vila-real");
        startMarker.setIcon(requireContext().getDrawable(R.drawable.ic_home_black_24dp));
        binding.map.getOverlays().add(startMarker);


        MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), binding.map);
        myLocationOverlay.enableMyLocation();
        binding.map.getOverlays().add(myLocationOverlay);

        CompassOverlay compassOverlay = new CompassOverlay(requireContext(), binding.map);
        compassOverlay.enableCompass();
        binding.map.getOverlays().add(compassOverlay);

        auth = FirebaseAuth.getInstance();
        DatabaseReference base = FirebaseDatabase.getInstance("https://proyectoincivismoikerstrava-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        DatabaseReference users = base.child("users");
        DatabaseReference uid = users.child(auth.getUid());
        rutas = uid.child("incidencies");

        Log.d("III", rutas.toString());

        rutas.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                if (binding == null || binding.map == null) {
                    Log.e("FirebaseError", "El fragmento ya no está visible.");
                    return;
                }
                Ruta ruta = snapshot.getValue(Ruta.class);
                Double latitud = snapshot.child("latitud").getValue(Double.class);
                Double longitud = snapshot.child("longitud").getValue(Double.class);
                Log.d("OWOWO", latitud + " " + longitud);

                if (ruta != null) {
                    GeoPoint location = new GeoPoint(latitud, longitud);

                    Marker marker = new Marker(binding.map);
                    marker.setPosition(location);
                    marker.setTitle(ruta.getNombre());
                    marker.setSnippet(ruta.getDireccio());

                    binding.map.getOverlays().add(marker);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.map.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
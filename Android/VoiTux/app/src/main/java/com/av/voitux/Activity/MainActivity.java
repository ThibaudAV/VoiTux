package com.av.voitux.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.av.voitux.Fragment.ConnexionFormFragment;
import com.av.voitux.Fragment.ConnexionsFragment;
import com.av.voitux.Models.Connexion;
import com.av.voitux.voituxandroid.R;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.Iconics;
import com.mikepenz.iconics.context.IconicsLayoutInflater;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));
        super.onCreate(savedInstanceState);

        Iconics.registerFont(new GoogleMaterial());

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(null == savedInstanceState){
            displayFragmentConnexions();
        }

    }

    @Override
    public void onBackPressed() {
        ConnexionFormFragment connexionFormFragment = (ConnexionFormFragment)
                getSupportFragmentManager().findFragmentByTag("TAG_EDIT_CONNEXION");
        if (connexionFormFragment != null && connexionFormFragment.isVisible()) {
            displayFragmentConnexions();
        }
        else {
            super.onBackPressed();
        }

    }


    public void displayFragmentConnexions(){
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.fl_fragment, new ConnexionsFragment(),"TAG_CONNEXIONS");
        t.commit();
    }



    public void displayFragmentEditConnexion(Connexion connexion){
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        ConnexionFormFragment cf = new ConnexionFormFragment();
        cf.addConnexion(connexion);
        t.replace(R.id.fl_fragment, cf,"TAG_EDIT_CONNEXION");
        t.commit();
    }


    // Ouvre l'acrtivite avec le video GS
    public void runConnexion(Connexion c) {

        Intent intent = new Intent(MainActivity.this, DriveActivity.class);

        intent.putExtra(DriveActivity.KEY_UUID_CONNEXION, c.getId().toString());
        startActivity(intent);
    }
}

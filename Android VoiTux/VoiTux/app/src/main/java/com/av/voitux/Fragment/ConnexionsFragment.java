package com.av.voitux.Fragment;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.av.voitux.Activity.MainActivity;
import com.av.voitux.Adapter.ConnexionAdapter;
import com.av.voitux.Models.Connexion;
import com.av.voitux.Models.ConnexionsManager;
import com.av.voitux.voituxandroid.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thibaud on 23/09/16.
 */
public class ConnexionsFragment extends Fragment{

    private RecyclerView recyclerView;
    private ConnexionAdapter cAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_connexions, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {


        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_add_con);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                ((MainActivity)getActivity()).displayFragmentEditConnexion(null);


            }
        });


        List<Connexion> connexionList = ConnexionsManager.get(getContext()).getConnexions();

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        cAdapter = new ConnexionAdapter(getActivity(), connexionList);
        Log.d("VoiTUx", "onViewCreated: ");
        cAdapter.notifyDataSetChanged();

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager( getActivity() );
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(cAdapter);

        cAdapter.setOnItemClickListener(new ConnexionAdapter.OnItemClickListener() {
            @Override
            public void onBtnRunClick(View itemView, int position) {
                Connexion c = cAdapter.getConnexion(position);
                ((MainActivity)getActivity()).runConnexion(c);

            }

            @Override
            public void onBtnEditClick(View itemView, int position) {
                Connexion c = cAdapter.getConnexion(position);
                ((MainActivity)getActivity()).displayFragmentEditConnexion(c);

            }

            @Override
            public void onBtnDeleteClick(View itemView, int p) {
                final Connexion c = cAdapter.getConnexion(p);
                final Integer position = p;
                ConnexionsManager.get(getContext()).removeConnexion(c.getId());
                cAdapter.removeConnexion(position);
                Snackbar.make(getView(), ""+c.getNom()+" est supprimé.", Snackbar.LENGTH_LONG)
                        .setAction("ANNULER", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ConnexionsManager.get(getContext()).addConnexion(c);
                                cAdapter.addConnexion(c,position);
                                Snackbar snackbar1 = Snackbar.make(getView(), c.getNom()+" est restauré [o_O]", Snackbar.LENGTH_SHORT);
                                snackbar1.show();
                            }
                        })
                        .show();
            }
        });

    }

}

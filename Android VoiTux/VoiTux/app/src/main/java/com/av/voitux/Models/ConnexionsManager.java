package com.av.voitux.Models;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by thibaud on 20/09/16.
 */
public class ConnexionsManager {


    private List<Connexion> connexionList = new ArrayList<Connexion>();

    private static final String PREF_KEY_CONNEXION = "PREF_KEY_CONNEXION";

    private static ConnexionsManager instance;
    private Context context;

    public synchronized static ConnexionsManager get(Context context) {
        if (instance == null) {
            instance = new ConnexionsManager();
            instance.loadAllConnexion(context);
        }
        return instance;
    }


    public List<Connexion> getConnexions() {
        return connexionList;
    }

    public void saveAllConnexion() {
        SharedPreferences settings = context.getSharedPreferences(PREF_KEY_CONNEXION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        String connexionListJSONString = new Gson().toJson(connexionList);

        editor.putString(PREF_KEY_CONNEXION, connexionListJSONString);
        editor.commit();
        Log.d("VoiTux", "saveAllConnexion: "+connexionList.size());
    }
    public void loadAllConnexion(Context context) {
        this.context = context;
        SharedPreferences prefs = context.getSharedPreferences(PREF_KEY_CONNEXION, Context.MODE_PRIVATE);

        if (prefs.contains(PREF_KEY_CONNEXION)) {
            String connexionListJSONString = prefs.getString(PREF_KEY_CONNEXION, null);
            Gson gson = new Gson();
            connexionList = gson.fromJson(connexionListJSONString, new TypeToken<List<Connexion>>() {
            }.getType());
            Log.d("VoiTux", "loadAllConnexion: "+connexionList.size());
        }

    }
    public Connexion getConnexionById(UUID id) {
        for (Connexion c :connexionList) {
            if (c.getId().equals(id))
                return c;
        }
        return null;
    }

    public void addConnexion(Connexion connexion) {
        if(connexion.getId() == null) {
            connexion.setId(UUID.randomUUID());
            connexionList.add(connexion);
        } else {
            connexionList.add(connexion);
        }
        this.saveAllConnexion();
    }

    public void removeConnexion(UUID id) {
        connexionList.remove(getConnexionById(id));
        this.saveAllConnexion();
    }
}

package com.av.voitux.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.av.voitux.Activity.MainActivity;
import com.av.voitux.Models.Connexion;
import com.av.voitux.Models.ConnexionsManager;
import com.av.voitux.voituxandroid.R;

import java.util.regex.Pattern;

/**
 * Created by thibaud on 23/09/16.
 */
public class ConnexionFormFragment extends Fragment {


    private Connexion connexion;
    private CheckBox cb_nc_com_active, cb_nc_video_active;
    private EditText et_nc_com_port, et_nc_nom, et_nc_ip, et_nc_video_pipeline, et_nc_video_port;
    private LinearLayout ll_nc_video_mode, ll_nc_video_adresse;
    private RadioGroup rg_nc_video_mode;
    private RelativeLayout rl_nc_com_adresse;
    private TextInputLayout til_nc_com_port, til_nc_nom, til_nc_ip, til_nc_video_pipeline, til_nc_video_port;
    private Spinner spi_nc_select_pipeline;
    private Button btn_new_con;
    ArrayAdapter<CharSequence> spi_nc_select_p_adapter;

    public void addConnexion(Connexion connexion) {
        this.connexion = connexion;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_connexion_form, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        // Nom de la connexion
        til_nc_nom = (TextInputLayout) view.findViewById(R.id.til_nc_nom);
        et_nc_nom = (EditText) view.findViewById(R.id.et_nc_nom);
        til_nc_ip = (TextInputLayout) view.findViewById(R.id.til_nc_ip);
        et_nc_ip = (EditText) view.findViewById(R.id.et_nc_ip);

        //Card Video
        cb_nc_video_active = (CheckBox) view.findViewById(R.id.cb_nc_video_active);
        rg_nc_video_mode = (RadioGroup) view.findViewById(R.id.rg_nc_video_mode);
        ll_nc_video_mode = (LinearLayout) view.findViewById(R.id.ll_nc_video_mode);
        ll_nc_video_adresse = (LinearLayout) view.findViewById(R.id.ll_nc_video_adresse);
        spi_nc_select_pipeline = (Spinner) view.findViewById(R.id.spi_nc_select_pipeline);
        // Create an ArrayAdapter using the string array and a default spinner layout
        spi_nc_select_p_adapter = ArrayAdapter.createFromResource(
                                                        getContext(),
                                                        R.array.GS_Piepeline,
                                                        android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        spi_nc_select_p_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spi_nc_select_pipeline.setAdapter(spi_nc_select_p_adapter);

        til_nc_video_port = (TextInputLayout) view.findViewById(R.id.til_nc_video_port);
        et_nc_video_port = (EditText) view.findViewById(R.id.et_nc_video_port);
        til_nc_video_pipeline = (TextInputLayout) view.findViewById(R.id.til_nc_video_pipeline);
        et_nc_video_pipeline = (EditText) view.findViewById(R.id.et_nc_video_pipeline);

        // Card Commande Joystick
        cb_nc_com_active = (CheckBox) view.findViewById(R.id.cb_nc_com_active);
        rl_nc_com_adresse = (RelativeLayout) view.findViewById(R.id.rl_nc_com_adresse);
        til_nc_com_port = (TextInputLayout) view.findViewById(R.id.til_nc_com_port);
        et_nc_com_port = (EditText) view.findViewById(R.id.et_nc_com_port);

        // btn Valider
        btn_new_con = (Button) view.findViewById(R.id.btn_new_con);

        // recupere la connexion
        if (connexion == null)
            connexion = new Connexion();

        // init du nom et de l'ip de la connexion
        et_nc_nom.setText(connexion.getNom());
        et_nc_nom.addTextChangedListener(new MyTextWatcher(et_nc_nom, til_nc_nom));
        et_nc_ip.setText(connexion.getIp());
        et_nc_ip.addTextChangedListener(new MyTextWatcher(et_nc_ip, til_nc_ip));

        initVideoCard();

        initCommandeCard();

        btn_new_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitForm();
            }
        });
    }


    private void initVideoCard() {

        cb_nc_video_active.setChecked(connexion.isVideoAcive());
        activeVideoCard(connexion.isVideoAcive());

        rg_nc_video_mode.check(  connexion.isVideoModeSimple() ? R.id.rb_nc_video_simple : R.id.rb_nc_video_avance );
        modeVideoCard(connexion.isVideoModeSimple() ? R.id.rb_nc_video_simple : R.id.rb_nc_video_avance);

        et_nc_video_pipeline.setEnabled(connexion.isVideoModeSimple() ? false : true);
        et_nc_video_pipeline.setFocusableInTouchMode(connexion.isVideoModeSimple() ? false : true);
        et_nc_video_port.setText(connexion.getVideoPort());

        spi_nc_select_pipeline.setSelection(connexion.getVideoSelectPipeline());

        updatePiepline();

        // Active la Video
        cb_nc_video_active.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                activeVideoCard(b);
                modeVideoCard(connexion.isVideoModeSimple() ? R.id.rb_nc_video_simple : R.id.rb_nc_video_avance);
            }
        });

        // Changer de mode : Simple ou Aancé
        rg_nc_video_mode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                modeVideoCard(i);
                updatePiepline();
            }
        });

        // Quand on sélectionne une pipeline
        spi_nc_select_pipeline.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePiepline();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Gestions des erreurs
        et_nc_video_port.addTextChangedListener(new MyTextWatcher(et_nc_video_port, til_nc_video_port));
        et_nc_video_pipeline.addTextChangedListener(new MyTextWatcher(et_nc_video_pipeline, til_nc_video_pipeline));

    }


    private void modeVideoCard(int mode) {
        switch (mode) {
            case R.id.rb_nc_video_avance:
                til_nc_video_port.setVisibility(View.GONE);
                spi_nc_select_pipeline.setEnabled(false);
                spi_nc_select_pipeline.setVisibility(View.GONE);
                et_nc_video_port.setEnabled(false);
                et_nc_video_pipeline.setEnabled(true);
                et_nc_video_pipeline.setFocusableInTouchMode(true);

                break;
            case R.id.rb_nc_video_simple:
                til_nc_video_port.setVisibility(View.VISIBLE);
                spi_nc_select_pipeline.setVisibility(View.VISIBLE);
                spi_nc_select_pipeline.setEnabled(true);
                et_nc_video_port.setEnabled(true);
                et_nc_video_pipeline.setEnabled(false);
                et_nc_video_pipeline.setFocusableInTouchMode(false);
                break;

        }
    }
    private void activeVideoCard(boolean active) {

        ll_nc_video_mode.setVisibility(active ? View.VISIBLE : View.GONE);
        ll_nc_video_adresse.setVisibility(active ? View.VISIBLE : View.GONE);
        til_nc_video_pipeline.setVisibility(active ? View.VISIBLE : View.GONE);
        et_nc_video_pipeline.setVisibility(active ? View.VISIBLE : View.GONE);

    }



    private void initCommandeCard() {

        // Default
        cb_nc_com_active.setChecked(connexion.isCommandeActive());
        activeCommandeCard(connexion.isCommandeActive());
        et_nc_com_port.setText(connexion.getCommandePort());

        // Si on active la Video
        cb_nc_com_active.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                activeCommandeCard(b);
            }

        });

        et_nc_com_port.addTextChangedListener(new MyTextWatcher(et_nc_com_port, til_nc_com_port));

    }

    private void activeCommandeCard(boolean active) {
        rl_nc_com_adresse.setVisibility(active ? View.VISIBLE : View.GONE);
        et_nc_com_port.setEnabled(active);
    }



    private void submitForm() {
        // Focus si erreur
        if(!validateIsNotEmpty(et_nc_nom, til_nc_nom)
                && et_nc_nom.isEnabled()) {requestFocus(et_nc_nom); return; }

        if(!validateIp(et_nc_ip, til_nc_ip)
                && et_nc_ip.isEnabled()) {requestFocus(et_nc_ip); return; }

        if(!validatePort(et_nc_video_port, til_nc_video_port)
                && et_nc_video_port.isEnabled()
                && cb_nc_video_active.isChecked()) {requestFocus(et_nc_video_port); return; }

        if(!validateIsNotEmpty(et_nc_video_pipeline, til_nc_video_pipeline)
                && et_nc_video_pipeline.isEnabled()
                && cb_nc_video_active.isChecked()) {requestFocus(et_nc_video_pipeline); return; }

        if(!validatePort(et_nc_com_port, til_nc_com_port)
                && et_nc_com_port.isEnabled()
                && cb_nc_com_active.isChecked()) {requestFocus(et_nc_com_port); return; }

        // edit de la connexion
        connexion.setNom(et_nc_nom.getText().toString());
        connexion.setIp(et_nc_ip.getText().toString());
        connexion.setVideoAcive(cb_nc_video_active.isChecked());
        connexion.setVideoModeSimple(rg_nc_video_mode.getCheckedRadioButtonId() == R.id.rb_nc_video_simple ? true : false);
        connexion.setVideoPort(et_nc_video_port.getText().toString());
        connexion.setVideoGSpipeline(et_nc_video_pipeline.getText().toString());
        connexion.setVideoSelectPipeline(spi_nc_select_pipeline.getSelectedItemPosition());

        connexion.setCommandeActive(cb_nc_com_active.isChecked());
        connexion.setCommandePort(et_nc_com_port.getText().toString());

        // hide keyboard avant de changer de fragment
        InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

        if(connexion.getId() == null) {
            ConnexionsManager.get(getContext()).addConnexion(connexion);
            Snackbar.make(getView(), connexion.getNom()+" est ajouté.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } else {
            Snackbar.make(getView(), connexion.getNom()+" est modifié.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            ConnexionsManager.get(getContext()).saveAllConnexion();
        }

        ((MainActivity)getActivity()).displayFragmentConnexions();



    }


    // Verifie les erreurs de saisie
    private class MyTextWatcher implements TextWatcher {


        private final EditText editeTexte;
        private final TextInputLayout texteLayout;

        private MyTextWatcher(EditText editeTexte, TextInputLayout texteLayout) {
            this.editeTexte = editeTexte;
            this.texteLayout = texteLayout;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            if(editeTexte.isEnabled()) {
                switch (editeTexte.getId()) {

                    case R.id.et_nc_nom:
                        validateIsNotEmpty(editeTexte, texteLayout);
                        break;
                    case R.id.et_nc_ip:
                        validateIp(editeTexte, texteLayout);
                        updatePiepline();
                        break;
                    case R.id.et_nc_video_port:
                        validatePort(editeTexte, texteLayout);
                        updatePiepline();
                        break;
                    case R.id.et_nc_com_port:
                        validatePort(editeTexte, texteLayout);
                        break;
                    case R.id.et_nc_video_pipeline:
                        validateIsNotEmpty(et_nc_video_pipeline, til_nc_video_pipeline);
                        break;
                }
            }
        }
    }

    private void requestFocus(View view) {
        Log.d("Log", "requestFocus: "+view.getId()+" "+view.isEnabled());
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        }
    }

    // mets a jours la preview du piepeline en fonction du mode
    private void updatePiepline() {
        String s = "";
        if(rg_nc_video_mode.getCheckedRadioButtonId() == R.id.rb_nc_video_simple ? true : false) {
            s = (String) spi_nc_select_p_adapter.getItem(spi_nc_select_pipeline.getSelectedItemPosition());
            s = s.replace("%videoIp%",et_nc_ip.getText());
            s = s.replace("%videoPort%",et_nc_video_port.getText());
        } else {
            if(connexion.getVideoGSpipeline().equals("")) {
                s = String.format(getResources().getString(R.string.video_pipeline_defaut), et_nc_ip.getText(), et_nc_video_port.getText());
            } else {
                s = connexion.getVideoGSpipeline();
            }

        }

        et_nc_video_pipeline.setText( s );
    }

    private boolean validatePort(EditText inputVideoPort, TextInputLayout inputLayoutVideoPort) {
        String port = inputVideoPort.getText().toString().trim();
        if (!Pattern.compile("([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])").matcher(port).matches()) {
            inputLayoutVideoPort.setError(getString(R.string.err_msg_port));
            return false;
        } else {
            inputLayoutVideoPort.setError(null);
        }
        return true;
    }

    private boolean validateIp(EditText inputVideoIp, TextInputLayout inputLayoutVideoIp) {
        String ip = inputVideoIp.getText().toString().trim();

        if (!Patterns.IP_ADDRESS.matcher(ip).matches()) {
            inputLayoutVideoIp.setError(getString(R.string.err_msg_ip));
            return false;
        } else {
            inputLayoutVideoIp.setError(null);
        }
        return true;
    }

    private boolean validateIsNotEmpty(EditText inputVideoPort, TextInputLayout inputLayoutVideoPort) {
        String pipeline = inputVideoPort.getText().toString().trim();
        if (pipeline.isEmpty()) {
            inputLayoutVideoPort.setError(getString(R.string.err_msg_isEmpty));
            return false;
        } else {
            inputLayoutVideoPort.setError(null);
        }
        return true;
    }

}

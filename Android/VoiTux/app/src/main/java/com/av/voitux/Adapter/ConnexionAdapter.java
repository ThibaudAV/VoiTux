package com.av.voitux.Adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.av.voitux.Models.Connexion;
import com.av.voitux.voituxandroid.R;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

/**
 * Created by thibaud on 17/09/16.
 */
public class ConnexionAdapter extends RecyclerView.Adapter<ConnexionAdapter.MyViewHolder> {

    private List<Connexion> connexionList;

    // Define listener member variable
    private OnItemClickListener listener;

    ColorGenerator generator = ColorGenerator.MATERIAL;


    // Define the listener interface
    public interface OnItemClickListener {
        void onBtnRunClick(View itemView, int position);
        void onBtnEditClick(View itemView, int position);
        void onBtnDeleteClick(View itemView, int position);
    }
    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public ConnexionAdapter(Context context, List<Connexion> moviesList) {
        this.connexionList = moviesList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_connexion, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Connexion connexion = getConnexion(position);
        holder.tv_co_nom.setText(connexion.getNom());

        // Video
        if(connexion.isVideoAcive()) {
            holder.showVideo(true);
            if (connexion.isVideoModeSimple()) {
                holder.tv_co_video_info.setText("Adresse: "+connexion.getIp()+":"+connexion.getVideoPort());

            } else {
                holder.tv_co_video_info.setText("GS Pipeline: "+connexion.getVideoGSpipeline());
            }
        } else {
            holder.showVideo(false);
        }

        //Commande

        if(connexion.isCommandeActive()) {
            holder.showCommande(true);
            holder.tv_co_commande_info.setText("Adresse: "+connexion.getIp()+":"+connexion.getCommandePort());
        } else {
            holder.showCommande(false);
        }


        String letter = String.valueOf(connexionList.get(position).getNom().charAt(0));
        TextDrawable drawable = TextDrawable.builder()
                                .buildRect(letter, generator.getRandomColor());
        holder.iv_co_avatar.setImageDrawable(drawable);
    }

    @Override
    public int getItemCount() {
        return connexionList.size();
    }


    public List<Connexion> getConnexionList() {
        return connexionList;
    }

    public Connexion getConnexion(int position) {
        return connexionList.get(position);
    }

    public void setConnexionList(List<Connexion> connexionList) {
        this.connexionList = connexionList;
        this.notifyDataSetChanged();
    }

    public void addConnexion(Connexion connexion,Integer position) {
        this.notifyItemInserted(position);
    }

    public void removeConnexion(int position) {
        this.notifyItemRemoved(position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public Button btn_co_delete, btn_co_run, btn_co_edit;
        public CardView cv_connexion;
        public LinearLayout ll_co_footer, ll_co_header;
        public ImageView iv_co_expand;
        public TextView tv_co_nom, tv_co_video_info, tv_co_commande_info, tv_co_titre_commande, tv_co_titre_video;
        public ImageView iv_co_avatar;
        public RelativeLayout rl_co_content;

        private IconicsDrawable draw_expand_less, draw_expand_more;

        private boolean activeContent = false;

        public MyViewHolder(View view) {
            super(view);

            tv_co_nom = (TextView) view.findViewById(R.id.tv_co_nom);
            tv_co_titre_commande = (TextView) view.findViewById(R.id.tv_co_titre_commande);
            tv_co_titre_video = (TextView) view.findViewById(R.id.tv_co_titre_video);
            tv_co_video_info = (TextView) view.findViewById(R.id.tv_co_video_info);
            tv_co_commande_info = (TextView) view.findViewById(R.id.tv_co_commande_info);
            iv_co_expand = (ImageView) view.findViewById(R.id.iv_co_expand);
            iv_co_avatar = (ImageView) view.findViewById(R.id.iv_co_avatar);
            rl_co_content = (RelativeLayout) view.findViewById(R.id.rl_co_content);
            ll_co_footer = (LinearLayout) view.findViewById(R.id.ll_co_footer);
            ll_co_header = (LinearLayout) view.findViewById(R.id.ll_co_header);
            btn_co_delete = (Button) view.findViewById(R.id.btn_co_delete);
            btn_co_edit = (Button) view.findViewById(R.id.btn_co_edit);
            btn_co_run = (Button) view.findViewById(R.id.btn_co_run);


            draw_expand_less = new IconicsDrawable(view.getContext())
                    .icon(GoogleMaterial.Icon.gmd_expand_less)
                    .colorRes(R.color.greyLight);

            draw_expand_more = new IconicsDrawable(view.getContext())
                    .icon(GoogleMaterial.Icon.gmd_expand_more)
                    .colorRes(R.color.greyLight);

            //init
            showConnexionContent(false);


            ll_co_header.setOnClickListener(this);
            btn_co_run.setOnClickListener(this);
            btn_co_edit.setOnClickListener(this);
            btn_co_delete.setOnClickListener(this);

        }



        private void showConnexionContent(boolean active) {
            rl_co_content.setVisibility(active ? View.VISIBLE : View.GONE);
            ll_co_footer.setVisibility(active ? View.VISIBLE : View.GONE);
            if(active) {
                iv_co_expand.setImageDrawable(draw_expand_less);
            } else {
                iv_co_expand.setImageDrawable(draw_expand_more);
            }
        }


        public void showVideo(boolean active) {
            tv_co_titre_video.setVisibility(active ? View.VISIBLE : View.GONE);
            tv_co_video_info.setVisibility(active ? View.VISIBLE : View.GONE);

        }

        public void showCommande(boolean active) {
            tv_co_titre_commande.setVisibility(active ? View.VISIBLE : View.GONE);
            tv_co_commande_info.setVisibility(active ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {

                case R.id.ll_co_header:
                    activeContent = !activeContent;
                    showConnexionContent(activeContent);
                    break;
                case R.id.btn_co_delete:
                    if (listener != null)
                        listener.onBtnDeleteClick(view, getLayoutPosition());
                    break;
                case R.id.btn_co_edit:
                    if (listener != null)
                        listener.onBtnEditClick(view, getLayoutPosition());
                    break;

                case R.id.btn_co_run:
                    if (listener != null)
                        listener.onBtnRunClick(view, getLayoutPosition());
                    break;


            }
        }
    }

}

package com.av.voitux.Models;

import java.util.UUID;

/**
 * Created by thibaud on 17/09/16.
 */
public class Connexion {
    private UUID id;

    private String nom                  = "";
    private String ip                   = "";

    private boolean videoAcive          = true;
    private boolean videoModeSimple     = true;
    private String videoPort            = "";
    private String videoGSpipeline      = "";
    private Integer videoSelectPipeline = 0;

    private boolean commandeActive      = true;
    private String commandePort         = "";

    public Connexion() {

    }

    public Connexion(String nom) {
        this.nom = nom;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public boolean isVideoAcive() {
        return videoAcive;
    }

    public void setVideoAcive(boolean videoAcive) {
        this.videoAcive = videoAcive;
    }

    public boolean isVideoModeSimple() {
        return videoModeSimple;
    }

    public void setVideoModeSimple(boolean videoModeSimple) {
        this.videoModeSimple = videoModeSimple;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getVideoPort() {
        return videoPort;
    }

    public void setVideoPort(String videoPort) {
        this.videoPort = videoPort;
    }

    public String getVideoGSpipeline() {
        return videoGSpipeline;
    }

    public void setVideoGSpipeline(String videoGSpipeline) {
        this.videoGSpipeline = videoGSpipeline;
    }

    public boolean isCommandeActive() {
        return commandeActive;
    }

    public void setCommandeActive(boolean commandeActive) {
        this.commandeActive = commandeActive;
    }

    public String getCommandePort() {
        return commandePort;
    }

    public void setCommandePort(String commandePort) {
        this.commandePort = commandePort;
    }

    public Integer getVideoSelectPipeline() {
        return videoSelectPipeline;
    }

    public void setVideoSelectPipeline(Integer videoSelectPipeline) {
        this.videoSelectPipeline = videoSelectPipeline;
    }
}

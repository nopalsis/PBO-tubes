package com.smarthome.domain;

/**
 * 6. PanelSirkuitRuang
 * Komponen sirkuit proteksi lokal tiap ruangan.
 * Digunakan secara KOMPOSISI oleh Ruangan (siklus hidupnya terikat pada Ruangan).
 */
public class PanelSirkuitRuang {
    private double batasDaya; // Batas daya aman sirkuit (Watt)
    private boolean statusAktif;

    public PanelSirkuitRuang(double batasDaya) {
        this.batasDaya = batasDaya;
        this.statusAktif = true;
    }

    public boolean periksaOverload(double totalDaya) {
        return statusAktif && (totalDaya > batasDaya);
    }

    public double getBatasDaya() {
        return batasDaya;
    }

    public void setBatasDaya(double batasDaya) {
        this.batasDaya = batasDaya;
    }

    public boolean isStatusAktif() {
        return statusAktif;
    }

    public void nonaktifkan() {
        this.statusAktif = false;
    }

    @Override
    public String toString() {
        return String.format("PanelSirkuit(Batas: %.1fW, Status: %s)",
                batasDaya, statusAktif ? "ONLINE" : "OFFLINE/HANCUR");
    }
}

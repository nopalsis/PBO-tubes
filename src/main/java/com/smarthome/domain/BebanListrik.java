package com.smarthome.domain;

/**
 * 1. BebanListrik (Abstract Class)
 * Mewakili konsep dasar beban listrik hunian pintar (Pilar: Abstraksi).
 */
public abstract class BebanListrik {
    private static int idCounter = 1;

    protected int id;
    protected String nama;
    protected double dayaNominal;
    protected boolean aktif;
    protected int prioritas; // 1 = tinggi (kritis), 2 = sedang, 3 = rendah (kandidat shedding)

    public BebanListrik(String nama, double dayaNominal, int prioritas) {
        this.id = idCounter++;
        this.nama = nama;
        this.dayaNominal = dayaNominal;
        this.prioritas = prioritas;
        this.aktif = false;
    }

    public BebanListrik(int id, String nama, double dayaNominal, int prioritas, boolean aktif) {
        this.id = id;
        this.nama = nama;
        this.dayaNominal = dayaNominal;
        this.prioritas = prioritas;
        this.aktif = aktif;
        if (id >= idCounter) {
            idCounter = id + 1;
        }
    }

    // Abstract Methods
    public abstract double hitungDayaAktif();
    public abstract void nyalakan();
    public abstract void matikan();
    public abstract String getTipePerangkat();

    // Standard Getters
    public int getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public double getDayaNominal() {
        return dayaNominal;
    }

    public boolean isAktif() {
        return aktif;
    }

    public int getPrioritas() {
        return prioritas;
    }

    public void setPrioritas(int prioritas) {
        this.prioritas = prioritas;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public void setDayaNominal(double dayaNominal) {
        this.dayaNominal = dayaNominal;
    }

    @Override
    public String toString() {
        return String.format("[%s #%d] %s (Nominal: %.1fW, Status: %s, Prioritas: %d)",
                getTipePerangkat(), id, nama, dayaNominal, aktif ? "ON" : "OFF", prioritas);
    }
}

package com.smarthome.domain;

/**
 * 4. WaterHeater (extends BebanListrik)
 * Beban listrik berdaya besar dengan prioritas rendah (3), kandidat utama load shedding.
 */
public class WaterHeater extends BebanListrik {
    private boolean sedangMemanaskan;

    public WaterHeater(String nama, double dayaNominal, int prioritas, boolean sedangMemanaskan) {
        super(nama, dayaNominal, prioritas);
        this.sedangMemanaskan = sedangMemanaskan;
    }

    public WaterHeater(String nama, double dayaNominal) {
        this(nama, dayaNominal, 3, false); // Default prioritas 3 (rendah)
    }

    public boolean isSedangMemanaskan() {
        return sedangMemanaskan;
    }

    public void setSedangMemanaskan(boolean sedangMemanaskan) {
        this.sedangMemanaskan = sedangMemanaskan;
    }

    @Override
    public double hitungDayaAktif() {
        if (!aktif) {
            return 0.0;
        }
        // Jika sedang aktif memanaskan, menggunakan daya nominal penuh. Jika standby, 5% daya.
        return sedangMemanaskan ? dayaNominal : (dayaNominal * 0.05);
    }

    @Override
    public void nyalakan() {
        this.aktif = true;
        this.sedangMemanaskan = true;
    }

    @Override
    public void matikan() {
        this.aktif = false;
        this.sedangMemanaskan = false;
    }

    @Override
    public String getTipePerangkat() {
        return "WaterHeater";
    }
}

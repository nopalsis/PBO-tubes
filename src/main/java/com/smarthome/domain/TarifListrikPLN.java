package com.smarthome.domain;

/**
 * 8. TarifListrikPLN
 * Digunakan secara ASOSIASI oleh SmartHomeManager untuk menghitung estimasi biaya listrik.
 */
public class TarifListrikPLN {
    private int golonganDaya;        // Contoh: 900, 1300, 2200, 3500 VA
    private double tarifPerKwh;      // Contoh: Rp 1.444,70 / kWh
    private double faktorBebanPuncak; // Contoh: 1.5 (Waktu Beban Puncak / WBP)

    public TarifListrikPLN(int golonganDaya, double tarifPerKwh, double faktorBebanPuncak) {
        this.golonganDaya = golonganDaya;
        this.tarifPerKwh = tarifPerKwh;
        this.faktorBebanPuncak = faktorBebanPuncak;
    }

    public TarifListrikPLN(int golonganDaya) {
        this.golonganDaya = golonganDaya;
        switch (golonganDaya) {
            case 900:
                this.tarifPerKwh = 1352.0;
                break;
            case 1300:
            case 2200:
                this.tarifPerKwh = 1444.70;
                break;
            case 3500:
            case 5500:
            case 6600:
                this.tarifPerKwh = 1699.53;
                break;
            default:
                this.tarifPerKwh = 1444.70;
                break;
        }
        this.faktorBebanPuncak = 1.25; // 25% pengali beban puncak
    }

    /**
     * Asosiasi: Menghitung biaya pemakaian listrik berdasarkan kWh dan kondisi jam puncak.
     */
    public double hitungBiaya(double kwh, boolean jamPuncak) {
        double faktor = jamPuncak ? faktorBebanPuncak : 1.0;
        return kwh * tarifPerKwh * faktor;
    }

    public int getGolonganDaya() {
        return golonganDaya;
    }

    public void setGolonganDaya(int golonganDaya) {
        this.golonganDaya = golonganDaya;
    }

    public double getTarifPerKwh() {
        return tarifPerKwh;
    }

    public void setTarifPerKwh(double tarifPerKwh) {
        this.tarifPerKwh = tarifPerKwh;
    }

    public double getFaktorBebanPuncak() {
        return faktorBebanPuncak;
    }

    public void setFaktorBebanPuncak(double faktorBebanPuncak) {
        this.faktorBebanPuncak = faktorBebanPuncak;
    }
}

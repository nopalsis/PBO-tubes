package com.smarthome.domain;

/**
 * 2. LampuCerdas (extends BebanListrik)
 * Perangkat lampu pintar yang mendukung pengaturan kecerahan (dimmer).
 */
public class LampuCerdas extends BebanListrik {
    private int tingkatKecerahan; // 0 - 100%

    public LampuCerdas(String nama, double dayaNominal, int prioritas, int tingkatKecerahan) {
        super(nama, dayaNominal, prioritas);
        setKecerahan(tingkatKecerahan);
    }

    public LampuCerdas(String nama, double dayaNominal, int prioritas) {
        this(nama, dayaNominal, prioritas, 100);
    }

    public int getTingkatKecerahan() {
        return tingkatKecerahan;
    }

    public void setKecerahan(int nilai) {
        if (nilai < 0 || nilai > 100) {
            throw new IllegalArgumentException("Tingkat kecerahan harus dalam rentang 0-100%! Diberikan: " + nilai);
        }
        this.tingkatKecerahan = nilai;
    }

    @Override
    public double hitungDayaAktif() {
        if (!aktif) {
            return 0.0;
        }
        return dayaNominal * (tingkatKecerahan / 100.0);
    }

    @Override
    public void nyalakan() {
        this.aktif = true;
    }

    @Override
    public void matikan() {
        this.aktif = false;
    }

    @Override
    public String getTipePerangkat() {
        return "LampuCerdas";
    }
}

package com.smarthome.domain;

/**
 * 3. AirConditioner (extends BebanListrik)
 * Menerapkan ENKAPSULASI ketat: setelan suhu hanya valid pada 16°C – 30°C.
 * Daya aktif meningkat proporsional saat suhu disetel lebih dingin.
 */
public class AirConditioner extends BebanListrik {
    private int suhuSetelan; // default 24°C

    public AirConditioner(String nama, double dayaNominal, int prioritas, int suhuSetelan) {
        super(nama, dayaNominal, prioritas);
        setSuhu(suhuSetelan);
    }

    public AirConditioner(String nama, double dayaNominal, int prioritas) {
        this(nama, dayaNominal, prioritas, 24);
    }

    public int getSuhuSetelan() {
        return suhuSetelan;
    }

    /**
     * Enkapsulasi ketat: hanya menerima 16–30°C.
     * Jika di luar rentang, lemparkan IllegalArgumentException (dan kembalikan false jika ditangkap).
     */
    public boolean setSuhu(int nilai) {
        if (nilai < 16 || nilai > 30) {
            throw new IllegalArgumentException("[ENKAPSULASI] Suhu AC harus berada dalam rentang 16°C sampai 30°C! Nilai ditolak: " + nilai + "°C");
        }
        this.suhuSetelan = nilai;
        return true;
    }

    /**
     * Daya aktif meningkat saat suhu disetel lebih dingin:
     * Baseline 24°C = 100% dayaNominal.
     * Tiap 1°C di bawah 24°C menambah beban 5% (+40% pada 16°C).
     * Tiap 1°C di atas 24°C mengurangi beban 3% (-18% pada 30°C).
     */
    @Override
    public double hitungDayaAktif() {
        if (!aktif) {
            return 0.0;
        }
        double faktor = 1.0 + (24 - suhuSetelan) * 0.05;
        return dayaNominal * Math.max(0.5, faktor);
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
        return "AirConditioner";
    }
}

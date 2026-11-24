package com.smarthome.domain;

/**
 * 5. InverterSurya (extends BebanListrik)
 * Sumber pembangkit listrik surya mandiri.
 * Menghasilkan daya aktif bernilai NEGATIF saat aktif untuk mereduksi beban total hunian.
 */
public class InverterSurya extends BebanListrik {
    private double kapasitasPuncak;  // Watt-peak (Wp)
    private double tingkatIrradiasi; // 0.0 - 1.0 (proporsi intensitas sinar matahari)

    public InverterSurya(String nama, double kapasitasPuncak, int prioritas, double tingkatIrradiasi) {
        super(nama, kapasitasPuncak, prioritas);
        this.kapasitasPuncak = kapasitasPuncak;
        setTingkatIrradiasi(tingkatIrradiasi);
    }

    public InverterSurya(String nama, double kapasitasPuncak) {
        this(nama, kapasitasPuncak, 1, 0.8); // Default prioritas tinggi (1), irradiasi 80%
    }

    public double getKapasitasPuncak() {
        return kapasitasPuncak;
    }

    public void setKapasitasPuncak(double kapasitasPuncak) {
        this.kapasitasPuncak = kapasitasPuncak;
        this.dayaNominal = kapasitasPuncak;
    }

    public double getTingkatIrradiasi() {
        return tingkatIrradiasi;
    }

    public void setTingkatIrradiasi(double tingkatIrradiasi) {
        if (tingkatIrradiasi < 0.0 || tingkatIrradiasi > 1.0) {
            throw new IllegalArgumentException("Tingkat irradiasi harus dalam rentang 0.0 - 1.0! Diberikan: " + tingkatIrradiasi);
        }
        this.tingkatIrradiasi = tingkatIrradiasi;
    }

    /**
     * Mengembalikan nilai NEGATIF saat aktif untuk mengurangi beban netto hunian.
     */
    @Override
    public double hitungDayaAktif() {
        if (!aktif) {
            return 0.0;
        }
        return -(kapasitasPuncak * tingkatIrradiasi);
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
        return "InverterSurya";
    }
}

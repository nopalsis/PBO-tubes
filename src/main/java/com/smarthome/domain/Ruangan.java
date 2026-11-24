package com.smarthome.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 7. Ruangan
 * Menerapkan KOMPOSISI terhadap PanelSirkuitRuang (dibuat di konstruktor & musnah bersama).
 * Menerapkan AGREGASI terhadap BebanListrik (perangkat bisa ditambah, dilepas, dan tetap hidup).
 */
public class Ruangan {
    private static int idCounter = 1;

    private int id;
    private String nama;
    private PanelSirkuitRuang panel;            // KOMPOSISI
    private List<BebanListrik> daftarPerangkat; // AGREGASI

    public Ruangan(String nama, double batasDayaPanel) {
        this.id = idCounter++;
        this.nama = nama;
        // KOMPOSISI: PanelSirkuitRuang wajib diinstansiasi di dalam konstruktor Ruangan
        this.panel = new PanelSirkuitRuang(batasDayaPanel);
        this.daftarPerangkat = new ArrayList<>();
    }

    public Ruangan(int id, String nama, double batasDayaPanel) {
        this.id = id;
        this.nama = nama;
        this.panel = new PanelSirkuitRuang(batasDayaPanel);
        this.daftarPerangkat = new ArrayList<>();
        if (id >= idCounter) {
            idCounter = id + 1;
        }
    }

    /**
     * KOMPOSISI: Menghancurkan ruangan.
     * PanelSirkuitRuang dinonaktifkan dan di-null-kan (ikut hancur).
     * Mengembalikan daftar perangkat di dalamnya agar dapat diselamatkan (AGREGASI).
     */
    public List<BebanListrik> hancurkan() {
        if (this.panel != null) {
            this.panel.nonaktifkan();
            this.panel = null;
        }
        List<BebanListrik> diselamatkan = new ArrayList<>(this.daftarPerangkat);
        this.daftarPerangkat.clear();
        return diselamatkan;
    }

    /**
     * AGREGASI: Menambahkan perangkat ke ruangan.
     */
    public void tambahPerangkat(BebanListrik b) {
        if (b != null && !daftarPerangkat.contains(b)) {
            daftarPerangkat.add(b);
        }
    }

    /**
     * AGREGASI: Melepas perangkat dari ruangan tanpa merusaknya.
     */
    public BebanListrik lepasPerangkat(int idPerangkat) {
        for (int i = 0; i < daftarPerangkat.size(); i++) {
            if (daftarPerangkat.get(i).getId() == idPerangkat) {
                return daftarPerangkat.remove(i);
            }
        }
        return null;
    }

    /**
     * POLIMORFISME: Menghitung total daya ruangan dalam satu loop polimorfik.
     */
    public double hitungDayaRuangan() {
        double total = 0.0;
        for (BebanListrik b : daftarPerangkat) {
            total += b.hitungDayaAktif();
        }
        return total;
    }

    public boolean isOverload() {
        return panel != null && panel.periksaOverload(hitungDayaRuangan());
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public PanelSirkuitRuang getPanel() {
        return panel;
    }

    public List<BebanListrik> getDaftarPerangkat() {
        return Collections.unmodifiableList(daftarPerangkat);
    }
}

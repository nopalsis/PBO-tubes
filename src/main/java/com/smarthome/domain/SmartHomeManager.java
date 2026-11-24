package com.smarthome.domain;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 9. SmartHomeManager
 * Kelas pengendali utama (Fasade) sistem automasi rumah pintar.
 * Mengelola ruangan, perangkat, tarif PLN, dan algoritma Load Shedding.
 */
public class SmartHomeManager {
    private List<Ruangan> daftarRuangan;
    private List<BebanListrik> perangkatBebas; // Menampung perangkat yatim saat ruangannya dihapus (Agregasi)
    private TarifListrikPLN tarif;             // Asosiasi
    private List<String> eventLogs;            // Audit log siklus hidup objek
    private double batasTotalRumah;            // Ambang batas total daya aman hunian (Watt)

    public SmartHomeManager(TarifListrikPLN tarif, double batasTotalRumah) {
        this.daftarRuangan = new ArrayList<>();
        this.perangkatBebas = new ArrayList<>();
        this.tarif = tarif != null ? tarif : new TarifListrikPLN(2200);
        this.eventLogs = new ArrayList<>();
        this.batasTotalRumah = batasTotalRumah > 0 ? batasTotalRumah : 2200.0;
        catatLog("SISTEM", "SmartHomeManager diinisiasi. Batas total rumah: " + this.batasTotalRumah + "W.");
    }

    public SmartHomeManager() {
        this(new TarifListrikPLN(2200), 2200.0);
    }

    /**
     * Pencatatan log siklus hidup objek (Audit Real-time)
     */
    public void catatLog(String tag, String pesan) {
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        String entry = String.format("[%s] [%s] %s", timestamp, tag.toUpperCase(), pesan);
        eventLogs.add(0, entry); // Log terbaru di awal
        if (eventLogs.size() > 100) {
            eventLogs.remove(eventLogs.size() - 1);
        }
    }

    /**
     * Daftarkan ruangan baru ke dalam sistem.
     */
    public void daftarkanRuangan(Ruangan r) {
        if (r != null && !daftarRuangan.contains(r)) {
            daftarRuangan.add(r);
            double batasPanel = (r.getPanel() != null) ? r.getPanel().getBatasDaya() : 0.0;
            catatLog("KOMPOSISI", "Ruangan '" + r.getNama() + "' dibuat dan PanelSirkuitRuang (Batas: " + batasPanel + "W) diinstansiasi secara Komposisi.");
        }
    }

    /**
     * Menghapus ruangan dari sistem.
     * Memicu demonstrasi KOMPOSISI (panel hancur) & AGREGASI (perangkat selamat ke pool bebas).
     */
    public boolean hapusRuangan(int idRuangan) {
        Ruangan target = findRuanganById(idRuangan);
        if (target == null) {
            return false;
        }

        String namaRuangan = target.getNama();
        // KOMPOSISI: panel hancur bersama ruangan
        List<BebanListrik> diselamatkan = target.hancurkan();
        catatLog("KOMPOSISI", "PanelSirkuitRuang milik \"" + namaRuangan + "\" ikut hancur bersama Ruangan.");

        // AGREGASI: perangkat tetap hidup dan dipindahkan ke pool bebas
        perangkatBebas.addAll(diselamatkan);
        catatLog("AGREGASI", diselamatkan.size() + " perangkat dari \"" + namaRuangan + "\" tetap hidup dan dipindah ke pool bebas.");

        daftarRuangan.remove(target);
        return true;
    }

    /**
     * Menghitung total daya seluruh perangkat aktif di semua ruangan
     * dalam SATU loop polimorfik atas BebanListrik tanpa instanceof atau type-checking.
     */
    public double hitungDayaTotal() {
        double total = 0.0;
        // Satu loop polimorfik melintasi seluruh ruangan dan koleksi BebanListrik
        for (Ruangan r : daftarRuangan) {
            for (BebanListrik b : r.getDaftarPerangkat()) {
                total += b.hitungDayaAktif(); // POLIMORFISME MURNI
            }
        }
        // Ditambah perangkat bebas jika ada yang aktif
        for (BebanListrik b : perangkatBebas) {
            total += b.hitungDayaAktif(); // POLIMORFISME MURNI
        }
        return total;
    }

    /**
     * Memeriksa apakah terjadi overload pada total hunian atau ada ruangan yang overload.
     */
    public boolean isOverload() {
        if (hitungDayaTotal() > batasTotalRumah) {
            return true;
        }
        for (Ruangan r : daftarRuangan) {
            if (r.isOverload()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Algoritma Load Shedding:
     * Jika terjadi kelebihan beban (overload) pada tingkat ruangan maupun total rumah,
     * sistem secara otomatis mematikan perangkat yang menyala
     * mulai dari prioritas terendah (angka prioritas terbesar: 3 -> 2 -> 1)
     * hingga beban kembali di bawah ambang batas aman.
     */
    public List<String> jalankanLoadShedding() {
        List<String> dimatikan = new ArrayList<>();
        catatLog("LOADSHEDDING", "Memulai protokol perlindungan kelebihan beban (Load Shedding Protocol)...");

        // Periksa apakah ada overload
        if (!isOverload()) {
            catatLog("LOADSHEDDING", "Pemeriksaan selesai: Seluruh sirkuit dan beban total dalam batas aman (" + hitungDayaTotal() + "W / " + batasTotalRumah + "W).");
            return dimatikan;
        }

        // Kumpulkan semua perangkat yang sedang AKTIF
        List<BebanListrik> kandidatAktif = new ArrayList<>();
        for (Ruangan r : daftarRuangan) {
            for (BebanListrik b : r.getDaftarPerangkat()) {
                // Inverter surya bernilai negatif, jangan matikan pembangkit surya!
                if (b.isAktif() && b.hitungDayaAktif() > 0) {
                    kandidatAktif.add(b);
                }
            }
        }
        for (BebanListrik b : perangkatBebas) {
            if (b.isAktif() && b.hitungDayaAktif() > 0) {
                kandidatAktif.add(b);
            }
        }

        // Urutkan prioritas terendah dahulu (angka 3 -> 2 -> 1).
        // Jika prioritas sama, matikan yang watt-nya paling besar terlebih dahulu.
        kandidatAktif.sort(Comparator.comparingInt(BebanListrik::getPrioritas).reversed()
                .thenComparing(Comparator.comparingDouble(BebanListrik::hitungDayaAktif).reversed()));

        for (BebanListrik b : kandidatAktif) {
            if (!isOverload()) {
                break; // Beban sudah aman
            }
            double dayaSebelum = b.hitungDayaAktif();
            b.matikan();
            String info = String.format("%s (ID: %d, Tipe: %s, Prioritas: %d, Daya: %.0fW)",
                    b.getNama(), b.getId(), b.getTipePerangkat(), b.getPrioritas(), dayaSebelum);
            dimatikan.add(info);
            catatLog("LOADSHEDDING", "Cut-off Otomatis: " + info + " dimatikan untuk meredakan beban.");
        }

        catatLog("LOADSHEDDING", "Selesai: " + dimatikan.size() + " perangkat berhasil dimatikan. Total beban terkini: " + hitungDayaTotal() + "W (Status: " + (isOverload() ? "MASIH OVERLOAD" : "AMAN") + ").");
        return dimatikan;
    }

    /**
     * Pindahkan perangkat antar ruangan atau dari/ke perangkatBebas.
     * @param idPerangkat ID perangkat
     * @param idRuangTujuan ID ruangan target (<= 0 berarti dilepas ke pool bebas)
     */
    public boolean pindahkanPerangkat(int idPerangkat, int idRuangTujuan) {
        BebanListrik targetPerangkat = null;

        // Cari di pool bebas
        for (int i = 0; i < perangkatBebas.size(); i++) {
            if (perangkatBebas.get(i).getId() == idPerangkat) {
                targetPerangkat = perangkatBebas.remove(i);
                break;
            }
        }

        // Cari di ruangan-ruangan
        if (targetPerangkat == null) {
            for (Ruangan r : daftarRuangan) {
                targetPerangkat = r.lepasPerangkat(idPerangkat);
                if (targetPerangkat != null) {
                    break;
                }
            }
        }

        if (targetPerangkat == null) {
            return false;
        }

        // Masukkan ke tujuan
        if (idRuangTujuan <= 0) {
            perangkatBebas.add(targetPerangkat);
            catatLog("AGREGASI", "Perangkat '" + targetPerangkat.getNama() + "' dipindahkan ke Pool Bebas.");
            return true;
        } else {
            Ruangan ruangTujuan = findRuanganById(idRuangTujuan);
            if (ruangTujuan != null) {
                ruangTujuan.tambahPerangkat(targetPerangkat);
                catatLog("AGREGASI", "Perangkat '" + targetPerangkat.getNama() + "' dipindahkan ke \"" + ruangTujuan.getNama() + "\".");
                return true;
            } else {
                // Kembalikan ke pool bebas jika ruang tidak ditemukan
                perangkatBebas.add(targetPerangkat);
                return false;
            }
        }
    }

    /**
     * Hitung estimasi tagihan listrik menggunakan TarifListrikPLN (Asosiasi).
     */
    public double hitungTagihan(double kwh, boolean jamPuncak) {
        return tarif.hitungBiaya(kwh, jamPuncak);
    }

    // Helper Finders
    public Ruangan findRuanganById(int id) {
        for (Ruangan r : daftarRuangan) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }

    public BebanListrik findPerangkatById(int id) {
        for (Ruangan r : daftarRuangan) {
            for (BebanListrik b : r.getDaftarPerangkat()) {
                if (b.getId() == id) {
                    return b;
                }
            }
        }
        for (BebanListrik b : perangkatBebas) {
            if (b.getId() == id) {
                return b;
            }
        }
        return null;
    }

    public void tambahPerangkatBebas(BebanListrik b) {
        if (b != null && !perangkatBebas.contains(b)) {
            perangkatBebas.add(b);
            catatLog("AGREGASI", "Perangkat baru '" + b.getNama() + "' didaftarkan ke Pool Bebas.");
        }
    }

    public boolean tambahPerangkatKeRuangan(int idRuangan, BebanListrik b) {
        Ruangan r = findRuanganById(idRuangan);
        if (r != null && b != null) {
            r.tambahPerangkat(b);
            catatLog("AGREGASI", "Perangkat '" + b.getNama() + "' didaftarkan ke \"" + r.getNama() + "\".");
            return true;
        }
        return false;
    }

    public void clearLogs() {
        this.eventLogs.clear();
    }

    // Getters & Setters
    public List<Ruangan> getDaftarRuangan() {
        return Collections.unmodifiableList(daftarRuangan);
    }

    public List<BebanListrik> getPerangkatBebas() {
        return Collections.unmodifiableList(perangkatBebas);
    }

    public TarifListrikPLN getTarif() {
        return tarif;
    }

    public void setTarif(TarifListrikPLN tarif) {
        this.tarif = tarif;
    }

    public List<String> getEventLogs() {
        return Collections.unmodifiableList(eventLogs);
    }

    public double getBatasTotalRumah() {
        return batasTotalRumah;
    }

    public void setBatasTotalRumah(double batasTotalRumah) {
        this.batasTotalRumah = batasTotalRumah;
    }
}

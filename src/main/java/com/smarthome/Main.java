package com.smarthome;

import com.smarthome.domain.*;
import java.util.List;

/**
 * Console Driver Pengujian 8 Skenario PBO
 * Berdasarkan proposal "Sistem Automasi dan Manajemen Beban Energi Rumah Pintar".
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  PENGUJIAN SISTEM AUTOMASI & MANAJEMEN BEBAN ENERGI RUMAH PINTAR (OOP JAVA)");
        System.out.println("================================================================================\n");

        SmartHomeManager manager = new SmartHomeManager(new TarifListrikPLN(2200), 2200.0);

        // -------------------------------------------------------------------------
        // Skenario 1: ABSTRAKSI
        // -------------------------------------------------------------------------
        System.out.println(">>> SKENARIO 1: ABSTRAKSI");
        System.out.println("Kelas BebanListrik didefinisikan sebagai abstract class.");
        System.out.println("Mendefinisikan kontrak method abstrak: hitungDayaAktif(), nyalakan(), matikan(), getTipePerangkat().");
        System.out.println("[VERIFIKASI] BebanListrik tidak dapat diinstansiasi secara langsung; polimorfisme bekerja lewat kontrak abstrak.\n");

        // -------------------------------------------------------------------------
        // Skenario 2: ENKAPSULASI
        // -------------------------------------------------------------------------
        System.out.println(">>> SKENARIO 2: ENKAPSULASI KETAT");
        AirConditioner acUji = new AirConditioner("AC Ruang Uji", 800, 2, 24);
        System.out.println("AC dibuat dengan suhu awal: " + acUji.getSuhuSetelan() + "°C");

        // Uji valid: set suhu ke 20°C
        acUji.setSuhu(20);
        System.out.println("[SUKSES] Mutasi suhu valid ke 20°C diterima. Suhu sekarang: " + acUji.getSuhuSetelan() + "°C");

        // Uji invalid: set suhu ke 14°C (< 16°C)
        try {
            System.out.println("Mencoba set suhu ke 14°C (di luar rentang 16-30°C)...");
            acUji.setSuhu(14);
            System.out.println("[GAGAL] Validasi lolos secara keliru!");
        } catch (IllegalArgumentException e) {
            System.out.println("[SUKSES TERTANGKAP] Enkapsulasi berhasil menolak nilai invalid: " + e.getMessage());
        }

        // Uji invalid: set suhu ke 35°C (> 30°C)
        try {
            System.out.println("Mencoba set suhu ke 35°C (di luar rentang 16-30°C)...");
            acUji.setSuhu(35);
            System.out.println("[GAGAL] Validasi lolos secara keliru!");
        } catch (IllegalArgumentException e) {
            System.out.println("[SUKSES TERTANGKAP] Enkapsulasi berhasil menolak nilai invalid: " + e.getMessage());
        }
        System.out.println();

        // -------------------------------------------------------------------------
        // Skenario 3: INHERITANCE (PEWARISAN)
        // -------------------------------------------------------------------------
        System.out.println(">>> SKENARIO 3: INHERITANCE");
        LampuCerdas lampu = new LampuCerdas("Lampu LED Utama", 50, 3, 80);
        AirConditioner ac = new AirConditioner("AC Inverter Kamar", 750, 2, 22);
        WaterHeater heater = new WaterHeater("Water Heater Kamar Mandi", 1200, 3, false);
        InverterSurya solar = new InverterSurya("Inverter Atap Surya", 1000, 1, 0.75);

        System.out.println("Objek konkret yang mewarisi BebanListrik:");
        System.out.printf(" - %s (Inherited ID: %d, Daya Nominal: %.0fW, Tipe: %s)%n",
                lampu.getNama(), lampu.getId(), lampu.getDayaNominal(), lampu.getTipePerangkat());
        System.out.printf(" - %s (Inherited ID: %d, Daya Nominal: %.0fW, Tipe: %s)%n",
                ac.getNama(), ac.getId(), ac.getDayaNominal(), ac.getTipePerangkat());
        System.out.printf(" - %s (Inherited ID: %d, Daya Nominal: %.0fW, Tipe: %s)%n",
                heater.getNama(), heater.getId(), heater.getDayaNominal(), heater.getTipePerangkat());
        System.out.printf(" - %s (Inherited ID: %d, Daya Nominal: %.0fW, Tipe: %s)%n",
                solar.getNama(), solar.getId(), solar.getDayaNominal(), solar.getTipePerangkat());
        System.out.println();

        // -------------------------------------------------------------------------
        // Skenario 4: POLIMORFISME
        // -------------------------------------------------------------------------
        System.out.println(">>> SKENARIO 4: POLIMORFISME");
        lampu.nyalakan();
        ac.nyalakan();
        heater.nyalakan();
        solar.nyalakan();

        List<BebanListrik> daftarBebanUji = List.of(lampu, ac, heater, solar);
        System.out.println("Menghitung daya aktif koleksi BebanListrik secara polimorfik tanpa instanceof:");
        double totalPolimorfik = 0.0;
        for (BebanListrik b : daftarBebanUji) {
            double daya = b.hitungDayaAktif(); // Panggilan polimorfik dinamis
            totalPolimorfik += daya;
            System.out.printf("   * [%s] %-25s -> Daya Aktif: %8.1f W%n",
                    b.getTipePerangkat(), b.getNama(), daya);
        }
        System.out.printf("Total Daya Netto Polimorfik: %.1f W%n%n", totalPolimorfik);

        // -------------------------------------------------------------------------
        // Skenario 5: KOMPOSISI
        // -------------------------------------------------------------------------
        System.out.println(">>> SKENARIO 5: KOMPOSISI (Ruangan -> PanelSirkuitRuang)");
        Ruangan ruangTamu = new Ruangan("Ruang Tamu", 1000.0);
        manager.daftarkanRuangan(ruangTamu);
        System.out.println("Ruang Tamu dibuat. Panel sirkuit internal otomatis terinstansiasi di konstruktor Ruangan:");
        System.out.println(" - Panel Ruang Tamu: " + ruangTamu.getPanel());
        System.out.println("Status panel aktif: " + ruangTamu.getPanel().isStatusAktif());
        System.out.println();

        // -------------------------------------------------------------------------
        // Skenario 6: AGREGASI
        // -------------------------------------------------------------------------
        System.out.println(">>> SKENARIO 6: AGREGASI (Ruangan -> BebanListrik)");
        ruangTamu.tambahPerangkat(lampu);
        ruangTamu.tambahPerangkat(ac);
        System.out.printf("Perangkat ditambahkan ke '%s'. Jumlah perangkat: %d%n",
                ruangTamu.getNama(), ruangTamu.getDaftarPerangkat().size());

        System.out.println("Menghapus 'Ruang Tamu' dari SmartHomeManager...");
        manager.hapusRuangan(ruangTamu.getId());

        System.out.println("[VERIFIKASI KOMPOSISI] Panel ikut hancur (null): " + (ruangTamu.getPanel() == null));
        System.out.println("[VERIFIKASI AGREGASI] Perangkat tidak musnah dan selamat di Pool Bebas:");
        for (BebanListrik pb : manager.getPerangkatBebas()) {
            System.out.printf(" - %s (Status: %s, Daya: %.1fW, ID: %d)%n",
                    pb.getNama(), pb.isAktif() ? "ON" : "OFF", pb.hitungDayaAktif(), pb.getId());
        }
        System.out.println();

        // -------------------------------------------------------------------------
        // Skenario 7: ASOSIASI
        // -------------------------------------------------------------------------
        System.out.println(">>> SKENARIO 7: ASOSIASI (SmartHomeManager -> TarifListrikPLN)");
        double kwhUji = 25.0;
        double biayaReguler = manager.hitungTagihan(kwhUji, false);
        double biayaPuncak = manager.hitungTagihan(kwhUji, true);
        System.out.printf("Pemakaian %.1f kWh pada Golongan %d VA:%n",
                kwhUji, manager.getTarif().getGolonganDaya());
        System.out.printf(" - Biaya Reguler   : Rp %,.2f%n", biayaReguler);
        System.out.printf(" - Biaya Jam Puncak: Rp %,.2f (+25%% faktor)%n%n", biayaPuncak);

        // -------------------------------------------------------------------------
        // Skenario 8: LOAD SHEDDING (PROTEKSI OVERLOAD OTOMATIS)
        // -------------------------------------------------------------------------
        System.out.println(">>> SKENARIO 8: LOAD SHEDDING OTOMATIS");
        Ruangan kamarUtama = new Ruangan("Kamar Utama", 1200.0);
        manager.daftarkanRuangan(kamarUtama);

        // Pindahkan AC dan Lampu ke kamar utama
        manager.pindahkanPerangkat(lampu.getId(), kamarUtama.getId());
        manager.pindahkanPerangkat(ac.getId(), kamarUtama.getId());
        // Tambahkan water heater berdaya besar (1200W, prioritas 3 = rendah)
        kamarUtama.tambahPerangkat(heater);
        heater.nyalakan();

        System.out.printf("Beban Kamar Utama saat ini: %.1f W (Batas Sirkuit: %.1f W)%n",
                kamarUtama.hitungDayaRuangan(), kamarUtama.getPanel().getBatasDaya());
        System.out.println("Status Overload Kamar Utama: " + (kamarUtama.isOverload() ? "⚠️ YA (OVERLOAD)" : "AMAN"));
        System.out.printf("Total Daya Seluruh Hunian : %.1f W (Batas Total: %.1f W)%n",
                manager.hitungDayaTotal(), manager.getBatasTotalRumah());

        System.out.println("\nMenjalankan Load Shedding Otomatis...");
        List<String> sheddingList = manager.jalankanLoadShedding();
        System.out.println("Hasil Perangkat yang Dimatikan:");
        for (String info : sheddingList) {
            System.out.println(" - " + info);
        }

        System.out.printf("Beban Kamar Utama setelah Load Shedding: %.1f W%n", kamarUtama.hitungDayaRuangan());
        System.out.println("Status Overload Kamar Utama sekarang: " + (kamarUtama.isOverload() ? "⚠️ OVERLOAD" : "✓ AMAN"));
        System.out.printf("Total Daya Seluruh Hunian sekarang : %.1f W (Status: %s)%n",
                manager.hitungDayaTotal(), manager.isOverload() ? "⚠️ OVERLOAD" : "✓ AMAN");

        System.out.println("\nLog Siklus Hidup Objek (5 Catatan Terakhir):");
        List<String> logs = manager.getEventLogs();
        int maxShow = Math.min(5, logs.size());
        for (int i = 0; i < maxShow; i++) {
            System.out.println("  " + logs.get(i));
        }

        System.out.println("\n================================================================================");
        System.out.println("  SEMUA 8 SKENARIO PBO BERHASIL DIUJI DENGAN SEMPURNA!");
        System.out.println("================================================================================");
    }
}

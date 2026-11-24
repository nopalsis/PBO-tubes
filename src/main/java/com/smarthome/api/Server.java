package com.smarthome.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthome.domain.*;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;

import java.util.*;

/**
 * REST API Server Javalin (Port 8080)
 * Melayani static web assets dari src/main/resources/public dan 10 endpoint REST API.
 * Seluruh logika bisnis didelegasikan secara murni ke SmartHomeManager (tanpa logika bisnis di Server).
 */
public class Server {
    private static final int DEFAULT_PORT = 8080;
    private final int port;
    private final SmartHomeManager manager;
    private final ObjectMapper mapper;
    private Javalin app;

    public Server() {
        this(new SmartHomeManager(new TarifListrikPLN(2200), 2200.0));
    }

    public Server(SmartHomeManager manager) {
        this.manager = manager;
        this.mapper = new ObjectMapper();
        
        // Membaca Port dinamis dari Render ($PORT), fallback ke 8080 untuk lokal
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            this.port = Integer.parseInt(envPort.trim());
        } else {
            this.port = DEFAULT_PORT;
        }

        inisialisasiDataAwal();
    }

    /**
     * Menyiapkan 3 ruangan awal dan beberapa perangkat siap demo.
     */
    private void inisialisasiDataAwal() {
        // 1. Kamar Tidur (Batas 1200W)
        Ruangan kamarTidur = new Ruangan("Kamar Tidur", 1200.0);
        AirConditioner acKamar = new AirConditioner("AC Daikin Inverter", 750, 2, 22);
        acKamar.nyalakan();
        LampuCerdas lampuKamar = new LampuCerdas("Lampu LED Kamar", 35, 3, 80);
        lampuKamar.nyalakan();
        kamarTidur.tambahPerangkat(acKamar);
        kamarTidur.tambahPerangkat(lampuKamar);
        manager.daftarkanRuangan(kamarTidur);

        // 2. Ruang Tamu (Batas 1500W)
        Ruangan ruangTamu = new Ruangan("Ruang Tamu", 1500.0);
        AirConditioner acTamu = new AirConditioner("AC Panasonic Eco", 800, 2, 24);
        acTamu.nyalakan();
        LampuCerdas lampuTamu = new LampuCerdas("Lampu Downlight Tamu", 45, 3, 100);
        lampuTamu.nyalakan();
        WaterHeater heaterTamu = new WaterHeater("Water Heater Mandi", 1200, 3, false);
        heaterTamu.matikan();
        ruangTamu.tambahPerangkat(acTamu);
        ruangTamu.tambahPerangkat(lampuTamu);
        ruangTamu.tambahPerangkat(heaterTamu);
        manager.daftarkanRuangan(ruangTamu);

        // 3. Dapur (Batas 1800W)
        Ruangan dapur = new Ruangan("Dapur", 1800.0);
        LampuCerdas lampuDapur = new LampuCerdas("Lampu Spotlight Dapur", 30, 3, 100);
        lampuDapur.nyalakan();
        InverterSurya solarAtap = new InverterSurya("Panel Surya Mandiri", 800, 1, 0.75);
        solarAtap.nyalakan();
        dapur.tambahPerangkat(lampuDapur);
        dapur.tambahPerangkat(solarAtap);
        manager.daftarkanRuangan(dapur);

        // Perangkat Bebas Awal (Orphan Pool)
        WaterHeater heaterCadangan = new WaterHeater("Water Heater Cadangan", 900, 3, false);
        manager.tambahPerangkatBebas(heaterCadangan);

        manager.catatLog("SISTEM", "Data demo berhasil dimuat: 3 Ruangan, 7 Perangkat (termasuk Inverter Surya & Pool Bebas).");
    }

    public void start() {
        app = Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
            config.plugins.enableCors(cors -> cors.add(it -> it.anyHost()));
        });

        registerRoutes();
        app.start(this.port);
        System.out.println("================================================================");
        System.out.println("  SmartHome REST API Server aktif di port " + this.port);
        System.out.println("  Static UI: http://localhost:" + this.port + "/index.html");
        System.out.println("================================================================");
    }

    public void stop() {
        if (app != null) {
            app.stop();
        }
    }

    private void registerRoutes() {
        // 1. GET /api/rumah -> status seluruh ruangan, panel, perangkat, perangkat bebas, daya, overload
        app.get("/api/rumah", ctx -> {
            ctx.json(buildRumahState());
        });

        // 2. POST /api/ruangan -> tambah ruangan baru (nama, batasDaya)
        app.post("/api/ruangan", ctx -> {
            Map<?, ?> body = ctx.bodyAsClass(Map.class);
            String nama = (String) body.get("nama");
            double batasDaya = Double.parseDouble(body.get("batasDaya").toString());

            if (nama == null || nama.trim().isEmpty() || batasDaya <= 0) {
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Nama ruangan dan batas daya harus valid (> 0)!"));
                return;
            }

            Ruangan r = new Ruangan(nama.trim(), batasDaya);
            manager.daftarkanRuangan(r);
            ctx.status(HttpStatus.CREATED).json(Map.of("success", true, "ruangan", serializeRuangan(r)));
        });

        // 3. DELETE /api/ruangan/{id} -> hapus ruangan (memicu penghancuran panel & penyelamatan perangkat)
        app.delete("/api/ruangan/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean deleted = manager.hapusRuangan(id);
            if (deleted) {
                ctx.json(Map.of("success", true, "message", "Ruangan berhasil dihapus. Panel musnah & perangkat dialihkan ke Pool Bebas."));
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json(Map.of("error", "Ruangan dengan ID " + id + " tidak ditemukan!"));
            }
        });

        // 4. POST /api/perangkat -> buat perangkat baru
        app.post("/api/perangkat", ctx -> {
            Map<?, ?> body = ctx.bodyAsClass(Map.class);
            String tipe = (String) body.get("tipe");
            String nama = (String) body.get("nama");
            double dayaNominal = Double.parseDouble(body.get("dayaNominal").toString());
            int prioritas = body.containsKey("prioritas") ? Integer.parseInt(body.get("prioritas").toString()) : 2;
            int idRuangan = body.containsKey("idRuangan") && body.get("idRuangan") != null ?
                    Integer.parseInt(body.get("idRuangan").toString()) : 0;

            if (tipe == null || nama == null || nama.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Tipe dan nama perangkat wajib diisi!"));
                return;
            }

            BebanListrik baru;
            String tipeNorm = tipe.trim().toUpperCase();
            switch (tipeNorm) {
                case "LAMPU":
                case "LAMPUCERDAS":
                    int kecerahan = body.containsKey("tingkatKecerahan") ?
                            Integer.parseInt(body.get("tingkatKecerahan").toString()) : 100;
                    baru = new LampuCerdas(nama.trim(), dayaNominal, prioritas, kecerahan);
                    break;
                case "AC":
                case "AIRCONDITIONER":
                    int suhu = body.containsKey("suhu") ?
                            Integer.parseInt(body.get("suhu").toString()) : 24;
                    baru = new AirConditioner(nama.trim(), dayaNominal, prioritas, suhu);
                    break;
                case "HEATER":
                case "WATERHEATER":
                case "PEMANASAIR":
                    boolean memanaskan = body.containsKey("sedangMemanaskan") &&
                            Boolean.parseBoolean(body.get("sedangMemanaskan").toString());
                    baru = new WaterHeater(nama.trim(), dayaNominal, prioritas, memanaskan);
                    break;
                case "INVERTER":
                case "INVERTERSURYA":
                case "SOLAR":
                    double irradiasi = body.containsKey("tingkatIrradiasi") ?
                            Double.parseDouble(body.get("tingkatIrradiasi").toString()) : 0.8;
                    baru = new InverterSurya(nama.trim(), dayaNominal, prioritas, irradiasi);
                    break;
                default:
                    ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Tipe perangkat tidak dikenali: " + tipe));
                    return;
            }

            // Jika ada opsi langsung menyala
            if (body.containsKey("aktif") && Boolean.parseBoolean(body.get("aktif").toString())) {
                baru.nyalakan();
            }

            if (idRuangan > 0) {
                boolean ditambahkan = manager.tambahPerangkatKeRuangan(idRuangan, baru);
                if (!ditambahkan) {
                    manager.tambahPerangkatBebas(baru);
                }
            } else {
                manager.tambahPerangkatBebas(baru);
            }

            ctx.status(HttpStatus.CREATED).json(Map.of("success", true, "perangkat", serializePerangkat(baru)));
        });

        // 5. PUT /api/perangkat/{id}/pindah -> pindah perangkat ke ruangan tujuan
        app.put("/api/perangkat/{id}/pindah", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Map<?, ?> body = ctx.bodyAsClass(Map.class);
            int idRuangTujuan = body.containsKey("idRuangTujuan") && body.get("idRuangTujuan") != null ?
                    Integer.parseInt(body.get("idRuangTujuan").toString()) : 0;

            boolean sukses = manager.pindahkanPerangkat(id, idRuangTujuan);
            if (sukses) {
                ctx.json(Map.of("success", true, "message", "Perangkat berhasil dipindahkan."));
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json(Map.of("error", "Perangkat dengan ID " + id + " tidak ditemukan!"));
            }
        });

        // 6. PUT /api/perangkat/{id}/nyala -> toggle ON/OFF perangkat
        app.put("/api/perangkat/{id}/nyala", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            BebanListrik dev = manager.findPerangkatById(id);
            if (dev == null) {
                ctx.status(HttpStatus.NOT_FOUND).json(Map.of("error", "Perangkat dengan ID " + id + " tidak ditemukan!"));
                return;
            }

            if (dev.isAktif()) {
                dev.matikan();
            } else {
                dev.nyalakan();
            }
            manager.catatLog("AGREGASI", "Perangkat '" + dev.getNama() + "' diubah status menjadi " + (dev.isAktif() ? "ON (Aktif)" : "OFF (Mati)") + ".");

            ctx.json(Map.of("success", true, "perangkat", serializePerangkat(dev)));
        });

        // 7. PUT /api/ac/{id}/suhu -> atur suhu AC (validasi enkapsulasi ketat 16-30°C)
        app.put("/api/ac/{id}/suhu", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            BebanListrik dev = manager.findPerangkatById(id);
            if (dev == null) {
                ctx.status(HttpStatus.NOT_FOUND).json(Map.of("error", "Perangkat tidak ditemukan!"));
                return;
            }
            if (!(dev instanceof AirConditioner)) {
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Perangkat bukan merupakan AirConditioner!"));
                return;
            }

            Map<?, ?> body = ctx.bodyAsClass(Map.class);
            int suhuBaru = Integer.parseInt(body.get("suhu").toString());
            AirConditioner ac = (AirConditioner) dev;

            try {
                // Enkapsulasi ketat: melempar exception jika di luar 16-30°C
                ac.setSuhu(suhuBaru);
                manager.catatLog("ENKAPSULASI", "Suhu AC '" + ac.getNama() + "' disetel ke " + suhuBaru + "°C.");
                ctx.json(Map.of("success", true, "suhu", ac.getSuhuSetelan(), "dayaAktif", ac.hitungDayaAktif()));
            } catch (IllegalArgumentException e) {
                manager.catatLog("ENKAPSULASI", "Validasi Ditolak: " + e.getMessage());
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", e.getMessage()));
            }
        });

        // 8. POST /api/simulasi/overload -> jalankan jalankanLoadShedding()
        app.post("/api/simulasi/overload", ctx -> {
            List<String> sheddingList = manager.jalankanLoadShedding();
            ctx.json(Map.of(
                    "success", true,
                    "dimatikan", sheddingList,
                    "totalDaya", manager.hitungDayaTotal(),
                    "isOverload", manager.isOverload()
            ));
        });

        // 9. GET /api/tagihan -> query param golongan, kwh, jamPuncak
        app.get("/api/tagihan", ctx -> {
            int golongan = ctx.queryParamAsClass("golongan", Integer.class).getOrDefault(manager.getTarif().getGolonganDaya());
            double kwh = ctx.queryParamAsClass("kwh", Double.class).getOrDefault(25.0);
            boolean jamPuncak = ctx.queryParamAsClass("jamPuncak", Boolean.class).getOrDefault(false);

            TarifListrikPLN tarif = new TarifListrikPLN(golongan);
            double totalBiaya = tarif.hitungBiaya(kwh, jamPuncak);

            ctx.json(Map.of(
                    "golongan", golongan,
                    "kwh", kwh,
                    "jamPuncak", jamPuncak,
                    "tarifPerKwh", tarif.getTarifPerKwh(),
                    "faktorBebanPuncak", tarif.getFaktorBebanPuncak(),
                    "totalBiaya", totalBiaya
            ));
        });

        // 10. GET /api/log -> ambil seluruh riwayat eventLogs siklus hidup objek
        app.get("/api/log", ctx -> {
            ctx.json(Map.of("logs", manager.getEventLogs()));
        });
    }

    // Helper Serializer untuk JSON response
    private Map<String, Object> buildRumahState() {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("totalDaya", manager.hitungDayaTotal());
        state.put("batasTotalRumah", manager.getBatasTotalRumah());
        state.put("isOverload", manager.isOverload());

        List<Map<String, Object>> listRuang = new ArrayList<>();
        for (Ruangan r : manager.getDaftarRuangan()) {
            listRuang.add(serializeRuangan(r));
        }
        state.put("ruangan", listRuang);

        List<Map<String, Object>> listBebas = new ArrayList<>();
        for (BebanListrik b : manager.getPerangkatBebas()) {
            listBebas.add(serializePerangkat(b));
        }
        state.put("perangkatBebas", listBebas);

        return state;
    }

    private Map<String, Object> serializeRuangan(Ruangan r) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", r.getId());
        data.put("nama", r.getNama());
        data.put("dayaAktif", r.hitungDayaRuangan());
        data.put("isOverload", r.isOverload());

        if (r.getPanel() != null) {
            Map<String, Object> panelData = new LinkedHashMap<>();
            panelData.put("batasDaya", r.getPanel().getBatasDaya());
            panelData.put("statusAktif", r.getPanel().isStatusAktif());
            data.put("panel", panelData);
        } else {
            data.put("panel", null);
        }

        List<Map<String, Object>> devs = new ArrayList<>();
        for (BebanListrik b : r.getDaftarPerangkat()) {
            devs.add(serializePerangkat(b));
        }
        data.put("daftarPerangkat", devs);
        return data;
    }

    private Map<String, Object> serializePerangkat(BebanListrik b) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", b.getId());
        data.put("nama", b.getNama());
        data.put("tipePerangkat", b.getTipePerangkat());
        data.put("dayaNominal", b.getDayaNominal());
        data.put("dayaAktif", b.hitungDayaAktif());
        data.put("aktif", b.isAktif());
        data.put("prioritas", b.getPrioritas());

        if (b instanceof AirConditioner) {
            data.put("suhuSetelan", ((AirConditioner) b).getSuhuSetelan());
        } else if (b instanceof LampuCerdas) {
            data.put("tingkatKecerahan", ((LampuCerdas) b).getTingkatKecerahan());
        } else if (b instanceof WaterHeater) {
            data.put("sedangMemanaskan", ((WaterHeater) b).isSedangMemanaskan());
        } else if (b instanceof InverterSurya) {
            data.put("kapasitasPuncak", ((InverterSurya) b).getKapasitasPuncak());
            data.put("tingkatIrradiasi", ((InverterSurya) b).getTingkatIrradiasi());
        }
        return data;
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}

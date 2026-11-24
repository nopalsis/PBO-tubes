# Sistem Automasi dan Manajemen Beban Energi Rumah Pintar
> Proyek Simulasi Pemrograman Berorientasi Objek (PBO) — Java 17+, Maven, Javalin REST API & Web Dashboard

Aplikasi full-stack simulasi hunian pintar yang mengimplementasikan 4 pilar utama PBO (Abstraksi, Enkapsulasi, Inheritance, Polimorfisme) serta relasi objek (Komposisi, Agregasi, Asosiasi) dan algoritma proteksi cerdas *Load Shedding*.

---

## 🏛️ Arsitektur & Rancangan OOP (`com.smarthome.domain`)

Sistem terdiri dari tepat 9 kelas domain murni:

| No | Kelas | Pilar / Relasi | Penjelasan |
|---|---|---|---|
| 1 | `BebanListrik` | **Abstraksi** | *Abstract base class* dengan method abstrak `hitungDayaAktif()`, `nyalakan()`, `matikan()`, dan `getTipePerangkat()`. |
| 2 | `LampuCerdas` | **Inheritance** | Mewarisi `BebanListrik`. Memiliki fitur dimmer (`tingkatKecerahan` 0–100%) dengan daya aktif proporsional kecerahan. |
| 3 | `AirConditioner` | **Enkapsulasi** | Menerapkan validasi ketat suhu (16°C – 30°C). Daya aktif meningkat proporsional saat suhu disetel lebih dingin. |
| 4 | `WaterHeater` | **Inheritance** | Beban daya besar dengan prioritas rendah (3), menjadi target utama pemadaman darurat saat *load shedding*. |
| 5 | `InverterSurya` | **Polimorfisme** | Menghasilkan daya aktif **NEGATIF** (`-(kapasitasPuncak * tingkatIrradiasi)`) saat aktif untuk mengurangi beban netto hunian. |
| 6 | `PanelSirkuitRuang` | **Komposisi** | Komponen proteksi lokal tiap ruangan (`batasDaya`, `statusAktif`, `periksaOverload`). |
| 7 | `Ruangan` | **Komposisi & Agregasi** | **Komposisi**: `PanelSirkuitRuang` diinstansiasi di dalam konstruktor dan ikut hancur saat `hancurkan()`.<br>**Agregasi**: Menampung `BebanListrik` yang dapat ditambah, dilepas, dan selamat saat ruangan dihapus. |
| 8 | `TarifListrikPLN` | **Asosiasi** | Menghitung biaya pemakaian listrik berdasarkan golongan (900, 1300, 2200, 3500 VA) dan faktor jam puncak (WBP +25%). |
| 9 | `SmartHomeManager` | **Fasade / Controller** | Mengelola seluruh ruangan, *perangkatBebas* (orphan pool), simulasi tagihan, dan algoritma *Load Shedding*. |

---

## 🌐 REST API Endpoints (Port 8080)

| Method | Endpoint | Deskripsi |
|---|---|---|
| `GET` | `/api/rumah` | Mengambil status seluruh ruangan, panel, perangkat, perangkat bebas, beban netto, dan status overload. |
| `POST` | `/api/ruangan` | Membuat ruangan baru (`nama`, `batasDaya`). Menginstansiasi panel sirkuit secara Komposisi. |
| `DELETE` | `/api/ruangan/{id}` | Menghapus ruangan. Memusnahkan panel sirkuit (Komposisi) dan menyelamatkan perangkat ke Pool Bebas (Agregasi). |
| `POST` | `/api/perangkat` | Membuat perangkat baru (`tipe`: AC/LAMPU/HEATER/INVERTER, `nama`, `dayaNominal`, `prioritas`, `idRuangan`). |
| `PUT` | `/api/perangkat/{id}/pindah` | Memindahkan perangkat antar ruangan atau ke Pool Bebas (`idRuangTujuan`). |
| `PUT` | `/api/perangkat/{id}/nyala` | Menyalakan / mematikan perangkat (*Toggle ON/OFF*). |
| `PUT` | `/api/ac/{id}/suhu` | Mengatur suhu AC (`suhu`). Mengembalikan HTTP 400 Bad Request jika melanggar enkapsulasi (<16 atau >30°C). |
| `POST` | `/api/simulasi/overload` | Menjalankan algoritma *Load Shedding* otomatis dari prioritas terendah (P3 -> P2 -> P1). |
| `GET` | `/api/tagihan` | Menghitung estimasi tagihan PLN (`golongan`, `kwh`, `jamPuncak`). |
| `GET` | `/api/log` | Mengambil riwayat *audit log* siklus hidup objek (Komposisi, Agregasi, Enkapsulasi, Load Shedding). |

---

## 🖥️ Antarmuka Web (Stitch Design Integration)

Antarmuka web editorial modern disajikan langsung oleh Javalin dari `src/main/resources/public/index.html`:
- **Header Ringkasan Beban & Kalkulator PLN**: Pemantauan beban netto secara *real-time*, status AMAN / OVERLOAD, dan kalkulator tarif PLN interaktif.
- **Grid Ruangan & Panel Sirkuit**: Progress bar kapasitas sirkuit ruangan dengan pewarnaan dinamis (Aman, Waspada, Overload).
- **Kontrol Perangkat & Mutasi**: Tombol Toggle ON/OFF, kontrol termostat AC dengan penanganan error enkapsulasi, indikator daya negatif panel surya, dan dropdown relokasi ruangan.
- **Pool Perangkat Bebas (Agregasi)**: Menampilkan perangkat yang selamat saat ruangannya dihapus, siap direlokasi.
- **Audit Log Siklus Hidup**: Terminal real-time mencatat setiap aksi Komposisi, Agregasi, Enkapsulasi, dan Load Shedding.

---

## 🚀 Cara Menjalankan

### 1. Menjalankan Uji 8 Skenario PBO (Console Driver)
Jalankan file batch:
```cmd
run-main.bat
```
atau menggunakan Maven:
```cmd
.\mvn.cmd exec:java -Dexec.mainClass="com.smarthome.Main"
```

### 2. Menjalankan REST API Server & Web UI
Jalankan file batch:
```cmd
run-server.bat
```
atau menggunakan Maven:
```cmd
.\mvn.cmd exec:java -Dexec.mainClass="com.smarthome.api.Server" -Djava.net.preferIPv4Stack=true
```

Buka peramban (browser) di:
👉 **http://localhost:8080/index.html**

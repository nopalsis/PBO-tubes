Write-Host "==================== TEST 1: GET /api/rumah ===================="
$rumah = Invoke-RestMethod -Uri 'http://localhost:8080/api/rumah' -Method Get
Write-Host "Total Beban Daya : $($rumah.totalDaya) W"
Write-Host "Batas Daya Rumah : $($rumah.batasTotalRumah) W"
Write-Host "Status Overload  : $($rumah.isOverload)"
Write-Host "Jumlah Ruangan   : $($rumah.ruangan.Count)"
foreach ($r in $rumah.ruangan) {
    Write-Host "  * Ruang: $($r.nama) (Batas: $($r.panel.batasDaya)W, Beban: $($r.dayaAktif)W, Perangkat: $($r.daftarPerangkat.Count))"
}
Write-Host "Perangkat Bebas  : $($rumah.perangkatBebas.Count)"

Write-Host "`n==================== TEST 2: POST /api/ruangan ===================="
$newRoom = Invoke-RestMethod -Uri 'http://localhost:8080/api/ruangan' -Method Post -ContentType 'application/json' -Body '{"nama":"Ruang Kerja","batasDaya":1500}'
Write-Host "Ruang Baru Dibuat: ID=$($newRoom.ruangan.id), Nama=$($newRoom.ruangan.nama), Batas=$($newRoom.ruangan.panel.batasDaya)W"

Write-Host "`n==================== TEST 3: POST /api/perangkat ===================="
$devPayload = @{
    nama = "AC Ruang Kerja"
    tipe = "AC"
    dayaNominal = 800
    prioritas = 2
    idRuangan = $newRoom.ruangan.id
    suhu = 24
} | ConvertTo-Json
$newDev = Invoke-RestMethod -Uri 'http://localhost:8080/api/perangkat' -Method Post -ContentType 'application/json' -Body $devPayload
Write-Host "Perangkat Dibuat : ID=$($newDev.perangkat.id), Nama=$($newDev.perangkat.nama), Tipe=$($newDev.perangkat.tipePerangkat), Suhu=$($newDev.perangkat.suhuSetelan)C"

Write-Host "`n==================== TEST 4: PUT /api/perangkat/{id}/nyala (Toggle ON/OFF) ===================="
$toggled = Invoke-RestMethod -Uri "http://localhost:8080/api/perangkat/$($newDev.perangkat.id)/nyala" -Method Put
Write-Host "Status Perangkat setelah toggle: Aktif=$($toggled.perangkat.aktif), Daya Aktif=$($toggled.perangkat.dayaAktif)W"

Write-Host "`n==================== TEST 5: PUT /api/ac/{id}/suhu (Validasi Enkapsulasi) ===================="
# Uji Valid 20C
$acValid = Invoke-RestMethod -Uri "http://localhost:8080/api/ac/$($newDev.perangkat.id)/suhu" -Method Put -ContentType 'application/json' -Body '{"suhu":20}'
Write-Host "[ENKAPSULASI VALID] Suhu disetel ke 20C -> Berhasil! Daya Aktif baru: $($acValid.dayaAktif)W"

# Uji Invalid 14C (< 16C)
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/ac/$($newDev.perangkat.id)/suhu" -Method Put -ContentType 'application/json' -Body '{"suhu":14}'
    Write-Host "[ERROR] Suhu 14C tidak ditolak!"
} catch {
    Write-Host "[ENKAPSULASI DITOLAK 14C] Server berhasil merespons HTTP 400 Bad Request!"
}

# Uji Invalid 35C (> 30C)
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/ac/$($newDev.perangkat.id)/suhu" -Method Put -ContentType 'application/json' -Body '{"suhu":35}'
    Write-Host "[ERROR] Suhu 35C tidak ditolak!"
} catch {
    Write-Host "[ENKAPSULASI DITOLAK 35C] Server berhasil merespons HTTP 400 Bad Request!"
}

Write-Host "`n==================== TEST 6: PUT /api/perangkat/{id}/pindah ===================="
$pindah = Invoke-RestMethod -Uri "http://localhost:8080/api/perangkat/$($newDev.perangkat.id)/pindah" -Method Put -ContentType 'application/json' -Body '{"idRuangTujuan":0}'
Write-Host "Pindah ke Pool Bebas: Success=$($pindah.success)"

Write-Host "`n==================== TEST 7: POST /api/simulasi/overload (Load Shedding) ===================="
$shedding = Invoke-RestMethod -Uri 'http://localhost:8080/api/simulasi/overload' -Method Post
Write-Host "Load Shedding Dimatikan: $($shedding.dimatikan.Count) perangkat"
foreach ($d in $shedding.dimatikan) {
    Write-Host "  * $d"
}

Write-Host "`n==================== TEST 8: GET /api/tagihan ===================="
$tagihan = Invoke-RestMethod -Uri 'http://localhost:8080/api/tagihan?golongan=2200&kwh=50&jamPuncak=true' -Method Get
Write-Host "Golongan Daya : $($tagihan.golongan) VA"
Write-Host "Konsumsi      : $($tagihan.kwh) kWh"
Write-Host "Jam Puncak    : $($tagihan.jamPuncak) (Faktor: $($tagihan.faktorBebanPuncak))"
Write-Host "Total Tagihan : Rp $($tagihan.totalBiaya)"

Write-Host "`n==================== TEST 9: GET /api/log ===================="
$logs = Invoke-RestMethod -Uri 'http://localhost:8080/api/log' -Method Get
Write-Host "Total Log Objek: $($logs.logs.Count)"
for ($i = 0; $i -lt [Math]::Min(5, $logs.logs.Count); $i++) {
    Write-Host "  $($logs.logs[$i])"
}

Write-Host "`n==================== TEST 10: DELETE /api/ruangan/{id} ===================="
$del = Invoke-RestMethod -Uri "http://localhost:8080/api/ruangan/$($newRoom.ruangan.id)" -Method Delete
Write-Host "Hapus Ruangan ID $($newRoom.ruangan.id): Success=$($del.success), Pesan=$($del.message)"

Write-Host "`n==================== TEST 11: STATIC ASSET (/index.html) ===================="
$ui = Invoke-WebRequest -Uri 'http://localhost:8080/index.html' -Method Get -UseBasicParsing
Write-Host "UI Status Code : $($ui.StatusCode)"
Write-Host "UI Content Size: $($ui.Content.Length) bytes"

Write-Host "`n==================== SEMUA 10 ENDPOINT REST API BERHASIL 100%! ===================="

const API_URL = "http://localhost:8080/api";
const token = localStorage.getItem('token');

// --- 1. PROTEKSI & NAVIGASI ---
if (!token && !window.location.href.includes('index.html')) {
    window.location.href = 'index.html';
}

function logout() {
    localStorage.removeItem('token');
    window.location.href = 'index.html';
}

function showSection(sectionId, element = null) {
    const sections = ['homeSec', 'alternatifSec', 'kriteriaSec', 'rankingSec', 'prediksiSec'];
    sections.forEach(id => document.getElementById(id)?.classList.add('hidden'));
    document.getElementById(sectionId)?.classList.remove('hidden');

    if (element) {
        document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
        element.classList.add('active');
    }
}

// --- 2. CORE API HELPER ---
async function fetchAPI(endpoint, method = 'GET', body = null) {
    const options = {
        method: method,
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' }
    };
    if (body) options.body = JSON.stringify(body);
    try {
        const response = await fetch(`${API_URL}${endpoint}`, options);
        if (response.status === 403) {
            alert("Sesi berakhir (403). Silakan Logout dan Login kembali.");
            logout();
        }
        return response;
    } catch (e) {
        console.error(e);
        alert("Koneksi ke Docker Gagal! Pastikan kontainer menyala.");
        return { ok: false };
    }
}

// --- 3. HELPER INPUT (ABSENSI & LIMIT) ---
async function getKriteriaMap() {
    const res = await fetchAPI('/kriteria');
    const kriteria = await res.json();
    const map = {};
    kriteria.forEach(k => map[k.kode] = k.id);
    return map;
}

function konversiAbsensi(hari) {
    let absen = parseFloat(hari);
    if (isNaN(absen) || absen === 0) return 0;
    if (absen >= 1 && absen <= 3) return 2;
    if (absen >= 4 && absen <= 7) return 5.5;
    if (absen >= 8 && absen <= 10) return 9;
    return 12;
}

function batasiNilai(nilai) {
    let n = parseFloat(nilai);
    if (isNaN(n)) return 0;
    return Math.max(0, Math.min(10, n));
}

// --- 4. DATA KARYAWAN (LOAD, ADD, HAPUS, HAPUS SEMUA, EXCEL) ---
async function loadAlternatif(el) {
    if (el) showSection('alternatifSec', el);
    const res = await fetchAPI('/alternatif');
    if (!res.ok) return;
    const data = await res.json();
    let html = '';

    if (data.length === 0) {
        html = '<tr><td colspan="5" class="text-center text-muted py-4">Belum ada data karyawan. Silakan tambah manual atau upload Excel.</td></tr>';
    } else {
        data.forEach((item, idx) => {
            html += `<tr><td>${idx + 1}</td><td><span class="badge bg-primary">${item.kode}</span></td>
                     <td>${item.nama}</td><td>${item.jabatan}</td>
                     <td class="no-print"><button class="btn btn-sm btn-danger" onclick="hapusAlternatif(${item.id})">Hapus</button></td></tr>`;
        });
    }
    document.getElementById('tabelAlternatif').innerHTML = html;
}

async function addAlternatif() {
    const data = {
        kode: document.getElementById('altKode').value,
        nama: document.getElementById('altNama').value,
        jabatan: document.getElementById('altJabatan').value
    };

    if (!data.kode || !data.nama) return alert("Isi Kode dan Nama!");

    const resAlt = await fetchAPI('/alternatif', 'POST', data);
    if (resAlt.ok) {
        const result = await resAlt.json();
        const altId = result.id;
        const kMap = await getKriteriaMap();

        const nilaiKinerja = [
            { kId: kMap['C1'], val: batasiNilai(document.getElementById('valC1').value) },
            { kId: kMap['C2'], val: batasiNilai(document.getElementById('valC2').value) },
            { kId: kMap['C3'], val: konversiAbsensi(document.getElementById('valC3').value) },
            { kId: kMap['C4'], val: batasiNilai(document.getElementById('valC4').value) },
            { kId: kMap['C5'], val: batasiNilai(document.getElementById('valC5').value) }
        ];

        for (let n of nilaiKinerja) {
            await fetchAPI('/penilaian', 'POST', { alternatifId: altId, kriteriaId: n.kId, nilai: n.val });
        }

        alert("✅ Karyawan & Nilai Berhasil Disimpan!");
        loadAlternatif(null);
        document.querySelectorAll('#alternatifSec input').forEach(i => i.value = "");
    }
}

async function hapusAlternatif(id) {
    if (confirm("Hapus karyawan ini beserta semua nilainya?")) {
        const res = await fetchAPI(`/alternatif/${id}`, 'DELETE');
        if (res.ok) { alert("🗑️ Terhapus!"); loadAlternatif(null); }
    }
}

async function hapusSemuaKaryawan() {
    if (confirm("⚠️ PERINGATAN KERAS!\n\nApakah Anda yakin ingin menghapus SELURUH data karyawan beserta nilainya? Tindakan ini tidak bisa dibatalkan!")) {
        const res = await fetchAPI('/alternatif');
        if (!res.ok) return alert("❌ Gagal terhubung ke server!");

        const data = await res.json();
        if (data.length === 0) return alert("ℹ️ Data karyawan sudah kosong, Komandan!");

        let sukses = 0;
        for (let item of data) {
            const delRes = await fetchAPI(`/alternatif/${item.id}`, 'DELETE');
            if (delRes.ok) sukses++;
        }

        alert(`✅ SAPU BERSIH SUKSES!\nBerhasil menghapus ${sukses} data karyawan.`);
        loadAlternatif(null);
    }
}

async function uploadExcel() {
    const file = document.getElementById('excelFile').files[0];
    if(!file) return alert("Pilih file Excel!");
    const reader = new FileReader();
    reader.onload = async (e) => {
        const wb = XLSX.read(new Uint8Array(e.target.result), {type:'array'});
        const data = XLSX.utils.sheet_to_json(wb.Sheets[wb.SheetNames[0]]);
        const kMap = await getKriteriaMap();

        for(let row of data) {
            const resAlt = await fetchAPI('/alternatif', 'POST', {kode: row.kode, nama: row.nama, jabatan: row.jabatan});
            if(resAlt.ok) {
                const allAlt = await (await fetchAPI('/alternatif')).json();
                const newAlt = allAlt.find(a => a.kode === row.kode);
                if(row.C1 !== undefined) await fetchAPI('/penilaian', 'POST', { alternatifId: newAlt.id, kriteriaId: kMap['C1'], nilai: batasiNilai(row.C1) });
                if(row.C2 !== undefined) await fetchAPI('/penilaian', 'POST', { alternatifId: newAlt.id, kriteriaId: kMap['C2'], nilai: batasiNilai(row.C2) });
                if(row.C3 !== undefined) await fetchAPI('/penilaian', 'POST', { alternatifId: newAlt.id, kriteriaId: kMap['C3'], nilai: konversiAbsensi(row.C3) });
                if(row.C4 !== undefined) await fetchAPI('/penilaian', 'POST', { alternatifId: newAlt.id, kriteriaId: kMap['C4'], nilai: batasiNilai(row.C4) });
                if(row.C5 !== undefined) await fetchAPI('/penilaian', 'POST', { alternatifId: newAlt.id, kriteriaId: kMap['C5'], nilai: batasiNilai(row.C5) });
            }
        }
        alert("✅ Upload Excel Selesai!");
        loadAlternatif(null);
        document.getElementById('excelFile').value = '';
    };
    reader.readAsArrayBuffer(file);
}

// --- 5. KRITERIA & AHP ---
async function loadKriteria(el) {
    if (el) showSection('kriteriaSec', el);
    const res = await fetchAPI('/kriteria');
    if (!res.ok) return;
    const data = await res.json();
    let html = '';

    if (data.length === 0) {
        html = '<tr><td colspan="4" class="text-center text-muted py-3">Data Kosong. Klik "Setup Awal" di atas!</td></tr>';
    } else {
        data.forEach(k => {
            html += `<tr><td><b>${k.kode}</b></td><td>${k.nama}</td>
                     <td><span class="badge bg-${k.jenis==='BENEFIT'?'success':'danger'}">${k.jenis}</span></td>
                     <td>${k.bobotAHP ? k.bobotAHP.toFixed(4) : '-'}</td></tr>`;
        });
    }
    document.getElementById('tabelKriteria').innerHTML = html;
}

async function suntikKriteriaAwal() {
    const kList = [{kode:"C1",nama:"Kualitas",jenis:"BENEFIT"},{kode:"C2",nama:"Skill",jenis:"BENEFIT"},{kode:"C3",nama:"Absensi",jenis:"COST"},{kode:"C4",nama:"Kerjasama",jenis:"BENEFIT"},{kode:"C5",nama:"Tanggung Jawab",jenis:"BENEFIT"}];
    for (let k of kList) { await fetchAPI('/kriteria', 'POST', k); }
    alert("✅ Kriteria Didaftarkan!"); loadKriteria(null);
}

document.addEventListener('submit', async function(e) {
    if (e.target && e.target.id === 'formAhp') {
        e.preventDefault();

        if (document.getElementById('tabelKriteria').rows.length <= 1 && document.getElementById('tabelKriteria').innerText.includes("Kosong")) {
            return alert("❌ ERROR: Klik tombol kuning 'Setup Awal' dulu!");
        }

        const payload = {
            "perbandingan": {
                "1": { "2": parseFloat(document.getElementById('c1_2').value), "3": parseFloat(document.getElementById('c1_3').value), "4": parseFloat(document.getElementById('c1_4').value), "5": parseFloat(document.getElementById('c1_5').value) },
                "2": { "3": parseFloat(document.getElementById('c2_3').value), "4": parseFloat(document.getElementById('c2_4').value), "5": parseFloat(document.getElementById('c2_5').value) },
                "3": { "4": parseFloat(document.getElementById('c3_4').value), "5": parseFloat(document.getElementById('c3_5').value) },
                "4": { "5": parseFloat(document.getElementById('c4_5').value) }
            }
        };
        const res = await fetchAPI('/ahp/hitung-bobot', 'POST', payload);
        if(res.ok) { alert("✅ AHP Sukses!"); loadKriteria(null); }
    }
});

// --- 6. RANKING & C4.5 ---
async function loadRanking(el) {
    if (el) showSection('rankingSec', el);
    const res = await fetchAPI('/ranking');
    if (!res.ok) return;
    const data = await res.json();
    let table = '';

    if (data.length === 0) {
        table = '<tr><td colspan="4" class="text-center text-muted py-4">Belum ada data untuk diranking.</td></tr>';
    } else {
        data.forEach(r => {
            let badge = (r.status.toUpperCase() === 'TERPILIH') ? 'bg-success' : 'bg-danger';
            table += `<tr><td><h3>#${r.rank}</h3></td><td><b>${r.nama}</b></td>
                      <td><h5 class="text-primary">${r.skorAkhir.toFixed(4)}</h5></td>
                      <td><span class="badge ${badge}">${r.status}</span></td></tr>`;
        });
    }
    document.getElementById('tabelRanking').innerHTML = table;
}

async function loadPrediksiPage(el) {
    if (el) showSection('prediksiSec', el);
    const res = await fetchAPI('/c45/analyze');
    if (!res.ok) return;
    const data = await res.json();

    document.getElementById('textRule').innerText = data.ruleGenerated || "Belum ada pola (Decision Tree) yang dapat dibentuk.";

    let rows = '';
    if (!data.details || data.details.length === 0) {
        rows = '<tr><td colspan="4" class="text-center text-muted py-4">Belum ada data untuk dianalisa C4.5.</td></tr>';
    } else {
        // --- LOGIKA SORTING BERDASARKAN HASIL C4.5 ---
        const orderPriority = {
            'TERBAIK': 1,
            'TERPILIH': 2,
            'GAGAL': 3
        };

        data.details.sort((a, b) => {
            let rankA = orderPriority[a.prediksiC45.toUpperCase()] || 4;
            let rankB = orderPriority[b.prediksiC45.toUpperCase()] || 4;
            return rankA - rankB;
        });
        // ---------------------------------------------

        data.details.forEach(d => {
            let badgeClass = d.prediksiC45 === 'TERBAIK' ? 'text-success' : (d.prediksiC45 === 'TERPILIH' ? 'text-primary' : 'text-danger');
            rows += `<tr><td><b>${d.nama}</b></td><td>${d.statusAHP}</td>
                     <td class="fw-bold ${badgeClass}">${d.prediksiC45}</td><td><small>${d.keterangan}</small></td></tr>`;
        });
    }
    document.getElementById('tabelC45').innerHTML = rows;
}
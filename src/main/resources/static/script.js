const API_URL = "http://localhost:8080/api";
const token = localStorage.getItem('token');

if (!token) {
    alert("Silakan login dulu!");
    window.location.href = 'index.html';
}

function logout() {
    localStorage.removeItem('token');
    window.location.href = 'index.html';
}

async function fetchAPI(endpoint, method = 'GET', body = null) {
    const options = {
        method: method,
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' }
    };
    if (body) options.body = JSON.stringify(body);
    try { return await fetch(`${API_URL}${endpoint}`, options); } 
    catch (e) { console.error(e); alert("Koneksi Backend Gagal!"); }
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

// Helper: Ambil Map ID Kriteria
async function getKriteriaMap() {
    const kriteria = await (await fetchAPI('/kriteria')).json();
    const map = {};
    kriteria.forEach(k => map[k.kode] = k.id);
    return map;
}

// Helper: Konversi Hari Absen (C3)
function konversiAbsensi(hari) {
    let absen = parseFloat(hari);
    if (isNaN(absen)) return 0;
    if (absen === 0) return 0;
    else if (absen >= 1 && absen <= 3) return 2;
    else if (absen >= 4 && absen <= 7) return 5.5;
    else if (absen >= 8 && absen <= 10) return 9;
    else return 12;
}

// Helper: Batasi Nilai Skala 1 - 10 (C1, C2, C4, C5)
function batasiNilai(nilai) {
    let n = parseFloat(nilai);
    if (isNaN(n)) return 0;
    if (n > 10) return 10.0; // PENGAMAN: Jika lebih dari 10, pangkas jadi 10
    if (n < 0) return 0.0;   // PENGAMAN: Tidak boleh minus
    return n;
}

// === FITUR DATA KARYAWAN & PENILAIAN ===
async function loadAlternatif(el) {
    showSection('alternatifSec', el);
    const data = await (await fetchAPI('/alternatif')).json();
    let html = '';
    data.forEach((item, idx) => {
        html += `<tr><td>${idx + 1}</td><td><span class="badge bg-primary">${item.kode}</span></td>
                 <td>${item.nama}</td><td>${item.jabatan}</td>
                 <td><button class="btn btn-sm btn-danger" onclick="hapusAlternatif(${item.id})">Hapus</button></td></tr>`;
    });
    document.getElementById('tabelAlternatif').innerHTML = html;
}

async function addAlternatif() {
    const kode = document.getElementById('altKode').value;
    const nama = document.getElementById('altNama').value;
    const jabatan = document.getElementById('altJabatan').value;
    
    // Ambil dan batasi nilainya langsung
    const c1 = batasiNilai(document.getElementById('valC1').value);
    const c2 = batasiNilai(document.getElementById('valC2').value);
    const c3 = document.getElementById('valC3').value; // Absensi diproses terpisah
    const c4 = batasiNilai(document.getElementById('valC4').value);
    const c5 = batasiNilai(document.getElementById('valC5').value);

    if(!kode || !nama || c3 === '') {
        return alert("Harap isi lengkap data karyawan!");
    }

    const resAlt = await fetchAPI('/alternatif', 'POST', {kode, nama, jabatan});
    if(!resAlt.ok) return alert("Gagal menyimpan! Kode mungkin sudah ada.");

    const allAlt = await (await fetchAPI('/alternatif')).json();
    const newAlt = allAlt.find(a => a.kode === kode);
    if(!newAlt) return;

    let nilaiAbsensi = konversiAbsensi(c3);
    const kMap = await getKriteriaMap();

    // Simpan nilai yang sudah di-filter
    await fetchAPI('/penilaian', 'POST', { alternatifId: newAlt.id, kriteriaId: kMap['C1'], nilai: c1 });
    await fetchAPI('/penilaian', 'POST', { alternatifId: newAlt.id, kriteriaId: kMap['C2'], nilai: c2 });
    await fetchAPI('/penilaian', 'POST', { alternatifId: newAlt.id, kriteriaId: kMap['C3'], nilai: nilaiAbsensi });
    await fetchAPI('/penilaian', 'POST', { alternatifId: newAlt.id, kriteriaId: kMap['C4'], nilai: c4 });
    await fetchAPI('/penilaian', 'POST', { alternatifId: newAlt.id, kriteriaId: kMap['C5'], nilai: c5 });

    alert("Berhasil! Nilai telah divalidasi dan disimpan.");
    
    document.getElementById('altKode').value = ''; document.getElementById('altNama').value = ''; document.getElementById('altJabatan').value = '';
    document.getElementById('valC1').value = ''; document.getElementById('valC2').value = ''; document.getElementById('valC3').value = ''; 
    document.getElementById('valC4').value = ''; document.getElementById('valC5').value = '';
    
    loadAlternatif();
}

async function uploadExcel() {
    const file = document.getElementById('excelFile').files[0];
    if(!file) return alert("Pilih file Excel!");
    
    const reader = new FileReader();
    reader.onload = async (e) => {
        const wb = XLSX.read(new Uint8Array(e.target.result), {type:'array'});
        const data = XLSX.utils.sheet_to_json(wb.Sheets[wb.SheetNames[0]]);
        const kMap = await getKriteriaMap();
        let suksesCount = 0;

        for(let row of data) {
            const resAlt = await fetchAPI('/alternatif', 'POST', {kode: row.kode, nama: row.nama, jabatan: row.jabatan});
            if(resAlt.ok) {
                const allAlt = await (await fetchAPI('/alternatif')).json();
                const newAlt = allAlt.find(a => a.kode === row.kode);
                
                let valC3 = konversiAbsensi(row.C3);
                // Batasi nilai excel secara otomatis
                if(row.C1 !== undefined) await fetchAPI('/penilaian', 'POST', { alternatifId: newAlt.id, kriteriaId: kMap['C1'], nilai: batasiNilai(row.C1) });
                if(row.C2 !== undefined) await fetchAPI('/penilaian', 'POST', { alternatifId: newAlt.id, kriteriaId: kMap['C2'], nilai: batasiNilai(row.C2) });
                if(row.C3 !== undefined) await fetchAPI('/penilaian', 'POST', { alternatifId: newAlt.id, kriteriaId: kMap['C3'], nilai: valC3 });
                if(row.C4 !== undefined) await fetchAPI('/penilaian', 'POST', { alternatifId: newAlt.id, kriteriaId: kMap['C4'], nilai: batasiNilai(row.C4) });
                if(row.C5 !== undefined) await fetchAPI('/penilaian', 'POST', { alternatifId: newAlt.id, kriteriaId: kMap['C5'], nilai: batasiNilai(row.C5) });
                
                suksesCount++;
            }
        }
        alert(`Upload Selesai! ${suksesCount} data tervalidasi dan disimpan.`);
        loadAlternatif();
    };
    reader.readAsArrayBuffer(file);
}

async function hapusAlternatif(id) {
    if(confirm("Yakin hapus karyawan beserta semua nilainya?")) { await fetchAPI(`/alternatif/${id}`, 'DELETE'); loadAlternatif(); }
}

// === FITUR KRITERIA ===
async function loadKriteria(el) {
    showSection('kriteriaSec', el);
    const data = await (await fetchAPI('/kriteria')).json();
    let html = '';
    data.forEach(k => {
        html += `<tr><td><b>${k.kode}</b></td><td>${k.nama}</td><td><span class="badge bg-${k.jenis==='BENEFIT'?'success':'danger'}">${k.jenis}</span></td><td>${k.bobotAHP ? k.bobotAHP.toFixed(4) : '-'}</td></tr>`;
    });
    document.getElementById('tabelKriteria').innerHTML = html;
}

// === HASIL RANKING AHP ===
async function loadRanking(el) {
    showSection('rankingSec', el);
    const data = await (await fetchAPI('/ranking')).json();
    let table = '';
    data.forEach(r => {
        let badge = (r.status.toUpperCase() === 'TERPILIH') ? 'bg-success' : 'bg-danger';
        table += `
            <tr>
                <td><h3>#${r.rank}</h3></td>
                <td><b>${r.nama}</b><br><small class="text-muted">${r.kode}</small></td>
                <td><h5 class="text-primary">${r.skorAkhir.toFixed(4)}</h5></td>
                <td><span class="badge ${badge} p-2">${r.status.toUpperCase()}</span></td>
            </tr>`;
    });
    document.getElementById('tabelRanking').innerHTML = table;
}

// === C4.5 OTOMATIS BERJALAN ===
async function loadPrediksiPage(el) {
    showSection('prediksiSec', el);
    // Langsung jalankan fungsi analisa begitu tab dibuka
    jalankanC45();
}

async function jalankanC45() {
    const data = await (await fetchAPI('/c45/analyze')).json();

    document.getElementById('textRule').innerText = data.ruleGenerated;

    let rows = '';
    data.details.forEach(d => {
        let ahpBadge = (d.statusAHP.toUpperCase() === 'TERPILIH') ? 'bg-success' : 'bg-danger';
        
        let colorC45 = 'text-warning'; 
        if (d.prediksiC45 === 'TERBAIK') colorC45 = 'text-success';
        if (d.prediksiC45 === 'TERPILIH') colorC45 = 'text-primary';
        if (d.prediksiC45 === 'GAGAL') colorC45 = 'text-danger';

        rows += `
            <tr>
                <td><b>${d.nama}</b></td>
                <td><span class="badge ${ahpBadge}">${d.statusAHP}</span></td>
                <td class="${colorC45} fw-bold">${d.prediksiC45}</td>
                <td><small>${d.keterangan}</small></td>
            </tr>`;
    });
    document.getElementById('tabelC45').innerHTML = rows;
}
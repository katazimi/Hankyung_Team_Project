/**
 * 
 */

let chartInstance = null; // 수익률 곡선 차트
let donutChartInstance = null; // 도넛 차트

document.addEventListener('DOMContentLoaded', () => {
    // [수정] 페이지 로드 시 DB에서 자산 불러오기
    loadMyAssets();
});

// [추가] DB에서 내 자산 목록 불러오기 (GET)
function loadMyAssets() {
    fetch('/api/my-assets')
        .then(res => {
            if (res.status === 401) return []; // 비로그인
            return res.json();
        })
        .then(data => {
            document.getElementById('assetList').innerHTML = ''; // 리스트 초기화
            
            if (data.length > 0) {
                data.forEach(asset => {
                    // DB ID를 포함하여 테이블에 추가
                    addAssetToTable(asset.stockName, asset.stockCode, asset.weight, asset.id);
                });
                // 데이터가 있을 때만 차트 업데이트
                const chartData = data.map(d => ({name: d.stockName, weight: d.weight}));
                updateDonutChart(chartData);
            } else {
                // 데이터가 없으면 빈 차트
                updateTotalWeight();
                updateDonutChart([]); 
            }
        });
}

// [추가] 자산 DB 저장 (POST)
function addAssetToDB(name, code) {
    fetch('/api/my-assets', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ stockName: name, stockCode: code })
    })
    .then(async res => {
        if (res.status === 401) { alert("로그인이 필요합니다."); return; }
        if (res.status === 400) { alert("이미 추가된 종목입니다."); return; }
        if (!res.ok) throw new Error("추가 실패");
        return res.json();
    })
    .then(savedAsset => {
        if(savedAsset) {
            addAssetToTable(savedAsset.stockName, savedAsset.stockCode, 0, savedAsset.id);
            closeSearchModal();
            ensureStockDataForBacktest(savedAsset.stockCode, savedAsset.stockName);
        }
    })
    .catch(err => console.error(err));
}

// [추가] 비중 DB 수정 (PUT)
function updateWeightDB(id, weight) {
    if (!id || id === 'null') return;
    fetch(`/api/my-assets/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: weight
    });
}

// [추가] 자산 DB 삭제 (DELETE)
function deleteAssetDB(rowElement, id) {
    if(!confirm("삭제하시겠습니까?")) return;

    if (id && id !== 'null') {
        fetch(`/api/my-assets/${id}`, { method: 'DELETE' })
            .then(res => {
                if (res.ok) {
                    rowElement.remove();
                    updateTotalWeight();
                }
            });
    } else {
        rowElement.remove();
        updateTotalWeight();
    }
}

// ------------------------
// 모달 공통 함수
// ------------------------
function openAnyModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.style.display = 'flex';
    setTimeout(() => { modal.classList.add('show'); }, 10);
}
function closeAnyModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.classList.remove('show');
    setTimeout(() => { modal.style.display = 'none'; }, 300);
}

// ------------------------
// 자산 리스트 관리
// ------------------------
function addAssetToTable(name, code, weight = 0, dbId = null) {
    const div = document.createElement('div');
    div.className = 'asset-row';
    // [수정] DB 연동을 위한 oninput, onchange, onclick 이벤트 연결
    div.innerHTML = `
        <div class="asset-info">
            ${name}
            <span class="asset-code">${code}</span>
            <input type="hidden" class="code-input" value="${code}">
            <input type="hidden" class="name-input" value="${name}">
        </div>
        <input type="number" class="weight-input" value="${weight}" 
               oninput="updateTotalWeight()" 
               onchange="updateWeightDB('${dbId}', this.value)" 
               placeholder="%">
        <span style="font-size:14px; color:#666;">%</span>
        <button class="btn-remove" onclick="deleteAssetDB(this.parentElement, '${dbId}')">×</button>
    `;
    document.getElementById('assetList').appendChild(div);
    updateTotalWeight();
}

function updateTotalWeight() {
    let sum = 0;
    const currentAssets = [];
    document.querySelectorAll('.asset-row').forEach(row => {
        const w = Number(row.querySelector('.weight-input').value);
        const n = row.querySelector('.name-input').value;
        sum += w;
        if(w > 0) currentAssets.push({name: n, weight: w});
    });
    
    const span = document.getElementById('totalWeight');
    span.innerText = sum;
    span.style.color = sum === 100 ? '#2563eb' : '#e11d48';
    
    // 비중 변경 시 실시간 도넛 차트 업데이트 (선택 사항)
     updateDonutChart(currentAssets); 
}

// ------------------------
// 분석 실행
// ------------------------
function runAnalysis() {
    const seed = document.getElementById('seedMoney').value;
    const period = document.getElementById('period').value;
    const benchmark = document.getElementById('benchmark').value;
    const rows = document.querySelectorAll('.asset-row');
    
    const assets = [];
    let weightSum = 0;

    rows.forEach(row => {
        const code = row.querySelector('.code-input').value;
        const name = row.querySelector('.name-input').value;
        const weight = Number(row.querySelector('.weight-input').value);
        
        if(weight > 0) {
            assets.push({ code: code, name: name, weight: weight });
            weightSum += weight;
        }
    });

    if(assets.length === 0) { alert("종목을 추가해주세요."); return; }
    if(weightSum !== 100) { alert("비중 합계는 100%여야 합니다."); return; }

    const req = { seedMoney: seed, periodMonths: period, assets: assets, benchmarkCode: benchmark };
    document.querySelector('.btn-submit').innerText = "분석 중...";

    fetch('/api/allocation/run', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(req)
    })
    .then(res => res.json())
    .then(data => {
        renderResult(data);
        // updateDonutChart(assets); // updateTotalWeight에서 이미 수행함
    })
    .catch(err => alert("분석 실패: " + err))
    .finally(() => { document.querySelector('.btn-submit').innerText = "Analyze Portfolio ▶"; });
}

function renderResult(data) {
    // Portfolio 값 채우기
    document.getElementById('resFinal').innerText = Math.round(data.finalBalance).toLocaleString();
    document.getElementById('resTotal').innerText = data.totalReturn + "%";
    document.getElementById('resCagr').innerText = data.cagr + "%";
    document.getElementById('resVol').innerText = data.volatility + "%";
    document.getElementById('resMdd').innerText = data.mdd + "%";
    document.getElementById('resSharpe').innerText = data.sharpeRatio;

    // Benchmark 값 채우기 (데이터 없으면 '-')
    const hasBm = data.bmFinalBalance > 0;
    document.getElementById('bmFinal').innerText = hasBm ? Math.round(data.bmFinalBalance).toLocaleString() : '-';
    document.getElementById('bmTotal').innerText = hasBm ? data.bmTotalReturn + "%" : '-';
    document.getElementById('bmCagr').innerText = hasBm ? data.bmCagr + "%" : '-';
    document.getElementById('bmVol').innerText = hasBm ? data.bmVolatility + "%" : '-';
    document.getElementById('bmMdd').innerText = hasBm ? data.bmMdd + "%" : '-';
    document.getElementById('bmSharpe').innerText = hasBm ? data.bmSharpeRatio : '-';

    // Chart 그리기 (데이터셋 2개)
    const ctx = document.getElementById('allocChart').getContext('2d');
    if(chartInstance) chartInstance.destroy();

    const datasets = [{
        label: 'Portfolio',
        data: data.equityCurve.map(d => d.value),
        borderColor: '#2563eb', // 파랑
        backgroundColor: 'rgba(37, 99, 235, 0.1)',
        fill: true, pointRadius: 0, borderWidth: 2
    }];

    // 벤치마크 데이터가 있으면 차트에 추가
    if (hasBm && data.bmEquityCurve) {
        datasets.push({
            label: 'Benchmark',
            data: data.bmEquityCurve.map(d => d.value),
            borderColor: '#e11d48', // 빨강
            borderDash: [5, 5], // 점선
            backgroundColor: 'transparent',
            fill: false, pointRadius: 0, borderWidth: 2
        });
    }

    chartInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels: data.equityCurve.map(d => d.date),
            datasets: datasets
        },
        options: {
            responsive: true, maintainAspectRatio: false,
            interaction: { mode: 'index', intersect: false },
            plugins: { tooltip: { callbacks: { label: (ctx) => ctx.dataset.label + ": " + Number(ctx.raw).toLocaleString() + " 원" } } },
            scales: { x: { grid: { display: false } }, y: { beginAtZero: false } }
        }
    });
}

// 도넛 차트 업데이트 함수
function updateDonutChart(assets) {
    const ctx = document.getElementById('donutChart').getContext('2d');
    if(donutChartInstance) donutChartInstance.destroy();

    const colors = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#6366f1'];
    
    // 데이터 없을 때 빈 차트 방지
    if(!assets || assets.length === 0) return;

    donutChartInstance = new Chart(ctx, {
        type: 'doughnut',
        plugins: [ChartDataLabels], // 플러그인 활성화
        data: {
            labels: assets.map(a => a.name),
            datasets: [{
                data: assets.map(a => a.weight),
                backgroundColor: colors.slice(0, assets.length),
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { 
                    display: true, 
                    position: 'right', // 범례 하단 배치
                    align: 'start',
                    labels: { boxWidth: 10, font: { size: 11 }, padding: 10 }
                },
                datalabels: {
                    color: '#fff',
                    font: { weight: 'bold', size: 12 },
                    formatter: (value) => value >= 5 ? value + '%' : '' // 5% 미만은 숨김
                },
                tooltip: { callbacks: { label: (ctx) => ` ${ctx.label}: ${ctx.raw}%` } }
            },
            cutout: '60%',
            layout: { padding: 10 }
        }
    });
}

// ------------------------
// 모달 기능 (검색 & 기록)
// ------------------------
function openSearchModal() {
    document.getElementById('modalSearchInput').value = '';
    document.getElementById('modalSearchList').style.display = 'none';
    openAnyModal('searchModal');
    setTimeout(() => document.getElementById('modalSearchInput').focus(), 100);
}
function closeSearchModal() { closeAnyModal('searchModal'); }

document.getElementById('modalSearchInput').addEventListener('keyup', function() {
    const keyword = this.value.trim();
    const list = document.getElementById('modalSearchList');
    if(keyword.length < 1) { list.style.display = 'none'; return; }

    fetch(`/api/stock/search?keyword=${keyword}`)
        .then(res => res.json())
        .then(data => {
            list.innerHTML = '';
            if(data.length > 0) {
                list.style.display = 'block';
                data.forEach(stock => {
                    const li = document.createElement('li');
                    li.innerHTML = `${stock.name} <span style="color:#999; font-size:12px;">${stock.code}</span>`;
                    li.onclick = () => {
                        // [수정] 클릭 시 DB 저장 함수 호출
                        addAssetToDB(stock.name, stock.code);
                    };
                    list.appendChild(li);
                });
            } else { list.style.display = 'none'; }
        });
});

function openHistoryModal() {
    fetch('/api/allocation/history')
        .then(res => {
            if (res.status === 401) { alert("로그인이 필요합니다."); return null; }
            return res.json();
        })
        .then(list => {
            if(!list) return;
            const tbody = document.getElementById('historyList');
            tbody.innerHTML = '';
            //여기부터 수정
			
         	// 모달 요소 가져오기
            const modal = document.getElementById('historyModal');
            
            const modalBody = modal.querySelector('.modal-body');
            if (modalBody) {
                modalBody.scrollTop = 0;
            }

            modal.style.display = 'flex';
            setTimeout(() => { modal.classList.add('show'); }, 10);
            
            //여기까지 수정
            if (list.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding:20px;">기록이 없습니다.</td></tr>';
                return;
            }

            list.forEach(item => {
                const row = document.createElement('tr');
                const profitClass = item.totalReturn > 0 ? 'up' : 'down';
                const jsonStr = item.assetsJson.replace(/"/g, '&quot;');

                row.innerHTML = `
                    <td>${item.date}</td>
                    <td>${item.assetsSummary}</td>
                    <td style="text-align:right;">${item.seedMoney.toLocaleString()}</td>
                    <td style="text-align:right;" class="${profitClass}">${item.totalReturn}%</td>
                    <td style="text-align:center;">
                        <button class="btn-load" onclick="restoreSettings('${item.seedMoney}', '${item.periodMonths}', '${jsonStr}')">불러오기</button>
                    </td>
                `;
                tbody.appendChild(row);
            });
        });
}
function closeHistoryModal() { closeAnyModal('historyModal'); }

function restoreSettings(seed, period, assetsJson) {
    if (!confirm("설정값을 불러오시겠습니까?")) return;
    document.getElementById('seedMoney').value = seed;
    document.getElementById('period').value = period;
    document.getElementById('assetList').innerHTML = '';
    
    const assets = JSON.parse(assetsJson.replace(/&quot;/g, '"'));
    
    // 불러온 값은 분석용이므로 DB ID 없이 화면에만 추가 (수정 시 저장 안됨)
    assets.forEach(a => addAssetToTable(a.name, a.code, a.weight, null));
    
    updateTotalWeight();
    // updateDonutChart(assets); // updateTotalWeight가 호출하므로 중복 제거
    closeHistoryModal();
}

window.addEventListener('click', (e) => {
    if (e.target.classList.contains('modal-overlay')) {
        e.target.classList.remove('show');
        setTimeout(() => { e.target.style.display = 'none'; }, 300);
    }
});

//modal 검색 후 데이터 확인 및 수집
function ensureStockDataForBacktest(code, name) {
    fetch(`/api/stock/${code}/candle-data?type=D`)
        .then(res => res.json())
        .then(data => {
            if (!Array.isArray(data) || data.length === 0) {
                if (confirm(`[${name}] 데이터가 없습니다.\n데이터를 수집하시겠습니까?`)) {
                    collectDataForBacktest(code, name);
                }
            }
        })
        .catch(err => console.error(err));
}

function collectDataForBacktest(code, name) {
    alert("수집 시작...");
    fetch(`/api/collect/${code}`).then(() => {
        let checkInterval = setInterval(() => {
            fetch(`/api/stock/${code}/candle-data?type=D`)
                .then(res => res.json())
                .then(data => {
                    if (data.length > 100) {   // 임시 완료 기준
                        clearInterval(checkInterval);
                        alert(`[${name}] 데이터 수집 완료!`);
                        // 필요하면 여기서 backtest 시작 함수 호출
                        // startBacktest(code, name);
                    }
                });
        }, 3000);
    });
}
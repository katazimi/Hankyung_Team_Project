/**
 * 
 */

   document.addEventListener('DOMContentLoaded', () => {
       loadPortfolio();
   });

   function loadPortfolio() {
       fetch('/api/portfolio')
           .then(res => res.json())
           .then(data => {
               renderPortfolio(data);
               calculateSummary(data);
           })
           .catch(err => console.error(err));
   }

   function renderPortfolio(data) {
       const tbody = document.getElementById('portfolioList');
       tbody.innerHTML = '';
       
       if (data.length === 0) {
           tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; padding: 20px;">보유 중인 종목이 없습니다.</td></tr>';
           return;
       }

       data.forEach(item => {
           const colorClass = item.profit > 0 ? 'up' : (item.profit < 0 ? 'down' : '');
           
           const tr = document.createElement('tr');
           tr.innerHTML = `
               <td>
                   <span class="stock-name" style="font-weight: bold;">${item.name}</span>
                   <br><span class="stock-code" style="font-size: 12px; color: #888;">${item.code}</span>
               </td>
               <td>${item.quantity.toLocaleString()} 주</td>
               <td>${item.averagePrice.toLocaleString()}</td>
               <td>${item.currentPrice.toLocaleString()}</td>
               <td>${item.totalValue.toLocaleString()}</td>
               <td class="${colorClass}">${item.profit.toLocaleString()}</td>
               <td class="${colorClass}">${item.profitRate}%</td>
               <td style="text-align: center;">
                   <button onclick="deletePortfolio(${item.id})" style="color: red; background: none; border: none; cursor: pointer;">🗑</button>
               </td>
           `;
           tbody.appendChild(tr);
       });
   }

   function calculateSummary(data) {
       let tInvested = 0;
       let tValue = 0;

       data.forEach(d => {
           tInvested += d.totalInvested;
           tValue += d.totalValue;
       });

       const tProfit = tValue - tInvested;
       const tRate = tInvested > 0 ? ((tProfit / tInvested) * 100).toFixed(2) : "0.00";
       
       document.getElementById('totalInvested').innerText = tInvested.toLocaleString() + " 원";
       document.getElementById('totalValue').innerText = tValue.toLocaleString() + " 원";
       
       const profitEl = document.getElementById('totalProfit');
       profitEl.innerText = tProfit.toLocaleString() + " 원";
       profitEl.className = "summary-value " + (tProfit > 0 ? 'up' : (tProfit < 0 ? 'down' : ''));
       
       const rateEl = document.getElementById('totalRate');
       rateEl.innerText = tRate + " %";
       rateEl.style.color = tProfit > 0 ? '#e12b2b' : (tProfit < 0 ? '#2b78e1' : '#666');
   }

   // --- 모달 및 추가 기능 ---
   function openAddModal() { document.getElementById('addModal').style.display = 'flex'; }
   function closeAddModal() { document.getElementById('addModal').style.display = 'none'; }

   // 종목 검색 (기존 API 활용)
   function searchStock() {
       const keyword = document.getElementById('pSearchInput').value;
       fetch(`/api/stock/search?keyword=${keyword}`)
           .then(res => res.json())
           .then(list => {
               const ul = document.getElementById('pSearchResult');
               ul.innerHTML = '';
               ul.style.display = 'block';
               list.forEach(stock => {
                   const li = document.createElement('li');
                   li.innerText = `${stock.name} (${stock.code})`;
                   li.style.padding = '5px';
                   li.style.cursor = 'pointer';
                   li.onclick = () => {
                       document.getElementById('selectedStockName').value = stock.name;
                       document.getElementById('selectedStockCode').value = stock.code;
                       ul.style.display = 'none';
                   };
                   ul.appendChild(li);
               });
           });
   }

   function submitPortfolio() {
       const code = document.getElementById('selectedStockCode').value;
       const name = document.getElementById('selectedStockName').value;
       const price = document.getElementById('buyPrice').value;
       const quantity = document.getElementById('quantity').value;

       if(!code || !price || !quantity) {
           alert("모든 정보를 입력해주세요.");
           return;
       }

       fetch('/api/portfolio', {
           method: 'POST',
           headers: {'Content-Type': 'application/x-www-form-urlencoded'},
           body: `code=${code}&name=${name}&price=${price}&quantity=${quantity}`
       }).then(res => {
           if(res.ok) {
               closeAddModal();
               loadPortfolio();
           } else {
               alert("추가 실패");
           }
       });
   }

   function deletePortfolio(id) {
       if(confirm("삭제하시겠습니까?")) {
           fetch(`/api/portfolio/${id}`, { method: 'DELETE' })
               .then(res => {
                   if(res.ok) loadPortfolio();
               });
       }
   }
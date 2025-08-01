// ─── 전역 저장 변수 ───
let lastTempNum = null;
let guideData = null;
let currentGender = "male"; // 'male' 또는 'female'

// ─── ① 의상 가이드만 로드하는 함수 ───
function loadClothingGuide(tempNum) {
  fetch(`/api/coordi/guide?temp=${tempNum}`)
    .then(res => res.json())
    .then(data => {
      guideData = data;
      renderByGender(currentGender);
    })
    .catch(() => {
      const row1 = document.getElementById("clothing-guide-row1");
      const row2 = document.getElementById("clothing-guide-row2");
      row1.textContent = "추천 코디 정보를 가져오지 못했습니다";
      row2.textContent = "";
    });
}

// ─── ② BEST LOOKS만 로드하는 함수 ───
function loadBestLooksData(tempNum) {
  console.log(`▶ loadBestLooksData 호출 (temp=${tempNum})`);
  fetch(`/api/community/best?temp=${tempNum}`)
    .then(res => res.json())
    .then(renderBestLooks)
    .catch(() => {
      const area = document.getElementById("best-looks-area");
      if (area) area.innerHTML = "<div class='text-danger'>※BEST LOOKS 정보를 가져올 수 없습니다!※</div>";
    });
}

// ─── ③ 날씨 로드 시 — 두 함수 모두 실행 ───
window.addEventListener("weatherLoaded", e => {
  lastTempNum = e.detail.tempNum;
  loadClothingGuide(lastTempNum);
  loadBestLooksData(lastTempNum);
});

// ─── ④ DOMContentLoaded 시 — 버튼 클릭 & 커스텀 드롭다운 바인딩 ───
document.addEventListener("DOMContentLoaded", () => {
  // 성별 토글 버튼
  const toggleBtn = document.getElementById("toggle-gender-btn");
  if (toggleBtn) {
    toggleBtn.addEventListener("click", () => {
      currentGender = currentGender === "male" ? "female" : "male";
      toggleBtn.textContent = currentGender === "male" ? "🧑🏻" : "👧🏻";
      if (guideData) renderByGender(currentGender);
    });
  }

  // 리프레시 버튼 — 의상 가이드만 재호출
  const refreshBtn = document.getElementById("refresh-images-btn");
  if (refreshBtn) {
    refreshBtn.addEventListener("click", () => {
      if (lastTempNum == null) return;
      loadClothingGuide(lastTempNum);
    });
  }

  // custom dropdown 클릭 핸들러
  function getCurrentProvince() {
    const span = document.querySelector(".dosi");
    return span ? span.textContent.trim() : null;
  }
  function getCurrentRegion() {
    const span = document.querySelector(".sigungu");
    return span ? span.textContent.trim() : null;
  }

  async function onRegionChange(province, region) {
    console.log("▶ 지역 변경 이벤트", province, region);
    if (!province || !region) return;
    const cityParam = encodeURIComponent(`${province} ${region}`);
    try {
      const res = await fetch(`/api/weather/currentByRegion?city=${cityParam}`);
      const weatherData = await res.json();
      const tmpRaw = weatherData[0].tmp;
      const tempNum = parseInt(tmpRaw, 10);
      console.log(`▶ 날씨 API 응답 tmp = ${tmpRaw}, parsed tempNum = ${tempNum}`);

      lastTempNum = tempNum;
      document.getElementById("current-temp-tag").innerHTML = `‘현재 온도 기준(<b>${tempNum}℃</b>)’`;
      const bestTempTag = document.getElementById("best-looks-temp-tag");
      if (bestTempTag) bestTempTag.textContent = `${tempNum}℃`;
      loadClothingGuide(tempNum);
      loadBestLooksData(tempNum);
    } catch (err) {
      console.error("지역 변경 시 날씨 로드 실패", err);
    }
  }

  document.querySelectorAll(".list-do li").forEach(li => {
    li.addEventListener("click", () => onRegionChange(li.textContent.trim(), getCurrentRegion()));
  });
  document.querySelectorAll(".list-si li").forEach(li => {
    li.addEventListener("click", () => onRegionChange(getCurrentProvince(), li.textContent.trim()));
  });

  // ─── ⑤ MutationObserver로 날씨 위젯 직접 감지 ───
  const weatherTempEl = document.getElementById("weather-temp");
  if (weatherTempEl) {
    const observer = new MutationObserver(muts => {
      muts.forEach(m => {
        const text = weatherTempEl.textContent.trim();
        const match = text.match(/(-?\d+)\s*℃/);
        if (match) {
          const newTemp = parseInt(match[1], 10);
          if (newTemp !== lastTempNum) {
            console.log(`▶ 날씨 위젯 변경 감지: ${newTemp}℃ (이전 ${lastTempNum}℃)`);
            lastTempNum = newTemp;
            loadClothingGuide(newTemp);
            loadBestLooksData(newTemp);
          }
        }
      });
    });
    observer.observe(weatherTempEl, { childList: true, subtree: true, characterData: true });
  }
});

// ─── ⑥ 성별별 렌더 함수 ───
function renderByGender(gender) {
  document.getElementById("gender-label").innerHTML = gender === "male" ? "- 남성 -" : "- 여성 -";
  const data = guideData[gender];
  const row1 = document.getElementById("clothing-guide-row1");
  const row2 = document.getElementById("clothing-guide-row2");
  row1.innerHTML = "";
  row2.innerHTML = "";
  const onePieceTops = ["피케/카라 원피스","원피스","맥시드레스"];
  const isOnePiece = onePieceTops.includes(data.top.name);
  renderSlot("top", data.top, row1);
  if (data.outer?.name) renderSlot("outer", data.outer, row1);
  else row1.innerHTML += emptySlotMarkup("아우터");
  if (gender === "female" && isOnePiece) row2.innerHTML += emptySlotMarkup("하의");
  else if (data.bottom.name) renderSlot("bottom", data.bottom, row2);
  else row2.innerHTML += emptySlotMarkup("하의");
  renderSlot("shoes", data.shoes, row2);
  row1.style.gap = "50px";
  row2.style.gap = "50px";
}

// ─── 슬롯 카드 헬퍼 ───
function renderSlot(part, item, container) {
  if (!item.name) return;
  const encodedItemName = encodeURIComponent(item.name); // 공백 포함 그대로 query param으로
  const labelMap = {outer:"아우터",top:"상의",bottom:"하의",shoes:"신발"};
  container.innerHTML += `
    <div style="display:inline-block;width:150px;text-align:center;color:inherit;margin:0 6px;">
      <div style="
           width:150px;
           height:150px;
           border-radius:10px;
           border:1px solid #ddd;
           background:#fafafa;
           display:flex;
           align-items:center;
           justify-content:center;
           overflow:hidden;
           margin-bottom:6px;
         ">
        <a href="/products/${currentGender}?item=${encodedItemName}" style="display:block;width:100%;height:100%;text-decoration:none;color:inherit;">
          <img src="${item.imageUrl}" alt="${item.name}"
               style="width:100%;height:100%;object-fit:cover;"/>
        </a>
      </div>
      <b style="display:block;margin-bottom:2px;text-decoration:none;">${labelMap[part]}</b>
      <span style="font-size:14px;line-height:1.2;display:block;text-decoration:none;">${item.name}</span>
    </div>`;
}

// ─── 빈 슬롯 헬퍼 ───
function emptySlotMarkup(label) {
  return `
    <div style="text-align:center;width:150px;margin:0 6px;">
      <div style="width:150px;height:150px;border:1px solid #ddd;border-radius:10px;background:#fafafa;color:#888;display:flex;justify-content:center;align-items:center;margin-bottom:6px;font-weight:600;">
        ${label} 없음
      </div>
      <b style="display:block;margin-bottom:2px;">${label}</b>
    </div>`;
}

// ─── BEST LOOKS 렌더 ───
function renderBestLooks(data) {
  const area = document.getElementById("best-looks-area");
  if (!area) return;
  const styleList = [
    {key:"CASUAL",label:"캐주얼"},{key:"STREET",label:"스트리트"},
    {key:"FORMAL",label:"포멀"},{key:"OUTDOOR",label:"기타"}
  ];
  let html = "";
  styleList.forEach(({key,label}) => {
    const post = data[key];
    html += `<div class="col-md-3 mb-4 d-flex"><div class="card flex-fill h-100"><div class="card-style-header">${label}</div><div class="card-body d-flex flex-column align-items-center">`;
    if (post) {
      html += `<div class="post-title mb-2" style="font-size:1.25rem;font-weight:600;">${post.title}</div>
               <div class="badge-list mb-3">${post.casual?'<span class="badge bg-secondary me-1">캐주얼</span>':''}${post.street?'<span class="badge bg-secondary me-1">스트리트</span>':''}${post.formal?'<span class="badge bg-secondary me-1">포멀</span>':''}${post.outdoor?'<span class="badge bg-secondary me-1">기타</span>':''}</div>
               <a href="/community/detail/${post.id}"><img src="/uploads/${post.repImageUrl}" class="mb-3" style="max-width:130px;max-height:130px;border-radius:10px;" alt="코디 이미지"/></a>
               <div class="text-muted mb-1">${post.authorNickname}</div>
               <div class="text-muted mb-3">추천수: ${post.recommendCount}</div>`;
    } else {
      html += `<div class="mt-5 text-muted">게시글 없음</div>`;
    }
    html += `</div><div class="card-footer bg-white border-top-0"><a class="btn btn-primary btn-sm w-100" href="/community/list?type=&keyword=&styleNames=${key}">${label} 게시글보기</a></div></div></div>`;
  });
  area.innerHTML = `<div class="row gx-4 gx-lg-5 d-flex align-items-stretch">${html}</div>`;
}

// src/main/resources/static/js/main.js

// ─── 전역 저장 변수 ───
let lastTempNum = null;
let guideData = null;
let tempRanges = [];
let currentGender = window.initialGender || "male";

// AI 모드 변수
let useAiGuide = false;
let aiResults = [];
let indexByCat = {};
const categories = ["상의", "아우터", "하의", "신발"];

// 인덱스 순환용 유틸 함수
function clamp(i, len) {
  return len ? ((i % len) + len) % len : 0;
}

// ─── 로딩 스피너 제어 ───
function showGuideLoading() {
  document.getElementById("guide-loading-overlay").style.display = "block";
  document.getElementById("guide-loading-spinner").style.display = "block";
  document.getElementById("guide-loading-text").style.display = "block";
}
function hideGuideLoading() {
  document.getElementById("guide-loading-overlay").style.display = "none";
  document.getElementById("guide-loading-spinner").style.display = "none";
  document.getElementById("guide-loading-text").style.display = "none";
}

function showBestLoading() {
  document.getElementById("best-loading-overlay").style.display = "block";
  document.getElementById("best-loading-spinner").style.display = "block";
  document.getElementById("best-loading-text").style.display = "block";
}
function hideBestLoading() {
  document.getElementById("best-loading-overlay").style.display = "none";
  document.getElementById("best-loading-spinner").style.display = "none";
  document.getElementById("best-loading-text").style.display = "none";
}

// ─── 공통 슬롯 렌더 헬퍼 ───
function renderSlots(data, gender) {
  const row1 = document.getElementById("clothing-guide-row1");
  const row2 = document.getElementById("clothing-guide-row2");
  row1.innerHTML = "";
  row2.innerHTML = "";

  // 상의
  renderSlot("top", data.top, row1);
  // 아우터
  renderSlotOrEmpty("outer", data.outer, row1);
  // 하의 (원피스 예외)
  const onePieceTops = ["피케/카라 원피스", "원피스", "맥시드레스"];
  const topName = data.top.productName || data.top.name;
  const isOnePiece = onePieceTops.includes(topName);
  if (gender === "female" && isOnePiece) {
    row2.innerHTML += emptySlotMarkup("하의");
  } else {
    renderSlotOrEmpty("bottom", data.bottom, row2);
  }
  // 신발
  renderSlot("shoes", data.shoes, row2);

  row1.style.gap = row2.style.gap = "50px";
}

function renderSlotOrEmpty(part, item, container) {
  const labelMap = { top: "상의", outer: "아우터", bottom: "하의", shoes: "신발" };
  if (item && (item.productName || item.name)) {
    renderSlot(part, item, container);
  } else {
    container.innerHTML += emptySlotMarkup(labelMap[part]);
  }
}

function renderSlot(part, item, container) {
  const name = item.productName || item.name;
  if (!name) return;
  const imageUrl = item.imageUrl;
  const encoded = encodeURIComponent(name);
  const labelMap = { top: "상의", outer: "아우터", bottom: "하의", shoes: "신발" };

  container.innerHTML += `
    <div style="display:inline-block;width:150px;text-align:center;margin:0 6px;">
      <div style="
           width:150px;height:150px;
           border-radius:10px;border:1px solid #ddd;
           background:#fafafa;display:flex;
           align-items:center;justify-content:center;
           overflow:hidden;margin-bottom:6px;">
        <a href="/products/${currentGender}?item=${encoded}"
           style="display:block;width:100%;height:100%;color:inherit;">
          <img src="${imageUrl}" alt="${name}"
               style="width:100%;height:100%;object-fit:cover;"/>
        </a>
      </div>
      <b style="display:block;margin-bottom:2px;">${labelMap[part]}</b>
      <span class="product-name" style="font-size:14px;line-height:1.2;">${name}</span>
    </div>`;
}

function emptySlotMarkup(label) {
  return `
    <div style="text-align:center;width:150px;margin:0 6px;">
      <div style="
           width:150px;height:150px;
           border:1px solid #ddd;border-radius:10px;
           background:#fafafa;color:#888;
           display:flex;justify-content:center;
           align-items:center;margin-bottom:6px;
           font-weight:600;">
        ${label} 없음
      </div>
      <b style="display:block;margin-bottom:2px;">${label}</b>
    </div>`;
}

// ─── DB 가이드 로드 ───
function loadClothingGuide(tempNum) {
  useAiGuide = false;
  showGuideLoading();
  fetch(`/api/coordi/guide?temp=${tempNum}`)
    .then(res => res.json())
    .then(data => {
      guideData = data;
      renderByGender(currentGender);
      hideGuideLoading();
    })
    .catch(() => {
      document.getElementById("clothing-guide-row1").textContent = "추천 코디 정보를 가져오지 못했습니다";
      document.getElementById("clothing-guide-row2").textContent = "";
      hideGuideLoading();
    });
}

// ─── AI 가이드 로드 ───
async function fetchAiRecommendations(tempNum, gender) {
  useAiGuide = true;
  showGuideLoading();
  try {
    // ① JS 에서 프롬프트 직접 생성
    const koreaGender = gender === 'male' ? '남성' : '여성';
    const prompt = `섭씨 ${tempNum}도 날씨에 어울리는 ${koreaGender} 옷 추천해줘. 브랜드와 상품명 추천해주고, 상의, 아우터, 하의, 신발 순으로 추천해줘. 다른 부가설명은 하지마.`;

    // ② POST /api/aiguide 로 요청
    const res = await fetch('/api/aiguide', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ prompt })
    });
    if (!res.ok) throw new Error(res.status);

    // ③ 컨트롤러가 반환하는 List<OpenAIStylistRecommendationResult>
    aiResults = await res.json();

    // ④ 카테고리별 인덱스 초기화
    indexByCat = {};
    aiResults.forEach(block => {
      indexByCat[block.product.category] = 0;
    });

    // ⑤ 렌더
    renderAiRecommendations();
  } catch (e) {
    console.error("AI 추천 정보 로드 실패", e);
    document.getElementById("clothing-guide-row1").textContent = "AI 추천 정보를 가져오지 못했습니다.";
    document.getElementById("clothing-guide-row2").textContent = "";
  } finally {
    hideGuideLoading();
  }
}


function renderAiRecommendations() {
  const row1 = document.getElementById("clothing-guide-row1");
  const row2 = document.getElementById("clothing-guide-row2");
  row1.innerHTML = "";
  row2.innerHTML = "";

  aiResults.forEach(block => {
    const cat = block.product.category;
    const items = block.items || [];
    const idx = clamp(indexByCat[cat], items.length);
    const item = items[idx] || {};
    const container = (cat === "상의" || cat === "아우터") ? row1 : row2;

    container.innerHTML += `
      <div style="display:inline-block;width:150px;text-align:center;margin:0 6px;">
        <div style="
             width:150px;height:150px;
             border-radius:10px;border:1px solid #ddd;
             background:#fafafa;display:flex;
             align-items:center;justify-content:center;
             overflow:hidden;margin-bottom:6px;">
          <a href="${item.link || '#'}" target="_blank" rel="noreferrer"
             style="display:block;width:100%;height:100%;color:inherit;">
            <img src="${item.image || ''}" alt="${item.title || ''}"
                 style="width:100%;height:100%;object-fit:cover;" />
          </a>
        </div>
        <b style="display:block;margin-bottom:2px;">${cat}</b>
        <span class="product-name" style="font-size:14px;line-height:1.2;">
          ${item.title || ''}
        </span>
      </div>`;
  });
}

// ─── BEST LOOKS 로드 ───
function loadBestLooksData(tempNum) {
  fetch(`/api/community/best?temp=${tempNum}`)
    .then(res => res.json())
    .then(renderBestLooks)
    .catch(() => {
      const area = document.getElementById("best-looks-area");
      if (area) area.innerHTML = "<div class='text-danger'>※BEST LOOKS 정보를 가져올 수 없습니다!※</div>";
    });
}

// ─── 날씨 로드 시 의상+베스트룩 ───
window.addEventListener("weatherLoaded", e => {
  lastTempNum = e.detail.tempNum;
  updateCurrentTempTag(lastTempNum);
  loadClothingGuide(lastTempNum);
  loadBestLooksData(lastTempNum);
});

// ─── 온도범위 로드 ───
async function loadTemperatureRanges() {
  try {
    const res = await fetch("/api/temperature/ranges");
    if (!res.ok) throw new Error(res.status);
    tempRanges = await res.json();
  } catch (e) {
    console.error("온도범위 로드 실패", e);
  }
}

// ─── 온도 태그 업데이트 ───
function getTempRangeCode(temp) {
  const r = tempRanges.find(r => temp >= r.minTemp && temp <= r.maxTemp);
  return r ? r.code : null;
}
function updateCurrentTempTag(tempNum) {
  const code = getTempRangeCode(tempNum);
  const isCold = code !== null && code <= 4;
  const iconHtml = isCold
    ? '<i class="bi bi-thermometer-snow me-1 text-primary"></i>'
    : '<i class="bi bi-thermometer-sun me-1 text-danger"></i>';
  const colorClass = isCold ? "text-primary" : "text-danger";

  document.getElementById("current-temp-tag").innerHTML =
    `${iconHtml}<b class="${colorClass}">${tempNum}℃</b>`;
}

// ─── 초기 바인딩 ───
document.addEventListener("DOMContentLoaded", async () => {
  await loadTemperatureRanges();

  // 성별 토글
  const toggleBtn = document.getElementById("toggle-gender-btn");
  if (toggleBtn) {
    toggleBtn.innerHTML = currentGender === "male"
      ? '<i class="bi bi-gender-male text-primary"></i>'
      : '<i class="bi bi-gender-female text-danger"></i>';
    toggleBtn.addEventListener("click", () => {
      currentGender = currentGender === "male" ? "female" : "male";
      toggleBtn.innerHTML = currentGender === "male"
        ? '<i class="bi bi-gender-male text-primary"></i>'
        : '<i class="bi bi-gender-female text-danger"></i>';
      renderByGender(currentGender);
    });
  }

  // AI 버튼
  const aiBtn = document.getElementById("ai-btn");
  if (aiBtn) {
    aiBtn.innerHTML = '<i class="bi bi-openai text-dark"></i>';
    aiBtn.addEventListener("click", () => {
      useAiGuide = !useAiGuide;
      aiBtn.innerHTML = useAiGuide
        ? '<i class="bi bi-grid"></i>'
        : '<i class="bi bi-openai text-dark"></i>';
      if (lastTempNum != null) {
        if (useAiGuide) fetchAiRecommendations(lastTempNum, currentGender);
        else loadClothingGuide(lastTempNum);
      }
    });
  }

  // 리프레시 버튼
  const refreshBtn = document.getElementById("refresh-images-btn");
  if (refreshBtn) {
    refreshBtn.addEventListener("click", () => {
      if (lastTempNum == null) return;
      if (useAiGuide) {
        categories.forEach(cat => {
          const block = aiResults.find(b => b.product.category === cat);
          const len = (block?.items.length) || 1;
          indexByCat[cat] = clamp(indexByCat[cat] + 1, len);
        });
        renderAiRecommendations();
      } else {
        loadClothingGuide(lastTempNum);
      }
    });
  }

  // 날씨 위젯 변화 감지
  const weatherTempEl = document.getElementById("weather-temp");
  if (weatherTempEl) {
    new MutationObserver(() => {
      const m = weatherTempEl.textContent.match(/(-?\d+)\s*℃/);
      if (m) {
        const newTemp = parseInt(m[1], 10);
        if (newTemp !== lastTempNum) {
          lastTempNum = newTemp;
          updateCurrentTempTag(newTemp);
          loadClothingGuide(newTemp);
          loadBestLooksData(newTemp);
        }
      }
    }).observe(weatherTempEl, { childList: true, subtree: true, characterData: true });
  }
});

// ─── DB 가이드 렌더 ───
function renderDbByGender(gender) {
  document.getElementById("gender-label").textContent =
    gender === "male" ? "- 남성 -" : "- 여성 -";
  renderSlots(guideData[gender], gender);
}

// ─── 통합 렌더 ───
function renderByGender(gender) {
  if (useAiGuide) {
    document.getElementById("gender-label").textContent =
      gender === "male" ? "- 남성 (AI) -" : "- 여성 (AI) -";
    renderAiRecommendations();
  } else {
    renderDbByGender(gender);
  }
}

// ─── BEST LOOKS 렌더 ───
function renderBestLooks(data) {
  const area = document.getElementById("best-looks-area");
  if (!area) return;
  const styleList = [
    { key: "CASUAL", label: "캐주얼" },
    { key: "STREET", label: "스트리트" },
    { key: "FORMAL", label: "포멀" },
    { key: "OUTDOOR", label: "기타" },
  ];
  let html = "";
  styleList.forEach(({ key, label }) => {
    const post = data[key];
    html += `<div class="col-md-3 mb-4 d-flex">
      <div class="card flex-fill h-100">
        <div class="card-style-header">${label}</div>
        <div class="card-body d-flex flex-column align-items-center">`;
    if (post) {
      html += `
          <div class="post-title mb-2" style="font-size:1.25rem;font-weight:600;">${post.title}</div>
          <div class="badge-list mb-3">
            ${post.casual ? '<span class="badge bg-secondary me-1">캐주얼</span>' : ""}
            ${post.street ? '<span class="badge bg-secondary me-1">스트리트</span>' : ""}
            ${post.formal ? '<span class="badge bg-secondary me-1">포멀</span>' : ""}
            ${post.outdoor ? '<span class="badge bg-secondary me-1">기타</span>' : ""}
          </div>
          <a href="/community/detail/${post.id}">
            <img src="/uploads/${post.repImageUrl}" class="mb-3"
                 style="max-width:130px;max-height:130px;border-radius:10px;" alt="코디 이미지"/>
          </a>
          <div class="text-muted mb-1">${post.authorNickname}</div>
          <div class="text-muted mb-3">추천수: ${post.recommendCount}</div>
      `;
    } else {
      html += `<div class="mt-5 text-muted">게시글 없음</div>`;
    }
    html += `
        </div>
        <div class="card-footer bg-white border-top-0">
          <a class="btn btn-primary btn-sm w-100"
             href="/community/list?type=&keyword=&styleNames=${key}">
            ${label} 게시글보기
          </a>
        </div>
      </div>
    </div>`;
  });
  area.innerHTML = `<div class="row gx-4 gx-lg-5 d-flex align-items-stretch">${html}</div>`;
}

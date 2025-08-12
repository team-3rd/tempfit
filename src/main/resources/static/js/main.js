// src/main/resources/static/js/main.js

// ─── 전역 저장 변수 ───
let lastTempNum = null;
// 기존 guideData는 더 이상 직접 쓰지 않고, 성별별 캐시로 분리
let guideData = null; // 남겨두되 사용 안 함
let tempRanges = [];
let currentGender = window.initialGender || "male";

// 모드
let useAiGuide = false;

// ─── 캐시: 4개의 가상 페이지 ───
// DB 가이드 캐시: 성별별로 따로 보관
const dbCache = { male: null, female: null };
// AI 결과 캐시: 성별별로 따로 보관
const aiCache = { male: null, female: null };

// ─── 텍스트 유틸 ───
function stripTags(s) {
  return s ? s.replace(/<[^>]*>/g, "") : "";
}
function escapeHtml(s) {
  if (s == null) return "";
  return String(s)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}
function formatBrandAndNameBold(brand, name) {
  const b = escapeHtml((brand || "").trim());
  const n = escapeHtml((name || "").trim());
  return b ? `<b>${b}</b>${n ? " " + n : ""}` : n;
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
  const topName = data.top?.productName || data.top?.name;
  const isOnePiece = gender === "female" && topName && onePieceTops.includes(topName);
  if (isOnePiece) {
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
  const name = item?.productName || item?.name;
  if (name) {
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
      <span class="product-name" style="font-size:14px;line-height:1.2;">${escapeHtml(name)}</span>
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

// ─── DB 가이드 로드: 둘 다 가져와 캐시에 분리 저장 ───
async function fetchDbGuideBoth(tempNum, { silent = false } = {}) {
  if (!silent) showGuideLoading();
  try {
    // 랜덤화를 위해 r 파라미터 추가
    const res = await fetch(`/api/coordi/guide?temp=${tempNum}&r=${Date.now()}`);
    if (!res.ok) throw new Error(res.status);
    const data = await res.json(); // { male: {...}, female: {...} }
    dbCache.male = data.male || null;
    dbCache.female = data.female || null;
  } catch (e) {
    console.error("DB 가이드 로드 실패", e);
    if (!silent) {
      document.getElementById("clothing-guide-row1").textContent = "추천 코디 정보를 가져오지 못했습니다";
      document.getElementById("clothing-guide-row2").textContent = "";
    }
  } finally {
    if (!silent) hideGuideLoading();
  }
}

// ─── DB 가이드 부분 갱신: 현재 성별만 새로 받아 해당 캐시만 대체 ───
async function fetchDbGuideForGender(tempNum, gender, { silent = false } = {}) {
  if (!silent) showGuideLoading();
  try {
    const res = await fetch(`/api/coordi/guide?temp=${tempNum}&r=${Date.now()}`);
    if (!res.ok) throw new Error(res.status);
    const data = await res.json();
    if (gender === "male") dbCache.male = data.male || null;
    else dbCache.female = data.female || null;
  } catch (e) {
    console.error("DB 가이드(성별별) 로드 실패", e);
    if (!silent) {
      document.getElementById("clothing-guide-row1").textContent = "추천 코디 정보를 가져오지 못했습니다";
      document.getElementById("clothing-guide-row2").textContent = "";
    }
  } finally {
    if (!silent) hideGuideLoading();
  }
}

// ─── AI 가이드: temp만으로 male/female 동시 로드(초기 프리페치/온도변경 시) ───
async function fetchAiBoth(tempNum, { silent = false } = {}) {
  if (!silent) showGuideLoading();
  try {
    const res = await fetch(`/api/aiguide?temp=${tempNum}`);
    if (!res.ok) throw new Error(res.status);
    const data = await res.json(); // { male: [...], female: [...] }
    aiCache.male = data.male || [];
    aiCache.female = data.female || [];
  } catch (e) {
    console.error("AI 추천 로드 실패", e);
    aiCache.male = aiCache.male || [];
    aiCache.female = aiCache.female || [];
    if (!silent) {
      document.getElementById("clothing-guide-row1").textContent = "AI 추천 정보를 가져오지 못했습니다.";
      document.getElementById("clothing-guide-row2").textContent = "";
    }
  } finally {
    if (!silent) hideGuideLoading();
  }
}

// ─── AI 가이드 부분 갱신: 현재 성별만 재호출하여 해당 캐시만 대체 ───
async function fetchAiForGender(gender, tempNum, { silent = false } = {}) {
  if (!silent) showGuideLoading();
  try {
    const res = await fetch(`/api/aiguide?temp=${tempNum}&r=${Date.now()}`);
    if (!res.ok) throw new Error(res.status);
    const data = await res.json();
    if (gender === "male") aiCache.male = data.male || [];
    else aiCache.female = data.female || [];
  } catch (e) {
    console.error("AI 추천(성별별) 로드 실패", e);
    if (!silent) {
      document.getElementById("clothing-guide-row1").textContent = "AI 추천 정보를 가져오지 못했습니다.";
      document.getElementById("clothing-guide-row2").textContent = "";
    }
  } finally {
    if (!silent) hideGuideLoading();
  }
}

// ─── DB 렌더 ───
function renderDbByGender(gender) {
  document.getElementById("gender-label").textContent =
    gender === "male" ? "- 남성 -" : "- 여성 -";

  const data = dbCache[gender];
  if (!data) {
    document.getElementById("clothing-guide-row1").textContent = "추천 코디 정보를 가져오지 못했습니다";
    document.getElementById("clothing-guide-row2").textContent = "";
    return;
  }
  renderSlots(data, gender);
}

// ─── AI 렌더 ───
async function renderAiByGender(gender) {
  document.getElementById("gender-label").textContent =
    gender === "male" ? "- 남성 (AI) -" : "- 여성 (AI) -";

  const results = aiCache[gender] || [];
  const row1 = document.getElementById("clothing-guide-row1");
  const row2 = document.getElementById("clothing-guide-row2");
  row1.innerHTML = "";
  row2.innerHTML = "";

  if (!results.length) {
    row1.innerHTML = "<div class='text-muted'>AI 추천 결과가 없습니다.</div>";
    row2.innerHTML = "";
    return;
  }

  results.forEach(block => {
    const cat = block.product.category; // "상의/아우터/하의/신발"
    const items = block.items || [];
    const item = items.length ? items[0] : null;
    const container = (cat === "상의" || cat === "아우터") ? row1 : row2;

    const brand = block.product.brandName || "";
    // 표시용 이름은 "브랜드 + 상품명"을 우리 쪽에서 조합 (네이버의 <b> 태그 무시)
    const productNameFallback = stripTags(item?.title || "");
    const pname = block.product.productName || productNameFallback;
    const displayTitle = formatBrandAndNameBold(brand, pname);

    if (!item) {
      container.innerHTML += emptySlotMarkup(cat);
      return;
    }

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
            <img src="${item.image || ''}" alt="${escapeHtml(productNameFallback)}"
                 style="width:100%;height:100%;object-fit:cover;" />
          </a>
        </div>
        <b style="display:block;margin-bottom:2px;">${cat}</b>
        <span class="product-name" style="font-size:14px;line-height:1.2;">${displayTitle}</span>
      </div>`;
  });
}

// ─── BEST LOOKS 로드 ───
function loadBestLooksData(tempNum) {
  showBestLoading();
  fetch(`/api/community/best?temp=${tempNum}`)
    .then(res => res.json())
    .then(renderBestLooks)
    .catch(() => {
      const area = document.getElementById("best-looks-area");
      if (area) area.innerHTML = "<div class='text-danger'>※BEST LOOKS 정보를 가져올 수 없습니다!※</div>";
    })
    .finally(hideBestLoading);
}

// ─── 날씨 로드 시 의상+베스트룩 ───
window.addEventListener("weatherLoaded", async (e) => {
  lastTempNum = e.detail.tempNum;
  updateCurrentTempTag(lastTempNum);

  // DB 가이드: 둘 다 프리페치
  await fetchDbGuideBoth(lastTempNum, { silent: true });
  renderDbByGender(currentGender);

  // BEST LOOKS
  loadBestLooksData(lastTempNum);

  // AI: 둘 다 프리페치
  await fetchAiBoth(lastTempNum, { silent: true });
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
    const renderIcon = () =>
      currentGender === "male"
        ? '<i class="bi bi-gender-male text-primary"></i>'
        : '<i class="bi bi-gender-female text-danger"></i>';
    toggleBtn.innerHTML = renderIcon();
    toggleBtn.addEventListener("click", async () => {
      currentGender = currentGender === "male" ? "female" : "male";
      toggleBtn.innerHTML = renderIcon();

      // 현재 모드에 맞게, 캐시 렌더만 수행(없으면 해당 성별만 새로 로드)
      if (useAiGuide) {
        if (!aiCache[currentGender]) {
          await fetchAiForGender(currentGender, lastTempNum, { silent: false });
        }
        await renderAiByGender(currentGender);
      } else {
        if (!dbCache[currentGender]) {
          await fetchDbGuideForGender(lastTempNum, currentGender, { silent: false });
        }
        renderDbByGender(currentGender);
      }
    });
  }

  // AI 버튼
  const aiBtn = document.getElementById("ai-btn");
  if (aiBtn) {
    aiBtn.innerHTML = '<i class="bi bi-openai text-dark"></i>';
    aiBtn.addEventListener("click", async () => {
      useAiGuide = !useAiGuide;
      aiBtn.innerHTML = useAiGuide ? '<i class="bi bi-grid"></i>' : '<i class="bi bi-openai text-dark"></i>';

      if (lastTempNum == null) return;

      // 모드 전환 시에는 현재 보이는 카드만 렌더(필요 시 해당 성별만 로드)
      if (useAiGuide) {
        if (!aiCache[currentGender]) {
          await fetchAiForGender(currentGender, lastTempNum, { silent: false });
        }
        await renderAiByGender(currentGender);
      } else {
        if (!dbCache[currentGender]) {
          await fetchDbGuideForGender(lastTempNum, currentGender, { silent: false });
        }
        renderDbByGender(currentGender);
      }
    });
  }

  // 리프레시 버튼: 현재 보이는 "가상페이지"만 갱신
  const refreshBtn = document.getElementById("refresh-images-btn");
  if (refreshBtn) {
    refreshBtn.addEventListener("click", async () => {
      if (lastTempNum == null) return;

      if (useAiGuide) {
        // 남성 AI / 여성 AI 각각 독립적으로 새로고침
        await fetchAiForGender(currentGender, lastTempNum, { silent: false });
        await renderAiByGender(currentGender);
      } else {
        // 남성 DB / 여성 DB 각각 독립적으로 새로고침
        await fetchDbGuideForGender(lastTempNum, currentGender, { silent: false });
        renderDbByGender(currentGender);
      }
    });
  }

  // 날씨 위젯 변화 감지(온도 바뀌면 캐시 전체를 새 온도로 갱신)
  const weatherTempEl = document.getElementById("weather-temp");
  if (weatherTempEl) {
    new MutationObserver(async () => {
      const m = weatherTempEl.textContent.match(/(-?\d+)\s*℃/);
      if (m) {
        const newTemp = parseInt(m[1], 10);
        if (newTemp !== lastTempNum) {
          lastTempNum = newTemp;
          updateCurrentTempTag(newTemp);

          // 새 온도 기준으로 DB/AI 캐시 모두 프리페치(둘 다 갱신)
          await fetchDbGuideBoth(newTemp, { silent: true });
          await fetchAiBoth(newTemp, { silent: true });

          // BEST LOOKS 갱신
          loadBestLooksData(newTemp);

          // 현재 모드/성별 다시 렌더
          if (useAiGuide) await renderAiByGender(currentGender);
          else renderDbByGender(currentGender);
        }
      }
    }).observe(weatherTempEl, { childList: true, subtree: true, characterData: true });
  }
});

// ─── 통합 렌더 ───
function renderByGender(gender) {
  if (useAiGuide) {
    renderAiByGender(gender);
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

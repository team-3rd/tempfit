// ─── 전역 저장 변수 ───
let lastTempNum = null;
let guideData = null;
let aiGuideData = null;
let useAiGuide = false;
let currentGender = "male"; // 'male' 또는 'female'
let tempRanges = [];

// ─── 로딩 스피너 제어 ───
function showLoading() {
  document.getElementById("loading-overlay").style.display = "block";
  document.getElementById("loading-spinner").style.display = "block";
  document.getElementById("loading-text").style.display = "block";
}
function hideLoading() {
  document.getElementById("loading-overlay").style.display = "none";
  document.getElementById("loading-spinner").style.display = "none";
  document.getElementById("loading-text").style.display = "none";
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

// ─── 슬롯 있으면 렌더, 없으면 빈 슬롯 ───
function renderSlotOrEmpty(part, item, container) {
  const labelMap = { top: "상의", outer: "아우터", bottom: "하의", shoes: "신발" };
  if (item && (item.productName || item.name)) {
    renderSlot(part, item, container);
  } else {
    container.innerHTML += emptySlotMarkup(labelMap[part]);
  }
}

// ─── 슬롯 카드 헬퍼 ───
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

// ─── 빈 슬롯 헬퍼 ───
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

// ─── ① 의상 가이드만 로드하는 함수 ───
function loadClothingGuide(tempNum) {
  useAiGuide = false;
  showLoading();
  fetch(`/api/coordi/guide?temp=${tempNum}`)
    .then((res) => res.json())
    .then((data) => {
      guideData = data;
      renderByGender(currentGender);
      hideLoading();
    })
    .catch(() => {
      document.getElementById("clothing-guide-row1").textContent = "추천 코디 정보를 가져오지 못했습니다";
      document.getElementById("clothing-guide-row2").textContent = "";
      hideLoading();
    });
}

// ─── AI 가이드만 로드하는 함수 ───
function loadAiGuide(tempNum) {
  useAiGuide = true;
  showLoading();
  fetch(`/api/aistylist/aiguide?temp=${tempNum}`)
    .then((res) => res.json())
    .then((data) => {
      aiGuideData = data;
      renderByGender(currentGender);
      hideLoading();
    })
    .catch(() => {
      document.getElementById("clothing-guide-row1").textContent = "AI 추천 정보를 가져오지 못했습니다.";
      document.getElementById("clothing-guide-row2").textContent = "";
      hideLoading();
    });
}

// ─── ② BEST LOOKS만 로드하는 함수 ───
function loadBestLooksData(tempNum) {
  fetch(`/api/community/best?temp=${tempNum}`)
    .then((res) => res.json())
    .then(renderBestLooks)
    .catch(() => {
      const area = document.getElementById("best-looks-area");
      if (area) area.innerHTML = "<div class='text-danger'>※BEST LOOKS 정보를 가져올 수 없습니다!※</div>";
    });
}

// ─── ③ 날씨 로드 시 — 두 함수 실행 ───
window.addEventListener("weatherLoaded", (e) => {
  lastTempNum = e.detail.tempNum;
  loadClothingGuide(lastTempNum);
  loadBestLooksData(lastTempNum);
});

// 서버에서 enum TemperatureRange 데이터를 가져와 tempRanges 에 저장
async function loadTemperatureRanges() {
  try {
    const res = await fetch("/api/temperature/ranges");
    if (!res.ok) throw new Error(res.status);
    tempRanges = await res.json(); // [{code, minTemp, maxTemp}, …]
  } catch (e) {
    console.error("온도범위 로드 실패", e);
  }
}

// temp 가 속한 code(id)를 찾아 리턴
function getTempRangeCode(temp) {
  const r = tempRanges.find((r) => temp >= r.minTemp && temp <= r.maxTemp);
  return r ? r.code : null;
}

// current-temp-tag 를 code≤4 → 눈송이+파랑색, 그 외 → 해+빨강색 으로
function updateCurrentTempTag(tempNum) {
  const code = getTempRangeCode(tempNum);
  const isCold = code !== null && code <= 4;
  const iconHtml = isCold
    ? '<i class="bi bi-thermometer-snow me-1"></i>'
    : '<i class="bi bi-thermometer-sun me-1"></i>';
  const colorClass = isCold ? 'text-primary' : 'text-danger';

  document.getElementById('current-temp-tag').innerHTML =
    `${iconHtml}<b class="${colorClass}">${tempNum}℃</b>`;

   loadClothingGuide(tempNum);
   loadBestLooksData(tempNum);
}

// ─── ④ DOMContentLoaded 시 바인딩 ───
document.addEventListener("DOMContentLoaded", async () => {
  await loadTemperatureRanges();
  // 성별 토글 버튼
  const toggleBtn = document.getElementById("toggle-gender-btn");
  // if (toggleBtn) {
  //   toggleBtn.addEventListener("click", () => {
  //     currentGender = currentGender === "male" ? "female" : "male";
  //     toggleBtn.textContent = currentGender === "male" ? "🧑🏻" : "👧🏻";
  //     renderByGender(currentGender);
  //   });
  // }
  if (toggleBtn) {
    // 초기 아이콘 세팅
    toggleBtn.innerHTML =
      currentGender === "male" ? '<i class="bi bi-gender-male"></i>' : '<i class="bi bi-gender-female"></i>';

    toggleBtn.addEventListener("click", () => {
      currentGender = currentGender === "male" ? "female" : "male";

      // 클릭할 때도 innerHTML로 변경
      toggleBtn.innerHTML =
        currentGender === "male" ? '<i class="bi bi-gender-male"></i>' : '<i class="bi bi-gender-female"></i>';

      renderByGender(currentGender);
    });
  }

  // COLOR 버튼
  // const colorBtn = document.getElementById("color-btn");
  // if (colorBtn) {
  //   // 미리 쓸 색상 배열 (원하는 색으로 바꿔주세요)
  //   const palette = ["#FFD3B6", "#FF8C94", "#6C5B7B", "#355C7D"];
  //   colorBtn.addEventListener("click", () => {
  //     // 모든 카드 순회
  //     document.querySelectorAll(".recommend-card .card-body").forEach((body, i) => {
  //       const color = palette[i % palette.length];
  //       // 이미지·텍스트 지우고 배경색만 남김
  //       body.innerHTML = "";
  //       body.style.backgroundColor = color;
  //     });
  //   });
  // }
  // let useColorGuide = false; // false: 일반 카드, true: 색상 모드
  // const colorBtn = document.getElementById("color-btn");
  // if (colorBtn) {
  //   // 초기 아이콘 세팅
  //   colorBtn.innerHTML = '<i class="bi bi-palette"></i>';

  //   colorBtn.addEventListener("click", () => {
  //     // 모드 토글
  //     useColorGuide = !useColorGuide;

  //     // 아이콘 변경
  //     colorBtn.innerHTML = useColorGuide ? '<i class="bi bi-grid"></i>' : '<i class="bi bi-palette"></i>';

  //     // 모드에 따라 카드 렌더링
  //     if (useColorGuide) {
  //       fillColorCards();
  //     } else {
  //       // 원래 카드로 복원: 이전에 렌더링하던 함수 호출
  //       // lastTempNum 이 null이 아닐 때만 재로드
  //       if (lastTempNum != null) {
  //         loadClothingGuide(lastTempNum);
  //       }
  //     }
  //   });
  // }

  // AI 버튼
  // const aiBtn = document.getElementById("ai-btn");
  // if (aiBtn) aiBtn.addEventListener("click", () => {
  //   if (lastTempNum != null) {
  //     // 이미 AI 모드면 DB 모드로, 아니면 AI 모드로
  //     if (useAiGuide) loadClothingGuide(lastTempNum);
  //     else            loadAiGuide(lastTempNum);
  //   }
  // });
  // AI 버튼 토글 기능
  let useAiGuide = false; // false: DB 가이드 모드, true: AI 가이드 모드
  const aiBtn = document.getElementById("ai-btn");
  if (aiBtn) {
    // 초기 아이콘 세팅
    aiBtn.innerHTML = useAiGuide ? '<i class="bi bi-images"></i>' : '<i class="bi bi-openai"></i>';

    aiBtn.addEventListener("click", () => {
      // 모드 토글
      useAiGuide = !useAiGuide;

      // 아이콘 변경
      aiBtn.innerHTML = useAiGuide ? '<i class="bi bi-images"></i>' : '<i class="bi bi-openai"></i>';

      // 가이드 로드
      if (lastTempNum != null) {
        if (useAiGuide) {
          loadAiGuide(lastTempNum);
        } else {
          loadClothingGuide(lastTempNum);
        }
      }
    });
  }

  // 리프레시 버튼
  const refreshBtn = document.getElementById("refresh-images-btn");
  if (refreshBtn)
    refreshBtn.addEventListener("click", () => {
      if (lastTempNum != null) {
        if (useAiGuide) loadAiGuide(lastTempNum);
        else loadClothingGuide(lastTempNum);
      }
    });

  // ⑤ MutationObserver로 날씨 위젯 감지
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

// ─── ⑥a DB 가이드 렌더 함수 ───
function renderDbByGender(gender) {
  document.getElementById("gender-label").textContent = gender === "male" ? "- 남성 -" : "- 여성 -";
  renderSlots(guideData[gender], gender);
}

// ─── ⑥b AI 가이드 렌더 함수 ───
function renderAiByGender(gender) {
  document.getElementById("gender-label").textContent = gender === "male" ? "- 남성 (AI) -" : "- 여성 (AI) -";
  renderSlots(aiGuideData[gender], gender);
}

// ─── ⑥ 통합 renderByGender ───
function renderByGender(gender) {
  if (useAiGuide) renderAiByGender(gender);
  else renderDbByGender(gender);
}

// ─── ⑦ AI 추천 호출 ───
function requestAiRecommendation(tempNum, gender) {
  loadAiGuide(tempNum);
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

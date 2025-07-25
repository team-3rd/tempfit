// ─── 전역 저장 변수 ───
let lastTempNum = null;
let guideData = null;
let currentGender = "male"; // 'male' 또는 'female'

// ─── ① 의상 가이드만 로드하는 함수 ───
function loadClothingGuide(tempNum) {
  fetch(`/api/coordi/guide?temp=${tempNum}`)
    .then((res) => res.json())
    .then((data) => {
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
  fetch(`/api/community/best?temp=${tempNum}`)
    .then((res) => res.json())
    .then(renderBestLooks)
    .catch(() => {
      const area = document.getElementById("best-looks-area");
      if (area) {
        area.innerHTML = "<div class='text-danger'>※BEST LOOKS 정보를 가져올 수 없습니다!※</div>";
      }
    });
}

// ─── ③ 날씨 로드 시 — 두 함수 모두 실행 ───
window.addEventListener("weatherLoaded", (e) => {
  lastTempNum = e.detail.tempNum;
  loadClothingGuide(lastTempNum);
  loadBestLooksData(lastTempNum);
});

// ─── ④ DOMContentLoaded 시 — 버튼 클릭 바인딩 ───
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

  // 🔄 리프레시 버튼 — 단 하나의 핸들러만 등록
  const refreshBtn = document.getElementById("refresh-images-btn");
  if (refreshBtn) {
    refreshBtn.addEventListener("click", () => {
      if (lastTempNum == null) return;

      // 현재 성별만 다시 가져오기
      fetch(`/api/coordi/guide?temp=${lastTempNum}`)
        .then((res) => res.json())
        .then((data) => {
          guideData[currentGender] = data[currentGender];
          renderByGender(currentGender);
        })
        .catch(() => {
          console.warn("추천 코디 정보를 새로고침하지 못했습니다.");
        });
    });
  }
});

// ─── ⑤ 성별별 렌더 함수 ───
function renderByGender(gender) {
  // 성별 라벨 갱신
  const genderLabel = document.getElementById("gender-label");
  const labelText = gender === "male" ? "남성" : "여성";
  genderLabel.innerHTML = `-${labelText}-`;

  const data = guideData[gender];
  const row1 = document.getElementById("clothing-guide-row1");
  const row2 = document.getElementById("clothing-guide-row2");
  row1.innerHTML = "";
  row2.innerHTML = "";

  // “원피스류” 상의 리스트
  const onePieceTops = ["피케/카라 원피스", "원피스", "맥시드레스"];
  const isOnePiece = onePieceTops.includes(data.top.name);

  // ─── 위쪽: 상의 ───
  renderSlot("top", data.top, row1);
  // ─── 위쪽: 아우터 ───
  if (data.outer && data.outer.name) {
    renderSlot("outer", data.outer, row1);
  } else {
    // 아우터가 빈 문자열이거나 undefined면 “아우터 없음” 표시
    row1.innerHTML += emptySlotMarkup("아우터");
  }

  // 아래쪽: 하의 · 신발
  if (gender === "female" && isOnePiece) {
    // 여성 & 원피스류면 하의 없음
    row2.innerHTML += emptySlotMarkup("하의");
  } else if (data.bottom.name) {
    renderSlot("bottom", data.bottom, row2);
  } else {
    row2.innerHTML += emptySlotMarkup("하의");
  }
  renderSlot("shoes", data.shoes, row2);

  row1.style.gap = "50px";
  row2.style.gap = "50px";
}
// ─── ⑥ 슬롯 카드 렌더 헬퍼 ───
function renderSlot(part, item, container) {
  if (!item.name) return;
  const labelMap = { outer: "아우터", top: "상의", bottom: "하의", shoes: "신발" };
  container.innerHTML += `
    <a href="/coordi/item/${item.productKey}"
       style="
         display:inline-block;
         width:150px;
         text-align:center;
         text-decoration:none;
         color:inherit;
         margin:0 6px;
       ">
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
        <img src="${item.imageUrl}"
             alt="${item.name}"
             style="
               width:100%;
               height:100%;
               object-fit:cover;
             "/>
      </div>
      <b style="display:block; margin-bottom:2px;">
        ${labelMap[part]}
      </b>
      <span style="font-size:14px; line-height:1.2;">
        ${item.name}
      </span>
    </a>`;
}

// ─── ⑦ 빈 슬롯(없음) 마크업 헬퍼 ───
function emptySlotMarkup(label) {
  return `
    <div style="
      text-align:center;
      width:150px;
      margin:0 6px;
    ">
      <div style="
        width:150px; height:150px;
        display:flex; align-items:center; justify-content:center;
        border-radius:10px; border:1px solid #ddd;
        background:#fafafa; color:#888;
        font-size:16px; font-weight:600;
        box-sizing:border-box;
        margin-bottom:6px;
      ">
        ${label} 없음
      </div>
      <b style="display:block; margin-bottom:2px;">
        ${label}
      </b>
    </div>`;
}

// ─── ⑧ BEST LOOKS 렌더 함수 ───
function renderBestLooks(data) {
  const area = document.getElementById("best-looks-area");
  if (!area) return;

  const styleList = [
    { key: "CASUAL",  label: "캐주얼"  },
    { key: "STREET",  label: "스트리트" },
    { key: "FORMAL",  label: "포멀"    },
    { key: "OUTDOOR", label: "아웃도어" }
  ];

  let html = "";
  styleList.forEach(({ key, label }) => {
    const post = data[key];
    html += `
      <div class="col-md-3 mb-4 d-flex">
        <div class="card flex-fill h-100">
          <div class="card-style-header">${label}</div>
          <div class="card-body d-flex flex-column align-items-center">`;

    if (post) {
      html += `
            <div class="post-title mb-2"
                 style="font-size:1.25rem; font-weight:600;">
              ${post.title}
            </div>
            <div class="badge-list mb-3">
              ${post.casual  ? '<span class="badge bg-secondary me-1">캐주얼</span>'  : ""}
              ${post.street  ? '<span class="badge bg-secondary me-1">스트리트</span>' : ""}
              ${post.formal  ? '<span class="badge bg-secondary me-1">포멀</span>'    : ""}
              ${post.outdoor ? '<span class="badge bg-secondary me-1">아웃도어</span>' : ""}
            </div>
            <a href="/community/detail/${post.id}">
              <img src="/uploads/${post.repImageUrl}"
                   class="mb-3"
                   style="max-width:130px; max-height:130px; border-radius:10px;"
                   alt="코디 이미지"/>
            </a>
            <div class="text-muted mb-1">${post.authorName}</div>
            <div class="text-muted mb-3">
              추천수: ${post.recommendCount}
            </div>`;
    } else {
      html += `
            <div class="mt-5 text-muted">게시글 없음</div>`;
    }

    html += `
          </div>
          <div class="card-footer bg-white border-top-0">
            <a class="btn btn-primary btn-sm w-100"
               href="/community/list?type=&keyword=&styleNames=${key}">
              ${label} 게시글 보기
            </a>
          </div>
        </div>
      </div>`;
  });

  area.innerHTML = `
    <div class="row gx-4 gx-lg-5 d-flex align-items-stretch">
      ${html}
    </div>`;
}
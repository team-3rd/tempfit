// src/main/resources/static/js/main.js
window.addEventListener("weatherLoaded", (e) => {
  const tempNum = e.detail.tempNum;

  // 1) 코디 추천 호출
  fetch(`/api/coordi/guide?temp=${tempNum}`)
    .then((res) => res.json())
    .then((data) => {
      renderGuide("clothing-guide-male", data.male);
      renderGuide("clothing-guide-female", data.female);
    })
    .catch(() => {
      document.getElementById("clothing-guide-male").textContent = "추천 코디 정보를 가져오지 못했습니다";
      document.getElementById("clothing-guide-female").textContent = "추천 코디 정보를 가져오지 못했습니다";
    });

  // 2) BEST LOOKS 호출
  fetch(`/api/community/best?temp=${tempNum}`)
    .then((res) => res.json())
    .then(renderBestLooks)
    .catch(() => {
      const area = document.getElementById("best-looks-area");
      if (area) {
        area.innerHTML = "<div class='text-danger'>※BEST LOOKS 정보를 가져올 수 없습니다!※</div>";
      }
    });
});

// 공통 렌더 함수 (코디)
function renderGuide(divId, data) {
  const guideDiv = document.getElementById(divId);
  guideDiv.innerHTML = "";

  // 원피스류 리스트
  const onePieceTops = ["피케/카라 원피스", "원피스", "동탄미시"];
  const isFemale = divId === "clothing-guide-female";
  const isOnePiece = onePieceTops.includes(data.top?.name);

  ["top", "bottom", "shoes"].forEach((part) => {
    if (data[part]) {
      // 여성 & 하의 & 원피스류면 안내문구
      if (isFemale && part === "bottom" && isOnePiece) {
        guideDiv.innerHTML += `
        <div style="text-align:center;">
          <div style="
            width:130px; height:130px; /* 이미지와 동일하게 */
            display:flex;align-items:center;justify-content:center;
            border-radius:10px; border:1px solid #ddd;
            background:#fafafa; color:#888;
            font-size:18px; font-weight:600; margin:0 auto 8px auto;
            box-sizing:border-box;">
            하의 없음
            </div>
          <b>하의</b>
        </div>
      `;
      } else {
        guideDiv.innerHTML += `
          <div style="text-align:center;">
            <img src="${data[part].imageUrl}" alt="${data[part].name}"
              style="max-width:130px; width:100%; height:auto;
                    border-radius:10px; border:1px solid #ddd;
                    background:#fafafa;"/><br/>
            <b>${part === "top" ? "상의" : part === "bottom" ? "하의" : "신발"}</b><br/>
            <span style="font-size:18px;">${data[part].name}</span>
          </div>`;
      }
    }
  });
}

// [BEST LOOKS] 영역에 게시글 뿌리기
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
        <!-- ★ 스타일 헤더 추가 ★ -->
        <div class="card-style-header">${label}</div>
        <div class="card-body d-flex flex-column align-items-center">`;

    if (post) {
      html += `
          <div class="post-title mb-2" style="font-size:1.25rem; font-weight:600;">
            ${post.title}
          </div>
          <div class="badge-list mb-3">
            ${post.casual ? '<span class="badge bg-secondary me-1">캐주얼</span>' : ""}
            ${post.street ? '<span class="badge bg-secondary me-1">스트리트</span>' : ""}
            ${post.formal ? '<span class="badge bg-secondary me-1">포멀</span>' : ""}
            ${post.outdoor? '<span class="badge bg-secondary me-1">아웃도어</span>' : ""}
          </div>
          <a href="/community/detail/${post.id}">
            <img src="/uploads/${post.repImageUrl}"
                 class="mb-3"
                 style="max-width:130px; max-height:130px; border-radius:10px;"
                 alt="코디 이미지"/>
          </a>
          <div class="text-muted mb-1">${post.authorName}</div>
          <div class="text-muted mb-3">추천수: ${post.recommendCount}</div>`;
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

  area.innerHTML = `<div class="row gx-4 gx-lg-5 d-flex align-items-stretch">${html}</div>`;
}
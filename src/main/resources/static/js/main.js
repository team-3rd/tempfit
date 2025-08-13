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

// function showBestLoading() {
//   document.getElementById("best-loading-overlay").style.display = "block";
//   document.getElementById("best-loading-spinner").style.display = "block";
//   document.getElementById("best-loading-text").style.display = "block";
// }
// function hideBestLoading() {
//   document.getElementById("best-loading-overlay").style.display = "none";
//   document.getElementById("best-loading-spinner").style.display = "none";
//   document.getElementById("best-loading-text").style.display = "none";
// }

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
  // showBestLoading(); // ← 스피너/오버레이 표시 (비활성화)

  fetch(`/api/community/best?temp=${tempNum}`, { credentials: "same-origin" })
    .then(res => res.json())
    .then(renderBestLooks)
    .catch(() => {
      const area = document.getElementById("best-looks-area");
      if (area) area.innerHTML = "<div class='text-danger'>※BEST LOOKS 정보를 가져올 수 없습니다!※</div>";
    })
    .finally(() => {
      // hideBestLoading(); // ← 스피너/오버레이 숨김 (비활성화)
    });
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
        await fetchAiForGender(currentGender, lastTempNum, { silent: false });
        await renderAiByGender(currentGender);
      } else {
        await fetchDbGuideForGender(lastTempNum, currentGender, { silent: false });
        renderDbByGender(currentGender);
      }
    });
  }

  // 날씨 위젯 변화 감지
  const weatherTempEl = document.getElementById("weather-temp");
  if (weatherTempEl) {
    new MutationObserver(async () => {
      const m = weatherTempEl.textContent.match(/(-?\d+)\s*℃/);
      if (m) {
        const newTemp = parseInt(m[1], 10);
        if (newTemp !== lastTempNum) {
          lastTempNum = newTemp;
          updateCurrentTempTag(newTemp);
          await fetchDbGuideBoth(newTemp, { silent: true });
          await fetchAiBoth(newTemp, { silent: true });
          loadBestLooksData(newTemp);
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

// ─── 모달 유틸 (detail.html fragment 로드) ───
function ensureDetailModal() {
  let modal = document.getElementById("detailModal");
  if (modal) return modal;
  const wrap = document.createElement("div");
  wrap.innerHTML = `
  <div class="modal fade" id="detailModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-xl">
      <div class="modal-content"></div>
    </div>
  </div>`;
  document.body.appendChild(wrap.firstElementChild);
  return document.getElementById("detailModal");
}
async function openDetailModal(postId) {
  const modalEl = ensureDetailModal();
  const content = modalEl.querySelector(".modal-content");
  content.innerHTML = ""; // 초기화
  try {
    // ✅ 경로 수정: /fragment 제거 + 쿠키 포함
    const res = await fetch(`/community/detail/${postId}`, { credentials: "same-origin" });
    content.innerHTML = await res.text();
    // detail.js 초기화
    if (window.initDetailModal) window.initDetailModal(content);
  } catch (e) {
    content.innerHTML = `<div class="p-4">상세 정보를 불러오지 못했습니다.</div>`;
  }
  const bsModal = new bootstrap.Modal(modalEl);
  bsModal.show();
}

// ─── BEST LOOKS 렌더(Top 4, 스타일 구분 없음) ───
function renderBestLooks(data) {
  const area = document.getElementById("best-looks-area");
  if (!area) return;

  // 응답이 배열(신규) or 맵(구버전 호환) 모두 처리
  const list = Array.isArray(data)
    ? data
    : Object.values(data || {}).filter(Boolean);

  // Top 4만 사용
  const posts = list.slice(0, 4);

  // 헬퍼들
  const timeAgo = (iso) => {
    if (!iso) return "";
    const t = new Date(iso).getTime();
    const s = Math.max(0, (Date.now() - t) / 1000);
    if (s < 60) return `${Math.floor(s)}초 전`;
    if (s < 3600) return `${Math.floor(s / 60)}분 전`;
    if (s < 86400) return `${Math.floor(s / 3600)}시간 전`;
    return `${Math.floor(s / 86400)}일 전`;
  };
  const skyIcon = (sky) => {
    switch (sky) {
      case "맑음":
        return "bi-sun";
      case "흐림":
        return "bi-cloud-sun";
      case "구름 많음":
        return "bi-clouds";
      case "비":
        return "bi-cloud-rain";
      case "눈":
        return "bi-cloud-snow";
      default:
        return "";
    }
  };
  const k = (n) => {
    const num = Number(n || 0);
    if (Math.abs(num) >= 1000) {
      const v = (num / 1000).toFixed(1).replace(/\.0$/, "");
      return `${v}k`;
    }
    return String(num);
  };

  // 렌더 시작
  area.innerHTML = "";

  // 카드 생성 함수 (리스트와 동일 스타일)
  const makeCardHtml = (post) => {
    const id = post.id;
    const nickname = post.authorNickname || post.author?.nickname || "익명";
    const profile =
      post.profileImageUrl || post.author?.profileImageUrl || "/assets/default-profile.png";
    const created = timeAgo(post.createdDate);
    const icon = skyIcon(post.sky);
    const maxT = Number.isFinite(post.maxTemp) ? `${post.maxTemp}°` : "";
    const minT = Number.isFinite(post.minTemp) ? `${post.minTemp}°` : "";
    const img = post.repImageUrl ? `/uploads/${post.repImageUrl}` : "/assets/no-image.png";
    const hasExtra = Array.isArray(post.extraImageUrls) && post.extraImageUrls.length > 0;

    const likeCount = post.recommendCount ?? 0;
    const commentCount = post.commentCount ?? 0;
    const content = escapeHtml(post.content || post.title || "");

    const liked = !!post.likedByMe;
    const bookmarked = !!post.bookmarkedByMe;

    return `
      <a href="/community/detail/${id}" class="post-card text-decoration-none text-reset h-100" data-id="${id}">
        <div class="card-top">
          <div class="left">
            <img class="profileImg" src="${profile}" alt="프로필"/>
            <div class="name-time">
              <p class="nickname">${escapeHtml(nickname)}</p>
              <p class="time">${created}</p>
            </div>
          </div>
          <div class="weather">
            ${icon ? `<i class="bi ${icon}"></i>` : ""}
            ${maxT ? `<span class="temp">${maxT}</span>` : ""}
            ${minT ? `<span class="temp">${minT}</span>` : ""}
          </div>
        </div>

        <div class="image-wrap">
          <img src="${img}" alt="대표사진" class="card-img-top"/>
          ${hasExtra ? `<i class="bi bi-stickies-fill multi-indicator"></i>` : ""}
        </div>

        <div class="card-body-ig">
          <div class="actions">
            <div class="left-actions">
              <div class="action-group">
                <button type="button" class="btn-action btn-like" data-id="${id}" title="좋아요">
                  <i class="bi ${liked ? "bi-heart-fill" : "bi-heart"}"></i>
                </button>
                <span class="count like-count" data-count="${likeCount}">${k(likeCount)}</span>
              </div>
              <div class="action-group">
                <button type="button" class="btn-action btn-comment" data-id="${id}" title="댓글">
                  <i class="bi bi-chat"></i>
                </button>
                <span class="count" data-count="${commentCount}">${k(commentCount)}</span>
              </div>
            </div>
            <div class="right-actions">
              <button type="button" class="btn-action btn-bookmark" data-id="${id}" title="북마크">
                <i class="bi ${bookmarked ? "bi-bookmark-fill" : "bi-bookmark"}"></i>
              </button>
            </div>
          </div>

          <div class="caption">
            <span class="name">${escapeHtml(nickname)}</span>
            <span class="content">${content}</span>
          </div>
        </div>
      </a>
    `;
  };

  // 카드 0~4개 렌더
  posts.forEach((post) => {
    const col = document.createElement("div");
    col.className = "col-12 col-sm-6 col-lg-3 d-flex";
    col.innerHTML = makeCardHtml(post);
    area.appendChild(col);
  });

  // 부족하면 빈 카드 채우기(레이아웃 유지)
  for (let i = posts.length; i < 4; i++) {
    const col = document.createElement("div");
    col.className = "col-12 col-sm-6 col-lg-3 d-flex";
    col.innerHTML = `
      <div class="post-card h-100" style="display:flex;flex-direction:column;">
        <div class="card-top">
          <div class="left">
            <img class="profileImg" src="/assets/default-profile.png" alt="프로필"/>
            <div class="name-time">
              <p class="nickname">게시글 없음</p>
              <p class="time">&nbsp;</p>
            </div>
          </div>
        </div>
        <div class="image-wrap" style="display:flex;align-items:center;justify-content:center;aspect-ratio:1/1;background:#fafafa;">
          <span class="text-muted">없음</span>
        </div>
        <div class="card-body-ig">
          <div class="caption"><span class="name">-</span><span class="content">해당 온도의 게시글이 아직 없어요.</span></div>
        </div>
      </div>
    `;
    area.appendChild(col);
  }

// ── 상호작용(모달/좋아요/북마크) 바인딩: 동적 렌더이므로 여기서 직접 연결 ──
area.querySelectorAll(".post-card").forEach((card) => {
  card.addEventListener("click", (e) => {
    if (e.target.closest(".btn-action")) return; // 액션 버튼 클릭은 무시
    e.preventDefault();
    const postId = card.getAttribute("data-id");
    openDetailModal(postId); // ✅ 공용 유틸 사용
  });
});

area.querySelectorAll(".btn-like").forEach((btn) => {
  btn.addEventListener("click", (e) => {
    e.stopPropagation();
    e.preventDefault();
    const id = btn.getAttribute("data-id");
    const icon = btn.querySelector(".bi");
    const countEl = btn.parentElement.querySelector(".like-count");
    let current = Number(countEl.getAttribute("data-count") || 0);
    const liked = icon.classList.contains("bi-heart-fill");

    fetch(`/community/recommend/${id}`, { method: "POST" })
      .catch(() => {})
      .finally(() => {
        if (liked) {
          icon.classList.remove("bi-heart-fill");
          icon.classList.add("bi-heart");
          current = Math.max(0, current - 1);
        } else {
          icon.classList.remove("bi-heart");
          icon.classList.add("bi-heart-fill");
          current = current + 1;
        }
        countEl.setAttribute("data-count", current);
        countEl.textContent = k(current);
      });
  });
});

area.querySelectorAll(".btn-bookmark").forEach((btn) => {
  btn.addEventListener("click", (e) => {
    e.stopPropagation();
    e.preventDefault();
    const id = btn.getAttribute("data-id");
    const icon = btn.querySelector(".bi");
    const checked = icon.classList.contains("bi-bookmark-fill");

    fetch(`/community/bookmark/${id}`, { method: "POST" })
      .catch(() => {})
      .finally(() => {
        icon.classList.toggle("bi-bookmark-fill", !checked);
        icon.classList.toggle("bi-bookmark", checked);
      });
  });
});

area.querySelectorAll(".btn-comment").forEach((btn) => {
  btn.addEventListener("click", (e) => {
    e.stopPropagation();
    e.preventDefault();
    btn.closest(".post-card")?.click();
  });
});
}
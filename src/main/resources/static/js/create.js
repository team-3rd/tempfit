/******************************************************
 * create.js  (글쓰기 페이지)
 * - 이미지 선택 → 큰 미리보기(캐러셀) + 썸네일 생성
 * - 업로드 타일(88×88)을 클릭해 파일 선택
 * - 한 장만 선택하면 자동으로 대표(= 첫 번째)
 * - 드래그&드롭 정렬(맨 앞 = 대표), 개별 X 삭제
 * - 삭제/정렬 시 캐러셀/개수/히든필드 동기화
 * - 캐러셀은 첫/마지막 장에서 화살표 비활성화
 ******************************************************/

/* ─── 엘리먼트 ─── */
const form = document.getElementById("communityForm");
const imageInput = document.getElementById("imageFiles");
const previewContainer = document.getElementById("previewContainer");
const repImageIndexInput = document.getElementById("repImageIndex");
const dateInput = document.getElementById("dates");
const submitBtn = document.getElementById("bsubmitBtn");

/* 큰 미리보기 캐러셀 요소 */
const carouselEl = document.getElementById("createCarousel");
const carouselInner = carouselEl ? carouselEl.querySelector(".carousel-inner") : null;
const prevBtn = document.getElementById("carouselPrev");
const nextBtn = document.getElementById("carouselNext");

/* (옵션) 제목/스타일/성별 */
const titleInput = document.getElementById("title"); // 없으면 null
const styleGroup = document.getElementById("styleGroup");
const sexGroup = document.getElementById("sexGroup");
const sexChecks = document.querySelectorAll("input[name='sexSet']");

/* ─── 상태 ─── */
let items = [];
let objectUrls = [];

/* 정렬 순서 히든 */
let orderHidden = document.getElementById("imageOrder");
if (!orderHidden) {
  orderHidden = document.createElement("input");
  orderHidden.type = "hidden";
  orderHidden.name = "imageOrder";
  orderHidden.id = "imageOrder";
  form && form.appendChild(orderHidden);
}

/* 업로드 타일/개수 엘리먼트(동적 생성) */
let uploadWrap = null;
/* ▼ 파일 개수 텍스트는 숨김 처리하므로 참조만 남김 */
let fileCountEl = null;

/* ───────────────────────────────────────────────────────────
   유틸
   ─────────────────────────────────────────────────────────── */
function revokeAllObjectUrls() {
  objectUrls.forEach((u) => URL.revokeObjectURL(u));
  objectUrls = [];
}

/* 파일 input(FileList) 을 현재 items 순서대로 동기화 */
function syncInputFilesFromItems() {
  const dt = new DataTransfer();
  items.forEach((it) => dt.items.add(it.file));
  imageInput.files = dt.files;
}

function getCarouselInstance() {
  if (!carouselEl || !window.bootstrap || !bootstrap.Carousel) return null;
  return bootstrap.Carousel.getOrCreateInstance(carouselEl, {
    interval: false,
    ride: false,
    touch: true,
    keyboard: true,
    pause: false,
    wrap: false, // 🔒 루프 금지
  });
}

function bootstrapCarouselTo(index) {
  const inst = getCarouselInstance();
  if (!inst) return;
  inst.to(index);
  updateCarouselArrows(index);
}

/* 대표는 항상 맨 앞 */
function setRepresentativeToFirst() {
  repImageIndexInput.value = items.length > 0 ? "0" : "";
}

/* 현재 items 배열의 원래 인덱스 순서를 hidden에 저장 */
function updateOrderHidden() {
  const order = items.map((it) => it.origIndex);
  orderHidden.value = order.join(",");
}

/* 캐러셀 화살표 활성/비활성 */
function updateCarouselArrows(activeIndex) {
  const n = items.length;
  if (!prevBtn || !nextBtn) return;

  if (n <= 1) {
    prevBtn.classList.add("is-disabled");
    nextBtn.classList.add("is-disabled");
    return;
  }
  if (activeIndex <= 0) prevBtn.classList.add("is-disabled");
  else prevBtn.classList.remove("is-disabled");

  if (activeIndex >= n - 1) nextBtn.classList.add("is-disabled");
  else nextBtn.classList.remove("is-disabled");
}

/* 파일 개수 업데이트 (표시 숨김 상태라면 아무것도 안 함) */
function updateFileCount() {
  if (!fileCountEl) return;
  fileCountEl.textContent = `${items.length}장`;
}

/* 업로드 타일 DOM 생성 */
function buildUploadTile() {
  uploadWrap = document.createElement("div");
  uploadWrap.className = "upload-tile-wrap";

  const tile = document.createElement("div");
  tile.className = "upload-tile";
  tile.title = "이미지 선택";
  tile.addEventListener("click", () => imageInput.click());

  const label = document.createElement("div");
  label.className = "label";
  label.textContent = "사진 업로드";
  tile.appendChild(label);

  /* ▼ 파일 개수 텍스트 숨김(나중에 다시 쓰려면 주석 해제)
  fileCountEl = document.createElement("div");
  fileCountEl.className = "file-count";
  fileCountEl.textContent = "0장";
  */

  uploadWrap.appendChild(tile);
  /* uploadWrap.appendChild(fileCountEl); */

  return uploadWrap;
}

/* 썸네일 '대표 사진' 뱃지/aria 업데이트 */
function refreshRepBadges() {
  const wraps = previewContainer.querySelectorAll(".thumb-wrap");
  wraps.forEach((wrap, idx) => {
    let badge = wrap.querySelector(".thumb-badge");
    if (idx === 0) {
      if (!badge) {
        badge = document.createElement("span");
        badge.className = "thumb-badge";
        badge.textContent = "대표 사진";
        wrap.appendChild(badge);
      }
      wrap.setAttribute("aria-label", "대표 사진");
    } else {
      if (badge) badge.remove();
      wrap.removeAttribute("aria-label");
    }
  });
}

/* 썸네일 DOM 전체 재생성 (+ 업로드 타일 항상 맨 앞) */
function rebuildThumbnails() {
  previewContainer.innerHTML = "";

  // 업로드 타일
  previewContainer.appendChild(buildUploadTile());

  // 실제 썸네일들
  items.forEach((it, idx) => {
    const wrap = document.createElement("div");
    wrap.className = "thumb-wrap";
    wrap.draggable = true;
    wrap.dataset.index = String(idx);
    wrap.title = "드래그해서 순서를 바꿔주세요";

    const img = document.createElement("img");
    img.className = "thumb";
    img.src = it.url;
    img.alt = `thumb-${idx + 1}`;

    // 캐러셀 해당 슬라이드로 이동
    img.addEventListener("click", () => {
      bootstrapCarouselTo(idx);
    });

    // 개별 삭제 버튼
    const rm = document.createElement("button");
    rm.type = "button";
    rm.className = "thumb-remove";
    rm.innerHTML = '<i class="bi bi-x" aria-hidden="true"></i>';
    rm.title = "이 사진 제거";
    rm.addEventListener("click", (e) => {
      e.stopPropagation();
      items.splice(idx, 1);
      rebuildThumbnails();
      rebuildCarousel();
      setRepresentativeToFirst();
      updateOrderHidden();
      syncInputFilesFromItems();
      updateFileCount();
    });

    // DnD
    wrap.addEventListener("dragstart", onDragStart);
    wrap.addEventListener("dragover", onDragOver);
    wrap.addEventListener("dragleave", onDragLeave);
    wrap.addEventListener("drop", onDrop);
    wrap.addEventListener("dragend", onDragEnd);

    wrap.appendChild(img);
    wrap.appendChild(rm);
    previewContainer.appendChild(wrap);
  });

  refreshRepBadges();
  updateFileCount();
}

/* 캐러셀 DOM 전체 재생성 */
function rebuildCarousel() {
  if (!carouselInner) return;
  carouselInner.innerHTML = "";

  if (items.length === 0) {
    const empty = document.createElement("div");
    empty.id = "carouselEmpty";
    empty.className = "stage-empty";
    empty.textContent = "이미지를 업로드하면 여기에서 미리볼 수 있어요";
    carouselInner.appendChild(empty);
    updateCarouselArrows(0);
    return;
  }

  items.forEach((it, idx) => {
    const item = document.createElement("div");
    item.className = "carousel-item" + (idx === 0 ? " active" : "");
    const img = document.createElement("img");
    img.className = "create-photo";
    img.src = it.url;
    img.alt = `image-${idx + 1}`;
    img.draggable = false;
    item.appendChild(img);
    carouselInner.appendChild(item);
  });

  bootstrapCarouselTo(0);
}

/* files → items 변환 + 화면 구성 */
function filesToItemsAndBuild(fileList) {
  revokeAllObjectUrls();
  items = Array.from(fileList || []).map((f, i) => {
    const url = URL.createObjectURL(f);
    objectUrls.push(url);
    return { file: f, url, origIndex: i };
  });

  rebuildThumbnails();
  rebuildCarousel();

  setRepresentativeToFirst();
  updateOrderHidden();
  syncInputFilesFromItems();
  updateFileCount();
}

/* 인덱스의 item 제거(외부에서 쓸 때) */
function removeItemAt(index) {
  if (index < 0 || index >= items.length) return;
  try { URL.revokeObjectURL(items[index].url); } catch(e){}
  items.splice(index, 1);
  rebuildThumbnails();
  rebuildCarousel();
  setRepresentativeToFirst();
  updateOrderHidden();
  syncInputFilesFromItems();
  updateFileCount();
}

/* 드래그&드롭 관련 */
let dragFromIndex = null;

function onDragStart(e) {
  const wrap = e.currentTarget;
  dragFromIndex = Number(wrap.dataset.index);
  wrap.classList.add("dragging");
  e.dataTransfer.effectAllowed = "move";
}

function onDragOver(e) {
  e.preventDefault();
  const target = e.currentTarget;
  if (!target.classList.contains("thumb-wrap")) return;

  const rect = target.getBoundingClientRect();
  const midX = rect.left + rect.width / 2;

  target.classList.toggle("over-before", e.clientX < midX);
  target.classList.toggle("over-after", e.clientX >= midX);
}

function onDragLeave(e) {
  const target = e.currentTarget;
  target.classList.remove("over-before", "over-after");
}

function onDrop(e) {
  e.preventDefault();
  const target = e.currentTarget;
  const toIndexBase = Number(target.dataset.index);

  const rect = target.getBoundingClientRect();
  const toAfter = e.clientX >= rect.left + rect.width / 2;

  let toIndex = toAfter ? toIndexBase + 1 : toIndexBase;

  if (dragFromIndex === null || dragFromIndex === toIndexBase) {
    target.classList.remove("over-before", "over-after");
    return;
  }

  const moved = items.splice(dragFromIndex, 1)[0];
  if (toIndex > items.length) toIndex = items.length;
  if (dragFromIndex < toIndexBase && toAfter) toIndex--;

  items.splice(toIndex, 0, moved);

  rebuildThumbnails();
  rebuildCarousel();
  setRepresentativeToFirst();
  updateOrderHidden();
  syncInputFilesFromItems();

  target.classList.remove("over-before", "over-after");
}

function onDragEnd(e) {
  const wraps = previewContainer.querySelectorAll(".thumb-wrap");
  wraps.forEach((w) => w.classList.remove("dragging", "over-before", "over-after"));
  dragFromIndex = null;
}

/* 파일 선택 */
imageInput.addEventListener("change", function () {
  const files = this.files;
  if (!files || files.length === 0) {
    items = [];
    revokeAllObjectUrls();
    previewContainer.innerHTML = "";
    previewContainer.appendChild(buildUploadTile());
    if (carouselInner) {
      carouselInner.innerHTML = "";
      const empty = document.createElement("div");
      empty.id = "carouselEmpty";
      empty.className = "stage-empty";
      empty.textContent = "이미지를 업로드하면 여기에서 미리볼 수 있어요";
      carouselInner.appendChild(empty);
    }
    repImageIndexInput.value = "";
    orderHidden.value = "";
    updateCarouselArrows(0);
    updateFileCount();
    return;
  }
  filesToItemsAndBuild(files);
});

/* 캐러셀 슬라이드 이동 시 화살표 갱신 */
if (carouselEl) {
  carouselEl.addEventListener("slid.bs.carousel", (ev) => {
    const activeIndex = typeof ev.to === "number"
      ? ev.to
      : Array.from(carouselInner.querySelectorAll(".carousel-item")).findIndex((el) =>
          el.classList.contains("active")
        );
    updateCarouselArrows(activeIndex);
  });
}

/* 제목 글자수 (옵션) */
const titleHelp = document.getElementById("titleHelp");
if (titleInput && titleHelp) {
  titleInput.addEventListener("input", function () {
    titleHelp.textContent = this.value.length + " / 11";
  });
}

/* 스타일 체크박스 최대 2개 제한 (옵션) */
document.querySelectorAll(".style-check").forEach((chk) => {
  chk.addEventListener("change", () => {
    const checked = document.querySelectorAll(".style-check:checked");
    if (checked.length > 2) chk.checked = false;
  });
});

/* 날짜 초기값 및 범위 설정 */
(function initDate() {
  if (!dateInput) return;
  const date_now = new Date();
  const fmt = (d) => {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, "0");
    const dd = String(d.getDate()).padStart(2, "0");
    return `${y}-${m}-${dd}`;
  };
  const shift = (d, days) => {
    const nd = new Date(d);
    nd.setDate(nd.getDate() + days);
    return nd;
  };
  const todayStr = fmt(date_now);
  const minStr = fmt(shift(date_now, -2));
  const maxStr = fmt(date_now);

  dateInput.value = todayStr;
  dateInput.min = minStr;
  dateInput.max = maxStr;
})();

/* 위치 정보 설정 + 업로드 타일 초기 렌더 */
window.addEventListener("DOMContentLoaded", () => {
  if (previewContainer && !previewContainer.querySelector(".upload-tile-wrap")) {
    previewContainer.appendChild(buildUploadTile());
  }
  updateFileCount();

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition((pos) => {
      const latEl = document.getElementById("lat");
      const lonEl = document.getElementById("lon");
      if (latEl) latEl.value = pos.coords.latitude;
      if (lonEl) lonEl.value = pos.coords.longitude;
    });
  }
});

/* 폼 유효성 & 중복 제출 방지 */
form &&
  form.addEventListener("submit", function (e) {
    let valid = true;

    if (titleInput) {
      if (!titleInput.value.trim()) {
        titleInput.classList.add("is-invalid");
        valid = false;
      } else {
        titleInput.classList.remove("is-invalid");
      }
    }

    if (!imageInput.files || imageInput.files.length === 0) {
      imageInput.classList.add("is-invalid");
      valid = false;
    } else {
      imageInput.classList.remove("is-invalid");
    }

    setRepresentativeToFirst();
    updateOrderHidden();

    if (styleGroup) {
      const checkedStyles = Array.from(
        document.querySelectorAll(".style-check")
      ).filter((chk) => chk.checked);
      if (checkedStyles.length < 1 || checkedStyles.length > 2) {
        styleGroup.classList.add("was-validated");
        valid = false;
      } else {
        styleGroup.classList.remove("was-validated");
      }
    }
    if (sexGroup) {
      const checkedSexes = Array.from(sexChecks || []).filter((chk) => chk.checked);
      const sexFeedback = sexGroup.querySelector(".invalid-feedback");
      if (checkedSexes.length < 1) {
        sexGroup.classList.add("was-validated");
        if (sexFeedback) sexFeedback.style.display = "block";
        valid = false;
      } else {
        sexGroup.classList.remove("was-validated");
        if (sexFeedback) sexFeedback.style.display = "none";
      }
    }

    if (!valid) {
      e.preventDefault();
      e.stopPropagation();
    } else if (submitBtn) {
      submitBtn.disabled = true;
      submitBtn.textContent = "등록중…";
    }
  });

/* 초기 상태 */
if (imageInput && imageInput.files && imageInput.files.length > 0) {
  filesToItemsAndBuild(imageInput.files);
} else {
  updateCarouselArrows(0);
}

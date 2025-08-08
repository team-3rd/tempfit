// ─── 대표 이미지 선택용 변수들 ───
const imageInput = document.getElementById("imageFiles");
const previewContainer = document.getElementById("previewContainer");
const repImageIndexInput = document.getElementById("repImageIndex");
let selectedFiles = [];

// ─── 대표 이미지 선택 미리보기 ───
imageInput.addEventListener("change", function () {
  selectedFiles = Array.from(this.files);
  previewContainer.innerHTML = "";
  repImageIndexInput.value = ""; // 대표 이미지 초기화

  selectedFiles.forEach((file, index) => {
    const reader = new FileReader();
    reader.onload = function (e) {
      const wrapper = document.createElement("div");
      wrapper.className = "position-relative";

      const img = document.createElement("img");
      img.src = e.target.result;
      img.style.width = "200px";
      img.style.height = "200px";
      img.style.objectFit = "cover";
      img.style.border = "3px solid transparent";
      img.style.cursor = "pointer";

      const images = document.querySelectorAll("#previewContainer img");

      img.addEventListener("click", () => {
        // 모든 이미지 테두리 제거
        images.forEach((el) => (el.style.border = "3px solid transparent"));

        // 선택한 이미지만 강조
        img.style.border = "3px solid #007bff";
        repImageIndexInput.value = index;
      });

      wrapper.appendChild(img);
      previewContainer.appendChild(wrapper);
    };
    reader.readAsDataURL(file);
  });
});

// ─── 제목 글자수 표시 ───
const titleInput = document.getElementById("title");
const titleHelp = document.getElementById("titleHelp");
titleInput.addEventListener("input", function () {
  titleHelp.textContent = this.value.length + " / 11";
});

// ─── 스타일 체크박스 최대 2개 제한 ───
document.querySelectorAll(".style-check").forEach((chk) => {
  chk.addEventListener("change", () => {
    const checked = document.querySelectorAll(".style-check:checked");
    if (checked.length > 2) chk.checked = false;
  });
});

// ─── 성별 체크박스 요소 ───
const sexChecks = document.querySelectorAll("input[name='sexSet']");

// ─── 날짜 초기값 및 범위 설정 ───
const date_now = new Date();
function formatDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}
function adjustDate(baseDate, days) {
  const newDate = new Date(baseDate);
  newDate.setDate(newDate.getDate() + days);
  return newDate;
}
const todayStr = formatDate(date_now);
const minStr = formatDate(adjustDate(date_now, -2));
const maxStr = formatDate(date_now);
const dateInput = document.getElementById("dates");
dateInput.value = todayStr;
dateInput.min = minStr;
dateInput.max = maxStr;

// ─── 위치 정보 설정 ───
window.addEventListener("DOMContentLoaded", () => {
  navigator.geolocation.getCurrentPosition((pos) => {
    document.getElementById("lat").value = pos.coords.latitude;
    document.getElementById("lon").value = pos.coords.longitude;
  });
});

// ─── 유효성 검사 및 중복 제출 방지 ───
document
  .getElementById("communityForm")
  .addEventListener("submit", function (e) {
    let valid = true;

    // 제목 검사
    if (!titleInput.value.trim()) {
      titleInput.classList.add("is-invalid");
      valid = false;
    } else {
      titleInput.classList.remove("is-invalid");
    }

    // 이미지 업로드 검사
    if (!imageInput.files || imageInput.files.length === 0) {
      imageInput.classList.add("is-invalid");
      valid = false;
    } else {
      imageInput.classList.remove("is-invalid");
    }

    // 대표 이미지 선택 여부 검사
    if (!repImageIndexInput.value) {
      alert("대표 이미지를 클릭해서 선택해주세요.");
      valid = false;
    }

    // 스타일 검사
    const checkedStyles = Array.from(
      document.querySelectorAll(".style-check")
    ).filter((chk) => chk.checked);
    const styleGroup = document.getElementById("styleGroup");
    if (checkedStyles.length < 1 || checkedStyles.length > 2) {
      styleGroup.classList.add("was-validated");
      valid = false;
    } else {
      styleGroup.classList.remove("was-validated");
    }

    // 성별 검사
    const checkedSexes = Array.from(sexChecks).filter((chk) => chk.checked);
    const sexGroup = document.getElementById("sexGroup");
    const sexFeedback = sexGroup.querySelector(".invalid-feedback");
    if (checkedSexes.length < 1) {
      sexGroup.classList.add("was-validated");
      sexFeedback.style.display = "block";
      valid = false;
    } else {
      sexGroup.classList.remove("was-validated");
      sexFeedback.style.display = "none";
    }

    if (!valid) {
      e.preventDefault();
      e.stopPropagation();
    } else {
      const btn = document.getElementById("bsubmitBtn");
      btn.disabled = true;
      btn.textContent = "등록중…";
    }
  });

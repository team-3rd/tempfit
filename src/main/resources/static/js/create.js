// 대표 사진 미리보기 + X 버튼 제어
const repInput = document.getElementById("repImage");
const repXBtn = document.getElementById("repImageXBtnPreview");
const previewImage = document.getElementById("previewImage");
const previewContainer = document.getElementById("repImagePreviewContainer");

repXBtn.classList.remove("show");
previewContainer.style.display = "none";
previewImage.style.display = "none";

repInput.addEventListener("change", function () {
  const file = this.files[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = function (e) {
      previewImage.src = e.target.result;
      previewImage.style.display = "block";
      previewContainer.style.display = "flex";
      repXBtn.classList.add("show");
    };
    reader.readAsDataURL(file);
  } else {
    previewImage.src = "";
    previewImage.style.display = "none";
    previewContainer.style.display = "none";
    repXBtn.classList.remove("show");
  }
});

repXBtn.onclick = function () {
  repInput.value = "";
  previewImage.src = "";
  previewImage.style.display = "none";
  previewContainer.style.display = "none";
  repXBtn.classList.remove("show");
};

// 추가 사진 X 버튼 제어
const extraInput = document.getElementById("extraImages");
const extraXBtn = document.getElementById("extraImagesXBtn");
extraXBtn.classList.remove("show");

extraInput.addEventListener("change", function () {
  extraXBtn.classList.toggle("show", this.files.length > 0);
});
extraXBtn.onclick = function () {
  extraInput.value = "";
  extraXBtn.classList.remove("show");
};

// 제목 글자수 표시
const titleInput = document.getElementById("title");
const titleHelp = document.getElementById("titleHelp");
titleInput.addEventListener("input", function () {
  titleHelp.textContent = this.value.length + " / 11";
});

// 스타일 체크박스 최대 2개 제한
document.querySelectorAll(".style-check").forEach(function (chk) {
  chk.addEventListener("change", function () {
    const checked = document.querySelectorAll(".style-check:checked");
    if (checked.length > 2) this.checked = false;
  });
});

// 성별 체크박스 (둘 다 선택 가능)
const sexChecks = document.querySelectorAll("input[name='sexSet']");

// 날짜 범위 설정
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

// 좌표 설정
window.addEventListener("DOMContentLoaded", () => {
  navigator.geolocation.getCurrentPosition((pos) => {
    document.getElementById("lat").value = pos.coords.latitude;
    document.getElementById("lon").value = pos.coords.longitude;
  });
});

// 커스텀 유효성 검사 + 중복 클릭 방지
document
  .getElementById("communityForm")
  .addEventListener("submit", function (e) {
    let valid = true;

    // 제목 필수
    if (!titleInput.value.trim()) {
      titleInput.classList.add("is-invalid");
      valid = false;
    } else {
      titleInput.classList.remove("is-invalid");
    }

    // 대표 사진 필수
    if (!repInput.files || repInput.files.length === 0) {
      repInput.classList.add("is-invalid");
      valid = false;
    } else {
      repInput.classList.remove("is-invalid");
    }

    // 스타일 필수 (1~2개)
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

    // 성별 필수 (최소 1개)
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

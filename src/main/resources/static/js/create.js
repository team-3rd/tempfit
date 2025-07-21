// 대표 사진 미리보기
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

// 추가 사진 X 버튼
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

// 커스텀 유효성 검사
document.getElementById("communityForm").addEventListener("submit", function (e) {
  let valid = true;

  // 제목
  if (!titleInput.value.trim()) {
    titleInput.classList.add("is-invalid");
    valid = false;
  } else {
    titleInput.classList.remove("is-invalid");
  }

  // 대표사진
  if (!repInput.files || repInput.files.length === 0) {
    repInput.classList.add("is-invalid");
    valid = false;
  } else {
    repInput.classList.remove("is-invalid");
  }

  // 스타일 체크
  const styleChecks = document.querySelectorAll(".style-check");
  const checkedStyles = Array.from(styleChecks).filter((chk) => chk.checked);
  const styleGroup = document.getElementById("styleGroup");
  if (checkedStyles.length < 1 || checkedStyles.length > 2) {
    styleGroup.classList.add("was-validated");
    valid = false;
  } else {
    styleGroup.classList.remove("was-validated");
  }

  // 성별 체크
  const sexChecks = document.querySelectorAll("input[name='sexSet']");
  const checkedSexes = Array.from(sexChecks).filter((chk) => chk.checked);
  const sexGroup = document.getElementById("sexGroup");
  const sexFeedback = sexGroup.querySelector(".invalid-feedback");

  if (checkedSexes.length !== 1) {
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
  }
});

// 스타일 2개 제한
document.querySelectorAll(".style-check").forEach(function (chk) {
  chk.addEventListener("change", function () {
    const checked = document.querySelectorAll(".style-check:checked");
    if (checked.length > 2) {
      this.checked = false;
    }
  });
});

// 성별 1개 제한
const sexChecks = document.querySelectorAll("input[name='sexSet']");
sexChecks.forEach(chk => {
  chk.addEventListener("change", () => {
    if (chk.checked) {
      sexChecks.forEach(other => {
        if (other !== chk) other.checked = false;
      });
    }
  });
});

// 날짜 세팅
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
const maxStr = formatDate(adjustDate(date_now, +4));
const dateInput = document.getElementById("dates");
dateInput.value = todayStr;
dateInput.min = minStr;
dateInput.max = maxStr;

// 시간대 체크 1개 제한
document.querySelectorAll(".time-check").forEach(function (chk) {
  chk.addEventListener("change", function () {
    const checked = document.querySelectorAll(".time-check:checked");
    if (checked.length > 1) {
      this.checked = false;
    }
  });
});

// 좌표
window.addEventListener("DOMContentLoaded", () => {
  navigator.geolocation.getCurrentPosition((pos) => {
    const lat = pos.coords.latitude;
    const lon = pos.coords.longitude;
    document.getElementById("lat").value = lat;
    document.getElementById("lon").value = lon;
  });
});

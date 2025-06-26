// 대표 사진 X 버튼 제어
const repInput = document.getElementById("repImage");
const repXBtn = document.getElementById("repImageXBtn");
repXBtn.classList.remove("show");
repInput.addEventListener("change", function () {
  repXBtn.classList.toggle("show", this.files.length > 0);
});
repXBtn.onclick = function () {
  repInput.value = "";
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

// 커스텀 유효성 검사
document
  .getElementById("communityForm")
  .addEventListener("submit", function (e) {
    let valid = true;

    // 1. 제목 필수
    const title = document.getElementById("title");
    if (!title.value.trim()) {
      title.classList.add("is-invalid");
      valid = false;
    } else {
      title.classList.remove("is-invalid");
    }

    // 2. 대표 사진 필수
    if (!repInput.files || repInput.files.length === 0) {
      repInput.classList.add("is-invalid");
      valid = false;
    } else {
      repInput.classList.remove("is-invalid");
    }

    // 3. 스타일 필수 (1~2개)
    const styleChecks = document.querySelectorAll(".style-check");
    const checkedStyles = Array.from(styleChecks).filter((chk) => chk.checked);
    const styleGroup = document.getElementById("styleGroup");
    if (checkedStyles.length < 1 || checkedStyles.length > 2) {
      styleGroup.classList.add("was-validated");
      valid = false;
    } else {
      styleGroup.classList.remove("was-validated");
    }

    if (!valid) {
      e.preventDefault();
      e.stopPropagation();
    }
  });

// 스타일 체크박스 체크 제한 (최대 2개)
document.querySelectorAll(".style-check").forEach(function (chk) {
  chk.addEventListener("change", function () {
    const checked = document.querySelectorAll(".style-check:checked");
    if (checked.length > 2) {
      this.checked = false;
    }
  });
});

// 날짜 입력칸 설정 부분
const date_now = new Date();

// 날짜 포멧팅 함수
function formatDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0"); // 0부터 시작하므로 +1
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

// 날짜 계산 함수
function adjustDate(baseDate, days) {
  const newDate = new Date(baseDate);
  newDate.setDate(newDate.getDate() + days);
  return newDate;
}

// 날짜 데이터 설정
const todayStr = formatDate(date_now);
const minStr = formatDate(adjustDate(date_now, -2));
const maxStr = formatDate(adjustDate(date_now, +4));

// input에 넣기
const dateInput = document.getElementById("date");
dateInput.value = todayStr;
dateInput.min = minStr;
dateInput.max = maxStr;

// 시간대 체크박스 1개로 제한
document.querySelectorAll(".time-check").forEach(function (chk) {
  chk.addEventListener("change", function () {
    const checked = document.querySelectorAll(".time-check:checked");
    if (checked.length > 1) {
      this.checked = false;
    }
  });
});

// 좌표 값 가져오기
document.getElementById("subminBtn").addEventListener("click", () => {
  navigator.geolocation.getCurrentPosition((pos) => {
    const lat = pos.coords.latitude;
    const lon = pos.coords.longitude;

    document.getElementById("lat").value = lat;
    document.getElementById("lon").value = lon;
  });
});

// 평균온도 출력을 위한 값 export하기
const dates = document.getElementById("date").value;
const isDay = document.getElementById("dayTime").checked;
const isNight = document.getElementById("nightTime").checked;
const lat = document.getElementById("lat").value;
const lon = document.getElementById("lon").value;

export { dates, isDay, isNight, lat, lon };

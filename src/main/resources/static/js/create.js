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

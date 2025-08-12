const detailModal = new bootstrap.Modal(document.getElementById("detailModal"));

detailModal.addEventListener("show-bs-modal", function () {
  // 이미지 슬라이드 설정
  const carouselList = document.querySelector(".carousel-indicators");
  const imgLength = document.querySelectorAll(".extraImg").length;

  for (let i = 0; i < imgLength; i++) {
    const extraImgCarousel = document.createElement("button");
    extraImgCarousel.type = "button";
    extraImgCarousel.setAttribute("data-bs-target", "#carouselIndicators");
    extraImgCarousel.setAttribute("data-bs-slide-to", `${i + 1}`);
    extraImgCarousel.ariaLabel = `Slide ${i + 2}`;

    carouselList.appendChild(extraImgCarousel);
  }

  // 모달 창
  const modal = new bootstrap.Modal(document.getElementById("imgModal"), {
    keyboard: true,
  });
  const imgNo = document.querySelector(".imgNo");
  const imgLists = document.querySelector(".imgList");
  const selectedImg = document.getElementById("selectedImg");

  const images = document.querySelectorAll(".detail-image");

  let currentInx = 0;

  images.forEach((e, index) => {
    const imgList = document.createElement("li");
    imgList.className = "imgLi";
    imgList.style.paddingLeft = "2px";
    imgList.style.paddingRight = "2px";
    imgList.style.listStyle = "none";

    const listImg = document.createElement("img");
    listImg.src = e.src;
    listImg.className = "imgLis";
    listImg.style.width = "100px";
    listImg.style.height = "100px";
    listImg.style.objectFit = "cover";
    listImg.style.border = "1px solid #999";
    listImg.style.cursor = "pointer";
    listImg.addEventListener("click", function () {
      document.querySelectorAll(".imgList img").forEach((i) => {
        i.style.border = "1px solid #999";
      });
      listImg.style.border = "3px solid #007bff";
      selectedImg.src = listImg.src;
      currentInx = index;
      imgNo.textContent = `${index + 1} / ${images.length}`;
    });

    imgList.appendChild(listImg);
    imgLists.appendChild(imgList);

    e.addEventListener("click", function () {
      currentInx = index;
      selectedImg.src = this.src;

      imgNo.textContent = `${index + 1} / ${images.length}`;

      const imgLis = document.querySelectorAll(".imgLis");
      imgLis.forEach((el) => {
        el.style.border = "1px solid #999";
        if (this.src == el.src) {
          el.style.border = "3px solid #007bff";
        }
      });
      modal.show();
    });
  });
});

detailModal.addEventListener("show.bs.modal", function () {
  // 이미지 확대
  const canvas1 = document.getElementById("zoomed1");
  const canvas2 = document.getElementById("zoomed2");
  const canvas3 = document.getElementById("zoomed3");

  const selectedImg = document.getElementById("selectedImg");
  const scaleX = selectedImg.naturalWidth / selectedImg.width;
  const scaleY = selectedImg.naturalHeight / selectedImg.height;

  const rect = selectedImg.getBoundingClientRect();
  let size = 600;

  // 확대 1단
  selectedImg.addEventListener("click", function (e) {
    const ctx1 = canvas1.getContext("2d");
    // 클릭한 좌표 구하기
    const x1 = e.clientX - rect.left;
    const y1 = e.clientY - rect.top;

    // 이미지 상 좌표 계산
    const imgX1 = x1 * scaleX;
    const imgY1 = y1 * scaleY;

    // display 설정
    canvas1.width = 750;
    canvas1.height = 750;
    selectedImg.style.display = "none";
    canvas1.style.display = "inline-block";

    // 이미지 로드 확인 후 확대
    const tempImg1 = new Image();
    tempImg1.src = selectedImg.src;
    tempImg1.onload = () => {
      ctx1.clearRect(0, 0, canvas1.width, canvas1.height);

      // 확대된 영역 그리기
      ctx1.drawImage(
        tempImg1,
        imgX1 - size / 2,
        imgY1 - size / 2,
        size,
        size,
        0,
        0,
        750,
        750
      );
    };
  });

  // 확대 2단
  canvas1.addEventListener("click", function (e) {
    const ctx2 = canvas2.getContext("2d");
    size = 450;

    // 클릭한 좌표 구하기
    const x2 = e.clientX - rect.left;
    const y2 = e.clientY - rect.top;

    // 이미지 상 좌표 계산
    const imgX2 = x2 * scaleX;
    const imgY2 = y2 * scaleY;

    // display 설정
    canvas2.width = 750;
    canvas2.height = 750;
    canvas1.style.display = "none";
    canvas2.style.display = "inline-block";

    // 이미지 로드 확인 후 확대
    const tempImg2 = new Image();
    tempImg2.src = selectedImg.src;
    tempImg2.onload = () => {
      ctx2.clearRect(0, 0, canvas2.width, canvas2.height);

      // 확대된 영역 그리기
      ctx2.drawImage(
        tempImg2,
        imgX2 - size / 2,
        imgY2 - size / 2,
        size,
        size,
        0,
        0,
        750,
        750
      );
    };
  });

  // 확대 3단
  canvas2.addEventListener("click", function (e) {
    const ctx3 = canvas3.getContext("2d");
    size = 300;

    // 클릭한 좌표 구하기
    const x3 = e.clientX - rect.left;
    const y3 = e.clientY - rect.top;

    // 이미지 상 좌표 계산
    const imgX3 = x3 * scaleX;
    const imgY3 = y3 * scaleY;

    // display 설정
    canvas3.width = 750;
    canvas3.height = 750;
    canvas2.style.display = "none";
    canvas3.style.display = "inline-block";

    // 이미지 로드 확인 후 확대
    const tempImg3 = new Image();
    tempImg3.src = selectedImg.src;
    tempImg3.onload = () => {
      ctx3.clearRect(0, 0, canvas3.width, canvas3.height);

      // 확대된 영역 그리기
      ctx3.drawImage(
        tempImg3,
        imgX3 - size / 2,
        imgY3 - size / 2,
        size,
        size,
        0,
        0,
        750,
        750
      );
    };
  });

  // 이미지 축소 1단
  canvas1.addEventListener("keydown", function (e) {
    if (e.key == "Escape") {
      canvas1.style.display = "none";
      canvas2.style.display = "none";
      canvas3.style.display = "none";
      selectedImg.style.display = "inline-block";
    }
  });

  // 이미지 축소 2단
  canvas1.addEventListener("keydown", function (e) {
    if (e.key == "Escape") {
      canvas1.style.display = "inline-block";
      canvas2.style.display = "none";
      canvas3.style.display = "none";
      selectedImg.style.display = "none";
    }
  });

  // 이미지 축소 3단
  canvas1.addEventListener("keydown", function (e) {
    if (e.key == "Escape") {
      canvas1.style.display = "none";
      canvas2.style.display = "inline-block";
      canvas3.style.display = "none";
      selectedImg.style.display = "none";
    }
  });
});

// 모달 창 닫기 설정
document
  .getElementById("imgModal")
  .addEventListener("hidden.bs.modal", function () {
    const canvas1 = document.getElementById("zoomed1");
    const canvas2 = document.getElementById("zoomed2");
    const canvas3 = document.getElementById("zoomed3");

    canvas1.style.display = "none";
    canvas2.style.display = "none";
    canvas3.style.display = "none";
    selectedImg.style.display = "inline-block";
    size = 600;
  });

// 이전 이미지
function showPrevImg() {
  const modalImgLi = document.querySelectorAll(".imgList img");

  canvas.style.display = "none";
  selectedImg.style.display = "inline-block";

  if (currentInx > 0) {
    currentInx--;
    selectedImg.src = images[currentInx].src;

    modalImgLi.forEach((e) => {
      e.style.border = "1px solid #999";
    });
    modalImgLi[currentInx].style.border = "3px solid #007bff";

    imgNo.textContent = `${currentInx + 1} / ${images.length}`;
  } else if (currentInx == 0) {
    currentInx += images.length - 1;
    selectedImg.src = images[currentInx].src;

    modalImgLi.forEach((e) => {
      e.style.border = "1px solid #999";
    });
    modalImgLi[currentInx].style.border = "3px solid #007bff";

    imgNo.textContent = `${currentInx + 1} / ${images.length}`;
  }
}

// 다음 이미지
function showNextImg() {
  const modalImgLi = document.querySelectorAll(".imgList img");

  canvas.style.display = "none";
  selectedImg.style.display = "inline-block";

  if (currentInx < images.length - 1) {
    currentInx++;
    selectedImg.src = images[currentInx].src;

    modalImgLi.forEach((e) => {
      e.style.border = "1px solid #999";
    });
    modalImgLi[currentInx].style.border = "3px solid #007bff";

    imgNo.textContent = `${currentInx + 1} / ${images.length}`;
  } else if (currentInx == images.length - 1) {
    currentInx = 0;
    selectedImg.src = images[currentInx].src;

    modalImgLi.forEach((e) => {
      e.style.border = "1px solid #999";
    });
    modalImgLi[currentInx].style.border = "3px solid #007bff";

    imgNo.textContent = `${currentInx + 1} / ${images.length}`;
  }
}

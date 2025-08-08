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
const modal = new bootstrap.Modal(document.getElementById("modal"), {
  keyboard: false,
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

// 모달 창 닫기
document.querySelector(".modal-body").addEventListener("click", () => {
  modal.hide();
});

// 이전 이미지
function showPrevImg() {
  const modalImgLi = document.querySelectorAll(".imgList img");

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

const img = document.getElementById("selectedImg");

// transform 값 설정
let transformState = {
  rotate: 0,
  scale: 1,
};

// 이미지 회전
function rotate() {
  transformState.rotate += 90;
  applyTransform();
}

// 이미지 확대
function zoomin() {
  transformState.scale += 0.2;
  applyTransform();
}

// 이미지 축소
function zoomout() {
  transformState.scale -= 0.2;
  applyTransform();
}

// transform 적용
function applyTransform() {
  img.style.transform = `rotate(${transformState.rotate}deg) scale(${transformState.scale})`;
}

// 이미지 원래대로
const original = document.querySelector(".btn-reset");

original.addEventListener("click", () => {
  img.style.transform = "none";
  transformState.rotate = 0;
  transformState.scale = 1;
});

// 툴팁 설정
const tooltipTriggerList = [].slice.call(
  document.querySelectorAll('[data-toggle="tooltip"]')
);
const tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
  return new bootstrap.Tooltip(tooltipTriggerEl);
});

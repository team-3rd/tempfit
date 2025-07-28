const adList = [];
const allAdList = [];

let clickedText = "";
let clickedSiText = "";
let dosis = "";

// 주소 데이터 로드
window.addEventListener("DOMContentLoaded", async () => {
  await fetch(
    `https://raw.githubusercontent.com/Dogyeong-Kim/adData/refs/heads/main/ad_data.json`
  )
    .then((res) => res.json())
    .then((data) => {
      data.forEach((ads) => {
        // 상위 주소 리스트
        if (!ads.ad_name.includes(" ")) {
          adList.push(ads.ad_name);
        }

        // 전체 주소 리스트
        if (
          ads.ad_name != ads.lowest_ad_name ||
          ads.lowest_ad_name == "세종특별자치시"
        ) {
          allAdList.push(ads.ad_name);
        }
      });
    });

  // 첫 접속 때 현위치 표시
  navigator.geolocation.getCurrentPosition((pos) => {
    const lat = pos.coords.latitude;
    const lon = pos.coords.longitude;

    fetch(
      `https://geocode.googleapis.com/v4beta/geocode/location/${lat},${lon}?key=AIzaSyAH3J5S71gGtsQUQ-ABAoLmHQZ2kaEA88g`
    )
      .then((res) => res.json())
      .then((loc) => {
        const addressArray = loc.results[2].formattedAddress.split(" ");

        document.querySelector(".dosi").textContent = "";

        document
          .querySelector(".dosi")
          .appendChild(document.createTextNode(addressArray[1] + " "));

        const icon = document.createElement("img");
        icon.classList.add("img");
        icon.width = `10`;
        icon.height = `10`;
        icon.src = `https://img.icons8.com/ios-filled/10/sort-down.png`;
        icon.alt = `sort-down`;
        document.querySelector(".dosi").appendChild(icon);
        document.querySelector(".layer-do").classList.add("hide");
        document.querySelector(".list-li-left").classList.remove("unfold");

        document.querySelector(".sigungu").textContent = "";

        document
          .querySelector(".sigungu")
          .appendChild(document.createTextNode(addressArray[2] + " "));

        const icons = document.createElement("img");
        icons.classList.add("img");
        icons.width = `10`;
        icons.height = `10`;
        icons.src = `https://img.icons8.com/ios-filled/10/sort-down.png`;
        icons.alt = `sort-down`;
        document.querySelector(".sigungu").appendChild(icons);
        document.querySelector(".layer-si").classList.add("hide");
        document.querySelector(".list-li-center").classList.remove("unfold");

        document.querySelector(".si-btn").classList.remove("disabled");

        dosis = document.querySelector(".dosi").textContent;
      });
  });
});

// 도·시 리스트
function doList() {
  let result = "";

  adList.forEach((ads) => {
    result += `<li class="list-li-do">`;
    result += `<a href="#" class="li-do-btn" onclick="showDo()">`;
    result += `${ads}</a>`;
    result += `</li>`;
  });
  result += `<a href="javascript:hideDoList()" class="list-close">`;
  result += `<svg xmlns="http://www.w3.org/2000/svg" x="0px" y="0px" width="20" height="20" viewBox="0 0 40 40">
            <path d="M 7.71875 6.28125 L 6.28125 7.71875 L 23.5625 25 L 6.28125 42.28125 L 7.71875 43.71875 L 25 26.4375 L 42.28125 43.71875 L 43.71875 42.28125 L 26.4375 25 L 43.71875 7.71875 L 42.28125 6.28125 L 25 23.5625 Z"></path>
            </svg>`;
  result += `</a>`;

  document.querySelector(".list-do").innerHTML = result;

  // 도·시 리스트 토글
  document.querySelector(".layer-do").classList.toggle("hide");
  document.querySelector(".list-li-left").classList.toggle("unfold");

  // 리스트 탭 접힘 확인표시 변경
  const dosi = document.querySelector(".dosi");
  if (document.querySelector(".list-li-left").classList.length == 2) {
    dosi.removeChild(dosi.querySelector(".img"));

    const icon = document.createElement("img");
    icon.classList.add("img");
    icon.width = `10`;
    icon.height = `10`;
    icon.src = `https://img.icons8.com/ios-filled/10/sort-up.png`;
    icon.alt = `sort-up`;
    dosi.appendChild(icon);
  } else {
    dosi.removeChild(dosi.querySelector(".img"));

    const icon = document.createElement("img");
    icon.classList.add("img");
    icon.width = `10`;
    icon.height = `10`;
    icon.src = `https://img.icons8.com/ios-filled/10/sort-down.png`;
    icon.alt = `sort-down`;
    dosi.appendChild(icon);
  }
}

// 선택한 도·시 이름 출력
function showDo() {
  document.querySelectorAll(".list-do").forEach((ads) => {
    ads.addEventListener("click", function (event) {
      if (event.target.classList.contains("li-do-btn")) {
        event.preventDefault();

        clickedText = event.target.textContent;
        document.querySelector(".dosi").textContent = "";

        document
          .querySelector(".dosi")
          .appendChild(document.createTextNode(clickedText + " "));

        const icon = document.createElement("img");
        icon.classList.add("img");
        icon.width = `10`;
        icon.height = `10`;
        icon.src = `https://img.icons8.com/ios-filled/10/sort-down.png`;
        icon.alt = `sort-down`;
        document.querySelector(".dosi").appendChild(icon);
        document.querySelector(".layer-do").classList.add("hide");
        document.querySelector(".list-li-left").classList.remove("unfold");

        dosis = clickedText;

        if (
          adList.indexOf(clickedText) != -1 &&
          clickedText != "세종특별자치시"
        ) {
          if (document.querySelector(".si-btn").classList.length == 2) {
            document.querySelector(".si-btn").classList.remove("disabled");
          }
        } else if (clickedText == "세종특별자치시") {
          document.querySelector(".si-btn").classList.add("disabled");
        }
      }
    });
  });
}

// 도·시 리스트 닫기
function hideDoList() {
  document.querySelector(".layer-do").classList.add("hide");
  document.querySelector(".list-li-left").classList.remove("unfold");

  const dosi = document.querySelector(".dosi");
  dosi.removeChild(dosi.querySelector(".img"));

  const icon = document.createElement("img");
  icon.classList.add("img");
  icon.width = `10`;
  icon.height = `10`;
  icon.src = `https://img.icons8.com/ios-filled/10/sort-down.png`;
  icon.alt = `sort-down`;
  dosi.appendChild(icon);
}

// 시·군·구 리스트
function siList() {
  let result = "";

  allAdList.forEach((ads) => {
    if (ads.includes(dosis) && ads != "세종특별자치시") {
      const adSplit = ads.split(" ");

      if (adSplit.length == 2) {
        result += `<li class="list-li-si">`;
        result += `<a href="javascript:weatherLoad()" class="li-si-btn" onclick="showSi()">`;
        result += `${adSplit[1]}</a>`;
        result += `</li>`;
      } else if ((adSplit.length = 3)) {
        result += `<li class="list-li-si">`;
        result += `<a href="javascript:weatherLoad()" class="li-si-btn" onclick="showSi()">`;
        result += `${adSplit[1]} ${adSplit[2]}</a>`;
        result += `</li>`;
      }
    }
  });
  result += `<a href="javascript:hideSiList()" class="list-close">`;
  result += `<svg xmlns="http://www.w3.org/2000/svg" x="0px" y="0px" width="20" height="20" viewBox="0 0 40 40">
            <path d="M 7.71875 6.28125 L 6.28125 7.71875 L 23.5625 25 L 6.28125 42.28125 L 7.71875 43.71875 L 25 26.4375 L 42.28125 43.71875 L 43.71875 42.28125 L 26.4375 25 L 43.71875 7.71875 L 42.28125 6.28125 L 25 23.5625 Z"></path>
            </svg>`;
  result += `</a>`;

  document.querySelector(".list-si").innerHTML = result;

  // 시·군·구 리스트 토글
  document.querySelector(".layer-si").classList.toggle("hide");
  document.querySelector(".list-li-center").classList.toggle("unfold");

  // 리스트 탭 접힘 확인표시 변경
  const sigungu = document.querySelector(".sigungu");
  if (document.querySelector(".list-li-center").classList.length == 2) {
    sigungu.removeChild(sigungu.querySelector(".img"));

    const icon = document.createElement("img");
    icon.classList.add("img");
    icon.width = `10`;
    icon.height = `10`;
    icon.src = `https://img.icons8.com/ios-filled/10/sort-up.png`;
    icon.alt = `sort-up`;
    sigungu.appendChild(icon);
  } else {
    sigungu.removeChild(sigungu.querySelector(".img"));

    const icon = document.createElement("img");
    icon.classList.add("img");
    icon.width = `10`;
    icon.height = `10`;
    icon.src = `https://img.icons8.com/ios-filled/10/sort-down.png`;
    icon.alt = `sort-down`;
    sigungu.appendChild(icon);
  }
}

// 시·군·구 리스트 닫기
function hideSiList() {
  document.querySelector(".layer-si").classList.add("hide");
  document.querySelector(".list-li-center").classList.toggle("unfold");

  const sigungu = document.querySelector(".sigungu");
  sigungu.removeChild(sigungu.querySelector(".img"));

  const icon = document.createElement("img");
  icon.classList.add("img");
  icon.width = `10`;
  icon.height = `10`;
  icon.src = `https://img.icons8.com/ios-filled/10/sort-down.png`;
  icon.alt = `sort-down`;
  sigungu.appendChild(icon);
}

// 선택한 시·군·구 이름 출력 + 주소 추출
function showSi() {
  document.querySelectorAll(".list-si").forEach((ads) => {
    ads.addEventListener("click", function (event) {
      if (event.target.classList.contains("li-si-btn")) {
        event.preventDefault();

        clickedSiText = event.target.textContent;
        document.querySelector(".sigungu").textContent = "";

        document
          .querySelector(".sigungu")
          .appendChild(document.createTextNode(clickedSiText + " "));

        const icon = document.createElement("img");
        icon.classList.add("img");
        icon.width = `10`;
        icon.height = `10`;
        icon.src = `https://img.icons8.com/ios-filled/10/sort-down.png`;
        icon.alt = `sort-down`;
        document.querySelector(".sigungu").appendChild(icon);
        document.querySelector(".layer-si").classList.add("hide");
        document.querySelector(".list-li-center").classList.remove("unfold");
      }
      weatherLoad();
    });
  });
}

// 선택한 주소의 날씨 로드
async function weatherLoad() {
  let wd = await import("./weatherLoad.js");
  let lat = null;
  let lon = null;

  await fetch(
    `https://maps.googleapis.com/maps/api/geocode/json?address=대한민국+${clickedText}+${clickedSiText}&key=AIzaSyAH3J5S71gGtsQUQ-ABAoLmHQZ2kaEA88g`
  )
    .then((res) => res.json())
    .then((data) => {
      lat = data.results[0].geometry.location.lat;
      lon = data.results[0].geometry.location.lng;
    });

  fetch(`/api/weather/current?lat=${lat}&lon=${lon}`)
    .then((res) => res.json())
    .then((weatherData) => {
      wd.weatherLoads(weatherData);
    });
}

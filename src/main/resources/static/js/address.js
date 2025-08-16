const adList = [];
const allAdList = [];

let clickedText = "";
let clickedSiText = "";
let dosis = "";

// 현위치 버튼
function currentPos() {
  location.reload();
}

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

        const span = document.createElement("span");
        span.className = "do-toggle-icon";
        const icon = document.createElement("i");
        icon.classList.add("bi");
        icon.classList.add("bi-caret-down-fill");
        document.querySelector(".dosi").appendChild(span);
        document.querySelector(".do-toggle-icon").appendChild(icon);
        document.querySelector(".layer-do").classList.add("hide");
        document.querySelector(".do-btn").classList.remove("unfold");

        document.querySelector(".sigungu").textContent = "";

        document
          .querySelector(".sigungu")
          .appendChild(document.createTextNode(addressArray[2] + " "));

        const spans = document.createElement("span");
        spans.className = "si-toggle-icon";
        const icons = document.createElement("i");
        icons.classList.add("bi");
        icons.classList.add("bi-caret-down-fill");
        document.querySelector(".sigungu").appendChild(spans);
        document.querySelector(".si-toggle-icon").appendChild(icons);
        document.querySelector(".layer-si").classList.add("hide");
        document.querySelector(".si-btn").classList.remove("unfold");

        document.querySelector(".si-btn").classList.remove("disabled");

        dosis = document.querySelector(".dosi").textContent;
      });
  });
});

// 도·시 리스트 토글
function doListToggle() {
  document.querySelector(".layer-do").classList.toggle("hide");
  document.querySelector(".do-btn").classList.toggle("unfold");

  // 리스트 탭 접힘 확인표시 변경
  const dosi = document.querySelector(".dosi");
  if (document.querySelector(".do-btn").classList.length == 2) {
    const icons = dosi.querySelector("i");
    icons.classList.remove("bi-caret-down-fill");
    icons.classList.add("bi-caret-up-fill");
  } else {
    const icons = dosi.querySelector("i");
    icons.classList.remove("bi-caret-up-fill");
    icons.classList.add("bi-caret-down-fill");
  }
}

// 시·군·구 리스트 토글
function siListToggle() {
  // 시·군·구 리스트 토글
  document.querySelector(".layer-si").classList.toggle("hide");
  document.querySelector(".si-btn").classList.toggle("unfold");

  // 리스트 탭 접힘 확인표시 변경
  const sigungu = document.querySelector(".sigungu");
  if (
    document.querySelector(".si-btn").classList.length == 2 &&
    !document.querySelector(".si-btn").classList.contains("disabled")
  ) {
    const icons = sigungu.querySelector("i");
    icons.classList.remove("bi-caret-down-fill");
    icons.classList.add("bi-caret-up-fill");
  } else if (document.querySelector(".si-btn").classList.length == 1) {
    const icons = sigungu.querySelector("i");
    icons.classList.remove("bi-caret-up-fill");
    icons.classList.add("bi-caret-down-fill");
  }
}

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
  result += `<i class="bi bi-x-lg"></i>`;
  result += `</a>`;

  document.querySelector(".list-do").innerHTML = result;

  // 도·시 리스트 토글
  doListToggle();

  // 시·군·구 리스트 열려있으면 닫기
  if (document.querySelector(".layer-si").classList.length == 1) {
    hideSiList();
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

        const span = document.createElement("span");
        span.className = "do-toggle-icon";
        const icons = document.createElement("i");
        icons.classList.add("bi");
        icons.classList.add("bi-caret-down-fill");
        document.querySelector(".dosi").appendChild(span);
        document.querySelector(".do-toggle-icon").appendChild(icons);
        document.querySelector(".layer-do").classList.add("hide");
        document.querySelector(".do-btn").classList.remove("unfold");

        if (document.querySelector(".sigungu").textContent != "시·군·구") {
          document.querySelector(".sigungu").textContent = "";

          document
            .querySelector(".sigungu")
            .appendChild(document.createTextNode("시·군·구 "));

          const span = document.createElement("span");
          span.className = "si-toggle-icon";
          const icons = document.createElement("i");
          icons.classList.add("bi");
          icons.classList.add("bi-caret-down-fill");
          document.querySelector(".sigungu").appendChild(span);
          document.querySelector(".si-toggle-icon").appendChild(icons);

          if (document.querySelector(".layer-si").classList.length == 1) {
            document.querySelector(".layer-si").classList.add("hide");
            document.querySelector(".si-btn").classList.remove("unfold");
          }
        }

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
  document.querySelector(".do-btn").classList.remove("unfold");

  const dosi = document.querySelector(".dosi");
  const icons = dosi.querySelector("i");
  icons.classList.remove("bi-caret-up-fill");
  icons.classList.add("bi-caret-down-fill");
  dosi.appendChild(icons);
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
  result += `<i class="bi bi-x-lg"></i>`;
  result += `</a>`;

  document.querySelector(".list-si").innerHTML = result;

  // 시·군·구 리스트 토글
  siListToggle();

  // 도·시 리스트 닫기
  if (document.querySelector(".layer-do").classList.length == 1) {
    hideDoList();
  }
}

// 시·군·구 리스트 닫기
function hideSiList() {
  document.querySelector(".layer-si").classList.add("hide");
  document.querySelector(".si-btn").classList.toggle("unfold");

  const sigungu = document.querySelector(".sigungu");
  const icons = sigungu.querySelector("i");
  icons.classList.remove("bi-caret-up-fill");
  icons.classList.add("bi-caret-down-fill");
  sigungu.appendChild(icons);
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

        const span = document.createElement("span");
        span.className = "si-toggle-icon";
        const icons = document.createElement("i");
        icons.classList.add("bi");
        icons.classList.add("bi-caret-down-fill");
        document.querySelector(".sigungu").appendChild(span);
        document.querySelector(".si-toggle-icon").appendChild(icons);
        document.querySelector(".layer-si").classList.add("hide");
        document.querySelector(".si-btn").classList.remove("unfold");
      }
      weatherLoad();
    });
  });
}
// ─── 날씨 로딩 스피너 제어 ───
function showWeatherLoading() {
  document.getElementById("weather-loading-overlay").style.display = "block";
  document.getElementById("weather-loading-spinner").style.display = "block";
  document.getElementById("weather-loading-text").style.display = "block";
}
function hideWeatherLoading() {
  document.getElementById("weather-loading-overlay").style.display = "none";
  document.getElementById("weather-loading-spinner").style.display = "none";
  document.getElementById("weather-loading-text").style.display = "none";
}

// 선택한 주소의 날씨 로드
async function weatherLoad() {
  let wd = await import("./weatherLoad.js");
  let lat = null;
  let lon = null;

  showWeatherLoading();
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
      const weather = weatherData[0];

      const tempNum = weather.tmp != null ? parseInt(weather.tmp, 10) : null;
      if (tempNum != null) {
        lastTempNum = tempNum;
        updateCurrentTempTag(tempNum);
      }
      hideWeatherLoading();
    })
    .catch(() => {
      document.getElementById("weather-temp").textContent =
        "날씨 정보를 불러오지 못했습니다.";
    });
}

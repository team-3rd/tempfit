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

// 페이지 리로드 함수
function refresh() {
  location.reload();
}

const ul = document.querySelector(".ul");

// 시간별 날씨 스크롤 버튼 작동 + 스크롤 끝까지 가면 한쪽 버튼 숨기기 함수
function nextBtn() {
  document.querySelector(".btn-prev").classList.remove("hide");
  ul.scrollBy({ left: 500, behavior: "smooth" });
  if (ul.scrollLeft == 2450 || ul.scrollLeft == 2500) {
    document.querySelector(".btn-next").classList.add("hide");
  } else {
    document.querySelector(".btn-next").classList.remove("hide");
  }
}

function prevBtn() {
  document.querySelector(".btn-next").classList.remove("hide");
  ul.scrollBy({ left: -500, behavior: "smooth" });
  if (ul.scrollLeft == 450 || ul.scrollLeft == 500) {
    document.querySelector(".btn-prev").classList.add("hide");
  } else {
    document.querySelector(".btn-prev").classList.remove("hide");
  }
}

// 날씨 불러오기
window.addEventListener("DOMContentLoaded", async () => {
  if (!navigator.geolocation) {
    document.getElementById("weather-temp").textContent = "브라우저가 위치정보를 지원하지 않습니다.";
    return;
  }

  let wd = await import("./weatherLoad.js");
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      const lat = pos.coords.latitude;
      const lon = pos.coords.longitude;

      showWeatherLoading();
      fetch(`/api/weather/current?lat=${lat}&lon=${lon}`)
        .then((res) => res.json())
        .then((weatherData) => {
          wd.weatherLoads(weatherData);
          // 날씨 카드 DOM 업데이트
          const weather = weatherData[0];

          // 온도 정수로 변환
          const tempNum =
            weather.tmp != null ? parseInt(weather.tmp, 10) : null;
          if (tempNum != null) {
            lastTempNum = tempNum;
            updateCurrentTempTag(tempNum);
          }

          // 날씨 로드 완료 이벤트 발생
          hideWeatherLoading();
          window.dispatchEvent(new CustomEvent("weatherLoaded", { detail: { tempNum } }));
        })
        .catch(() => {
          document.getElementById("weather-temp").textContent = "날씨 정보를 불러오지 못했습니다";
          hideWeatherLoading();
        });
    },
    () => {
      document.getElementById("weather-temp").textContent = "위치 권한을 허용해 주세요!";
    }
  );
});

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
    document.getElementById("weather-temp").textContent =
      "브라우저가 위치정보를 지원하지 않습니다.";
    return;
  }

  let wd = await import("./weatherLoad.js");
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      const lat = pos.coords.latitude;
      const lon = pos.coords.longitude;

      fetch(`/api/weather/current?lat=${lat}&lon=${lon}`)
        .then((res) => res.json())
        .then((weatherData) => {
          wd.weatherLoads(weatherData);
          // 날씨 카드 DOM 업데이트
          const weather = weatherData[0];

          // 온도 정수로 변환
          const tempNum =
            weather.tmp != null ? parseInt(weather.tmp, 10) : null;
          const tagElem = document.getElementById("current-temp-tag");
          if (tagElem) {
            tagElem.innerHTML =
              tempNum != null
                ? `‘🌡현재 온도 기준(<b>${tempNum}℃</b>)’`
                : "‘🌡현재 온도 기준(-℃)’";
          }

          // 날씨 로드 완료 이벤트 발생
          window.dispatchEvent(
            new CustomEvent("weatherLoaded", { detail: { tempNum } })
          );
        })
        .catch(() => {
          document.getElementById("weather-temp").textContent =
            "날씨 정보를 가져오지 못했습니다";
        });
    },
    () => {
      document.getElementById("weather-temp").textContent =
        "위치 권한을 허용해 주세요!";
    }
  );
});

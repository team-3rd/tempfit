// src/main/resources/static/js/weather.js
window.addEventListener("DOMContentLoaded", () => {
  if (!navigator.geolocation) {
    document.getElementById("weather-temp").textContent =
      "브라우저가 위치정보를 지원하지 않습니다.";
    return;
  }

  navigator.geolocation.getCurrentPosition(
    (pos) => {
      const lat = pos.coords.latitude;
      const lon = pos.coords.longitude;

      fetch(`/api/weather/current?lat=${lat}&lon=${lon}`)
        .then(res => res.json())
        .then(data => {
          // 날씨 카드 DOM 업데이트
          document.getElementById("weather-sky").textContent    = data.sky    || "-";
          document.getElementById("weather-desc").textContent   = data.rainType || "-";
          document.getElementById("weather-temp").textContent   =
            data.temp != null ? `${data.temp}` : "-℃";
          document.getElementById("weather-date").textContent   = data.fcstDate || "-";
          document.getElementById("weather-time").textContent   = data.fcstTime || "-";
          document.getElementById("weather-location").textContent = data.location || "";

          // 온도 정수로 변환
          const tempNum = data.temp != null ? parseInt(data.temp, 10) : null;
          const tagElem = document.getElementById("current-temp-tag");
          if (tagElem) {
            tagElem.innerHTML = tempNum != null
              ? `‘현재 온도 기준(<b>${tempNum}℃</b>)’`
              : "‘현재 온도 기준(-℃)’";
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

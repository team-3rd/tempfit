// 위치 + 날씨 + 추천 JS
window.addEventListener("DOMContentLoaded", function () {
  // 1. 위치정보 받아오기
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      function (pos) {
        const lat = pos.coords.latitude;
        const lon = pos.coords.longitude;

        // 2. 날씨 API 호출 (온도 받아오기)
        fetch(`/api/weather/current?lat=${lat}&lon=${lon}`)
          .then((res) => res.json())
          .then((weatherData) => {
            document.getElementById("weather-sky").textContent = weatherData.sky || "-";
            document.getElementById("weather-desc").textContent = weatherData.rainType || "-";
            document.getElementById("weather-temp").textContent =
              weatherData.temp !== undefined ? weatherData.temp : "-";
            document.getElementById("weather-date").textContent = weatherData.fcstDate || "-";
            document.getElementById("weather-time").textContent = weatherData.fcstTime || "-";
            document.getElementById("weather-location").textContent = weatherData.location || "";

            // tempNum은 반드시 정수! parseInt로 묶어줌
            // '현재 온도 기준(-℃)' 스타일별 BEST LOOKS의 (-℃)에 들어갈 온도
            let tempNum = weatherData.temp !== undefined ? parseInt(weatherData.temp) : "-";
            var tagElem = document.getElementById("current-temp-tag");
            if (tagElem && tempNum !== "-") {
              tagElem.innerHTML = `‘현재 온도 기준(<b>${tempNum}℃</b>)’`;
            } else if (tagElem) {
              tagElem.innerHTML = "‘현재 온도 기준(-℃)’";
            }

            // 3. 온도로 남/여 코디 추천 API 호출
            fetch(`/api/coordi/guide?temp=${tempNum}`)
              .then((res) => res.json())
              .then((data) => {
                renderGuide("clothing-guide-male", data.male);
                renderGuide("clothing-guide-female", data.female);
              })
              .catch(() => {
                document.getElementById("clothing-guide-male").innerText = "추천 코디 정보를 가져오지 못했습니다";
                document.getElementById("clothing-guide-female").innerText = "추천 코디 정보를 가져오지 못했습니다";
              });
          })
          .catch(() => {
            document.getElementById("weather-temp").textContent = "날씨 정보를 가져오지 못했습니다";
          });
      },
      function (error) {
        document.getElementById("weather-temp").textContent = "위치 권한을 허용해 주세요!";
      }
    );
  } else {
    document.getElementById("weather-temp").textContent = "브라우저가 위치정보를 지원하지 않습니다.";
  }

  // 공통 렌더 함수
  function renderGuide(divId, data) {
    const guideDiv = document.getElementById(divId);
    guideDiv.innerHTML = "";
    ["top", "bottom", "shoes"].forEach((part) => {
      if (data[part]) {
        guideDiv.innerHTML += `
          <div style="text-align:center;">
            <img src="${data[part].imageUrl}" alt="${data[part].name}" width="150" height="150"
              style="border-radius:10px; border:1px solid #ddd; background:#fafafa;"/><br/>
            <b>${part === "top" ? "상의" : part === "bottom" ? "하의" : "신발"}</b><br/>
            <span style="font-size:18px;">${data[part].name}</span>
          </div>
        `;
      }
    });
  }
});
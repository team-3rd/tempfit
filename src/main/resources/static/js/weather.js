// 페이지 리로드 함수
function refresh() {
  location.reload();
}

const ul = document.querySelector(".ul");

// 시간별 날씨 스크롤 버튼 작동 + 스크롤 끝까지 가면 한쪽 버튼 숨기기 함수
function nextBtn() {
  document.querySelector(".btn-prev").classList.remove("hide");
  ul.scrollBy({ left: 500, behavior: "smooth" });
  if (ul.scrollLeft == 3000) {
    document.querySelector(".btn-next").classList.add("hide");
  } else {
    document.querySelector(".btn-next").classList.remove("hide");
  }
}

function prevBtn() {
  document.querySelector(".btn-next").classList.remove("hide");
  ul.scrollBy({ left: -500, behavior: "smooth" });
  if (ul.scrollLeft == 500) {
    document.querySelector(".btn-prev").classList.add("hide");
  } else {
    document.querySelector(".btn-prev").classList.remove("hide");
  }
}

// 날씨 불러오기
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

      fetch(
        `https://geocode.googleapis.com/v4beta/geocode/location/${lat},${lon}?key=AIzaSyAH3J5S71gGtsQUQ-ABAoLmHQZ2kaEA88g`
      )
        .then((res) => res.json())
        .then((loc) => {
          const location = loc.results[6].formattedAddress;
          const address = location.split(" ");

          let locs = "";
          locs += `<div class="figure">`;
          locs += `<span class="h3">${address[1] + " " + address[2]}</span>`;
          locs += `</div>`;
          locs += `<div class="figure offset-1">`;
          locs += `<button
          class="btn btn-lg btn-light fw-bold border-white bg-white offset-1"
          onclick="refresh()"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="32"
            height="32"
            fill="currentColor"
            class="bi bi-crosshair"
            viewBox="0 0 16 16"
          >
            <path
              d="M8.5.5a.5.5 0 0 0-1 0v.518A7 7 0 0 0 1.018 7.5H.5a.5.5 0 0 0 0 1h.518A7 7 0 0 0 7.5 14.982v.518a.5.5 0 0 0 1 0v-.518A7 7 0 0 0 14.982 8.5h.518a.5.5 0 0 0 0-1h-.518A7 7 0 0 0 8.5 1.018zm-6.48 7A6 6 0 0 1 7.5 2.02v.48a.5.5 0 0 0 1 0v-.48a6 6 0 0 1 5.48 5.48h-.48a.5.5 0 0 0 0 1h.48a6 6 0 0 1-5.48 5.48v-.48a.5.5 0 0 0-1 0v.48A6 6 0 0 1 2.02 8.5h.48a.5.5 0 0 0 0-1zM8 10a2 2 0 1 0 0-4 2 2 0 0 0 0 4"
            />
          </svg>
        </button>`;
          locs += `</div>`;
          document.querySelector("#weather-location").innerHTML = locs;
        });

      fetch(`/api/weather/current?lat=${lat}&lon=${lon}`)
        .then((res) => res.json())
        .then((weatherData) => {
          // 날씨 카드 DOM 업데이트
          const weather = weatherData[0];
          let icon = "";

          if (weather.sky == "맑음") {
            icon += `<svg width="120" height="120" viewBox="0 0 394 380" fill="none" xmlns="http://www.w3.org/2000/svg">
                <g filter="url(#filter0_f_5_102)">
                <rect x="77" y="77" width="240" height="226" rx="94" fill="#FFEF9A"/>
                </g>
                <g filter="url(#filter1_i_5_102)">
                <path d="M303 190C303 249.094 255.542 297 197 297C138.458 297 91 249.094 91 190C91 130.906 138.458 83 197 83C255.542 83 303 130.906 303 190Z" fill="url(#paint0_linear_5_102)"/>
                </g>
                <defs>
                <filter id="filter0_f_5_102" x="0" y="0" width="394" height="380" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                <feBlend mode="normal" in="SourceGraphic" in2="BackgroundImageFix" result="shape"/>
                <feGaussianBlur stdDeviation="38.5" result="effect1_foregroundBlur_5_102"/>
                </filter>
                <filter id="filter1_i_5_102" x="91" y="83" width="212" height="219" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                <feBlend mode="normal" in="SourceGraphic" in2="BackgroundImageFix" result="shape"/>
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                <feOffset dy="5"/>
                <feGaussianBlur stdDeviation="9"/>
                <feComposite in2="hardAlpha" operator="arithmetic" k2="-1" k3="1"/>
                <feColorMatrix type="matrix" values="0 0 0 0 1 0 0 0 0 1 0 0 0 0 1 0 0 0 0.81 0"/>
                <feBlend mode="normal" in2="shape" result="effect1_innerShadow_5_102"/>
                </filter>
                <linearGradient id="paint0_linear_5_102" x1="180.899" y1="248.241" x2="269.356" y2="94.5502" gradientUnits="userSpaceOnUse">
                <stop stop-color="#FF9900"/>
                <stop offset="1" stop-color="#FFEE94"/>
                </linearGradient>
                </defs>
                </svg>`;
          } else if (weather.sky == "구름 많음") {
            icon += `<svg width="120" height="96" viewBox="0 0 394 380" fill="none" xmlns="http://www.w3.org/2000/svg">
                <g filter="url(#filter0_f_8_7)">
                <rect x="77" y="77" width="240" height="226" rx="94" fill="#FFEF9A"/>
                </g>
                <g filter="url(#filter1_i_8_7)">
                <path d="M301 193C301 250.438 254.438 297 197 297C139.562 297 93 250.438 93 193C93 135.562 139.562 89 197 89C254.438 89 301 135.562 301 193Z" fill="url(#paint0_linear_8_7)"/>
                </g>
                <g filter="url(#filter3_i_8_7)">
                <path d="M256.326 246.629C257.049 242.524 257.426 238.302 257.426 233.993C257.426 193.68 224.441 161 183.752 161C153.503 161 127.512 179.061 116.166 204.893C106.974 197.03 95.0468 192.283 82.0124 192.283C52.949 192.283 29.3884 215.885 29.3884 245C29.3884 246.621 29.4615 248.225 29.6045 249.809C15.6255 256.593 6 270.828 6 287.29C6 310.326 24.8484 329 48.0992 329H246.901C270.152 329 289 310.326 289 287.29C289 267.464 275.039 250.869 256.326 246.629Z" fill="url(#paint1_linear_8_7)"/>
                </g>
                <defs>
                <filter id="filter0_f_8_7" x="0" y="0" width="394" height="380" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                <feBlend mode="normal" in="SourceGraphic" in2="BackgroundImageFix" result="shape"/>
                <feGaussianBlur stdDeviation="38.5" result="effect1_foregroundBlur_8_7"/>
                </filter>
                <filter id="filter1_i_8_7" x="93" y="89" width="208" height="213" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                <feBlend mode="normal" in="SourceGraphic" in2="BackgroundImageFix" result="shape"/>
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                <feOffset dy="5"/>
                <feGaussianBlur stdDeviation="9"/>
                <feComposite in2="hardAlpha" operator="arithmetic" k2="-1" k3="1"/>
                <feColorMatrix type="matrix" values="0 0 0 0 1 0 0 0 0 1 0 0 0 0 1 0 0 0 0.81 0"/>
                <feBlend mode="normal" in2="shape" result="effect1_innerShadow_8_7"/>
                </filter>
                <filter id="filter3_i_8_7" x="6" y="161" width="283" height="178" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                <feBlend mode="normal" in="SourceGraphic" in2="BackgroundImageFix" result="shape"/>
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                <feOffset dy="11"/>
                <feGaussianBlur stdDeviation="5"/>
                <feComposite in2="hardAlpha" operator="arithmetic" k2="-1" k3="1"/>
                <feColorMatrix type="matrix" values="0 0 0 0 1 0 0 0 0 1 0 0 0 0 1 0 0 0 1 0"/>
                <feBlend mode="normal" in2="shape" result="effect1_innerShadow_8_7"/>
                </filter>
                <linearGradient id="paint0_linear_8_7" x1="181.203" y1="249.608" x2="266.772" y2="99.5317" gradientUnits="userSpaceOnUse">
                <stop stop-color="#FF9900"/>
                <stop offset="1" stop-color="#FFEE94"/>
                </linearGradient>
                <linearGradient id="paint1_linear_8_7" x1="24" y1="314" x2="309.5" y2="100.5" gradientUnits="userSpaceOnUse">
                <stop stop-color="#EAEAEA"/>
                <stop offset="1" stop-color="white" stop-opacity="0.58"/>
                </linearGradient>
                </defs>
                </svg>`;
          } else if (weather.sky == "흐림" && weather.pty == "강수없음") {
            icon += `<svg width="120" height="96" viewBox="0 0 314 187" fill="none" xmlns="http://www.w3.org/2000/svg">
                <g filter="url(#filter1_i_5_31)">
                <path d="M277.747 95.0084C278.549 90.4546 278.967 85.7701 278.967 80.9888C278.967 36.2599 242.369 0 197.223 0C163.661 0 134.823 20.0397 122.234 48.7007C112.035 39.9768 98.801 34.7095 84.3389 34.7095C52.0918 34.7095 25.9504 60.8972 25.9504 93.2014C25.9504 95.0003 26.0315 96.7801 26.1902 98.5376C10.6799 106.065 3.05176e-05 121.858 3.05176e-05 140.124C3.05176e-05 165.683 20.9131 186.403 46.7108 186.403H267.289C293.087 186.403 314 165.683 314 140.124C314 118.126 298.51 99.7132 277.747 95.0084Z" fill="url(#paint0_linear_5_31)"/>
                </g>
                <defs>
                <filter id="filter1_i_5_31" x="0" y="0" width="314" height="196.403" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                <feBlend mode="normal" in="SourceGraphic" in2="BackgroundImageFix" result="shape"/>
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                <feOffset dy="11"/>
                <feGaussianBlur stdDeviation="5"/>
                <feComposite in2="hardAlpha" operator="arithmetic" k2="-1" k3="1"/>
                <feColorMatrix type="matrix" values="0 0 0 0 1 0 0 0 0 1 0 0 0 0 1 0 0 0 1 0"/>
                <feBlend mode="normal" in2="shape" result="effect1_innerShadow_5_31"/>
                </filter>
                <linearGradient id="paint0_linear_5_31" x1="19.9717" y1="169.76" x2="336.746" y2="-67.1272" gradientUnits="userSpaceOnUse">
                <stop stop-color="white"/>
                <stop offset="1" stop-color="#D5D5D5" stop-opacity="0.58"/>
                </linearGradient>
                </defs>
                </svg>`;
          } else if (
            (weather.sky == "흐림" && weather.pty == "비") ||
            weather.pty == "이슬비"
          ) {
            icon += `<svg width="120" height="96" viewBox="0 0 396 312" fill="none" xmlns="http://www.w3.org/2000/svg">
                <g filter="url(#filter1_i_8_9)">
                <path d="M318.747 95.0084C319.549 90.4546 319.967 85.7701 319.967 80.9888C319.967 36.2599 283.369 0 238.223 0C204.661 0 175.823 20.0397 163.234 48.7007C153.035 39.9768 139.801 34.7095 125.339 34.7095C93.0918 34.7095 66.9504 60.8972 66.9504 93.2014C66.9504 95.0003 67.0315 96.7801 67.1902 98.5376C51.6799 106.065 41 121.858 41 140.124C41 165.683 61.9131 186.403 87.7108 186.403H308.289C334.087 186.403 355 165.683 355 140.124C355 118.126 339.51 99.7132 318.747 95.0084Z" fill="url(#paint0_linear_8_9)"/>
                </g>
                <g filter="url(#filter2_di_8_9)">
                <path d="M125.5 136C107.012 160.333 81.1281 209 125.5 209C169.872 209 143.988 160.333 125.5 136Z" fill="#00BCFF"/>
                </g>
                <g filter="url(#filter3_di_8_9)">
                <path d="M269.5 136C251.012 160.333 225.128 209 269.5 209C313.872 209 287.988 160.333 269.5 136Z" fill="#00BCFF"/>
                </g>
                <g filter="url(#filter4_di_8_9)">
                <path d="M200.5 200C182.012 224.333 156.128 273 200.5 273C244.872 273 218.988 224.333 200.5 200Z" fill="#00BCFF"/>
                </g>
                <g filter="url(#filter5_f_8_9)">
                <rect x="78" y="192" width="192" height="29" rx="17.5" fill="#00BCFF"/>
                </g>
                <defs>
                <filter id="filter1_i_8_9" x="41" y="0" width="314" height="196.403" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                <feBlend mode="normal" in="SourceGraphic" in2="BackgroundImageFix" result="shape"/>
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                <feOffset dy="11"/>
                <feGaussianBlur stdDeviation="5"/>
                <feComposite in2="hardAlpha" operator="arithmetic" k2="-1" k3="1"/>
                <feColorMatrix type="matrix" values="0 0 0 0 1 0 0 0 0 1 0 0 0 0 1 0 0 0 1 0"/>
                <feBlend mode="normal" in2="shape" result="effect1_innerShadow_8_9"/>
                </filter>
                <filter id="filter2_di_8_9" x="82" y="136" width="87" height="112" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                <feOffset dy="20"/>
                <feGaussianBlur stdDeviation="9.5"/>
                <feComposite in2="hardAlpha" operator="out"/>
                <feColorMatrix type="matrix" values="0 0 0 0 0.283785 0 0 0 0 0.178889 0 0 0 0 0.933333 0 0 0 1 0"/>
                <feBlend mode="normal" in2="BackgroundImageFix" result="effect1_dropShadow_8_9"/>
                <feBlend mode="normal" in="SourceGraphic" in2="effect1_dropShadow_8_9" result="shape"/>
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                <feOffset dy="9"/>
                <feGaussianBlur stdDeviation="1.5"/>
                <feComposite in2="hardAlpha" operator="arithmetic" k2="-1" k3="1"/>
                <feColorMatrix type="matrix" values="0 0 0 0 1 0 0 0 0 1 0 0 0 0 1 0 0 0 0.21 0"/>
                <feBlend mode="normal" in2="shape" result="effect2_innerShadow_8_9"/>
                </filter>
                <filter id="filter3_di_8_9" x="226" y="136" width="87" height="112" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                <feOffset dy="20"/>
                <feGaussianBlur stdDeviation="9.5"/>
                <feComposite in2="hardAlpha" operator="out"/>
                <feColorMatrix type="matrix" values="0 0 0 0 0.283785 0 0 0 0 0.178889 0 0 0 0 0.933333 0 0 0 1 0"/>
                <feBlend mode="normal" in2="BackgroundImageFix" result="effect1_dropShadow_8_9"/>
                <feBlend mode="normal" in="SourceGraphic" in2="effect1_dropShadow_8_9" result="shape"/>
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                <feOffset dy="9"/>
                <feGaussianBlur stdDeviation="1.5"/>
                <feComposite in2="hardAlpha" operator="arithmetic" k2="-1" k3="1"/>
                <feColorMatrix type="matrix" values="0 0 0 0 1 0 0 0 0 1 0 0 0 0 1 0 0 0 0.21 0"/>
                <feBlend mode="normal" in2="shape" result="effect2_innerShadow_8_9"/>
                </filter>
                <filter id="filter4_di_8_9" x="157" y="200" width="87" height="112" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                <feOffset dy="20"/>
                <feGaussianBlur stdDeviation="9.5"/>
                <feComposite in2="hardAlpha" operator="out"/>
                <feColorMatrix type="matrix" values="0 0 0 0 0.283785 0 0 0 0 0.178889 0 0 0 0 0.933333 0 0 0 1 0"/>
                <feBlend mode="normal" in2="BackgroundImageFix" result="effect1_dropShadow_8_9"/>
                <feBlend mode="normal" in="SourceGraphic" in2="effect1_dropShadow_8_9" result="shape"/>
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                <feOffset dy="9"/>
                <feGaussianBlur stdDeviation="1.5"/>
                <feComposite in2="hardAlpha" operator="arithmetic" k2="-1" k3="1"/>
                <feColorMatrix type="matrix" values="0 0 0 0 1 0 0 0 0 1 0 0 0 0 1 0 0 0 0.21 0"/>
                <feBlend mode="normal" in2="shape" result="effect2_innerShadow_8_9"/>
                </filter>
                <filter id="filter5_f_8_9" x="0" y="114" width="396" height="191" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                <feBlend mode="normal" in="SourceGraphic" in2="BackgroundImageFix" result="shape"/>
                <feGaussianBlur stdDeviation="39" result="effect1_foregroundBlur_8_9"/>
                </filter>
                <linearGradient id="paint0_linear_8_9" x1="60.9717" y1="169.76" x2="377.746" y2="-67.1272" gradientUnits="userSpaceOnUse">
                <stop stop-color="#D5D5D5"/>
                <stop offset="1" stop-color="white" stop-opacity="0.58"/>
                </linearGradient>
                </defs>
                </svg>`;
          }
          document.querySelector("#weather-icon").innerHTML = icon;

          document.getElementById("weather-temp").textContent =
            weather.tmp !== undefined ? weather.tmp + "℃" : "-";

          if (weather.sky == "맑음") {
            document.getElementById("weather-sky").textContent = "맑음";
          } else if (weather.sky == "구름 많음") {
            document.getElementById("weather-sky").textContent = "구름 많음";
          } else if (weather.sky == "흐림" && weather.pty == "강수없음") {
            document.getElementById("weather-sky").textContent = "흐림";
          } else if (weather.sky == "흐림" && weather.pty == "이슬비") {
            document.getElementById("weather-sky").textContent =
              "흐리고 약한 비";
          } else if (weather.sky == "흐림" && weather.pty == "비") {
            document.getElementById("weather-sky").textContent = "흐리고 비";
          }

          document.querySelector(
            "#weather-rains"
          ).innerHTML = `<span>강수확률 </span>${weather.pop + "%"}`;

          document.querySelector(
            "#weather-humid"
          ).innerHTML = `<span>습도 </span>${weather.reh}`;

          document.querySelector(
            "#weather-wind"
          ).innerHTML = `<span>풍속 </span>${weather.wsd}`;

          // 시간대별 온도
          let result = "";

          weatherData.forEach((weathers) => {
            const time = weathers.fcstTime.slice(0, 2);

            result += `<li class="li">`;
            result += `<dl class="weather-content">`;
            result += `<dt class="time">`;
            result += `<p>${time + "시"}</p></dt>`;
            result += `<dd class="icon-box">`;

            if (weathers.sky == "맑음") {
              result += `<svg width="32" height="32" viewBox="0 0 394 380" fill="none" xmlns="http://www.w3.org/2000/svg">
                <g filter="url(#filter0_f_5_102)">
                <rect x="77" y="77" width="240" height="226" rx="94" fill="#FFEF9A"/>
                </g>
                <g filter="url(#filter1_i_5_102)">
                <path d="M303 190C303 249.094 255.542 297 197 297C138.458 297 91 249.094 91 190C91 130.906 138.458 83 197 83C255.542 83 303 130.906 303 190Z" fill="url(#paint0_linear_5_102)"/>
                </g>
                <defs>
                <filter id="filter0_f_5_102" x="0" y="0" width="394" height="380" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                <feBlend mode="normal" in="SourceGraphic" in2="BackgroundImageFix" result="shape"/>
                <feGaussianBlur stdDeviation="38.5" result="effect1_foregroundBlur_5_102"/>
                </filter>
                <filter id="filter1_i_5_102" x="91" y="83" width="212" height="219" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                <feBlend mode="normal" in="SourceGraphic" in2="BackgroundImageFix" result="shape"/>
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                <feOffset dy="5"/>
                <feGaussianBlur stdDeviation="9"/>
                <feComposite in2="hardAlpha" operator="arithmetic" k2="-1" k3="1"/>
                <feColorMatrix type="matrix" values="0 0 0 0 1 0 0 0 0 1 0 0 0 0 1 0 0 0 0.81 0"/>
                <feBlend mode="normal" in2="shape" result="effect1_innerShadow_5_102"/>
                </filter>
                <linearGradient id="paint0_linear_5_102" x1="180.899" y1="248.241" x2="269.356" y2="94.5502" gradientUnits="userSpaceOnUse">
                <stop stop-color="#FF9900"/>
                <stop offset="1" stop-color="#FFEE94"/>
                </linearGradient>
                </defs>
                </svg>`;
            } else if (weathers.sky == "구름 많음") {
              result += `<svg width="32" height="28" viewBox="0 0 394 380" fill="none" xmlns="http://www.w3.org/2000/svg">
                <g filter="url(#filter0_f_8_7)">
                <rect x="77" y="77" width="240" height="226" rx="94" fill="#FFEF9A"/>
                </g>
                <g filter="url(#filter1_i_8_7)">
                <path d="M301 193C301 250.438 254.438 297 197 297C139.562 297 93 250.438 93 193C93 135.562 139.562 89 197 89C254.438 89 301 135.562 301 193Z" fill="url(#paint0_linear_8_7)"/>
                </g>
                <g filter="url(#filter3_i_8_7)">
                <path d="M256.326 246.629C257.049 242.524 257.426 238.302 257.426 233.993C257.426 193.68 224.441 161 183.752 161C153.503 161 127.512 179.061 116.166 204.893C106.974 197.03 95.0468 192.283 82.0124 192.283C52.949 192.283 29.3884 215.885 29.3884 245C29.3884 246.621 29.4615 248.225 29.6045 249.809C15.6255 256.593 6 270.828 6 287.29C6 310.326 24.8484 329 48.0992 329H246.901C270.152 329 289 310.326 289 287.29C289 267.464 275.039 250.869 256.326 246.629Z" fill="url(#paint1_linear_8_7)"/>
                </g>
                <defs>
                <filter id="filter0_f_8_7" x="0" y="0" width="394" height="380" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                <feBlend mode="normal" in="SourceGraphic" in2="BackgroundImageFix" result="shape"/>
                <feGaussianBlur stdDeviation="38.5" result="effect1_foregroundBlur_8_7"/>
                </filter>
                <filter id="filter1_i_8_7" x="93" y="89" width="208" height="213" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                <feBlend mode="normal" in="SourceGraphic" in2="BackgroundImageFix" result="shape"/>
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                <feOffset dy="5"/>
                <feGaussianBlur stdDeviation="9"/>
                <feComposite in2="hardAlpha" operator="arithmetic" k2="-1" k3="1"/>
                <feColorMatrix type="matrix" values="0 0 0 0 1 0 0 0 0 1 0 0 0 0 1 0 0 0 0.81 0"/>
                <feBlend mode="normal" in2="shape" result="effect1_innerShadow_8_7"/>
                </filter>
                <filter id="filter3_i_8_7" x="6" y="161" width="283" height="178" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                <feBlend mode="normal" in="SourceGraphic" in2="BackgroundImageFix" result="shape"/>
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                <feOffset dy="11"/>
                <feGaussianBlur stdDeviation="5"/>
                <feComposite in2="hardAlpha" operator="arithmetic" k2="-1" k3="1"/>
                <feColorMatrix type="matrix" values="0 0 0 0 1 0 0 0 0 1 0 0 0 0 1 0 0 0 1 0"/>
                <feBlend mode="normal" in2="shape" result="effect1_innerShadow_8_7"/>
                </filter>
                <linearGradient id="paint0_linear_8_7" x1="181.203" y1="249.608" x2="266.772" y2="99.5317" gradientUnits="userSpaceOnUse">
                <stop stop-color="#FF9900"/>
                <stop offset="1" stop-color="#FFEE94"/>
                </linearGradient>
                <linearGradient id="paint1_linear_8_7" x1="24" y1="314" x2="309.5" y2="100.5" gradientUnits="userSpaceOnUse">
                <stop stop-color="#EAEAEA"/>
                <stop offset="1" stop-color="white" stop-opacity="0.58"/>
                </linearGradient>
                </defs>
                </svg>`;
            } else if (weathers.sky == "흐림" && weathers.pty == "강수없음") {
              result += `<svg width="32" height="28" viewBox="0 0 314 187" fill="none" xmlns="http://www.w3.org/2000/svg">
                <g filter="url(#filter1_i_5_31)">
                <path d="M277.747 95.0084C278.549 90.4546 278.967 85.7701 278.967 80.9888C278.967 36.2599 242.369 0 197.223 0C163.661 0 134.823 20.0397 122.234 48.7007C112.035 39.9768 98.801 34.7095 84.3389 34.7095C52.0918 34.7095 25.9504 60.8972 25.9504 93.2014C25.9504 95.0003 26.0315 96.7801 26.1902 98.5376C10.6799 106.065 3.05176e-05 121.858 3.05176e-05 140.124C3.05176e-05 165.683 20.9131 186.403 46.7108 186.403H267.289C293.087 186.403 314 165.683 314 140.124C314 118.126 298.51 99.7132 277.747 95.0084Z" fill="url(#paint0_linear_5_31)"/>
                </g>
                <defs>
                <filter id="filter1_i_5_31" x="0" y="0" width="314" height="196.403" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                <feBlend mode="normal" in="SourceGraphic" in2="BackgroundImageFix" result="shape"/>
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                <feOffset dy="11"/>
                <feGaussianBlur stdDeviation="5"/>
                <feComposite in2="hardAlpha" operator="arithmetic" k2="-1" k3="1"/>
                <feColorMatrix type="matrix" values="0 0 0 0 1 0 0 0 0 1 0 0 0 0 1 0 0 0 1 0"/>
                <feBlend mode="normal" in2="shape" result="effect1_innerShadow_5_31"/>
                </filter>
                <linearGradient id="paint0_linear_5_31" x1="19.9717" y1="169.76" x2="336.746" y2="-67.1272" gradientUnits="userSpaceOnUse">
                <stop stop-color="white"/>
                <stop offset="1" stop-color="#D5D5D5" stop-opacity="0.58"/>
                </linearGradient>
                </defs>
                </svg>`;
            } else if (
              (weathers.sky == "흐림" && weathers.pty == "비") ||
              weathers.pty == "이슬비"
            ) {
              result += `<svg width="32" height="28" viewBox="0 0 396 312" fill="none" xmlns="http://www.w3.org/2000/svg">
                <g filter="url(#filter1_i_8_9)">
                <path d="M318.747 95.0084C319.549 90.4546 319.967 85.7701 319.967 80.9888C319.967 36.2599 283.369 0 238.223 0C204.661 0 175.823 20.0397 163.234 48.7007C153.035 39.9768 139.801 34.7095 125.339 34.7095C93.0918 34.7095 66.9504 60.8972 66.9504 93.2014C66.9504 95.0003 67.0315 96.7801 67.1902 98.5376C51.6799 106.065 41 121.858 41 140.124C41 165.683 61.9131 186.403 87.7108 186.403H308.289C334.087 186.403 355 165.683 355 140.124C355 118.126 339.51 99.7132 318.747 95.0084Z" fill="url(#paint0_linear_8_9)"/>
                </g>
                <g filter="url(#filter2_di_8_9)">
                <path d="M125.5 136C107.012 160.333 81.1281 209 125.5 209C169.872 209 143.988 160.333 125.5 136Z" fill="#00BCFF"/>
                </g>
                <g filter="url(#filter3_di_8_9)">
                <path d="M269.5 136C251.012 160.333 225.128 209 269.5 209C313.872 209 287.988 160.333 269.5 136Z" fill="#00BCFF"/>
                </g>
                <g filter="url(#filter4_di_8_9)">
                <path d="M200.5 200C182.012 224.333 156.128 273 200.5 273C244.872 273 218.988 224.333 200.5 200Z" fill="#00BCFF"/>
                </g>
                <g filter="url(#filter5_f_8_9)">
                <rect x="78" y="192" width="192" height="29" rx="17.5" fill="#00BCFF"/>
                </g>
                <defs>
                <filter id="filter1_i_8_9" x="41" y="0" width="314" height="196.403" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                <feBlend mode="normal" in="SourceGraphic" in2="BackgroundImageFix" result="shape"/>
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                <feOffset dy="11"/>
                <feGaussianBlur stdDeviation="5"/>
                <feComposite in2="hardAlpha" operator="arithmetic" k2="-1" k3="1"/>
                <feColorMatrix type="matrix" values="0 0 0 0 1 0 0 0 0 1 0 0 0 0 1 0 0 0 1 0"/>
                <feBlend mode="normal" in2="shape" result="effect1_innerShadow_8_9"/>
                </filter>
                <filter id="filter2_di_8_9" x="82" y="136" width="87" height="112" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                <feOffset dy="20"/>
                <feGaussianBlur stdDeviation="9.5"/>
                <feComposite in2="hardAlpha" operator="out"/>
                <feColorMatrix type="matrix" values="0 0 0 0 0.283785 0 0 0 0 0.178889 0 0 0 0 0.933333 0 0 0 1 0"/>
                <feBlend mode="normal" in2="BackgroundImageFix" result="effect1_dropShadow_8_9"/>
                <feBlend mode="normal" in="SourceGraphic" in2="effect1_dropShadow_8_9" result="shape"/>
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                <feOffset dy="9"/>
                <feGaussianBlur stdDeviation="1.5"/>
                <feComposite in2="hardAlpha" operator="arithmetic" k2="-1" k3="1"/>
                <feColorMatrix type="matrix" values="0 0 0 0 1 0 0 0 0 1 0 0 0 0 1 0 0 0 0.21 0"/>
                <feBlend mode="normal" in2="shape" result="effect2_innerShadow_8_9"/>
                </filter>
                <filter id="filter3_di_8_9" x="226" y="136" width="87" height="112" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                <feOffset dy="20"/>
                <feGaussianBlur stdDeviation="9.5"/>
                <feComposite in2="hardAlpha" operator="out"/>
                <feColorMatrix type="matrix" values="0 0 0 0 0.283785 0 0 0 0 0.178889 0 0 0 0 0.933333 0 0 0 1 0"/>
                <feBlend mode="normal" in2="BackgroundImageFix" result="effect1_dropShadow_8_9"/>
                <feBlend mode="normal" in="SourceGraphic" in2="effect1_dropShadow_8_9" result="shape"/>
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                <feOffset dy="9"/>
                <feGaussianBlur stdDeviation="1.5"/>
                <feComposite in2="hardAlpha" operator="arithmetic" k2="-1" k3="1"/>
                <feColorMatrix type="matrix" values="0 0 0 0 1 0 0 0 0 1 0 0 0 0 1 0 0 0 0.21 0"/>
                <feBlend mode="normal" in2="shape" result="effect2_innerShadow_8_9"/>
                </filter>
                <filter id="filter4_di_8_9" x="157" y="200" width="87" height="112" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                <feOffset dy="20"/>
                <feGaussianBlur stdDeviation="9.5"/>
                <feComposite in2="hardAlpha" operator="out"/>
                <feColorMatrix type="matrix" values="0 0 0 0 0.283785 0 0 0 0 0.178889 0 0 0 0 0.933333 0 0 0 1 0"/>
                <feBlend mode="normal" in2="BackgroundImageFix" result="effect1_dropShadow_8_9"/>
                <feBlend mode="normal" in="SourceGraphic" in2="effect1_dropShadow_8_9" result="shape"/>
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                <feOffset dy="9"/>
                <feGaussianBlur stdDeviation="1.5"/>
                <feComposite in2="hardAlpha" operator="arithmetic" k2="-1" k3="1"/>
                <feColorMatrix type="matrix" values="0 0 0 0 1 0 0 0 0 1 0 0 0 0 1 0 0 0 0.21 0"/>
                <feBlend mode="normal" in2="shape" result="effect2_innerShadow_8_9"/>
                </filter>
                <filter id="filter5_f_8_9" x="0" y="114" width="396" height="191" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB">
                <feFlood flood-opacity="0" result="BackgroundImageFix"/>
                <feBlend mode="normal" in="SourceGraphic" in2="BackgroundImageFix" result="shape"/>
                <feGaussianBlur stdDeviation="39" result="effect1_foregroundBlur_8_9"/>
                </filter>
                <linearGradient id="paint0_linear_8_9" x1="60.9717" y1="169.76" x2="377.746" y2="-67.1272" gradientUnits="userSpaceOnUse">
                <stop stop-color="#D5D5D5"/>
                <stop offset="1" stop-color="white" stop-opacity="0.58"/>
                </linearGradient>
                </defs>
                </svg>`;
            }

            result += `</dd>`;
            result += `<dd class="temp">`;
            result += `<div class="degree">`;
            result += `<span>${weathers.tmp + "°"}</span></div>`;
            result += `</div>`;
            result += `</dl>`;
            result += `</li>`;
          });

          document.querySelector(".ul").innerHTML = result;

          // 온도 정수로 변환
          const tempNum =
            weather.tmp != null ? parseInt(weather.tmp, 10) : null;
          const tagElem = document.getElementById("current-temp-tag");
          if (tagElem) {
            tagElem.innerHTML =
              tempNum != null
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

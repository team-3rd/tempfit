export function weatherLoads(weatherData) {
  const weather = weatherData[0];
  let icon = "";

  if (weather.sky == "맑음") {
    icon += `<svg xmlns="http://www.w3.org/2000/svg" x="0px" y="0px" width="120" height="120" viewBox="0,0,240,240">
              <defs><radialGradient cx="24" cy="24" r="22" gradientUnits="userSpaceOnUse" id="color-1_8EUmYhfLPTCF_gr1"><stop offset="0.724" stop-color="#ffed54"></stop><stop offset="0.779" stop-color="#ffe649" stop-opacity="0.76863"></stop><stop offset="0.877" stop-color="#ffd22d" stop-opacity="0"></stop><stop offset="1" stop-color="#ffb300" stop-opacity="0"></stop></radialGradient><linearGradient x1="8.092" y1="8.092" x2="35.996" y2="35.996" gradientUnits="userSpaceOnUse" id="color-2_8EUmYhfLPTCF_gr2"><stop offset="0" stop-color="#fed100" stop-opacity="0.65882"></stop><stop offset="1" stop-color="#e36001" stop-opacity="0.63137"></stop></linearGradient></defs><g fill="none" fill-rule="nonzero" stroke="none" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="10" stroke-dasharray="" stroke-dashoffset="0" font-family="none" font-weight="none" font-size="none" text-anchor="none" style="mix-blend-mode: normal"><g transform="scale(5.33333,5.33333)"><path d="M24,2l1.421,1.474c0.93,0.965 2.388,1.196 3.571,0.566l1.807,-0.963l0.896,1.841c0.586,1.205 1.902,1.876 3.222,1.641l2.016,-0.357l0.283,2.028c0.185,1.328 1.229,2.371 2.557,2.557l2.028,0.283l-0.357,2.016c-0.234,1.32 0.436,2.635 1.641,3.222l1.841,0.896l-0.963,1.807c-0.631,1.183 -0.4,2.641 0.566,3.571l1.471,1.418l-1.474,1.421c-0.965,0.93 -1.196,2.388 -0.566,3.571l0.963,1.807l-1.841,0.896c-1.205,0.586 -1.876,1.902 -1.641,3.222l0.357,2.016l-2.028,0.283c-1.328,0.185 -2.371,1.229 -2.557,2.557l-0.283,2.028l-2.016,-0.357c-1.32,-0.234 -2.635,0.436 -3.222,1.641l-0.896,1.841l-1.807,-0.963c-1.183,-0.631 -2.641,-0.4 -3.571,0.566l-1.418,1.471l-1.421,-1.474c-0.93,-0.965 -2.388,-1.196 -3.571,-0.566l-1.807,0.963l-0.896,-1.841c-0.586,-1.205 -1.902,-1.876 -3.222,-1.641l-2.016,0.357l-0.283,-2.028c-0.185,-1.328 -1.229,-2.371 -2.557,-2.557l-2.028,-0.283l0.357,-2.016c0.234,-1.32 -0.436,-2.635 -1.641,-3.222l-1.841,-0.896l0.963,-1.807c0.631,-1.183 0.4,-2.641 -0.566,-3.571l-1.471,-1.418l1.474,-1.421c0.965,-0.93 1.196,-2.388 0.566,-3.571l-0.963,-1.807l1.841,-0.896c1.205,-0.586 1.876,-1.902 1.641,-3.222l-0.357,-2.016l2.028,-0.283c1.328,-0.185 2.371,-1.229 2.557,-2.557l0.283,-2.028l2.016,0.357c1.32,0.234 2.635,-0.436 3.222,-1.641l0.896,-1.841l1.807,0.963c1.183,0.631 2.641,0.4 3.571,-0.566z" fill="url(#color-1_8EUmYhfLPTCF_gr1)"></path><path d="M24,7c-9.389,0 -17,7.611 -17,17c0,9.389 7.611,17 17,17c9.389,0 17,-7.611 17,-17c0,-9.389 -7.611,-17 -17,-17z" fill="url(#color-2_8EUmYhfLPTCF_gr2)"></path></g></g>
              </svg>`;
  } else if (weather.sky == "구름 많음") {
    icon += `<img width="120" height="120" src="https://img.icons8.com/fluency/120/partly-cloudy-day.png" alt="partly-cloudy-day"/>`;
  } else if (weather.sky == "흐림" && weather.pty == "강수없음") {
    icon += `<img width="120" height="120" src="https://img.icons8.com/fluency/120/clouds--v3.png" alt="clouds--v3"/>`;
  } else if (
    (weather.sky == "흐림" && weather.pty == "비") ||
    weather.pty == "소나기"
  ) {
    icon += `<img width="120" height="120" src="https://img.icons8.com/fluency/120/heavy-rain.png" alt="heavy-rain"/>`;
  }
  document.querySelector("#weather-icon").innerHTML = icon;

  document.getElementById("weather-temp").textContent =
    weather.tmp !== undefined ? weather.tmp + "℃" : "-";

  let sky = "";
  sky += `<dt class="name">날씨</dt>`;
  sky += `<dd class="vals sky nameSpace">`;
  if (weather.sky == "맑음") {
    sky += `맑음</dd>`;
  } else if (weather.sky == "구름 많음") {
    sky += `구름 많음</dd>`;
  } else if (weather.sky == "흐림" && weather.pty == "강수없음") {
    sky += `흐림</dd>`;
  } else if (
    (weather.sky == "흐림" && weather.pty == "비") ||
    weather.pty == "소나기"
  ) {
    sky += `흐리고 비</dd>`;
  }
  document.querySelector("#weather-sky").innerHTML = sky;

  let hum = "";
  hum += `<img width="16" height="16" src="https://img.icons8.com/ios/16/water.png" alt="water"/>`;
  hum += `<dt class="name">습도</dt>`;
  hum += `<dd class="vals val nameSpace">${weather.reh}</dd>`;
  document.querySelector("#weather-humid").innerHTML = hum;

  let wind = "";
  wind += `<img width="16" height="16" src="https://img.icons8.com/ios/16/wind--v1.png" alt="wind--v1"/>`;
  wind += `<dt class="name name">풍속</dt>`;
  wind += `<dd class="vals val">${weather.wsd}</dd>`;
  document.querySelector("#weather-wind").innerHTML = wind;

  // 시간대별 온도
  let result = "";
  const now = new Date();
  const year = now.getFullYear();
  const month = now.getMonth();
  const day = now.getDate();
  const nowDay = new Date(year, month, day);

  weatherData.forEach((weathers) => {
    const time = weathers.fcstTime.slice(0, 2);
    const date = new Date(weathers.fcstDate);
    const threed = weathers.fcstDate.split("-");

    const diffms = date - nowDay;
    const msday = 24 * 60 * 60 * 1000;
    const diffday = Math.floor(diffms / msday);

    result += `<li class="li">`;
    result += `<dl class="weather-content">`;
    if (diffday == 1) {
      if (time == "00") {
        result += `<dt class="time tommorow tom-border">`;
        result += `<p>내일</p></dt>`;
      } else {
        result += `<dt class="time tommorow">`;
        result += `<p>${time}시</p></dt>`;
      }
    } else if (diffday == 2) {
      if (time == "00") {
        result += `<dt class="time after-tommorow aft-tom-border">`;
        result += `<p>모레</p></dt>`;
      } else {
        result += `<dt class="time after-tommorow">`;
        result += `<p>${time}시</p></dt>`;
      }
    } else if (diffday == 3) {
      if (time == "00") {
        result += `<dt class="time after-tommorow">`;
        result += `<p>${threed[1]}.${threed[2]}</p></dt>`;
      } else {
        result += `<dt class="time after-tommorow">`;
        result += `<p>${time}시</p></dt>`;
      }
    } else {
      result += `<dt class="time">`;
      result += `<p>${time}시</p></dt>`;
    }

    result += `<dd class="icon-box">`;

    if (weathers.sky == "맑음") {
      result += `<svg xmlns="http://www.w3.org/2000/svg" x="0px" y="0px" width="32" height="32" viewBox="0,0,360,360">
                <defs><radialGradient cx="24" cy="24" r="22" gradientUnits="userSpaceOnUse" id="color-1_8EUmYhfLPTCF_gr1"><stop offset="0.724" stop-color="#ffed54"></stop><stop offset="0.779" stop-color="#ffe649" stop-opacity="0.76863"></stop><stop offset="0.877" stop-color="#ffd22d" stop-opacity="0"></stop><stop offset="1" stop-color="#ffb300" stop-opacity="0"></stop></radialGradient><linearGradient x1="8.092" y1="8.092" x2="35.996" y2="35.996" gradientUnits="userSpaceOnUse" id="color-2_8EUmYhfLPTCF_gr2"><stop offset="0" stop-color="#fed100" stop-opacity="0.65882"></stop><stop offset="1" stop-color="#e36001" stop-opacity="0.63137"></stop></linearGradient></defs><g fill="none" fill-rule="nonzero" stroke="none" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="10" stroke-dasharray="" stroke-dashoffset="0" font-family="none" font-weight="none" font-size="none" text-anchor="none" style="mix-blend-mode: normal"><g transform="scale(5.33333,5.33333)"><path d="M24,2l1.421,1.474c0.93,0.965 2.388,1.196 3.571,0.566l1.807,-0.963l0.896,1.841c0.586,1.205 1.902,1.876 3.222,1.641l2.016,-0.357l0.283,2.028c0.185,1.328 1.229,2.371 2.557,2.557l2.028,0.283l-0.357,2.016c-0.234,1.32 0.436,2.635 1.641,3.222l1.841,0.896l-0.963,1.807c-0.631,1.183 -0.4,2.641 0.566,3.571l1.471,1.418l-1.474,1.421c-0.965,0.93 -1.196,2.388 -0.566,3.571l0.963,1.807l-1.841,0.896c-1.205,0.586 -1.876,1.902 -1.641,3.222l0.357,2.016l-2.028,0.283c-1.328,0.185 -2.371,1.229 -2.557,2.557l-0.283,2.028l-2.016,-0.357c-1.32,-0.234 -2.635,0.436 -3.222,1.641l-0.896,1.841l-1.807,-0.963c-1.183,-0.631 -2.641,-0.4 -3.571,0.566l-1.418,1.471l-1.421,-1.474c-0.93,-0.965 -2.388,-1.196 -3.571,-0.566l-1.807,0.963l-0.896,-1.841c-0.586,-1.205 -1.902,-1.876 -3.222,-1.641l-2.016,0.357l-0.283,-2.028c-0.185,-1.328 -1.229,-2.371 -2.557,-2.557l-2.028,-0.283l0.357,-2.016c0.234,-1.32 -0.436,-2.635 -1.641,-3.222l-1.841,-0.896l0.963,-1.807c0.631,-1.183 0.4,-2.641 -0.566,-3.571l-1.471,-1.418l1.474,-1.421c0.965,-0.93 1.196,-2.388 0.566,-3.571l-0.963,-1.807l1.841,-0.896c1.205,-0.586 1.876,-1.902 1.641,-3.222l-0.357,-2.016l2.028,-0.283c1.328,-0.185 2.371,-1.229 2.557,-2.557l0.283,-2.028l2.016,0.357c1.32,0.234 2.635,-0.436 3.222,-1.641l0.896,-1.841l1.807,0.963c1.183,0.631 2.641,0.4 3.571,-0.566z" fill="url(#color-1_8EUmYhfLPTCF_gr1)"></path><path d="M24,7c-9.389,0 -17,7.611 -17,17c0,9.389 7.611,17 17,17c9.389,0 17,-7.611 17,-17c0,-9.389 -7.611,-17 -17,-17z" fill="url(#color-2_8EUmYhfLPTCF_gr2)"></path></g></g>
                </svg>`;
    } else if (weathers.sky == "구름 많음") {
      result += `<img width="32" height="32" src="https://img.icons8.com/fluency/32/partly-cloudy-day.png" alt="partly-cloudy-day"/>`;
    } else if (weathers.sky == "흐림" && weathers.pty == "강수없음") {
      result += `<img width="32" height="32" src="https://img.icons8.com/fluency/32/clouds--v3.png" alt="clouds--v3"/>`;
    } else if (
      (weathers.sky == "흐림" && weathers.pty == "비") ||
      weathers.pty == "소나기"
    ) {
      result += `<img width="32" height="32" src="https://img.icons8.com/fluency/32/heavy-rain.png" alt="heavy-rain"/>`;
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
}

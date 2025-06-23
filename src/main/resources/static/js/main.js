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

            // [TOP LOOKS] 온도로 스타일별 TOP 게시글 fetch
            fetch(`/api/community/top?temp=${tempNum}`)
              .then((res) => res.json())
              .then((data) => {
                renderTopLooks(data);
              })
              .catch(() => {
                if (document.getElementById("top-looks-area")) {
                  document.getElementById("top-looks-area").innerHTML =
                    "<div class='text-danger'>TOP LOOKS 정보를 가져올 수 없습니다</div>";
                }
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

  // 공통 렌더 함수 (코디)
  function renderGuide(divId, data) {
    const guideDiv = document.getElementById(divId);
    guideDiv.innerHTML = "";
    ["top", "bottom", "shoes"].forEach((part) => {
      if (data[part]) {
        guideDiv.innerHTML += `
          <div style="text-align:center;">
            <img src="${data[part].imageUrl}" alt="${data[part].name}"
              style="max-width:130px; width:100%; height:auto; border-radius:10px; border:1px solid #ddd; background:#fafafa;"/><br/>
            <b>${part === "top" ? "상의" : part === "bottom" ? "하의" : "신발"}</b><br/>
            <span style="font-size:18px;">${data[part].name}</span>
          </div>
        `;
      }
    });
  }

  // [TOP LOOKS] 영역에 게시글 뿌리기
  function renderTopLooks(data) {
    const area = document.getElementById("top-looks-area");
    if (!area) return;
    const styleList = [
      { key: "캐주얼", label: "캐주얼" },
      { key: "포멀", label: "포멀" },
      { key: "스트리트", label: "스트리트" },
      { key: "아웃도어", label: "아웃도어" },
    ];
    let html = "";
    styleList.forEach((styleObj) => {
      const posts = data[styleObj.key] || [];
      html += `
      <div class="col-md-3 mb-4 d-flex">
        <div class="card flex-fill h-100">
          <div class="card-body d-flex flex-column">
            <h2 class="card-title">${styleObj.label}</h2>
            <div class="card-text flex-grow-1" style="font-size:0.97rem;">
              ${
                posts.length > 0
                  ? posts
                      .map(
                        (post) =>
                          `<div style="margin-bottom:14px;">
                      <a href="/community/detail/${post.id}" style="font-weight:600;">
                        ${post.title}
                      </a><br>
                      ${
                        post.repImageUrl
                          ? `<img src="/uploads/${post.repImageUrl}" style="max-width:100%;max-height:80px;display:block;margin:8px auto 4px auto;border-radius:8px;">`
                          : ""
                      }
                      <small style="color:#888;">추천수: ${post.recommendCount}</small>
                    </div>`
                      )
                      .join("")
                  : "<span class='text-muted'>게시글 없음</span>"
              }
            </div>
          </div>
          <div class="card-footer bg-white border-0">
            <a class="btn btn-primary btn-sm w-100" href="/community/list?style=${encodeURIComponent(
              styleObj.key
            )}">전체보기</a>
          </div>
        </div>
      </div>
    `;
    });
    area.innerHTML = html;
  }
});

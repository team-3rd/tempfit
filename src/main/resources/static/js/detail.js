document.addEventListener('DOMContentLoaded', function () {
    const scriptTag = document.getElementById('detail-script'); // null 아님
  
    if (!scriptTag) {
      console.error('스크립트 태그를 찾을 수 없습니다.');
      return;
    }
  
    const lat = parseFloat(scriptTag.getAttribute('data-lat'));
    const lon = parseFloat(scriptTag.getAttribute('data-lon'));
    const date = scriptTag.getAttribute('data-date');
  
    if (!isNaN(lat) && !isNaN(lon) && date) {
      loadWeather(lat, lon, date);
    } else {
      console.error('좌표 또는 날짜가 올바르지 않습니다.', { lat, lon, date });
    }
  });
  
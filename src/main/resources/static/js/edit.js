// static/js/edit.js

document.addEventListener('DOMContentLoaded', () => {
  const form        = document.getElementById('communityForm');          // 커뮤니티 폼
  const repInput    = document.getElementById('repImage');              // 대표 이미지 input
  const previewImg  = document.getElementById('previewImage');          // 미리보기 img 요소
  const xBtn        = document.getElementById('repImageXBtn');          // 이미지 삭제(X) 버튼
  const removeFlag  = document.getElementById('removeRepImage');        // 삭제 플래그 hidden input
  const styleChecks = document.querySelectorAll('.style-check');        // 스타일 체크박스 NodeList
  const styleGroup  = document.getElementById('styleGroup');            // 스타일 그룹 div (유효성 피드백용)

  // 미리보기 리셋 함수: 이미지 숨기기, input 초기화, 플래그 설정, 버튼 숨기기, validation 표시
  function resetPreviewImage() {
    previewImg.style.display = 'none';       // 미리보기 이미지 숨김
    repInput.value           = '';           // file input 초기화
    removeFlag.value         = 'true';       // 서버에 삭제 요청 플래그 설정
    xBtn.classList.remove('show');           // X 버튼 숨기기
    repInput.classList.add('is-invalid');    // Bootstrap validation 스타일 적용
  }

  // 1) 페이지 로드 시 원본 src 값을 data-origin-src에 저장
  if (previewImg) {
    previewImg.setAttribute('data-origin-src', previewImg.src);
  }

  // 2) 초기 로드: 기존 이미지(src) 있으면 보여주고 X 버튼 표시
  if (previewImg && previewImg.src) {
    previewImg.style.display = '';
    xBtn.classList.add('show');
  }

  // 3) 파일 선택 시 미리보기 업데이트, X 버튼 보이기, 삭제 플래그 초기화, validation 해제
  repInput.addEventListener('change', e => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = ev => {
        previewImg.src           = ev.target.result;    // 읽어온 데이터를 src에 설정
        previewImg.style.display = '';                 // 이미지 보이기
        removeFlag.value         = 'false';            // 삭제 플래그 해제
        xBtn.classList.add('show');                    // X 버튼 보이기
        repInput.classList.remove('is-invalid');       // validation 해제
      };
      reader.readAsDataURL(file);
    } else {
      // 파일 선택 취소 시 원본 복구
      resetPreviewImage();
    }
  });

  // 4) X 버튼 클릭 시 미리보기 리셋 함수 호출
  xBtn.addEventListener('click', resetPreviewImage);

  // 스타일 체크박스 최대 2개 선택 제한
  styleChecks.forEach(chk => {
    chk.addEventListener('change', () => {
      const checked = document.querySelectorAll('.style-check:checked');
      if (checked.length > 2) {
        chk.checked = false;  // 2개 초과 시 체크 해제
      }
    });
  });

  // 폼 제출 시 유효성 검사
  form.addEventListener('submit', e => {
    let valid = true;

    // 대표이미지 필수 검사: 파일 없고 삭제 플래그가 true면 오류
    const noFileChosen = repInput.files.length === 0 && removeFlag.value === 'true';
    if (noFileChosen) {
      repInput.classList.add('is-invalid');  // 빨간 테두리 표시
      valid = false;
    } else {
      repInput.classList.remove('is-invalid');
    }

    // 스타일 체크박스 1~2개 필수 검사
    const checkedStyles = Array.from(styleChecks).filter(c => c.checked);
    if (checkedStyles.length < 1 || checkedStyles.length > 2) {
      styleGroup.classList.add('was-validated');  // 스타일 그룹 validation 표시
      valid = false;
    } else {
      styleGroup.classList.remove('was-validated');
    }

    // 유효하지 않으면 폼 제출 막기
    if (!valid) {
      e.preventDefault();
      e.stopPropagation();
    }
    form.classList.add('was-validated');  // Bootstrap validation 스타일 활성화
  });
});

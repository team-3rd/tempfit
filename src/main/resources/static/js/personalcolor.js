(function () {
  const $choices = document.getElementById('choices');
  const $toast = document.getElementById('toast');
  const $toneLabel = document.getElementById('toneLabel');

  // CSRF (메타가 없을 수도 있음)
  const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
  const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
  const CSRF_TOKEN = csrfTokenMeta ? csrfTokenMeta.getAttribute('content') : null;
  const CSRF_HEADER = csrfHeaderMeta ? csrfHeaderMeta.getAttribute('content') : null;

  function showToast(text) {
    if (!$toast) return;
    $toast.textContent = text || '저장되었습니다';
    $toast.style.display = 'block';
    setTimeout(() => { $toast.style.display = 'none'; }, 1200);
  }

  function codeToLabel(c) {
    const n = typeof c === 'string' ? parseInt(c, 10) : c;
    switch (n) {
      case 1: return '봄 웜톤';
      case 2: return '여름 쿨톤';
      case 3: return '가을 웜톤';
      case 4: return '겨울 쿨톤';
      default: return '미선택';
    }
  }

  function updateSummary(code) {
    if ($toneLabel) $toneLabel.textContent = codeToLabel(code); // 따옴표는 CSS로 자동
  }

  // 서버 플래시 토스트 즉시 표시(있으면)
  if ($toast && $toast.textContent && $toast.textContent.trim().length > 0) {
    showToast($toast.textContent.trim());
  }

  if (!$choices) return;

  $choices.addEventListener('click', async (e) => {
    const btn = e.target.closest('button.imgbtn[data-code]');
    if (!btn) return;
    const code = btn.getAttribute('data-code');

    // UI 즉시 반영
    Array.from($choices.querySelectorAll('.imgbtn')).forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    updateSummary(code);

    try {
      const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
      if (CSRF_TOKEN && CSRF_HEADER) headers[CSRF_HEADER] = CSRF_TOKEN;

      const resp = await fetch('/personalcolor/select', {
        method: 'POST',
        headers,
        body: new URLSearchParams({ code })
      });

      if (!resp.ok) {
        const msg = await resp.text();
        throw new Error(msg || '저장 실패');
      }

      showToast('저장되었습니다');
    } catch (err) {
      console.error(err);
      showToast('저장 실패');
    }
  });
})();

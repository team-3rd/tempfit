(function () {
  const $choices = document.getElementById('choices');
  const $toast = document.getElementById('toast');

  // CSRF (메타가 없을 수도 있음)
  const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
  const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
  const CSRF_TOKEN = csrfTokenMeta ? csrfTokenMeta.getAttribute('content') : null;
  const CSRF_HEADER = csrfHeaderMeta ? csrfHeaderMeta.getAttribute('content') : null;

  function showToast(text) {
    if (!$toast) return;
    $toast.textContent = text || $toast.textContent || '저장되었습니다';
    $toast.style.display = 'block';
    setTimeout(() => { $toast.style.display = 'none'; }, 1200);
  }

  // 플래시 토스트가 서버에서 온 경우 즉시 표시
  if ($toast && $toast.textContent && $toast.textContent.trim().length > 0) {
    showToast($toast.textContent.trim());
  }

  if (!$choices) return;

  $choices.addEventListener('click', async (e) => {
    const btn = e.target.closest('button[data-code]');
    if (!btn) return;
    const code = btn.getAttribute('data-code');

    // UI 즉시 반영
    Array.from($choices.querySelectorAll('.btn')).forEach(b => b.classList.remove('active'));
    btn.classList.add('active');

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

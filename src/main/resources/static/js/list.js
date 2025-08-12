// 공통 유틸: 1k 포맷
function formatCount(n) {
  const num = Number(n || 0);
  if (Math.abs(num) >= 1000) {
    const val = (num / 1000).toFixed(1).replace(/\.0$/, "");
    return `${val}k`;
  }
  return String(num);
}

document.addEventListener("DOMContentLoaded", () => {
  // 카드 hover scale은 CSS로 처리, 클릭 시 상세 모달 로드
  document.querySelectorAll(".post-card").forEach((card) => {
    card.addEventListener("click", (e) => {
      // 액션 버튼 클릭 시 카드 클릭 막기
      if (e.target.closest(".btn-action")) return;

      e.preventDefault();
      const postId = card.getAttribute("data-id");

      fetch(`/community/detail/${postId}`)
        .then((res) => res.text())
        .then((html) => {
          document.getElementById("modal-fragment").innerHTML = html;
          new bootstrap.Modal(document.getElementById("detailModal")).show();
        });
    });
  });

  // 숫자 포맷팅 적용
  document.querySelectorAll(".count").forEach((el) => {
    const n = el.getAttribute("data-count");
    el.textContent = formatCount(n);
  });

  // 좋아요 토글
  document.querySelectorAll(".btn-like").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      e.stopPropagation();
      const id = btn.getAttribute("data-id");
      const icon = btn.querySelector(".bi");
      const countEl = btn.parentElement.querySelector(".like-count");
      let current = Number(countEl.getAttribute("data-count") || 0);
      const liked = icon.classList.contains("bi-heart-fill");

      fetch(`/community/recommend/${id}`, { method: "POST" })
        .catch(() => {}) // 실패해도 UI는 낙관적 반영
        .finally(() => {
          if (liked) {
            icon.classList.remove("bi-heart-fill");
            icon.classList.add("bi-heart");
            current = Math.max(0, current - 1);
          } else {
            icon.classList.remove("bi-heart");
            icon.classList.add("bi-heart-fill");
            current = current + 1;
          }
          countEl.setAttribute("data-count", current);
          countEl.textContent = formatCount(current);
        });
    });
  });

  // 북마크 토글(맨 오른쪽)
  document.querySelectorAll(".btn-bookmark").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      e.stopPropagation();
      const id = btn.getAttribute("data-id");
      const icon = btn.querySelector(".bi");
      const checked = icon.classList.contains("bi-bookmark-check-fill");

      fetch(`/community/bookmark/${id}`, { method: "POST" })
        .catch(() => {})
        .finally(() => {
          if (checked) {
            icon.classList.remove("bi-bookmark-check-fill");
            icon.classList.add("bi-bookmark");
          } else {
            icon.classList.remove("bi-bookmark");
            icon.classList.add("bi-bookmark-check-fill");
          }
        });
    });
  });

  // 댓글 버튼: 카드 클릭(모달)과 동일 동작
  document.querySelectorAll(".btn-comment").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      e.stopPropagation();
      btn.closest(".post-card")?.click();
    });
  });
});

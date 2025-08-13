// /js/list.js

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
  // 카드 클릭 → 상세 모달
  document.querySelectorAll(".post-card").forEach((card) => {
    card.addEventListener("click", async (e) => {
      // 기본 이동 완전히 차단
      e.preventDefault();
      e.stopPropagation();

      // 액션 버튼 클릭은 무시
      if (e.target.closest(".btn-action")) return;

      const postId = card.getAttribute("data-id");

      // fragment 우선, 실패 시 기본 경로로 폴백
      const fetchHtml = async () => {
        const tryFetch = async (url) => {
          const res = await fetch(url, { credentials: "same-origin" });
          if (!res.ok) throw new Error(String(res.status));
          return res.text();
        };
        try {
          // ✅ 프래그먼트 전용 엔드포인트(이번 수정)
          return await tryFetch(`/community/detail/${postId}/fragment`);
        } catch (_) {
          // ↩️ 호환용 폴백
          return await tryFetch(`/community/detail/${postId}`);
        }
      };

      try {
        const html = await fetchHtml();

        const host = document.getElementById("modal-fragment");
        if (!host) {
          console.error("#modal-fragment 컨테이너가 없습니다.");
          return;
        }
        host.innerHTML = html; // fragment(.modal-content) 주입

        // 💡 주입 후 모달 내부 이벤트 바인딩
        if (window.initDetailModal) {
          window.initDetailModal(host);
        }

        if (!window.bootstrap || !window.bootstrap.Modal) {
          console.error("Bootstrap JS가 로드되지 않았습니다.");
          return;
        }
        const modal = new bootstrap.Modal(document.getElementById("detailModal"), {
          backdrop: true,
          focus: true,
        });
        modal.show();
      } catch (err) {
        console.error("모달 로드 실패:", err);
      }
    });
  });

  // 숫자 포맷팅
  document.querySelectorAll(".count").forEach((el) => {
    const n = el.getAttribute("data-count");
    el.textContent = formatCount(n);
  });

  // 좋아요 토글
  document.querySelectorAll(".btn-like").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      e.stopPropagation();
      e.preventDefault();

      const id = btn.getAttribute("data-id");
      const icon = btn.querySelector(".bi");
      const countEl = btn.parentElement.querySelector(".like-count");
      let current = Number(countEl.getAttribute("data-count") || 0);
      const liked = icon.classList.contains("bi-heart-fill");

      fetch(`/community/recommend/${id}`, { method: "POST" })
        .catch(() => {})
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

  // 북마크 토글
  document.querySelectorAll(".btn-bookmark").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      e.stopPropagation();
      e.preventDefault();

      const id = btn.getAttribute("data-id");
      const icon = btn.querySelector(".bi");
      const checked = icon.classList.contains("bi-bookmark-fill");

      fetch(`/community/bookmark/${id}`, { method: "POST" })
        .catch(() => {})
        .finally(() => {
          if (checked) {
            icon.classList.remove("bi-bookmark-fill");
            icon.classList.add("bi-bookmark");
          } else {
            icon.classList.remove("bi-bookmark");
            icon.classList.add("bi-bookmark-fill");
          }
        });
    });
  });

  // 댓글 버튼 = 카드 클릭
  document.querySelectorAll(".btn-comment").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      e.stopPropagation();
      e.preventDefault();
      btn.closest(".post-card")?.click();
    });
  });
});

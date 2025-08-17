// /js/profile
// .js

// 공통 유틸: 1k 포맷
function formatCount(n) {
  const num = Number(n || 0);
  if (Math.abs(num) >= 1000) {
    const val = (num / 1000).toFixed(1).replace(/\.0$/, "");
    return `${val}k`;
  }
  return String(num);
}

// 오버레이 메시지
function showCardNotice(cardEl, message) {
  if (!cardEl) return;
  const cs = window.getComputedStyle(cardEl);
  if (!["relative", "absolute", "fixed", "sticky"].includes(cs.position)) {
    cardEl.style.position = "relative";
  }
  const note = document.createElement("div");
  note.className = "card-notice";
  note.textContent = message;
  note.style.position = "absolute";
  note.style.left = "50%";
  note.style.bottom = "14px";
  note.style.transform = "translateX(-50%)";
  note.style.background = "rgba(20,20,20,0.96)";
  note.style.color = "#fff";
  note.style.padding = "8px 12px";
  note.style.fontSize = "13px";
  note.style.whiteSpace = "nowrap"; // ✅ 한 줄 고정
  note.style.borderRadius = "0";
  note.style.boxShadow = "0 2px 10px rgba(0,0,0,0.25)";
  note.style.pointerEvents = "none";
  note.style.zIndex = "50";
  cardEl.appendChild(note);
  setTimeout(() => {
    note.style.transition = "opacity 220ms ease";
    note.style.opacity = "0";
    setTimeout(() => note.remove(), 240);
  }, 1400);
}

// 서버 응답이 로그인 필요인지 판별
function needsLogin(res) {
  const url = (res && res.url) || "";
  if (res.status === 401 || res.status === 403) return true;
  if (
    res.redirected &&
    (url.includes("/member/login") || url.includes("/login"))
  )
    return true;
  return false;
}

// 공통: 유효 ID
function isValidId(v) {
  if (v == null) return false;
  if (v === "" || v === "null" || v === "undefined") return false;
  const n = Number(v);
  return Number.isInteger(n) && n > 0;
}

document.addEventListener("DOMContentLoaded", () => {
  // 상세 모달 로드
  document.querySelectorAll(".posts").forEach((card) => {
    card.addEventListener("click", async (e) => {
      const postId = card.getAttribute("data-id");
      if (!isValidId(postId)) return; // placeholder 가드

      // ✅ 공용 로더 호출
      if (
        window.ModalLoader &&
        typeof window.ModalLoader.setDetailModal === "function"
      ) {
        window.ModalLoader.setDetailModal(postId);
      }

      const modal = new bootstrap.Modal("#detailModal");
      modal.show();
    });
  });

  // 좋아요 토글: 서버 응답으로 로그인 판별
  document.querySelectorAll(".btn-like").forEach((btn) => {
    btn.addEventListener("click", function (e) {
      e.stopPropagation();
      e.preventDefault();

      const card = btn.closest(".posts");
      const id = btn.getAttribute("data-id");
      const icon = btn.querySelector(".bi");
      const countEl = btn.parentElement.querySelector(".like-count");
      let current = Number(countEl.getAttribute("data-count") || 0);
      const liked = icon.classList.contains("bi-heart-fill");

      fetch(`/community/recommend/${id}`, {
        method: "POST",
        credentials: "same-origin",
      })
        .then((res) => {
          if (needsLogin(res)) {
            showCardNotice(card, "로그인이 필요합니다.");
            return;
          }
          if (!res.ok) throw new Error(String(res.status));
          // 성공 시에만 UI 토글
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
        })
        .catch(() => {
          showCardNotice(card, "로그인이 필요합니다.");
        });
    });
  });

  // 북마크 토글: 서버 응답으로 로그인 판별
  document.querySelectorAll(".btn-bookmark").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      e.stopPropagation();
      e.preventDefault();

      const card = btn.closest(".posts");
      const id = btn.getAttribute("data-id");
      const icon = btn.querySelector(".bi");
      const checked = icon.classList.contains("bi-bookmark-fill");

      fetch(`/community/bookmark/${id}`, {
        method: "POST",
        credentials: "same-origin",
      })
        .then((res) => {
          if (needsLogin(res)) {
            showCardNotice(card, "로그인이 필요합니다.");
            return;
          }
          if (!res.ok) throw new Error(String(res.status));
          // 성공 시에만 UI 토글
          icon.classList.toggle("bi-bookmark-fill", !checked);
          icon.classList.toggle("bi-bookmark", checked);
        })
        .catch(() => {
          showCardNotice(card, "로그인이 필요합니다.");
        });
    });
  });

  // 댓글 버튼 = 카드 클릭
  document.querySelectorAll(".btn-comment").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      e.stopPropagation();
      e.preventDefault();
      btn.closest(".posts")?.click();
    });
  });
});

document.addEventListener("DOMContentLoaded", () => {
  // 북마크 게시글 상세 모달 로드
  document.querySelectorAll(".bookmarkedPosts").forEach((card) => {
    card.addEventListener("click", async (e) => {
      const postId = card.getAttribute("data-id");
      if (!isValidId(postId)) return; // placeholder 가드

      // ✅ 공용 로더 호출
      if (
        window.ModalLoader &&
        typeof window.ModalLoader.setDetailModal === "function"
      ) {
        window.ModalLoader.setDetailModal(postId);
      }

      const modal = new bootstrap.Modal("#detailModal");
      modal.show();
    });
  });

  // 좋아요 토글: 서버 응답으로 로그인 판별
  document.querySelectorAll(".btn-like").forEach((btn) => {
    btn.addEventListener("click", function (e) {
      e.stopPropagation();
      e.preventDefault();

      const card = btn.closest(".bookmarkedPosts");
      const id = btn.getAttribute("data-id");
      const icon = btn.querySelector(".bi");
      const countEl = btn.parentElement.querySelector(".like-count");
      let current = Number(countEl.getAttribute("data-count") || 0);
      const liked = icon.classList.contains("bi-heart-fill");

      fetch(`/community/recommend/${id}`, {
        method: "POST",
        credentials: "same-origin",
      })
        .then((res) => {
          if (needsLogin(res)) {
            showCardNotice(card, "로그인이 필요합니다.");
            return;
          }
          if (!res.ok) throw new Error(String(res.status));
          // 성공 시에만 UI 토글
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
        })
        .catch(() => {
          showCardNotice(card, "로그인이 필요합니다.");
        });
    });
  });

  // 북마크 토글: 서버 응답으로 로그인 판별
  document.querySelectorAll(".btn-bookmark").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      e.stopPropagation();
      e.preventDefault();

      const card = btn.closest(".bookmarkedPosts");
      const id = btn.getAttribute("data-id");
      const icon = btn.querySelector(".bi");
      const checked = icon.classList.contains("bi-bookmark-fill");

      fetch(`/community/bookmark/${id}`, {
        method: "POST",
        credentials: "same-origin",
      })
        .then((res) => {
          if (needsLogin(res)) {
            showCardNotice(card, "로그인이 필요합니다.");
            return;
          }
          if (!res.ok) throw new Error(String(res.status));
          // 성공 시에만 UI 토글
          icon.classList.toggle("bi-bookmark-fill", !checked);
          icon.classList.toggle("bi-bookmark", checked);
        })
        .catch(() => {
          showCardNotice(card, "로그인이 필요합니다.");
        });
    });
  });

  // 댓글 버튼 = 카드 클릭
  document.querySelectorAll(".btn-comment").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      e.stopPropagation();
      e.preventDefault();
      btn.closest(".posts")?.click();
    });
  });
});

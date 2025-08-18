// /js/detail.js
// 상세 모달(fragment) 내부의 좋아요/북마크 버튼에 대해
// 서버 응답 기반 로그인 판별 + 성공 시 토글 처리.

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
  console.log(res);
  console.log(res.url);
  if (res.status === 401 || res.status === 403) return true;
  if (
    res.redirected &&
    (url.includes("/member/login") || url.includes("/login"))
  )
    return true;
  return false;
}

// 숫자 축약 표시
function k(n) {
  const num = Number(n || 0);
  if (Math.abs(num) >= 1000) {
    const v = (num / 1000).toFixed(1).replace(/\.0$/, "");
    return `${v}k`;
  }
  return String(num);
}

// ✅ 모달에서 변경된 상태를 바깥(메인/리스트)의 카드에도 반영
function syncOuterCards(id, { liked, likeCount, bookmarked }) {
  const nodes = Array.from(
    document.querySelectorAll(`.post-card[data-id="${id}"]`)
  ).filter((el) => !el.closest(".modal")); // 모달 내부 제외

  nodes.forEach((card) => {
    // 좋아요 아이콘/카운트
    const likeIcon = card.querySelector(".btn-like .bi");
    const likeCountEl = card.querySelector(".like-count");
    if (likeIcon && typeof liked === "boolean") {
      likeIcon.classList.toggle("bi-heart-fill", liked);
      likeIcon.classList.toggle("bi-heart", !liked);
    }
    if (likeCountEl && typeof likeCount === "number") {
      likeCountEl.setAttribute("data-count", String(likeCount));
      likeCountEl.textContent = k(likeCount);
    }
    // 북마크 아이콘
    if (typeof bookmarked === "boolean") {
      const bmIcon = card.querySelector(".btn-bookmark .bi");
      if (bmIcon) {
        bmIcon.classList.toggle("bi-bookmark-fill", bookmarked);
        bmIcon.classList.toggle("bi-bookmark", !bookmarked);
      }
    }
  });
}

// 상세 모달 초기화 후 (list.js/main.js에서 fragment 주입 후 호출)
window.initDetailModal = function initDetailModal(host) {
  const root = host || document; // host가 없으면 document 기준
  const card =
    root.querySelector(".post-card") ||
    root.querySelector(".modal-content") ||
    root;

  // 좋아요 버튼
  root.querySelectorAll(".btn-like").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      e.preventDefault();
      e.stopPropagation();

      const id = btn.getAttribute("data-id");
      const icon = btn.querySelector(".bi");
      // ✅ 우선 '가까운' 액션 그룹에서 like-count 찾고, 없으면 전체에서 하나
      const countEl =
        btn.closest(".action-group")?.querySelector(".like-count") ||
        document.getElementById("igLikeCount");

      let current = Number(countEl?.getAttribute("data-count") || 0);
      const wasLiked = icon.classList.contains("bi-heart-fill");

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
          const nowLiked = !wasLiked;
          if (nowLiked) {
            icon.classList.remove("bi-heart");
            icon.classList.add("bi-heart-fill");
            current = current + 1;
          } else {
            icon.classList.remove("bi-heart-fill");
            icon.classList.add("bi-heart");
            current = Math.max(0, current - 1);
          }
          if (countEl) {
            countEl.setAttribute("data-count", current);
            countEl.textContent = k(current);
          }

          // ✅ 바깥 카드들도 동기화
          syncOuterCards(id, { liked: nowLiked, likeCount: current });
        })
        .catch(() => {
          showCardNotice(card, "로그인이 필요합니다.");
        });
    });
  });

  // 북마크 버튼
  root.querySelectorAll(".btn-bookmark").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      e.preventDefault();
      e.stopPropagation();

      const id = btn.getAttribute("data-id");
      const icon = btn.querySelector(".bi");
      const wasBookmarked = icon.classList.contains("bi-bookmark-fill");

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
          const nowBookmarked = !wasBookmarked;
          icon.classList.toggle("bi-bookmark-fill", nowBookmarked);
          icon.classList.toggle("bi-bookmark", !nowBookmarked);

          // ✅ 바깥 카드들도 동기화 (북마크만)
          syncOuterCards(id, { bookmarked: nowBookmarked });
        })
        .catch(() => {
          showCardNotice(card, "로그인이 필요합니다.");
        });
    });
  });

  // 댓글
  if (document.querySelector(".ig-input")) {
    const commentForm = document.querySelector(".ig-form");
    const id = commentForm.getAttribute("data-id");

    commentForm.addEventListener("submit", async function (e) {
      e.preventDefault();

      const formData = new FormData(commentForm);
      await fetch(`/api/community/detail/${id}/comments`, {
        method: "POST",
        credentials: "same-origin",
        body: formData,
      })
        .then((res) => res.json())
        .then((data) => {
          console.log(data);

          fetch(`/community/detail/${id}/fragment`)
            .then((res) => res.text())
            .then((data) => {
              const dialog = document.querySelector(".modal-dialog");
              dialog.innerHTML = data;

              // 댓글 로드 후 사진 1개면 스크롤 버튼 숨기기
              const imgCount =
                document.querySelectorAll(".carousel-item").length;
              const prevBtn = document.querySelector(".carousel-control-prev");
              const nextBtn = document.querySelector(".carousel-control-next");

              if (imgCount == 1) {
                prevBtn.style.display = "none";
                nextBtn.style.display = "none";
              } else {
                prevBtn.style.display = "flex";
                nextBtn.style.display = "flex";
              }
            });
        });
    });
  }

  // 사진 1개면 스크롤 버튼 숨기기
  const imgCount = document.querySelectorAll(".carousel-item").length;
  const prevBtn = document.querySelector(".carousel-control-prev");
  const nextBtn = document.querySelector(".carousel-control-next");

  if (imgCount == 1) {
    prevBtn.style.display = "none";
    nextBtn.style.display = "none";
  } else {
    prevBtn.style.display = "flex";
    nextBtn.style.display = "flex";
  }
};

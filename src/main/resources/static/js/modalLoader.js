// /js/modal-loader.js
// 메인/커뮤니티에서 공용으로 쓰는 모달 로더
// - fragment 우선 로드 (/community/detail/{id}/fragment), 실패 시 전체 페이지 폴백 (/community/detail/{id})
// - 모달 컨테이너가 없으면 자동 생성 (#detailModal 안에 #modal-fragment)
// - 로드 후 window.initDetailModal(host) 호출(있을 때만)

(function (win) {
  "use strict";

  function isValidId(v) {
    if (v == null) return false;
    if (v === "" || v === "null" || v === "undefined") return false;
    const n = Number(v);
    return Number.isInteger(n) && n > 0;
  }

  function ensureDetailModal() {
    let modal = document.getElementById("detailModal");
    if (modal) {
      // #modal-fragment가 없으면 추가
      if (!modal.querySelector("#modal-fragment")) {
        const content = modal.querySelector(".modal-content") || modal;
        const host = document.createElement("div");
        host.id = "modal-fragment";
        content.appendChild(host);
      }
      return modal;
    }
    const wrap = document.createElement("div");
    wrap.innerHTML = `
      <div class="modal fade" id="detailModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-xl">
          <div class="modal-content">
            <div id="modal-fragment"></div>
          </div>
        </div>
      </div>`;
    document.body.appendChild(wrap.firstElementChild);
    return document.getElementById("detailModal");
  }

  async function fetchHtmlPreferFragment(postId) {
    const tryFetch = async (url) => {
      const res = await fetch(url, { credentials: "same-origin" });
      if (!res.ok) throw new Error(String(res.status));
      return res.text();
    };
    try {
      return await tryFetch(`/community/detail/${postId}/fragment`);
    } catch (_) {
      return await tryFetch(`/community/detail/${postId}`);
    }
  }

  async function openDetailModal(postId) {
    if (!isValidId(postId)) return;

    const modalEl = ensureDetailModal();
    const host = modalEl.querySelector("#modal-fragment");
    if (!host) {
      console.error("#modal-fragment 컨테이너가 없습니다.");
      return;
    }
    host.innerHTML = "";

    try {
      const html = await fetchHtmlPreferFragment(postId);
      host.innerHTML = html;

      if (typeof window.initDetailModal === "function") {
        window.initDetailModal(host);
      }
    } catch (e) {
      host.innerHTML = `<div class="p-4">상세 정보를 불러오지 못했습니다.</div>`;
    }

    if (!window.bootstrap || !window.bootstrap.Modal) {
      console.error("Bootstrap JS가 로드되지 않았습니다.");
      return;
    }
    const modal = new bootstrap.Modal(modalEl, { backdrop: true, focus: true });
    modal.show();
  }

  win.ModalLoader = {
    ensureDetailModal,
    openDetailModal,
  };
})(window);

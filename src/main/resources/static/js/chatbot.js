// src/main/resources/static/js/chatbot.js
(function () {
  "use strict";

  const chatBox = document.getElementById("chatBox");
  const msgInput = document.getElementById("msgInput");
  const sendBtn = document.getElementById("sendBtn");

  // 컨텍스트(hidden)
  const ctxTempEl = document.getElementById("ctxTemp");
  const ctxLocEl = document.getElementById("ctxLoc");
  const ctxDateEl = document.getElementById("ctxDate");
  const ctxLatEl = document.getElementById("ctxLat");
  const ctxLonEl = document.getElementById("ctxLon");
  const ctxLoggedEl = document.getElementById("ctxLoggedIn");

  document.addEventListener("DOMContentLoaded", () => {
    // lat/lon 미지정이면 브라우저 Geolocation으로 채우기
    if ((!ctxLatEl.value || !ctxLonEl.value) && navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          ctxLatEl.value = String(pos.coords.latitude);
          ctxLonEl.value = String(pos.coords.longitude);
        },
        () => {
          /* 무시: 위치 거부 시 서버가 안내 */
        },
        {
          enableHighAccuracy: true,
        }
      );
    }

    const isLoggedIn = String(ctxLoggedEl?.value) === "true";

    // ★ 로그인 상태에 따라 첫 메시지 분기
    if (isLoggedIn) {
      appendBotWelcome();
    } else {
      appendMessage({
        isMine: false,
        content: "로그인 후 이용할 수 있어요.",
        sentAt: new Date(),
      });
      // UX: 입력 비활성화(선택)
      if (msgInput) {
        msgInput.disabled = true;
        msgInput.placeholder = "로그인 후 이용할 수 있어요.";
      }
      if (sendBtn) sendBtn.disabled = true;
    }

    setupHandlers();
    autoResize(msgInput);
    msgInput?.focus();
  });

  function setupHandlers() {
    if (sendBtn) sendBtn.addEventListener("click", onSend);
    if (msgInput) {
      msgInput.addEventListener("keydown", (e) => {
        if (e.key === "Enter" && !e.shiftKey) {
          e.preventDefault();
          onSend();
        }
      });
      msgInput.addEventListener("input", () => autoResize(msgInput));
    }
  }

  async function onSend() {
    const text = (msgInput?.value || "").trim();
    if (!text) return;

    appendMessage({ isMine: true, content: text, sentAt: new Date() });
    msgInput.value = "";
    autoResize(msgInput);

    const placeholder = appendMessage({
      isMine: false,
      content: "답변 생성 중...",
      sentAt: new Date(),
    });

    try {
      const payload = {
        message: text,
        temperature: parseTemp(ctxTempEl?.value),
        location: (ctxLocEl?.value || "").trim() || null,
        date: (ctxDateEl?.value || "").trim() || null,
        lat: parseNum(ctxLatEl?.value),
        lon: parseNum(ctxLonEl?.value),
      };

      const res = await fetch("/api/chatbot", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      // 로그인 미상태 등으로 401/403/500이어도 본문 메시지를 시도
      let textBody = "";
      try {
        textBody = await res.text();
      } catch {}
      let data;
      try {
        data = JSON.parse(textBody);
      } catch {}

      const answer = data?.answer || textBody || "(빈 응답)";
      replaceBubbleText(placeholder, String(answer));
    } catch (e) {
      replaceBubbleText(placeholder, "오류: " + (e?.message || "요청 실패"));
    }
  }

  function parseTemp(v) {
    if (!v) return null;
    const n = Number(v);
    return Number.isFinite(n) ? n : null;
  }
  function parseNum(v) {
    if (!v) return null;
    const n = Number(v);
    return Number.isFinite(n) ? n : null;
  }

  function appendMessage({ isMine, content, sentAt }) {
    const row = document.createElement("div");
    row.className = `message-row ${isMine ? "message-right" : "message-left"}`;

    const bubble = document.createElement("div");
    bubble.className = `bubble ${isMine ? "bubble-right" : "bubble-left"}`;
    bubble.textContent = content;
    row.appendChild(bubble);

    const time = document.createElement("div");
    time.className = "timestamp";
    time.textContent = formatTime(sentAt || new Date());
    row.appendChild(time);

    chatBox.appendChild(row);
    scrollToBottom();
    return bubble;
  }

  function replaceBubbleText(bubbleEl, text) {
    if (!bubbleEl) return;
    bubbleEl.textContent = text;
    scrollToBottom();
  }

  function appendBotWelcome() {
    appendMessage({
      isMine: false,
      content: "안녕하세요! TempFit 챗봇입니다. 무엇을 도와드릴까요?",
      sentAt: new Date(),
    });
  }

  function scrollToBottom() {
    if (!chatBox) return;
    chatBox.scrollTop = chatBox.scrollHeight;
  }

  function autoResize(el) {
    if (!el) return;
    el.style.height = "auto";
    el.style.height = el.scrollHeight + "px";
  }

  function formatTime(date) {
    const d = date instanceof Date ? date : new Date(date);
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, "0");
    const dd = String(d.getDate()).padStart(2, "0");
    const hh = String(d.getHours()).padStart(2, "0");
    const mi = String(d.getMinutes()).padStart(2, "0");
    return `${yyyy}-${mm}-${dd} ${hh}:${mi}`;
  }
})();

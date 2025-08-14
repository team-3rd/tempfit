// 채팅 버튼 푸터 위까지만
const toggleBtn = document.getElementById("chat-btn");
const offcanvas = document.getElementById("offcanvasBottom");

window.addEventListener("scroll", () => {
  const footer = document.querySelector("footer");

  if (!footer) return; // footer 없으면 무시

  const footerTop = footer.getBoundingClientRect().top + window.scrollY;
  const windowBottom = window.scrollY + window.innerHeight;

  if (toggleBtn.style.bottom != "432px") {
    if (windowBottom > footerTop) {
      const overlap = windowBottom - footerTop;
      toggleBtn.style.bottom = `${overlap}px`;
    } else {
      toggleBtn.style.bottom = "0";
    }
  } else {
    return;
  }
});

// 채팅 버튼 채팅창 열면 위로, 채팅창 열림 / 닫힘 시 아이콘 변경
const chatToggle = document.querySelector(".chatToggle");

offcanvas.addEventListener("show.bs.offcanvas", function () {
  toggleBtn.style.bottom = `432px`;

  chatToggle.classList.add("bi-chevron-double-down");
  chatToggle.classList.remove("bi-chevron-double-up");

  if (document.querySelector(".backBtn")) {
    document.querySelector(".backBtn").removeAttribute("hidden");
  }
});
offcanvas.addEventListener("hide.bs.offcanvas", function () {
  toggleBtn.style.bottom = `0`;

  chatToggle.classList.add("bi-chevron-double-up");
  chatToggle.classList.remove("bi-chevron-double-down");

  if (document.querySelector(".backBtn")) {
    document.querySelector(".backBtn").setAttribute("hidden", "");
  }
});

document.addEventListener("DOMContentLoaded", function () {
  chatFunc();
});

function chatFunc() {
  const chatContainer = document.querySelector(".offcanvas-body");
  const chatTitle = document.querySelector(".chatTitle");
  let chatUrl = null;

  fetch(`/messages/chat`)
    .then((res) => {
      return res.text();
    })
    .then((html) => {
      if (chatContainer) {
        chatContainer.innerHTML = "";
        chatContainer.innerHTML = html;

        chatTitle.innerHTML = `<i class="bi bi-chat-dots"></i>`;
        const chatList = document.querySelectorAll(".list-group-item span");

        chatList.forEach((e) => {
          e.addEventListener("click", function (i) {
            i.preventDefault();

            chatUrl = i.currentTarget.getAttribute("data-url");

            chatTitle.innerHTML =
              `<i class="backBtn bi bi-arrow-left"></i>` +
              i.currentTarget.textContent;

            fetch(chatUrl)
              .then((res) => {
                return res.text();
              })
              .then((data) => {
                chatContainer.innerHTML = "";
                chatContainer.innerHTML = data;

                const backBtn = document.querySelector(".backBtn");
                const offcanvasb = new bootstrap.Offcanvas("#offcanvasBottom");

                backBtn.addEventListener(
                  "click",
                  function (btn) {
                    btn.stopPropagation();
                    offcanvasb.show();
                    chatFunc();
                  },
                  true
                );

                if (chatUrl != "/chatbot/frag") {
                  // 채팅 기능 코드
                  let currentUser = null;
                  let targetUser = null;

                  if (document.querySelector(".chat-container")) {
                    currentUser = document.getElementById("senderEmail").value;
                    targetUser = document.getElementById("receiverEmail").value;
                  }
                  let stompClient = null;

                  const chatBox = document.getElementById("chatBox");

                  // ✅ 스크롤을 가장 아래로 이동
                  if (chatBox) {
                    chatBox.scrollTop = chatBox.scrollHeight;
                  }

                  function connectWebSocket() {
                    const socket = new SockJS("/ws");
                    stompClient = Stomp.over(socket);

                    stompClient.connect({}, function (frame) {
                      console.log("Connected: " + frame);

                      stompClient.subscribe(
                        "/user/queue/messages",
                        function (msg) {
                          const message = JSON.parse(msg.body);
                          showMessage(message);
                        }
                      );
                    });
                  }

                  function showMessage(message) {
                    const box = document.getElementById("chatBox");

                    const div = document.createElement("div");
                    div.className =
                      message.sender === currentUser
                        ? "message-right"
                        : "message-left";
                    div.innerHTML = `
                  <div class="message">${message.content}</div>
                  <div class="text-muted small">${new Date(
                    message.sentAt
                  ).toLocaleString()}</div>
                `;

                    box.appendChild(div);
                    box.scrollTop = box.scrollHeight;
                  }

                  function sendMessage() {
                    const contentInput = document.getElementById("msgInput");
                    const content = contentInput.value.trim();
                    if (!content) return;

                    const message = {
                      sender: currentUser,
                      receiver: targetUser,
                      content: content,
                      sentAt: new Date().toISOString(),
                    };

                    stompClient.send(
                      "/app/chat.send",
                      {},
                      JSON.stringify(message)
                    );

                    contentInput.value = "";
                  }

                  const sendBtn = document.getElementById("sendBtn");
                  if (sendBtn) {
                    sendBtn.addEventListener("click", function () {
                      sendMessage();
                    });
                  }

                  connectWebSocket();
                } else {
                  // 챗봇
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

                  // lat/lon 미지정이면 브라우저 Geolocation으로 채우기
                  if (
                    (!ctxLatEl.value || !ctxLonEl.value) &&
                    navigator.geolocation
                  ) {
                    navigator.geolocation.getCurrentPosition(
                      (pos) => {
                        ctxLatEl.value = String(pos.coords.latitude);
                        ctxLonEl.value = String(pos.coords.longitude);
                      },
                      () => {
                        /* 무시: 위치 거부 시 서버가 안내 */
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

                  function setupHandlers() {
                    if (sendBtn) sendBtn.addEventListener("click", onSend);
                    if (msgInput) {
                      msgInput.addEventListener("keydown", (e) => {
                        if (e.key === "Enter" && !e.shiftKey) {
                          e.preventDefault();
                          onSend();
                        }
                      });
                      msgInput.addEventListener("input", () =>
                        autoResize(msgInput)
                      );
                    }
                  }

                  async function onSend() {
                    const text = (msgInput?.value || "").trim();
                    if (!text) return;

                    appendMessage({
                      isMine: true,
                      content: text,
                      sentAt: new Date(),
                    });
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
                      replaceBubbleText(
                        placeholder,
                        "오류: " + (e?.message || "요청 실패")
                      );
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
                    row.className = `message-row ${
                      isMine ? "message-right" : "message-left"
                    }`;

                    const bubble = document.createElement("div");
                    bubble.className = `bubble ${
                      isMine ? "bubble-right" : "bubble-left"
                    }`;
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
                      content:
                        "안녕하세요! TempFit 챗봇입니다. 무엇을 도와드릴까요?",
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
                }
              })
              .catch((err) => console.error(err));
          });
        });
      }
    })
    .catch((err) => console.error(err));
}

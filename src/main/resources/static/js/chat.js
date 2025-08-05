document.addEventListener("DOMContentLoaded", function () {
  const currentUser = document.getElementById("senderEmail").value;
  const targetUser = document.getElementById("receiverEmail").value;
  let stompClient = null;

  function connectWebSocket() {
    const socket = new SockJS("/ws");
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
      console.log("Connected: " + frame);

      stompClient.subscribe(`/user/${currentUser}/topic/messages`, function (msg) {
        const message = JSON.parse(msg.body);
        showMessage(message);
      });
    });
  }

  function showMessage(message) {
    const box = document.getElementById("chatBox");

    const div = document.createElement("div");
    div.className = message.sender === currentUser ? "message-right" : "message-left";
    div.innerHTML = `
      <div class="message">${message.content}</div>
      <div class="text-muted small">${new Date(message.sentAt).toLocaleString()}</div>
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
      sentAt: new Date().toISOString()
    };

    stompClient.send("/app/chat.send", {}, JSON.stringify(message));

    contentInput.value = '';
    showMessage(message);
  }

  const sendBtn = document.getElementById("sendBtn");
  if (sendBtn) {
    sendBtn.addEventListener("click", function () {
      sendMessage();
    });
  }

  connectWebSocket();
});

// static/js/detail.js
(function(){
  function kFormat(n){
    if (n >= 1000){
      const v = Math.round((n/1000)*10)/10;
      return (v % 1 === 0 ? v.toFixed(0) : v.toFixed(1)) + 'k';
    }
    return String(n);
  }

  function relTime(iso){
    try{
      const t = new Date(String(iso).replace(' ', 'T'));
      const now = new Date();
      const diff = now - t;
      const mins = Math.floor(diff/60000);
      if (mins < 60) return (mins<=0?1:mins) + '분 전';
      const hrs = Math.floor(mins/60);
      if (hrs < 24) return hrs + '시간 전';
      const days = Math.floor(hrs/24);
      if (days < 30) return days + '일 전';
      const m = t.getMonth()+1, d = t.getDate();
      return (m<10?'0'+m:m) + '-' + (d<10?'0'+d:d);
    }catch(e){ return ''; }
  }

  // 외부에서 호출: fragment 주입 직후 이벤트 바인딩
  window.initDetailModal = function(root){
    const ctx = root || document;

    // 상대시간
    ctx.querySelectorAll('.js-rel').forEach(el=>{
      const iso = el.getAttribute('data-time');
      if (iso) el.textContent = relTime(iso);
    });

    // 카운트
    const likeNode = ctx.querySelector('#igLikeCount');
    const cmtNode  = ctx.querySelector('#igCommentCount');
    if (likeNode){
      const n = parseInt(likeNode.getAttribute('data-count')||'0',10);
      likeNode.textContent = kFormat(n);
    }
    if (cmtNode){
      const n = parseInt(cmtNode.getAttribute('data-count')||'0',10);
      cmtNode.textContent = kFormat(n);
    }

    // 댓글 버튼 → 입력 포커스
    const chatBtn = ctx.querySelector('.btn-chat');
    const input   = ctx.querySelector('.ig-textarea');
    if (chatBtn && input){
      chatBtn.addEventListener('click', ()=> input.focus());
    }

    // 좋아요 토글
    const likeBtn = ctx.querySelector('.btn-like');
    if (likeBtn && likeNode){
      likeBtn.addEventListener('click', async ()=>{
        const id = likeBtn.getAttribute('data-id');
        const icon = likeBtn.querySelector('i');
        const isOn = icon.classList.contains('bi-heart-fill');
        try{
          await fetch(`/community/recommend/${id}`, {method:'POST', credentials:'same-origin'});
        }catch(e){ /* 네트워크 오류여도 UI는 낙관적 반영 */ }
        icon.classList.toggle('bi-heart-fill', !isOn);
        icon.classList.toggle('bi-heart', isOn);
        let n = parseInt(likeNode.getAttribute('data-count')||'0',10);
        n = isOn ? Math.max(0,n-1) : n+1;
        likeNode.setAttribute('data-count', n);
        likeNode.textContent = kFormat(n);
      });
    }

    // 북마크 토글
    const bmBtn = ctx.querySelector('.btn-bookmark');
    if (bmBtn){
      bmBtn.addEventListener('click', async ()=>{
        const id = bmBtn.getAttribute('data-id');
        const icon = bmBtn.querySelector('i');
        const isOn = icon.classList.contains('bi-bookmark-fill');
        try{
          await fetch(`/community/bookmark/${id}`, {method:'POST', credentials:'same-origin'});
        }catch(e){ /* 무시 */ }
        icon.classList.toggle('bi-bookmark-fill', !isOn);
        icon.classList.toggle('bi-bookmark', isOn);
      });
    }
  };
})();

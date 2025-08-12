// 북마크 이미지 호버
const bookmark = document.querySelectorAll(".bookmark");

bookmark.forEach((e) => {
  e.addEventListener("mouseenter", function () {
    e.classList.add("bi-bookmark-fill");
    e.classList.remove("bi-bookmark");
  });

  e.addEventListener("mouseleave", function () {
    e.classList.add("bi-bookmark");
    e.classList.remove("bi-bookmark-fill");
  });
});

document.querySelectorAll(".post-card").forEach((post) => {
  post.addEventListener("click", function (e) {
    e.preventDefault();
    const postId = this.dataset.id;

    fetch(`/community/detail/${postId}`)
      .then((res) => res.text())
      .then((data) => {
        document.getElementById("modal-fragment").innerHTML = data;
        new bootstrap.Modal(document.getElementById("detailModal")).show();
      });
  });
});

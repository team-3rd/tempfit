/* weatherTheme.js
   - 날씨 텍스트(#weather-sky, .sky, [data-weather-sky]) 감시 → body에 weather-XXX 클래스 적용
   - 비(weather-rain) / 눈(weather-snow) DOM 오버레이 (캔버스 미사용)
   - 빗방울/눈송이는 footer 윗선에서 splat 후 소멸
   - 외부 제어:
       window.applyWeatherThemeByText("맑음"|"흐림"|"비"|"눈")
       window.weatherRain.setOptions({ splat:true|false, backRow:true|false, single:false|true, density:100 })
       window.weatherRain.remake()
       window.weatherSnow.setOptions({ splat:true|false, backRow:true|false, single:false|true, density:120, minSize:6, maxSize:12 })
       window.weatherSnow.remake()
*/

(function () {
  "use strict";

  const THEMES = ["weather-clear", "weather-cloudy", "weather-rain", "weather-snow"];
  const BODY = document.body;
  const DOC_EL = document.documentElement;

  // ────────────────────────────────────────────────────────────────────────────
  // 공통: footer 윗선(ground) 계산 유틸
  // ────────────────────────────────────────────────────────────────────────────
  function detectFooterEl() {
    return (
      document.querySelector("footer") ||
      document.querySelector("#footer") ||
      document.querySelector(".footer") ||
      document.querySelector("[data-footer]") ||
      null
    );
  }
  function calcGroundVars(prefix, footerEl) {
    const h = footerEl ? footerEl.offsetHeight : 0;
    DOC_EL.style.setProperty(`--${prefix}-ground-offset`, `${h}px`);
    const fallPx = Math.max(0, Math.floor(window.innerHeight - h - 2));
    DOC_EL.style.setProperty(`--${prefix}-fall-distance`, `${fallPx}px`);
  }

  // ────────────────────────────────────────────────────────────────────────────
  // Rain overlay manager (vanilla JS)
  // ────────────────────────────────────────────────────────────────────────────
  const RAIN = {
    overlayId: "weather-rain-overlay",
    defaultOptions: {
      splat: true,
      backRow: true,
      single: false,
      density: 100
    },
    opts: null,
    footerEl: null,
    footerObserver: null,
    resizeHandler: null,

    ensureOverlay() {
      let wrap = document.getElementById(this.overlayId);
      if (!wrap) {
        wrap = document.createElement("div");
        wrap.id = this.overlayId;
        wrap.className = "weather-rain-overlay";

        const front = document.createElement("div");
        front.className = "rain front-row";

        const back = document.createElement("div");
        back.className = "rain back-row";

        wrap.appendChild(front);
        wrap.appendChild(back);
        document.body.appendChild(wrap);
      }
      return wrap;
    },

    clearOverlay() {
      const wrap = document.getElementById(this.overlayId);
      if (wrap && wrap.parentNode) wrap.parentNode.removeChild(wrap);
    },

    _applyToggleClasses() {
      BODY.classList.toggle("splat-toggle", !!this.opts.splat);
      BODY.classList.toggle("back-row-toggle", !!this.opts.backRow);
      BODY.classList.toggle("single-toggle", !!this.opts.single);
    },

    setOptions(next) {
      this.opts = Object.assign({}, this.opts || this.defaultOptions, next || {});
      this._applyToggleClasses();
    },

    _empty(el) { while (el.firstChild) el.removeChild(el.firstChild); },

    _makeDrops() {
      const wrap = this.ensureOverlay();
      const front = wrap.querySelector(".rain.front-row");
      const back = wrap.querySelector(".rain.back-row");
      if (!front || !back) return;

      this._empty(front);
      this._empty(back);

      let increment = 0;
      const max = Math.max(1, this.opts.density);

      while (increment < max) {
        const randoHundo = Math.floor(Math.random() * 98) + 1; // 1~98
        const randoFiver = Math.floor(Math.random() * 4) + 2;  // 2~5
        increment += randoFiver;

        // front
        const dropF = document.createElement("div");
        dropF.className = "drop";
        dropF.style.left = increment + "%";
        dropF.style.bottom = (randoFiver + randoFiver - 1 + 100) + "%";
        dropF.style.animationDelay = "0." + randoHundo + "s";
        dropF.style.animationDuration = "0.5" + randoHundo + "s";

        const stemF = document.createElement("div");
        stemF.className = "stem";
        stemF.style.animationDelay = "0." + randoHundo + "s";
        stemF.style.animationDuration = "0.5" + randoHundo + "s";

        const splatF = document.createElement("div");
        splatF.className = "splat";
        splatF.style.animationDelay = "0." + randoHundo + "s";
        splatF.style.animationDuration = "0.5" + randoHundo + "s";

        dropF.appendChild(stemF);
        dropF.appendChild(splatF);
        front.appendChild(dropF);

        // back
        const dropB = document.createElement("div");
        dropB.className = "drop";
        dropB.style.right = increment + "%";
        dropB.style.bottom = (randoFiver + randoFiver - 1 + 100) + "%";
        dropB.style.animationDelay = "0." + randoHundo + "s";
        dropB.style.animationDuration = "0.5" + randoHundo + "s";

        const stemB = document.createElement("div");
        stemB.className = "stem";
        stemB.style.animationDelay = "0." + randoHundo + "s";
        stemB.style.animationDuration = "0.5" + randoHundo + "s";

        const splatB = document.createElement("div");
        splatB.className = "splat";
        splatB.style.animationDelay = "0." + randoHundo + "s";
        splatB.style.animationDuration = "0.5" + randoHundo + "s";

        dropB.appendChild(stemB);
        dropB.appendChild(splatB);
        back.appendChild(dropB);
      }

      if (this.opts.single) {
        const drops = wrap.querySelectorAll(".drop");
        drops.forEach((d, i) => { if (i !== 9) d.style.display = "none"; });
      }

      const backRowEl = wrap.querySelector(".rain.back-row");
      if (backRowEl) backRowEl.style.display = this.opts.backRow ? "block" : "none";
    },

    updateGround() {
      this.footerEl = detectFooterEl();
      calcGroundVars("rain", this.footerEl);
      const wrap = this.ensureOverlay();
      const h = this.footerEl ? this.footerEl.offsetHeight : 0;
      wrap.style.bottom = `${h}px`;
    },

    _attachObservers() {
      this.resizeHandler = () => { this.updateGround(); this.remake(); };
      window.addEventListener("resize", this.resizeHandler);

      const f = this.footerEl;
      if (f && "ResizeObserver" in window) {
        this.footerObserver = new ResizeObserver(() => { this.updateGround(); this.remake(); });
        this.footerObserver.observe(f);
      }
    },

    _detachObservers() {
      if (this.resizeHandler) { window.removeEventListener("resize", this.resizeHandler); this.resizeHandler = null; }
      if (this.footerObserver) { this.footerObserver.disconnect(); this.footerObserver = null; }
    },

    mount() {
      if (!this.opts) this.opts = Object.assign({}, this.defaultOptions);
      this._applyToggleClasses();
      this.ensureOverlay();
      this.updateGround();
      this._makeDrops();
      this._attachObservers();
    },

    unmount() {
      this._detachObservers();
      BODY.classList.remove("splat-toggle", "back-row-toggle", "single-toggle");
      this.clearOverlay();
    },

    remake() {
      const wrap = document.getElementById(this.overlayId);
      if (!wrap) return;
      this._makeDrops();
    }
  };

  window.weatherRain = {
    setOptions: (o) => RAIN.setOptions(o),
    remake: () => RAIN.remake(),
    updateGround: () => RAIN.updateGround()
  };

  // ────────────────────────────────────────────────────────────────────────────
  // Snow overlay manager (vanilla JS) — 수직 낙하만 (흩날림/회전 제거)
  // ────────────────────────────────────────────────────────────────────────────
  const SNOW = {
    overlayId: "weather-snow-overlay",
    defaultOptions: {
      splat: true,
      backRow: true,
      single: false,
      density: 120,
      minSize: 6,
      maxSize: 12
    },
    opts: null,
    footerEl: null,
    footerObserver: null,
    resizeHandler: null,

    ensureOverlay() {
      let wrap = document.getElementById(this.overlayId);
      if (!wrap) {
        wrap = document.createElement("div");
        wrap.id = this.overlayId;
        wrap.className = "weather-snow-overlay";

        const front = document.createElement("div");
        front.className = "snow front-row";

        const back = document.createElement("div");
        back.className = "snow back-row";

        wrap.appendChild(front);
        wrap.appendChild(back);
        document.body.appendChild(wrap);
      }
      return wrap;
    },

    clearOverlay() {
      const wrap = document.getElementById(this.overlayId);
      if (wrap && wrap.parentNode) wrap.parentNode.removeChild(wrap);
    },

    _applyToggleClasses() {
      BODY.classList.toggle("splat-toggle", !!this.opts.splat);
      BODY.classList.toggle("back-row-toggle", !!this.opts.backRow);
      BODY.classList.toggle("single-toggle", !!this.opts.single);
    },

    setOptions(next) {
      this.opts = Object.assign({}, this.opts || this.defaultOptions, next || {});
      this._applyToggleClasses();
    },

    _empty(el) { while (el.firstChild) el.removeChild(el.firstChild); },

    _rand(min, max) { return Math.random() * (max - min) + min; },

    _makeFlake(isBackRow, leftOrRightPercent) {
      const size = Math.round(this._rand(this.opts.minSize, this.opts.maxSize) * (isBackRow ? 0.85 : 1));
      const fallDur = (this._rand(8, 14) * (isBackRow ? 1.15 : 1)).toFixed(2) + "s";
      const delay = (Math.random() * 2).toFixed(2) + "s";

      const flake = document.createElement("div");
      flake.className = "flake";
      flake.style.setProperty("--flake-size", size + "px");
      flake.style.setProperty("--fall-dur", fallDur);
      flake.style.animationDelay = delay;
      if (isBackRow) flake.classList.add("is-back");

      // 수직 낙하만 → 좌우 배치만 초기값으로 주고 움직이지 않음
      if (leftOrRightPercent.side === "left") {
        flake.style.left = leftOrRightPercent.value + "%";
      } else {
        flake.style.right = leftOrRightPercent.value + "%";
      }

      const inner = document.createElement("div");
      inner.className = "flake-inner";
      inner.style.animationDelay = delay;

      // ＊ 모양: 0°, 60°, 120° (회전 제거)
      [0, 60, 120].forEach((deg) => {
        const arm = document.createElement("div");
        arm.className = "arm";
        arm.style.transform = `translate(-50%, -50%) rotate(${deg}deg)`;
        inner.appendChild(arm);
      });

      // 바닥 스플랫
      const splat = document.createElement("div");
      splat.className = "splat-snow";
      splat.style.animationDuration = fallDur;
      inner.appendChild(splat);

      flake.appendChild(inner);
      return flake;
    },

    _makeFlakes() {
      const wrap = this.ensureOverlay();
      const front = wrap.querySelector(".snow.front-row");
      const back = wrap.querySelector(".snow.back-row");
      if (!front || !back) return;

      this._empty(front);
      this._empty(back);

      let increment = 0;
      const max = Math.max(1, this.opts.density);

      while (increment < max) {
        const step = Math.floor(Math.random() * 3) + 1; // 1~3
        increment += step;

        const pos = { side: "left", value: increment };
        const posBack = { side: "right", value: increment };

        const flakeF = this._makeFlake(false, pos);
        const flakeB = this._makeFlake(true, posBack);

        front.appendChild(flakeF);
        back.appendChild(flakeB);
      }

      if (this.opts.single) {
        const flakes = wrap.querySelectorAll(".flake");
        flakes.forEach((d, i) => { if (i !== 9) d.style.display = "none"; });
      }

      const backRowEl = wrap.querySelector(".snow.back-row");
      if (backRowEl) backRowEl.style.display = this.opts.backRow ? "block" : "none";
    },

    updateGround() {
      this.footerEl = detectFooterEl();
      calcGroundVars("snow", this.footerEl);
      const wrap = this.ensureOverlay();
      const h = this.footerEl ? this.footerEl.offsetHeight : 0;
      wrap.style.bottom = `${h}px`;
    },

    _attachObservers() {
      this.resizeHandler = () => { this.updateGround(); this.remake(); };
      window.addEventListener("resize", this.resizeHandler);

      const f = this.footerEl;
      if (f && "ResizeObserver" in window) {
        this.footerObserver = new ResizeObserver(() => { this.updateGround(); this.remake(); });
        this.footerObserver.observe(f);
      }
    },

    _detachObservers() {
      if (this.resizeHandler) { window.removeEventListener("resize", this.resizeHandler); this.resizeHandler = null; }
      if (this.footerObserver) { this.footerObserver.disconnect(); this.footerObserver = null; }
    },

    mount() {
      if (!this.opts) this.opts = Object.assign({}, this.defaultOptions);
      this._applyToggleClasses();
      this.ensureOverlay();
      this.updateGround();
      this._makeFlakes();
      this._attachObservers();
    },

    unmount() {
      this._detachObservers();
      BODY.classList.remove("splat-toggle", "back-row-toggle", "single-toggle");
      this.clearOverlay();
    },

    remake() {
      const wrap = document.getElementById(this.overlayId);
      if (!wrap) return;
      this._makeFlakes();
    }
  };

  window.weatherSnow = {
    setOptions: (o) => SNOW.setOptions(o),
    remake: () => SNOW.remake(),
    updateGround: () => SNOW.updateGround()
  };

  // ────────────────────────────────────────────────────────────────────────────
  // Theme applier
  // ────────────────────────────────────────────────────────────────────────────
  function applyTheme(theme) {
    THEMES.forEach(c => BODY.classList.remove(c));
    BODY.classList.add(theme);

    if (theme === "weather-rain") {
      SNOW.unmount();
      RAIN.mount();
    } else if (theme === "weather-snow") {
      RAIN.unmount();
      SNOW.mount();
    } else {
      RAIN.unmount();
      SNOW.unmount();
    }
  }

  function mapTextToTheme(text) {
    const t = (text || "").toLowerCase();
    if (/(비|소나기|rain|shower)/.test(t)) return "weather-rain";
    if (/(눈|진눈깨비|snow|blizzard)/.test(t)) return "weather-snow";
    if (/(흐림|흐리|구름|cloud|overcast)/.test(t)) return "weather-cloudy";
    if (/(맑|sun|clear)/.test(t)) return "weather-clear";
    return "weather-clear";
  }

  function updateFromDom() {
    const el = document.querySelector("#weather-sky, .sky, [data-weather-sky]");
    if (!el) return;
    const txt = (el.textContent || el.innerText || "").trim();
    applyTheme(mapTextToTheme(txt));
  }

  function attachObserver() {
    const el = document.querySelector("#weather-sky, .sky, [data-weather-sky]");
    if (!el) return false;
    const obs = new MutationObserver(updateFromDom);
    obs.observe(el, { childList: true, subtree: true, characterData: true });
    updateFromDom();
    return true;
  }

  document.addEventListener("DOMContentLoaded", function () {
    if (!attachObserver()) {
      const int = setInterval(() => { if (attachObserver()) clearInterval(int); }, 300);
      setTimeout(() => clearInterval(int), 20000);
    }
  });

  // 수동 제어
  window.applyWeatherThemeByText = function (text) {
    applyTheme(mapTextToTheme(text));
  };
})();

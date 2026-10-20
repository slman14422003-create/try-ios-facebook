(function () {
  'use strict';
  if (window.__iosfb || !document.documentElement) return;
  window.__iosfb = true;

  var HIDE_ATTR = 'data-iosfb-hide';

  // Legacy selectors (best effort) – the heuristic below handles the modern site.
  var KNOWN = ['#header', '#MChromeHeader', '#mJewelNav', '[data-sigil~="MTopBlueBarHeader"]'];

  function root() { return document.head || document.documentElement; }

  function addStyle() {
    if (document.getElementById('iosfb-style')) return;
    var s = document.createElement('style');
    s.id = 'iosfb-style';
    s.textContent = window.__IOSFB_CSS || '';
    root().appendChild(s);
  }

  function fixViewport() {
    var m = document.querySelector('meta[name="viewport"]');
    if (!m) { m = document.createElement('meta'); m.name = 'viewport'; root().appendChild(m); }
    m.content = 'width=device-width, initial-scale=1, maximum-scale=1, viewport-fit=cover';
  }

  function mark(el) { el.setAttribute(HIDE_ATTR, '1'); }

  // A full-width, short, fixed/sticky bar glued to the top or bottom edge.
  function isEdgeBar(el) {
    var r = el.getBoundingClientRect();
    if (r.height < 1 || r.height > 150 || r.width < window.innerWidth * 0.9) return false;
    var pos = getComputedStyle(el).position;
    if (pos !== 'fixed' && pos !== 'sticky') return false;
    return r.top <= 4 || Math.abs(r.bottom - window.innerHeight) <= 4;
  }

  function hideChrome() {
    KNOWN.forEach(function (sel) {
      document.querySelectorAll(sel).forEach(mark);
    });
    document.querySelectorAll('[role="banner"],[role="navigation"],header,nav').forEach(function (el) {
      if (!el.hasAttribute(HIDE_ATTR) && isEdgeBar(el)) mark(el);
    });
  }

  var timer = null;
  function schedule() {
    if (timer) return;
    timer = setTimeout(function () {
      timer = null;
      addStyle();
      hideChrome();
    }, 120);
  }

  addStyle();
  fixViewport();
  hideChrome();
  new MutationObserver(schedule).observe(document.documentElement, { childList: true, subtree: true });
  window.addEventListener('resize', schedule);
})();

(function () {
  'use strict';
  if (window.__iosfb || !document.documentElement) return;
  window.__iosfb = true;

  var HIDE_ATTR = 'data-iosfb-hide';

  // Legacy selectors (best effort).
  var KNOWN = ['#header', '#MChromeHeader', '#mJewelNav', '[data-sigil~="MTopBlueBarHeader"]'];

  // "Open app" style promos (English + Arabic). Compared against the element's whole text.
  var APP_PROMO = [
    'open app', 'open in app', 'open the app', 'use the app', 'get app', 'get the app', 'install app',
    'فتح التطبيق', 'افتح التطبيق', 'استخدم التطبيق', 'تثبيت التطبيق', 'تنزيل التطبيق', 'احصل على التطبيق'
  ];

  // aria-labels / alt of the web header and its icon row (logo, menu, search, jewels).
  var CHROME_LABELS = [
    'facebook', 'menu', 'search', 'home', 'friends', 'watch', 'reels', 'video', 'marketplace',
    'notifications', 'messenger', 'messages', 'profile', 'groups',
    'القائمة', 'بحث', 'الرئيسية', 'الأصدقاء', 'ريلز', 'ماسنجر', 'الإشعارات', 'الرسائل',
    'الصفحة الشخصية', 'المجموعات', 'ماركت بليس', 'فيسبوك'
  ];

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

  function mark(el) { if (el && el.setAttribute) el.setAttribute(HIDE_ATTR, '1'); }

  function norm(t) { return (t || '').replace(/\s+/g, ' ').trim().toLowerCase(); }

  // Never hide anything the user types into (comment bars, composers, chat inputs).
  function hasInput(el) {
    return !!el.querySelector('input,textarea,[contenteditable="true"],[role="textbox"],video');
  }

  function pageTop(el) { return el.getBoundingClientRect().top + (window.scrollY || 0); }

  // A full-width, short, fixed/sticky bar glued to the top or bottom edge.
  function isEdgeBar(el) {
    var r = el.getBoundingClientRect();
    if (r.height < 1 || r.height > 150 || r.width < window.innerWidth * 0.9) return false;
    var pos = getComputedStyle(el).position;
    if (pos !== 'fixed' && pos !== 'sticky') return false;
    return r.top <= 4 || Math.abs(r.bottom - window.innerHeight) <= 4;
  }

  // Highest ancestor that is still a short full-width row near the top of the page.
  function topRowOf(el) {
    var best = null, cur = el, depth = 0;
    while (cur && cur !== document.body && cur !== document.documentElement && depth < 12) {
      var r = cur.getBoundingClientRect();
      if (r.height > 170) break;
      if (r.width >= window.innerWidth * 0.9 && pageTop(cur) < 320) best = cur;
      cur = cur.parentElement; depth++;
    }
    return best;
  }

  function hideTagBars() {
    document.querySelectorAll('[role="banner"],[role="navigation"],header,nav').forEach(function (el) {
      if (!el.hasAttribute(HIDE_ATTR) && isEdgeBar(el) && !hasInput(el)) mark(el);
    });
  }

  // Web header: logo + hamburger + search, and the icon row below it.
  function hideTopChrome() {
    var els = document.querySelectorAll('[aria-label],svg[role="img"] ,img[alt]');
    var n = Math.min(els.length, 500);
    var hits = [];
    for (var i = 0; i < n; i++) {
      var el = els[i];
      var label = norm(el.getAttribute('aria-label') || el.getAttribute('alt'));
      if (!label || CHROME_LABELS.indexOf(label) < 0) continue;
      if (pageTop(el) > 260) continue;
      if (el.closest('[role="main"],[role="feed"],article,[role="dialog"]')) continue;
      var row = topRowOf(el);
      if (row && !hasInput(row)) hits.push(row);
    }
    hits.forEach(mark);
  }

  // "Open app" banners / buttons.
  function hideAppPromos() {
    var cands = document.querySelectorAll('a,button,[role="button"],div[tabindex]');
    var n = Math.min(cands.length, 800);
    for (var i = 0; i < n; i++) {
      var el = cands[i];
      if (el.hasAttribute(HIDE_ATTR)) continue;
      var t = norm(el.textContent);
      if (!t || t.length > 40 || APP_PROMO.indexOf(t) < 0) continue;
      // Prefer the fixed/sticky wrapper (the bar), else the wrapper that holds only this button.
      var target = null, cur = el, depth = 0;
      while (cur && cur !== document.body && depth < 8) {
        var pos = getComputedStyle(cur).position;
        if (pos === 'fixed' || pos === 'sticky') { target = cur; break; }
        cur = cur.parentElement; depth++;
      }
      if (!target) {
        target = el;
        while (target.parentElement && target.parentElement !== document.body &&
               norm(target.parentElement.textContent) === t) target = target.parentElement;
      }
      if (!hasInput(target)) mark(target);
    }
  }

  function hideChrome() {
    KNOWN.forEach(function (sel) {
      document.querySelectorAll(sel).forEach(mark);
    });
    hideTagBars();
    hideTopChrome();
    hideAppPromos();
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

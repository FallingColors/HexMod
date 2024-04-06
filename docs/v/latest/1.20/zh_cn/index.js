"use strict";
import semver from 'https://cdn.jsdelivr.net/npm/semver@7.5.4/+esm';
// these are filled by Jinja
const BOOK_URL = "https://hexcasting.hexxy.media";
const VERSION = "latest/1.20";
const MINECRAFT_VERSION = "1.20.1";
const FULL_VERSION = "0.11.1.1.0rc7.dev23";
const LANG = "zh_cn";
const SHOW_DROPDOWN_MINECRAFT_VERSION = "true" === "true";
const DROPDOWN_MINECRAFT_TEMPLATE = "Minecraft {version}";
// https://stackoverflow.com/a/61634647
function replaceTemplate(template, replacements) {
  return template.replace(
    /{(\w+)}/g,
    (placeholderWithDelimiters, placeholderWithoutDelimiters) =>
    replacements.hasOwnProperty(placeholderWithoutDelimiters) ?
      replacements[placeholderWithoutDelimiters] : placeholderWithDelimiters
  );
}
let cycleNodes = [];
function hookLoad(elem) {
  elem.addEventListener("toggle", () => {
    cycleNodes = document.querySelectorAll(".details-collapsible[open] .cycle-textures");
    if (elem.hasAttribute("open")) {
      for (const child of cycleNodes) {
        setEnabledMultiTexture(child, cycleIndex);
      }
    }
  });
}
function hookToggle(elem) {
  const details = Array.from(
    document.querySelectorAll("details." + elem.dataset.target)
  );
  elem.addEventListener("click", () => {
    if (details.some((x) => x.open)) {
      details.forEach((x) => (x.open = false));
    } else {
      details.forEach((x) => (x.open = true));
    }
  });
}
const params = new URLSearchParams(document.location.search);
function hookSpoiler(elem) {
  if (params.get("nospoiler") !== null) {
    elem.classList.add("unspoilered");
  } else {
    const thunk = (ev) => {
      if (!elem.classList.contains("unspoilered")) {
        ev.preventDefault();
        ev.stopImmediatePropagation();
        elem.classList.add("unspoilered");
      }
      elem.removeEventListener("click", thunk);
    };
    elem.addEventListener("click", thunk);
    if (elem instanceof HTMLAnchorElement) {
      const href = elem.getAttribute("href");
      if (href.startsWith("#")) {
        elem.addEventListener("click", () =>
          document
            .getElementById(href.substring(1))
            .querySelector(".spoilered")
            .classList.add("unspoilered")
        );
      }
    }
  }
}
let startTime = null;
function hookSyncAnimations(elem) {
  elem.addEventListener("animationstart", (e) => {
    for (const anim of e.target.getAnimations()) {
      if (startTime == null) {
        startTime = anim.startTime;
      } else {
        anim.startTime = startTime;
      }
    }
  })
}
let currentGaslight = 0;
let intersectingGaslights = 0;
let lastLookTimeMs = 0;
function startGaslighting(timeMs) {
  let newGaslight = Math.round(20 * (timeMs - lastLookTimeMs) / 1000);
  if (newGaslight >= 40) {
    currentGaslight = newGaslight - 40;
  }
  for (const elem of gaslightNodes) {
    setEnabledMultiTexture(elem, currentGaslight);
  }
}
function stopGaslighting(timeMs) {
  lastLookTimeMs = timeMs;
  for (const elem of gaslightNodes) {
    setEnabledMultiTexture(elem, null);
  }
}
let isFirstIntersectionEvent = true;
function hookIntersectionObserver(entries) {
  const wasLooking = intersectingGaslights > 0;
  let earliestStartMs = Number.MAX_VALUE;
  let latestStopMs = 0;
  for (const entry of entries) {
    if (entry.isIntersecting) {
      intersectingGaslights++;
      earliestStartMs = Math.min(earliestStartMs, entry.time);
    } else {
      if (!isFirstIntersectionEvent) intersectingGaslights--;
      latestStopMs = Math.max(latestStopMs, entry.time);
    }
  }
  isFirstIntersectionEvent = false;
  intersectingGaslights = Math.max(intersectingGaslights, 0);
  const isLooking = intersectingGaslights > 0;
  if (!wasLooking && isLooking) {
    startGaslighting(earliestStartMs);
  } else if (wasLooking && !isLooking) {
    stopGaslighting(latestStopMs);
  }
}
function hookVisibilityChange() {
  const time = performance.now();
  if (document.visibilityState === "visible") {
    startGaslighting(time);
  } else {
    stopGaslighting(time);
  }
}
let gaslightNodes;
let cycleIndex = 0;
let cycleTimeoutID;
function setEnabledMultiTexture(elem, index) {
  Array.from(elem.children).forEach((child, i) => {
    if (index !== null && i === (index % elem.children.length)) {
      child.classList.add("multi-texture-active");
    } else {
      child.classList.remove("multi-texture-active");
    }
  });
}
function doCycleTexturesForever() {
  cycleIndex += 1;
  for (const elem of cycleNodes) {
    setEnabledMultiTexture(elem, cycleIndex);
  }
  cycleTimeoutID = setTimeout(doCycleTexturesForever, 2000);
}
// Creates an element in the form `<li><a href=${href}>${text}</a></li>`
function dropdownItem(text, href) {
  let a = document.createElement("a");
  a.href = href;
  a.textContent = text;
  let li = document.createElement("li");
  li.appendChild(a);
  return li;
}
function versionDropdownItem(sitemap, versionKey) {
  const {defaultPath, langPaths, markers, defaultLang} = sitemap[versionKey];
  const {version, full_version, minecraft_version} = markers[LANG] ?? markers[defaultLang];
  // link to the current language if available, else link to the default language
  let path;
  if (langPaths.hasOwnProperty(LANG)) {
    path = langPaths[LANG];
  } else {
    path = defaultPath;
  }
  const li = dropdownItem(version, BOOK_URL + path);
  if (full_version === FULL_VERSION && versionKey == VERSION && minecraft_version == MINECRAFT_VERSION) {
    li.classList.add("disabled");
  }
  return li;
}
function versionDropdownItems(sitemap, versions) {
  return versions.map((version) => (
    versionDropdownItem(sitemap, version)
  ));
}
function dropdownSeparator() {
  let li = document.createElement("li");
  li.className = "divider";
  li.setAttribute("role", "separator");
  return li;
}
function dropdownHeader(text) {
  let li = document.createElement("li");
  li.className = "dropdown-header";
  li.textContent = text;
  return li;
}
function dropdownSubmenu(text, items) {
  let li = dropdownItem(text, "#");
  li.className = "dropdown-submenu";
  let ul = document.createElement("ul");
  ul.className = "dropdown-menu";
  ul.append(...items);
  li.appendChild(ul);
  return li;
}
// Like array.filter(predicate), but also returns the items which didn't match the filter.
function partition(array, predicate) {
  let matched = [];
  let unmatched = [];
  array.forEach((value, index) => {
    if (predicate(value, index, array)) {
      matched.push(value);
    } else {
      unmatched.push(value);
    }
  });
  return [matched, unmatched];
}
function sortSitemapVersions(unsortedVersions) {
  let [versions, branches] = partition(unsortedVersions, (v) => semver.valid(v) != null);
  // branches ascending, versions descending
  // eg. ["dev", "main"], ["0.10.0", "0.9.0"]
  branches.sort();
  versions.sort(semver.rcompare);
  return [branches, versions];
}
// Fills the version dropdown menus and the "old version" message.
function addDropdowns(sitemap) {
  let isOldVersion, dropdownItems, langNames, langPaths;
  if (SHOW_DROPDOWN_MINECRAFT_VERSION) {
    let minecraftVersions = Object.keys(sitemap);
    minecraftVersions.sort(semver.rcompare);
    let [_, currentVersions] = sortSitemapVersions(Object.keys(sitemap[MINECRAFT_VERSION]));
    isOldVersion = currentVersions.slice(1).includes(VERSION);
    dropdownItems = [];
    for (const minecraftVersion of minecraftVersions) {
      const subSitemap = sitemap[minecraftVersion];
      const [branches, versions] = sortSitemapVersions(Object.keys(subSitemap));
      dropdownItems.push(
        dropdownHeader(replaceTemplate(
          DROPDOWN_MINECRAFT_TEMPLATE,
          {version: minecraftVersion},
        )),
        ...versionDropdownItems(subSitemap, versions),
      );
      // honestly, i shouldn't be allowed to write javascript
      if (branches.length > 1) {
        dropdownItems.push(
          dropdownSubmenu(
            "latest/...", // TODO: this really shouldn't be hardcoded
            versionDropdownItems(subSitemap, branches).map(item => {
              item.firstChild.textContent = item.firstChild.textContent.replace(/^latest\//, "");
              return item;
            }),
          ),
        );
      } else if (branches.length == 1) {
        dropdownItems.push(
          versionDropdownItem(subSitemap, branches[0]),
        );
      }
    }
    langNames = sitemap[MINECRAFT_VERSION][VERSION].langNames;
    langPaths = sitemap[MINECRAFT_VERSION][VERSION].langPaths;
  } else { // legacy sitemap, not using minecraft versions
    let [branches, versions] = sortSitemapVersions(Object.keys(sitemap));
    isOldVersion = versions.slice(1).includes(VERSION);
    dropdownItems = [
      ...versionDropdownItems(sitemap, versions),
      dropdownSeparator(),
      ...versionDropdownItems(sitemap, branches),
    ];
    langNames = sitemap[VERSION].langNames;
    langPaths = sitemap[VERSION].langPaths;
  }
  // reveal the "old version" message if this page is a version number, but not the latest one
  // this isn't a dropdown, but it's here since we have the data anyway
  if (isOldVersion) {
    document.getElementById("old-version-notice").classList.remove("hidden")
  }
  // versions
  document.getElementById("version-dropdown").append(...dropdownItems);
  // languages for the current version
  const langs = Object.keys(langPaths).sort();
  document.getElementById("lang-dropdown").append(
    ...langs.map((lang) => dropdownItem(langNames[lang], BOOK_URL + langPaths[lang])),
  );
  // return sitemap for chaining, i guess
  return sitemap
}
document.addEventListener("DOMContentLoaded", () => {
  // fetch the sitemap from the root and use it to generate the navbar
  fetch(`${BOOK_URL}/meta/${SHOW_DROPDOWN_MINECRAFT_VERSION ? "sitemap-minecraft" : "sitemap"}.json`)
    .then(r => r.json())
    .then(addDropdowns)
    .catch(e => console.error(e))
  document.querySelectorAll(".details-collapsible").forEach(hookLoad);
  document.querySelectorAll("a.toggle-link").forEach(hookToggle);
  document.querySelectorAll(".spoilered").forEach(hookSpoiler);
  document.querySelectorAll(".animated-sync").forEach(hookSyncAnimations);
  doCycleTexturesForever();
  $(function () {
    $('[data-toggle="tooltip"]').tooltip()
  });
  $(".cycle-textures > .texture")
  .on("mouseenter", () => { // start hover
    if (cycleTimeoutID != null) {
      clearTimeout(cycleTimeoutID);
    }
  })
  .on("mouseleave", () => { // stop hover
    cycleTimeoutID = setTimeout(doCycleTexturesForever, 1000);
  });
  gaslightNodes = document.querySelectorAll(".gaslight-textures");
  for (const elem of gaslightNodes) {
    setEnabledMultiTexture(elem, 0);
  }
  const observer = new IntersectionObserver(hookIntersectionObserver, {
    rootMargin: "32px 32px 32px 32px",
  });
  gaslightNodes.forEach((elem) => observer.observe(elem));
  document.addEventListener("visibilitychange", hookVisibilityChange);
});
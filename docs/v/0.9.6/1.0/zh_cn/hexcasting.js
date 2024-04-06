"use strict";
const speeds = [0, 0.25, 0.5, 1, 2, 4];
const scrollThreshold = 100;
const rfaQueue = [];
const colorCache = new Map();
function getColorRGB(ctx, str) {
  if (!colorCache.has(str)) {
    ctx.fillStyle = str;
    ctx.clearRect(0, 0, 1, 1);
    ctx.fillRect(0, 0, 1, 1);
    const imgData = ctx.getImageData(0, 0, 1, 1);
    colorCache.set(str, imgData.data);
  }
  return colorCache.get(str);
}
function startAngle(str) {
  switch (str) {
    case "east":
      return 0;
    case "north_east":
      return 1;
    case "north_west":
      return 2;
    case "west":
      return 3;
    case "south_west":
      return 4;
    case "south_east":
      return 5;
    default:
      return 0;
  }
}
function offsetAngle(str) {
  switch (str) {
    case "w":
      return 0;
    case "q":
      return 1;
    case "a":
      return 2;
    case "s":
      return 3;
    case "d":
      return 4;
    case "e":
      return 5;
    default:
      return -1;
  }
}
export function initializeElem(canvas) {
  const str = canvas.dataset.string;
  let angle = startAngle(canvas.dataset.start);
  const perWorld = canvas.dataset.perWorld === "True";
  // build geometry
  const points = [[0, 0]];
  let lastPoint = points[0];
  let minPoint = lastPoint,
    maxPoint = lastPoint;
  for (const ch of "w" + str) {
    const addAngle = offsetAngle(ch);
    if (addAngle < 0) continue;
    angle = (angle + addAngle) % 6;
    const trueAngle = (Math.PI / 3) * angle;
    const [lx, ly] = lastPoint;
    const newPoint = [lx + Math.cos(trueAngle), ly - Math.sin(trueAngle)];
    points.push(newPoint);
    lastPoint = newPoint;
    const [mix, miy] = minPoint;
    minPoint = [Math.min(mix, newPoint[0]), Math.min(miy, newPoint[1])];
    const [max, may] = maxPoint;
    maxPoint = [Math.max(max, newPoint[0]), Math.max(may, newPoint[1])];
  }
  const size = Math.min(canvas.width, canvas.height) * 0.8;
  const scale =
    size /
    Math.max(3, Math.max(maxPoint[1] - minPoint[1], maxPoint[0] - minPoint[0]));
  const center = [
    (minPoint[0] + maxPoint[0]) * 0.5,
    (minPoint[1] + maxPoint[1]) * 0.5,
  ];
  const truePoints = points.map((p) => [
    canvas.width * 0.5 + scale * (p[0] - center[0]),
    canvas.height * 0.5 + scale * (p[1] - center[1]),
  ]);
  let uniqPoints = [];
  l1: for (const point of truePoints) {
    for (const pt of uniqPoints) {
      if (
        Math.abs(point[0] - pt[0]) < 0.00001 &&
        Math.abs(point[1] - pt[1]) < 0.00001
      ) {
        continue l1;
      }
    }
    uniqPoints.push(point);
  }
  // rendering code
  const speed = 0.0025;
  const context = canvas.getContext("2d");
  const negaProgress = -3;
  let progress = 0;
  let scrollTimeout = 1e309;
  let speedLevel = 3;
  let speedIncrement = 0;
  function speedScale() {
    return speeds[speedLevel];
  }
  const style = getComputedStyle(canvas);
  const getProp = (n) => style.getPropertyValue(n);
  const tick = (dt) => {
    scrollTimeout += dt;
    if (canvas.offsetParent === null) return;
    const strokeStyle = getProp("--path-color");
    const strokeVisitedStyle = getProp("--visited-path-color");
    const startDotStyle = getProp("--start-dot-color");
    const dotStyle = getProp("--dot-color");
    const movDotStyle = getProp("--moving-dot-color");
    const strokeWidth = scale * +getProp("--line-scale");
    const dotRadius = scale * +getProp("--dot-scale");
    const movDotRadius = scale * +getProp("--moving-dot-scale");
    const pauseScale = scale * +getProp("--pausetext-scale");
    const bodyBg = scale * +getProp("--pausetext-scale");
    const darkMode = +getProp("--dark-mode");
    const bgColors = getColorRGB(
      context,
      getComputedStyle(document.body).backgroundColor
    );
    if (!perWorld) {
      progress +=
        speed * dt * (progress > 0 ? speedScale() : Math.sqrt(speedScale()));
    }
    if (progress >= truePoints.length - 1) {
      progress = negaProgress;
    }
    let ix = Math.floor(progress),
      frac = progress - ix,
      core = null,
      fadeColor = 0;
    if (ix < 0) {
      const rawFade = (2 * progress) / negaProgress - 1;
      fadeColor = 1 - Math.abs(rawFade);
      context.strokeStyle = rawFade > 0 ? strokeVisitedStyle : strokeStyle;
      ix = rawFade > 0 ? truePoints.length - 2 : 0;
      frac = +(rawFade > 0);
    } else {
      context.strokeStyle = strokeVisitedStyle;
    }
    const [lx, ly] = truePoints[ix];
    const [rx, ry] = truePoints[ix + 1];
    core = [lx + (rx - lx) * frac, ly + (ry - ly) * frac];
    context.clearRect(0, 0, canvas.width, canvas.height);
    context.beginPath();
    context.lineWidth = strokeWidth;
    context.moveTo(truePoints[0][0], truePoints[0][1]);
    for (let i = 1; i < ix + 1; i++) {
      context.lineTo(truePoints[i][0], truePoints[i][1]);
    }
    context.lineTo(core[0], core[1]);
    context.stroke();
    context.beginPath();
    context.strokeStyle = strokeStyle;
    context.moveTo(core[0], core[1]);
    for (let i = ix + 1; i < truePoints.length; i++) {
      context.lineTo(truePoints[i][0], truePoints[i][1]);
    }
    context.stroke();
    for (let i = 0; i < uniqPoints.length; i++) {
      context.beginPath();
      context.fillStyle = i == 0 && !perWorld ? startDotStyle : dotStyle;
      const radius = i == 0 && !perWorld ? movDotRadius : dotRadius;
      context.arc(uniqPoints[i][0], uniqPoints[i][1], radius, 0, 2 * Math.PI);
      context.fill();
    }
    if (!perWorld) {
      context.beginPath();
      context.fillStyle = movDotStyle;
      context.arc(core[0], core[1], movDotRadius, 0, 2 * Math.PI);
      context.fill();
    }
    if (fadeColor) {
      context.fillStyle = `rgba(${bgColors[0]}, ${bgColors[1]}, ${bgColors[2]}, ${fadeColor})`;
      context.fillRect(0, 0, canvas.width, canvas.height);
    }
    if (scrollTimeout <= 2000) {
      context.fillStyle = `rgba(200, 200, 200, ${
        (2000 - scrollTimeout) / 1000
      })`;
      context.font = `${pauseScale}px sans-serif`;
      context.fillText(
        // these variables are filled by Jinja
        // slightly scuffed, but it works for now
        speedScale() ? `${speedScale()}x` : "暂停",
        0.2 * scale,
        canvas.height - 0.2 * scale
      );
    }
  };
  rfaQueue.push(tick);
  // scrolling input
  if (!perWorld) {
    canvas.addEventListener("wheel", (ev) => {
      speedIncrement += ev.deltaY;
      const oldSpeedLevel = speedLevel;
      if (speedIncrement >= scrollThreshold) {
        speedLevel--;
      } else if (speedIncrement <= -scrollThreshold) {
        speedLevel++;
      }
      if (oldSpeedLevel != speedLevel) {
        speedIncrement = 0;
        speedLevel = Math.max(0, Math.min(speeds.length - 1, speedLevel));
        scrollTimeout = 0;
      }
      ev.preventDefault();
    });
  }
}
export function hookLoad(elem) {
  let init = false;
  const canvases = elem.querySelectorAll("canvas");
  elem.addEventListener("toggle", (a) => {
    if (!init) {
      canvases.forEach(initializeElem);
      init = true;
    }
  }, {once: true});
}
document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll(".details-collapsible").forEach(hookLoad);
  function tick(prevTime, time) {
    const dt = time - prevTime;
    for (const q of rfaQueue) {
      q(dt);
    }
    requestAnimationFrame((t) => tick(time, t));
  }
  requestAnimationFrame((t) => tick(t, t));
});
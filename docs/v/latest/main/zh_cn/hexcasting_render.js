// // @ts-check
// "uncomment" above to get typechecking to work, will error unless hex_renderer_javascript (npm) is installed
"use-strict";
// @ts-ignore
//imports the initializer function from the animated renderer
import { initializeElem } from "./hexcasting.js";
// @ts-ignore
//actual import of the renderer
//package here: https://www.npmjs.com/package/hex_renderer_javascript
import init_renderer, {draw_bound_pattern} from "https://cdn.jsdelivr.net/npm/hex_renderer_javascript@0.1.4/hex_renderer_javascript.js";
//initializes the WASM code used in the hex_renderer_javascript library
init_renderer();
//manually importing typedefs (used for ensuring the render options are formatted correctly)
/**
 * @typedef {import('hex_renderer_javascript').GridOptions} GridOptions
 * @typedef {import('hex_renderer_javascript').Color} Color
 * @typedef {import('hex_renderer_javascript').Intersections} Intersections
 * @typedef {import('hex_renderer_javascript').Point} Point
 * @typedef {import('hex_renderer_javascript').Lines} Lines
 */
//renders the patterns in a collapsible menu
//It reads the previous data of the images/animation, uses it to draw the new one
//and then it deletes the old
function render_collapsible(draw_options, collapsible, palette, class_name) {
  let patterns = Array.from(collapsible.getElementsByClassName("spell-viz"));
  for (let pat of patterns) {
    let img = pat;
    if (img.tagName != "IMG") {
      var attr = pat.attributes;
      img = new Image();
      img.setAttribute("data-start", attr["data-start"].value);
      img.setAttribute("data-string", attr["data-string"].value);
      img.setAttribute("width", attr["width"].value);
      img.setAttribute("height", attr["height"].value);
      img.setAttribute("data-per-world", attr["data-per-world"].value);
      //img.setAttribute("class", "spell-viz");
      img.innerHTML = pat.innerHTML;
      pat.parentElement?.appendChild(img);
      pat.remove()
    } else {
      URL.revokeObjectURL(img.src);
    }
    img.setAttribute("class", class_name + " pattern-settings spell-viz");
    let per_world = "True" == img.attributes["data-per-world"].value;
    let start_dir = img.attributes["data-start"].value;
    let pattern_str = img.attributes["data-string"].value;
    let width = img.attributes["width"].value;
    let height = img.attributes["height"].value;
    var pattern = {
      great_spell: per_world,
      direction: start_dir,
      angle_sigs: pattern_str,
    };
    let style = getStyles(img);
    let grid_options = draw_options(style, palette);
    let img_data = draw_bound_pattern(grid_options, pattern, 0.35, width, height);
    img.src = URL.createObjectURL(new Blob([img_data.buffer], { type: 'image/png' }))
  }
}
//last_load_func allows the previous event listeners to be removed
//that way we aren't doing unecessary work when you switch between a ton of options
let last_load_func = null;
//lazily renders all of the images with the chosen render options
//loads all of the currently open ones and then adds event listeners to open the rest when they're opened
function render_images(draw_options, palette, class_name) {
  let collapsibles = document.getElementsByClassName("details-collapsible");
  let load_func = (event) => {
    render_collapsible(draw_options, event.target, palette, class_name);
  }
  for (let collapsible of collapsibles) {
    // @ts-ignore
    collapsible.removeEventListener("toggle", last_load_func, {once: true});
    if (collapsible.hasAttribute("open")) {
      render_collapsible(draw_options, collapsible, palette, class_name);
    } else {
      collapsible.addEventListener("toggle", load_func, {once: true});
    }
  }
  last_load_func = load_func;
}
//loads all of the animated canvas elements back
//also adds an event listener to enable them when their menu is opened
function load_animated() {
  let collapsibles = document.getElementsByClassName("details-collapsible");
  let load_func = (event) => {
    const canvases = event.target.querySelectorAll("canvas");
    canvases.forEach(initializeElem);
  };
  for (let collapsible of collapsibles) {
    let patterns = Array.from(collapsible.getElementsByClassName("spell-viz"));
    let open = collapsible.hasAttribute("open");
    // @ts-ignore
    collapsible.removeEventListener("toggle", last_load_func, {once: true});
    if (!open) {
      collapsible.addEventListener("toggle", load_func, {once:true})
    }
    for (let pattern of patterns) {
      let attr_to_transfer = ["data-start", "data-string", "width", "height", "data-per-world", "class"];
      let canvas = document.createElement("canvas");
      for (const index in attr_to_transfer) {
        const attr = attr_to_transfer[index];
        let value = pattern.getAttribute(attr);
        canvas.setAttribute(attr, value ? value : "");
      }
      canvas.innerHTML = pattern.innerHTML;
      collapsible.appendChild(canvas);
      if (open) {
        initializeElem(canvas);
      }
      if (pattern.tagName == "IMG") {
        URL.revokeObjectURL(/** @type {HTMLImageElement} */ (pattern).src);
      }
      pattern.remove()
    }
  }
  last_load_func = load_func;
}
/**
 * 
 * @param {string} color 
 * @returns {Color}
 */
function parseColor(color) {
  let trimmed_color = color.trim().substring(1,7);
  let first = 0;
  let second = 0;
  let third = 0;
  //color is either 12 bits (RGB) or 16 bits (RGBA)
  if (trimmed_color.length == 3 || trimmed_color.length == 4) {
    first = Number("0x" + trimmed_color[0]) * 16;
    second = Number("0x" + trimmed_color[1]) * 16;
    third = Number("0x" + trimmed_color[2]) * 16;
  } else { //color is either 24 bits (RGB) or 32 bits (RGBA)
    first = Number("0x" + trimmed_color.substring(0,2));
    second = Number("0x" + trimmed_color.substring(2,4));
    third = Number("0x" + trimmed_color.substring(4,6));
  }
  if (Number.isNaN(first) || Number.isNaN(second) || Number.isNaN(third)) {
    throw new Error("Failed to parse color!");
  }
  return [first, second, third, 255]
}
/**
 * 
 * @param {Element} elem
 * @returns {CSSStyleDeclaration}
 */
function getStyles(elem) {
  return window.getComputedStyle(elem);
}
/**
 * 
 * @param {CSSStyleDeclaration} style 
 * @param {string} palette 
 * @returns {Color[]}
 */
function getPalette(style, palette) {
  return style.getPropertyValue("--" + palette)
      .split(',')
      .map(row => parseColor(row.trim()));
}
/**
 * 
 * @param {CSSStyleDeclaration} style
 * @param {Lines} lines 
 * @returns {GridOptions}
 */
function generateGridOptions(style, lines) {
  /** @type {Point} */
  let intersection_point = {
      type: "Single",
      marker: {
          color: parseColor(style.getPropertyValue("--point-color")),
          radius: Number(style.getPropertyValue("--point-outer-radius"))
      }
  };
  /** @type {Intersections} */
  let intersections = {
      type: "EndsAndMiddle",
      start: {
          type: "BorderedMatch",
          match_radius: Number(style.getPropertyValue("--point-inner-radius")),
          border: intersection_point.marker
      },
      middle: intersection_point,
      end: {
          type: "Point",
          point: intersection_point
      }
  };
  return {
      line_thickness: Number(style.getPropertyValue("--line-thickness")),
      center_dot: {
          type: "None"
      },
      pattern_options: {
          type: "Uniform",
          lines: lines,
          intersections: intersections
      }
  }
}
/**
 * @param {CSSStyleDeclaration} style 
 * @param {string} palette 
 * @returns 
 */
function generateMonocolor(style, palette) {
  let palette_offset = Number(style.getPropertyValue("--palette-offset"));
  /** @type {Lines} */
  let lines = {
      type: "Monocolor",
      color: getPalette(style, palette)[palette_offset],
      bent: true
  }
  return generateGridOptions(style, lines);
}
/**
 * 
 * @param {CSSStyleDeclaration} style 
 * @param {string} palette 
 * @returns 
 */
function generateSegment(style, palette) {
  let triangle_inner_radius = Number(style.getPropertyValue("--triangle-inner-radius"));
  let triangle_outer_radius = Number(style.getPropertyValue("--triangle-outer-radius"));
  let triangle_color = parseColor(style.getPropertyValue("--triangle-color"));
  /** @type {Lines} */
  let lines = {
      type: "SegmentColors",
      colors: getPalette(style, palette),
      triangles: {
          type: "BorderStartMatch",
          match_radius: triangle_inner_radius,
          border: {
              color: triangle_color,
              radius: triangle_outer_radius
          }
      },
      collisions: {
          type: "ParallelLines"
      }
  }
  return generateGridOptions(style, lines);
}
/**
 * 
 * @param {CSSStyleDeclaration} style 
 * @param {string} palette 
 * @returns 
 */
function generateGradient(style, palette) {
  let segs_per_color = Number(style.getPropertyValue("--segs-per-color"));
  /** @type {Lines} */
  let lines = {
      type: "Gradient",
      colors: getPalette(style, palette),
      bent: true,
      segments_per_color: segs_per_color
  };
  return generateGridOptions(style, lines);
}
let selected = "animated";
//this is not programmed to accept the Changing pattern option type (it will crash)
//it doesn't make sense in this context (as it's rendering single patterns)
//and it would take more complex color palettes to work properly
let options = {
  "animated": -1,
  "monocolor": generateMonocolor,
  "gradient": generateGradient,
  "segment": generateSegment,
};
function load_render(name) {
  if (name == "animated") {
    if (selected != name) {
      cachedPatternImage = null;
      load_animated();
    }
  } else {
    render_images(options[name], last_palette, name + "-settings");
    //update cached settings to avoid double update
    old_settings = getSettings(name);
  }
  selected = name;
}
function checkEqual(ob1, ob2) {
  if (ob1 == ob2) {
    return true;
  } else if (ob1 == null || ob2 == null) {
    return false;
  } else if (Object.keys(ob1).length != Object.keys(ob2).length) {
    return false;
  }
  for (let key in ob1) {
    if (!(key in ob2)) {
      return false;
    } else if (typeof ob1[key] != typeof ob2[key]) {
      return false;
    } else if (typeof ob1[key] == "object") {
      if (!checkEqual(ob1[key], ob2[key])) {
        return false;
      }
    } else if (ob1[key] != ob2[key]) {
      return false;
    }
  }
  return true;
}
let cachedPatternImage = null;
let old_settings = null;
function getSettings(render_option) {
  if (cachedPatternImage == null) {
    cachedPatternImage = document.querySelector("img.spell-viz");
    if (cachedPatternImage == null) return;
  }
  let styles = getStyles(cachedPatternImage);
  return options[render_option](styles, last_palette);
}
function updateRenders() {
  if (selected == "animated") {
    return;
  }
  let new_settings = getSettings(selected);
  if (!checkEqual(new_settings,old_settings)) {
    console.log("updated pattern render from css");
    old_settings = new_settings;
    load_render(selected);
  }
}
//palette values stored via css so they can be changedaround
let palette_options = {
  default: true,
  turbo: true,
  dark2: true,
  tab10: true
}
let last_palette = "default";
export function load_palette(name) {
  if (!palette_options[name]) {
    return;
  }
  last_palette = name;
  if (selected != "animated") {
    render_images(options[selected], last_palette, selected + "-settings");
  }
}
const render_lang = {
  "动画": "animated",
  "单色": "monocolor",
  "渐变": "gradient",
  "线段": "segment",
}
const palette_lang = {
  "默认": "default",
  "turbo": "turbo",
  "dark2": "dark2",
  "tab10": "tab10",
};
//adds all of the options to the menus (renderer and palette menus)
function setup_menus() {
  let render_dropdowns = document.getElementsByClassName("render-dropdowns");
  for (let render_bar of render_dropdowns) {
    for (const [key, val] of Object.entries(render_lang)) {
      let item = document.createElement("li");
      let button = document.createElement("a");
      button.onclick = () => { load_render(val) };
      button.innerText = key;
      button.className = "render-option";
      item.appendChild(button);
      render_bar?.appendChild(item);
    }
  }
  let palette_dropdowns = document.getElementsByClassName("palette-dropdowns");
  for (let palette_bar of palette_dropdowns) {
    for (const [key, val] of Object.entries(palette_lang)) {
      let item = document.createElement("li");
      let button = document.createElement("a");
      button.onclick = () => { load_palette(val) };
      button.innerText = key;
      button.className = "palette-option";
      item.appendChild(button);
      palette_bar?.appendChild(item);
    }
  }
}
function setup_update_triggers() {
  let collapsibles = document.getElementsByClassName("details-collapsible");
  for (let collapsible of collapsibles) {
      collapsible.addEventListener("toggle", () => {
        if (collapsible.open) {
          updateRenders();
        }
      }, {once: false});
  }
}
setup_menus();
setup_update_triggers();
// // @ts-check
// "uncomment" above to get typechecking to work, will error unless hex_renderer_javascript (npm) is installed
"use-strict";
// @ts-ignore
//imports the initializer function from the animated renderer
import { initializeElem } from "./hexcasting.js";
// @ts-ignore
//actual import of the renderer
//package here: https://www.npmjs.com/package/hex_renderer_javascript
import init_renderer, {draw_bound_pattern} from "https://cdn.jsdelivr.net/npm/hex_renderer_javascript@0.1.2/hex_renderer_javascript.js";
//used for when type hints are needed for the functions (doesn't actually import it in runtime)
//requires hex_renderer_javascript to be installed (npm install hex_renderer_javascript)
//  import init_renderer, {draw_bound_pattern} from "hex_renderer_javascript";
//initializes the WASM code used in the hex_renderer_javascript library
init_renderer();
//manually importing typedefs (used for ensuring the render options are formatted correctly)
/**
 * @typedef {import('hex_renderer_javascript').GridOptions} GridOptions
 * @typedef {import('hex_renderer_javascript').Color} Color
 * @typedef {import('hex_renderer_javascript').Intersections} Intersections
 * @typedef {import('hex_renderer_javascript').Point} Point
 */
//renders the patterns in a collapsible menu
//It reads the previous data of the images/animation, uses it to draw the new one
//and then it deletes the old
function render_collapsible(draw_options, collapsible) {
  let patterns = Array.from(collapsible.getElementsByClassName("spell-viz"));
  for (let pat of patterns) {
    var attr = pat.attributes;
    var start_dir = attr["data-start"].value;
    var pattern_str = attr["data-string"].value;
    var width = attr["width"].value;
    var height = attr["height"].value;
    var per_world = "True" == attr["data-per-world"].value;
    var pattern = {
      great_spell: per_world,
      direction: start_dir,
      angle_sigs: pattern_str,
    };
    let img_data = draw_bound_pattern(draw_options, pattern, 0.35, width, height);
    let img = new Image();
    img.src = URL.createObjectURL(new Blob([img_data.buffer], { type: 'image/png' }))
    img.setAttribute("data-start", start_dir);
    img.setAttribute("data-string", pattern_str);
    img.setAttribute("width", width);
    img.setAttribute("height", height);
    img.setAttribute("data-per-world", attr["data-per-world"].value);
    img.setAttribute("class", "spell-viz");
    img.innerHTML = pat.innerHTML;
    pat.parentElement?.appendChild(img);
    pat.remove()
  }
}
//last_load_func allows the previous event listeners to be removed
//that way we aren't doing unecessary work when you switch between a ton of options
let last_load_func = null;
//lazily renders all of the images with the chosen render options
//loads all of the currently open ones and then adds event listeners to open the rest when they're opened
function render_images(draw_options) {
  let collapsibles = document.getElementsByClassName("details-collapsible");
  let load_func = (event) => {
    render_collapsible(draw_options, event.target);
  }
  for (let collapsible of collapsibles) {
    // @ts-ignore
    collapsible.removeEventListener("toggle", last_load_func, {once: true});
    if (collapsible.hasAttribute("open")) {
      render_collapsible(draw_options, collapsible);
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
      pattern.remove()
    }
  }
  last_load_func = load_func;
}
/** @type {Point} */
const intersection_point = {
  type: "Single",
  marker: {
    color: [255, 255, 255, 255],
    radius: 0.07,
  }
};
/** @type {Intersections} */
const intersections = {
  type: "EndsAndMiddle",
  start: {
    type: "BorderedMatch",
    match_radius: 0.04,
    border: intersection_point.marker
  },
  middle: intersection_point,
  end: {
    type: "Point",
    point: intersection_point
  }
};
const line_thickness = 0.12;
/** @type {Color[]} */
const default_colors = [
  [255, 107, 255, 255],
  [168, 30, 227, 255],
  [100, 144, 237, 255],
  [177, 137, 199, 255],
];
/** @type {Point} */
const center_dot = {
  type: "None"
};
/** @type {GridOptions} */
const monocolor = {
  line_thickness: line_thickness,
  center_dot: center_dot,
  pattern_options: {
    type: "Uniform",
    lines: {
      type: "Monocolor",
      color: default_colors[0],
      bent: true
    },
    intersections: intersections,
  },
};
/** @type {GridOptions} */
const gradient = {
  line_thickness: line_thickness,
  center_dot: center_dot,
  pattern_options: {
    type: "Uniform",
    lines: {
      type: "Gradient",
      colors: default_colors,
      bent: true,
      segments_per_color: 15,
    },
    intersections: intersections,
  },
};
/** @type {GridOptions} */
const segment = {
  line_thickness: line_thickness,
  pattern_options: {
    type: "Uniform",
    intersections: intersections,
    lines: {
      type: "SegmentColors",
      colors: default_colors,
      triangles: {
        type: "BorderStartMatch",
        match_radius: 0.13,
        border: {
          color: [255, 255, 255, 255],
          radius: 0.20,
        }
      },
      collisions: {
        type: "ParallelLines"
      }
    }
  },
  center_dot: center_dot,
}
let selected = "animated";
//this is not programmed to accept the Changing pattern option type (it will crash)
//it doesn't make sense in this context (as it's rendering single patterns)
//and it would take more complex color palettes to work properly
let options = {
  "animated": -1,
  "monocolor": monocolor,
  "gradient": gradient,
  "segment": segment,
};
function load_render(name) {
  if (selected == name) {
    return;
  }
  selected = name;
  if (name == "animated") {
    load_animated();
  } else {
    render_images(options[name]);
  }
}
//all palettes must be an array of colors (to switch between)
//the monocolor option only selects the first color in the palette
//each color is 4 values RGBA (Red, Green, Blue, Alpha (Transparency))
let palette_options = {
  default: default_colors,
  turbo: [
    [63, 61, 156, 255],
    [64, 150, 254, 255],
    [25, 227, 184, 255],
    [132, 254, 80, 255],
    [223, 222, 54, 255],
    [253, 140, 39, 255],
    [214, 52, 5, 255],
    [122, 4, 2, 255],
  ],
  dark2: [
    [27, 158, 119, 255],
    [217, 95, 2, 255],
    [117, 112, 179, 255],
    [231, 41, 138, 255],
    [102, 166, 30, 255],
    [230, 171, 2, 255],
    [166, 118, 29, 255],
    [102, 102, 102, 255],
  ],
  tab10: [
    [31, 119, 180, 255],
    [255, 127, 14, 255],
    [44, 160, 44, 255],
    [214, 39, 40, 255],
    [148, 103, 189, 255],
    [140, 86, 75, 255],
    [227, 119, 194, 255],
    [127, 127, 127, 255],
    [188, 189, 34, 255],
    [23, 190, 207, 255],
  ]
}
let last_palette = "default";
export function load_palette(name) {
  if (last_palette == name) {
    return;
  }
  if (!palette_options[name]) {
    return;
  }
  for (let key in options) {
    if (!(options[key] instanceof Object) || !options[key].pattern_options || !options[key].pattern_options.lines) {
      continue;
    }
    if (options[key].pattern_options.lines.color) {
      options[key].pattern_options.lines.color = palette_options[name][0];
    } else {
      options[key].pattern_options.lines.colors = palette_options[name];
    }
  }
  last_palette = name;
  if (selected != "animated") {
    render_images(options[selected]);
  }
}
const render_lang = {
  "animated": "animated",
  "monocolor": "monocolor",
  "gradient": "gradient",
  "segment": "segment",
}
const palette_lang = {
  "default": "default",
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
setup_menus();
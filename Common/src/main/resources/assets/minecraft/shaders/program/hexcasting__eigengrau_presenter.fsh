#version 150

// see PostPass.java: the game texture has a defined name
uniform sampler2D DiffuseSampler;
uniform sampler2D veil;
uniform sampler2D bz;

uniform vec2 ScreenSize;

const float resolution = 5.0;

in vec2 texCoord0;
out vec4 fragColor;

ivec2 pxToHex(vec2 px) {
    float sqrt3 = sqrt(3.0);

    float x = px.x / (resolution * sqrt3);
    float y = px.y / (resolution * sqrt3);

    float tmp = floor(x + sqrt3 * y + 1.0);
    int q = int((floor(2.0 * x + 1.0) + tmp) / 3.0);
    int r = int((tmp + floor(-x + sqrt3 * y + 1.0)) / 3.0);

    return ivec2(q, r);
}

vec2 hexToPx(ivec2 hex) {
    float sqrt3 = sqrt(3.0);

    // I'm not sure why I need the -hex.y here
    vec2 xy = vec2(
        sqrt3 * float(hex.x - hex.y) + sqrt3 / 2.0 * float(hex.y),
        1.5 * float(hex.y)
    );
    return xy * resolution;
}

void main() {
    vec4 worldSample4 = texture(DiffuseSampler, texCoord0);
    vec3 worldSample = worldSample4.rgb;
    // This does piecewise mul
    ivec2 hex = pxToHex(texCoord0 * ScreenSize);
    vec2 pxAgain = hexToPx(hex);

    vec2 veil = texture(veil, texCoord0).rg;
    float brightness = (worldSample.r + worldSample.g + worldSample.b) / 3.0;
    float eigengrauAmount = clamp(veil.r + mix(0.0, veil.g, 1.0 - brightness), 0.5, 1.0);

    vec2 st = vec2(float(hex.x), float(hex.y)) / resolution;
    float eigengrauSample = texture(bz, st).r / 100.0;
    //    vec3 col = smoothstep(worldSample, vec3(eigengrauSample), vec3(eigengrauAmount));
    vec3 col = worldSample;
    gl_FragColor = vec4(col, worldSample4.a);
}

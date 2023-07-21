// Runs the belousov-zhabotinsky simulation in the background.
// See https://www.shadertoy.com/view/DddGW4
// State is stored in the R channel -- I would make this operate over integers but I think that requires
// a compute shader and ughhghhh

#version 150

uniform sampler2D State;
uniform vec2 ScreenSize;

in vec2 texCoord0;

const float GRANULARITY = 100.0;
const float INFECTION_PROGRESSION = 1.0;
const float ILL_INFLUENCE = 1.0;
const float INFECTED_INFLUENCE = 1.0;
const float EXAMINED_COUNT = 7.0; // 7 cells are examined

float bz(vec2 px) {
    float self = texture(State, px / ScreenSize).r;

    float illCount = 0.0;
    float infectedCount = 0.0;
    float sum = self;
    for (float dx = -1.0; dx <= 1.0; dx++) {
        for (float dy = -1.0; dy <= 1.0; dy++) {
            if (dx == -dy) continue;

            vec2 npos = (px + vec2(dx, dy)) / ScreenSize;
            float neighbor = texture(State, npos).r;
            if (neighbor >= GRANULARITY) {
                illCount += 1.0;
            } else if (neighbor > 0.0) {
                infectedCount += 1.0;
            }

            sum += neighbor;
        }
    }

    float result = 0.0;
    if (self >= GRANULARITY) {
        result = 0.0;
    } else if (self > 0.0) {
        result = min(
            trunc(sum / EXAMINED_COUNT + INFECTION_PROGRESSION),
            GRANULARITY
        );
    } else {
        result = trunc(infectedCount / INFECTED_INFLUENCE)
        + trunc(illCount / ILL_INFLUENCE);
    }

    return result;
}

void main() {
    float rawBZ = bz(texCoord0 * ScreenSize);
    gl_FragColor = vec4(vec3(rawBZ), 1.0);
}

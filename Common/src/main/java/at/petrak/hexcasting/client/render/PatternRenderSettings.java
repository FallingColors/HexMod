package at.petrak.hexcasting.client.render;

/*
TODO:
 - handle padding, full size (for inline) vs normal
    - it's actually more about scaling
    - it's all the same for a square pattern, but we need to account for non-square patterns.
    - ie, does it get fit to a specific axis (like inline fits to vertical and stretches as needed horizontally)
      or does it fit on both/smaller fit like a scroll does.
 - figure out how much is decided alongside zappy point list vs given from before -- all size stuff varies by like,
 - figure out main render args
 */

import at.petrak.hexcasting.api.casting.math.HexPattern;

import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * Immutable data class for informing how a pattern is rendered.
 *
 * (it's a pain but this isn't a record or kotlin data class because i want it non-final)
 */
public class PatternRenderSettings {

    protected FitAxis fitAxis; // which axes the pattern needs to be fit to.

    protected String id = "default";

    // all measurements are in the scale of whatever pose stack is given to the renderer.
    protected double baseScale; // length between 2 adjacent points if not squished by any fit.
    protected double minWidth;
    protected double minHeight;

    // height and with are only relevant if it's set to fit on that axis.
    protected double spaceWidth = 1.0;
    protected double spaceHeight = 1.0;

    // horizontal and vertical padding. used no matter the fit axis.
    protected double hPadding;
    protected double vPadding;

    protected UnaryOperator<Float> innerWidthProvider = (scale) -> 0.1f;
    protected UnaryOperator<Float> outerWidthProvider = (scale) -> 0.15f;

    // zappy settings -- unused if you pass points instead of a pattern
    protected int hops = 10;
    protected float variance = 0.5f;
    protected float speed;
    protected float flowIrregular = 0.2f;
    protected float readabilityOffset;
    protected float lastSegmentLenProportion = 1f;

    public PatternRenderSettings(){}

    private PatternRenderSettings(
        FitAxis fitAxis, double baseScale, double minWidth, double minHeight, double spaceWidth, double spaceHeight,
        double hPadding, double vPadding, int hops, float variance, float speed, float flowIrregular, float readabilityOffset,
        float lastSegmentLenProportion, UnaryOperator<Float> innerWidthProvider, UnaryOperator<Float> outerWidthProvider
    ){
        this.fitAxis = fitAxis; this.baseScale = baseScale; this.minWidth = minWidth; this.minHeight = minHeight;
        this.spaceWidth = spaceWidth; this.spaceHeight = spaceHeight; this.hPadding = hPadding; this.vPadding = vPadding; this.hops = hops; this.variance = variance; this.speed = speed;
        this.flowIrregular = flowIrregular; this.readabilityOffset = readabilityOffset; this.lastSegmentLenProportion = lastSegmentLenProportion;
        this.innerWidthProvider = innerWidthProvider; this.outerWidthProvider = outerWidthProvider;
        // *dies*
    }

    public String getCacheKey(HexPattern pattern, double seed){
        return (pattern.getStartDir().toString() + "-" + pattern.anglesSignature() + "-" + id + "-" + seed).toLowerCase();
    }

    private PatternRenderSettings copy(){
        PatternRenderSettings newSets = new PatternRenderSettings(fitAxis, baseScale, minWidth, minHeight, spaceWidth, spaceHeight, hPadding, vPadding,
                hops, variance, speed, flowIrregular, readabilityOffset, lastSegmentLenProportion, innerWidthProvider, outerWidthProvider);
        // add a UUID attached to the id (or our best guess, it doesn't really matter this is just to get a unique different id)
        newSets.id = id.substring(0, Math.max(id.indexOf('_'), 0)) + "_" + UUID.randomUUID();
        return newSets;
    }

    public PatternRenderSettings withSizings(FitAxis fitAxis, Double spaceWidth, Double spaceHeight, Double hPadding,
                                             Double vPadding, Double baseScale, Double minWidth, Double minHeight,
                                             UnaryOperator<Float> innerWidthProvider, UnaryOperator<Float> outerWidthProvider){
        PatternRenderSettings newSettings = copy();
        newSettings.fitAxis = fitAxis == null ? this.fitAxis : fitAxis;
        newSettings.spaceWidth = spaceWidth == null ? this.spaceWidth : spaceWidth;
        newSettings.spaceHeight = spaceHeight == null ? this.spaceHeight : spaceHeight;
        newSettings.hPadding = hPadding == null ? this.hPadding : hPadding;
        newSettings.vPadding = vPadding == null ? this.vPadding : vPadding;
        newSettings.baseScale = baseScale == null ? this.baseScale : baseScale;
        newSettings.minWidth = minWidth == null ? this.minWidth : minWidth;
        newSettings.minHeight = minHeight == null ? this.minHeight : minHeight;
        newSettings.innerWidthProvider = innerWidthProvider == null ? this.innerWidthProvider : innerWidthProvider;
        newSettings.outerWidthProvider = outerWidthProvider == null ? this.outerWidthProvider : outerWidthProvider;
        return newSettings;
    }

    public PatternRenderSettings withZappySettings(Integer hops, Float variance, Float speed, Float flowIrregular,
                                                   Float readabilityOffset, Float lastSegmentLenProportion){
        PatternRenderSettings newSettings = copy();
        newSettings.hops = hops == null ? this.hops : hops;
        newSettings.variance = variance == null ? this.variance : variance;
        newSettings.speed = speed == null ? this.speed : speed;
        newSettings.flowIrregular = flowIrregular == null ? this.flowIrregular : flowIrregular;
        newSettings.readabilityOffset = readabilityOffset == null ? this.readabilityOffset : readabilityOffset;
        newSettings.lastSegmentLenProportion = lastSegmentLenProportion == null ? this.lastSegmentLenProportion : lastSegmentLenProportion;
        return newSettings;
    }

    public PatternRenderSettings named(String id){
        PatternRenderSettings newSettings = copy();
        newSettings.id = id;
        return newSettings;
    }

    public enum FitAxis{
        HOR(true, false),
        VERT(false, true),
        BOTH(true, true),
        NONE(false, false);

        public final boolean horFit;
        public final boolean vertFit;

        FitAxis(boolean horFit, boolean vertFit){
            this.horFit = horFit;
            this.vertFit = vertFit;
        }
    }
}

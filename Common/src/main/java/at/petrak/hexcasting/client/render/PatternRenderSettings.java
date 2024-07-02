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

/**
 * Immutable data class for informing how a pattern is rendered.
 *
 * (it's a pain but this isn't a record or kotlin data class because i want it non-final)
 */
public class PatternRenderSettings {
    protected boolean drawOuter;

    protected FitAxis fitAxis; // which axes the pattern needs to be fit to.

    // all measurements are in the scale of whatever pose stack is given to the renderer.
    protected double baseScale; // length between 2 adjacent points if not squished by any fit.
    // TODO: consider doing a min size too, although that assumes there's a base scale already
    protected double minWidth;
    protected double minHeight;

    // height and with are only relevant if it's set to fit on that axis.
    protected double spaceWidth = 1.0;
    protected double spaceHeight = 1.0;

    // horizontal and vertical padding. used no matter the fit axis.
    protected double hPadding;
    protected double vPadding;

    // TODO: these should maybe be functions that take ?? something ??
    protected float innerWidth;
    protected float outerWidth;

    // colors
    protected int innerStartColor;
    protected int innerEndColor;
    protected int outerStartColor;
    protected int outerEndColor;

    // zappy settings -- unused if you pass points instead of a pattern
    protected int hops = 10;
    protected float variance = 0.5f;
    protected float speed;
    protected float flowIrregular = 0.2f;
    protected float readabilityOffset;
    protected float lastSegmentLenProportion = 1f;


    public PatternRenderSettings(

    ){

    }

    // TODO: make this actually copy.
    public PatternRenderSettings copy(){
        return this;
    }

    public int getInnerStartColor(){ return innerStartColor; }
    public int getInnerEndColor(){ return innerEndColor; }
    public int getOuterStartColor(){ return outerStartColor; }
    public int getOuterEndColor(){ return outerEndColor; }

    public PatternRenderSettings withColors(Integer startInner, Integer endInner, Integer startOuter, Integer endOuter){
        PatternRenderSettings newSettings = copy();
        newSettings.innerStartColor = (startInner == null) ? innerStartColor : startInner;
        newSettings.innerEndColor = (endInner == null) ? innerEndColor : endInner;
        newSettings.outerStartColor = (startOuter == null) ? outerStartColor : startOuter;
        newSettings.outerEndColor = (endOuter == null) ? outerEndColor : endOuter;
        return newSettings;
    }

    public PatternRenderSettings withSizings(FitAxis fitAxis, Double spaceWidth, Double spaceHeight, Double hPadding,
                                             Double vPadding, Double baseScale, Double minWidth, Double minHeight){
        PatternRenderSettings newSettings = copy();
        newSettings.fitAxis = fitAxis == null ? this.fitAxis : fitAxis;
        newSettings.spaceWidth = spaceWidth == null ? this.spaceWidth : spaceWidth;
        newSettings.spaceHeight = spaceHeight == null ? this.spaceHeight : spaceHeight;
        newSettings.hPadding = hPadding == null ? this.hPadding : hPadding;
        newSettings.vPadding = vPadding == null ? this.vPadding : vPadding;
        newSettings.baseScale = baseScale == null ? this.baseScale : baseScale;
        newSettings.minWidth = minWidth == null ? this.minWidth : minWidth;
        newSettings.minHeight = minHeight == null ? this.minHeight : minHeight;
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

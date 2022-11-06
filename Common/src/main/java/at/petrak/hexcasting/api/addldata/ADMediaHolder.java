package at.petrak.hexcasting.api.addldata;

import org.jetbrains.annotations.ApiStatus;

public interface ADMediaHolder {

    /**
     * Use {@code withdrawMedia(-1, true)}
     *
     * @see ADMediaHolder#withdrawMedia(int, boolean)
     */
    @ApiStatus.OverrideOnly
    int getMedia();

    /**
     * Use {@code withdrawMedia(-1, true) + insertMedia(-1, true)} where possible
     *
     * @see ADMediaHolder#insertMedia(int, boolean)
     * @see ADMediaHolder#withdrawMedia(int, boolean)
     */
    @ApiStatus.OverrideOnly
    int getMaxMedia();

    /**
     * Use {@code insertMedia(media - withdrawMedia(-1, true), false)} where possible
     *
     * @see ADMediaHolder#insertMedia(int, boolean)
     * @see ADMediaHolder#withdrawMedia(int, boolean)
     */
    @ApiStatus.OverrideOnly
    void setMedia(int media);

    /**
     * Whether this media holder can have media inserted into it.
     */
    boolean canRecharge();

    /**
     * Whether this media holder can be extracted from.
     */
    boolean canProvide();

    /**
     * The priority for this media holder to be selected when casting a hex. Higher priorities are taken first.
     *
     * By default,
     * * Charged Amethyst has priority 1000
     * * Amethyst Shards have priority 2000
     * * Amethyst Dust has priority 3000
     * * Items which hold media have priority 4000
     */
    int getConsumptionPriority();

    /**
     * Whether the media inside this media holder may be used to construct a battery.
     */
    boolean canConstructBattery();

    /**
     * Withdraws media from the holder. Returns the amount of media extracted, which may be less or more than the cost.
     *
     * Even if {@link ADMediaHolder#canProvide} is false, you can still withdraw media this way.
     *
     * Withdrawing a negative amount will act as though you attempted to withdraw as much media as the holder contains.
     */
    default int withdrawMedia(int cost, boolean simulate) {
        var mediaHere = getMedia();
        if (cost < 0) {
            cost = mediaHere;
        }
        if (!simulate) {
            var mediaLeft = mediaHere - cost;
            setMedia(mediaLeft);
        }
        return Math.min(cost, mediaHere);
    }

    /**
     * Inserts media into the holder. Returns the amount of media inserted, which may be less than the requested amount.
     *
     * Even if {@link ADMediaHolder#canRecharge} is false, you can still insert media this way.
     *
     * Inserting a negative amount will act as though you attempted to insert exactly as much media as the holder was missing.
     */
    default int insertMedia(int amount, boolean simulate) {
        var mediaHere = getMedia();
        int emptySpace = getMaxMedia() - mediaHere;
        if (emptySpace <= 0) {
            return 0;
        }
        if (amount < 0) {
            amount = emptySpace;
        }

        int inserting = Math.min(amount, emptySpace);

        if (!simulate) {
            var newMedia = mediaHere + inserting;
            setMedia(newMedia);
        }
        return inserting;
    }

    int CHARGED_AMETHYST_PRIORITY = 1000;
    int AMETHYST_SHARD_PRIORITY = 2000;
    int AMETHYST_DUST_PRIORITY = 3000;
    int BATTERY_PRIORITY = 4000;
}

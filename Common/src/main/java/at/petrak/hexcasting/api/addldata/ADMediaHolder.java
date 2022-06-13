package at.petrak.hexcasting.api.addldata;

public interface ADMediaHolder {

    int getMedia();

    int getMaxMedia();

    void setMedia(int media);

    boolean canRecharge();

    boolean canProvide();

    /**
     * When scanning an inventory for media-containing items, the items with the highest priority will
     * get their media consumed first.
     */
    int getConsumptionPriority();

    boolean canConstructBattery();

    default int withdrawMedia(int cost, boolean simulate) {
        if (!canProvide()) {
            return 0;
        }
        var manaHere = getMedia();
        if (cost < 0) {
            cost = manaHere;
        }
        if (!simulate) {
            var manaLeft = manaHere - cost;
            setMedia(manaLeft);
        }
        return Math.min(cost, manaHere);
    }

    public int CHARGED_AMETHYST_PRIORITY = 1000;
    public int AMETHYST_SHARD_PRIORITY = 2000;
    public int AMETHYST_DUST_PRIORITY = 3000;
    public int BATTERY_PRIORITY = 4000;
}

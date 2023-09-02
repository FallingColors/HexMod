package at.petrak.hexcasting.api.addldata;

import org.jetbrains.annotations.ApiStatus;

public interface ManaHolder {

    /**
     * Use {@code withdrawMana(-1, true)}
     *
     * @see ManaHolder#withdrawMana(int, boolean)
     */
    @ApiStatus.OverrideOnly
    int getMana();

    /**
     * Use {@code withdrawMana(-1, true) + insertMana(-1, true)} where possible
     *
     * @see ManaHolder#insertMana(int, boolean)
     * @see ManaHolder#withdrawMana(int, boolean)
     */
    @ApiStatus.OverrideOnly
    int getMaxMana();

    /**
     * Use {@code insertMana(mana - withdrawMana(-1, true), false)} where possible
     *
     * @see ManaHolder#insertMana(int, boolean)
     * @see ManaHolder#withdrawMana(int, boolean)
     */
    @ApiStatus.OverrideOnly
    void setMana(int mana);

    /**
     * Whether this mana holder can have mana inserted into it.
     */
    boolean canRecharge();

    /**
     * Whether this mana holder can be extracted from.
     */
    boolean canProvide();

    /**
     * The priority for this mana holder to be selected when casting a hex. Higher priorities are taken first.
     *
     * By default,
     * * Charged Amethyst has priority 1
     * * Amethyst Shards have priority 2
     * * Amethyst Dust has priority 3
     * * Items which hold mana have priority 40
     */
    int getConsumptionPriority();

    /**
     * Whether the mana inside this mana holder may be used to construct a battery.
     */
    boolean canConstructBattery();

    /**
     * Withdraws mana from the holder. Returns the amount of mana extracted, which may be less or more than the cost.
     *
     * Even if {@link ManaHolder#canProvide} is false, you can still withdraw mana this way.
     *
     * Withdrawing a negative amount will act as though you attempted to withdraw as much mana as the holder contains.
     */
    default int withdrawMana(int cost, boolean simulate) {
        var manaHere = getMana();
        if (cost < 0) {
            cost = manaHere;
        }
        if (!simulate) {
            var manaLeft = manaHere - cost;
            setMana(manaLeft);
        }
        return Math.min(cost, manaHere);
    }

    /**
     * Inserts mana into the holder. Returns the amount of mana inserted, which may be less than the requested amount.
     *
     * Even if {@link ManaHolder#canRecharge} is false, you can still insert mana this way.
     *
     * Inserting a negative amount will act as though you attempted to insert exactly as much mana as the holder was missing.
     */
    default int insertMana(int amount, boolean simulate) {
        var manaHere = getMana();
        int emptySpace = getMaxMana() - manaHere;
        if (emptySpace <= 0) {
            return 0;
        }
        if (amount < 0) {
            amount = emptySpace;
        }

        int inserting = Math.min(amount, emptySpace);

        if (!simulate) {
            var newMana = manaHere + inserting;
            setMana(newMana);
        }
        return inserting;
    }
}

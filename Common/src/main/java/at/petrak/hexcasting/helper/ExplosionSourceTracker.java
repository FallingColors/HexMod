package at.petrak.hexcasting.helper;

public final class ExplosionSourceTracker {
    private static final ThreadLocal<Boolean> IS_SPELL_SOURCE = ThreadLocal.withInitial(() -> false);

    public static boolean isSpellSource() {
        return IS_SPELL_SOURCE.get();
    }

    public static void setSpellSource(boolean spellSource) {
        IS_SPELL_SOURCE.set(spellSource);
    }
}

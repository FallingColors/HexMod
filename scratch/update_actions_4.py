with open('D:/GEMINI AGY/workspaces/HexMod_feature_kinetics/Common/src/main/java/at/petrak/hexcasting/common/lib/hex/HexActions.java', 'r', encoding='utf-8') as f:
    content = f.read()

target = 'import at.petrak.hexcasting.common.casting.actions.math.OpVectorCatmullRom;'
replacement = 'import at.petrak.hexcasting.common.casting.actions.math.OpVectorCatmullRom;\nimport at.petrak.hexcasting.common.casting.actions.math.OpVectorHermiteSpline;'

assert target in content, 'Target 1 not found'
content = content.replace(target, replacement)

math_target = '''    public static final ActionRegistryEntry VECTOR_CATMULL_ROM = make("vector_catmull_rom",
        new ActionRegistryEntry(HexPattern.fromAngles("wdedwawq", HexDir.NORTH_WEST), OpVectorCatmullRom.INSTANCE));'''

math_replacement = math_target + '''
    public static final ActionRegistryEntry VECTOR_HERMITE_SPLINE = make("vector_hermite_spline",
        new ActionRegistryEntry(HexPattern.fromAngles("waqawde", HexDir.NORTH_EAST), OpVectorHermiteSpline.INSTANCE));'''

assert math_target in content, 'Target 2 not found'
content = content.replace(math_target, math_replacement)

with open('D:/GEMINI AGY/workspaces/HexMod_feature_kinetics/Common/src/main/java/at/petrak/hexcasting/common/lib/hex/HexActions.java', 'w', encoding='utf-8') as f:
    f.write(content)
print('Successfully updated HexActions.java with Hermite Spline action')

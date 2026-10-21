with open('D:/GEMINI AGY/workspaces/HexMod_feature_kinetics/Common/src/main/java/at/petrak/hexcasting/common/lib/hex/HexActions.java', 'r', encoding='utf-8') as f:
    content = f.read()

target = 'import at.petrak.hexcasting.common.casting.actions.math.OpCoerceToAxial;\nimport at.petrak.hexcasting.common.casting.actions.math.OpRandom;'
replacement = 'import at.petrak.hexcasting.common.casting.actions.math.OpCoerceToAxial;\nimport at.petrak.hexcasting.common.casting.actions.math.OpRandom;\nimport at.petrak.hexcasting.common.casting.actions.math.OpVectorClampMagnitude;\nimport at.petrak.hexcasting.common.casting.actions.math.OpVectorBezier;\nimport at.petrak.hexcasting.common.casting.actions.math.OpSmoothstep;'

assert target in content, 'Target 1 not found'
content = content.replace(target, replacement)

math_target = '''    public static final ActionRegistryEntry COERCE_AXIAL = make("coerce_axial",
        new ActionRegistryEntry(HexPattern.fromAngles("qqqqqaww", HexDir.NORTH_WEST), OpCoerceToAxial.INSTANCE));'''

math_replacement = '''    public static final ActionRegistryEntry COERCE_AXIAL = make("coerce_axial",
        new ActionRegistryEntry(HexPattern.fromAngles("qqqqqaww", HexDir.NORTH_WEST), OpCoerceToAxial.INSTANCE));
    public static final ActionRegistryEntry VECTOR_CLAMP_MAGNITUDE = make("vector_clamp_magnitude",
        new ActionRegistryEntry(HexPattern.fromAngles("waqawqqq", HexDir.NORTH_EAST), OpVectorClampMagnitude.INSTANCE));
    public static final ActionRegistryEntry VECTOR_BEZIER = make("vector_bezier",
        new ActionRegistryEntry(HexPattern.fromAngles("wdedweee", HexDir.NORTH_WEST), OpVectorBezier.INSTANCE));
    public static final ActionRegistryEntry SMOOTHSTEP = make("smoothstep",
        new ActionRegistryEntry(HexPattern.fromAngles("eqqqqwaq", HexDir.EAST), OpSmoothstep.INSTANCE));'''

assert math_target in content, 'Target 2 not found'
content = content.replace(math_target, math_replacement)

with open('D:/GEMINI AGY/workspaces/HexMod_feature_kinetics/Common/src/main/java/at/petrak/hexcasting/common/lib/hex/HexActions.java', 'w', encoding='utf-8') as f:
    f.write(content)
print('Successfully updated HexActions.java')

with open('D:/GEMINI AGY/workspaces/HexMod_feature_kinetics/Common/src/main/java/at/petrak/hexcasting/common/lib/hex/HexActions.java', 'r', encoding='utf-8') as f:
    content = f.read()

target = 'import at.petrak.hexcasting.common.casting.actions.math.OpVectorHermiteSpline;'
replacement = 'import at.petrak.hexcasting.common.casting.actions.math.OpVectorHermiteSpline;\nimport at.petrak.hexcasting.common.casting.actions.math.OpVectorBounce;'

assert target in content, 'Target 1 not found'
content = content.replace(target, replacement)

math_target = '''    public static final ActionRegistryEntry VECTOR_HERMITE_SPLINE = make("vector_hermite_spline",
        new ActionRegistryEntry(HexPattern.fromAngles("waqawde", HexDir.NORTH_EAST), OpVectorHermiteSpline.INSTANCE));'''

math_replacement = math_target + '''
    public static final ActionRegistryEntry VECTOR_BOUNCE = make("vector_bounce",
        new ActionRegistryEntry(HexPattern.fromAngles("waqawq", HexDir.NORTH_EAST), OpVectorBounce.INSTANCE));'''

assert math_target in content, 'Target 2 not found'
content = content.replace(math_target, math_replacement)

with open('D:/GEMINI AGY/workspaces/HexMod_feature_kinetics/Common/src/main/java/at/petrak/hexcasting/common/lib/hex/HexActions.java', 'w', encoding='utf-8') as f:
    f.write(content)
print('Successfully updated HexActions.java with Vector Bounce action')

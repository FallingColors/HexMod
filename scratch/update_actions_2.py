with open('D:/GEMINI AGY/workspaces/HexMod_feature_kinetics/Common/src/main/java/at/petrak/hexcasting/common/lib/hex/HexActions.java', 'r', encoding='utf-8') as f:
    content = f.read()

target = 'import at.petrak.hexcasting.common.casting.actions.math.OpSmoothstep;'
replacement = 'import at.petrak.hexcasting.common.casting.actions.math.OpSmoothstep;\nimport at.petrak.hexcasting.common.casting.actions.math.OpVectorSpring;\nimport at.petrak.hexcasting.common.casting.actions.math.OpVectorReflectNormal;'

assert target in content, 'Target 1 not found'
content = content.replace(target, replacement)

math_target = '''    public static final ActionRegistryEntry SMOOTHSTEP = make("smoothstep",
        new ActionRegistryEntry(HexPattern.fromAngles("eqqqqwaq", HexDir.EAST), OpSmoothstep.INSTANCE));'''

math_replacement = math_target + '''
    public static final ActionRegistryEntry VECTOR_SPRING = make("vector_spring",
        new ActionRegistryEntry(HexPattern.fromAngles("waqawddw", HexDir.NORTH_EAST), OpVectorSpring.INSTANCE));
    public static final ActionRegistryEntry VECTOR_REFLECT_NORMAL = make("vector_reflect_normal",
        new ActionRegistryEntry(HexPattern.fromAngles("wdedwaqw", HexDir.NORTH_WEST), OpVectorReflectNormal.INSTANCE));'''

assert math_target in content, 'Target 2 not found'
content = content.replace(math_target, math_replacement)

with open('D:/GEMINI AGY/workspaces/HexMod_feature_kinetics/Common/src/main/java/at/petrak/hexcasting/common/lib/hex/HexActions.java', 'w', encoding='utf-8') as f:
    f.write(content)
print('Successfully updated HexActions.java with spring and reflect actions')

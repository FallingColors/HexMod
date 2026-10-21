with open('D:/GEMINI AGY/workspaces/HexMod_feature_kinetics/Common/src/main/java/at/petrak/hexcasting/common/lib/hex/HexActions.java', 'r', encoding='utf-8') as f:
    content = f.read()

target = 'import at.petrak.hexcasting.common.casting.actions.math.OpVectorReflect;'
replacement = 'import at.petrak.hexcasting.common.casting.actions.math.OpVectorReflect;\nimport at.petrak.hexcasting.common.casting.actions.math.OpVectorDampen;'

assert target in content, 'Target 1 not found'
content = content.replace(target, replacement)

math_target = '''    public static final ActionRegistryEntry VECTOR_REFLECT = make("vector_reflect",
        new ActionRegistryEntry(HexPattern.fromAngles("wdedwddw", HexDir.NORTH_WEST), OpVectorReflect.INSTANCE));'''

math_replacement = math_target + '''
    public static final ActionRegistryEntry VECTOR_DAMPEN = make("vector_dampen",
        new ActionRegistryEntry(HexPattern.fromAngles("waqawwa", HexDir.NORTH_EAST), OpVectorDampen.INSTANCE));'''

assert math_target in content, 'Target 2 not found'
content = content.replace(math_target, math_replacement)

with open('D:/GEMINI AGY/workspaces/HexMod_feature_kinetics/Common/src/main/java/at/petrak/hexcasting/common/lib/hex/HexActions.java', 'w', encoding='utf-8') as f:
    f.write(content)
print('Successfully updated HexActions.java with Vector Dampen action')

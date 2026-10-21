with open('D:/GEMINI AGY/workspaces/HexMod_feature_kinetics/Common/src/main/resources/assets/hexcasting/lang/en_us.flatten.json5', 'r', encoding='utf-8') as f:
    content = f.read()

target = '        smoothstep: "Smoothstep Interpolation",'
replacement = '''        smoothstep: "Smoothstep Interpolation",
        vector_spring: "Vector Spring Simulation",
        vector_reflect_normal: "Surface Normal Reflection",'''

assert target in content, 'Lang target not found'
content = content.replace(target, replacement)

target_desc = '        smoothstep: "Evaluates Hermite smoothstep interpolation between edge0 and edge1 for x.",'
replacement_desc = '''        smoothstep: "Evaluates Hermite smoothstep interpolation between edge0 and edge1 for x.",
        vector_spring: "Simulates a harmonic spring step with position, velocity, target, stiffness, and dt.",
        vector_reflect_normal: "Reflects a vector across a surface normal with given elasticity.",'''

assert target_desc in content, 'Lang desc target not found'
content = content.replace(target_desc, replacement_desc)

with open('D:/GEMINI AGY/workspaces/HexMod_feature_kinetics/Common/src/main/resources/assets/hexcasting/lang/en_us.flatten.json5', 'w', encoding='utf-8') as f:
    f.write(content)
print('Updated en_us.flatten.json5 successfully for spring and reflect!')

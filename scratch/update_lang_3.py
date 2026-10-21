with open('D:/GEMINI AGY/workspaces/HexMod_feature_kinetics/Common/src/main/resources/assets/hexcasting/lang/en_us.flatten.json5', 'r', encoding='utf-8') as f:
    content = f.read()

target = '        vector_reflect_normal: "Surface Normal Reflection",'
replacement = '''        vector_reflect_normal: "Surface Normal Reflection",
        vector_catmull_rom: "Catmull-Rom Spline Interpolation",'''

assert target in content, 'Lang target not found'
content = content.replace(target, replacement)

target_desc = '        vector_reflect_normal: "Reflects a vector across a surface normal with given elasticity.",'
replacement_desc = '''        vector_reflect_normal: "Reflects a vector across a surface normal with given elasticity.",
        vector_catmull_rom: "Evaluates a Catmull-Rom spline interpolation at parameter t given four control points.",'''

assert target_desc in content, 'Lang desc target not found'
content = content.replace(target_desc, replacement_desc)

with open('D:/GEMINI AGY/workspaces/HexMod_feature_kinetics/Common/src/main/resources/assets/hexcasting/lang/en_us.flatten.json5', 'w', encoding='utf-8') as f:
    f.write(content)
print('Updated en_us.flatten.json5 successfully for Catmull-Rom!')

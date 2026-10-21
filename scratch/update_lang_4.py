with open('D:/GEMINI AGY/workspaces/HexMod_feature_kinetics/Common/src/main/resources/assets/hexcasting/lang/en_us.flatten.json5', 'r', encoding='utf-8') as f:
    content = f.read()

target = '        vector_catmull_rom: "Catmull-Rom Spline Interpolation",'
replacement = '''        vector_catmull_rom: "Catmull-Rom Spline Interpolation",
        vector_hermite_spline: "Cubic Hermite Spline Interpolation",'''

assert target in content, 'Lang target not found'
content = content.replace(target, replacement)

target_desc = '        vector_catmull_rom: "Evaluates a Catmull-Rom spline interpolation at parameter t given four control points.",'
replacement_desc = '''        vector_catmull_rom: "Evaluates a Catmull-Rom spline interpolation at parameter t given four control points.",
        vector_hermite_spline: "Evaluates a cubic Hermite spline given start position P0, tangent T0, end position P1, tangent T1, and progress t.",'''

assert target_desc in content, 'Lang desc target not found'
content = content.replace(target_desc, replacement_desc)

with open('D:/GEMINI AGY/workspaces/HexMod_feature_kinetics/Common/src/main/resources/assets/hexcasting/lang/en_us.flatten.json5', 'w', encoding='utf-8') as f:
    f.write(content)
print('Updated en_us.flatten.json5 successfully for Hermite Spline!')

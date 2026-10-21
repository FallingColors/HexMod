with open('D:/GEMINI AGY/workspaces/HexMod_feature_kinetics/Common/src/main/resources/assets/hexcasting/lang/en_us.flatten.json5', 'r', encoding='utf-8') as f:
    content = f.read()

target = '        vector_hermite_spline: "Cubic Hermite Spline Interpolation",'
replacement = '''        vector_hermite_spline: "Cubic Hermite Spline Interpolation",
        vector_bounce: "Elastic Collision Bounce",'''

assert target in content, 'Lang target not found'
content = content.replace(target, replacement)

target_desc = '        vector_hermite_spline: "Evaluates a cubic Hermite spline given start position P0, tangent T0, end position P1, tangent T1, and progress t.",'
replacement_desc = '''        vector_hermite_spline: "Evaluates a cubic Hermite spline given start position P0, tangent T0, end position P1, tangent T1, and progress t.",
        vector_bounce: "Simulates an elastic collision bounce with restitution and tangential friction.",'''

assert target_desc in content, 'Lang desc target not found'
content = content.replace(target_desc, replacement_desc)

with open('D:/GEMINI AGY/workspaces/HexMod_feature_kinetics/Common/src/main/resources/assets/hexcasting/lang/en_us.flatten.json5', 'w', encoding='utf-8') as f:
    f.write(content)
print('Updated en_us.flatten.json5 successfully for Vector Bounce!')

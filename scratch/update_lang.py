with open('D:/GEMINI AGY/workspaces/HexMod_feature_kinetics/Common/src/main/resources/assets/hexcasting/lang/en_us.flatten.json5', 'r', encoding='utf-8') as f:
    content = f.read()

target = '        coerce_axial: "For a vector, coerce it to its nearest axial direction, a unit vector. For a number, return the sign of the number; 1 if positive, -1 if negative. In both cases, zero is unaffected.",'
replacement = '''        coerce_axial: "For a vector, coerce it to its nearest axial direction, a unit vector. For a number, return the sign of the number; 1 if positive, -1 if negative. In both cases, zero is unaffected.",
        vector_clamp_magnitude: "Clamps the length of a vector to a maximum value without altering its direction.",
        vector_bezier: "Evaluates a cubic Bézier curve at parameter t given four control points and t.",
        smoothstep: "Evaluates Hermite smoothstep interpolation between edge0 and edge1 for x.",'''

assert target in content, 'Target 2 not found'
content = content.replace(target, replacement)

with open('D:/GEMINI AGY/workspaces/HexMod_feature_kinetics/Common/src/main/resources/assets/hexcasting/lang/en_us.flatten.json5', 'w', encoding='utf-8') as f:
    f.write(content)
print('Updated tooltips in en_us.flatten.json5 successfully!')

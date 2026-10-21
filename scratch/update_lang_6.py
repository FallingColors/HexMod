with open('D:/GEMINI AGY/workspaces/HexMod_feature_kinetics/Common/src/main/resources/assets/hexcasting/lang/en_us.flatten.json5', 'r', encoding='utf-8') as f:
    content = f.read()

target = '        vector_bounce: "Elastic Collision Bounce",'
replacement = '''        vector_bounce: "Elastic Collision Bounce",
        vector_reflect: "Specular Vector Reflection",'''

assert target in content, 'Lang target not found'
content = content.replace(target, replacement)

target_desc = '        vector_bounce: "Simulates an elastic collision bounce with restitution and tangential friction.",'
replacement_desc = '''        vector_bounce: "Simulates an elastic collision bounce with restitution and tangential friction.",
        vector_reflect: "Computes the specular reflection vector v - 2 * (v . n) * n given velocity v and surface normal n.",'''

assert target_desc in content, 'Lang desc target not found'
content = content.replace(target_desc, replacement_desc)

with open('D:/GEMINI AGY/workspaces/HexMod_feature_kinetics/Common/src/main/resources/assets/hexcasting/lang/en_us.flatten.json5', 'w', encoding='utf-8') as f:
    f.write(content)
print('Updated en_us.flatten.json5 successfully for Vector Reflect!')

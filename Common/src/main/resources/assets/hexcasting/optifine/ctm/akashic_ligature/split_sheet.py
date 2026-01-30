# simple script to split up the big ctm sheet into the textures continuity wants
from PIL import Image

target = "akashic_ligature_sheet.png"

sheet = Image.open(target)
for i in range(47):
    left = i%12
    upper = i//12
    crop = sheet.crop((left*16, upper*16, left*16+16, upper*16+16))
    crop.save(f"{i}.png")
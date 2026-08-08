import os
from PIL import Image

src_dir = r"C:\Users\User\.gemini\antigravity\brain\99f8b630-4462-4dad-92d3-30b21bc25a45"
dest_dir = r"d:\Deepan\Minecraft_Villager_mod\src\main\resources\assets\bettervillagers"

images = {
    "wildwood_dialogue_1786109349321.jpg": "textures/gui/wildwood_dialogue.png",
    "quest_board_ui_1786109368849.jpg": "textures/gui/quest_board.png",
    "quest_board_front_1786109388881.jpg": "textures/block/quest_board_front.png",
    "quest_board_side_1786109408070.jpg": "textures/block/quest_board_side.png"
}

for src_name, dest_rel_path in images.items():
    src_path = os.path.join(src_dir, src_name)
    dest_path = os.path.join(dest_dir, dest_rel_path)
    
    print(f"Converting {src_path} to {dest_path}")
    
    with Image.open(src_path) as img:
        # Resize block textures exactly to 16x16
        if "block" in dest_rel_path:
            img = img.resize((16, 16), Image.Resampling.NEAREST)
        img.save(dest_path, "PNG")
        
print("Conversion complete.")

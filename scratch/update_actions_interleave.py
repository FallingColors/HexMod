import os

path = 'Common/src/main/java/at/petrak/hexcasting/common/lib/hex/HexActions.java'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

target = '''    public static final ActionRegistryEntry LIST_WINDOWS = make("list_windows",
        new ActionRegistryEntry(HexPattern.fromAngles("wdedwede", HexDir.NORTH_WEST), OpListWindows.INSTANCE));'''

addition = '''    public static final ActionRegistryEntry LIST_INTERLEAVE = make("list_interleave",
        new ActionRegistryEntry(HexPattern.fromAngles("waqawqwaq", HexDir.NORTH_EAST), OpListInterleave.INSTANCE));'''

if target in content:
    new_content = content.replace(target, target + '\n' + addition)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    print('SUCCESS')
else:
    print('NOT FOUND')

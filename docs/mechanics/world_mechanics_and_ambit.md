# World Mechanics & Casting Ambit

## 1. Ambit and World Boundaries (Issue #1305)

Previously, `CastingEnvironment#isVecInAmbit` and `CastingEnvironment#isEntityInRange` strictly required target positions and entity locations to satisfy both range validation (`isVecInRange`) and world boundary checks (`isVecInWorld`). 

This caused spellcasting or targeting operations near world height limits or world borders to instantly fail out-of-ambit even when the target was well within the caster's ambit radius.

### Fix
- Updated `CastingEnvironment#isVecInAmbit` to evaluate range (`isVecInRange`) without enforcing strict world boundary exclusion (`isVecInWorld`).
- Updated `CastingEnvironment#isEntityInRange` to rely on range checks (`isVecInRange`) and truename configuration rules without prematurely failing on world bounds.

---

## 2. Silk Touch on Amethyst Clusters (Issue #1282)

Amethyst clusters in Hex Casting inject custom loot tables (`inject/amethyst_cluster`) to yield amethyst dust and charged amethyst. 

### Fix
- Ensured proper condition gates on Silk Touch enchantments (`Enchantments.SILK_TOUCH`) so that harvesting amethyst clusters with Silk Touch correctly drops the cluster block intact, while non-silk touch harvesting produces amethyst dust and shards according to fortune and harvesting tier.

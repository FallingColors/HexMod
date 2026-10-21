# RFC: Next-Generation Geometric & Linear Algebra Operators for HexMod

## Abstract
This RFC proposes three new advanced mathematical and spatial operations for Hex Casting, expanding vector manipulation capabilities into 3D rotational matrices, quaternion spherical linear interpolation (slerp), and Axis-Aligned Bounding Box (AABB) raycast intersection queries.

---

## 1. Proposed Operators & Signatures

### A. 3x3 Matrix Multiplication (`matrix_multiply`)
- **Description**: Multiplies a 3x3 transformation/rotation matrix (represented as a list of 3 vector iotas) by a vector or another matrix.
- **Pattern Signature**: `waqwade` (Starting East `->`)
- **Media Cost**: $1/4$ Amethyst Dust (`ConstMediaAction`)

### B. Quaternion Slerp (`quaternion_slerp`)
- **Description**: Performs spherical linear interpolation between two orientation quaternions (represented as 4-tuples or Vec4/lists) by interpolation factor $t \in [0, 1]$.
- **Pattern Signature**: `wdedwdeqa` (Starting West `<-`)
- **Media Cost**: $1/2$ Amethyst Dust

### C. Bounding Box Raycast (`raycast_aabb`)
- **Description**: Tests ray intersection against an Axis-Aligned Bounding Box defined by min and max corner vectors, returning hit status and hit point.
- **Pattern Signature**: `qaeaqwde` (Starting North-West `↖`)
- **Media Cost**: $1$ Amethyst Dust (Raycast/World Interaction tier)

---

## 2. Collision & Stroke Geometry Validation
- All proposed pattern signatures have been checked against core Hex Casting action registries and addon namespaces. No geometric collisions or overlapping angle sequences exist.

## 3. Mishap Handling & Safety
- Matrix dimensions and quaternion normalizations are strictly validated; invalid inputs or degenerate vectors trigger appropriate mishaps (`MishapInvalidIota`, `MishapDivideByZero`).

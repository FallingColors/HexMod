# Vector Rotate Around Axis (`vector_rotate`)

Rotates a 3D vector around an arbitrary axis vector by a specified angle in radians using Rodrigues' rotation formula.

## Pattern Signature
- **Starting Direction**: East (`->`)
- **Angles**: `qawqe` (North-West, West, South-West, South-East)

```text
       o --- o
      /     /
     o --- o  (East Start)
```

## Stack Behavior
- **Arguments**:
  1. `vector` (`Vec3`): The vector to rotate.
  2. `axis` (`Vec3`): The axis of rotation (normalized internally).
  3. `angle` (`Double`): The rotation angle in radians.
- **Result**:
  1. `rotated_vector` (`Vec3`): The resulting rotated vector.

## Media Cost
- Standard constant media cost (`ConstMediaAction`).

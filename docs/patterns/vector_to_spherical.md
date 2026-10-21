# Vector to Spherical (`vector_to_spherical`)

Converts a Cartesian 3D vector `(x, y, z)` into spherical coordinates `(r, theta, phi)`.

## Pattern Signature
- **Starting Direction**: West (`<-`)
- **Angles**: `dedwde` (South-East, East, North-East, North-West)

```text
       o --- o
      /     /
     o --- o   (West Start)
```

## Stack Behavior
- **Arguments**:
  1. `vector` (`Vec3`): Cartesian coordinates `(x, y, z)`.
- **Result**:
  1. `spherical` (`Vec3`): Where `x` = radius $r$, `y` = inclination $\theta$, `z` = azimuth $\phi$.

## Media Cost
- Standard constant media cost (`ConstMediaAction`).

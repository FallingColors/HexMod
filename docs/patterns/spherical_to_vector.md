# Spherical to Vector (`spherical_to_vector`)

Converts spherical coordinates `(r, theta, phi)` back into a Cartesian 3D vector `(x, y, z)`.

## Pattern Signature
- **Starting Direction**: East (`->`)
- **Angles**: `qaqwa` (North-West, South-West, South, South-East)

```text
       o --- o
        \   /
         o o   (East Start)
```

## Stack Behavior
- **Arguments**:
  1. `spherical` (`Vec3`): Where `x` = radius $r$, `y` = inclination $\theta$ (polar angle from $+Z$), `z` = azimuth $\phi$ in the $X-Y$ plane.
- **Result**:
  1. `vector` (`Vec3`): Cartesian coordinates `(x, y, z)`.

## Media Cost
- Standard constant media cost (`ConstMediaAction`).

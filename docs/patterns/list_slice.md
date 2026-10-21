# List Slice (`list_slice`)

Extracts a sublist from a list between two given integer indices with rigorous bounds checking and mishaps.

## Pattern Signature
- **Starting Direction**: South-East (`↘`)
- **Angles**: `dedqwde`

```text
         o
        / \
       o---o   (South-East Start)
```

## Stack Behavior
- **Arguments**:
  1. `list` (`List`): The source list.
  2. `from_index` (`Int`): Start index.
  3. `to_index` (`Int`): End index.
- **Result**:
  1. `sublist` (`List`): The sliced portion of the list.

## Mishaps
- Throws `MishapIndexOutOfBounds` if indices are out of range.

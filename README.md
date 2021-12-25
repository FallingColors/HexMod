# Hex

A minecraft mod about casting spells, inspired by PSI.

## To Cast A Spell

- Get a Wand, which stores mana.
- Right-click with it to start casting a *Hex*.
- Look around to draw a *pattern*, a sequence of lines on the invisible hexagonal grid that permeates reality.
- When you are done with your pattern, release right-click. Hopefully, that pattern corresponds to an *action*. (If not,
  you blow up.)
- Actions manipulate a stack of data. The stack starts empty when you first start casting a hex. Some actions just push
  data, while some pop off some arguments and push some arguments in return.
- Certain actions, will push a *spell* to the stack. When the stack is empty except for one spell, the hex is
  successfully cast with the given effect.
  
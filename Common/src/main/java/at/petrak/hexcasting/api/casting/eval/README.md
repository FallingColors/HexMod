# How Casting Works

because I keep forgetting.

- [`CastingVM`][] is the casting virtual machine. It is what figures out what to do with an incoming iota to be
  executed,
  producing a functional state update. This is transient and is reconstructed every time the whole state needs to be
  saved and loaded.
- [`CastingImage`][] is the state of the cast itself. If [`CastingVM`][] is an emulator, [`CastingImage`][] is a
  snapshot of the
  emulator's memory. This is the only thing serialized to NBT.
- [`CastingEnvironment`][] is what is doing the casting, abstractly. Stuff like "the player with a staff," "the player
  with a trinket," "a spell circle." This is an abstract class that Hexcasting (and addons!) make subclasses of.

## Beware The Pipeline

1. An iota or list of iotas come to be executed. This is the entrypoint to the VM,
   `CastingVM#queueAndExecuteIotas`, and returns an [`ExecutionClientView`][] (might change later).
2. Those iotas are put into a [`ContinuationFrame`][] (specifically, [`FrameEvaluate`][]).
3. While there are still frames, control flows to the top one.
4. The frame (usually) returns control flow back to the VM by calling `CastingVM#executeInner` with an iota.
5. `executeInner` first does some pre-checks with intro/retro/consideration (like escaping embedded iotas), but usually
   makes sure that the passed iota is a pattern and passes *that* on to `executePattern`.
6. `executePattern` is where the execution actually happens. The pattern is matched to an action or special handler.
   and executed.
7. That execution doesn't mutate anything, but returns a [`CastResult`]. It contains:
    - the rest of the current continuation (read as: the patterns to execute next);
    - the updated state of the [`CastingImage`];
    - a list of `OperatorSideEffects`, like withdrawing media, casting spells or mishaping;
    - misc display info like the color the pattern should be when drawn to the staff GUI and the sound.
8. Each of the side effects is applied to the world in turn. If any of them make the casting stop (like mishaping),
   then it does!
9. If there's still iotas left in the current continuation, then control goes back to step 5, called on the next iota in
   continuation.
10. Otherwise, control goes to the top frame of the stack (step 3). And if there are no stack frames, execution is
    finished! What a ride.

## The Hell's A Continuation And A Continuation Frame

IDFK ask Alwinfy.

But as I understand it, a [continuation frame][ContinuationFrame] tells the VM what to do *next*.

While there are frames left on the stack (not the normal stack, a special execution stack), it is popped and
queried. It will then operate the VM into executing a [continuation][Continuation] in a certain way.

Continuations are just a linked list of iotas to execute. The VM goes through each one and executes them.

- For staffcasting, each pattern drawn spins up the VM with a `FrameEvaluate`, which will provide exactly one
  continuation containing the pattern.
- For trinket casting, the VM gets a `FrameEvaluate` again, but the continuation list it provides has
  all the patterns in the trinket.
- Hermes' Gambit pushes a new `FrameEvaluate` to the stack with the pattern list argument inside it. This doesn't clear
  the continuation underneath, so once those patterns are through execution control goes right back to where it was
  interrupted.
- Thoth's and Charon's do something with different kinds of stack frames, I don't really understand it.
- Iris' gambit is waaaay outside of my pay grade

[CastingVM]: vm/CastingVM.kt

[CastingImage]: vm/CastingImage.kt

[CastingEnvironment]: CastingEnvironment.java

[ExecutionClientView]: ExecutionClientView.kt

[ContinuationFrame]: vm/ContinuationFrame.kt

[Continuation]: vm/SpellContinuation.kt

[FrameEvaluate]: vm/FrameEvaluate.kt

[CastResult]: CastResult.kt


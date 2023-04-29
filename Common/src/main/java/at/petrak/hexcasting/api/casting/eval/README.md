# How Casting Works

because I keep forgetting.

- [CastingVM][] is the casting virtual machine. It is what figures out what to do with an incoming iota to be
  executed,
  producing a functional state update. This is transient and is reconstructed every time the whole state needs to be
  saved and loaded.
- [CastingImage][] is the state of the cast itself. If [CastingVM][] is an emulator, [CastingImage][] is a
  snapshot of the
  emulator's memory. This is the only thing serialized to NBT.
- [CastingEnvironment][] is what is doing the casting, abstractly. Stuff like "the player with a staff," "the player
  with a trinket," "a spell circle." This is an abstract class that Hexcasting (and addons!) make subclasses of.

## Beware The Pipeline

1. An iota or list of iotas come to be executed. This is the entrypoint to the VM,
   `CastingVM#queueAndExecuteIotas`, and returns an [ExecutionClientView][] (might change later).
2. Those iotas are put into a [ContinuationFrame][] (specifically, [FrameEvaluate][]).
3. While there are still frames, control flows to the top one.
4. The frame (usually) returns control flow back to the VM by calling `CastingVM#executeInner` with an iota.
5. `executeInner` first does some pre-checks with intro/retro/consideration (like escaping embedded iotas), but usually
   makes sure that the passed iota is a pattern and passes *that* on to `executePattern`.
6. `executePattern` is where the execution actually happens. The pattern is matched to an action or special handler.
   and executed.
7. The execution returns an [OperationResult], containing:
    - the rest of the current continuation (read as: the patterns to execute next);
    - the updated state of the [CastingImage];
    - a list of `OperatorSideEffects`, like withdrawing media, casting spells or mishaping;
8. The operation result is composed with some misc display info, like the color the pattern should be when drawn to
   the staff GUI and the sound.
9. Each of the side effects is applied to the world in turn. If any of them make the casting stop (like mishaping),
   then it does!
10. If there's still iotas left in the current continuation, then control goes back to step 5, called on the next iota
    in the continuation.
11. Otherwise, control goes to the top frame of the stack (step 3). And if there are no stack frames, execution is
    finished! What a ride.

## The Hell's A Continuation And A Continuation Frame

~~IDFK ask Alwinfy.~~ <- Disregard this, I am a goober

A [continuation][Continuation] roughly represents a "call stack" in a more traditional VM structure.
It is a stack (implemented as a linked list) of [continuation frames][ContinuationFrame] ("call frames" in more
traditional nomenclature, where each frame is usually a list of iotas remaining to execute.

While there are frames left on the Continuation stack, the topmost frame is told to execute. During execution,
a frame can push more frames to execute (Hermes' Gambit does this), pop itself (once a Hermes execution
finishes), or even remove frames below it (Charon's Gambit)!

There are three types of frames:

1. [FrameEvaluate][] is a list of iotas to execute. The VM will step through the list and execute each pattern.
   Once its patterns are exhausted, it pops itself (returning flow control to the frame below).
- For staffcasting, each pattern drawn spins up the VM with a `FrameEvaluate` containing a single pattern.
- For trinket casting, the VM gets a `FrameEvaluate` again, but the continuation list it provides has
  all the patterns in the trinket.
- Hermes' Gambit pushes a new `FrameEvaluate` to the continuation stack with the pattern list argument inside it.
2. [FrameForEach][] manages the state of a Thoth's Gambit.
- It stores the template data-stack, the list of remaining values to foreach over, and the accumulated output list.
- When told to execute, it will push a `FrameEvaluate` for the next iteration and push the next value;
  it will also append the previous iteration's values to the accumulated output.
- Once finished, `FrameForEach` will append the final output list to the data stack, then pop itself.
3. [FrameFinishEval][] does not perform any function; when executed it simply pops itself.
- Its purpose is to serve as a marker for Charon's Gambit to know how many frames to abort.
  In the "call stack" analogy, this is a "catch" handler which serves as a counterpart to Charon's Gambit "throwing".
- Charon's Gambit pops continuation frames until it reaches a FrameFinishEval.
- Hermes' Gambit pushes this before pushing a FrameEvaluate, so that a Charon will abort down to the FrameEvaluate, and no further.

[CastingVM]: vm/CastingVM.kt

[CastingImage]: vm/CastingImage.kt

[CastingEnvironment]: CastingEnvironment.java

[ExecutionClientView]: ExecutionClientView.kt

[ContinuationFrame]: vm/ContinuationFrame.kt

[Continuation]: vm/SpellContinuation.kt

[FrameEvaluate]: vm/FrameEvaluate.kt

[FrameForEach]: vm/FrameForEach.kt

[FrameFinishEval]: vm/FrameFinishEval.kt

[OperationResult]: OperationResult.kt

[CastResult]: CastResult.kt

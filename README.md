# SortSphere

A self-contained Java Swing game for teaching sorting algorithms visually. Learners enter 2–12 comma- or space-separated whole numbers and watch coloured balls move into order.

## Run

From the `SortSphere` folder, use a JDK (17+ recommended):

```powershell
javac -d out src\SortSphereGame.java
java -cp out SortSphereGame
```

## Learning mechanics

- Bubble, selection, insertion, and merge sort each expose their own movement pattern.
- Gold balls are the current comparison; coral balls swap places.
- Completing an algorithm earns a badge. Complete all four for the Sorting Champion prompt.
- **Save certificate** exports a personalised PNG certificate through a normal file chooser.

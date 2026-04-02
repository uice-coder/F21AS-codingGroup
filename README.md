# F21AS Coffee Shop - Stage 2

This repository root is the authoritative Stage 2 submission project for the F21AS coffee shop simulation.
The supported runtime model is simple and coursework-friendly: run the app from the project root with `data/` and `assets/` kept as normal external folders.

## Project Layout

```text
F21AS-codingGroup/
├─ src/         Java source code
├─ test/        JUnit tests
├─ data/        CSV input files
├─ assets/      GUI image assets
├─ build.bat    Windows build script
├─ build.sh     Linux/Mac build script
└─ .classpath   Eclipse project classpath
```

## Main Class

- `coffeeshop.CoffeeShopApp`

## Stage 2 Features

- One producer thread gradually adds orders into a shared queue
- Multiple staff threads process queued orders
- Synchronized producer-consumer queue using `wait()` / `notifyAll()`
- Swing GUI for queue state, staff state, and live simulation activity
- Singleton simulation logger that writes `simulation_log.txt`
- MVC/Observer structure in the simulation layer

## Supported Runtime Model

- Supported: compile to `bin/` and run from the repository root
- Supported: Eclipse run configuration using the repository root as the working directory
- Not supported as a fully self-contained packaged JAR: the application intentionally uses external `data/` and `assets/` folders

## Build and Run

### Option A: Windows

```bat
build.bat
java -cp bin coffeeshop.CoffeeShopApp
```

### Option B: Linux / Mac

```bash
chmod +x build.sh
./build.sh
java -cp bin coffeeshop.CoffeeShopApp
```

### Option C: Manual

```bash
mkdir bin
find src -name "*.java" | xargs javac -d bin -sourcepath src
java -cp bin coffeeshop.CoffeeShopApp
```

## Eclipse Setup

1. Import the repository root `F21AS-codingGroup/` as an Eclipse project.
2. Ensure `src/` is a source folder for application code.
3. Ensure `test/` is a source folder for JUnit tests.
4. Keep `data/` and `assets/` at the repository root.
5. Run `coffeeshop.CoffeeShopApp` as a Java Application.

## Data and Assets

- Menu CSV: `data/menu.csv`
- Orders CSV: `data/orders.csv`
- Product/category images: `assets/images/...`
- Simulation log output: `simulation_log.txt` in the repository root

## Notes

- The root `test/` folder contains the existing coursework tests.
- The code now resolves project resources by locating the project root and then loading `data/` and `assets/` from there.
- Build output such as `bin/`, `sources.txt`, and `simulation_log.txt` should be treated as generated artifacts.
- The project does not currently aim to ship a fully self-contained JAR; the reliable supported mode is project-root execution.

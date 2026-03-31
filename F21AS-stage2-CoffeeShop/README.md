# F21AS Coffee Shop — Stage 2

## How to Build & Run

### Option A: Shell (Linux/Mac)
```bash
chmod +x build.sh
./build.sh
java -jar CoffeeShop.jar
```

### Option B: Windows
```
build.bat
java -jar CoffeeShop.jar
```

### Option C: Manual
```bash
mkdir bin
find src -name "*.java" | xargs javac -d bin -sourcepath src
java -cp bin coffeeshop.CoffeeShopApp
```

## What's New in Stage 2

1. Thread-based simulation: one thread per staff member + one producer thread
2. CustomerQueue: synchronized wait/notify queue (producer-consumer)
3. SimulationLog: Singleton, writes to simulation_log.txt on exit
4. SimulationGUI: live queue + staff display (Observer/MVC pattern)
5. Speed control and Add Staff at runtime

## Design Patterns
- Singleton: SimulationLog
- Observer: SimulationObserver interface notified on every state change
- MVC: SimulationController (model/ctrl), SimulationGUI (view)
- Producer-Consumer: CustomerProducer + StaffMember threads via CustomerQueue

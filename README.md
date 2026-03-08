# F21AS Coffee Shop Simulation – Stage 1

## Project Structure

```
F21AS-codingGroup/
├── src/
│   ├── coffeeshop/
│   │   └── CoffeeShopApp.java          ← Main entry point
│   ├── model/
│   │   ├── Item.java
│   │   ├── Order.java
│   │   ├── Menu.java
│   │   └── Manager.java
│   ├── gui/
│   │   └── ShopGUI.java
│   ├── exception/
│   │   └── InvalidDataException.java
│   └── util/
│       └── Validator.java
├── test/
│   └── coffeeshop/
│       ├── ItemTest.java
│       └── ManagerTest.java
└── data/
    ├── menu.csv
    └── orders.csv
```

---

## How to Set Up in Eclipse

1. **File → Open Projects from File System...**
   - Click `Directory...` and select the project root folder

2. **Add source folders** (if not auto-detected):
   - Right-click project → Properties → Java Build Path → Source tab
   - Add `src` folder (main code)
   - Add `test` folder (JUnit tests)

3. **Copy `data/` folder** into the **root of the Eclipse project** (same level as `src/`)

4. **Add JUnit 5 library:**
   - Right-click project → Build Path → Add Libraries → JUnit → JUnit 5

5. **Run the application:**
   - Right-click `CoffeeShopApp.java` → Run As → Java Application

6. **Run the JUnit tests:**
   - Right-click `ItemTest.java` or `ManagerTest.java` → Run As → JUnit Test

---

## Discount Rules

| Rule      | Condition                          | Discount |
|-----------|------------------------------------|----------|
| Combo     | 1+ Beverage AND 2+ Food items      | 20% off  |
| Threshold | Subtotal > £50                     | £5 off   |

Only the **best** discount is applied (no stacking).

---

## ID Formats

- **Item ID:** `[A-Z]+-[0-9]{3}` e.g. `COF-001`, `FOOD-002`, `OTH-123`
- **Customer ID:** `CUST-[0-9]{3}` e.g. `CUST-001`
- **Timestamp:** `yyyy-MM-dd HH:mm:ss` e.g. `2025-01-15 09:30:00`

---

## Key Classes

| Class                  | Responsibility                                                              |
|------------------------|-----------------------------------------------------------------------------|
| `CoffeeShopApp`        | Main entry point; loads CSV files and launches the GUI                      |
| `Item`                 | Menu item entity; constructor validates ID, price, name and category; throws `InvalidDataException` |
| `Order`                | A multi-item order basket; holds a list of Items with discount and final price |
| `Menu`                 | HashMap-based menu store; loads and validates items from CSV                |
| `Manager`              | Business logic: loads orders, calculates discounts, generates sales report  |
| `ShopGUI`              | Swing GUI – menu browser, basket, live bill display, report viewer          |
| `InvalidDataException` | Custom checked exception for data validation errors                         |
| `Validator`            | Static utility methods for validating IDs, prices, timestamps and CSV headers |

---

## Packages

| Package     | Contents                          |
|-------------|-----------------------------------|
| `coffeeshop`| `CoffeeShopApp` (entry point)     |
| `model`     | `Item`, `Order`, `Menu`, `Manager`|
| `gui`       | `ShopGUI`                         |
| `exception` | `InvalidDataException`            |
| `util`      | `Validator`                       |

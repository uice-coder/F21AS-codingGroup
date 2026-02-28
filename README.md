# F21AS Coffee Shop Simulation – Stage 1

## Project Structure

```
coffeeshop/
├── src/
│   └── coffeeshop/
│       ├── CoffeeShopApp.java          ← Main entry point
│       ├── exception/
│       │   └── InvalidDataException.java
│       ├── model/
│       │   ├── Item.java
│       │   ├── Order.java
│       │   ├── Menu.java
│       │   └── Manager.java
│       └── gui/
│           └── ShopGUI.java
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

1. **File → New → Java Project**
   - Project name: `CoffeeShop`

2. **Add source folders:**
   - Right-click project → Properties → Java Build Path → Source tab
   - Add `src` folder (main code)
   - Add `test` folder (JUnit tests)

3. **Copy all `.java` files** into Eclipse's matching package folders under `src/` and `test/`

4. **Copy `data/` folder** into the **root of the Eclipse project** (same level as `src/`)

5. **Add JUnit 5 library:**
   - Right-click project → Build Path → Add Libraries → JUnit → JUnit 5

6. **Run the application:**
   - Right-click `CoffeeShopApp.java` → Run As → Java Application

7. **Run the JUnit tests:**
   - Right-click `ItemTest.java` or `ManagerTest.java` → Run As → JUnit Test

---

## Discount Rules

| Rule | Condition | Discount |
|------|-----------|----------|
| Small Combo | 1 Beverage + 1 Food item | 8% off |
| Large Combo | 1 Beverage + 2+ Food items | 15% off |
| High Value | Subtotal > £50 | £5 off |

Only the **best** discount is applied (not cumulative).

---

## ID Formats

- **Item ID:** `[A-Z]{3}-[0-9]{3}` e.g. `BEV-001`, `FOO-042`, `OTH-123`
- **Customer ID:** `CUST-[0-9]{3}` e.g. `CUST-001`
- **Timestamp:** `yyyy-MM-dd HH:mm:ss` e.g. `2025-01-15 09:30:00`

---

## Key Classes

| Class | Responsibility |
|-------|---------------|
| `Item` | Menu item with validation in constructor; throws `InvalidDataException` |
| `Order` | One order line (one item) linked to a customer and timestamp |
| `Menu` | HashMap-based menu store; loads & validates CSV |
| `Manager` | Business logic: loads orders, calculates discounts, generates report |
| `ShopGUI` | Swing GUI – menu browser, basket, bill display, report viewer |
| `InvalidDataException` | Custom checked exception for data validation errors |

---

## 前端说明 (GUI Notes)

**不需要单独的前端工具！** 前端和后端都在同一个 Eclipse Java 项目里：
- **后端** = `model/` 包（Item, Order, Menu, Manager）
- **前端** = `gui/ShopGUI.java`（Java Swing，纯 Java，不需要 HTML/CSS/React）
- 在同一个 Eclipse 里写所有代码，直接 Run As Java Application 即可运行

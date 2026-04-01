# F21AS 咖啡店模拟系统 — 快速入门指南

## 项目概述

本项目是 Heriot-Watt University F21AS 课程的课程作业，模拟一家咖啡店的点单与运营流程。项目分为两个阶段：

- **Stage 1**：交互式点单界面，支持从菜单选取商品、计算折扣、生成销售报告。
- **Stage 2**：多线程队列模拟，模拟顾客排队、服务员并行处理订单的全过程。

---

## 项目结构说明

```
F21AS-codingGroup/
├── data/                        数据文件目录
│   ├── menu.csv                 菜单数据（商品ID、名称、描述、价格、类别）
│   └── orders.csv               历史订单数据（订单ID、顾客ID、商品ID、时间戳）
│
├── src/                         源代码目录
│   ├── coffeeshop/              程序入口
│   │   ├── CoffeeShopApp.java   Stage 1 入口：加载数据，启动交互式点单 GUI
│   │   └── Stage2App.java       Stage 2 入口：加载数据，启动多线程模拟
│   │
│   ├── model/                   数据模型层（Stage 1 核心）
│   │   ├── Item.java            菜单商品实体，包含ID、名称、价格、类别等
│   │   ├── Menu.java            菜单管理器，从 CSV 加载所有商品，提供 O(1) 查找
│   │   ├── Order.java           订单实体，包含商品列表与折扣计算逻辑
│   │   └── Manager.java         业务逻辑核心：加载订单、计算折扣、生成销售报告
│   │
│   ├── gui/                     图形界面层
│   │   ├── ShopGUI.java         Stage 1 GUI：菜单浏览、购物篮、下单、报告
│   │   ├── SimulationModel.java Stage 2 MVC Model：持有队列与服务员状态快照
│   │   └── SimulationView.java  Stage 2 MVC View：实时展示队列与服务员状态
│   │
│   ├── simulation/              Stage 2 模拟层（多线程）
│   │   ├── CustomerQueue.java   线程安全的顾客队列（手动 synchronized）
│   │   ├── Staff.java           服务员线程：从队列取订单并模拟处理时间
│   │   ├── OrderProducer.java   生产者线程：按间隔将订单逐条加入队列
│   │   ├── SimulationController.java  模拟编排器：启动所有线程，监控结束
│   │   └── StaffStatus.java     服务员状态快照（不可变数据类）
│   │
│   ├── observer/                Observer 设计模式接口
│   │   ├── QueueObserver.java   观察队列变化
│   │   ├── StaffObserver.java   观察服务员状态变化
│   │   └── ModelObserver.java   GUI View 实现此接口以接收模型更新
│   │
│   ├── log/                     日志模块
│   │   └── EventLog.java        Singleton 事件日志：记录所有模拟事件，退出时写入文件
│   │
│   ├── exception/               自定义异常
│   │   └── InvalidDataException.java  数据校验失败时抛出的受检异常
│   │
│   └── util/                    工具类
│       └── Validator.java       静态校验方法：商品ID格式、价格范围、顾客ID、时间戳
│
└── test/                        JUnit 5 单元测试
    └── coffeeshop/
        ├── ItemTest.java        测试 Item 构造函数与数据校验
        └── ManagerTest.java     测试折扣计算逻辑与销售统计
```

---

## 各模块详细说明

### model 层（两个阶段共用）

| 类 | 作用 |
|----|------|
| `Item` | 表示菜单上的一个商品。构造时自动校验 ID 格式（如 `COF-001`）和价格，格式非法则抛出 `InvalidDataException`。 |
| `Menu` | 以 `HashMap` 存储所有商品，支持按 ID 的 O(1) 查找。启动时从 `menu.csv` 读取，非法行跳过并记录错误。 |
| `Order` | 一张订单，包含多个 `Item`。调用 `calculateFinalPrice()` 时自动选取最优折扣方案。 |
| `Manager` | 从 `orders.csv` 加载订单，将相同 `orderId` 的多行合并为一张订单，生成完整的销售报告。 |

### 折扣规则

两条规则互斥，取折扣金额较大者：

| 规则 | 触发条件 | 优惠 |
|------|---------|------|
| 组合折扣 | 订单包含 ≥1 件饮料 AND ≥2 件食品 | 总价打八折（20% off） |
| 满减折扣 | 订单小计 > £50 | 减 £5 |

### simulation 层（Stage 2）

| 类 | 作用 |
|----|------|
| `CustomerQueue` | 核心共享队列。`enqueue()` 添加订单并唤醒等待的服务员；`dequeue()` 在队列为空时阻塞（`wait()`）；`close()` 在生产者结束后通知所有服务员退出。**不使用** Java 内置线程安全集合，完全手动同步。 |
| `OrderProducer` | 生产者线程。按设定的时间间隔将预加载的订单逐条加入队列，全部添加完毕后调用 `queue.close()`。 |
| `Staff` | 服务员线程。循环调用 `dequeue()` 取订单，按商品类别模拟处理时间（饮料 2-4 秒，食品 6-10 秒），完成后通知观察者更新 GUI。 |
| `SimulationController` | 编排器。创建并启动所有线程，运行监控线程（Monitor Thread）等待全部服务员退出后触发收尾工作（生成报告、写日志文件）。提供 `setSpeedMultiplier()` 方法支持运行时调速。 |

### GUI 层（Stage 2，MVC 模式）

| 类 | 角色 | 作用 |
|----|------|------|
| `SimulationModel` | **Model** | 持有队列快照和服务员状态。实现 `QueueObserver` 和 `StaffObserver`，接收后台线程的变更通知，通过 `SwingUtilities.invokeLater()` 安全地转发给 View。 |
| `SimulationView` | **View** | Swing 窗口。实现 `ModelObserver`，在 EDT 上直接更新界面。左侧显示排队顾客列表，右侧显示每位服务员的当前状态（处理中/空闲，订单详情）。 |
| `SimulationController` | **Controller** | 负责线程生命周期管理，并响应用户的速度控制操作。 |

### Observer 模式数据流

```
CustomerQueue ──onQueueChanged()──→ SimulationModel ──onModelUpdated()──→ SimulationView
Staff         ──onStaffStatusChanged()──→ SimulationModel
```

### log 模块（Singleton 模式）

`EventLog` 是全局唯一的日志实例，通过 `EventLog.getInstance()` 获取。所有后台线程均可安全并发调用 `log()` 方法。模拟结束后，完整日志写入 `data/simulation_log.txt`。

---

## 如何在 Eclipse 中运行

### 环境准备

1. 安装 **JDK 11** 或更高版本。
2. 打开 Eclipse，选择 **File → Open Projects from File System**，选中 `F21AS-codingGroup` 文件夹导入项目。
3. 右键项目 → **Properties → Java Build Path → Source**，确认 `src` 和 `test` 均已添加为 Source Folder。
4. 右键项目 → **Build Path → Add Libraries → JUnit → JUnit 5**，添加测试依赖。
5. 确认 `data/` 文件夹与 `src/` 同级（Eclipse 运行时工作目录为项目根目录）。

### 运行 Stage 1（交互式点单）

1. 打开 `src/coffeeshop/CoffeeShopApp.java`。
2. 右键 → **Run As → Java Application**。
3. 程序启动后会加载菜单和历史订单，然后弹出点单界面：
   - 在左侧菜单列表中**双击**或选中后点击 **Add to Basket** 添加商品。
   - 输入顾客 ID（格式：`CUST-001`），点击 **Place Order** 下单。
   - 点击 **Generate Report** 查看销售报告。
   - 点击 **Exit** 退出（可选择是否先生成报告）。

### 运行 Stage 2（多线程模拟）

1. 打开 `src/coffeeshop/Stage2App.java`。
2. 右键 → **Run As → Java Application**。
3. 程序会自动加载订单数据并启动模拟窗口：
   - **左侧**：实时显示正在排队的顾客列表。
   - **右侧**：每位服务员的当前状态（处理中显示订单详情，空闲时显示等待提示）。
   - **底部滑块**：拖动可调节模拟速度（0.5x 至 4x），即时生效。
4. 所有订单处理完毕后，程序自动弹出销售报告，点击确认后退出。
5. 退出后可在 `data/simulation_log.txt` 查看完整的事件日志。

### 运行单元测试

1. 打开 `test/coffeeshop/ItemTest.java` 或 `ManagerTest.java`。
2. 右键 → **Run As → JUnit Test**。

---

## 数据文件格式

### menu.csv

```
itemId,name,description,price,category
COF-001,Espresso,Strong black coffee,2.50,Beverage
FOO-001,Croissant,Butter croissant,2.50,Food
```

- `itemId`：格式必须为 `[大写字母]+-[三位数字]`，例如 `COF-001`、`FOOD-002`。
- `category`：建议使用 `Beverage`、`Food`、`Other`（影响折扣计算和 Stage 2 处理时间）。

### orders.csv

```
orderId,customerId,itemId,timestamp
ORD-001,CUST-001,COF-001,2025-01-15 09:00:00
ORD-001,CUST-001,FOO-001,2025-01-15 09:00:00
```

- 同一 `orderId` 的多行会被合并为一张订单。
- `customerId`：格式必须为 `CUST-[三位数字]`。
- `timestamp`：格式必须为 `yyyy-MM-dd HH:mm:ss`。
- 格式非法的行会被跳过并在控制台输出错误信息，不影响其余数据加载。

---

## 常见问题

**Q：程序启动时提示找不到数据文件怎么办？**
确保 Eclipse 的运行工作目录为项目根目录（`F21AS-codingGroup/`），即 `data/` 文件夹与 `src/` 同级。可在 **Run Configurations → Arguments → Working directory** 中确认。

**Q：Stage 2 模拟速度太慢，看不出变化？**
拖动底部速度滑块至 2x 或 4x，处理时间将等比缩短。

**Q：如何增加或减少服务员数量？**
在 `Stage2App.java` 的命令行参数中指定，或直接修改 `SimulationController.DEFAULT_STAFF_COUNT` 的值（默认为 3）。

**Q：日志文件在哪里？**
Stage 2 结束后，事件日志自动写入 `data/simulation_log.txt`。

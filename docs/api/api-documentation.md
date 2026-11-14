# API 接口说明

依据当前 Controller 的映射与方法签名整理。未启动后端生成运行期 OpenAPI；本清单不是联调通过证明。接口业务实现与约束以对应 Service/DTO 为准。

## 基础约定

后端地址默认 `http://localhost:8080`；前端通过同源 `/api` 代理，转发时移除 `/api`。除两个登录接口外，所有业务路由分别要求管理员或用户 Token，请求头名为 `token`，不能互换身份。

成功：`{"code":1,"msg":"success","data":...}`；业务失败：`{"code":0,"msg":"错误信息","data":null}`。当前异常处理通常仍返回 HTTP 200，客户端和 JMeter 应同时断言业务 `code`。分页响应为 `data.total` 和 `data.records`。

GET 的普通参数与查询 DTO 由查询字符串绑定；`@PathVariable` 表示路径参数；`@RequestBody` 使用 JSON；上传使用 multipart/form-data 字段 `file`，上限 5MB。分页默认 page=1/pageSize=10，但部分旧 DTO 缺少范围校验。

## 完整路由

签名保留 Java 类型和绑定注解，以准确说明请求体、查询和路径参数。DTO 字段见后文。`routes.json` 提供相同路由的机器可读清单。

| 方法 | 路径 | 身份 | Controller / 方法签名 |
|---|---|---|---|
| POST | `/user/addressBook` | USER | AddressBookController.java / `Result<Long> save( @RequestBody AddressBook addressBook)` |
| GET | `/user/addressBook/list` | USER | AddressBookController.java / `Result<List<AddressBook>> list()` |
| POST | `/admin/category` | ADMIN | CategoryController.java / `Result save( @RequestBody @Valid CategorySaveDTO dto )` |
| GET | `/admin/category/page` | ADMIN | CategoryController.java / `Result<PageResult<Category>> page(CategoryPageQueryDTO dto)` |
| PUT | `/admin/category` | ADMIN | CategoryController.java / `Result update( @RequestBody @Valid CategoryUpdateDTO dto )` |
| DELETE | `/admin/category` | ADMIN | CategoryController.java / `Result delete(Long id)` |
| GET | `/admin/category/list` | ADMIN | CategoryController.java / `Result<List<Category>> list( @RequestParam Integer type)` |
| POST | `/admin/common/upload` | ADMIN | CommonController.java / `Result<String> upload( @RequestParam("file") MultipartFile file)` |
| GET | `/admin/dish/page` | ADMIN | DishController.java / `Result<PageResult> page( @ParameterObject DishPageQueryDTO dto )` |
| POST | `/admin/dish` | ADMIN | DishController.java / `Result<Void> save(@RequestBody @Valid DishDTO dto)` |
| PUT | `/admin/dish` | ADMIN | DishController.java / `Result update(@RequestBody DishDTO dto)` |
| DELETE | `/admin/dish` | ADMIN | DishController.java / `Result delete(Long id)` |
| POST | `/admin/dish/status/{status}` | ADMIN | DishController.java / `Result status( @PathVariable Integer status, Long id )` |
| GET | `/admin/dish/{id}` | ADMIN | DishController.java / `Result<DishDTO> getById( @PathVariable Long id)` |
| GET | `/admin/dishFlavor/{dishId}` | ADMIN | DishFlavorController.java / `Result<List<DishFlavor>> getByDishId( @PathVariable Long dishId)` |
| POST | `/admin/dishFlavor` | ADMIN | DishFlavorController.java / `Result save( @RequestBody DishFlavor dishFlavor)` |
| DELETE | `/admin/dishFlavor/{id}` | ADMIN | DishFlavorController.java / `Result delete( @PathVariable Long id)` |
| POST | `/admin/employee/login` | 无需 Token | EmployeeController.java / `Result<EmployeeLoginVO> login( @RequestBody EmployeeLoginDTO employeeLoginDTO)` |
| GET | `/admin/employee/list` | ADMIN | EmployeeController.java / `Result<List<Employee>> list( @Parameter( name = "token", description = "登录成功后返回的JWT Token", required = true, in = ParameterIn.HEADER ) @RequestHeader("token") String token)` |
| GET | `/admin/employee/page` | ADMIN | EmployeeController.java / `Result<PageResult<EmployeeVO>> page( @Parameter( description = "JWT Token", required = true, in = ParameterIn.HEADER ) @RequestHeader("token") String token, @ParameterObject EmployeePageQueryDTO employeePageQueryDTO)` |
| POST | `/admin/employee` | ADMIN | EmployeeController.java / `Result<String> save( @RequestBody EmployeeSaveDTO employeeSaveDTO)` |
| GET | `/admin/employee/{id}` | ADMIN | EmployeeController.java / `Result<EmployeeVO> getById( @PathVariable Long id)` |
| PUT | `/admin/employee` | ADMIN | EmployeeController.java / `Result<String> update( @RequestBody EmployeeUpdateDTO employeeUpdateDTO)` |
| POST | `/admin/employee/status/{status}` | ADMIN | EmployeeController.java / `Result<String> updateStatus( @PathVariable Integer status, @RequestParam Long id)` |
| DELETE | `/admin/employee/{id}` | ADMIN | EmployeeController.java / `Result<String> delete( @PathVariable Long id)` |
| GET | `/admin/order/conditionSearch` | ADMIN | OrdersController.java / `Result<PageResult<Orders>> conditionSearch( @Valid OrdersPageQueryDTO dto)` |
| GET | `/admin/order/details/{id}` | ADMIN | OrdersController.java / `Result<OrdersDetailDTO> details( @PathVariable Long id)` |
| PUT | `/admin/order/confirm` | ADMIN | OrdersController.java / `Result confirm( @Valid @RequestBody OrdersConfirmDTO dto)` |
| PUT | `/admin/order/delivery/{id}` | ADMIN | OrdersController.java / `Result delivery( @PathVariable Long id)` |
| PUT | `/admin/order/complete/{id}` | ADMIN | OrdersController.java / `Result complete( @PathVariable Long id)` |
| PUT | `/admin/order/rejection` | ADMIN | OrdersController.java / `Result rejection( @Valid @RequestBody OrdersRejectionDTO dto)` |
| GET | `/admin/order/statistics` | ADMIN | OrdersController.java / `Result<OrderStatisticsVO> statistics()` |
| GET | `/admin/report/turnoverStatistics` | ADMIN | ReportController.java / `Result<TurnoverReportVO> turnoverStatistics( @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end)` |
| GET | `/admin/report/ordersStatistics` | ADMIN | ReportController.java / `Result<OrderReportVO> ordersStatistics( @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end)` |
| GET | `/admin/report/top10` | ADMIN | ReportController.java / `Result<SalesTop10ReportVO> top10( @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate begin, @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate end)` |
| GET | `/admin/report/user` | ADMIN | ReportController.java / `Result<UserReportVO> user( @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end)` |
| POST | `/admin/setmeal` | ADMIN | SetmealController.java / `Result save( @RequestBody SetmealDTO dto)` |
| GET | `/admin/setmeal/page` | ADMIN | SetmealController.java / `Result<PageResult<Setmeal>> page( SetmealPageQueryDTO dto)` |
| GET | `/admin/setmeal/{id}` | ADMIN | SetmealController.java / `Result<SetmealDTO> getById( @PathVariable Long id)` |
| PUT | `/admin/setmeal` | ADMIN | SetmealController.java / `Result update( @RequestBody SetmealDTO dto)` |
| POST | `/admin/setmeal/status/{status}` | ADMIN | SetmealController.java / `Result updateStatus( @PathVariable Integer status, @RequestParam Long id)` |
| DELETE | `/admin/setmeal` | ADMIN | SetmealController.java / `Result delete( @RequestParam Long id)` |
| PUT | `/admin/shop/status/{status}` | ADMIN | ShopController.java / `Result setStatus( @PathVariable Integer status)` |
| GET | `/admin/shop/status` | ADMIN | ShopController.java / `Result<Integer> getAdminStatus()` |
| GET | `/user/shop/status` | USER | ShopController.java / `Result<Integer> getUserStatus()` |
| POST | `/user/shoppingCart/add` | USER | ShoppingCartController.java / `Result add( @RequestBody ShoppingCartDTO dto)` |
| GET | `/user/shoppingCart/list` | USER | ShoppingCartController.java / `Result<List<ShoppingCart>> list()` |
| POST | `/user/shoppingCart/sub` | USER | ShoppingCartController.java / `Result sub( @RequestBody ShoppingCartDTO dto)` |
| DELETE | `/user/shoppingCart/clean` | USER | ShoppingCartController.java / `Result clean()` |
| GET | `/user/category/list` | USER | UserCategoryController.java / `Result<List<Category>> list(@RequestParam Integer type)` |
| POST | `/user/user/login` | 无需 Token | UserController.java / `Result<String> login( @Valid @RequestBody UserLoginDTO dto)` |
| GET | `/user/dish/list` | USER | UserDishController.java / `Result<List<DishDTO>> list( @RequestParam Long categoryId)` |
| POST | `/user/order/submit` | USER | UserOrderController.java / `Result<Void> submit( @Valid @RequestBody OrdersSubmitDTO dto)` |
| GET | `/user/order/historyOrders` | USER | UserOrderController.java / `Result<PageResult<Orders>> historyOrders( @Valid UserOrderPageQueryDTO dto)` |
| GET | `/user/order/orderDetail/{id}` | USER | UserOrderController.java / `Result<OrdersDetailDTO> orderDetail( @PathVariable Long id)` |
| PUT | `/user/order/cancel/{id}` | USER | UserOrderController.java / `Result<Void> cancel( @PathVariable Long id)` |
| POST | `/user/order/repetition/{id}` | USER | UserOrderController.java / `Result<Void> repetition( @PathVariable Long id)` |
| POST | `/user/order/mock-pay/{id}` | USER | UserOrderController.java / `Result<Void> mockPay(@PathVariable Long id)` |
| GET | `/user/setmeal/list` | USER | UserSetmealController.java / `Result<List<Setmeal>> list( @RequestParam Long categoryId)` |
| GET | `/admin/workspace/businessData` | ADMIN | WorkspaceController.java / `Result<BusinessDataVO> businessData()` |

## DTO 字段参考

这里只列 Controller 方法签名直接引用的 DTO；字段注解是否执行还取决于方法是否声明 @Valid。详细校验消息见源码。

### CategoryPageQueryDTO

| 字段 | Java 类型 | 默认值 |
|---|---|---|
| page | `Integer` | 1 |
| pageSize | `Integer` | 10 |
| name | `String` | 未指定 |
| type | `Integer` | 未指定 |

### CategorySaveDTO

| 字段 | Java 类型 | 默认值 |
|---|---|---|
| name | `String` | 未指定 |
| type | `Integer` | 未指定 |
| sort | `Integer` | 未指定 |

### CategoryUpdateDTO

| 字段 | Java 类型 | 默认值 |
|---|---|---|
| id | `Long` | 未指定 |
| name | `String` | 未指定 |
| type | `Integer` | 未指定 |
| sort | `Integer` | 未指定 |

### DishDTO

| 字段 | Java 类型 | 默认值 |
|---|---|---|
| id | `Long` | 未指定 |
| name | `String` | 未指定 |
| categoryId | `Long` | 未指定 |
| price | `BigDecimal` | 未指定 |
| image | `String` | 未指定 |
| description | `String` | 未指定 |
| status | `Integer` | 未指定 |
| flavors | `List<DishFlavor>` | 未指定 |

### DishPageQueryDTO

| 字段 | Java 类型 | 默认值 |
|---|---|---|
| page | `Integer` | 1 |
| pageSize | `Integer` | 10 |
| name | `String` | 未指定 |
| categoryId | `Long` | 未指定 |
| status | `Integer` | 未指定 |

### EmployeeLoginDTO

| 字段 | Java 类型 | 默认值 |
|---|---|---|
| username | `String` | 未指定 |
| password | `String` | 未指定 |

### EmployeePageQueryDTO

| 字段 | Java 类型 | 默认值 |
|---|---|---|
| page | `Integer` | 1 |
| pageSize | `Integer` | 10 |
| name | `String` | 未指定 |

### EmployeeSaveDTO

| 字段 | Java 类型 | 默认值 |
|---|---|---|
| username | `String` | 未指定 |
| name | `String` | 未指定 |
| phone | `String` | 未指定 |
| sex | `String` | 未指定 |
| idNumber | `String` | 未指定 |

### EmployeeUpdateDTO

| 字段 | Java 类型 | 默认值 |
|---|---|---|
| id | `Long` | 未指定 |
| username | `String` | 未指定 |
| name | `String` | 未指定 |
| phone | `String` | 未指定 |
| sex | `String` | 未指定 |
| idNumber | `String` | 未指定 |

### OrdersConfirmDTO

| 字段 | Java 类型 | 默认值 |
|---|---|---|
| id | `Long` | 未指定 |

### OrdersDetailDTO

| 字段 | Java 类型 | 默认值 |
|---|---|---|
| id | `Long` | 未指定 |
| number | `String` | 未指定 |
| status | `Integer` | 未指定 |
| userId | `Long` | 未指定 |
| addressBookId | `Long` | 未指定 |
| orderTime | `LocalDateTime` | 未指定 |
| checkoutTime | `LocalDateTime` | 未指定 |
| payMethod | `Integer` | 未指定 |
| payStatus | `Integer` | 未指定 |
| amount | `BigDecimal` | 未指定 |
| remark | `String` | 未指定 |
| phone | `String` | 未指定 |
| address | `String` | 未指定 |
| userName | `String` | 未指定 |
| consignee | `String` | 未指定 |
| cancelReason | `String` | 未指定 |
| rejectionReason | `String` | 未指定 |
| cancelTime | `LocalDateTime` | 未指定 |
| estimatedDeliveryTime | `LocalDateTime` | 未指定 |
| deliveryTime | `LocalDateTime` | 未指定 |
| orderDetails | `List<OrderDetail>` | 未指定 |

### OrdersPageQueryDTO

| 字段 | Java 类型 | 默认值 |
|---|---|---|
| page | `Integer` | 1 |
| pageSize | `Integer` | 10 |
| number | `String` | 未指定 |
| phone | `String` | 未指定 |
| status | `Integer` | 未指定 |

### OrdersRejectionDTO

| 字段 | Java 类型 | 默认值 |
|---|---|---|
| id | `Long` | 未指定 |
| rejectionReason | `String` | 未指定 |

### OrdersSubmitDTO

| 字段 | Java 类型 | 默认值 |
|---|---|---|
| addressBookId | `Long` | 未指定 |
| payMethod | `Integer` | 未指定 |
| remark | `String` | 未指定 |

### SetmealDTO

| 字段 | Java 类型 | 默认值 |
|---|---|---|
| id | `Long` | 未指定 |
| categoryId | `Long` | 未指定 |
| name | `String` | 未指定 |
| price | `BigDecimal` | 未指定 |
| status | `Integer` | 未指定 |
| description | `String` | 未指定 |
| image | `String` | 未指定 |
| setmealDishes | `List<SetmealDish>` | 未指定 |

### SetmealPageQueryDTO

| 字段 | Java 类型 | 默认值 |
|---|---|---|
| page | `Integer` | 1 |
| pageSize | `Integer` | 10 |
| name | `String` | 未指定 |
| categoryId | `Long` | 未指定 |
| status | `Integer` | 未指定 |

### ShoppingCartDTO

| 字段 | Java 类型 | 默认值 |
|---|---|---|
| dishId | `Long` | 未指定 |
| setmealId | `Long` | 未指定 |
| dishFlavor | `String` | 未指定 |
| number | `Integer` | 未指定 |
| name | `String` | 未指定 |
| image | `String` | 未指定 |
| amount | `BigDecimal` | 未指定 |

### UserLoginDTO

| 字段 | Java 类型 | 默认值 |
|---|---|---|
| code | `String` | 未指定 |

### UserOrderPageQueryDTO

| 字段 | Java 类型 | 默认值 |
|---|---|---|
| page | `Integer` | 1 |
| pageSize | `Integer` | 10 |
| status | `Integer` | 未指定 |

## 核心请求示例

以下为示例输入，ID、Token 和账号均需替换为本地真实数据。示例响应不代表已执行请求。

管理员登录：

```http
POST /admin/employee/login
Content-Type: application/json

{"username":"admin","password":"你自行初始化的密码"}
```

登录结果中 `data.token` 为管理员 Token。用户模拟登录为 `POST /user/user/login`，请求体 `{"code":"local-demo-user"}`，返回的 `data` 是用户 Token 字符串。相同 code 直接代表同一个用户，无真实微信校验。

查询菜品：`GET /user/dish/list?categoryId=1`，请求头 `token: <用户Token>`。categoryId 必须为正数；合法但不存在的分类返回空列表并可能缓存。

修改菜品：

```http
PUT /admin/dish
Content-Type: application/json
token: <管理员Token>

{"id":1,"name":"示例菜品","categoryId":1,"price":18.00,"status":1,"flavors":[]}
```

修改时 flavors 会替换旧口味，空列表会删除已有口味。菜品删除是 `DELETE /admin/dish?id=1`；状态切换是 `POST /admin/dish/status/1?id=1`，不是把 ID 放进 JSON。套餐删除同样使用查询参数，员工删除使用路径 ID。

加购：`POST /user/shoppingCart/add`，菜品可传 `{"dishId":1,"dishFlavor":"不辣","number":1}`；套餐传 setmealId。客户端商品名称/金额不应作为可信结算依据。

地址新增：`POST /user/addressBook`，请求体含 consignee、phone、provinceName、cityName、districtName、detail；服务端覆盖 userId 为当前登录用户。新增后 `data` 返回地址 ID。

提交订单：`POST /user/order/submit`，请求体 `{"addressBookId":1,"payMethod":1,"remark":"本地演示"}`。当前接口成功 `data` 为空，需从订单历史查询刚创建的订单 ID。店铺应已营业、地址归属当前用户、购物车非空。

模拟支付：仅设置 `MOCK_PAYMENT_ENABLED=true` 的本地环境使用 `POST /user/order/mock-pay/{id}`。管理端后续使用 confirm（请求体 `{"id":1}`）、delivery/{id}、complete/{id}；拒单使用 `{"id":1,"rejectionReason":"原因"}`。

## 订单与支付状态

| 字段 | 值 | 含义 |
|---|---|---|
| status | 1 / 2 / 3 / 4 / 5 / 6 | 待支付 / 待接单 / 已接单 / 派送中 / 已完成 / 已取消 |
| payStatus | 0 / 1 / 2 / 3 | 未支付 / 已支付 / 已退款 / 待退款 |

拒绝已支付订单和用户取消已支付订单会进入待退款；没有退款执行接口，不能显示为退款成功。

## 鉴权与运行期文档

未登录、失效 Token、角色不符的消息分别包含 NOT_LOGIN、TOKEN_INVALID、NO_PERMISSION。用户 ID 从拦截器上下文获取，并在请求完成后清理。管理员停用/删除后，现有 Token 尚无撤销机制。

后端如能正常启动，可检查 `/v3/api-docs`、`/swagger-ui/index.html` 或 `/doc.html`。当前文档组件组合运行兼容性待验证；若启动或文档入口报错，保留堆栈后单独处理依赖兼容性，不能据此宣称自动文档已可用。

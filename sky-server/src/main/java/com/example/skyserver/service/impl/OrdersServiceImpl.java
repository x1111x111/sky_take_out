package com.example.skyserver.service.impl;

import com.example.skyserver.context.UserContext;
import com.example.skyserver.dto.*;
import com.example.skyserver.entity.AddressBook;
import com.example.skyserver.entity.OrderDetail;
import com.example.skyserver.entity.Orders;
import com.example.skyserver.entity.ShoppingCart;
import com.example.skyserver.mapper.AddressBookMapper;
import com.example.skyserver.mapper.OrderDetailMapper;
import com.example.skyserver.mapper.OrdersMapper;
import com.example.skyserver.mapper.ShoppingCartMapper;
import com.example.skyserver.result.PageResult;
import com.example.skyserver.service.OrdersService;
import com.example.skyserver.vo.OrderReportVO;
import com.example.skyserver.vo.OrderStatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.example.skyserver.service.ShopService;
import com.example.skyserver.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class OrdersServiceImpl implements OrdersService {

    private final OrderDetailMapper orderDetailMapper;
    private final OrdersMapper ordersMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final AddressBookMapper addressBookMapper;
    private final ShopService shopService;
    @Override
    public PageResult<Orders> pageQuery(
            OrdersPageQueryDTO dto) {

        Integer offset =
                (dto.getPage() - 1)
                        * dto.getPageSize();

        Long total =
                ordersMapper.count(
                        dto.getNumber(),
                        dto.getPhone(),
                        dto.getStatus()
                );

        List<Orders> records =
                ordersMapper.pageQuery(
                        dto.getNumber(),
                        dto.getPhone(),
                        dto.getStatus(),
                        offset,
                        dto.getPageSize()
                );

        return new PageResult<>(
                total,
                records
        );
    }
    @Override
    public OrdersDetailDTO getById(Long id) {

        // 1. 查询订单基本信息
        Orders orders = ordersMapper.getById(id);

        if (orders == null) {
            throw new BusinessException("订单不存在");
        }

        // 2. 查询订单明细
        List<OrderDetail> orderDetails =
                orderDetailMapper.getByOrderId(id);

        // 3. 组合结果
        OrdersDetailDTO dto = new OrdersDetailDTO();

        BeanUtils.copyProperties(orders, dto);

        dto.setOrderDetails(orderDetails);

        return dto;
    }
    @Override
    public void confirm(Long id) {

        // 1. 查询订单
        Orders orders = ordersMapper.getById(id);

        if (orders == null) {
            throw new BusinessException("订单不存在");
        }

        // 2. 只有待接单状态才能接单
        if (orders.getStatus() == null
                || orders.getStatus() != 2) {

            throw new BusinessException(
                    "当前订单状态不能接单"
            );
        }

        // 3. 修改为已接单
        if (ordersMapper.updateStatus(id, 2, 3) != 1) {
            throw new BusinessException("接单失败，订单状态可能已经发生变化");
        }
    }
    @Override
    public void delivery(Long id) {


        // 1. 查询订单
        Orders orders =
                ordersMapper.getById(id);


        if (orders == null) {

            throw new BusinessException(
                    "订单不存在"
            );

        }


        // 2. 判断订单状态
        if (orders.getStatus() != 3) {

            throw new BusinessException(
                    "当前订单状态不能派送"
            );

        }


        // 3. 修改状态为派送中
        if (ordersMapper.updateStatus(id, 3, 4) != 1) {
            throw new BusinessException("派送失败，订单状态可能已经发生变化");
        }

    }
    @Override
    public void complete(Long id) {


        // 1. 查询订单
        Orders orders =
                ordersMapper.getById(id);


        if (orders == null) {

            throw new BusinessException(
                    "订单不存在"
            );

        }


        // 2. 只能派送中的订单完成
        if (orders.getStatus() != 4) {

            throw new BusinessException(
                    "当前订单状态不能完成"
            );

        }


        // 3. 修改状态
        if (ordersMapper.updateStatus(id, 4, 5) != 1) {
            throw new BusinessException("完成订单失败，订单状态可能已经发生变化");
        }

    }
    @Override
    public void rejection(OrdersRejectionDTO dto) {

        // 1. 查询订单
        Orders orders =
                ordersMapper.getById(dto.getId());

        if (orders == null) {
            throw new BusinessException("订单不存在");
        }

        // 2. 只有待接单订单才能拒单
        if (orders.getStatus() == null
                || orders.getStatus() != 2) {

            throw new BusinessException(
                    "当前订单状态不能拒单"
            );
        }

        // 3. 拒单原因不能为空
        if (dto.getRejectionReason() == null
                || dto.getRejectionReason().trim().isEmpty()) {

            throw new BusinessException(
                    "拒单原因不能为空"
            );
        }

        // 4. 修改为已取消
        int affectedRows = ordersMapper.rejection(
                dto.getId(),
                dto.getRejectionReason()
        );
        if (affectedRows != 1) {
            throw new BusinessException("拒单失败，订单状态可能已经发生变化");
        }
    }
    @Override
    public OrderStatisticsVO statistics() {

        // 待接单
        Integer toBeConfirmed =
                ordersMapper.countByStatus(2);

        // 已接单、等待派送
        Integer confirmed =
                ordersMapper.countByStatus(3);

        // 派送中
        Integer deliveryInProgress =
                ordersMapper.countByStatus(4);


        OrderStatisticsVO vo =
                new OrderStatisticsVO();

        vo.setToBeConfirmed(toBeConfirmed);

        vo.setConfirmed(confirmed);

        vo.setDeliveryInProgress(
                deliveryInProgress
        );

        return vo;
    }
    @Override
    @Transactional
    public void submit(OrdersSubmitDTO dto) {

        Integer shopStatus=shopService.getStatus();
        if(!Integer.valueOf(1).equals(shopStatus)){
            throw new BusinessException("店铺已打烊，暂不接单");
        }
        // 1. 获取当前登录用户
        Long userId = UserContext.getUserId();


        // 2. 查询当前用户购物车
        List<ShoppingCart> cartList =
                shoppingCartMapper.listForUpdate(userId);

        if (cartList == null || cartList.isEmpty()) {
            throw new BusinessException("购物车为空，无法下单");
        }


        // 3. 查询收货地址
        AddressBook addressBook =
                addressBookMapper.getById(
                        dto.getAddressBookId()
                );


        if (addressBook == null) {
            throw new BusinessException("收货地址不存在");
        }

        // 防止使用其他用户地址
        if (!userId.equals(addressBook.getUserId())) {
            throw new BusinessException("无权使用该收货地址");
        }


        // 4. 后端计算订单总金额
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (ShoppingCart cart : cartList) {

            BigDecimal itemTotal =
                    cart.getAmount()
                            .multiply(
                                    BigDecimal.valueOf(
                                            cart.getNumber()
                                    )
                            );

            totalAmount =
                    totalAmount.add(itemTotal);
        }


        // 5. 创建订单
        Orders orders = new Orders();

        orders.setNumber(UUID.randomUUID().toString().replace("-", ""));

        // 待付款
        orders.setStatus(1);

        orders.setUserId(userId);

        orders.setAddressBookId(
                dto.getAddressBookId()
        );

        orders.setOrderTime(
                LocalDateTime.now()
        );

        orders.setPayMethod(
                dto.getPayMethod()
        );

        // 未支付
        orders.setPayStatus(0);

        orders.setAmount(totalAmount);

        orders.setRemark(
                dto.getRemark()
        );

        orders.setPhone(
                addressBook.getPhone()
        );

        orders.setConsignee(
                addressBook.getConsignee()
        );

        orders.setAddress(
                addressBook.getProvinceName()
                        + addressBook.getCityName()
                        + addressBook.getDistrictName()
                        + addressBook.getDetail()
        );


        // 6. 保存订单
        ordersMapper.insert(orders);


        // 7. 把购物车复制成订单明细
        for (ShoppingCart cart : cartList) {

            OrderDetail detail =
                    new OrderDetail();

            detail.setOrderId(
                    orders.getId()
            );

            detail.setName(
                    cart.getName()
            );

            detail.setImage(
                    cart.getImage()
            );

            detail.setDishId(
                    cart.getDishId()
            );

            detail.setSetmealId(
                    cart.getSetmealId()
            );

            detail.setDishFlavor(
                    cart.getDishFlavor()
            );

            detail.setNumber(
                    cart.getNumber()
            );

            detail.setAmount(
                    cart.getAmount()
            );

            orderDetailMapper.insert(detail);
        }


        // 8. 清空当前用户购物车
        List<Long> cartIds = cartList.stream().map(ShoppingCart::getId).toList();
        if (shoppingCartMapper.deleteSelected(userId, cartIds) != cartIds.size()) {
            throw new BusinessException("购物车已发生变化，请重新提交");
        }
    }
    @Override
    public PageResult<Orders> historyOrders(
            UserOrderPageQueryDTO dto) {

        Long userId =
                UserContext.getUserId();

        Integer offset =
                (dto.getPage() - 1)
                        * dto.getPageSize();

        Long total =
                ordersMapper.countByUser(
                        userId,
                        dto.getStatus()
                );

        List<Orders> records =
                ordersMapper.pageQueryByUser(
                        userId,
                        dto.getStatus(),
                        offset,
                        dto.getPageSize()
                );

        return new PageResult<>(
                total,
                records
        );
    }
    @Override
    public OrdersDetailDTO userOrderDetail(Long id) {

        Long userId =
                UserContext.getUserId();

        Orders orders =
                ordersMapper.getById(id);

        if (orders == null) {
            throw new BusinessException("订单不存在");
        }

        // 非本人订单禁止查看
        if (!userId.equals(orders.getUserId())) {
            throw new BusinessException("无权查看该订单");
        }

        List<OrderDetail> details =
                orderDetailMapper.getByOrderId(id);

        OrdersDetailDTO dto =
                new OrdersDetailDTO();

        BeanUtils.copyProperties(
                orders,
                dto
        );

        dto.setOrderDetails(details);

        return dto;
    }@Override
    public void userCancel(Long id) {

        Long userId = UserContext.getUserId();

        // 查询订单
        Orders orders = ordersMapper.getById(id);

        if (orders == null) {
            throw new BusinessException("订单不存在");
        }

        // 必须是自己的订单
        if (!userId.equals(orders.getUserId())) {
            throw new BusinessException("无权取消该订单");
        }

        Integer status = orders.getStatus();

        // 当前先允许待付款、待接单取消
        if (status == null || (status != 1 && status != 2)) {
            throw new BusinessException("当前订单状态不能取消");
        }

        int affectedRows = ordersMapper.cancel(
                id,
                userId,
                "用户取消订单"
        );
        if (affectedRows != 1) {
            throw new BusinessException("取消失败，订单状态可能已经发生变化");
        }
    }
    @Override
    @Transactional
    public void repetition(Long id) {

        Long userId = UserContext.getUserId();

        // 1. 查询原订单
        Orders orders = ordersMapper.getById(id);

        if (orders == null) {
            throw new BusinessException("订单不存在");
        }

        // 2. 只能操作自己的订单
        if (!userId.equals(orders.getUserId())) {
            throw new BusinessException("无权操作该订单");
        }

        // 3. 查询原订单明细
        List<OrderDetail> details =
                orderDetailMapper.getByOrderId(id);

        if (details == null || details.isEmpty()) {
            throw new BusinessException("订单中没有商品");
        }

        // 4. 将历史订单商品重新加入购物车
        for (OrderDetail detail : details) {

            ShoppingCart cart = new ShoppingCart();

            cart.setUserId(userId);

            cart.setName(detail.getName());

            cart.setImage(detail.getImage());

            cart.setDishId(detail.getDishId());

            cart.setSetmealId(detail.getSetmealId());

            cart.setDishFlavor(
                    detail.getDishFlavor()
            );

            cart.setNumber(
                    detail.getNumber()
            );

            cart.setAmount(
                    detail.getAmount()
            );


            // 查询购物车是否已经存在相同商品
            ShoppingCart old =
                    shoppingCartMapper
                            .getByUserIdAndDish(cart);


            if (old != null) {

                // 已存在 → 数量累加
                old.setNumber(
                        old.getNumber()
                                + detail.getNumber()
                );

                shoppingCartMapper
                        .updateNumber(old);

            } else {

                // 不存在 → 新增
                shoppingCartMapper.insert(cart);
            }
        }
    }
    @Override
    @Transactional
    public void mockPay(Long orderId) {

        if (orderId == null || orderId <= 0) {
            throw new BusinessException("订单ID不合法");
        }

        // 1. 获取当前登录用户
        Long userId = UserContext.getUserId();

        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        // 2. 查询订单
        Orders orders = ordersMapper.getById(orderId);

        if (orders == null) {
            throw new BusinessException("订单不存在");
        }

        // 3. 不允许操作其他用户订单
        if (!userId.equals(orders.getUserId())) {
            throw new BusinessException("无权操作该订单");
        }

        // 4. 只允许未支付且待付款的订单
        if (!Integer.valueOf(1).equals(orders.getStatus())
                || !Integer.valueOf(0).equals(orders.getPayStatus())) {
            throw new BusinessException("当前订单状态不能模拟支付");
        }

        // 5. 原子更新订单状态和支付状态
        int affectedRows =
                ordersMapper.mockPaySuccess(orderId, userId);

        if (affectedRows != 1) {
            throw new BusinessException(
                    "模拟支付失败，订单状态可能已经发生变化"
            );
        }
    }
}

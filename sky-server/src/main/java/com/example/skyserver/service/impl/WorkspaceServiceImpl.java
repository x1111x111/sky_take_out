package com.example.skyserver.service.impl;

import com.example.skyserver.mapper.WorkspaceMapper;
import com.example.skyserver.service.WorkspaceService;
import com.example.skyserver.vo.BusinessDataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl
        implements WorkspaceService {


    private final WorkspaceMapper workspaceMapper;



    @Override
    public BusinessDataVO businessData(){


        BusinessDataVO vo =
                new BusinessDataVO();



        vo.setTurnover(
                workspaceMapper.todayTurnover()
        );


        vo.setOrderCount(
                workspaceMapper.todayOrderCount()
        );


        vo.setValidOrderCount(
                workspaceMapper.validOrderCount()
        );


        vo.setNewUserCount(
                workspaceMapper.todayUserCount()
        );


        vo.setWaitingOrders(
                workspaceMapper.waitingOrders()
        );


        vo.setDeliveryOrders(
                workspaceMapper.deliveryOrders()
        );


        return vo;

    }
}
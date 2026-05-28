package com.qiyun.restaurant.service;

import com.qiyun.restaurant.entity.DiningTable;

import java.util.List;

/**
 * 桌台Service
 */
public interface DiningTableService {
    
    /**
     * 获取所有桌台
     */
    List<DiningTable> getAllTables();
    
    /**
     * 根据ID获取桌台详情
     */
    DiningTable getTableById(Long id);
    
    /**
     * 添加桌台
     */
    void addTable(DiningTable table);
    
    /**
     * 更新桌台
     */
    void updateTable(DiningTable table);
    
    /**
     * 删除桌台
     */
    void deleteTable(Long id);
    
    /**
     * 更新桌台状态
     */
    void updateTableStatus(Long id, String status);
}

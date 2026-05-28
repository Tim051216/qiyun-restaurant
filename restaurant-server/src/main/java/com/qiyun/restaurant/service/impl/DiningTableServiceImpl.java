package com.qiyun.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiyun.restaurant.entity.DiningTable;
import com.qiyun.restaurant.mapper.DiningTableMapper;
import com.qiyun.restaurant.service.DiningTableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 桌台Service实现
 */
@Service
@Slf4j
public class DiningTableServiceImpl implements DiningTableService {
    
    @Autowired
    private DiningTableMapper diningTableMapper;
    
    @Override
    public List<DiningTable> getAllTables() {
        LambdaQueryWrapper<DiningTable> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(DiningTable::getTableNo);
        return diningTableMapper.selectList(wrapper);
    }
    
    @Override
    public DiningTable getTableById(Long id) {
        DiningTable table = diningTableMapper.selectById(id);
        if (table == null) {
            throw new RuntimeException("桌台不存在");
        }
        return table;
    }
    
    @Override
    public void addTable(DiningTable table) {
        // 检查桌号是否已存在
        LambdaQueryWrapper<DiningTable> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiningTable::getTableNo, table.getTableNo());
        Long count = diningTableMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("桌号已存在");
        }
        
        // 默认状态为空闲
        if (table.getStatus() == null) {
            table.setStatus("available");
        }
        
        diningTableMapper.insert(table);
    }
    
    @Override
    public void updateTable(DiningTable table) {
        diningTableMapper.updateById(table);
    }
    
    @Override
    public void deleteTable(Long id) {
        diningTableMapper.deleteById(id);
    }
    
    @Override
    public void updateTableStatus(Long id, String status) {
        DiningTable table = diningTableMapper.selectById(id);
        if (table == null) {
            throw new RuntimeException("桌台不存在");
        }
        table.setStatus(status);
        diningTableMapper.updateById(table);
    }
}

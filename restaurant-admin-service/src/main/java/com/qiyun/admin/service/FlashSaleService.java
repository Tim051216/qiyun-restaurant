package com.qiyun.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiyun.admin.entity.FlashSale;
import com.qiyun.admin.mapper.FlashSaleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlashSaleService {
    
    private final FlashSaleMapper flashSaleMapper;
    
    /**
     * 获取秒杀列表
     */
    public List<FlashSale> list() {
        LambdaQueryWrapper<FlashSale> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlashSale::getDeleted, 0)
               .orderByAsc(FlashSale::getStartTime);
        return flashSaleMapper.selectList(wrapper);
    }
    
    /**
     * 分页查询秒杀
     */
    public Page<FlashSale> page(int page, int pageSize, String sessionTime, String status) {
        Page<FlashSale> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<FlashSale> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlashSale::getDeleted, 0);
        
        if (sessionTime != null && !sessionTime.isEmpty()) {
            wrapper.eq(FlashSale::getSessionTime, sessionTime);
        }
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(FlashSale::getStatus, status);
        }
        
        wrapper.orderByAsc(FlashSale::getStartTime);
        return flashSaleMapper.selectPage(pageObj, wrapper);
    }
    
    /**
     * 根据场次时间查询秒杀商品
     */
    public List<FlashSale> listBySession(String sessionTime) {
        LambdaQueryWrapper<FlashSale> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlashSale::getDeleted, 0)
               .eq(FlashSale::getSessionTime, sessionTime)
               .orderByDesc(FlashSale::getHot)
               .orderByAsc(FlashSale::getId);
        return flashSaleMapper.selectList(wrapper);
    }
    
    /**
     * 获取秒杀详情
     */
    public FlashSale getById(Long id) {
        return flashSaleMapper.selectById(id);
    }
    
    /**
     * 添加秒杀
     */
    public boolean save(FlashSale flashSale) {
        return flashSaleMapper.insert(flashSale) > 0;
    }
    
    /**
     * 更新秒杀
     */
    public boolean update(FlashSale flashSale) {
        return flashSaleMapper.updateById(flashSale) > 0;
    }
    
    /**
     * 删除秒杀（逻辑删除）
     */
    public boolean delete(Long id) {
        FlashSale flashSale = new FlashSale();
        flashSale.setId(id);
        flashSale.setDeleted(1);
        return flashSaleMapper.updateById(flashSale) > 0;
    }
}

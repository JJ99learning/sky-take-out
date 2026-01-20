package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.BaseException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {


    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;



    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        // Check if the setmeal name is not exist
        List<Setmeal> setmeal  = setmealMapper.getByName(setmealDTO.getName());
        if (setmeal != null && setmeal.size() > 0) {
            String msg = java.text.MessageFormat.format("Setmeal name {0} already existed", setmealDTO.getName());
            throw new BaseException(msg);
        }

        Setmeal setmeal1 = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal1);
        setmealMapper.insert(setmeal1);

        Long setmealId = setmeal1.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });

        setmealDishMapper.insertBatch(setmealDishes);


    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        int page = setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize();

        PageHelper.startPage(page, pageSize);
        Page<SetmealVO> pageResult = setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(pageResult.getTotal(), pageResult.getResult());
    }

    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.getByCategoryId(setmeal.getCategoryId());
        return list;
    }
}

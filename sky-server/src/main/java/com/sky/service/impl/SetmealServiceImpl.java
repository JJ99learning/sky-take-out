package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
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
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.ArrayList;
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

    @Override
    public SetmealVO getById(Long id) {

        // Get the setmeal by Id
        Setmeal setmeal = setmealMapper.getById(id);

        SetmealVO setmealvo = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealvo);

        // Query the setmeal_dish for getting the dishes under this setmeal
        List<SetmealDish> setmealDishes = setmealDishMapper.getSetmealDishBySetmealId(id);
        setmealvo.setSetmealDishes(setmealDishes);

        return setmealvo;


    }

    @Override
    public void update(SetmealDTO setmealDTO) {

        //Check if this setmeal is exist
        Setmeal setmeal = setmealMapper.getById(setmealDTO.getId());

        if (setmeal == null) {
            throw new BaseException("套餐不存在");
        }

        BeanUtils.copyProperties(setmealDTO, setmeal);


        setmealMapper.update(setmeal);
    }

    @Override
    @Transactional
    public List<Long> delete(List<Long> ids) {

        List<Long> unableDeleteList = new ArrayList<>();

        ids.forEach(id -> {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
                unableDeleteList.add(id);
            }
        });

        if (unableDeleteList.size() > 0) {
            ids.removeAll(unableDeleteList);
        }

        // Remove the id that can be deleted
        ids.forEach(id -> {
            setmealMapper.delete(id);
            setmealDishMapper.delete(id);
        });

        return unableDeleteList;
    }


}

package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish  = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);

        Long id = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()){
            for (DishFlavor flavor : flavors){
                flavor.setDishId(dish.getId());
            }
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 菜品删除 / 菜品批量删除
     * @param ids
     * @return
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
       //判断是否存在起售中的菜品
       List<Dish> dishes = dishMapper.selectBatchId(ids);
       for (Dish dish : dishes){
           if (dish.getStatus() == StatusConstant.ENABLE) {
               throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
           }
       }

       //判断是否和套餐关联
        List<Long> setmealIds = setmealDishMapper.getSetmealDishIdsByDishIds(ids);
       if (setmealIds != null && !setmealIds.isEmpty()){
           throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
       }

       //删除菜品
        dishMapper.deleteById(ids);

       //删除菜品关联的口味
        dishFlavorMapper.deleteByDishId(ids);

    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        List<Long> ids = Collections.singletonList(id);
        List<Dish> dish = dishMapper.selectBatchId(ids);
        List<DishFlavor> dishFlavors = dishMapper.getByDishId(ids.get(0));

        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish.get(0),dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    /**
     * 根据id修改菜品信息和对应的口味信息
     * @param dishDTO
     */

    @Override
    public void updateWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);

        //更新菜品对应的口味，先删除后插入新数据
        List<Long> ids = Collections.singletonList(dish.getId());
        dishFlavorMapper.deleteByDishId(ids);

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()){
            for (DishFlavor flavor : flavors){
                flavor.setDishId(dish.getId());
            }
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        List<DishVO> list = dishMapper.getByCategoryId(dish.getCategoryId());
        return list;
    }


}

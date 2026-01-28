package com.sky.controller.admin;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminSetmealController")
@RequestMapping("/admin/setmeal")
@Slf4j
@Api("套餐相关接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

//    @Autowired
//    private SetmealDish setmealDish;


    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id) {

        log.info("Get setmeal by id:{}", id);
        SetmealVO setmealVO = setmealService.getById(id);

        return Result.success(setmealVO);
    }

    @PostMapping
    @ApiOperation("新增套餐")
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        log.info("Save setmeal {}", setmealDTO);
        setmealService.saveWithDish(setmealDTO);
        return Result.success();

    }

    @PostMapping("status/{status}")
    @ApiOperation("套餐起售，停售")
    public Result enableDisable(@PathVariable Integer status, @RequestParam Long id) {
        log.info("Update setmeal id, status:{},{}", status,id);

        SetmealDTO setmealDTO = new SetmealDTO();
        setmealDTO.setId(id);
        setmealDTO.setStatus(status);

        setmealService.update(setmealDTO);

        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("Page setmeal {}", setmealPageQueryDTO);

        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);

        return Result.success(pageResult);

    }

    @PutMapping
    @ApiOperation("修改套餐")
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        log.info("Update setmeal {}", setmealDTO);

        setmealService.update(setmealDTO);

        return Result.success();

    }

    @DeleteMapping
    @ApiOperation("批量删除套餐")
    public Result delete(@RequestParam List<Long> ids) {

        log.info("Delete setmeal {}", ids);

        List<Long> Ids = setmealService.delete(ids);

        if (Ids != null && Ids.size() > 0) {
            return Result.error("Unable to delete some setmeal: " + Ids);
        }

        return Result.success();
    }

}

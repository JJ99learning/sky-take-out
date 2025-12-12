package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.PasswordEditFailedException;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api("员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("员工登出")
    public Result<String> logout() {
        return Result.success();
    }


    /**
     * 新增员工
     * @param employeeDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增员工")
    public Result save(@RequestBody EmployeeDTO employeeDTO) {

        log.info("Saving Employee: {}", employeeDTO);
        employeeService.save(employeeDTO);

        return Result.success();
    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("员工分页查询")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {

        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);

        return Result.success(pageResult);
    }

    /**
     * 启用或禁用员工账号
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用或禁用员工账号")
    public Result enbaleOrDisable(@PathVariable Integer status, Long id) {

        employeeService.enableOrDisable(status, id);

        return Result.success();

    }

    /**
     * 根据员工id查找信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查找员工信息")
    public Result<Employee> getById(@PathVariable Long id) {

        Employee employee = employeeService.queryById(id);
        employee.setPassword(null);

        return Result.success(employee);
    }

    /**
     * 修改员工资料
     * @param employeeDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改员工资料")
    public Result updateInfo(@RequestBody EmployeeDTO employeeDTO) {

        employeeService.updateInfo(employeeDTO);

        return Result.success();
    }

    /**
     * 修改员工密码
     * @param passwordEditDTO
     * @return
     */
    @PutMapping("/editPassword")
    @ApiOperation("修改员工密码")
    public Result editPassword(@RequestBody PasswordEditDTO  passwordEditDTO) {


        // 和DB里面的记录对比一下旧密码
        String dbPassword = employeeService.queryById(BaseContext.getCurrentId()).getPassword();
        if (!employeeService.isPasswordMatched(passwordEditDTO.getOldPassword(), dbPassword)) {
            throw new PasswordEditFailedException("oldPassword 不正确");
        }

        // 修改密码
        Employee build = Employee.builder().id(BaseContext.getCurrentId()).password(passwordEditDTO.getNewPassword()).build();
        boolean status = employeeService.changePassword(build);

        if (!status) {
            throw new PasswordEditFailedException("DB更新失败");
        }

        return Result.success();


    }



}

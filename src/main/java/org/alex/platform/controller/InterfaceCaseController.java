package org.alex.platform.controller;

import org.alex.platform.common.Result;
import org.alex.platform.exception.BusinessException;
import org.alex.platform.exception.ParseException;
import org.alex.platform.exception.SqlException;
import org.alex.platform.pojo.InterfaceCaseDO;
import org.alex.platform.pojo.InterfaceCaseDTO;
import org.alex.platform.pojo.InterfaceCaseListDTO;
import org.alex.platform.service.InterfaceCaseExecuteLogService;
import org.alex.platform.service.InterfaceCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
public class InterfaceCaseController {
    @Autowired
    InterfaceCaseService interfaceCaseService;
    @Autowired
    InterfaceCaseExecuteLogService executeLogService;

    /**
     * 插入接口测试用例
     *
     * @param interfaceCaseDTO
     * @return
     * @throws BusinessException
     */
    @PostMapping("/interface/case/save")
    public Result saveInterfaceCase(@RequestBody @Validated InterfaceCaseDTO interfaceCaseDTO) throws BusinessException {
        Date date = new Date();
        interfaceCaseDTO.setCreatedTime(date);
        interfaceCaseDTO.setUpdateTime(date);
        interfaceCaseService.saveInterfaceCaseAndAssert(interfaceCaseDTO);
        return Result.success("新增成功");
    }

    /**
     * 修改接口测试用例
     *
     * @param interfaceCaseDTO
     * @return
     * @throws BusinessException
     */
    @PostMapping("/interface/case/modify")
    public Result modifyInterfaceCase(@RequestBody @Validated InterfaceCaseDTO interfaceCaseDTO) throws BusinessException {
        interfaceCaseDTO.setUpdateTime(new Date());
        interfaceCaseService.modifyInterfaceCase(interfaceCaseDTO);
        return Result.success("修改成功");
    }

    /**
     * 删除接口测试用例
     *
     * @param interfaceCaseId
     * @return
     */
    @GetMapping("/interface/case/remove/{interfaceCaseId}")
    public Result removeInterfaceCase(@PathVariable Integer interfaceCaseId) throws BusinessException {
        interfaceCaseService.removeInterfaceCase(interfaceCaseId);
        return Result.success("删除成功");
    }

    /**
     * 获取接口测试用例列表
     *
     * @param interfaceCaseListDTO
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/interface/case/list")
    public Result listInterfaceCase(InterfaceCaseListDTO interfaceCaseListDTO, Integer pageNum, Integer pageSize) {
        int num = pageNum == null ? 1 : pageNum;
        int size = pageSize == null ? 10 : pageSize;
        return Result.success(interfaceCaseService.findInterfaceCaseList(interfaceCaseListDTO, num, size));
    }

    /**
     * 查看接口测试用例详情
     *
     * @param caseId
     * @return
     */
    @GetMapping("/interface/case/info/{caseId}")
    public Result findInterfaceCaseByCaseId(@PathVariable Integer caseId) {
        return Result.success(interfaceCaseService.findInterfaceCaseByCaseId(caseId));
    }

    /**
     * 执行接口测试用例
     *
     * @param caseId
     * @return
     */
    @GetMapping("/interface/case/execute/{caseId}")
    public Result executeInterfaceCase(@PathVariable Integer caseId) throws ParseException, BusinessException, SqlException {
        Integer executeLog = interfaceCaseService.executeInterfaceCase(caseId);
        Byte status = executeLogService.findExecute(executeLog).getStatus();
        if (status == 0) {
            return Result.success("测试用例执行成功");
        } else if (status == 1) {
            return Result.fail("测试用例执行失败");
        } else {
            return Result.fail("测试用例执行错误");
        }
    }
}

package org.alex.platform.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.alex.platform.exception.BusinessException;
import org.alex.platform.exception.ParseException;
import org.alex.platform.exception.SqlException;
import org.alex.platform.mapper.InterfaceCaseExecuteLogMapper;
import org.alex.platform.mapper.InterfaceCaseSuiteMapper;
import org.alex.platform.mapper.InterfaceSuiteCaseRefMapper;
import org.alex.platform.pojo.*;
import org.alex.platform.service.InterfaceCaseService;
import org.alex.platform.service.InterfaceSuiteCaseRefService;
import org.alex.platform.service.InterfaceSuiteLogService;
import org.alex.platform.util.ExceptionUtil;
import org.alex.platform.util.NoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class InterfaceSuiteCaseRefServiceImpl implements InterfaceSuiteCaseRefService {
    @Autowired
    InterfaceSuiteCaseRefMapper interfaceSuiteCaseRefMapper;
    @Autowired
    InterfaceCaseService interfaceCaseService;
    @Autowired
    InterfaceCaseSuiteMapper interfaceCaseSuiteMapper;
    @Autowired
    InterfaceCaseExecuteLogMapper interfaceCaseExecuteLogMapper;
    @Autowired
    InterfaceSuiteLogService interfaceSuiteLogService;
    private static final Logger LOG = LoggerFactory.getLogger(InterfaceSuiteCaseRefServiceImpl.class);

    /**
     * 测试套件新增用例
     *
     * @param interfaceSuiteCaseRefDOList interfaceSuiteCaseRefDOList
     */
    @Override
    public void saveSuiteCase(List<InterfaceSuiteCaseRefDO> interfaceSuiteCaseRefDOList) {
        List<InterfaceSuiteCaseRefDO> list = new ArrayList<>();
        for (InterfaceSuiteCaseRefDO interfaceSuiteCaseRefDO :
                interfaceSuiteCaseRefDOList) {
            Integer caseId = interfaceSuiteCaseRefDO.getCaseId();
            Integer suiteId = interfaceSuiteCaseRefDO.getSuiteId();
            InterfaceSuiteCaseRefDTO interfaceSuiteCaseRefDTO = new InterfaceSuiteCaseRefDTO();
            interfaceSuiteCaseRefDTO.setCaseId(caseId);
            interfaceSuiteCaseRefDTO.setSuiteId(suiteId);
            List<InterfaceSuiteCaseRefVO> refVOList = interfaceSuiteCaseRefMapper.selectSuiteCaseList(interfaceSuiteCaseRefDTO);
            if (refVOList.isEmpty()) {
                list.add(interfaceSuiteCaseRefDO);
            }
        }
        if (!list.isEmpty()) {
            interfaceSuiteCaseRefMapper.insertSuiteCase(list);
        }
    }

    /**
     * 修改测试套件的用例
     *
     * @param interfaceSuiteCaseRefDO interfaceSuiteCaseRefDO
     */
    @Override
    public void modifySuiteCase(InterfaceSuiteCaseRefDO interfaceSuiteCaseRefDO) {
        interfaceSuiteCaseRefMapper.modifySuiteCase(interfaceSuiteCaseRefDO);
    }

    /**
     * 删除测试套件内的用例
     *
     * @param incrementKey incrementKey
     */
    @Override
    public void removeSuiteCase(Integer incrementKey) {
        interfaceSuiteCaseRefMapper.deleteSuiteCase(incrementKey);
    }

    /**
     * 批量删除测试套件内的用例
     *
     * @param interfaceSuiteCaseRefDO interfaceSuiteCaseRefDO
     */
    @Override
    public void removeSuiteCaseByObject(InterfaceSuiteCaseRefDO interfaceSuiteCaseRefDO) {
        interfaceSuiteCaseRefMapper.deleteSuiteCaseByObject(interfaceSuiteCaseRefDO);
    }

    /**
     * 查看测试套件内用例列表
     *
     * @param interfaceSuiteCaseRefDTO interfaceSuiteCaseRefDTO
     * @param pageNum                  pageNum
     * @param pageSize                 pageSize
     * @return PageInfo<InterfaceSuiteCaseRefVO>
     */
    @Override
    public PageInfo<InterfaceSuiteCaseRefVO> findSuiteCaseList(InterfaceSuiteCaseRefDTO interfaceSuiteCaseRefDTO, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(interfaceSuiteCaseRefMapper.selectSuiteCaseList(interfaceSuiteCaseRefDTO));
    }

    /**
     * 查看测试套件内所有的用例（不分页）
     *
     * @param interfaceSuiteCaseRefDTO interfaceSuiteCaseRefDTO
     * @return List<InterfaceSuiteCaseRefVO>
     */
    @Override
    public List<InterfaceSuiteCaseRefVO> findAllSuiteCase(InterfaceSuiteCaseRefDTO interfaceSuiteCaseRefDTO) {
        return interfaceSuiteCaseRefMapper.selectSuiteCaseList(interfaceSuiteCaseRefDTO);
    }

    /**
     * 执行测试套件
     *
     * @param suiteId 测试套件编号
     * @throws ParseException    ParseException
     * @throws BusinessException BusinessException
     * @throws SqlException      SqlException
     */
    @Override
    public void executeSuiteCaseById(Integer suiteId, String executor) throws ParseException, BusinessException, SqlException {
        // 获取测试套件下所有测试用例
        InterfaceSuiteCaseRefDTO interfaceSuiteCaseRefDTO = new InterfaceSuiteCaseRefDTO();
        interfaceSuiteCaseRefDTO.setSuiteId(suiteId);
        List<InterfaceSuiteCaseRefVO> suiteCaseList = interfaceSuiteCaseRefMapper.selectSuiteCaseList(interfaceSuiteCaseRefDTO);
        // 判断测试套件执行方式 0并行1串行
        InterfaceCaseSuiteVO interfaceCaseSuiteVO = interfaceCaseSuiteMapper.selectInterfaceCaseSuiteById(suiteId);
        Byte type = interfaceCaseSuiteVO.getExecuteType();

        // 写入测试套件执行日志表
        String suiteLogNo = NoUtil.genSuiteLogNo();
        AtomicInteger totalRunCase = new AtomicInteger();
        Integer totalCase = suiteCaseList.size();
        AtomicInteger totalSkip = new AtomicInteger();
        AtomicInteger totalSuccess = new AtomicInteger();
        AtomicInteger totalFailed = new AtomicInteger();
        AtomicInteger totalError = new AtomicInteger();
        Date startTime = new Date();
        long begin = System.currentTimeMillis();
        if (type == 0) { // 异步
            LOG.info("-----------------------开始并行执行测试套件，suiteId={}-----------------------", suiteId);
            suiteCaseList.parallelStream().forEach(suiteCase -> {
                Integer caseId = suiteCase.getCaseId();
                try {
                    // 禁用
                    if (suiteCase.getCaseStatus() == 1) {
                        totalSkip.getAndIncrement();
                    } else {
                        Integer executeLogId = interfaceCaseService.executeInterfaceCase(caseId, executor, suiteLogNo, NoUtil.genChainNo(), suiteId);
                        InterfaceCaseExecuteLogVO interfaceCaseExecuteLogVO = interfaceCaseExecuteLogMapper.selectExecute(executeLogId);
                        Byte status = interfaceCaseExecuteLogVO.getStatus();
                        if (status == 0) {
                            totalSuccess.getAndIncrement();
                        } else if (status == 1) {
                            totalFailed.getAndIncrement();
                        } else {
                            totalError.getAndIncrement();
                        }
                        totalRunCase.getAndIncrement();
                    }
                } catch (Exception e) {
                    LOG.error("并行执行测试套件出现异常，errorMsg={}", ExceptionUtil.msg(e));
                    throw new RuntimeException(e.getMessage());
                }
            });
        } else { // 同步
            LOG.info("-----------------------开始串行执行测试套件，suiteId={}-----------------------", suiteId);
            for (InterfaceSuiteCaseRefVO suiteCase : suiteCaseList) {
                Integer caseId = suiteCase.getCaseId();
                // 禁用
                if (suiteCase.getCaseStatus() == 1) {
                    totalSkip.getAndIncrement();
                } else {
                    Integer executeLogId = interfaceCaseService.executeInterfaceCase(caseId, executor, suiteLogNo, NoUtil.genChainNo(), suiteId);
                    InterfaceCaseExecuteLogVO interfaceCaseExecuteLogVO = interfaceCaseExecuteLogMapper.selectExecute(executeLogId);
                    Byte status = interfaceCaseExecuteLogVO.getStatus();
                    if (status == 0) {
                        totalSuccess.getAndIncrement();
                    } else if (status == 1) {
                        totalFailed.getAndIncrement();
                    } else {
                        totalError.getAndIncrement();
                    }
                    totalRunCase.getAndIncrement();
                }
            }
        }

        // 写入测试套件执行日志表
        long end = System.currentTimeMillis();
        Date endTime = new Date();
        InterfaceSuiteLogDO interfaceSuiteLogDO = new InterfaceSuiteLogDO();
        interfaceSuiteLogDO.setSuiteId(suiteId);
        interfaceSuiteLogDO.setSuiteLogNo(suiteLogNo);
        interfaceSuiteLogDO.setRunTime((end - begin));
        interfaceSuiteLogDO.setTotalSuccess(totalSuccess.intValue());
        interfaceSuiteLogDO.setTotalFailed(totalFailed.intValue());
        interfaceSuiteLogDO.setTotalError(totalError.intValue());
        interfaceSuiteLogDO.setTotalSkip(totalSkip.intValue());
        interfaceSuiteLogDO.setTotalRunCase(totalRunCase.intValue());
        interfaceSuiteLogDO.setTotalCase(totalCase);
        interfaceSuiteLogDO.setStartTime(startTime);
        interfaceSuiteLogDO.setEndTime(endTime);
        interfaceSuiteLogService.saveIfSuiteLog(interfaceSuiteLogDO);
        LOG.info("写入测试套件执行日志表，suiteLogNo={}，success={}，failed={}，error={}, skip={}",
                suiteLogNo, totalSuccess.intValue(), totalFailed.intValue(), totalError.intValue(), totalSkip.intValue());
        LOG.info("-----------------------测试套件执行完成-----------------------");
    }
}

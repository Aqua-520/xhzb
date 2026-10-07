package com.wcy.nursing.service.impl;

import java.util.List;
import com.wcy.common.utils.DateUtils;
import com.wcy.common.utils.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wcy.nursing.mapper.NursingPlanMapper;
import com.wcy.nursing.domain.NursingPlan;
import com.wcy.nursing.domain.dto.NursingPlanDto;
import com.wcy.nursing.domain.vo.NursingPlanVo;
import com.wcy.nursing.service.INursingPlanService;
import com.wcy.system.domain.NursingProjectPlan;
import com.wcy.system.mapper.NursingProjectPlanMapper;

/**
 * 护理计划Service业务层处理
 *
 * @author 汪宸宇
 * @date 2026-10-07
 */
@Service
public class NursingPlanServiceImpl implements INursingPlanService
{
    @Autowired
    private NursingPlanMapper nursingPlanMapper;

    @Autowired
    private NursingProjectPlanMapper nursingProjectPlanMapper;

    /**
     * 查询护理计划（携带关联的护理项目明细列表）
     *
     * @param id 护理计划主键
     * @return 护理计划
     */
    @Override
    public NursingPlanVo selectNursingPlanById(Long id)
    {
        // 1. 查询护理计划
        NursingPlan nursingPlan = nursingPlanMapper.selectNursingPlanById(id);
        if (StringUtils.isNull(nursingPlan))
        {
            return null;
        }
        // 2. 属性拷贝到VO对象
        NursingPlanVo vo = new NursingPlanVo();
        BeanUtils.copyProperties(nursingPlan, vo);
        // 3. 查询护理计划关联的护理项目列表并封装返回
        vo.setProjectPlans(nursingProjectPlanMapper.selectNursingProjectPlanByPlanId(id));
        return vo;
    }

    /**
     * 查询护理计划列表
     *
     * @param nursingPlan 护理计划
     * @return 护理计划
     */
    @Override
    public List<NursingPlan> selectNursingPlanList(NursingPlan nursingPlan)
    {
        return nursingPlanMapper.selectNursingPlanList(nursingPlan);
    }

    /**
     * 新增护理计划（级联保存关联的护理项目明细）
     *
     * @param nursingPlanDto 护理计划
     * @return 结果
     */
    @Override
    @Transactional
    public int insertNursingPlan(NursingPlanDto nursingPlanDto)
    {
        // 1. 保存护理计划，回填主键id
        nursingPlanDto.setCreateTime(DateUtils.getNowDate());
        int rows = nursingPlanMapper.insertNursingPlan(nursingPlanDto);
        // 2. 级联保存关联的护理项目明细
        insertProjectPlans(nursingPlanDto.getId(), nursingPlanDto.getProjectPlans());
        return rows;
    }

    /**
     * 修改护理计划（级联更新关联的护理项目明细）
     *
     * @param nursingPlanDto 护理计划
     * @return 结果
     */
    @Override
    @Transactional
    public int updateNursingPlan(NursingPlanDto nursingPlanDto)
    {
        // 1. 修改护理计划
        nursingPlanDto.setUpdateTime(DateUtils.getNowDate());
        int rows = nursingPlanMapper.updateNursingPlan(nursingPlanDto);
        // 2. 明细列表为null时不触碰关联表（例如仅切换启用/禁用状态）
        if (nursingPlanDto.getProjectPlans() != null)
        {
            // 3. 删除当前护理计划所关联的所有项目明细，再批量插入新的明细
            nursingProjectPlanMapper.deleteNursingProjectPlanByPlanIds(new Long[] { nursingPlanDto.getId() });
            insertProjectPlans(nursingPlanDto.getId(), nursingPlanDto.getProjectPlans());
        }
        return rows;
    }

    /**
     * 批量删除护理计划（级联删除关联的护理项目明细）
     *
     * @param ids 需要删除的护理计划主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteNursingPlanByIds(Long[] ids)
    {
        // 先删除关联的护理项目明细，再删除护理计划
        nursingProjectPlanMapper.deleteNursingProjectPlanByPlanIds(ids);
        return nursingPlanMapper.deleteNursingPlanByIds(ids);
    }

    /**
     * 删除护理计划信息
     *
     * @param id 护理计划主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteNursingPlanById(Long id)
    {
        nursingProjectPlanMapper.deleteNursingProjectPlanByPlanIds(new Long[] { id });
        return nursingPlanMapper.deleteNursingPlanById(id);
    }

    /**
     * 批量插入护理计划关联的项目明细（补上计划id，过滤未选项目的空行）
     */
    private void insertProjectPlans(Long planId, List<NursingProjectPlan> projectPlans)
    {
        if (StringUtils.isNull(planId) || StringUtils.isEmpty(projectPlans))
        {
            return;
        }
        List<NursingProjectPlan> list = projectPlans.stream()
                .filter(item -> StringUtils.isNotNull(item) && StringUtils.isNotNull(item.getProjectId()))
                .peek(item -> item.setPlanId(planId))
                .toList();
        if (StringUtils.isNotEmpty(list))
        {
            nursingProjectPlanMapper.batchNursingProjectPlan(list);
        }
    }
}

package com.wcy.nursing.service;

import java.util.List;
import com.wcy.nursing.domain.NursingPlan;
import com.wcy.nursing.domain.dto.NursingPlanDto;
import com.wcy.nursing.domain.vo.NursingPlanVo;

/**
 * 护理计划Service接口
 *
 * @author 汪宸宇
 * @date 2026-10-07
 */
public interface INursingPlanService
{
    /**
     * 查询护理计划（携带关联的护理项目明细列表）
     *
     * @param id 护理计划主键
     * @return 护理计划
     */
    public NursingPlanVo selectNursingPlanById(Long id);

    /**
     * 查询护理计划列表
     *
     * @param nursingPlan 护理计划
     * @return 护理计划集合
     */
    public List<NursingPlan> selectNursingPlanList(NursingPlan nursingPlan);

    /**
     * 新增护理计划（级联保存关联的护理项目明细）
     *
     * @param nursingPlanDto 护理计划
     * @return 结果
     */
    public int insertNursingPlan(NursingPlanDto nursingPlanDto);

    /**
     * 修改护理计划（级联更新关联的护理项目明细）
     *
     * @param nursingPlanDto 护理计划
     * @return 结果
     */
    public int updateNursingPlan(NursingPlanDto nursingPlanDto);

    /**
     * 批量删除护理计划（级联删除关联的护理项目明细）
     *
     * @param ids 需要删除的护理计划主键集合
     * @return 结果
     */
    public int deleteNursingPlanByIds(Long[] ids);

    /**
     * 删除护理计划信息
     *
     * @param id 护理计划主键
     * @return 结果
     */
    public int deleteNursingPlanById(Long id);
}

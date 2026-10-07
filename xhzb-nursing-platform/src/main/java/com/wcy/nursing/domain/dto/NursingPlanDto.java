package com.wcy.nursing.domain.dto;

import java.util.List;
import com.wcy.nursing.domain.NursingPlan;
import com.wcy.system.domain.NursingProjectPlan;

/**
 * 护理计划请求DTO（新增/修改入参）
 * 在护理计划基础上携带关联的护理项目明细列表
 *
 * @author 汪宸宇
 * @date 2026-10-07
 */
public class NursingPlanDto extends NursingPlan
{
    private static final long serialVersionUID = 1L;

    /** 护理计划关联的项目明细列表 */
    private List<NursingProjectPlan> projectPlans;

    public List<NursingProjectPlan> getProjectPlans()
    {
        return projectPlans;
    }

    public void setProjectPlans(List<NursingProjectPlan> projectPlans)
    {
        this.projectPlans = projectPlans;
    }
}

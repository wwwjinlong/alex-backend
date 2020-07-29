package org.alex.platform.controller;

import org.alex.platform.common.Result;
import org.alex.platform.pojo.Project;
import org.alex.platform.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProjectController {

    @Autowired
    ProjectService projectService;

    private static final Logger LOG = LoggerFactory.getLogger(ProjectController.class);

    /**
     * 查询项目列表
     * @param project
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("project/list")
    public Result getProjectList(Project project, Integer pageNum, Integer pageSize){
        Integer num = pageNum==null?1:pageNum;
        Integer size = pageSize==null?10:pageSize;
        return Result.success(projectService.findProjectList(project, num, size));
    }

    /**
     * 查看项目信息
     * @param project
     * @return
     */
    @GetMapping("project/info")
    public Result getProject(Project project){
        Integer projectId = project.getProjectId();
        String projectName = project.getName();
        Project p = new Project();
        p.setProjectId(projectId);
        p.setName(projectName);
        return Result.success(projectService.findProject(project));
    }

    /**
     * 新增项目
     * @param project
     * @return
     */
    @PostMapping("project/save")
    public Result saveProject(@Validated Project project){
        try {
            projectService.saveProject(project);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(e.getMessage());
            return Result.fail(e.getMessage());
        }
        return Result.success("新增成功");
    }

    /**
     * 修改项目
     * @param project
     * @return
     */
    @PostMapping("project/update")
    public Result modifyProject(@Validated Project project){
        try {
            projectService.modifyProject(project);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(e.getMessage());
            return Result.fail(e.getMessage());
        }
        return Result.success("修改成功");
    }

    /**
     * 删除项目
     * @param projectId 项目编号
     * @return
     */
    @GetMapping("project/delete/{projectId}")
    public Result deleteProject(@PathVariable Integer projectId){
        projectService.removeProjectById(projectId);
        return Result.success("删除成功");
    }
}
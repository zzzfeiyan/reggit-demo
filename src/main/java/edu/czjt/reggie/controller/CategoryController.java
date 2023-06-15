package edu.czjt.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.czjt.reggie.common.R;
import edu.czjt.reggie.entity.Category;
import edu.czjt.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理
 */
@RestController  // 标识该类是一个控制器
@RequestMapping("/category")  // 定义控制器的根路径
@Slf4j  // 引入日志注解
public class CategoryController {

    @Autowired
    private CategoryService categoryService;  // 自动注入CategoryService


    /**
     * 新增分类
     * @param category
     * @return
     */
    @PostMapping  // 处理HTTP的POST请求
    public R<String> save(@RequestBody Category category){
        log.info("category:{}",category);  // 打印日志，输出category对象的内容
        categoryService.save(category);  // 调用categoryService的save方法保存分类记录
        return R.success("新增分类成功");  // 返回成功的响应，并携带成功的消息
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")  // 处理HTTP的GET请求，路径为"/category/page"
    public R<Page> page(int page,int pageSize){
        // 分页构造器
        Page<Category> pageInfo = new Page<>(page,pageSize);
        // 条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 添加排序条件，根据sort进行排序
        queryWrapper.orderByAsc(Category::getSort);

        // 分页查询
        categoryService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);  // 返回成功的响应，并携带查询到的分页结果
    }

    /**
     * 根据id删除分类
     * @param id
     * @return
     */
    @DeleteMapping  // 处理HTTP的DELETE请求
    public R<String> delete(Long id){
        log.info("删除分类，id为：{}",id);  // 打印日志，输出要删除的分类的ID

        categoryService.removeById(id);  // 调用categoryService的removeById方法根据ID删除分类记录

        return R.success("分类信息删除成功");  // 返回成功的响应，并携带成功的消息
    }

    /**
     * 根据id修改分类信息
     * @param category
     * @return
     */
    @PutMapping  // 处理HTTP的PUT请求
    public R<String> update(@RequestBody Category category){
        log.info("修改分类信息：{}",category);  // 打印日志，输出要修改的分类信息

        categoryService.updateById(category);  // 调用categoryService的updateById方法根据ID修改分类信息

        return R.success("修改分类信息成功");  // 返回成功的响应，并携带成功的消息
    }

    /**
     * 根据条件查询分类数据
     * @param category
     * @return
     */
    @GetMapping("/list")  // 处理HTTP的GET请求，路径为"/category/list"
    public R<List<Category>> list(Category category){
        // 条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 添加条件
        queryWrapper.eq(category.getType() != null,Category::getType,category.getType());
        // 添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);  // 根据条件查询分类数据
        return R.success(list);  // 返回成功的响应，并携带查询到的分类数据列表
    }
}

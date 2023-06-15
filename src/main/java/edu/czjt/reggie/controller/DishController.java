package edu.czjt.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.czjt.reggie.common.R;
import edu.czjt.reggie.dto.DishDto;
import edu.czjt.reggie.entity.Category;
import edu.czjt.reggie.entity.Dish;
import edu.czjt.reggie.entity.DishFlavor;
import edu.czjt.reggie.service.CategoryService;
import edu.czjt.reggie.service.DishFlavorService;
import edu.czjt.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by jinkun.tian on 2023/4/27
 */
@RestController  // 标识该类是一个控制器
@RequestMapping("/dish")  // 定义控制器的根路径
@Slf4j  // 引入日志注解
public class DishController {

    @Autowired
    private DishService dishService;  // 自动注入DishService
    @Autowired
    private DishFlavorService dishFlavorService;  // 自动注入DishFlavorService
    @Autowired
    private CategoryService categoryService;  // 自动注入CategoryService

    @GetMapping("/list")  // 处理HTTP的GET请求，路径为"/dish/list"
    public R<List<DishDto>> list(Dish dish) {
        // 根据分类查询菜品，并且为起售的菜品
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus, 1);
        List<Dish> dishes = dishService.list(queryWrapper);

        // 遍历dishes，创建List<DishDto>
        List<DishDto> dishDtos = dishes.stream().map((item) -> {
            // 创建DishDto
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            // 补充CategoryName
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }

            // 补充List<DishFlavor>
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, item.getId());
            List<DishFlavor> flavors = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
            dishDto.setFlavors(flavors);

            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtos);  // 返回成功的响应，并携带查询到的菜品数据列表
    }

    @GetMapping("/{id}")  // 处理HTTP的GET请求，路径为"/dish/{id}"
    public R<DishDto> get(@PathVariable Long id) {
        log.debug("通过ID：{} 获取菜品", id);
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);  // 返回成功的响应，并携带查询到的菜品数据
    }

    @GetMapping("/page")  // 处理HTTP的GET请求，路径为"/dish/page"
    public R<Page> page(int page, int pageSize, String name) {
        // 构建分页构造器对象
        Page<Dish> dishPage = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>(page, pageSize);

        // 条件构造器
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.like(name != null, Dish::getName, name);
        dishLambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);

        // 分页查询
        dishService.page(dishPage, dishLambdaQueryWrapper);

        // 将分页信息拷贝到dishDtoPage
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");

        // 将dishPage的record转为dishDtoPage的record
        List<Dish> records = dishPage.getRecords();

        List<DishDto> dishDtoList = records.stream().map((item) -> {
            return dish2dishDto(item);
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(dishDtoList);

        return R.success(dishDtoPage);  // 返回成功的响应，并携带查询到的菜品分页数据
    }


    /**
     * 将dish转化为dishDto
     *
     * @param dish
     * @return
     */
    private DishDto dish2dishDto(Dish dish) {
        DishDto dishDto = new DishDto();

        BeanUtils.copyProperties(dish, dishDto);

        Category category = categoryService.getById(dish.getCategoryId());

        if (category != null) {
            dishDto.setCategoryName(category.getName());
        }

        List<DishFlavor> dishFlavors = dishFlavorService.getFlavorsByDishId(dish.getId());

        dishDto.setFlavors(dishFlavors);

        return dishDto;
    }

    @PostMapping()  // 处理HTTP的POST请求，路径为"/dish"
    public R<String> save(@RequestBody DishDto dishDto) {
        log.debug("保存dish: {}", dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");  // 返回成功的响应
    }

    @PutMapping("/status/{status}")  // 处理HTTP的PUT请求，路径为"/dish/status/{status}"
    public R<String> status(@PathVariable("status") Integer status, @RequestBody String ids) {
        log.debug("修改菜品ids:{} 的状态为：{}。", ids, status);

        List<Long> idList = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        UpdateWrapper<Dish> dishUpdateWrapper = new UpdateWrapper<>();
        dishUpdateWrapper.in("id", idList).set("status", status);

        dishService.update(dishUpdateWrapper);
        return R.success("更新成功");  // 返回成功的响应
    }

    @PutMapping()  // 处理HTTP的PUT请求，路径为"/dish"
    @Transactional  // 声明事务
    public R<String> update(@RequestBody DishDto dishDto) {
        log.debug("更新dishDto:{}", dishDto.toString());

        dishService.updateById(dishDto);

        // 按照dishid删除dishflavor
        dishFlavorService.removeByDishId(dishDto.getId());

        List<DishFlavor> dishFlavors = dishDto.getFlavors().stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(dishFlavors);

        // dishFlavorService.updateBatchById(dishFlavors);

        return R.success("更新成功");  // 返回成功的响应
    }

    @DeleteMapping()  // 处理HTTP的DELETE请求，路径为"/dish"
    @Transactional  // 声明事务
    public R<String> delete(@RequestParam("ids") String ids) {
        log.info("删除菜名ids：{}", ids);
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(item->{
                    Long id = Long.valueOf(item);
                    dishFlavorService.removeByDishId(id);
                    dishService.removeById(id);
                    return id;
                })
                .collect(Collectors.toList());
        return R.success("删除菜品成功。");  // 返回成功的响应
    }

}

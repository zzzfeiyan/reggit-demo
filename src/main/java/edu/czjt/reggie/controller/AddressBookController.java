package edu.czjt.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import edu.czjt.reggie.common.BaseContext;
import edu.czjt.reggie.common.R;
import edu.czjt.reggie.entity.AddressBook;
import edu.czjt.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址簿管理
 */
@Slf4j  // 引入日志注解
@RestController  // 标识该类是一个控制器
@RequestMapping("/addressBook")  // 定义控制器的根路径
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;  // 自动注入AddressBookService

    /**
     * 新增
     */
    @PostMapping  // 处理HTTP的POST请求
    public R<AddressBook> save(@RequestBody AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());  // 设置addressBook对象的userId属性为当前用户的ID
        log.info("addressBook:{}", addressBook);  // 打印日志，输出addressBook对象的内容
        addressBookService.save(addressBook);  // 调用addressBookService的save方法保存地址簿记录
        return R.success(addressBook);  // 返回成功的响应，并携带保存后的addressBook对象
    }

    /**
     * 设置默认地址
     */
    @PutMapping("default")  // 处理HTTP的PUT请求，路径为"/addressBook/default"
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
        log.info("addressBook:{}", addressBook);  // 打印日志，输出addressBook对象的内容
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();  // 创建LambdaUpdateWrapper对象
        wrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());  // 设置更新条件，即AddressBook对象的userId属性等于当前用户的ID
        wrapper.set(AddressBook::getIsDefault, 0);  // 设置更新操作，将AddressBook对象的isDefault属性设置为0，即将之前的默认地址取消
        // SQL:update address_book set is_default = 0 where user_id = ?
        addressBookService.update(wrapper);  // 调用addressBookService的update方法执行更新操作


        addressBook.setIsDe
    fault(1);  // 将当前地址簿对象的isDefault属性设置为1，即设置为默认地址
        // SQL:update address_book set is_default = 1 where id = ?
        addressBookService.updateById(addressBook);  // 调用addressBookService的updateById方法更新当前地址簿对象
        return R.success(addressBook);  // 返回成功的响应，并携带更新后的addressBook对象
    }

    /**
     * 根据id查询地址
     */
    @GetMapping("/{id}")  // 处理HTTP的GET请求，路径为"/addressBook/{id}"
    public R get(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);  // 根据id查询地址簿记录
        if (addressBook != null) {  // 如果查询到地址簿记录
            return R.success(addressBook);  // 返回成功的响应，并携带查询到的addressBook对象
        } else {
            return R.error("没有找到该对象");  // 返回错误的响应，提示未找到对象
        }
    }

    /**
     * 查询默认地址
     */
    @GetMapping("default")  // 处理HTTP的GET请求，路径为"/addressBook/default"
    public R<AddressBook> getDefault() {
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();  // 创建LambdaQueryWrapper对象
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());  // 设置查询条件，即AddressBook对象的userId属性等于当前用户的ID
        queryWrapper.eq(AddressBook::getIsDefault, 1);  // 设置查询条件，即AddressBook对象的isDefault属性为1

        // SQL:select * from address_book where user_id = ? and is_default = 1
        AddressBook addressBook = addressBookService.getOne(queryWrapper);  // 查询满足条件的一条地址簿记录

        if (addressBook != null) {  // 如果查询到地址簿记录
            return R.success(addressBook);  // 返回成功的响应，并携带查询到的addressBook对象
        } else {
            return R.error("没有找到该对象");  // 返回错误的响应，提示未找到对象
        }
    }

    /**
     * 查询指定用户的全部地址
     */
    @GetMapping("/list")  // 处理HTTP的GET请求，路径为"/addressBook/list"
    public R<List<AddressBook>> list(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());  // 设置addressBook对象的userId属性为当前用户的ID
        log.info("addressBook:{}", addressBook);  // 打印日志，输出addressBook对象的内容

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();  // 创建LambdaQueryWrapper对象
        queryWrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, addressBook.getUserId());  // 设置查询条件，即AddressBook对象的userId属性等于当前用户的ID
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);  // 设置查询结果的排序方式，按照更新时间降序排列

        // SQL:select * from address_book where user_id = ? order by update_time desc
        return R.success(addressBookService.list(queryWrapper));  // 返回成功的响应，并携带查询到的addressBook对象列表
    }
}

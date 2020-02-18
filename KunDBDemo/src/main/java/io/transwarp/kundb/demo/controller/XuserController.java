package io.transwarp.kundb.demo.controller;

import java.util.List;
import java.util.Map;

import io.transwarp.kundb.demo.entity.Xuser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.transwarp.kundb.demo.Mapper.XuserMapper;

import javax.sql.DataSource;

@RestController
public class XuserController {
    private Logger logger = LoggerFactory.getLogger(XuserController.class);
	@Autowired
    private XuserMapper xuserMapper;
	@Autowired
    private DataSource dataSource;
	
	@RequestMapping("getUsers")
    public List<Xuser> getUsers() {
        List<Xuser> users = xuserMapper.getAll();
        return users;
    }
     
    @RequestMapping("getUser")
    public Xuser getUser(Long id) {
    	Xuser user = xuserMapper.getOne(id);
//    	logger.info(dataSource.getConnection().)
        return user;
    }
     
    @RequestMapping("add")
    public Xuser save(Xuser user) {
        xuserMapper.insert(user);
        logger.info("add user result is:{}", user.getId());
        return user;
    }
     
    @RequestMapping(value="update")
    public boolean update(Xuser user) {
	    if (null == user.getId() || user.getId() < 0) {
            logger.error("userid must be request");
	        return false;
        }
        boolean updateResult = xuserMapper.update(user);
        logger.info("update user:{} result:{}", user.getId(), updateResult);
        return updateResult;
    }
     
    @RequestMapping(value="delete/{id}")
    public int delete(@PathVariable("id") Long id) {
        int deleteresult = xuserMapper.delete(id);
        logger.info("delete user result:{}", deleteresult);
        return deleteresult;
    }
    
    @RequestMapping(value = "hello", method = RequestMethod.GET)
    public String hello(Map<String,Object> map){   
    return "hello";
    }
}

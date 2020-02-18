package io.transwarp.kundb.demo.Mapper;

import java.util.List;

import io.transwarp.kundb.demo.entity.Xuser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface XuserMapper {
	List<Xuser> getAll();
    
    Xuser getOne(Long id);
 
    int insert(Xuser user);
 
    boolean update(Xuser user);
 
    int delete(Long id);
    
    @Options()
    @Select(value = "select * from ")
    void test(int id);
    
}

//操作user的接口
//id， username, email , password
//按username Email查询




package com.heritage.platform.repository;
import com.heritage.platform.model.HeritageUser;
import org.springframework.data.jpa.repository.JpaRepository;
//import java.util.Optional;

public interface HeritageUserRepository extends JpaRepository<HeritageUser, Long> {
    HeritageUser findByUsername(String username);
    HeritageUser findByEmail(String email);    

    //pbi4
    HeritageUser findByResetToken(String resetToken);


    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
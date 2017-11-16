import java.io.IOException;
import java.io.Reader;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class InMybatis {
    private static SqlSessionFactory sqlSessionFactory;
    private static Reader reader;
    static {
        try {
            reader = Resources.getResourceAsReader("Configuration.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SqlSessionFactory getSession() {
        return sqlSessionFactory;
    }

    public static void main(String[] args) throws IOException {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            User user = (User) session.selectOne("UserMapper.selectUserByID", new Integer(1));
            System.out.println(user.getId());
            System.out.println(user.getUserName());
            System.out.println(user.getUserAge());
            System.out.println(user.getUserAddress());
        } finally {
            session.close();
        }
    }
}
package org.alex.platform.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import org.alex.platform.exception.SqlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JdbcUtil {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcUtil.class);

    private JdbcUtil() {

    }

    /**
     * 数据库预检
     * @param url 数据库连接地址
     * @param username 用户名
     * @param password 密码
     * @return 是否连接
     */
    public static String checkJdbcConnection(String url, String username, String password) {
        String msg = "连接成功";
        DruidDataSource ds = new DruidDataSource();
        try {
            WallFilter wallFilter = new WallFilter();
            WallConfig wallConfig = new WallConfig();
            // 配置仅允许查询
            wallConfig.setDeleteAllow(false);
            wallConfig.setUpdateAllow(false);
            wallConfig.setInsertAllow(false);
            wallConfig.setDropTableAllow(false);
            wallConfig.setAlterTableAllow(false);

            // 将配置加入过滤器
            wallFilter.setConfig(wallConfig);
            List wallFilters = new ArrayList<WallFilter>();
            wallFilters.add(wallFilter);

            // 添加过滤器
            ds.setProxyFilters(wallFilters);
            ds.setUsername(username);
            ds.setFailFast(true);
            ds.setConnectionErrorRetryAttempts(3);
            ds.setPassword(password);
            ds.setUrl(url);
            // 执行一条sql才会检查连接是否出现异常
            JdbcTemplate jdbc = new JdbcTemplate(ds);
            jdbc.queryForList("select 1");
        } catch (Exception e) {
            ds.close();
            msg = "连接失败，" + e.getMessage();
            LOG.error("JDBC TEMPLATE 连接失败， errorMsg={}", ExceptionUtil.msg(e));
        }
        return msg;
    }

    /**
     * @param url 数据库url
     * @param username 连接用户名
     * @param password 连接密码
     * @return spring template
     */
    public static JdbcTemplate getInstance(String url, String username, String password){
        DruidDataSource ds = new DruidDataSource();
        WallFilter wallFilter = new WallFilter();
        WallConfig wallConfig = new WallConfig();
        // 配置仅允许查询
        wallConfig.setDeleteAllow(false);
        wallConfig.setUpdateAllow(false);
        wallConfig.setInsertAllow(false);
        wallConfig.setDropTableAllow(false);
        wallConfig.setAlterTableAllow(false);

        // 将配置加入过滤器
        wallFilter.setConfig(wallConfig);
        List wallFilters = new ArrayList<WallFilter>();
        wallFilters.add(wallFilter);

        // 添加过滤器
        ds.setProxyFilters(wallFilters);
        ds.setUsername(username);
        ds.setFailFast(true);
        ds.setConnectionErrorRetryAttempts(3);
        ds.setPassword(password);
        ds.setUrl(url);
        return new JdbcTemplate(ds);
    }

    /**
     * 查询首行首列
     * @param url 数据库url
     * @param username 连接用户名
     * @param password 连接密码
     * @param sql sql预计
     * @return 查询首行首列
     */
    public static String selectFirstColumn(String url, String username, String password, String sql) throws SqlException {
        String resultStr = "";
        try {
            resultStr = "";
            JdbcTemplate jdbcTemplate = getInstance(url, username, password);
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
            if (list.isEmpty()) {
                throw new SqlException("查询结果为空");
            }
            Map result = list.get(0);
            for (Object key : result.keySet()) {
                resultStr = result.get(key).toString();
                break;
            }
        } catch (DataAccessException e) {
            LOG.error("JDBC TEMPLATE 连接失败， errorMsg={}", ExceptionUtil.msg(e));
            throw new SqlException("数据库连接异常/SQL语句错误/非查询语句/查询结果为空");
        }
        return resultStr;
    }

    /**
     * 查询首行首列
     * @param jdbcTemplate spring template
     * @param sql 查询语句
     * @return 查询首行首列
     */
    public static String selectFirstColumn(JdbcTemplate jdbcTemplate, String sql) throws SqlException {
        String resultStr = "";
        try {
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
            if (list.isEmpty()) {
                throw new SqlException("查询结果为空");
            }
            Map result = list.get(0);
            for (Object key : result.keySet()) {
                resultStr = result.get(key).toString();
                break;
            }
        } catch (DataAccessException e) {
            LOG.error("JDBC TEMPLATE 连接失败， errorMsg={}", ExceptionUtil.msg(e));
            throw new SqlException("数据库连接异常/SQL语句错误/非查询语句/查询结果为空");
        }
        return resultStr;
    }

    /**
     * 查询首行首列，带参数
     * @param url 数据库url
     * @param username 连接用户名
     * @param password 连接密码
     * @param sql sql预计
     * @param params 参数
     * @return 查询结果
     * @throws SqlException 数据库异常
     */
    public static String selectFirstColumn(String url, String username, String password, String sql, String[] params) throws SqlException {
        String resultStr = "";
        try {
            resultStr = "";
            JdbcTemplate jdbcTemplate = getInstance(url, username, password);
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, params);
            if (list.isEmpty()) {
                throw new SqlException("查询结果为空");
            }
            Map result = list.get(0);
            for (Object key : result.keySet()) {
                resultStr = result.get(key).toString();
                break;
            }
        } catch (DataAccessException e) {
            LOG.error("JDBC TEMPLATE 连接失败， errorMsg={}", ExceptionUtil.msg(e));
            throw new SqlException("数据库连接异常/SQL语句错误/非查询语句/查询结果为空");
        }
        return resultStr;
    }


}

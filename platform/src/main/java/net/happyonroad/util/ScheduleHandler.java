/**
 * Developer: Kadvin Date: 14/12/22 下午8:59
 */
package net.happyonroad.util;

import net.happyonroad.support.JsonSupport;
import net.happyonroad.type.Schedule;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Schedule Mybatis Handler
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
public class ScheduleHandler implements TypeHandler<Schedule> {
    @Override
    public void setParameter(PreparedStatement ps, int i, Schedule parameter, JdbcType jdbcType) throws SQLException {
        if( parameter == null )
            ps.setString(i, null);
        else{
            ps.setString(i, JsonSupport.toJSONString(parameter));
        }
    }

    @Override
    public Schedule getResult(ResultSet rs, String columnName) throws SQLException {
        String raw = rs.getString(columnName);
        return stringToSchedule(raw);
    }

    @Override
    public Schedule getResult(ResultSet rs, int columnIndex) throws SQLException {
        String raw = rs.getString(columnIndex);
        return stringToSchedule(raw);
    }

    @Override
    public Schedule getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String raw = cs.getString(columnIndex);
        return stringToSchedule(raw);
    }

    private Schedule stringToSchedule(String raw) {
        if( raw != null ){
            return JsonSupport.parseJson(raw, Schedule.class);
        }
        return null;
    }
}